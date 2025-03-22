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

public class Address {

    private final String street;
    private final City city;
    private final String country;

    public Address(String street, City city, String country) {
        this.street = street;
        this.city = city;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public City getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Boolean isStreetValid() {
        return street != null && !street.isEmpty();
    }

    public Boolean isCityValid() {
        return city != null;
    }

    public Boolean isCountryValid() {
        return country != null && !country.isEmpty();
    }

}
