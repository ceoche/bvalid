package io.github.ceoche.bvalid.mock;

public class Phone {

    private final String number;

    private final String countryCode;

    public Phone(String number, String countryCode) {
        this.number = number;
        this.countryCode = countryCode;
    }

    public String getNumber() {
        return number;
    }


    public Boolean isNumberValid() {
        return number != null && !number.isEmpty();
    }

    public String getCountryCode() {
        return countryCode;
    }

    public Boolean isCountryCodeValid() {
        return countryCode != null && !countryCode.isEmpty() && countryCode.startsWith("+");
    }
}
