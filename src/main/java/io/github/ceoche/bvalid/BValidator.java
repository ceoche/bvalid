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

import java.util.*;

/**
 * The {@link BValidator} provides method to validate business rules and members of POJO business
 * based on the rules and members provided by the {@link BValidatorManualBuilder}.
 * {@link BusinessMember}.
 *
 * @author ceoche
 */
public class BValidator<T> {

    private final Set<BusinessRuleObject<T>> rules;

    private final Set<BusinessMemberObject<T, ?>> members;


    private final String businessObjectName;

    /**
     * Hidden constructor. Only {@link BValidatorManualBuilder} can create a {@link BValidator}.
     */
    BValidator(Set<BusinessRuleObject<T>> rules, Set<BusinessMemberObject<T, ?>> members, String businessObjectName) {
        this.businessObjectName = businessObjectName;
        this.rules = rules;
        this.members = members;
    }

    /**
     * Verify if an object of type T is valid by running business
     * rules tests methods listed in {@link BValidator#rules} and by validating all members
     * accessible from the {@link BValidator#members}.
     * The validation will test all business rules and store the results in an
     * {@link ObjectResult}.
     *
     * @param object business object to validate.
     * @return an {@link ObjectResult} that hold all the business rule and member results.
     * @throws InvocationException            if an exception is raised while invoking a
     *                                        {@link java.util.function.Predicate} or a {@link java.util.function.Function}.
     *                                        function. The original exception will be wrapped as cause.
     * @throws IllegalBusinessObjectException if an error occurs while validating a member (Wrong return type,...)
     * @throws NullPointerException           if the given object is null.
     */

    public ObjectResult validate(final T object) {
        return this.validate(object, businessObjectName, new HashSet<>());
    }


    /**
     * Verify if an array of objects is valid by running business
     * rules tests methods listed in {@link BValidator#rules} and by validating all members
     * accessible from the {@link BValidator#members}.
     * The validation will test all business rules and store the results in an
     * {@link ObjectResult}.
     *
     * @param collection collection of business objects to validate.
     * @return an {@link ObjectResult} that hold all the business rule and member results.
     * @throws InvocationException            if an exception is raised while invoking a
     *                                        {@link java.util.function.Predicate} or a {@link java.util.function.Function}.
     *                                        function. The original exception will be wrapped as cause.
     * @throws IllegalBusinessObjectException if an error occurs while validating a member (Wrong return type,...)
     * @throws NullPointerException           if the given object is null.
     */
    public List<ObjectResult> validate(final Collection<T> collection) {
        return this.validate(collection, businessObjectName, new HashSet<>());
    }

    /**
     * Verify if a collections of type T is valid by running business
     * rules tests methods listed in {@link BValidator#rules} and by validating all members
     * accessible from the {@link BValidator#members}.
     * The validation will test all business rules and store the results in an
     * {@link ObjectResult}.
     *
     * @param array array of business objects to validate.
     * @return an {@link ObjectResult} that hold all the business rule and member results.
     * @throws InvocationException            if an exception is raised while invoking a
     *                                        {@link java.util.function.Predicate} or a {@link java.util.function.Function}.
     *                                        function. The original exception will be wrapped as cause.
     * @throws IllegalBusinessObjectException if an error occurs while validating a member (Wrong return type,...)
     * @throws NullPointerException           if the given object is null.
     */
    public List<ObjectResult> validate(final T[] array) {
        return this.validate(array, businessObjectName, new HashSet<>());
    }

    private <R> ObjectResult validate(T object, String name, Set<Object> visitedObjects) {
        if (object == null) {
            throw new NullPointerException("The object to validate cannot be null");
        }
        final ObjectResult result = new ObjectResult(name);
        List<RuleResult> ruleResults = validateBusinessRules(object);
        List<ObjectResult> memberResults = this.<R>validateBusinessMembers(object, visitedObjects);
        result.addRuleResults(ruleResults);
        result.addMemberResults(memberResults);
        return result;
    }

    private List<ObjectResult> validate(Collection<T> collection, String name, Set<Object> visitedObjects) {
        List<ObjectResult> results = new ArrayList<>();
        int index = -1;
        for (T object : collection) {
            results.add(this.validate(object, name + "[" + ++index + "]", visitedObjects));
        }
        return results;
    }

    private List<ObjectResult> validate(T[] array, String name, Set<Object> visitedObjects) {
        return validate(Arrays.asList(array), name, visitedObjects);
    }

    private <R, F extends R> ObjectResult validateMember(final F object, final BValidator<? extends R> validator, final String memberName, Set<Object> visitedObjects) {
        return ((BValidator<F>) validator).validate(object, memberName, visitedObjects);
    }

    private <R, F extends R> List<ObjectResult> validateMemberCollection(final Collection<F> collection, final Map<Class<? extends R>, BValidator<? extends R>> validators, final String memberName, Set<Object> visitedObjects) {
        List<ObjectResult> results = new ArrayList<>();
        int index = -1;
        for (F object : collection) {
            results.add(((BValidator<F>) (getValidatorByType(validators, object))).validate(object, memberName + "[" + ++index + "]", visitedObjects));
        }
        return results;
    }

    private <R> List<ObjectResult> validateMemberArray(final R[] array, final Map<Class<? extends R>, BValidator<? extends R>> validators, final String memberName, Set<Object> visitedObjects) {
        return validateMemberCollection(Arrays.asList(array), validators, memberName, visitedObjects);
    }


    private List<RuleResult> validateBusinessRules(final T object) {
        final List<RuleResult> results = new ArrayList<>();
        for (final BusinessRuleObject<T> rule : rules) {
            try {
                results.add(new RuleResult(rule.getId(), rule.getDescription(), rule.apply(object)));
            } catch (InvocationException e) {
                throw new InvocationException(e.getCause());
            }
        }
        return results;
    }

    private List<ObjectResult> validateBusinessMembers(final T object, Set<Object> visitedObjects) {
        final List<ObjectResult> results = new ArrayList<>();
        for (final BusinessMemberObject<T, ?> member : members) {
            try {
                final Object memberValue = getMemberValue(object, member);
                if (!isObjectAlreadyVisited(memberValue, visitedObjects)) {
                    visitedObjects.add(memberValue);
                    results.addAll(validateAnyMember(memberValue, member.getValidators(), member.getName(), visitedObjects));
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalBusinessObjectException(
                        "Method '" + member.getName() + "' does not respect BusinessMember " +
                                "method format (should be public with no arguments and return an object " +
                                "value that is a BusinessObject or a group of BusinessObject).", e);
            } catch (ClassCastException e) {
                throw new IllegalBusinessObjectException("Wrong member type", e);
            } catch (final Throwable e) {
                if (e.getCause() != null)
                    throw new InvocationException(e.getCause());
                throw new InvocationException(e);
            }
        }
        return results;
    }

    private boolean isObjectAlreadyVisited(Object memberValue, Set<Object> visitedObjects) {
        if (memberValue == null) {
            return false;
        }
        for (Object visitedObject : visitedObjects) {
            if (memberValue == visitedObject) {
                return true;
            }
        }
        return false;
    }


    private Object getMemberValue(final T object, final BusinessMemberObject<T, ?> member) throws Throwable {
        try {
            return member.getMemberValue(object);
        } catch (final InvocationException e) {
            throw e.getCause();
        }
    }

    private <R> List<ObjectResult> validateAnyMember(final Object memberValue, Map<Class<? extends R>, BValidator<? extends R>> validators, String name, Set<Object> visitedObjects) {
        final List<ObjectResult> results = new ArrayList<>();
        if (memberValue == null) {
            return Collections.emptyList();
        }
        if (isValidCollection(memberValue)) {
            if (!((Collection<?>) memberValue).isEmpty()) {
                results.addAll(this.validateMemberCollection((Collection<R>) memberValue, validators, name, visitedObjects));
            }
        } else if (isValidArray(memberValue)) {
            if (((Object[]) memberValue).length > 0) {
                results.addAll(this.validateMemberArray((R[]) memberValue, validators, name, visitedObjects));
            }
        } else {
            results.add(this.validateMember(memberValue, getValidatorByType(validators, memberValue), name, visitedObjects));
        }
        return results;
    }

    private boolean isValidCollection(Object memberValue) {
        return (memberValue instanceof Collection);
    }

    private boolean isValidArray(Object memberValue) {
        return (memberValue instanceof Object[]);
    }

    private <R> BValidator<? extends R> getValidatorByType(Map<Class<? extends R>, BValidator<? extends R>> validators, Object object) {
        Class<?> clazz = object.getClass();
        String className = clazz.getName();
        do {
            if(validators.containsKey(clazz)) {
                return validators.get(clazz);
            }
            clazz = clazz.getSuperclass();
        }
        while (clazz != null);

        throw new IllegalBusinessObjectException("No validator found for type " + className);
    }


}
