package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
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
        BValidatorManualBuilder<Person> builder = new BValidatorManualBuilder<>(Person.class);
        assertEquals(0, builder.getRulesCount());
        assertEquals(0, builder.getMembersCount());
        assertTrue(builder.isEmpty());
        Throwable exception = assertThrows(IllegalBusinessObjectException.class, builder::build);
        assertEquals("Rules or members must be provided for a business object: ", exception.getMessage());
    }

    @Test
    void testBuildValidatorWithSameRuleId() {
        BValidatorManualBuilder<Person> validatorBuilder = new BValidatorManualBuilder<>(Person.class)
                .addRule("rule1", p -> true, "Always true")
                .addRule("rule1", p -> true, "Always true");
        assertEquals(1, validatorBuilder.getRulesCount());
    }


    @Test
    void testBuildValidatorWithThrowRules() {
        BValidator<Person> validator = new BValidatorManualBuilder<>(Person.class)
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
        assertThrows(IllegalArgumentException.class, () -> new BValidatorManualBuilder<>(Person.class)
                .addMember(memberName, memberFunction, validatorSupplier));
    }


    @ParameterizedTest
    @MethodSource("provideInvalidRules")
    void testBuildValidatorWithNullRule(String ruleId, Predicate<Person> rule, String message) {
        assertThrows(IllegalArgumentException.class, () -> new BValidatorManualBuilder<>(Person.class)
                .addRule(ruleId, rule, message));
    }

    @Test
    void testBuildValidatorWithNullMemberGetter() {
        new BValidatorManualBuilder<>(Person.class)
                .addMember("name", (Function<Person, ?>) p -> null, new BValidatorManualBuilder<>(Address.class)
                        .addRule("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson());
    }

    @Test
    void testBuildValidatorWithWrongMemberCollectionType() {
        assertThrows(InvocationException.class, () -> new BValidatorManualBuilder<>(Person.class)
                .addMember("Address", (Function<Person, ?>) p -> List.of(new Phone("11", "+22")), new BValidatorManualBuilder<>(Address.class)
                        .addRule("rule1", s -> true, "Always true"))
                .build()
                .validate(createAllCorrectPerson()));

    }

    @Test
    void testBuildValidatorWithEmptyMemberCollection() {
        new BValidatorManualBuilder<>(Person.class)
                .addMember("Address", (Function<Person, ?>) p -> List.of(), new BValidatorManualBuilder<>(Address.class)
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
                Map.of(Address.class,new BValidatorManualBuilder<>(Address.class)
                        .addRule("rule1", s -> true, "Always true")
                        .build()));
        BusinessMemberObject<Person, Email> member2 = new BusinessMemberObject<>("member1", Person::getEmails,
                Map.of(Email.class,new BValidatorManualBuilder<>(Email.class)
                        .addRule("rule1", s -> true, "Always true")
                        .build()));
        BusinessMemberObject<Person, Phone> member3 = new BusinessMemberObject<>("member2", Person::getPhones,
                Map.of(Phone.class,new BValidatorManualBuilder<>(Phone.class)
                        .addRule("rule1", s -> true, "Always true")
                        .build()));
        assertEquals(member1, member2);
        assertNotEquals(member1, member3);
        assertEquals(member1, member1);
        assertNotEquals("member1", member1);

    }

    @Test
    void testRecursiveObject() {
        BValidatorManualBuilder<FirstRecursiveObject> builder = new BValidatorManualBuilder<>(FirstRecursiveObject.class)
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
        BValidatorManualBuilder<FirstRecursiveObject> builder = new BValidatorManualBuilder<>(FirstRecursiveObject.class)
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

        assertFalse(result.isValid());
        assertFalse(result.getMemberResults().get(0).getMemberResults().get(0).getRuleResults().get(0).isValid());

    }

    @Test
    void testRecursiveObjectWithSubElement() {
        BValidatorManualBuilder<FirstRecursiveObject> builder = new BValidatorManualBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        builder.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builder.addRule("emailValid", FirstRecursiveObject::isEmailValid, "Email must be valid");
        builder.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builder);
        builder.addMember("email", FirstRecursiveObject::getEmail, new BValidatorManualBuilder<>(Email.class)
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
        assertTrue(result.isValid());
    }

    @Test
    void testCrossRecursiveObject() {
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorManualBuilder<SecondRecursiveObject> builderSecond = new BValidatorManualBuilder<>(SecondRecursiveObject.class)
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addRule("emailValid", FirstRecursiveObject::isEmailValid, "Email must be valid");
        builderFirst.addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addRule("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("email", FirstRecursiveObject::getEmail, new BValidatorManualBuilder<>(Email.class)
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
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorManualBuilder<SecondRecursiveObject> builderSecond = new BValidatorManualBuilder<>(SecondRecursiveObject.class)
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addRule("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addRule("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addRule("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

        BValidatorManualBuilder<CollectionRecursiveObject> builderCollection = new BValidatorManualBuilder<>(CollectionRecursiveObject.class)
                .setBusinessObjectName("CollectionRecursiveObject")
                .addRule("rule3", CollectionRecursiveObject::isCollectionAttrValid, "collection attr is not null")
                .addRule("firstRecursiveObjectValid", CollectionRecursiveObject::isFirstRecursiveObjectsValid, "firstRecursiveObject in collectionRecursiveObject must be valid")
                .addRule("secondRecursiveObjectValid", CollectionRecursiveObject::isSecondRecursiveObjectsValid, "secondRecursiveObject in collectionRecursiveObject must be valid")
                .addMember("firstRecursiveObjects", CollectionRecursiveObject::getFirstRecursiveObjects, builderFirst)
                .addMember("secondRecursiveObjects", CollectionRecursiveObject::getSecondRecursiveObjects, builderSecond);


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
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject")
                .addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null")
                .addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);

        FirstRecursiveObject firstLoopObject = new FirstRecursiveObject().setAttr1("attr1");
        firstLoopObject.setFirstRecursiveObject(firstLoopObject);

        ObjectResult result = builderFirst.build().validate(firstLoopObject);


        assertTrue(result.isValid());
        assertEquals(4, result.getNbOfTests());
        assertEquals(4, countAllRulesResults(result, true));

    }

    @Test
    public void testRecursiveCrossLoopObject() {
        BValidatorManualBuilder<FirstRecursiveObject> builderFirst = new BValidatorManualBuilder<>(FirstRecursiveObject.class)
                .setBusinessObjectName("FirstRecursiveObject");
        BValidatorManualBuilder<SecondRecursiveObject> builderSecond = new BValidatorManualBuilder<>(SecondRecursiveObject.class)
                .setBusinessObjectName("SecondRecursiveObject");

        builderFirst.addRule("rule1", FirstRecursiveObject::isAttr1Valid, "attr1 is not null");
        builderFirst.addRule("firstRecursiveObjectValid", FirstRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addRule("secondRecursiveObjectValid", FirstRecursiveObject::isSecondRecursiveObjectValid, "SecondRecursiveObject in firstRecursiveObject must be valid");
        builderFirst.addMember("firstRecursiveObject", FirstRecursiveObject::getFirstRecursiveObject, builderFirst);
        builderFirst.addMember("secondRecursiveObject", FirstRecursiveObject::getSecondRecursiveObject, builderSecond);

        builderSecond.addRule("rule2", SecondRecursiveObject::isAttr2Valid, "attr2 is not null");
        builderSecond.addRule("firstRecursiveObjectValid", SecondRecursiveObject::isFirstRecursiveObjectValid, "firstRecursiveObject in secondRecursiveObject must be valid");
        builderSecond.addMember("firstRecursiveObject", SecondRecursiveObject::getFirstRecursiveObject, builderFirst);

        FirstRecursiveObject firstLoopObject = new FirstRecursiveObject().setAttr1("attr1");
        firstLoopObject.setFirstRecursiveObject(firstLoopObject);
        SecondRecursiveObject secondLoopObject = new SecondRecursiveObject().setAttr2("attr2");
        firstLoopObject.setSecondRecursiveObject(secondLoopObject);
        secondLoopObject.setFirstRecursiveObject(firstLoopObject);

        ObjectResult result = builderFirst.build().validate(firstLoopObject);
        assertTrue(result.isValid());
        assertEquals(8, result.getNbOfTests());
    }

    @Test
    public void testPolymorphismCorrect(){

        BValidatorManualBuilder<Graphic> graphicBValidatorManualBuilder = createGraphicValidatorBuilder();

        ObjectResult result = graphicBValidatorManualBuilder.build().validate(createGraphic());
        assertTrue(result.isValid());
        assertEquals(19, result.getNbOfTests());
        assertTrue(result.getRuleResult("Graphic.shapesList[0] [sqNameValid]").isValid());
        assertTrue(result.getRuleResult("Graphic.shapesList[1] [crRadiusValid]").isValid());
        assertTrue(result.getRuleResult("Graphic.shapesList[2] [recHeightValid]").isValid());
        assertTrue(result.getRuleResult("Graphic.shapesArray[0] [sqNameValid]").isValid());
        assertTrue(result.getRuleResult("Graphic.shapesArray[1] [crRadiusValid]").isValid());
        assertTrue(result.getRuleResult("Graphic.shapesArray[2] [recHeightValid]").isValid());
        assertTrue(result.getRuleResult("Graphic.squareOrRectangle [sqNameValid]").isValid());
        assertTrue(result.getRuleResult("Graphic.circle [crRadiusValid]").isValid());
    }

    @Test
    public void testPolymorphismUnsatisfiedImplementation(){
        BValidatorManualBuilder<Square> squareBValidatorManualBuilder = new BValidatorManualBuilder<>(Square.class)
                .setBusinessObjectName("Square")
                .addRule("sqNameValid", Square::isNameValid, "name is not null")
                .addRule("sqSideValid", Square::isSideValid, "side is not null");
        BValidatorManualBuilder<Graphic> graphicBValidatorManualBuilder = new BValidatorManualBuilder<>(Graphic.class)
                .setBusinessObjectName("Graphic")
                .addRule("rule1", Graphic::isNameValid, "name is not null")
                .addMember("shapesList", Graphic::getShapeList,squareBValidatorManualBuilder);
        Throwable throwable = assertThrows(InvocationException.class, () ->
                graphicBValidatorManualBuilder
                        .build()
                        .validate(new Graphic()
                        .setName("graphic")
                        .addShapeToList(new Rectangle().setName("rectangleInList").setHeight(1).setSide(1)))
        );
        assertInstanceOf(IllegalBusinessObjectException.class, throwable.getCause());
        assertEquals("No validator found for type io.github.ceoche.bvalid.mock.Rectangle", throwable.getCause().getMessage());

    }

    @Test
    public void testPolymorphismWithRecursiveMember(){
        BValidatorManualBuilder<Graphic> graphicBValidatorManualBuilder = createGraphicValidatorBuilder();
        graphicBValidatorManualBuilder.addMember("innerGraphic", Graphic::getInnerGraphic, graphicBValidatorManualBuilder);
        ObjectResult result = graphicBValidatorManualBuilder.build().validate(createGraphic()
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
        BValidatorManualBuilder<Graphic> graphicBValidatorManualBuilder = createGraphicValidatorBuilder();
        graphicBValidatorManualBuilder.addMember("innerGraphic", Graphic::getInnerGraphic, graphicBValidatorManualBuilder);
        Graphic graphic = createGraphic();
        graphic.setInnerGraphic(graphic);
        ObjectResult result = graphicBValidatorManualBuilder.build().validate(graphic);
        assertTrue(result.isValid());
    }

    @Test
    public void testEmptySubBuildersBuilders(){
        BValidatorManualBuilder<Graphic> shapeBValidatorManualBuilder = new BValidatorManualBuilder<>(Graphic.class)
                .addMember("squareOrRectangle", Graphic::getSquareOrRectangle,
                        new BValidatorManualBuilder<>(Square.class),
                        new BValidatorManualBuilder<>(Rectangle.class));
        Throwable throwable = assertThrows(IllegalStateException.class, shapeBValidatorManualBuilder::build);
        assertEquals("All sub validators are empty", throwable.getMessage());
    }

    @Test
    public void testNullType(){
        BValidatorManualBuilder<Graphic> shapeBValidatorManualBuilder = new BValidatorManualBuilder<>(Graphic.class)
                .addMember("squareOrRectangle", Graphic::getSquareOrRectangle,
                        new BValidatorManualBuilder<Square>(null)
                                .addRule("sqNameValid", Square::isNameValid, "name is not null")
                                .addRule("sqSideValid", Square::isSideValid, "side is not null")
                );
        Throwable throwable = assertThrows(IllegalStateException.class, shapeBValidatorManualBuilder::build);
        assertEquals("Type is not set", throwable.getMessage());
    }

    @Test
    public void testPolymorphismCollection(){
        BValidatorManualBuilder<Square> shapeBValidatorManualBuilder = new BValidatorManualBuilder<>(Square.class)
                .addRule("sqNameValid", Square::isNameValid, "name is not null")
                .addRule("sqSideValid", Square::isSideValid, "side is not null");
        List<ObjectResult> result = shapeBValidatorManualBuilder.build()
                .validate(List.of(new Square().setName("squareInList2").setSide(2),
                        new Square().setName("squareInList").setSide(2),
                        new Rectangle().setName("rectangleInList").setHeight(1).setSide(1)));
        assertTrue(result.get(0).isValid());
    }

    @Test
    public void testPolymorphismArray(){
        BValidatorManualBuilder<Square> shapeBValidatorManualBuilder = new BValidatorManualBuilder<>(Square.class)
                .addRule("sqNameValid", Square::isNameValid, "name is not null")
                .addRule("sqSideValid", Square::isSideValid, "side is not null");
        List<ObjectResult> result = shapeBValidatorManualBuilder.build()
                .validate(new Square[]{new Square().setName("squareInArray2").setSide(2),
                        new Square().setName("squareInArray").setSide(2),
                        new Rectangle().setName("rectangleInArray").setHeight(1).setSide(1)});
        assertTrue(result.get(0).isValid());
    }

    @Test
    public void testValidateEmptyArray(){
        BValidatorManualBuilder<Graphic> graphicBValidatorManualBuilder = createGraphicValidatorBuilder();
        ObjectResult result = graphicBValidatorManualBuilder.build().validate(new Graphic().setName("shape").setShapeArray(new Square[0]));
        assertTrue(result.isValid());
        assertEquals(1, result.getNbOfTests());
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


    private BValidatorManualBuilder<Person> createCompleteBuilder() {
        return new BValidatorManualBuilder<>(Person.class)
                .setBusinessObjectName("Person")
                .addRule("ageValid", Person::isAgeValid, "Name must not be null")
                .addRule("NameNotEmpty", Person::isNameValid, "Name must not be empty")
                .addRule("ValidEmail", Person::isEmailValid, "Email must be valid")
                .addMember("address", Person::getAddress, new BValidatorManualBuilder<>(Address.class)
                        .setBusinessObjectName("Address")
                        .addRule("cityValid", Address::isCityValid, "City must not be null")
                        .addRule("StreetValid", Address::isStreetValid, "Street must not be empty")
                        .addMember("city", Address::getCity, new BValidatorManualBuilder<>(City.class)
                                .setBusinessObjectName("City")
                                .addRule("cityNameValid", City::isNamesValid, "City name must not be empty")
                                .addRule("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
                        )
                )
                .addMember("phones", Person::getPhones, new BValidatorManualBuilder<>(Phone.class)
                        .setBusinessObjectName("Phone")
                        .addRule("numberValid", Phone::isNumberValid, "Number must not be null")
                        .addRule("countryCodeValid", Phone::isCountryCodeValid, "Country code must not be valid")
                )
                .addMember("emails", Person::getEmails, new BValidatorManualBuilder<>(Email.class)
                        .setBusinessObjectName("Email")
                        .addRule("emailValid", Email::isEmailValid, "Email must be valid")
                        .addRule("domainValid", Email::isDomainValid, "Domain must be valid")
                );
    }

    private BValidatorManualBuilder<Graphic> createGraphicValidatorBuilder(){
        BValidatorManualBuilder<Square> squareBValidatorManualBuilder = new BValidatorManualBuilder<>(Square.class)
                .setBusinessObjectName("Square")
                .addRule("sqNameValid", Square::isNameValid, "name is not null")
                .addRule("sqSideValid", Square::isSideValid, "side is not null");
        BValidatorManualBuilder<Rectangle> rectangleBValidatorManualBuilder = new BValidatorManualBuilder<>(Rectangle.class)
                .setBusinessObjectName("Rectangle")
                .addRule("recNameValid", Rectangle::isNameValid, "name is not null")
                .addRule("recHeightValid", Rectangle::isHeightValid, "height is not null")
                .addRule("recSideValid", Rectangle::isSideValid, "side is not null");
        BValidatorManualBuilder<Circle> circleBValidatorManualBuilder = new BValidatorManualBuilder<>(Circle.class)
                .addRule("crNameValid", Circle::isNameValid, "name is not null")
                .addRule("crRadiusValid", Circle::isRadiusValid, "radius is not null");

        return new BValidatorManualBuilder<>(Graphic.class)
                .setBusinessObjectName("Graphic")
                .addRule("rule1", Graphic::isNameValid, "name is not null")
                .addMember("shapesList", Graphic::getShapeList,squareBValidatorManualBuilder, rectangleBValidatorManualBuilder, circleBValidatorManualBuilder)
                .addMember("shapesArray", Graphic::getShapeArray,squareBValidatorManualBuilder, rectangleBValidatorManualBuilder, circleBValidatorManualBuilder)
                .addMember("squareOrRectangle", Graphic::getSquareOrRectangle,squareBValidatorManualBuilder, rectangleBValidatorManualBuilder)
                .addMember("circle", Graphic::getCircle,circleBValidatorManualBuilder);
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
