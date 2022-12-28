package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BValidatorBuilderTest {

    @Test
    public void testBuildValidatorCorrectValidObject(){
        BValidatorBuilderImpl<Person> builder = createCompleteBuilder();
        assertEquals(3, builder.getRulesCount());
        assertEquals(3, builder.getMembersCount());
        ObjectResult result = builder.build().validate(createAllCorrectPerson());
        assertTrue(result.isValid());
        for(ObjectResult memberResult : result.getMemberResults()){
            assertTrue(memberResult.toString().contains("true"));
        }
        for(RuleResult ruleResult : result.getRuleResults()){
            assertTrue(ruleResult.toString().contains("true"));
        }
        assertMemberResults(result, true);

    }

    @Test
    public void testBuildValidatorCorrectInvalidObject(){
        BValidatorBuilderImpl<Person> builder = createCompleteBuilder();
        assertEquals(3, builder.getRulesCount());
        assertEquals(3, builder.getMembersCount());
        ObjectResult result = builder.build().validate(createPersonWithIncorrectEmailAndPhone());
        assertFalse(result.isValid());
        assertFalse(result.getMemberResults().get(0).getRuleResults().get(1).isValid());
        assertFalse(result.getMemberResults().get(4).getRuleResults().get(1).isValid());
    }

    @Test
    void testBuildValidatorEmpty(){
        BValidatorBuilderImpl<Person> builder = new BValidatorBuilderImpl<>();
        assertEquals(0, builder.getRulesCount());
        assertEquals(0, builder.getMembersCount());
        Throwable exception = assertThrows(IllegalBusinessObjectException.class, builder::build);
        assertEquals("Rules or members must be provided for a business object: ", exception.getMessage());
    }

    @Test
    void testBuildValidatorWithSameRuleId(){
        BValidatorBuilderImpl<Person> validatorBuilder = new BValidatorBuilderImpl<Person>()
                .addRule("rule1", p->true, "Always true")
                .addRule("rule1", p->true, "Always true");
        assertEquals(1, validatorBuilder.getRulesCount());
    }


    @Test
    void testBuildValidatorWithThrowRules(){
        BValidator<Person> validator = new BValidatorBuilderImpl<Person>()
                        .addRule("rule1",
                                p->{throw new IllegalStateException("Exception in rule1");},
                                "Name must not be null")
                        .build();
        Person person = createAllCorrectPerson();
        Throwable throwable = assertThrows(IllegalStateException.class, ()->validator.validate(person));
        assertEquals("Exception in rule1", throwable.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidMembers")
    void testBuildValidatorWithNullMember(String memberName, Function<Person, ?> memberFunction, BValidatorBuilder<Person> validatorSupplier){
        assertThrows(IllegalArgumentException.class, ()->new BValidatorBuilderImpl<Person>()
                .addMember(memberName, memberFunction, validatorSupplier));
    }


    @ParameterizedTest
    @MethodSource("provideInvalidRules")
    void testBuildValidatorWithNullRule(String ruleId, Predicate<Person> rule, String message){
        assertThrows(IllegalArgumentException.class, ()->new BValidatorBuilderImpl<Person>()
                .addRule(ruleId, rule, message));
    }

    @Test
    void testBuildValidatorWithNullMemberGetter(){
        new BValidatorBuilderImpl<Person>()
                .addMember("name", (Function<Person, ?>) p->null, new BValidatorBuilderImpl<Address>()
                        .addRule("rule1", s->true, "Always true"))
                .build()
                .validate(createAllCorrectPerson());
    }

    @Test
    void testBuildValidatorWithWrongMemberCollectionType(){
        assertThrows(IllegalBusinessObjectException.class , () -> new BValidatorBuilderImpl<Person>()
                .addMember("Adress", (Function<Person, ?>) p->List.of(new Phone("11","+22")), new BValidatorBuilderImpl<Address>()
                        .addRule("rule1", s->true, "Always true"))
                .build()
                .validate(createAllCorrectPerson()));

    }

    @Test
    void testCompareRules(){
        BusinessRuleObject<Person> rule1 = new BusinessRuleObject<>("rule1", p->true, "Always true");
        BusinessRuleObject<Person> rule2 = new BusinessRuleObject<>("rule1", p->true, "Always true");
        BusinessRuleObject<Person> rule3 = new BusinessRuleObject<>("rule2", p->true, "Always true");
        assertEquals(rule1, rule2);
        assertNotEquals(rule1, rule3);
    }


    private void assertMemberResults(ObjectResult result, boolean expected){
        for(ObjectResult memberResult : result.getMemberResults()){
            assertEquals(expected, memberResult.isValid());
            assertMemberResults(memberResult, expected);
        }
    }

    private static Stream<Arguments> provideInvalidMembers(){
        return Stream.of(
                Arguments.of(null, (Function<Person, Object>) Person::getAddress, null),
                Arguments.of("name", null, null),
                Arguments.of("name", (Function<Person, Object>) Person::getAddress, null)
        );
    }

    private static Stream<Arguments> provideInvalidRules(){
        return Stream.of(
                Arguments.of(null, (Predicate<Person>) p->true, "message"),
                Arguments.of("rule1", null, "message"),
                Arguments.of(null, null, null)
        );
    }

    private Person createAllCorrectPerson() {
        return new Person(
                "John",
                new Address("Main Street", new City("Paris", 75000), "France"),
                35,
                new Email[]{new Email("aa@bb.cc", "bb.cc"), new Email("dd@ee.ff", "ee.ff")},
                List.of(new Phone("123456789", "+11"), new Phone("987654321", "+22")));
    }

    private Person createPersonWithIncorrectEmailAndPhone() {
        return new Person(
                "John",
                new Address("Main Street", new City("Paris", 75000), "France"),
                35,
                new Email[]{new Email("aa/bb.com", "bb.cc"), new Email("aa@bb.com", "bb.cc")},
                List.of(new Phone("123456789", "+11"), new Phone("987654321", "-22")));
    }



    private BValidatorBuilderImpl<Person> createCompleteBuilder(){
        return new BValidatorBuilderImpl<Person>()
                .setBusinessObjectName("Person")
                .addRule("ageValid", Person::isAgeValid, "Name must not be null")
                .addRule("NameNotEmpty", Person::isNameValid, "Name must not be empty")
                .addRule("ValidEmail", Person::isEmailValid, "Email must be valid")
                .addMember("address", Person::getAddress, new BValidatorBuilderImpl<Address>()
                        .setBusinessObjectName("Address")
                        .addRule("cityValid", Address::isCityValid, "City must not be null")
                        .addRule("StreetValid", Address::isStreetValid, "Street must not be empty")
                        .addMember("city", Address::getCity,  new BValidatorBuilderImpl<City>()
                                .setBusinessObjectName("City")
                                .addRule("cityNameValid", City::isNamesValid, "City name must not be empty")
                                .addRule("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                                )
                        )
                .addMember("phones", Person::getPhones,  new BValidatorBuilderImpl<Phone>()
                        .setBusinessObjectName("Phone")
                        .addRule("numberValid", Phone::isNumberValid, "Number must not be null")
                        .addRule("countryCodeValid", Phone::isCountryCodeValid, "Country code must not be valid")
                        )
                .addMember("emails", Person::getEmails, new BValidatorBuilderImpl<Email>()
                        .setBusinessObjectName("Email")
                        .addRule("emailValid", Email::isEmailValid, "Email must be valid")
                        .addRule("domainValid", Email::isDomainValid, "Domain must be valid")
                        );
    }



}
