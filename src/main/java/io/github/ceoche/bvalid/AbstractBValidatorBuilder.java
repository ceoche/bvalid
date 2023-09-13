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

import java.util.*;

/**
 * This class contains the common logic for all BValidatorBuilders
 * which is the engine behind building a BValidator
 *
 * @param <T> The type of the object to validate
 * @author Achraf Achkari
 */
public abstract class AbstractBValidatorBuilder<T> implements BValidatorBuilder<T> {

    /**
     * Type of the business object to build a validator for.
     */
    protected Class<T> type;

    /**
     * Name of the business object to build a validator for.
     */
    protected String businessObjectName = "";

    /**
     * Constructor of AbstractBValidatorBuilder
     *
     * @param type the type of the business object to build a validator for.
     */
    public AbstractBValidatorBuilder(Class<T> type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return getRules().isEmpty() && getMembers().isEmpty();
    }

    /**
     * Get the name of the business object to validate. Will be used in reporting.
     *
     * @return the name of the business object.
     */
    public String getBusinessObjectName() {
        return businessObjectName;
    }

    /**
     * Define the name of the business object to build a validator for. It is recommended to set the name of the root
     * object for a better reporting.
     *
     * @param businessObjectName name of the business object.
     * @return this instance of BValidatorBuilder.
     */
    public AbstractBValidatorBuilder<T> setBusinessObjectName(String businessObjectName) {
        this.businessObjectName = businessObjectName;
        return this;
    }

    abstract Set<BusinessRuleObject<T>> getRules();

    abstract Set<BusinessMemberBuilder<T, ?>> getMembers();

    final BValidator<T> build(Map<AbstractBValidatorBuilder<?>, BValidator<?>> visitedBuilders) {
        if (type == null) {
            throw new IllegalStateException("Type is not set");
        }
        Set<BusinessMemberObject<T, ?>> businessMemberObjects = new LinkedHashSet<>();
        BValidator<T> validator = new BValidator<>(getRules(), businessMemberObjects, getBusinessObjectName());
        visitedBuilders.put(this, validator);
        for (BusinessMemberBuilder<T, ?> businessMemberBuilder : getMembers()) {
            if (!allBuildersAreEmpty(businessMemberBuilder.getValidatorBuilders())) {
                AbstractBValidatorBuilder<?>[] subValidatorBuilders = Arrays
                        .stream(businessMemberBuilder.getValidatorBuilders())
                        .map(bValidatorBuilder -> (AbstractBValidatorBuilder<?>) bValidatorBuilder)
                        .toArray(AbstractBValidatorBuilder[]::new);
                BusinessMemberObject<T, Object> businessMemberObject = new BusinessMemberObject<>(businessMemberBuilder.getName(), businessMemberBuilder.getGetter(), new HashMap<>());
                for (AbstractBValidatorBuilder<?> subValidatorBuilder : subValidatorBuilders) {
                    if (!visitedBuilders.containsKey(subValidatorBuilder)) {
                        businessMemberObject.addValidator(subValidatorBuilder.type, subValidatorBuilder.build(visitedBuilders));
                    } else {
                        businessMemberObject.addValidator(subValidatorBuilder.type, visitedBuilders.get(subValidatorBuilder));
                    }
                }
                businessMemberObjects.add(businessMemberObject);

            } else {
                throw new IllegalStateException("All sub validators are empty");
            }
        }
        assertBuilderNotEmpty();
        return validator;
    }


    private void assertBuilderNotEmpty() {
        if (isEmpty()) {
            throw new IllegalBusinessObjectException("Rules or members must be provided to build a validator.");
        }
    }

    private boolean allBuildersAreEmpty(BValidatorBuilder<?>[] bValidatorBuilders) {
        for (BValidatorBuilder<?> bValidatorBuilder : bValidatorBuilders) {
            if (!bValidatorBuilder.isEmpty()) {
                return false;
            }
        }
        return true;
    }


}
