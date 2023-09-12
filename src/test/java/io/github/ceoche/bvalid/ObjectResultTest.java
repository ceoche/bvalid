package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.Address;
import io.github.ceoche.bvalid.mock.City;
import io.github.ceoche.bvalid.mock.Person;
import io.github.ceoche.bvalid.mock.Phone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectResultTest {

    private static BValidator<Address> addressValidator;

    private static BValidator<Person> personValidatorWithPhones;

    @BeforeAll
    public static void setUp() {
        addressValidator = new BValidatorManualBuilder<>(Address.class)
                .setBusinessObjectName("Address")
                .addRule("cityValid", Address::isCityValid, "City must not be null")
                .addRule("StreetValid", Address::isStreetValid, "Street must not be empty")
                .addMember("city", Address::getCity, new BValidatorManualBuilder<>(City.class)
                        .setBusinessObjectName("city")
                        .addRule("cityNameValid", City::isNamesValid, "City name must not be empty")
                        .addRule("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                )
                .build();
        personValidatorWithPhones = new BValidatorManualBuilder<>(Person.class)
                .setBusinessObjectName("Person")
                .addMember("phones", Person::getPhones, new BValidatorManualBuilder<>(Phone.class)
                        .setBusinessObjectName("Phone")
                        .addRule("numberValid", Phone::isNumberValid, "Number must not be null")
                        .addRule("countryCodeValid", Phone::isCountryCodeValid, "Country code must not be valid")
                )
                .build();
    }

    @Test
    public void testGetNbOfTests(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        ObjectResult result = new BValidatorAnnotationBuilder<>(BusinessObjectMocks.DefaultValidableMock.class).build().validate(object);
        assertEquals(3, result.getNbOfTests());
    }

    @Test
    public void testToString(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        ObjectResult result = new BValidatorAnnotationBuilder<>(BusinessObjectMocks.DefaultValidableMock.class).build().validate(object);
        String stringResult = result.toString();
        assertTrue(stringResult.contains("validable-mock [rule01] mandatoryAttribute must be defined. => valid"));
        assertTrue(stringResult.contains("validable-mock oneOrMoreAssociation must have at least one element. => valid"));
        assertTrue(stringResult.contains("validable-mock optionalAttribute must be defined if present. => valid"));
    }

    @Test
    public void testGetRuleResultCorrect(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        ObjectResult result = new BValidatorAnnotationBuilder<>(BusinessObjectMocks.DefaultValidableMock.class).build().validate(object);
        RuleResult ruleResult = result.getRuleResult("validable-mock [rule01]");
        assertTrue(ruleResult.isValid());
    }

    @Test
    public void testGetRuleResultIncorrectRoot(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        ObjectResult result = new BValidatorAnnotationBuilder<>(BusinessObjectMocks.DefaultValidableMock.class).build().validate(object);
        Throwable throwable = assertThrows(IllegalArgumentException.class ,() -> result.getRuleResult("wrongRoot [rule01]"));
        assertEquals("Rule path does not start with the root object name", throwable.getMessage());
    }

    @Test
    public void testGetRuleResultIncorrectRule(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        ObjectResult result = new BValidatorAnnotationBuilder<>(BusinessObjectMocks.DefaultValidableMock.class).build().validate(object);
        assertNull(result.getRuleResult("validable-mock [wrongRule]"));
    }

    @Test
    public void testGetRuleResultWithMemberCorrect(){
        Address address = new Address("street", new City("city",-12345), "country");
        ObjectResult result = addressValidator.validate(address);
        assertTrue(result.getRuleResult("Address [cityValid]").isValid());
        assertTrue(result.getRuleResult("Address [StreetValid]").isValid());
        assertTrue(result.getRuleResult("Address.city [cityNameValid]").isValid());
        assertFalse(result.getRuleResult("Address.city [cityZipcodeValid]").isValid());
    }

    @Test
    public void testGetRuleResultWithMemberIncorrect(){
        Address address = new Address("street", new City("",-12345), "country");
        ObjectResult result = addressValidator.validate(address);
        Throwable throwable = assertThrows(IllegalArgumentException.class ,() -> result.getRuleResult("Address.wrongMember [cityNameValid]"));
        assertEquals("Rule path does not match any member", throwable.getMessage());
    }

    @Test
    public void testGetRuleResultWithListMemberCorrect(){
        Person person = new Person(null,null,null,null,
                List.of(new Phone("123456789", "+33"), new Phone("987654321", "aa"))
        );
        ObjectResult result = personValidatorWithPhones.validate(person);
        System.out.println(result);
        assertTrue(result.getRuleResult("Person.phones[0] [numberValid]").isValid());
        assertTrue(result.getRuleResult("Person.phones[0] [countryCodeValid]").isValid());
        assertTrue(result.getRuleResult("Person.phones[1] [numberValid]").isValid());
        assertFalse(result.getRuleResult("Person.phones[1] [countryCodeValid]").isValid());
    }

}
