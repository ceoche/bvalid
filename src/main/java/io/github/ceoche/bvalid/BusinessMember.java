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
 * This annotation is used to create composition or association of {@link BusinessObject} through getter of the class.
 * It Indicates to the {@link BValidator} that the return value of the annotated getter is an instance of a class
 * annotated with {@link BusinessObject} and must therefore be validated as part of the validation scheme.
 * <p>
 * Methods that are declared {@link BusinessMember} must be public, take no parameters and return an instance of class
 * annotated with {@link BusinessObject}.
 * <p>
 *
 * @author ceoche
 * @see BusinessObject
 * @see BValidator
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BusinessMember {
   public String name() default "";
}
