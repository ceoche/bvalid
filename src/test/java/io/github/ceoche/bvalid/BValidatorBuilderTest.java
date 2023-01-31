package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BValidatorBuilderTest {

    @Test
    public void testBuildValidatorCorrectValidObject() {
        BValidatorManualBuilder<Person> builder = createCompleteBuilder();
        assertEquals(3, builder.getRulesCount());
        assertEquals(3, builder.getMembersCount());
        ObjectResult result = builder.build().validate(createAllCorrectPerson());
        assertTrue(result.isValid());
        for (ObjectResult memberResult : result.getMemberResults()) {
            assertTrue(memberResult.toString().contains("true"));
        }
        for (RuleResult ruleResult : result.getRuleResults()) {
            assertTrue(ruleResult.toString().contains("true"));
        }
        assertMemberResults(result, true);
        System.out.println(result);

    }

    @Test
    public void testBuildValidatorCorrectInvalidObject() {
        BValidatorManualBuilder<Person> builder = createCompleteBuilder();
        assertEquals(3, builder.getRulesCount());
        assertEquals(3, builder.getMembersCount());
        ObjectResult result = builder.build().validate(createPersonWithIncorrectEmailAndPhone());
        System.out.println(result);
        assertFalse(result.isValid());
        assertFalse(result.getRuleResult("Person.phones[1] [countryCodeValid]").isValid());
        assertFalse(result.getRuleResult("Person.emails[0] [emailValid]").isValid());
    }

    @Test
    void testBuildValidatorEmpty() {
        BValidatorManualBuilder<Person> builder = new BValidatorManualBuilder<>();
        assertEquals(0, builder.getRulesCount());
        assertEquals(0, builder.getMembersCount());
        assertTrue(builder.isEmpty());
        Throwable exception = assertThrows(IllegalBusinessObjectException.class, builder::build);
        assertEquals("Rules or members must be provided for a business object: ", exception.getMessage());
    }

    @Test
    void testBuildValidatorWithSameRuleId() {
        BValidatorManualBuilder<Person> validatorBuilder = new BValidatorManualBuilder<Person>()
                .addRule("rule1", p -> true, "Always true")
                .addRule("rule1", p -> true, "Always true");
        assertEquals(1, validatorBuilder.getRulesCount());
    }


    @Test
    void testBuildValidatorWithThrowRules() {
        BValidator<Person> validator = new BValidatorManualBuilder<Person>()
                .addRule("rule1",
                        p -> {
                            throw new IllegalStateException("Exception in rule1");
                        },
                        "Name must not be null")
                .build();
        Person person = createAllCorrectPerson();
        Throwable throwable = assertThrows(IllegalStateException.class, () -> validator.validate(person));
        assertEquals("Exception in rule1", throwable.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidMembers")
    void testBuildValidatorWithNullMember(String memberName, Function<Person, ?> memberFunction, BValidatorBuilder<Person> validatorSupplier) {
        assertThrows(IllegalArgumentException.class, () -> new BValidatorManualBuilder<Person>()
                .addMember(memberName, memberFunction, validatorSupplier));
    }


    @ParameterizedTest
    @MethodSource("provideInvalidRules")
    void testBuildValidatorWithNullRule(String ruleId, Predicate<Person> rule, String message) {
        assertThrows(IllegalArgumentException.class, () -> new BValidatorManualBuilder<Person>()
                .addRule(ruleId, rule, message));
    }

    @Test
    void testBuildValidatorWithNullMemberGetter() {
        new BValidatorManualBuilder<Person>()
                .addMember("name", (Function<Person, ?>) p -> null, new BValidatorManualBuilder<Address>()
                        .addRule("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson());
    }

    @Test
    void testBuildValidatorWithWrongMemberCollectionType() {
        assertThrows(IllegalBusinessObjectException.class, () -> new BValidatorManualBuilder<Person>()
                .addMember("Address", (Function<Person, ?>) p -> List.of(new Phone("11", "+22")), new BValidatorManualBuilder<Address>()
                        .addRule("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson()));

    }

    @Test
    void testBuildValidatorWithEmptyMemberCollection() {
        new BValidatorManualBuilder<Person>()
                .addMember("Address", (Function<Person, ?>) p -> List.of(), new BValidatorManualBuilder<Address>()
                        .addRule("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson());
    }

    @Test
    void testCompareRules() {
        BusinessRuleObject<Person> rule1 = new BusinessRuleObject<>("rule1", p -> true, "Always true");
        BusinessRuleObject<Person> rule2 = new BusinessRuleObject<>("rule1", p -> true, "Always true");
        BusinessRuleObject<Person> rule3 = new BusinessRuleObject<>("rule2", p -> true, "Always true");
        assertEquals(rule1, rule2);
        assertNotEquals(rule1, rule3);
        assertEquals(rule1, rule1);
        assertNotEquals("rule1", rule1);
    }

    @Test
    void testCompareMembers() {
        BusinessMemberObject<Person, Address> member1 = new BusinessMemberObject<>("member1",
                Person::getAddress,
                new BValidatorManualBuilder<Address>()
                        .addRule("rule1", s -> true, "Always true")
                        .build());
        BusinessMemberObject<Person, Email> member2 = new BusinessMemberObject<>("member1", Person::getEmails,
                new BValidatorManualBuilder<Email>()
                        .addRule("rule1", s -> true, "Always true")
                        .build());
        BusinessMemberObject<Person, Phone> member3 = new BusinessMemberObject<>("member2", Person::getPhones,
                new BValidatorManualBuilder<Phone>()
                        .addRule("rule1", s -> true, "Always true")
                        .build());
        assertEquals(member1, member2);
        assertNotEquals(member1, member3);
        assertEquals(member1, member1);
        assertNotEquals("member1", member1);

    }

    @Test
    void testRecursiveObject() {
        BValidatorManualBuilder<FirstRecursiveObject> builder = new BValidatorManualBuilder<FirstRecursiveObject>()
                .setBusinessObjectName("FirstRecursiveObject");
        builder.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "Always true");
        builder.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builder);
        BValidator<FirstRecursiveObject> validator = builder.build();
        FirstRecursiveObject firstRecursiveObject = new FirstRecursiveObject()
                .setAttr1("attr1")
                .setFirstRecursiveObject(new FirstRecursiveObject()
                        .setAttr1("attr2")
                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                .setAttr1("attr3")
                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                        .setAttr1("attr4"))));
        ObjectResult result = validator.validate(firstRecursiveObject);
        System.out.println(result);
        assertTrue(result.isValid());
    }

    @Test
    void testRecursiveObjectWithNull() {
        BValidatorManualBuilder<FirstRecursiveObject> builder = new BValidatorManualBuilder<FirstRecursiveObject>()
                .setBusinessObjectName("FirstRecursiveObject");
        builder.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "Always true");
        builder.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builder);
        BValidator<FirstRecursiveObject> validator = builder.build();
        FirstRecursiveObject firstRecursiveObject = new FirstRecursiveObject()
                .setAttr1("attr1")
                .setFirstRecursiveObject(new FirstRecursiveObject()
                        .setAttr1("attr2")
                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                .setAttr1(null)));

        ObjectResult result = validator.validate(firstRecursiveObject);
        System.out.println(result);
        assertFalse(result.isValid());
        assertFalse(result.getMemberResults().get(0).getMemberResults().get(0).getRuleResults().get(0).isValid());

    }

    @Test
    void testRecursiveObjectWithSubElement() {
        BValidatorManualBuilder<FirstRecursiveObject> builder = new BValidatorManualBuilder<FirstRecursiveObject>()
                .setBusinessObjectName("FirstRecursiveObject");
        builder.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builder.addRule("emailValid", FirstRecursiveObject::isEmailValid, "Email must be valid");
        builder.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builder);
        builder.addMember("email", FirstRecursiveObject::getEmail, new BValidatorManualBuilder<Email>()
                        .addRule("emailValid", Email::isEmailValid, "Email must be valid")
                        .addRule("domainValid", Email::isDomainValid, "Domain must be valid"))
                .build();
        BValidator<FirstRecursiveObject> validator = builder.build();
        FirstRecursiveObject firstRecursiveObject = new FirstRecursiveObject()
                .setAttr1("attr1")
                .setFirstRecursiveObject(new FirstRecursiveObject()
                        .setAttr1("attr2")
                        .setEmail(new Email("aa@bb.com", "aa.com")))
                .setEmail(new Email("bb@vv", "aa.com"));
        ObjectResult result = validator.validate(firstRecursiveObject);
        System.out.println(result);
        assertTrue(result.isValid());
    }

    @Test
    void testCrossRecursiveObject() {
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<FirstRecursiveObject>()
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorManualBuilder<SecondRecursiveObject> builderSecond = new BValidatorManualBuilder<SecondRecursiveObject>()
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addRule("emailValid", FirstRecursiveObject::isEmailValid, "Email must be valid");
        builderFirst.addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addRule("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("email", FirstRecursiveObject::getEmail, new BValidatorManualBuilder<Email>()
                .addRule("emailValid", Email::isEmailValid, "Email must be valid")
                .addRule("domainValid", Email::isDomainValid, "Domain must be valid"));
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addRule("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addRule("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

        builderSecond.build();

        ObjectResult result = builderFirst.build().validate(new FirstRecursiveObject()
                .setAttr1("attr1")
                .setEmail(new Email("aa@bb", "ff.v"))
                .setFirstRecursiveObject(new FirstRecursiveObject()
                        .setAttr1("attr1.2")
                        .setEmail(new Email("aa@vv", "ff.v"))
                        .setSecondRecursiveObject(new SecondRecursiveObject()
                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                        .setAttr1("attr2.1"))
                                .setAttr2("attr2")))
                .setSecondRecursiveObject(new SecondRecursiveObject()
                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                .setAttr1(null))
                        .setAttr2("attr2")));
        assertEquals(19, countAllRulesResults(result, true));
        assertEquals(5, countAllRulesResults(result, false));
        assertFalse(result.getRuleResult("FirstRecursiveObject.secondRecursiveObject.firstRecursiveObject [secondRecursiveObjectValid]").isValid());
        assertFalse(result.getRuleResult("FirstRecursiveObject.secondRecursiveObject.firstRecursiveObject [rule1]").isValid());
        assertFalse(result.getRuleResult("FirstRecursiveObject.secondRecursiveObject.firstRecursiveObject [emailValid]").isValid());
        assertFalse(result.getRuleResult("FirstRecursiveObject.firstRecursiveObject.secondRecursiveObject.firstRecursiveObject [secondRecursiveObjectValid]").isValid());
        assertFalse(result.getRuleResult("FirstRecursiveObject.firstRecursiveObject.secondRecursiveObject.firstRecursiveObject [emailValid]").isValid());

    }

    @Test
    public void testCrossCollectionRecursiveObject(){
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<FirstRecursiveObject>()
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorManualBuilder<SecondRecursiveObject> builderSecond = new BValidatorManualBuilder<SecondRecursiveObject>()
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
//        builderFirst.addRule("emailValid", FirstRecursiveObject::isEmailValid, "Email must be valid");
        builderFirst.addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addRule("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
//        builderFirst.addMember("email", FirstRecursiveObject::getEmail, new BValidatorManualBuilder<Email>()
//                .addRule("emailValid", Email::isEmailValid, "Email must be valid")
//                .addRule("domainValid", Email::isDomainValid, "Domain must be valid"));
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addRule("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addRule("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

        builderSecond.build();

        BValidatorManualBuilder<CollectionRecursiveObject> builderCollection = new BValidatorManualBuilder<CollectionRecursiveObject>()
                .setBusinessObjectName("CollectionRecursiveObject")
                .addRule("rule3", CollectionRecursiveObject::isCollectionAttrValid, "collection attr is not null")
                .addRule("firstRecursiveObjectValid", CollectionRecursiveObject::isFirstRecursiveObjectsValid, "firstRecursiveObject in collectionRecursiveObject must be valid")
                .addRule("secondRecursiveObjectValid", CollectionRecursiveObject::isSecondRecursiveObjectsValid, "secondRecursiveObject in collectionRecursiveObject must be valid")
                .addMember("firstRecursiveObjects", CollectionRecursiveObject::getFirstRecursiveObjects, builderFirst)
                .addMember("secondRecursiveObjects", CollectionRecursiveObject::getSecondRecursiveObjects, builderSecond);

        builderSecond.build();
        builderFirst.build();


        ObjectResult result = builderCollection.build().validate(new CollectionRecursiveObject()
                .setCollectionAttr("collectionAttr")
                .setFirstRecursiveObjects(new FirstRecursiveObject[]{
                        new FirstRecursiveObject()
                                .setAttr1("attr[0]1")
                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                        .setAttr1("attr[0]1.2")
                                        .setSecondRecursiveObject(new SecondRecursiveObject()
                                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                                        .setAttr1("attr2.1"))
                                                .setAttr2("attr2")))
                                .setSecondRecursiveObject(new SecondRecursiveObject()
                                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                                .setAttr1(null))
                                        .setAttr2("attr2")),
                        new FirstRecursiveObject()
                                .setAttr1("attr[1]1")
                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                        .setAttr1("attr[0]1.2")
                                        .setEmail(new Email("aa@vv", "ff.v"))
                        )
                })
                .setSecondRecursiveObjects(List.of(
                        new SecondRecursiveObject()
                                .setAttr2("attr[0]2")
                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                        .setAttr1("attr[0]2.1")
                                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                                .setAttr1("attr[0]2.1.1")
                                                .setSecondRecursiveObject(new SecondRecursiveObject()
                                                        .setAttr2("attr[0]2.1.1.2")
                                                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                                                .setAttr1("attr[0]2.1.1.2.1"))
                                                )
                                        )
                                        .setSecondRecursiveObject(new SecondRecursiveObject()
                                                .setAttr2("attr[0]2.1.2")
                                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                                        .setAttr1(null)) // invalid attr[0]2.1.2.1
                                        )
                                )
                        ,
                        new SecondRecursiveObject()
                                .setAttr2("attr[1]2")
                                .setFirstRecursiveObject(new FirstRecursiveObject()
                                        .setAttr1("attr[1]2.1")
                                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                                .setAttr1("attr[1]2.1.1")
                                        )
                                )
                        )
                )
        );
        assertFalse(result.isValid());
        assertEquals(10, countAllRulesResults(result, false));
        assertFalse(result.getRuleResult("CollectionRecursiveObject.secondRecursiveObjects[0].firstRecursiveObject.secondRecursiveObject.firstRecursiveObject [secondRecursiveObjectValid]").isValid());

    }

    @Test
    public void testRecursiveLoopObject() {
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<FirstRecursiveObject>()
                .setBusinessObjectName("FirstRecursiveObject")
                .addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null")
                .addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);

        FirstRecursiveObject firstLoopObject = new FirstRecursiveObject().setAttr1("attr1");
        firstLoopObject.setFirstRecursiveObject(firstLoopObject);

        ObjectResult result = builderFirst.build().validate(firstLoopObject);

        System.out.println(result);

        assertTrue(result.isValid());
        assertEquals(4, result.getNbOfTests());
        assertEquals(4, countAllRulesResults(result, true));

    }

    @Test
    public void testRecursiveCrossLoopObject() {
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<FirstRecursiveObject>()
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorManualBuilder<SecondRecursiveObject> builderSecond = new BValidatorManualBuilder<SecondRecursiveObject>()
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addRule("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addRule("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addRule("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

//        builderSecond.build();

//        builderSecond.build();
//        builderFirst.build();

        FirstRecursiveObject firstLoopObject = new FirstRecursiveObject().setAttr1("attr1");
        firstLoopObject.setFirstRecursiveObject(firstLoopObject);
        SecondRecursiveObject secondLoopObject = new SecondRecursiveObject().setAttr2("attr2");
        firstLoopObject.setSecondRecursiveObject(secondLoopObject);
        secondLoopObject.setFirstRecursiveObject(firstLoopObject);

        ObjectResult result = builderFirst.build().validate(firstLoopObject);
        assertTrue(result.isValid());
        assertEquals(8, result.getNbOfTests());
    }

    private void assertMemberResults(ObjectResult result, boolean expected) {
        for (ObjectResult memberResult : result.getMemberResults()) {
            assertEquals(expected, memberResult.isValid());
            assertMemberResults(memberResult, expected);
        }
    }

    private static Stream<Arguments> provideInvalidMembers() {
        return Stream.of(
                Arguments.of(null, (Function<Person, Object>) Person::getAddress, null),
                Arguments.of("name", null, null),
                Arguments.of("name", (Function<Person, Object>) Person::getAddress, null)
        );
    }

    private static Stream<Arguments> provideInvalidRules() {
        return Stream.of(
                Arguments.of(null, (Predicate<Person>) p -> true, "message"),
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


    private BValidatorManualBuilder<Person> createCompleteBuilder() {
        return new BValidatorManualBuilder<Person>()
                .setBusinessObjectName("Person")
                .addRule("ageValid", Person::isAgeValid, "Name must not be null")
                .addRule("NameNotEmpty", Person::isNameValid, "Name must not be empty")
                .addRule("ValidEmail", Person::isEmailValid, "Email must be valid")
                .addMember("address", Person::getAddress, new BValidatorManualBuilder<Address>()
                        .setBusinessObjectName("Address")
                        .addRule("cityValid", Address::isCityValid, "City must not be null")
                        .addRule("StreetValid", Address::isStreetValid, "Street must not be empty")
                        .addMember("city", Address::getCity, new BValidatorManualBuilder<City>()
                                .setBusinessObjectName("City")
                                .addRule("cityNameValid", City::isNamesValid, "City name must not be empty")
                                .addRule("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                        )
                )
                .addMember("phones", Person::getPhones, new BValidatorManualBuilder<Phone>()
                        .setBusinessObjectName("Phone")
                        .addRule("numberValid", Phone::isNumberValid, "Number must not be null")
                        .addRule("countryCodeValid", Phone::isCountryCodeValid, "Country code must not be valid")
                )
                .addMember("emails", Person::getEmails, new BValidatorManualBuilder<Email>()
                        .setBusinessObjectName("Email")
                        .addRule("emailValid", Email::isEmailValid, "Email must be valid")
                        .addRule("domainValid", Email::isDomainValid, "Domain must be valid")
                );
    }

    //count number of (Ture of False) rules in the result recursively
    private int countAllRulesResults(ObjectResult result, boolean expected) {
        int count = countRulesResults(result, expected);
        for (ObjectResult memberResult : result.getMemberResults()) {
            count += countAllRulesResults(memberResult, expected);
        }
        return count;
    }

    public int countRulesResults(ObjectResult result, boolean expected) {
        int count = 0;
        for (RuleResult ruleResult : result.getRuleResults()) {
            if (ruleResult.isValid() == expected) {
                count++;
            }
        }
        return count;
    }

}
