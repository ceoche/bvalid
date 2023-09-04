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
