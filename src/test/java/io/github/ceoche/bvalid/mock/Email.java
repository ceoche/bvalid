package io.github.ceoche.bvalid.mock;

import io.github.ceoche.bvalid.BusinessObject;
import io.github.ceoche.bvalid.BusinessRule;

public class Email {

    private final String email;

    private final String domain;

    public Email(String email, String domain) {
        this.email = email;
        this.domain = domain;
    }

    public String getEmail() {
        return email;
    }

    public Boolean isEmailValid() {
        return email != null && !email.isEmpty() && email.contains("@");
    }

    public String getDomain() {
        return domain;
    }

    public Boolean isDomainValid() {
        return domain != null && !domain.isEmpty() && domain.contains(".");
    }
}