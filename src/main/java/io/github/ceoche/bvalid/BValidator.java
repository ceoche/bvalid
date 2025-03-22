/*
 * TODO update copyright
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

import java.util.*;

/**
 * The {@link BValidator} validates business-objects based on  a pre-defined list of assertions and identified members
 * to explore.
 * <p>
 * Use {@link BValidatorBuilder} or {@link AnnotationResolver} to create a {@link BValidator}.
 *
 * @param <T> The type of business object to validate. For complex object structures (aggregates), this should be the
 *            type of the root/container object from which the validator can access all nested objects requiring
 *            validation.
 *
 * @author ceoche
 */
public class BValidator<T> {

   private final Class<T> type;
   private final String objectName;
   private final Set<BAssertion<? super T>> assertions;
   private final Set<BMember<? super T, ?>> members;

   /**
    * Hidden constructor. Use {@link BValidatorBuilder} or {@link AnnotationResolver} can create a {@link BValidator}.
    */
   BValidator(Class<T> type, String objectName, Set<BAssertion<? super T>> assertions,
              Set<BMember<? super T, ?>> members) {
      this.type = type;
      this.objectName = objectName;
      this.assertions = assertions;
      this.members = members;
   }

   /**
    * Verify if an object of type T is valid by running business assertions tests methods listed in
    * {@link BValidator#assertions} and by validating all members accessible from the {@link BValidator#members}. The
    * validation will test all business assertions and store the results in an {@link BReport}.
    *
    * @param object business object to validate.
    *
    * @return an {@link BReport} that hold all the business assertion and member results.
    *
    * @throws InvocationException            if an exception is raised while invoking a
    *                                        {@link java.util.function.Predicate} or a
    *                                        {@link java.util.function.Function}. function. The original exception will
    *                                        be wrapped as cause.
    * @throws IllegalBusinessObjectException if an error occurs while validating a member (Wrong return type,...)
    * @throws NullPointerException           if the given object is null.
    */
   public BReport validate(final T object) {
      return this.validate(object, objectName, new HashSet<>());
   }


   /**
    * Verify if an array of objects is valid by running business assertions tests methods listed in
    * {@link BValidator#assertions} and by validating all members accessible from the {@link BValidator#members}. The
    * validation will test all business assertions and store the results in an {@link BReport}.
    *
    * @param collection collection of business objects to validate.
    *
    * @return an {@link BReport} that hold all the business assertion and member results.
    *
    * @throws InvocationException            if an exception is raised while invoking a
    *                                        {@link java.util.function.Predicate} or a
    *                                        {@link java.util.function.Function}. function. The original exception will
    *                                        be wrapped as cause.
    * @throws IllegalBusinessObjectException if an error occurs while validating a member (Wrong return type,...)
    * @throws NullPointerException           if the given object is null.
    */
   public List<BReport> validate(final Collection<T> collection) {
      return this.validate(collection, objectName, new HashSet<>());
   }

   /**
    * Verify if a collections of type T is valid by running business assertions tests methods listed in
    * {@link BValidator#assertions} and by validating all members accessible from the {@link BValidator#members}. The
    * validation will test all business assertions and store the results in an {@link BReport}.
    *
    * @param array array of business objects to validate.
    *
    * @return an {@link BReport} that hold all the business assertion and member results.
    *
    * @throws InvocationException            if an exception is raised while invoking a
    *                                        {@link java.util.function.Predicate} or a
    *                                        {@link java.util.function.Function}. function. The original exception will
    *                                        be wrapped as cause.
    * @throws IllegalBusinessObjectException if an error occurs while validating a member (Wrong return type,...)
    * @throws NullPointerException           if the given object is null.
    */
   public List<BReport> validate(final T[] array) {
      return this.validate(array, objectName, new HashSet<>());
   }

   Class<T> getType() {
      return type;
   }

   private BReport validate(T object, String name, Set<Object> visitedObjects) {
      if (object == null) {
         throw new NullPointerException("The object to validate cannot be null");
      }
      visitedObjects.add(object);
      final BReport result = new BReport(name);
      List<AssertionReport> assertionReports = this.validateBusinessAssertions(object);
      List<BReport> memberResults = this.validateBusinessMembers(object, visitedObjects);
      result.addRuleReports(assertionReports);
      result.addMemberReports(memberResults);
      return result;
   }

   private List<BReport> validate(Collection<T> collection, String name, Set<Object> visitedObjects) {
      List<BReport> results = new ArrayList<>();
      int index = -1;
      for (T object : collection) {
         results.add(this.validate(object, name + "[" + ++index + "]", visitedObjects));
      }
      return results;
   }

   private List<BReport> validate(T[] array, String name, Set<Object> visitedObjects) {
      return validate(Arrays.asList(array), name, visitedObjects);
   }

   //FIXME clean un correlation between BVallidator and member
   private <R, F extends R> BReport validateMember(final F object, final BValidator<? extends R> validator,
                                                   final String memberName, Set<Object> visitedObjects) {
      return ((BValidator<F>) validator).validate(object, memberName, visitedObjects);
   }

   private <R, F extends R> List<BReport> validateMemberCollection(final Collection<F> collection,
                                                                   final Map<Class<? extends R>, BValidator<? extends R>> validators,
                                                                   final String memberName,
                                                                   Set<Object> visitedObjects) {
      List<BReport> results = new ArrayList<>();
      int index = -1;
      for (F object : collection) {
         results.add(((BValidator<F>) getValidatorByType(validators, object)).validate(object,
               memberName + "[" + ++index + "]", visitedObjects));
      }
      return results;
   }

   private <R> List<BReport> validateMemberArray(final R[] array,
                                                 final Map<Class<? extends R>, BValidator<? extends R>> validators,
                                                 final String memberName, Set<Object> visitedObjects) {
      return validateMemberCollection(Arrays.asList(array), validators, memberName, visitedObjects);
   }


   private List<AssertionReport> validateBusinessAssertions(final T object) {
      final List<AssertionReport> results = new ArrayList<>();
      for (final BAssertion<? super T> rule : assertions) {
         results.add(new AssertionReport(rule.getId(), rule.getDescription(), rule.apply(object)));
      }
      return results;
   }

   private List<BReport> validateBusinessMembers(final T object, Set<Object> visitedObjects) {
      final List<BReport> results = new ArrayList<>();
      for (final BMember<? super T, ?> member : members) {
         try {
            final Object memberValue = getMemberValue(object, member);
            if (!isObjectAlreadyVisited(memberValue, visitedObjects)) {
               results.addAll(validateAnyMember(memberValue, member.getValidators(), member.getName(), visitedObjects));
            }
         } catch (IllegalBusinessObjectException e) {
            throw e;
         } catch (ClassCastException e) {
            throw new IllegalBusinessObjectException("Wrong member type", e);
         } catch (final Exception e) {
            if (e.getCause() != null) {
               throw new InvocationException(e.getCause());
            }
            throw new InvocationException(e);
         }
      }
      return results;
   }

   // O(1) complexity for hashset
   private boolean isObjectAlreadyVisited(Object memberValue, Set<Object> visitedObjects) {
      if (memberValue == null) {
         return false;
      }
      return visitedObjects.contains(memberValue);
   }


   private Object getMemberValue(final T object, final BMember<? super T, ?> member) {
      return member.getMemberValue(object);
   }


   @SuppressWarnings("unchecked")
   private <M> List<BReport> validateAnyMember(final Object memberValue,
                                               Map<Class<? extends M>, BValidator<? extends M>> validators,
                                               String name, Set<Object> visitedObjects) {
      final List<BReport> results = new ArrayList<>();
      if (memberValue == null) {
         return Collections.emptyList();
      }
      if (isValidCollection(memberValue)) {
         if (!((Collection<?>) memberValue).isEmpty()) {
            results.addAll(
                  this.validateMemberCollection((Collection<M>) memberValue, validators, name, visitedObjects));
         }
      } else if (isValidArray(memberValue)) {
         if (((Object[]) memberValue).length > 0) {
            results.addAll(this.validateMemberArray((M[]) memberValue, validators, name, visitedObjects));
         }
      } else {
         results.add(
               this.validateMember(memberValue, getValidatorByType(validators, memberValue), name, visitedObjects));
      }
      return results;
   }

   private boolean isValidCollection(Object memberValue) {
      return (memberValue instanceof Collection);
   }

   private boolean isValidArray(Object memberValue) {
      return (memberValue instanceof Object[]);
   }

   private <R> BValidator<? extends R> getValidatorByType(Map<Class<? extends R>, BValidator<? extends R>> validators,
                                                          Object object) {
      Class<?> clazz = object.getClass();
      String className = clazz.getName();
      do {
         if (validators.containsKey(clazz)) {
            return validators.get(clazz);
         }
         clazz = clazz.getSuperclass();
      }
      while (clazz != null);

      throw new IllegalBusinessObjectException("No validator found for type " + className);
   }

}
