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
    void testNullType(){
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> new BValidatorBuilder<>(null));
        assertEquals("Type must not be null.", throwable.getMessage());
    }

    @Test
    public void testBuildValidatorCorrectValidObject() {
        BValidatorBuilder<Person> builder = createCompleteBuilder();
        assertEquals(3, builder.getRulesCount());
        assertEquals(3, builder.getMembersCount());
        BReport result = builder.build().validate(createAllCorrectPerson());
        assertTrue(result.isValid());
        for (BReport memberResult : result.getMemberReports()) {
            assertTrue(memberResult.toString().contains("valid"));
        }
        for (AssertionReport assertionReport : result.getRuleResults()) {
            assertTrue(assertionReport.toString().contains("valid"));
        }
        assertMemberResults(result, true);
        System.out.println(result);

    }

    @Test
    public void testBuildValidatorCorrectInvalidObject() {
        BValidatorBuilder<Person> builder = createCompleteBuilder();
        assertEquals(3, builder.getRulesCount());
        assertEquals(3, builder.getMembersCount());
        BReport result = builder.build().validate(createPersonWithIncorrectEmailAndPhone());
        System.out.println(result);
        assertFalse(result.isValid());
        assertFalse(BReportTest.getRuleResult(result, "Person.phones[1] [countryCodeValid]").isValid());
        assertFalse(BReportTest.getRuleResult(result, "Person.emails[0] [emailValid]").isValid());
    }

    @Test
    void testBuildValidatorEmpty() {
        BValidatorBuilder<Person> builder = new BValidatorBuilder<>(Person.class);
        assertEquals(0, builder.getRulesCount());
        assertEquals(0, builder.getMembersCount());
        assertTrue(builder.isEmpty());
        Throwable exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("At least one rule or one member must be provided to build a validator.", exception.getMessage());
    }

    @Test
    void testBuildValidatorWithSameRuleId() {
        Predicate<Person> rule = p -> true;
        BValidatorBuilder<Person> validatorBuilder = new BValidatorBuilder<>(Person.class)
                .addAssertion("rule1", rule, "Always true")
                .addAssertion("rule1", rule, "Always true");
        assertEquals(1, validatorBuilder.getRulesCount());
    }


    @Test
    void testBuildValidatorWithThrowRules() {
        BValidator<Person> validator = new BValidatorBuilder<>(Person.class)
                .addAssertion("rule1",
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
    void testBuildValidatorWithNullMember(String memberName, Function<Person, ?> memberFunction, BValidatorBuilder<?> validatorSupplier) {
        assertThrows(IllegalArgumentException.class, () -> new BValidatorBuilder<>(Person.class)
                .addMember(memberName, memberFunction, validatorSupplier));
    }


    @Test
    void testBuildValidatorWithNullRule() {
        assertThrows(IllegalArgumentException.class, () -> new BValidatorBuilder<>(Person.class)
                .addAssertion("rule", null, "message"));
    }

    @Test
    void testBuildValidatorWithNullMemberGetter() {
        new BValidatorBuilder<>(Person.class)
                .addMember("name", (Function<Person, ?>) p -> null, new BValidatorBuilder<>(Address.class)
                        .addAssertion("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson());
    }

    @Test
    void testBuildValidatorWithWrongMemberCollectionType() {
        assertThrows(InvocationException.class, () -> new BValidatorBuilder<>(Person.class)
                .addMember("Address", (Function<Person, ?>) p -> List.of(new Phone("11", "+22")), new BValidatorBuilder<>(Address.class)
                        .addAssertion("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson()));

    }

    @Test
    void testBuildValidatorWithEmptyMemberCollection() {
        new BValidatorBuilder<>(Person.class)
                .addMember("Address", (Function<Person, ?>) p -> List.of(), new BValidatorBuilder<>(Address.class)
                        .addAssertion("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson());
    }

    @Test
    void testCompareRules() {
        Predicate<Person> predicate1 = p -> true;
        Predicate<Person> predicate2 = p -> true;
        BAssertion<Person> rule1 = new BAssertion<>("rule1", predicate1, "Rule 1 Always true");
        BAssertion<Person> rule2 = new BAssertion<>("rule1", predicate1, "Rule 1 Always true");
        BAssertion<Person> rule3 = new BAssertion<>("rule2", predicate2, "Rule 2 Always true");
        assertEquals(rule1, rule2);
        assertNotEquals(rule1, rule3);
        assertEquals(rule1, rule1);
        assertNotEquals("rule1", rule1);
    }

//    @Test
//    void testCompareMembers() {
//        BMember<Person, Address> member1 = new BMember<>("member1",
//                Person::getAddress,
//                Set.of(new BValidatorBuilder<>(Address.class)
//                        .addRule("rule1", s -> true, "Always true")
//                        .build()));
//        BMember<Person, Email> member2 = new BMember<>("member1", Person::getEmails,
//                Set.of(new BValidatorBuilder<>(Email.class)
//                        .addRule("rule1", s -> true, "Always true")
//                        .build()));
//        BMember<Person, Phone> member3 = new BMember<>("member2", Person::getPhones,
//                Set.of(new BValidatorBuilder<>(Phone.class)
//                        .addRule("rule1", s -> true, "Always true")
//                        .build()));
//        assertEquals(member1, member2);
//        assertNotEquals(member1, member3);
//        assertEquals(member1, member1);
//        assertNotEquals("member1", member1);
//
//    }

    @Test
    void testRecursiveObject() {
        BValidatorBuilder<FirstRecursiveObject> builder = new BValidatorBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        builder.addAssertion("rule1", FirstRecursiveObject::isAttr1Valid, "Always true");
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
        BReport result = validator.validate(firstRecursiveObject);
        System.out.println(result);
        assertTrue(result.isValid());
    }

    @Test
    void testRecursiveObjectWithNull() {
        BValidatorBuilder<FirstRecursiveObject> builder = new BValidatorBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        builder.addAssertion("rule1", FirstRecursiveObject::isAttr1Valid, "Always true");
        builder.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builder);
        BValidator<FirstRecursiveObject> validator = builder.build();
        FirstRecursiveObject firstRecursiveObject = new FirstRecursiveObject()
                .setAttr1("attr1")
                .setFirstRecursiveObject(new FirstRecursiveObject()
                        .setAttr1("attr2")
                        .setFirstRecursiveObject(new FirstRecursiveObject()
                                .setAttr1(null)));

        BReport result = validator.validate(firstRecursiveObject);

        assertFalse(result.isValid());
        assertFalse(result.getMemberReports().get(0).getMemberReports().get(0).getRuleResults().get(0).isValid());

    }

    @Test
    void testRecursiveObjectWithSubElement() {
        BValidatorBuilder<FirstRecursiveObject> builder = new BValidatorBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        builder.addAssertion("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builder.addAssertion("emailValid", FirstRecursiveObject::isEmailValid, "Email must be valid");
        builder.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builder);
        builder.addMember("email", FirstRecursiveObject::getEmail, new BValidatorBuilder<>(Email.class)
                        .addAssertion("emailValid", Email::isEmailValid, "Email must be valid")
                        .addAssertion("domainValid", Email::isDomainValid, "Domain must be set and not empty"))
                .build();
        BValidator<FirstRecursiveObject> validator = builder.build();
        FirstRecursiveObject firstRecursiveObject = new FirstRecursiveObject()
                .setAttr1("attr1")
                .setFirstRecursiveObject(new FirstRecursiveObject()
                        .setAttr1("attr2")
                        .setEmail(new Email("aa@bb.com", "aa.com")))
                .setEmail(new Email("bb@vv", "aa.com"));
        BReport result = validator.validate(firstRecursiveObject);
        assertTrue(result.isValid());
    }

    @Test
    void testCrossRecursiveObject() {
        BValidatorBuilder<FirstRecursiveObject> builderFirst = new BValidatorBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorBuilder<SecondRecursiveObject> builderSecond = new BValidatorBuilder<>(SecondRecursiveObject.class)
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addAssertion("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addAssertion("emailValid", FirstRecursiveObject::isEmailValid, "Email must be valid");
        builderFirst.addAssertion("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addAssertion("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("email", FirstRecursiveObject::getEmail, new BValidatorBuilder<>(Email.class)
                .addAssertion("emailValid", Email::isEmailValid, "Email must be valid")
                .addAssertion("domainValid", Email::isDomainValid, "Domain must be set and not empty"));
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addAssertion("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addAssertion("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

        builderSecond.build();

        BReport result = builderFirst.build().validate(new FirstRecursiveObject()
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
        assertFalse(BReportTest.getRuleResult(result, "FirstRecursiveObject.secondRecursiveObject.firstRecursiveObject [secondRecursiveObjectValid]").isValid());
        assertFalse(BReportTest.getRuleResult(result, "FirstRecursiveObject.secondRecursiveObject.firstRecursiveObject [rule1]").isValid());
        assertFalse(BReportTest.getRuleResult(result, "FirstRecursiveObject.secondRecursiveObject.firstRecursiveObject [emailValid]").isValid());
        assertFalse(BReportTest.getRuleResult(result, "FirstRecursiveObject.firstRecursiveObject.secondRecursiveObject.firstRecursiveObject [secondRecursiveObjectValid]").isValid());
        assertFalse(BReportTest.getRuleResult(result, "FirstRecursiveObject.firstRecursiveObject.secondRecursiveObject.firstRecursiveObject [emailValid]").isValid());

    }

    @Test
    public void testCrossCollectionRecursiveObject(){
        BValidatorBuilder<FirstRecursiveObject> builderFirst = new BValidatorBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorBuilder<SecondRecursiveObject> builderSecond = new BValidatorBuilder<>(SecondRecursiveObject.class)
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addAssertion("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addAssertion("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addAssertion("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addAssertion("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addAssertion("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

        BValidatorBuilder<CollectionRecursiveObject> builderCollection = new BValidatorBuilder<>(CollectionRecursiveObject.class)
                .setBusinessObjectName("CollectionRecursiveObject")
                .addAssertion("rule3", CollectionRecursiveObject::isCollectionAttrValid, "collection attr is not null")
                .addAssertion("firstRecursiveObjectValid", CollectionRecursiveObject::isFirstRecursiveObjectsValid, "firstRecursiveObject in collectionRecursiveObject must be valid")
                .addAssertion("secondRecursiveObjectValid", CollectionRecursiveObject::isSecondRecursiveObjectsValid, "secondRecursiveObject in collectionRecursiveObject must be valid")
                .addMember("firstRecursiveObjects", CollectionRecursiveObject::getFirstRecursiveObjects, builderFirst)
                .addMember("secondRecursiveObjects", CollectionRecursiveObject::getSecondRecursiveObjects, builderSecond);


        BReport result = builderCollection.build().validate(new CollectionRecursiveObject()
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
        assertFalse(BReportTest.getRuleResult(result, "CollectionRecursiveObject.secondRecursiveObjects[0].firstRecursiveObject.secondRecursiveObject.firstRecursiveObject [secondRecursiveObjectValid]").isValid());

    }

    @Test
    public void testRecursiveLoopObject() {
        BValidatorBuilder<FirstRecursiveObject> builderFirst = new BValidatorBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject")
                .addAssertion("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null")
                .addAssertion("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);

        FirstRecursiveObject firstLoopObject = new FirstRecursiveObject().setAttr1("attr1");
        firstLoopObject.setFirstRecursiveObject(firstLoopObject);

        BReport result = builderFirst.build().validate(firstLoopObject);


        assertTrue(result.isValid());
        assertEquals(4, result.getNbOfTests());
        assertEquals(4, countAllRulesResults(result, true));

    }

    @Test
    public void testRecursiveCrossLoopObject() {
        BValidatorBuilder<FirstRecursiveObject> builderFirst = new BValidatorBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorBuilder<SecondRecursiveObject> builderSecond = new BValidatorBuilder<>(SecondRecursiveObject.class)
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addAssertion("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addAssertion("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addAssertion("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addAssertion("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addAssertion("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

        FirstRecursiveObject firstLoopObject = new FirstRecursiveObject().setAttr1("attr1");
        firstLoopObject.setFirstRecursiveObject(firstLoopObject);
        SecondRecursiveObject secondLoopObject = new SecondRecursiveObject().setAttr2("attr2");
        firstLoopObject.setSecondRecursiveObject(secondLoopObject);
        secondLoopObject.setFirstRecursiveObject(firstLoopObject);

        BReport result = builderFirst.build().validate(firstLoopObject);
        assertTrue(result.isValid());
        assertEquals(8, result.getNbOfTests());
    }

    @Test
    public void testPolymorphismCorrect(){

        BValidatorBuilder<Graphic> graphicBValidatorBuilder = createGraphicValidatorBuilder();

        BReport result = graphicBValidatorBuilder.build().validate(createGraphic());
        System.out.println(result);
        assertTrue(result.isValid());
        assertEquals(19, result.getNbOfTests());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.shapesList[0] [sqNameValid]").isValid());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.shapesList[1] [crRadiusValid]").isValid());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.shapesList[2] [recHeightValid]").isValid());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.shapesArray[0] [sqNameValid]").isValid());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.shapesArray[1] [crRadiusValid]").isValid());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.shapesArray[2] [recHeightValid]").isValid());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.squareOrRectangle [sqNameValid]").isValid());
        assertTrue(BReportTest.getRuleResult(result, "Graphic.circle [crRadiusValid]").isValid());
    }

    @Test
    public void testPolymorphismUnsatisfiedImplementation(){
        BValidatorBuilder<Square> squareBValidatorBuilder = new BValidatorBuilder<>(Square.class)
                .setBusinessObjectName("Square")
                .addAssertion("sqNameValid", Square::isNameValid, "name is not null")
                .addAssertion("sqSideValid", Square::isSideValid, "side is not null");
        BValidatorBuilder<Graphic> graphicBValidatorBuilder = new BValidatorBuilder<>(Graphic.class)
                .setBusinessObjectName("Graphic")
                .addAssertion("rule1", Graphic::isNameValid, "name is not null")
                .addMember("shapesList", Graphic::getShapeList, squareBValidatorBuilder);
        Throwable throwable = assertThrows(InvocationException.class, () ->
                graphicBValidatorBuilder
                        .build()
                        .validate(new Graphic()
                        .setName("graphic")
                        .addShapeToList(new Circle().setName("circleInList").setRadius(1)))
        );
        assertInstanceOf(IllegalBusinessObjectException.class, throwable.getCause());
        assertEquals("No validator found for type io.github.ceoche.bvalid.mock.Circle", throwable.getCause().getMessage());

    }

    @Test
    public void testPolymorphismWithRecursiveMember(){
        BValidatorBuilder<Graphic> graphicBValidatorBuilder = createGraphicValidatorBuilder();
        graphicBValidatorBuilder.addMember("innerGraphic", Graphic::getInnerGraphic, graphicBValidatorBuilder);
        BReport result = graphicBValidatorBuilder.build().validate(createGraphic()
                .setInnerGraphic(new Graphic()
                        .setName("innerGraphic")
                        .addShapeToList(new Square().setName("innerSquareInList").setSide(1))
                        .addShapeToList(new Circle().setName("innerCircleInList").setRadius(1))
                        .addShapeToList(new Rectangle().setName("innerRectangleInList").setHeight(1).setSide(1))
                        .setShapeArray(new Shape[]{new Square().setName("innerSquareInArray").setSide(1),
                                new Circle().setName("innerCircleInArray").setRadius(1),
                                new Rectangle().setName("innerRectangleInArray").setHeight(1).setSide(1)})
                        .setSquareOrRectangle(new Square().setName("innerSquareOrRectangle").setSide(1))
                        .setCircle(new Circle().setName("innerCircle").setRadius(1))
                )
        );
        assertTrue(result.isValid());
        assertEquals(38, result.getNbOfTests());
    }

    @Test
    public void testPolymorphismWithLoopRecursiveMember(){
        BValidatorBuilder<Graphic> graphicBValidatorBuilder = createGraphicValidatorBuilder();
        graphicBValidatorBuilder.addMember("innerGraphic", Graphic::getInnerGraphic, graphicBValidatorBuilder);
        Graphic graphic = createGraphic();
        graphic.setInnerGraphic(graphic);
        BReport result = graphicBValidatorBuilder.build().validate(graphic);
        assertTrue(result.isValid());
    }

    @Test
    public void testEmptySubBuildersBuilders(){
        BValidatorBuilder<Graphic> shapeBValidatorBuilder = new BValidatorBuilder<>(Graphic.class)
                .addMember("squareOrRectangle", Graphic::getSquareOrRectangle,
                        new BValidatorBuilder<>(Square.class),
                        new BValidatorBuilder<>(Rectangle.class));
        Throwable throwable = assertThrows(IllegalStateException.class, shapeBValidatorBuilder::build);
        assertEquals("At least one rule or one member must be provided to build a validator.", throwable.getMessage());
    }

    @Test
    public void testPolymorphismCollection(){
        BValidatorBuilder<Square> shapeBValidatorBuilder = new BValidatorBuilder<>(Square.class)
                .addAssertion("sqNameValid", Square::isNameValid, "name is not null")
                .addAssertion("sqSideValid", Square::isSideValid, "side is not null");
        List<BReport> result = shapeBValidatorBuilder.build()
                .validate(List.of(new Square().setName("squareInList2").setSide(2),
                        new Square().setName("squareInList").setSide(2),
                        new Rectangle().setName("rectangleInList").setHeight(1).setSide(1)));
        assertTrue(result.get(0).isValid());
    }

    @Test
    public void testPolymorphismArray(){
        BValidatorBuilder<Square> shapeBValidatorBuilder = new BValidatorBuilder<>(Square.class)
                .addAssertion("sqNameValid", Square::isNameValid, "name is not null")
                .addAssertion("sqSideValid", Square::isSideValid, "side is not null");
        List<BReport> result = shapeBValidatorBuilder.build()
                .validate(new Square[]{new Square().setName("squareInArray2").setSide(2),
                        new Square().setName("squareInArray").setSide(2),
                        new Rectangle().setName("rectangleInArray").setHeight(1).setSide(1)});
        assertTrue(result.get(0).isValid());
    }

    @Test
    public void testValidateEmptyArray(){
        BValidatorBuilder<Graphic> graphicBValidatorBuilder = createGraphicValidatorBuilder();
        BReport result = graphicBValidatorBuilder.build().validate(new Graphic().setName("shape").setShapeArray(new Square[0]));
        assertTrue(result.isValid());
        assertEquals(1, result.getNbOfTests());
    }

    @Test
    public void testValidateGraphic(){
        BValidatorBuilder<Graphic> graphicBValidatorBuilder = createGraphicValidatorBuilder();
        Graphic graphic = createGraphic().addShapeToList(new Losange().setName("losange").setDiagonal(10));
        BReport result = graphicBValidatorBuilder.build().validate(graphic);
        System.out.println(result);
        assertFalse(result.isValid());
        assertEquals(22, result.getNbOfTests());
    }

    @Test
    public void testAddMemberWithoutId(){
        BValidator<Phone> bValidator = new BValidatorBuilder<>(Phone.class)
                .setBusinessObjectName("phone")
                .addAssertion(Phone::isCountryCodeValid, "country code is not valid")
                .addAssertion(Phone::isNumberValid, "number is not valid")
                .build();
        BReport result = bValidator.validate(new Phone("01234567", "+33"));
        assertTrue(result.isValid());
    }



    private void assertMemberResults(BReport result, boolean expected) {
        for (BReport memberResult : result.getMemberReports()) {
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

    private Graphic createGraphic(){
        return new Graphic()
                .setName("graphic")
                .addShapeToList(new Square().setName("squareInList").setSide(1))
                .addShapeToList(new Circle().setName("circleInList").setRadius(1))
                .addShapeToList(new Rectangle().setName("rectangleInList").setHeight(1).setSide(1))
                .setShapeArray(new Shape[]{new Square().setName("squareInArray").setSide(1),
                        new Circle().setName("circleInArray").setRadius(1),
                        new Rectangle().setName("rectangleInArray").setHeight(1).setSide(1)})
                .setSquareOrRectangle(new Square().setName("squareOrRectangle").setSide(1))
                .setCircle(new Circle().setName("circle").setRadius(1));
    }


    private BValidatorBuilder<Person> createCompleteBuilder() {
        return new BValidatorBuilder<>(Person.class)
                .setBusinessObjectName("Person")
                .addAssertion("ageValid", Person::isAgeValid, "Name must not be null")
                .addAssertion("NameNotEmpty", Person::isNameValid, "Name must not be empty")
                .addAssertion("ValidEmail", Person::isEmailValid, "Email must be valid")
                .addMember("address", Person::getAddress, new BValidatorBuilder<>(Address.class)
                        .setBusinessObjectName("Address")
                        .addAssertion("cityValid", Address::isCityValid, "City must not be null")
                        .addAssertion("StreetValid", Address::isStreetValid, "Street must not be empty")
                        .addMember("city", Address::getCity, new BValidatorBuilder<>(City.class)
                                .setBusinessObjectName("City")
                                .addAssertion("cityNameValid", City::isNamesValid, "City name must not be empty")
                                .addAssertion("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                        )
                )
                .addMember("phones", Person::getPhones, new BValidatorBuilder<>(Phone.class)
                        .setBusinessObjectName("Phone")
                        .addAssertion("numberValid", Phone::isNumberValid, "Number must not be null")
                        .addAssertion("countryCodeValid", Phone::isCountryCodeValid, "Country code must not be valid")
                )
                .addMember("emails", Person::getEmails, new BValidatorBuilder<>(Email.class)
                        .setBusinessObjectName("Email")
                        .addAssertion("emailValid", Email::isEmailValid, "Email must be valid")
                        .addAssertion("domainValid", Email::isDomainValid, "Domain must be set and not empty")
                );
    }

    private BValidatorBuilder<Graphic> createGraphicValidatorBuilder(){
        BValidatorBuilder<Square> squareBValidatorBuilder = new BValidatorBuilder<>(Square.class)
                .setBusinessObjectName("Square")
                .addAssertion("sqNameValid", Square::isNameValid, "name is not null")
                .addAssertion("sqSideValid", Square::isSideValid, "side is not null");
        BValidatorBuilder<Rectangle> rectangleBValidatorBuilder = new BValidatorBuilder<>(
              Rectangle.class, squareBValidatorBuilder)
                .setBusinessObjectName("Rectangle")
                .addAssertion("recHeightValid", Rectangle::isHeightValid, "height is not null");
        BValidatorBuilder<Circle> circleBValidatorBuilder = new BValidatorBuilder<>(Circle.class)
                .addAssertion("crNameValid", Circle::isNameValid, "name is not null")
                .addAssertion("crRadiusValid", Circle::isRadiusValid, "radius is not null");

        return new BValidatorBuilder<>(Graphic.class)
                .setBusinessObjectName("Graphic")
                .addAssertion("rule1", Graphic::isNameValid, "name is not null")
                .addMember("shapesList", Graphic::getShapeList, squareBValidatorBuilder, rectangleBValidatorBuilder,
                      circleBValidatorBuilder)
                .addMember("shapesArray", Graphic::getShapeArray, squareBValidatorBuilder, rectangleBValidatorBuilder,
                      circleBValidatorBuilder)
                .addMember("squareOrRectangle", Graphic::getSquareOrRectangle, squareBValidatorBuilder,
                      rectangleBValidatorBuilder)
                .addMember("circle", Graphic::getCircle, circleBValidatorBuilder);
    }

    //count number of (Ture of False) rules in the result recursively
    private int countAllRulesResults(BReport result, boolean expected) {
        int count = countRulesResults(result, expected);
        for (BReport memberResult : result.getMemberReports()) {
            count += countAllRulesResults(memberResult, expected);
        }
        return count;
    }

    public int countRulesResults(BReport result, boolean expected) {
        int count = 0;
        for (AssertionReport assertionReport : result.getRuleResults()) {
            if (assertionReport.isValid() == expected) {
                count++;
            }
        }
        return count;
    }

}
