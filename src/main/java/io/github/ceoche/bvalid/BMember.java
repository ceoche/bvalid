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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

class BMember<T, M> {

    private final String name;

    private final Function<T, ?> getter;

    private final Map<Class<? extends M>, BValidator<? extends M>> validators = new java.util.HashMap<>();

    BMember(String name, Function<T, ?> getter, Set<BValidator<? extends M>> validators) {
        this.name = name;
        this.getter = getter;
        validators.forEach(this::addValidator);
    }

    String getName() {
        return name;
    }

    Object getMemberValue(T object) {
        return getter.apply(object);
    }

    void addValidator(BValidator<? extends M> validator) {
        this.validators.put(validator.getType(), validator);
    }

    Map<Class<? extends M>, BValidator<? extends M>> getValidators() {
        return validators;
    }

}
