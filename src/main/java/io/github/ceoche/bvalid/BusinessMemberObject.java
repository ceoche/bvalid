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
import java.util.function.Function;

class BusinessMemberObject<T,R> {

    private final String name;

    private final Function<T, ?> getter;

    private final Map<Class<? extends R>,BValidator<? extends R>> validators;

    BusinessMemberObject(String name, Function<T, ?> getter, Map<Class<? extends R>,BValidator<? extends R>> validators) {
        this.name = name;
        this.getter = getter;
        this.validators = validators;
    }

    String getName() {
        return name;
    }

    Object getMemberValue(T object) {
        return getter.apply(object);
    }

    void addValidator(Class<? extends R> clazz, BValidator<? extends R> validator) {
        this.validators.put(clazz, validator);

    }

    public Map<Class<? extends R>, BValidator<? extends R>> getValidators() {
        return validators;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessMemberObject)) return false;
        BusinessMemberObject<?, ?> that = (BusinessMemberObject<?, ?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
