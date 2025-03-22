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

import java.util.ArrayList;
import java.util.List;

public class Person {

    private final String name;
    private final Address address;
    private final Integer age;
    private final Email[] emails;
    private final List<Phone> phones = new ArrayList<>();

    public Person(String name, Address address, Integer age, Email[] email, List<Phone> phones) {
        this.name = name;
        this.address = address;
        this.age = age;
        this.emails = email;
        this.phones.addAll(phones);
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Integer getAge() {
        return age;
    }

    public Email[] getEmails() {
        return emails;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public Boolean isAgeValid() {
        return age > 0 && age < 150;
    }

    public Boolean isNameValid() {
        return name != null && !name.isEmpty();
    }

    public Boolean isAddressValid() {
        return address != null;
    }

    public Boolean isEmailValid() {
        return emails != null && emails.length>0;
    }

}