package io.github.ceoche.bvalid.mock;

import io.github.ceoche.bvalid.BusinessMember;
import io.github.ceoche.bvalid.BusinessObject;
import io.github.ceoche.bvalid.BusinessRule;

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