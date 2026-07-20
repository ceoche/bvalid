package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

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
      assertEquals("Person Address must not be null => valid", report.getAssertionReports().getFirst().toString());
      assertTrue(report.getAssertionReports().get(1).toString().contains("Actual values : {age=25} - "));
      assertTrue(report.getAssertionReports().get(2).toString().contains("Actual values : {name=Jean} - "));
      assertEquals(1, report.getAssertionReports().get(1).getActualValues().size());
      assertEquals("25", report.getAssertionReports().get(1).getActualValues().get("age"));

      BReport emailReport = findReport(report, "emails[0]");
      assertTrue(emailReport.getAssertionReports().getFirst().toString().contains("Actual values : {domain=test.com} - "));
      assertTrue(emailReport.getAssertionReports().get(1).toString().contains("Actual values : {email=test@test.com} - "));

      BReport addressReport = findReport(report, "address");
      assertEquals("Person.address City must not be null => valid", addressReport.getAssertionReports().getFirst().toString());
      assertTrue(addressReport.getAssertionReports().get(1).toString().contains("Actual values : {street=Rue de la Paix} - "));

      BReport cityReport = findReport(addressReport, "city");
      assertTrue(cityReport.getAssertionReports().getFirst().toString().contains("Actual values : {names=Paris} - "));
      assertTrue(cityReport.getAssertionReports().get(1).toString().contains("Actual values : {zipcode=50} - "));

      BReport phoneReport = findReport(report, "phones[0]");
      assertTrue(phoneReport.getAssertionReports().getFirst().toString().contains("Actual values : {number=987654321} - "));
      assertTrue(phoneReport.getAssertionReports().get(1).toString().contains("Actual values : {countryCode=aa} - "));
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
