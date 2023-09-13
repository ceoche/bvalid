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
                .setBusinessObjectName("address")
                .addRule( "CityValid", Address::isCityValid, "City must not be null")
                .addRule("StreetValid", Address::isStreetValid, "Street must not be empty")
                .addMember("city", Address::getCity, new BValidatorManualBuilder<>(City.class)
                        .setBusinessObjectName("city")
                        .addRule("cityNameValid", City::isNamesValid, "City name must not be empty")
                        .addRule("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                )
                .build();
        personValidatorWithPhones = new BValidatorManualBuilder<>(Person.class)
                .setBusinessObjectName("person")
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
        RuleResult ruleResult = getRuleResult(result, "validable-mock [rule01]");
        assertTrue(ruleResult.isValid());
    }

    @Test
    public void testGetRuleResultIncorrectRoot(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        ObjectResult result = new BValidatorAnnotationBuilder<>(BusinessObjectMocks.DefaultValidableMock.class).build().validate(object);
        Throwable throwable = assertThrows(IllegalArgumentException.class ,() -> getRuleResult(result, "wrongRoot [rule01]"));
        assertEquals("Rule path does not start with the root object name", throwable.getMessage());
    }

    @Test
    public void testGetRuleResultIncorrectRule(){
        BusinessObjectMocks.DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        ObjectResult result = new BValidatorAnnotationBuilder<>(BusinessObjectMocks.DefaultValidableMock.class).build().validate(object);
        assertNull(getRuleResult(result, "validable-mock [wrongRule]"));
    }

    @Test
    public void testGetRuleResultWithMemberCorrect(){
        Address address = new Address("street", new City("city",-12345), "country");
        ObjectResult result = addressValidator.validate(address);
        assertTrue(getRuleResult(result, "address [CityValid]").isValid());
        assertTrue(getRuleResult(result, "address [StreetValid]").isValid());
        assertTrue(getRuleResult(result, "address.city [cityNameValid]").isValid());
        assertFalse(getRuleResult(result, "address.city [cityZipcodeValid]").isValid());
    }

    @Test
    public void testGetRuleResultWithMemberIncorrect(){
        Address address = new Address("street", new City("",-12345), "country");
        ObjectResult result = addressValidator.validate(address);
        Throwable throwable = assertThrows(IllegalArgumentException.class ,() -> getRuleResult(result, "address.wrongMember [cityNameValid]"));
        assertEquals("Rule path does not match any member", throwable.getMessage());
    }

    @Test
    public void testGetRuleResultWithListMemberCorrect(){
        Person person = new Person(null,null,null,null,
                List.of(new Phone("123456789", "+33"), new Phone("987654321", "aa"))
        );
        ObjectResult result = personValidatorWithPhones.validate(person);
        System.out.println(result);
        assertTrue(getRuleResult(result, "person.phones[0] [numberValid]").isValid());
        assertTrue(getRuleResult(result, "person.phones[0] [countryCodeValid]").isValid());
        assertTrue(getRuleResult(result, "person.phones[1] [numberValid]").isValid());
        assertFalse(getRuleResult(result, "person.phones[1] [countryCodeValid]").isValid());
    }

    // get RuleResult path from root, ex: "person.address.street[streetNameValid]"

    /**
     * Get the path of a {@link RuleResult} from the root of the {@link ObjectResult} tree.
     * @param rulePath Ex: "person.address.street[streetNameValid]"
     *
     * The path is composed of the business object name, followed by the path of
     * the member, followed by the id of the rule.
     *
     *     <ul>
     *         <li>person is the root {@link ObjectResult}</li>
     *         <li>address is the businessObjectName of {@link BusinessMember} person</li>
     *         <li>street is the businessObjectName of {@link BusinessMember} address</li>
     *         <li>streetNameValid is the id of {@link BusinessRule} street</li>
     *     </ul>
     *
     * FIXME: Rule path does not work with rules that does not have an id.
     *
     * @return the {@link RuleResult} or null if not found.
     * @throws IllegalArgumentException if a member is not found.
     */
    public static RuleResult getRuleResult(ObjectResult result, String rulePath) {
        String[] path = rulePath.split("[\\.\\s]");
        ObjectResult currentObjectResult = result;
        if(!path[0].equals(result.getBusinessObjectName())) {
            throw new IllegalArgumentException("Rule path does not start with the root object name");
        }
        if(path.length == 1) {
            throw new IllegalArgumentException("Rule path must contain at least one member");
        }
        if(elementIsRule(path[1])) {
            for (RuleResult ruleResult : currentObjectResult.getRuleResults()) {
                if(ruleResult.getId().equals(path[1].substring(1, path[1].length() - 1))) {
                    return ruleResult;
                }
            }
        }
        else {
            ObjectResult memberResult = result.getMemberResults().stream()
                    .filter(objectResult -> objectResult.getBusinessObjectName().equals(path[1]))
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
