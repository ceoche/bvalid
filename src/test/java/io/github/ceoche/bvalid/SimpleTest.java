package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.BusinessObjectMocks.DefaultValidableMock;
import org.junit.jupiter.api.Test;

import static io.github.ceoche.bvalid.Assertions4BValid.assertReportContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SimpleTest {

   @Test
   void testValid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
      BReport report = this.buildObjectValidator(DefaultValidableMock.class).validate(object);

      assertTrue(report.isValid(), "the business object must be valid");

      assertReportContains(
            new Object[][]{
                  {"validable-mock", "rule01", "mandatoryAttribute must be defined.", true},
                  {"validable-mock", "", "optionalAttribute must be defined if present.", true},
                  {"validable-mock", "", "oneOrMoreAssociation must have at least one element.", true}
            },
            report);
   }

   @Test
   void testInvalid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateInvalid();
      BReport report = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertFalse(report.isValid(), "The object must be invalid");
   }

   @Test
   void testAssertionIdReport() {
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
   void testBusinessObjectMemberInvalid() {
      BusinessObjectMocks.OnlyBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberInvalid();
      BReport report = buildObjectValidator(BusinessObjectMocks.OnlyBusinessMembers.class).validate(object);
      assertFalse(
            report.isValid(),
            "When an attribute of a BusinessObject is an invalid BusinessObject, the validation " +
                  "must detect it and " +
                  "invalidate the businessObject");

      assertReportContains(
            new Object[][]{
                  {"my-only-member", "rule01", "mandatoryAttribute must be defined.", false},
                  {"my-only-member", "", "optionalAttribute must be defined if present.", false},
                  {"my-only-member", "", "oneOrMoreAssociation must have at least one element.",
                        false}
            },
            report);
   }

   @Test
   void testBusinessObjectNullMember() {
      BusinessObjectMocks.OnlyBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberNull();
      BReport report = buildObjectValidator(BusinessObjectMocks.OnlyBusinessMembers.class).validate(object);
      assertTrue(report.isValid());
   }

   @Test
   void testNotABusinessObjectError() {
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(String.class));
   }

   @Test
   void testNoBusinessRuleNorMemberError() {
      BusinessObjectMocks.IllegalBusinessObject object = BusinessObjectMocks.instantiateWithoutAssertions();
      assertThrows(IllegalStateException.class,
            () -> buildObjectValidator(BusinessObjectMocks.IllegalBusinessObject.class).validate(object));
   }

   @Test
   void testIllegalBusinessRuleError() {
      Object object = BusinessObjectMocks.instantiateIllegalBusinessRule();
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(Object.class).validate(object));
   }

   @Test
   void testIllegalBusinessMemberError() {
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(BusinessObjectMocks.IllegalBusinessMemberObject.class));
   }

   @Test
   void testExceptionWhileValidatingRule() {
      BusinessObjectMocks.ExceptionBusinessRuleObject object = BusinessObjectMocks.instantiateExceptionBusinessRule();
      try {
         BValidator<BusinessObjectMocks.ExceptionBusinessRuleObject> validator = buildObjectValidator(
               BusinessObjectMocks.ExceptionBusinessRuleObject.class);
         validator.validate(object);
         fail("Should have raised an " + InvocationException.class.getCanonicalName());
      } catch (InvocationException e) {
         assertEquals(IllegalStateException.class, e.getCause().getClass(),
               "The original exception of the BusinessRule should be wrapped as cause.");
      }
   }

   @Test
   void testExceptionWhileGettingMember() {
      BusinessObjectMocks.ExceptionBusinessMemberObject object = BusinessObjectMocks.instantiateExceptionBusinessMember();
      try {
         buildObjectValidator(BusinessObjectMocks.ExceptionBusinessMemberObject.class).validate(object);
         fail("Should have raised an " + InvocationException.class.getCanonicalName());
      } catch (InvocationException e) {
         assertEquals(IllegalStateException.class, e.getCause().getClass(),
               "The original exception of the BusinessMember should be wrapped as cause.");
      }
   }

   private <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }

}
