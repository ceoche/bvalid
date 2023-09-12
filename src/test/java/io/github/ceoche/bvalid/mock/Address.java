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
