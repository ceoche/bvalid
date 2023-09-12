/*
 * Copyright 2022 Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * ou may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ceoche.bvalid;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.ceoche.bvalid.BusinessObjectMocks.*;
import static org.junit.jupiter.api.Assertions.*;

public class BValidatorAnnotationTest {

   public static final int MEMBER_NAME = 0;
   public static final int RULE_ID = 1;
   public static final int DESCRIPTION = 2;
   public static final int RESULT = 3;

   @Test
   public void testValid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
      ObjectResult objectResult = this.buildObjectValidator(DefaultValidableMock.class).validate(object);

      assertTrue(objectResult.isValid(), "the business object must be valid");

      assertResultsContains(
            new Object[][]{
                  {"validable-mock", "rule01", "mandatoryAttribute must be defined.", true},
                  {"validable-mock", "", "optionalAttribute must be defined if present.", true},
                  {"validable-mock", "", "oneOrMoreAssociation must have at least one element.",
                        true}
            },
            objectResult);
   }

   @Test
   public void testParentIsBusinessObjectValid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateInheritanceWithoutAnnotationValid();
      ObjectResult objectResult = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertTrue(objectResult.isValid(), "the business object must be valid");
   }

   @Test
   public void testInvalid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateInvalid();
      ObjectResult objectResult = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertFalse(objectResult.isValid(), "The object must be invalid");
   }

   @Test
   public void testRuleAttributesResult() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
      ObjectResult objectResult = buildObjectValidator(DefaultValidableMock.class).validate(object);
      for (RuleResult RuleResult : objectResult.getRuleResults()) {
         if (RuleResult.getDescription().contains("mandatoryAttribute")) {
            assertEquals("rule01", RuleResult.getId());
         } else {
            assertTrue(RuleResult.getId().isEmpty());
         }
      }
   }

   @Test
   public void testParentInvalid() {
      WithInheritance object = BusinessObjectMocks.instantiateInheritanceWithInvalidParent();
      ObjectResult objectResult = buildObjectValidator(WithInheritance.class).validate(object);
      assertFalse(objectResult.isValid(), "The object must be invalid");

      assertResultsContains(
            new Object[][]{
                  {"With-inheritance", "", "Sub type must be defined.", true},
                  {"With-inheritance", "rule01", "mandatoryAttribute must be defined.", false},
                  {"With-inheritance", "", "optionalAttribute must be defined if present.", true},
                  {"With-inheritance", "", "oneOrMoreAssociation must have at least one element.",
                        false}
            },
            objectResult);
   }

   @Test
   public void testBusinessObjectMemberInvalid() {
      OnlyBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberInvalid();
      ObjectResult objectResult = buildObjectValidator(OnlyBusinessMembers.class).validate(object);
      assertFalse(
            objectResult.isValid(),
            "When an attribute of a BusinessObject is an invalid BusinessObject, the validation " +
                  "must detect it and " +
                  "invalidate the businessObject");

      assertResultsContains(
            new Object[][]{
                  {"my-only-member", "rule01", "mandatoryAttribute must be defined.", false},
                  {"my-only-member", "", "optionalAttribute must be defined if present.", false},
                  {"my-only-member", "", "oneOrMoreAssociation must have at least one element.",
                        false}
            },
            objectResult);
   }

   @Test
   @Disabled
   public void testBusinessObjectMemberInheritance() {
      fail("to implement");
   }

   @Test
   public void testCollectionOfBO() {
      List<DefaultValidableMock> objects = new ArrayList<>();
      objects.add(BusinessObjectMocks.instantiateValid());
      objects.add(BusinessObjectMocks.instantiateValid());
      objects.add(BusinessObjectMocks.instantiateValid());

      List<ObjectResult> results = buildObjectValidator(DefaultValidableMock.class).validate(objects);

      assertFalse(results.isEmpty());
      for (int index = 0; index < results.size(); index++) {
         assertTrue(results.get(index).isValid());
         assertEquals("validable-mock[" + index + "]", results.get(index).getBusinessObjectName(),
               "Results' name of BusinessObjects in a collection should be incremented like an " +
                     "array.");
      }
   }

   @Test
   public void testBusinessObjectCollectionMember() {
      CollectionBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberCollection();
      ObjectResult objectResult = buildObjectValidator(CollectionBusinessMembers.class).validate(object);
      assertFalse(objectResult.isValid());
   }

   @Test
   public void testArrayOfBO() {
      DefaultValidableMock[] objects = new DefaultValidableMock[]{
            BusinessObjectMocks.instantiateValid(),
            BusinessObjectMocks.instantiateValid()
      };
      List<ObjectResult> results = buildObjectValidator(DefaultValidableMock.class).validate(objects);
      assertFalse(results.isEmpty());
      for (ObjectResult result : results) {
         assertTrue(result.isValid());
      }
   }

   @Test
   public void testBusinessObjectArrayMember() {
      ArrayBusinessMember object = BusinessObjectMocks.instantiateBusinessMemberArray();
      ObjectResult objectResult = buildObjectValidator(ArrayBusinessMember.class).validate(object);
      assertFalse(objectResult.isValid());
   }

   @Test
   @Disabled
   public void testBusinessObjectMapMember() {
      fail("to implement");
   }

   @Test
   @Disabled
   public void testBusinessObjectGenericMember() {
      fail("to implement");
   }

   @Test
   public void testBusinessObjectNullMember() {
      OnlyBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberNull();
      ObjectResult objectResult = buildObjectValidator(OnlyBusinessMembers.class).validate(object);
      assertTrue(objectResult.isValid());
   }


   @Test
   public void testNotABusinessObjectError() {
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(String.class).validate("Not a validable"));
   }

   @Test
   public void testNoBusinessRuleNorMemberError() {
      IllegalBusinessObject object = BusinessObjectMocks.instantiateWithoutAssertions();
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(IllegalBusinessObject.class).validate(object));
   }

   @Test
   public void testIllegalBusinessRuleError() {
      Object object = BusinessObjectMocks.instantiateIllegalBusinessRule();
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(Object.class).validate(object));
   }

   @Test
   public void testIllegalBusinessMemberError() {
      IllegalBusinessMemberObject object = BusinessObjectMocks.instantiateIllegalBusinessMember();
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(IllegalBusinessMemberObject.class).validate(object));
   }

   @Test
   public void testExceptionWhileValidatingRule() {
      ExceptionBusinessRuleObject object = BusinessObjectMocks.instantiateExceptionBusinessRule();
      try {
          BValidator<ExceptionBusinessRuleObject> validator = buildObjectValidator(ExceptionBusinessRuleObject.class);
          validator.validate(object);
         fail("Should have raised an " + InvocationException.class.getCanonicalName());
      } catch (InvocationException e) {
         assertEquals(IllegalStateException.class, e.getCause().getClass(),
               "The original exception of the BusinessRule should be wrapped as cause.");
      }
   }

   @Test
   public void testExceptionWhileGettingMember() {
      ExceptionBusinessMemberObject object = BusinessObjectMocks.instantiateExceptionBusinessMember();
      try {
          buildObjectValidator(ExceptionBusinessMemberObject.class).validate(object);
         fail("Should have raised an " + InvocationException.class.getCanonicalName());
      } catch (InvocationException e) {
         assertEquals(IllegalStateException.class, e.getCause().getClass(),
               "The original exception of the BusinessMember should be wrapped as cause.");
      }
   }

    @Test
    public void testBusinessObjectWithBusinessRuleOnSuperClass() {
        BusinessObjectWithNoAnnotation object = BusinessObjectMocks.instantiateBusinessObjectWithNoAnnotation();
        ObjectResult objectResult = buildObjectValidator(BusinessObjectWithNoAnnotation.class).validate(object);
        assertTrue(objectResult.isValid());
    }

    @Test
    public void testBuildValidatorWithName() {
        BValidator<DefaultValidableMock> validator = new BValidatorAnnotationBuilder<>(DefaultValidableMock.class)
                .setBusinessObjectName("myValidator")
                .build();
        ObjectResult objectResult = validator.validate(BusinessObjectMocks.instantiateValid());
        assertEquals("myValidator", objectResult.getBusinessObjectName());
    }

   private void assertResultsContains(Object[][] expectedResultsMatrix,
                                      ObjectResult actualResults) {

      assertTrue(expectedResultsMatrix.length <= actualResults.getNbOfTests());
      for (Object[] expectedResultRaw : expectedResultsMatrix) {
         RuleResult expectedRuleResult = buildExpectedRuleResult(expectedResultRaw);
         assertTrue(assertMemberResult(actualResults, (String) expectedResultRaw[MEMBER_NAME])
                     .getRuleResults().contains(expectedRuleResult),
               "actualResults should contains the entry: " + expectedRuleResult);
      }
   }

   private RuleResult buildExpectedRuleResult(Object[] expectedResult) {
      if (((String) expectedResult[RULE_ID]).isEmpty()) {
         return new RuleResult((String) expectedResult[DESCRIPTION],
               (Boolean) expectedResult[RESULT]);
      } else {
         return new RuleResult((String) expectedResult[RULE_ID],
               (String) expectedResult[DESCRIPTION], (Boolean) expectedResult[RESULT]);
      }
   }

   private ObjectResult assertMemberResult(ObjectResult objectResult,
                                           String memberName) {
      ObjectResult targetedMemberResult = getMemberResult(objectResult, memberName);
      assertNotNull(targetedMemberResult,
            String.format("A member result with name '%s' must be find", memberName));
      return targetedMemberResult;
   }

   private ObjectResult getMemberResult(ObjectResult objectResult,
                                        String memberName) {
      if (objectResult.getBusinessObjectName().equals(memberName)) {
         return objectResult;
      } else {
         for (ObjectResult businessMemberResult : objectResult.getMemberResults()) {
            ObjectResult findMemberResult = getMemberResult(businessMemberResult,
                  memberName);
            if (findMemberResult != null) {
               return findMemberResult;
            }
         }
      }
      return null;
   }

   private <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new BValidatorAnnotationBuilder<>(clazz).build();
   }

}
