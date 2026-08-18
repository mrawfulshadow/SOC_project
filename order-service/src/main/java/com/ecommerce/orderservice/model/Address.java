package com.ecommerce.orderservice.model;

public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public Address() {}

    public Address(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public static class AddressBuilder {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;

        public AddressBuilder street(String street) { this.street = street; return this; }
        public AddressBuilder city(String city) { this.city = city; return this; }
        public AddressBuilder state(String state) { this.state = state; return this; }
        public AddressBuilder zipCode(String zipCode) { this.zipCode = zipCode; return this; }
        public AddressBuilder country(String country) { this.country = country; return this; }

        public Address build() {
            return new Address(street, city, state, zipCode, country);
        }
    }
}
