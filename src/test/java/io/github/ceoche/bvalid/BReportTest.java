/*
 * Copyright 2025. Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
                .setObjectName("address")
                .addAssertion( "CityValid", Address::isCityValid, "City must not be null")
                .addAssertion("StreetValid", Address::isStreetValid, "Street must not be empty")
                .addMember("city", Address::getCity, new BValidatorBuilder<>(City.class)
                        .setObjectName("city")
                        .addAssertion("cityNameValid", City::isNamesValid, "City name must not be empty")
                        .addAssertion("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                )
                .build();
        personValidatorWithPhones = new BValidatorBuilder<>(Person.class)
                .setObjectName("person")
                .addMember("phones", Person::getPhones, new BValidatorBuilder<>(Phone.class)
                        .setObjectName("Phone")
                        .addAssertion("numberValid", Phone::isNumberValid, "Number must not be null")
                        .addAssertion("countryCodeValid", Phone::isCountryCodeValid, "Country code must not be valid")
                )
                .build();
    }

    @Test
    void testGetNbOfAssertions(){
        ObjectMocks.DefaultValidableMock object = ObjectMocks.instantiateValid();
        BReport result = new AnnotationResolver<>(ObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        assertEquals(3, result.getNbOfAssertions());
    }

    @Test
    void testToString(){
        ObjectMocks.DefaultValidableMock object = ObjectMocks.instantiateValid();
        BReport result = new AnnotationResolver<>(ObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        String stringResult = result.toString();
        assertTrue(stringResult.contains("validable-mock [rule01] mandatoryAttribute must be defined. => valid"));
        assertTrue(stringResult.contains("validable-mock oneOrMoreAssociation must have at least one element. => valid"));
        assertTrue(stringResult.contains("validable-mock optionalAttribute must be defined if present. => valid"));
    }

    @Test
    void testAssertAPIInvalid() {
        ObjectMocks.ArrayBusinessMember object = ObjectMocks.instantiateBusinessMemberArray();
        BValidator<ObjectMocks.ArrayBusinessMember> validator = getValidator(
              ObjectMocks.ArrayBusinessMember.class);
        assertThrows(
              IllegalArgumentException.class,
              () -> validator.validate(object).orThrow(IllegalArgumentException::new)
        );
    }

    @Test
    void testAssertAPIValid() {
        ObjectMocks.DefaultValidableMock object = ObjectMocks.instantiateValid();
        BValidator<ObjectMocks.DefaultValidableMock> validator = getValidator(
              ObjectMocks.DefaultValidableMock.class);
        Assertions.assertDoesNotThrow(
              () -> validator.validate(object).orThrow(IllegalArgumentException::new)
        );
    }

    private <T> BValidator<T> getValidator(Class<T> clazz) {
        return new AnnotationResolver<>(clazz).buildValidator();
    }

    @Test
    void testGetRuleResultCorrect(){
        ObjectMocks.DefaultValidableMock object = ObjectMocks.instantiateValid();
        BReport result = new AnnotationResolver<>(ObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        AssertionReport assertionReport = getRuleResult(result, "validable-mock [rule01]");
        assertTrue(assertionReport.isValid());
    }

    @Test
    void testGetRuleResultIncorrectRoot(){
        ObjectMocks.DefaultValidableMock object = ObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        BReport result = new AnnotationResolver<>(ObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
        Throwable throwable = assertThrows(IllegalArgumentException.class ,() -> getRuleResult(result, "wrongRoot [rule01]"));
        assertEquals("Rule path does not start with the root object name", throwable.getMessage());
    }

    @Test
    void testGetRuleResultIncorrectRule(){
        ObjectMocks.DefaultValidableMock object = ObjectMocks.instantiateValid();
        object.setMandatoryAttribute("  ");
        BReport result = new AnnotationResolver<>(ObjectMocks.DefaultValidableMock.class).buildValidator().validate(object);
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
        if(!path[0].equals(result.getObjectName())) {
            throw new IllegalArgumentException("Rule path does not start with the root object name");
        }
        if(path.length == 1) {
            throw new IllegalArgumentException("Rule path must contain at least one member");
        }
        if(elementIsRule(path[1])) {
            for (AssertionReport assertionReport : currentReport.getAssertionReports()) {
                if(assertionReport.getId().equals(path[1].substring(1, path[1].length() - 1))) {
                    return assertionReport;
                }
            }
        }
        else {
            BReport memberResult = result.getMemberReports().stream()
                    .filter(bReport -> bReport.getObjectName().equals(path[1]))
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
