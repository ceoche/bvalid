package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.Address;
import io.github.ceoche.bvalid.mock.City;
import io.github.ceoche.bvalid.mock.Person;
import io.github.ceoche.bvalid.mock.Phone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BReportTest {

    private static BValidator<Address> addressValidator;

    private static BValidator<Person> personValidatorWithPhones;

    @BeforeAll
    static void setUp() {
        addressValidator = new BValidatorBuilder<>(Address.class)
                .setBusinessObjectName("address")
                .addAssertion( "CityValid", Address::isCityValid, "City must not be null")
                .addAssertion("StreetValid", Address::isStreetValid, "Street must not be empty")
                .addMember("city", Address::getCity, new BValidatorBuilder<>(City.class)
                        .setBusinessObjectName("city")
                        .addAssertion("cityNameValid", City::isNamesValid, "City name must not be empty")
                        .addAssertion("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                )
                .build();
        personValidatorWithPhones = new BValidatorBuilder<>(Person.class)
                .setBusinessObjectName("person")
                .addMember("phones", Person::getPhones, new BValidatorBuilder<>(Phone.class)
                        .setBusinessObjectName("Phone")
                        .addAssertion("numberValid", Phone::isNumberValid, "Number must not be null")
                        .addAssertion("countryCodeValid", Phone::isCountryCodeValid, "Country code must not be valid")
                )
                .build();
    }

    @Test
    void testGetNbOfTests(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        BReport result = new AnnotationResolver<>(BusinessObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        assertEquals(3, result.getNbOfTests());
    }

    @Test
    void testToString(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        BReport result = new AnnotationResolver<>(BusinessObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        String stringResult = result.toString();
        assertTrue(stringResult.contains("validable-mock [rule01] mandatoryAttribute must be defined. => valid"));
        assertTrue(stringResult.contains("validable-mock oneOrMoreAssociation must have at least one element. => valid"));
        assertTrue(stringResult.contains("validable-mock optionalAttribute must be defined if present. => valid"));
    }

    @Test
    void testAssertAPIInvalid() {
        BusinessObjectMocks.ArrayBusinessMember object = BusinessObjectMocks.instantiateBusinessMemberArray();
        BValidator<BusinessObjectMocks.ArrayBusinessMember> validator = getValidator(
              BusinessObjectMocks.ArrayBusinessMember.class);
        assertThrows(
              IllegalArgumentException.class,
              () -> validator.validate(object).orThrow(IllegalArgumentException::new)
        );
    }

    @Test
    void testAssertAPIValid() {
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        BValidator<BusinessObjectMocks.DefaultValidableMock> validator = getValidator(
              BusinessObjectMocks.DefaultValidableMock.class);
        Assertions.assertDoesNotThrow(
              () -> validator.validate(object).orThrow(IllegalArgumentException::new)
        );
    }

    private <T> BValidator<T> getValidator(Class<T> clazz) {
        return new AnnotationResolver<>(clazz).buildValidator();
    }

    @Test
    void testGetRuleResultCorrect(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        BReport result = new AnnotationResolver<>(BusinessObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        AssertionReport assertionReport = getRuleResult(result, "validable-mock [rule01]");
        assertTrue(assertionReport.isValid());
    }

    @Test
    void testGetRuleResultIncorrectRoot(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        BReport result = new AnnotationResolver<>(BusinessObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        Throwable throwable = assertThrows(IllegalArgumentException.class ,() -> getRuleResult(result, "wrongRoot [rule01]"));
        assertEquals("Rule path does not start with the root object name", throwable.getMessage());
    }

    @Test
    void testGetRuleResultIncorrectRule(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        BReport result = new AnnotationResolver<>(BusinessObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        assertNull(getRuleResult(result, "validable-mock [wrongRule]"));
    }

    @Test
    void testGetRuleResultWithMemberCorrect(){
        Address address = new Address("street", new City("city",-12345), "country");
        BReport result = addressValidator.validate(address);
        assertTrue(getRuleResult(result, "address [CityValid]").isValid());
        assertTrue(getRuleResult(result, "address [StreetValid]").isValid());
        assertTrue(getRuleResult(result, "address.city [cityNameValid]").isValid());
        assertFalse(getRuleResult(result, "address.city [cityZipcodeValid]").isValid());
    }

    @Test
    void testGetRuleResultWithMemberIncorrect(){
        Address address = new Address("street", new City("",-12345), "country");
        BReport result = addressValidator.validate(address);
        Throwable throwable = assertThrows(IllegalArgumentException.class ,() -> getRuleResult(result, "address.wrongMember [cityNameValid]"));
        assertEquals("Rule path does not match any member", throwable.getMessage());
    }

    @Test
    void testGetRuleResultWithListMemberCorrect(){
        Person person = new Person(null,null,null,null,
                List.of(new Phone("123456789", "+33"), new Phone("987654321", "aa"))
        );
        BReport result = personValidatorWithPhones.validate(person);
        System.out.println(result);
        assertTrue(getRuleResult(result, "person.phones[0] [numberValid]").isValid());
        assertTrue(getRuleResult(result, "person.phones[0] [countryCodeValid]").isValid());
        assertTrue(getRuleResult(result, "person.phones[1] [numberValid]").isValid());
        assertFalse(getRuleResult(result, "person.phones[1] [countryCodeValid]").isValid());
    }

    // get RuleResult path from root, ex: "person.address.street[streetNameValid]"

    /**
     * Get the path of a {@link AssertionReport} from the root of the {@link BReport} tree.
     * @param rulePath Ex: "person.address.street[streetNameValid]"
     *
     * The path is composed of the business object name, followed by the path of
     * the member, followed by the id of the rule.
     *
     *     <ul>
     *         <li>person is the root {@link BReport}</li>
     *         <li>address is the businessObjectName of {@link BusinessMember} person</li>
     *         <li>street is the businessObjectName of {@link BusinessMember} address</li>
     *         <li>streetNameValid is the id of {@link BusinessAssertion} street</li>
     *     </ul>
     *
     * FIXME: Rule path does not work with rules that does not have an id.
     *
     * @return the {@link AssertionReport} or null if not found.
     * @throws IllegalArgumentException if a member is not found.
     */
    public static AssertionReport getRuleResult(BReport result, String rulePath) {
        String[] path = rulePath.split("[\\.\\s]");
        BReport currentReport = result;
        if(!path[0].equals(result.getBusinessObjectName())) {
            throw new IllegalArgumentException("Rule path does not start with the root object name");
        }
        if(path.length == 1) {
            throw new IllegalArgumentException("Rule path must contain at least one member");
        }
        if(elementIsRule(path[1])) {
            for (AssertionReport assertionReport : currentReport.getRuleResults()) {
                if(assertionReport.getId().equals(path[1].substring(1, path[1].length() - 1))) {
                    return assertionReport;
                }
            }
        }
        else {
            BReport memberResult = result.getMemberReports().stream()
                    .filter(bReport -> bReport.getBusinessObjectName().equals(path[1]))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Rule path does not match any member"));
            return getRuleResult(memberResult, rulePath.substring(rulePath.indexOf(".") + 1));
        }
        return null;
    }

    private static boolean elementIsRule(String element) {
        return element.startsWith("[") && element.endsWith("]");
    }

}
