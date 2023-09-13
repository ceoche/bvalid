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


/**
 * The API to build a {@link BValidator}.
 *
 * @param <T> the type of the business object to validate
 * @author Achraf Achkari
 */
public interface BValidatorBuilder<T> {

    /**
     * Build the {@link BValidator} from the builder.
     *
     * @return the {@link BValidator} built
     * @throws IllegalStateException          if the type of the business object is not set
     * @throws IllegalBusinessObjectException if the builder is empty (i.e. no rules or members)
     */
    BValidator<T> build();

    /**
     * Check if the builder is empty (i.e. no rules or members).
     *
     * @return true if the builder is empty, false otherwise
     */
    boolean isEmpty();

}
