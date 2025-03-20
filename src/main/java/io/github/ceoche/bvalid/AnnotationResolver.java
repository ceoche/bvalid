/*
 * Copyright 2022-2023 Cédric Eoche-Duval
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Build a {@link BValidator} from a {@link BusinessObject} annotated class.
 *
 * @param <T> type of the root object to create a validator for.
 *
 * @author Achraf Achkari
 */
public class AnnotationResolver<T> {

   private final Class<T> objectClass;

   /**
    * Construct a new {@link AnnotationResolver} for the given business object class.
    *
    * @param objectClass the class to resolve.
    *
    * @throws IllegalBusinessObjectException if the class is neither annotated with {@link BusinessObject} nor any of
    *                                        its super-class.
    */
   public AnnotationResolver(Class<T> objectClass) {
      this.objectClass = (Class<T>) assertBusinessObjectClass(objectClass);
   }

   /**
    * Construct and initialize a {@link BValidatorBuilder} from a class annotated with {@link BusinessObject}.
    * <p>
    * All assertions and Members identified with {@link BusinessAssertion} and {@link BusinessMember} annotations are
    * automatically added to the builder.
    *
    * @return the {@link BValidatorBuilder} initialized.
    *
    * @throws TypeResolutionException if the resolver fails to resolve types of members.
    */
   public BValidatorBuilder<T> getBuilder() {
      return new BValidatorBuilder<T>(objectClass)
            .setBusinessObjectName(getObjectName(objectClass))
            .addAllRules(getRules(objectClass))
            .addAllMembers(getMembers(objectClass));
   }

   /**
    * Shortcut to construct the builder and immediatly build the {@link BValidator}.
    *
    * @return the {@link BValidator} intialized with assertions and members defined in the builder.
    *
    * @throws TypeResolutionException        if the resolver fails to resolve types of members.
    * @throws IllegalStateException          if one of the member is not a validable business object.
    * @throws IllegalBusinessObjectException if a business object does not have any rules nor members.
    */
   public BValidator<T> buildValidator() {
      return getBuilder().build();
   }

   private String getObjectName(Class<T> clazz) {
      BusinessObject businessObject = clazz.getAnnotation(BusinessObject.class);
      if (businessObject != null && businessObject.name() != null && !businessObject.name().isBlank()) {
         return businessObject.name();
      } else {
         return clazz.getSimpleName();
      }
   }

   private Set<BAssertion<? super T>> getRules(Class<T> clazz) {
      Set<BAssertion<? super T>> rulesResult = new LinkedHashSet<>();
      for (Method method : clazz.getMethods()) {
         if (method.isAnnotationPresent(BusinessAssertion.class)) {
            BusinessAssertion businessAssertion = method.getAnnotation(BusinessAssertion.class);
            rulesResult.add(
                  new BAssertion<>(businessAssertion.id(), getPredicate(method), businessAssertion.description()));
         }
      }
      return rulesResult;
   }

   private Set<BusinessMemberBuilder<? super T, ?>> getMembers(Class<T> clazz) {
      Set<BusinessMemberBuilder<? super T, ?>> memberBuilderList = new LinkedHashSet<>();
      for (Method method : clazz.getMethods()) {
         if (method.isAnnotationPresent(BusinessMember.class)) {
            BusinessMember businessMember = method.getAnnotation(BusinessMember.class);
            memberBuilderList.add(new BusinessMemberBuilder<>(businessMember.name(), getFunction(method),
                  Set.of(getValidatorBuilderFromReturnType(method))));
         }
      }
      return memberBuilderList;
   }

   private Predicate<T> getPredicate(Method method) throws InvocationException {
      return object -> {
         try {
            return (boolean) method.invoke(object);
         } catch (Exception e) {
            throw new InvocationException(e.getCause());
         }
      };
   }

   private Function<T, ?> getFunction(Method method) throws InvocationException {
      return object -> {
         try {
            return method.invoke(object);
         } catch (InvocationTargetException e) {
            throw new InvocationException(e.getCause());
         } catch (Exception e) {
            throw new InvocationException(e);
         }
      };
   }

   private BValidatorBuilder<?> getValidatorBuilderFromReturnType(Method method) throws InvocationException {
      Class<?> clazz = getUnboxedReturnType(method);
      return new AnnotationResolver<>(clazz).getBuilder();
   }

   private Class<?> getUnboxedReturnType(Method method) {
      Class<?> clazz = method.getReturnType();
      if (clazz.isArray()) {
         return clazz.getComponentType();
      } else if (Collection.class.isAssignableFrom(clazz)) {
         return getGenericTypeParameter(method);
      } else {
         return clazz;
      }
   }

   private Class<?> getGenericTypeParameter(Method method) {
      String genericType = method.getGenericReturnType().getTypeName();
      if (genericType.contains("<") && genericType.contains(">")) {
         String className = genericType.substring(genericType.indexOf("<") + 1, genericType.indexOf(">"));
         try {
            return Class.forName(className);
         } catch (ClassNotFoundException e) {
            throw new TypeResolutionException(e);
         }
      }
      throw new TypeResolutionException("Cannot resolve the generic return type of method " + method.getName());
   }


   private Class<?> assertBusinessObjectClass(Class<?> clazz) {
      if (!Object.class.equals(clazz) && (isBusinessObject(clazz) || hasASuperClassBusinessObject(
            clazz.getSuperclass()))) {
         return clazz;
      } else {
         throw new IllegalBusinessObjectException("Neither the class " + clazz.getCanonicalName()
               + "nor any of its super-class is annotated with @BusinessObject.");
      }
   }

   private boolean isBusinessObject(Class<?> clazz) {
      return clazz.isAnnotationPresent(BusinessObject.class);
   }

   private boolean hasASuperClassBusinessObject(Class<?> superClass) {
      if (superClass == null || isOnTopClassHierarchy(superClass)) {
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
