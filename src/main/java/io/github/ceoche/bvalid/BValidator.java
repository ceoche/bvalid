/*
 * Copyright 2022 Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * ou may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ceoche.bvalid;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The {@link BValidator} provides method to validate business rules and members of POJO business
 * objects as long as they are annotated with {@link BusinessObject}, {@link BusinessRule} and
 * {@link BusinessMember}.
 *
 * @author ceoche
 */
public class BValidator {

   /**
    * Verify if an object annotated with {@link BusinessObject} is valid by running business
    * rules tests methods annotated with {@link BusinessRule} and by validating all nested
    * {@link BusinessObject} accessible through methods annotated with {@link BusinessMember}.
    * The validation will test all business rules and store the results in an
    * {@link ObjectResult}.
    *
    * @param object business object to validate.
    * @return an {@link ObjectResult} that hold all the business rule and member results.
    * @throws IllegalBusinessObjectException if the object is not an {@link BusinessObject}, or
    *                                        if it has no {@link BusinessRule} nor
    *                                        {@link BusinessMember} public methods. If one of the
    *                                        methods annotated with {@link BusinessRule} have
    *                                        more than zero parameters or does not return a
    *                                        boolean value. Or if one of the methods annotated with
    *                                        {@link BusinessMember} have more than zero parameters
    *                                        or does not return a {@link BusinessObject} (or a
    *                                        collection/array of it).
    * @throws InvocationException            if an exception is raised while invoking a
    *                                        {@link BusinessRule} or a {@link BusinessMember}
    *                                        method. The original exception will be wrapped as
    *                                        cause.
    * @throws NullPointerException           if the given object is null.
    */
   public ObjectResult validate(final Object object) {
      return validate(object, getBusinessObjectName(assertBusinessObjectClass(object)));
   }

   /**
    * Verify if a collection of objects annotated with {@link BusinessObject} are valid by
    * running business rules tests methods annotated with {@link BusinessRule} and by validating
    * all nested {@link BusinessObject} accessible through methods annotated with
    * {@link BusinessMember}. For each element of the collection, the validation will test all
    * business rules and store the results in an {@link ObjectResult}.
    *
    * @param objects business objects to validate.
    * @return a list of {@link ObjectResult}, one for each object.
    * @throws IllegalBusinessObjectException if one of the object is not an {@link BusinessObject},
    *                                        or if it has no {@link BusinessRule} nor
    *                                        {@link BusinessMember} public methods. If one of
    *                                        the methods annotated with {@link BusinessRule} have
    *                                        more than zero parameters or does not return a
    *                                        boolean value. Or if one of the methods annotated
    *                                        with {@link BusinessMember} have more than zero
    *                                        parameters or does not return a {@link BusinessObject}
    *                                        (or a collection/array of it).
    * @throws InvocationException            if an exception is raised while invoking a
    *                                        {@link BusinessRule} or a {@link BusinessMember}
    *                                        method. The original exception will be wrapped as
    *                                        cause.
    * @throws NullPointerException           if the given object is null.
    */
   public List<ObjectResult> validate(final Collection<?> objects) {
      return validate(objects, objects.getClass().getSimpleName());
   }

   /**
    * Verify if an array of objects annotated with {@link BusinessObject} are valid by running
    * business rules tests methods annotated with {@link BusinessRule} and by validating all nested
    * {@link BusinessObject} accessible through methods annotated with {@link BusinessMember}.
    * For each element of the array, the validation will test all business rules and store the
    * results in an {@link ObjectResult}.
    *
    * @param objects business objects to validate.
    * @return a list of {@link ObjectResult}, one for each object.
    * @throws IllegalBusinessObjectException if one of the object is not an {@link BusinessObject},
    *                                        or if it has no {@link BusinessRule} nor
    *                                        {@link BusinessMember} public methods. If one of
    *                                        the methods annotated with {@link BusinessRule} have
    *                                        more than zero parameters or does not return a
    *                                        boolean value. Or if one of the methods annotated
    *                                        with {@link BusinessMember} have more than zero
    *                                        parameters or does not return a {@link BusinessObject}
    *                                        (or a collection/array of it).
    * @throws InvocationException            if an exception is raised while invoking a
    *                                        {@link BusinessRule} or a {@link BusinessMember}
    *                                        method. The original exception will be wrapped as
    *                                        cause.
    * @throws NullPointerException           if the given object is null.
    */
   public List<ObjectResult> validate(final Object[] objects) {
      return validate(objects, objects.getClass().getSimpleName());
   }

   private List<ObjectResult> validateAnyMember(final Object object, String name) {
      List<ObjectResult> objectResults = new ArrayList<>();
      Class<?> objectClass = object.getClass();
      if (isCollection(objectClass)) {
         objectResults.addAll(validate((Collection<?>) object, name));
      } else if (objectClass.isArray()) {
         objectResults.addAll(validate((Object[]) object, name));
      } else {
         objectResults.add(validate(object, name));
      }
      return objectResults;
   }

   private boolean isCollection(Class<?> objectClass) {
      return Collection.class.isAssignableFrom(objectClass);
   }

   private List<ObjectResult> validate(Collection<?> objects, String name) {
      return validate(objects.toArray(), name);
   }

   private List<ObjectResult> validate(Object[] objects, String name) {
      List<ObjectResult> results = new ArrayList<>();
      int index = -1;
      for (Object object : objects) {
         if (object != null) {
            results.add(validate(object, name + "[" + ++index + "]"));
         }
      }
      return results;
   }

   private ObjectResult validate(final Object object, final String name) {
      ObjectResult objectResult = new ObjectResult(name);
      Class<?> clazz = assertBusinessObjectClass(object);
      List<Method> businessRules = getBusinessRuleMethods(clazz);
      List<Method> businessMembers = getBusinessMemberMethods(clazz);
      if (!businessRules.isEmpty() || !businessMembers.isEmpty()) {
         objectResult.addRuleResults(validateBusinessRules(object, businessRules));
         objectResult.addMemberResults(validateBusinessMembers(object, businessMembers));
      } else {
         throw new IllegalBusinessObjectException(
               "The class " + clazz.getCanonicalName() + " annotated with @BusinessObject does " +
                     "not have any public  @BusinessRule nor any public @BusinessMember methods " +
                     "to verify.");
      }
      return objectResult;
   }

   /**
    * Perform validation of methods marked with {@link BusinessRule}. It will also validate
    * inherited {@link BusinessRule} methods.
    *
    * @param object        object to validate
    * @param businessRules business rule methods to run.
    */
   private List<RuleResult> validateBusinessRules(Object object, List<Method> businessRules) {
      List<RuleResult> RuleResults = new ArrayList<>();
      for (Method businessRuleMethod : businessRules) {
         try {
            RuleResults.add(new RuleResult(
                  getBusinessRuleId(businessRuleMethod),
                  getBusinessRuleDescription(businessRuleMethod),
                  (Boolean) businessRuleMethod.invoke(object)
            ));
         } catch (IllegalAccessException | IllegalArgumentException | ClassCastException e) {
            throw new IllegalBusinessObjectException(String.format(
                  "Method '%s' of class '%s' does not respect BusinessRule method format (should " +
                        "be  public with no arguments and return a boolean value).",
                  businessRuleMethod.getName(),
                  object.getClass().getCanonicalName()), e);
         } catch (InvocationTargetException e) {
            throw new InvocationException(e.getCause());
         }
      }
      return RuleResults;
   }

   private List<ObjectResult> validateBusinessMembers(Object object, List<Method> businessMembers) {
      List<ObjectResult> membersResults = new ArrayList<>();
      for (Method businessMember : businessMembers) {
         try {
            Object member = businessMember.invoke(object);
            if (member != null) {
               //Recursive call
               membersResults.addAll(validateAnyMember(member, getMemberName(businessMember)));
            }
         } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalBusinessObjectException(
                  "Method '" + businessMember.getName() + "' does not respect BusinessMember " +
                        "method format (should be public with no arguments and return an object " +
                        "value that is a BusinessObject or a group of BusinessObject).", e);
         } catch (InvocationTargetException e) {
            throw new InvocationException(e.getCause());
         }
      }
      return membersResults;
   }

   private String getBusinessObjectName(Class<?> clazz) {
      BusinessObject annotation = clazz.getAnnotation(BusinessObject.class);
      return annotation == null || annotation.name().isEmpty() ? clazz.getSimpleName() :
            annotation.name();
   }

   private String getBusinessRuleDescription(Method businessRule) {
      return businessRule.getAnnotation(BusinessRule.class).description();
   }

   private String getBusinessRuleId(Method businessRule) {
      return businessRule.getAnnotation(BusinessRule.class).id();
   }

   private String getMemberName(Method businessMember) {
      BusinessMember annotation = businessMember.getAnnotation(BusinessMember.class);
      return annotation.name().isEmpty() ? businessMember.getName() : annotation.name();
   }

   private List<Method> getBusinessRuleMethods(Class<?> clazz) {
      return getAnnotatedPublicMethod(clazz, BusinessRule.class);
   }

   private List<Method> getBusinessMemberMethods(Class<?> clazz) {
      return getAnnotatedPublicMethod(clazz, BusinessMember.class);
   }

   private List<Method> getAnnotatedPublicMethod(Class<?> clazz,
                                                 Class<? extends Annotation> annotation) {
      List<Method> annotatedPublicMethods = new ArrayList<>();
      for (Method method : clazz.getMethods()) {
         if (method.isAnnotationPresent(annotation) && method.getModifiers() == Modifier.PUBLIC) {
            annotatedPublicMethods.add(method);
         }
      }
      return annotatedPublicMethods;
   }

   private Class<?> assertBusinessObjectClass(Object object) {
      Class<?> clazz = object.getClass();
      if (isBusinessObject(clazz) || hasASuperClassBusinessObject(clazz.getSuperclass())) {
         return clazz;
      } else {
         throw new IllegalBusinessObjectException("The object's class " + clazz.getCanonicalName()
               + " is not annotated with @BusinessObject.");
      }
   }

   private boolean isBusinessObject(Class<?> clazz) {
      return clazz.isAnnotationPresent(BusinessObject.class);
   }

   private boolean hasASuperClassBusinessObject(Class<?> superClass) {
      if (isOnTopClassHierarchy(superClass)) {
         return false;
      } else {
         if (isBusinessObject(superClass)) {
            return true;
         } else {
            return hasASuperClassBusinessObject(superClass.getSuperclass());
         }
      }
   }

   private boolean isOnTopClassHierarchy(Class<?> superClass) {
      return superClass.equals(Object.class);
   }
}
