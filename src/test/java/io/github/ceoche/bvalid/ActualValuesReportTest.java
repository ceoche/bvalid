package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;

class ActualValuesReportTest {

   private static BValidator<Person> personValidatorWithPhones;

   @BeforeAll
   static void setUp() {
      BValidatorBuilder<Address> addressValidator = new BValidatorBuilder<>(Address.class)
            .setObjectName("Address")
            .addAssertion(Address::isCityValid, "City must not be null")
            .addAssertion(Address::isStreetValid, "Street must not be empty", new ActualValueSupplier<>("street", Address::getStreet))
            .addMember("city", Address::getCity, new BValidatorBuilder<>(City.class)
                  .setObjectName("City")
                  .addAssertion(City::isNamesValid, "City name must not be empty", new ActualValueSupplier<>("names", City::getNames))
                  .addAssertion(City::isZipCodeValid, "City zipcode must be valid", new ActualValueSupplier<>("zipcode", City::getZipCode))
            );
      personValidatorWithPhones = new BValidatorBuilder<>(Person.class)
            .setObjectName("Person")
            .addAssertion(Person::isAddressValid, "Address must not be null")
            .addAssertion(Person::isAgeValid, "Age must be between 0 and 150", new ActualValueSupplier<>("age", Person::getAge))
            .addAssertion(Person::isNameValid, "Name must not be empty", new ActualValueSupplier<>("name", Person::getName))
            .addMember("phones", Person::getPhones, new BValidatorBuilder<>(Phone.class)
                  .setObjectName("Phone")
                  .addAssertion(Phone::isNumberValid, "Number must not be null", new ActualValueSupplier<>("number", Phone::getNumber))
                  .addAssertion(Phone::isCountryCodeValid, "Country code must not be valid", new ActualValueSupplier<>("countryCode", Phone::getCountryCode))
            )
            .addMember("emails", Person::getEmails, new BValidatorBuilder<>(Email.class)
                  .setObjectName("Email")
                  .addAssertion(Email::isDomainValid, "Domain must not be empty", new ActualValueSupplier<>("domain", Email::getDomain))
                  .addAssertion(Email::isEmailValid, "Email must not be empty", new ActualValueSupplier<>("email", Email::getEmail))
            )
            .addMember("address", Person::getAddress, addressValidator)
            .build();
   }

   @Test
   void shouldPrintActualValues() {
      Person person = new Person(
            "Jean",
            new Address("Rue de la Paix", new City("Paris", 50), "France"),
            25,
            new Email[] {new Email("test@test.com", "test.com")},
            List.of(new Phone("987654321", "aa"))
      );
       BReport report = personValidatorWithPhones.validate(person);

       assertThat(report.getAssertionReports(),
               hasItem(hasToString(is("Person Address must not be null => valid"))));
       assertThat(report.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {age=25} - "))));
       assertThat(report.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {name=Jean} - "))));

       assertThat(report.getAssertionReports(), hasSize(greaterThanOrEqualTo(2)));
       assertThat(report.getAssertionReports().stream()
                       .map(Object::toString)
                       .toList(),
               hasItem(containsString("Actual values : {age=25} - ")));
       assertThat(report.getAssertionReports().stream()
               .filter(item -> item.toString().contains("Actual values : {age=25} - "))
               .findFirst()
               .get()
               .getActualValues(), hasEntry("age", "25"));

       BReport emailReport = findReport(report, "emails[0]");
       assertThat(emailReport.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {domain=test.com} - "))));
       assertThat(emailReport.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {email=test@test.com} - "))));

       BReport addressReport = findReport(report, "address");
       assertThat(addressReport.getAssertionReports(),
               hasItem(hasToString(is("Person.address City must not be null => valid"))));
       assertThat(addressReport.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {street=Rue de la Paix} - "))));

       BReport cityReport = findReport(addressReport, "city");
       assertThat(cityReport.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {names=Paris} - "))));
       assertThat(cityReport.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {zipcode=50} - "))));

       BReport phoneReport = findReport(report, "phones[0]");
       assertThat(phoneReport.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {number=987654321} - "))));
       assertThat(phoneReport.getAssertionReports(),
               hasItem(hasToString(containsString("Actual values : {countryCode=aa} - "))));
   }

   @Test
   void testAnnotationActualValues() {
      ObjectMocks.BusinessObjectWithNoAnnotation object = ObjectMocks.instantiateBusinessObjectWithNoAnnotation();
      BValidator<ObjectMocks.BusinessObjectWithNoAnnotation> validator = new AnnotationResolver<>(ObjectMocks.BusinessObjectWithNoAnnotation.class).buildValidator();
      BReport report = validator.validate(object);
      assertTrue(report.isValid());

      AssertionReport assertionReport = report.getAssertionReports().getFirst();
      assertTrue(assertionReport.isValid());

      assertEquals(1, assertionReport.getActualValues().size());
      assertEquals("noAnnotation", assertionReport.getActualValues().get("name"));
   }

   @Test
   void testIllegalSupplierName() {
      AnnotationResolver<IllegalSupplierNameObject> resolver = new AnnotationResolver<>(IllegalSupplierNameObject.class);
      InvocationException e = assertThrows(InvocationException.class, resolver::buildValidator);
      assertInstanceOf(NoSuchMethodException.class, e.getCause());
      assertTrue(e.getMessage().contains("getname()"));
   }

   @Test
   void testNullValueSupplier() {
      BValidator<City> validator = new BValidatorBuilder<>(City.class)
            .setObjectName("City")
            .addAssertion(City::isNamesValid, "City name must not be empty", new ActualValueSupplier<>(null, c -> {
               throw new RuntimeException("");
            }))
            .addAssertion(City::isZipCodeValid, "City zipcode must be valid", new ActualValueSupplier<>("zipcode", City::getZipCode))
            .build();
      City city = new City("", 22);
      assertThrows(InvocationException.class, () -> validator.validate(city));
   }

   private BReport findReport(BReport report, String objectName) {
      return report.getMemberReports().stream().filter(
            r -> objectName.equals(r.getObjectName())
      ).findFirst().orElseThrow();
   }

   @BusinessObject(name = "IllegalSupplierName")
   public static class IllegalSupplierNameObject {

      private String name;

      public String getName() {
         return name;
      }

      public IllegalSupplierNameObject setName(String name) {
         this.name = name;
         return this;
      }

      @BusinessAssertion(
            description = "An assertion must not take any parameter.",
            actualValueSuppliers = {
                  @BusinessAssertion.ActualValueSupplier(attributeName = "name", supplier = "getname"),
            }
      )
      public boolean isValid(Object object) {
         return object != null;
      }
   }
}
