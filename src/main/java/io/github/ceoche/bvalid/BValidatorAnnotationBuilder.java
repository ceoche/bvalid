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
 * @author Achraf Achkari
 */
public class BValidatorAnnotationBuilder<T> extends AbstractBValidatorBuilder<T> {

    private final Set<BusinessRuleObject<T>> rules;

    private final Set<BusinessMemberBuilder<T, ?>> members;

    /**
     * Constructor of a BValidatorBuilder for class annotated with BusinessObject.
     *
     * @param clazz Root class of a model to create a validator for.
     */
    public BValidatorAnnotationBuilder(Class<T> clazz) {
        super(clazz);
        BusinessObject businessObject = clazz.getAnnotation(BusinessObject.class);
        if (businessObject != null) {
            businessObjectName = businessObject.name();
            if (businessObjectName.isEmpty()) {
                businessObjectName = clazz.getSimpleName();
            }
        }
        Class<T> assertedClass = (Class<T>) assertBusinessObjectClass(clazz);
        this.rules = getRules(assertedClass);
        this.members = getMembers(assertedClass);
    }

    @Override
    Set<BusinessRuleObject<T>> getRules() {
        return rules;
    }

    @Override
    Set<BusinessMemberBuilder<T, ?>> getMembers() {
        return members;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BValidatorAnnotationBuilder<T> setBusinessObjectName(String businessObjectName) {
        super.setBusinessObjectName(businessObjectName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BValidator<T> build() {
        return new BValidatorManualBuilder<T>(type)
                .addAllMembers(members)
                .addAllRules(rules)
                .setBusinessObjectName(businessObjectName)
                .build();
    }

    private Set<BusinessRuleObject<T>> getRules(Class<T> clazz) {
        Set<BusinessRuleObject<T>> rulesResult = new LinkedHashSet<>();
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(BusinessRule.class)) {
                BusinessRule businessRule = method.getAnnotation(BusinessRule.class);
                rulesResult.add(new BusinessRuleObject<>(businessRule.id(), getPredicate(method), businessRule.description()));
            }
        }
        return rulesResult;
    }

    private Set<BusinessMemberBuilder<T, ?>> getMembers(Class<T> clazz) {
        Set<BusinessMemberBuilder<T, ?>> memberBuilderList = new LinkedHashSet<>();
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(BusinessMember.class)) {
                BusinessMember businessMember = method.getAnnotation(BusinessMember.class);
                memberBuilderList.add(new BusinessMemberBuilder<>(businessMember.name(), getFunction(method), getValidatorBuilderSupplier(method)));
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
            } catch (Exception e) {
                throw new InvocationException(e);
            }
        };
    }

    private BValidatorBuilder<?> getValidatorBuilderSupplier(Method method) throws InvocationException {
        Class<?> clazz = getRealType(method);
        return new BValidatorAnnotationBuilder<>(clazz);
    }

    private Class<?> getRealType(Method method) {
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
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Cannot find generic type for method " + method.getName());
    }


    private Class<?> assertBusinessObjectClass(Class<?> clazz) {
        if (!Object.class.equals(clazz) && (isBusinessObject(clazz) || hasASuperClassBusinessObject(clazz.getSuperclass()))) {
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
