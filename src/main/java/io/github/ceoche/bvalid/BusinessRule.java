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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Mark a method as business rule validation in an object annotated with {@link BusinessObject}. Such business rule
 * method must be a boolean expression assertion. By convention if the result is 'true', it means the business rule is
 * correct/valid, and false otherwise.
 * <p>
 * A method marked with this annotation must be public, take no parameters and return a boolean value.
 * <p>
 * example:
 * <pre>
 * {@code @BusinessObject(name="my-entity")
 * public class MyEntity {
 *
 *    public String name;
 *
 *    {@literal @}BusinessRule(description = "name must be defined.")
 *    public boolean isNameValid() {
 *       return name != null && !name.trim().isEmpty();
 *    }
 * }}</pre>
 *
 * @author ceoche
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BusinessRule {

    /**
     * Identifier of the business rule. To be used in an environment that requires requirement traceability.
     *
     * @return the identifier of the business rule.
     */
    public String id() default "";

    /**
     * A textual description of the formal expression implemented by the method.
     *
     * @return the description of the rule.
     */
    String description();
}
