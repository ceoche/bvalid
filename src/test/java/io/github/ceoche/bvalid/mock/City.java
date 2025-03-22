/*
 * Copyright 2025. Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.github.ceoche.bvalid.mock;

public class City {

    private final String names;

    private final Integer zipCode;

    public City(String names, Integer zipCode) {
        this.names = names;
        this.zipCode = zipCode;
    }

    public String getNames() {
        return names;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public Boolean isNamesValid() {
        return names != null && !names.isEmpty();
    }

    public Boolean isZipCodeValid() {
        return zipCode != null && zipCode > 0;
    }
}
