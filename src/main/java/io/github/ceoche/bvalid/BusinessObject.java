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
 * Indicate that instances of this class can be validated using {@link BValidator}.
 * <p>
 * Classes that are declared {@link BusinessObject} must :
 * <ul>
 * <li>implement at least one business rule (method annotated with {@link BusinessRule} or have
 * at least one business member (getter annotated with {@link BusinessMember} or,</li>
 * <li>have a parent class that respect the previous rule.</li>
 * </ul>
 * <p>
 * <i>Sub-classes of a class annotated with {@link BusinessObject} can also be validated with
 * {@link BValidator}.</i>
 *
 * @author ceoche
 * @see BusinessRule
 * @see BusinessMember
 * @see BValidator
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BusinessObject {
   /**
    * Name of the business object. The class name by default.
    *
    * @return the name of the business object.
    */
   String name() default "";
}
