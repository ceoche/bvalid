/*
 * Copyright 2022-2023 Cédric Eoche-Duval
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

   //--------------- Simple tests -------------------------

   @Test
   public void testValid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
      BReport report = this.buildObjectValidator(DefaultValidableMock.class).validate(object);

      assertTrue(report.isValid(), "the business object must be valid");

      assertResultsContains(
            new Object[][]{
                  {"validable-mock", "rule01", "mandatoryAttribute must be defined.", true},
                  {"validable-mock", "", "optionalAttribute must be defined if present.", true},
                  {"validable-mock", "", "oneOrMoreAssociation must have at least one element.", true}
            },
            report);
   }

   @Test
   public void testInvalid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateInvalid();
      BReport report = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertFalse(report.isValid(), "The object must be invalid");
   }

   @Test
   public void testRuleAttributesResult() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
      BReport report = buildObjectValidator(DefaultValidableMock.class).validate(object);
      for (AssertionReport AssertionReport : report.getRuleResults()) {
         if (AssertionReport.getDescription().contains("mandatoryAttribute")) {
            assertEquals("rule01", AssertionReport.getId());
         } else {
            assertTrue(AssertionReport.getId().isEmpty());
         }
      }
   }

   @Test
   public void testBusinessObjectMemberInvalid() {
      OnlyBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberInvalid();
      BReport report = buildObjectValidator(OnlyBusinessMembers.class).validate(object);
      assertFalse(
            report.isValid(),
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
            report);
   }

   @Test
   public void testBusinessObjectNullMember() {
      OnlyBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberNull();
      BReport report = buildObjectValidator(OnlyBusinessMembers.class).validate(object);
      assertTrue(report.isValid());
   }

   @Test
   public void testNotABusinessObjectError() {
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(String.class).validate("Not a validable"));
   }

   @Test
   public void testNoBusinessRuleNorMemberError() {
      IllegalBusinessObject object = BusinessObjectMocks.instantiateWithoutAssertions();
      assertThrows(IllegalStateException.class,
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

   //----------- inheritence tests -----------------

   @Test
   public void testParentIsBusinessObjectValid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateInheritanceWithoutAnnotationValid();
      BReport report = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertTrue(report.isValid(), "the business object must be valid");
   }

   @Test
   public void testParentInvalid() {
      WithInheritance object = BusinessObjectMocks.instantiateInheritanceWithInvalidParent();
      BReport report = buildObjectValidator(WithInheritance.class).validate(object);
      assertFalse(report.isValid(), "The object must be invalid");

      assertResultsContains(
            new Object[][]{
                  {"With-inheritance", "", "Sub type must be defined.", true},
                  {"With-inheritance", "rule01", "mandatoryAttribute must be defined.", false},
                  {"With-inheritance", "", "optionalAttribute must be defined if present.", true},
                  {"With-inheritance", "", "oneOrMoreAssociation must have at least one element.",
                        false}
            },
            report);
   }

   @Test
   public void testBusinessObjectWithBusinessRuleOnSuperClass() {
      BusinessObjectWithNoAnnotation object = BusinessObjectMocks.instantiateBusinessObjectWithNoAnnotation();
      BReport report = buildObjectValidator(BusinessObjectWithNoAnnotation.class).validate(object);
      assertTrue(report.isValid());
   }

   @Test
   @Disabled
   public void testBusinessObjectMemberInheritance() {
      fail("to implement");
   }

   //--------- Collections and Arrays -------

   @Test
   public void testCollectionOfBO() {
      List<DefaultValidableMock> objects = new ArrayList<>();
      objects.add(BusinessObjectMocks.instantiateValid());
      objects.add(BusinessObjectMocks.instantiateValid());
      objects.add(BusinessObjectMocks.instantiateValid());

      List<BReport> results = buildObjectValidator(DefaultValidableMock.class).validate(objects);

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
      BReport report = buildObjectValidator(CollectionBusinessMembers.class).validate(object);
      assertFalse(report.isValid());
   }

   @Test
   public void testArrayOfBO() {
      DefaultValidableMock[] objects = new DefaultValidableMock[]{
            BusinessObjectMocks.instantiateValid(),
            BusinessObjectMocks.instantiateValid()
      };
      List<BReport> results = buildObjectValidator(DefaultValidableMock.class).validate(objects);
      assertFalse(results.isEmpty());
      for (BReport result : results) {
         assertTrue(result.isValid());
      }
   }

   @Test
   public void testBusinessObjectArrayMember() {
      ArrayBusinessMember object = BusinessObjectMocks.instantiateBusinessMemberArray();
      BReport report = buildObjectValidator(ArrayBusinessMember.class).validate(object);
      assertFalse(report.isValid());
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








   private void assertResultsContains(Object[][] expectedResultsMatrix,
                                      BReport actualResults) {

      assertTrue(expectedResultsMatrix.length <= actualResults.getNbOfTests());
      for (Object[] expectedResultRaw : expectedResultsMatrix) {
         AssertionReport expectedAssertionReport = buildExpectedRuleResult(expectedResultRaw);
         assertTrue(assertMemberResult(actualResults, (String) expectedResultRaw[MEMBER_NAME])
                     .getRuleResults().contains(expectedAssertionReport),
               "actualResults should contains the entry: " + expectedAssertionReport);
      }
   }

   private AssertionReport buildExpectedRuleResult(Object[] expectedResult) {
      if (((String) expectedResult[RULE_ID]).isEmpty()) {
         return new AssertionReport((String) expectedResult[DESCRIPTION],
               (Boolean) expectedResult[RESULT]);
      } else {
         return new AssertionReport((String) expectedResult[RULE_ID],
               (String) expectedResult[DESCRIPTION], (Boolean) expectedResult[RESULT]);
      }
   }

   private BReport assertMemberResult(BReport report,
                                      String memberName) {
      BReport targetedMemberResult = getMemberResult(report, memberName);
      assertNotNull(targetedMemberResult,
            String.format("A member result with name '%s' must be find", memberName));
      return targetedMemberResult;
   }

   private BReport getMemberResult(BReport report,
                                   String memberName) {
      if (report.getBusinessObjectName().equals(memberName)) {
         return report;
      } else {
         for (BReport businessMemberResult : report.getMemberReports()) {
            BReport findMemberResult = getMemberResult(businessMemberResult,
                  memberName);
            if (findMemberResult != null) {
               return findMemberResult;
            }
         }
      }
      return null;
   }

   private <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }

}
