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

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A business rule object is a predicate with an id and a description.
 * It is used to validate a business object with the manual builder.
 * It's the equivalent of a {@link BusinessRule} in annotation based validation.
 *
 * @param <T> the type of the business object on which the rule apply.
 */
class BusinessRuleObject<T> {

    private final String id;

    private final String description;

    private final Predicate<T> rule;


    /**
     * Constructor of a BusinessRuleObject.
     *
     * @param rule        Java predicate (assertion) that will be applied during the validation to assess whether the rule is respected or not.
     * @param description Textual description of the rule.
     */
    BusinessRuleObject(Predicate<T> rule, String description) {
        this("", rule, description);
    }

    /**
     * Constructor of a BusinessRuleObject with a requirement id.
     *
     * @param id          id of the rule. Used for requirement engineering.
     * @param rule        Java predicate (assertion) that will be applied during the validation to assess whether the rule is respected or not.
     * @param description Textual description of the rule.
     */
    BusinessRuleObject(String id, Predicate<T> rule, String description) {
        this.id = id != null ? id : "";
        this.description = description;
        this.rule = rule;
    }

    /**
     * Get the id of the rule.
     *
     * @return the id of the rule.
     */
    String getId() {
        return id;
    }

    /**
     * Get the description of the rule
     *
     * @return the description.
     */
    String getDescription() {
        return description;
    }

    Boolean apply(T object) {
        return rule.test(object);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessRuleObject)) return false;
        BusinessRuleObject<?> that = (BusinessRuleObject<?>) o;

        return description.equals(that.description) &&
                rule.equals(that.rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, rule);
    }
}
