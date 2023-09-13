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

import java.util.function.Function;

/**
 * Utility class to hold builders of a member at runtime.
 * Class not intended to be used outside the library.
 * It's the equivalent of a {@link BusinessMember} in annotation based validation.
 *
 * @param <T> the type of the business object that contain the member.
 * @param <R> the type of the member to validate
 */
class BusinessMemberBuilder<T, R> {

    private final String name;

    private final Function<T, ?> getter;

    private final BValidatorBuilder<? extends R>[] validatorBuilder;

    @SafeVarargs
    BusinessMemberBuilder(String name, Function<T, ?> getter, BValidatorBuilder<? extends R>... bValidatorBuilder) {
        this.name = name;
        this.getter = getter;
        this.validatorBuilder = bValidatorBuilder;
    }

    String getName() {
        return name;
    }

    Function<T, ?> getGetter() {
        return getter;
    }

    BValidatorBuilder<? extends R>[] getValidatorBuilders() {
        return validatorBuilder;
    }

}
