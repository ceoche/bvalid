package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.ObjectMocks.DefaultValidableMock;
import io.github.ceoche.bvalid.ObjectMocks.ExceptionBusinessMemberObject;
import io.github.ceoche.bvalid.ObjectMocks.ExceptionBusinessRuleObject;
import io.github.ceoche.bvalid.ObjectMocks.IllegalBusinessObject;
import io.github.ceoche.bvalid.ObjectMocks.MemberIsNotBO;
import io.github.ceoche.bvalid.ObjectMocks.OnlyBusinessMember;
import org.junit.jupiter.api.Test;

import static io.github.ceoche.bvalid.Assertions4BValid.assertReportContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ManualSimpleTest {

   @Test
   void testNullClass() {
      assertThrows(IllegalArgumentException.class, () -> buildObjectValidator(null));
   }

   @Test
   void testValid() {
      DefaultValidableMock object = ObjectMocks.instantiateValid();
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
      DefaultValidableMock object = ObjectMocks.instantiateInvalid();
      BReport report = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertFalse(report.isValid(), "The object must be invalid");
   }

   @Test
   void testAssertionIdReport() {
      DefaultValidableMock object = ObjectMocks.instantiateValid();
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
      OnlyBusinessMember object = ObjectMocks.instantiateBusinessMemberInvalid();
      BReport report = buildObjectValidator(OnlyBusinessMember.class).validate(object);
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
      OnlyBusinessMember object = ObjectMocks.instantiateBusinessMemberNull();
      BReport report = buildObjectValidator(OnlyBusinessMember.class).validate(object);
      assertTrue(report.isValid());
   }

   @Test
   void testNoBusinessRuleNorMemberError() {
      IllegalBusinessObject object = ObjectMocks.instantiateWithoutAssertions();
      assertThrows(IllegalStateException.class,
            () -> buildObjectValidator(IllegalBusinessObject.class));
   }

   @Test
   void testMemberNoValidatorFound() {
      MemberIsNotBO object = ObjectMocks.instantiateMemberIsNotBO();
      BValidator<MemberIsNotBO> validator = buildObjectValidator(MemberIsNotBO.class);
      Throwable throwable = assertThrows(IllegalBusinessObjectException.class,
            () -> validator.validate(object));
      assertEquals("No validator found for type io.github.ceoche.bvalid.ObjectMocks$NotABusinessObject",
            throwable.getMessage());
   }

   @Test
   void testExceptionWhileValidatingRule() {
      ExceptionBusinessRuleObject object = ObjectMocks.instantiateExceptionBusinessRule();
      BValidator<ExceptionBusinessRuleObject> validator = buildObjectValidator(ExceptionBusinessRuleObject.class);
      try {
         validator.validate(object);
         fail("Should have raised an " + InvocationException.class.getCanonicalName());
      } catch (InvocationException e) {
         assertEquals(IllegalStateException.class, e.getCause().getClass(),
               "The original exception of the assertion should be wrapped as cause.");
      }
   }

   @Test
   void testExceptionWhileGettingMember() {
      ExceptionBusinessMemberObject object = ObjectMocks.instantiateExceptionBusinessMember();
      try {
         buildObjectValidator(ExceptionBusinessMemberObject.class).validate(object);
         fail("Should have raised an " + InvocationException.class.getCanonicalName());
      } catch (InvocationException e) {
         assertEquals(IllegalStateException.class, e.getCause().getClass(),
               "The original exception of the BusinessMember should be wrapped as cause.");
      }
   }

   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      if (DefaultValidableMock.class.equals(clazz)) {
         return (BValidator<R>) getDefaultValidableMockBValidatorBuilder().build();
      } else if (OnlyBusinessMember.class.equals(clazz)) {
         return (BValidator<R>) getOnlyBusinessMembersBValidatorBuilder().build();
      } else if (IllegalBusinessObject.class.equals(clazz)) {
         return (BValidator<R>) getIllegalBusinessObjectBValidatorBuilder().build();
      } else if (ExceptionBusinessRuleObject.class.equals(clazz)) {
         return (BValidator<R>) getExceptionBusinessRuleObjectBValidatorBuilder().build();
      } else if (ExceptionBusinessMemberObject.class.equals(clazz)) {
         return (BValidator<R>) getExceptionBusinessMemberObjectBValidatorBuilder().build();
      } else if (MemberIsNotBO.class.equals(clazz)) {
         return (BValidator<R>) getMemberIsNotBOBValidatorBuilder().build();
      } else if (clazz == null) {
         return (BValidator<R>) new BValidatorBuilder<>(clazz).build();
      }
      throw new IllegalArgumentException("Unknown class: " + clazz);
   }

   private static BValidatorBuilder<MemberIsNotBO> getMemberIsNotBOBValidatorBuilder() {
      return new BValidatorBuilder<>(MemberIsNotBO.class)
            .addMember("member", MemberIsNotBO::getMember);
   }

   static BValidatorBuilder<DefaultValidableMock> getDefaultValidableMockBValidatorBuilder() {
      return new BValidatorBuilder<>(DefaultValidableMock.class)
            .setObjectName("validable-mock")
            .addAssertion("rule01", DefaultValidableMock::isMandatoryAttributeValid,
                  "mandatoryAttribute must be defined.")
            .addAssertion(DefaultValidableMock::isOptionalAttributeValid,
                  "optionalAttribute must be defined if present.")
            .addAssertion(DefaultValidableMock::isOneOrMoreAssociationValid,
                  "oneOrMoreAssociation must have at least one element.");
   }

   static BValidatorBuilder<OnlyBusinessMember> getOnlyBusinessMembersBValidatorBuilder() {
      return new BValidatorBuilder<>(OnlyBusinessMember.class)
            .addMember("my-only-member", OnlyBusinessMember::getValidableMock,
                  getDefaultValidableMockBValidatorBuilder());
   }

   static BValidatorBuilder<IllegalBusinessObject> getIllegalBusinessObjectBValidatorBuilder() {
      return new BValidatorBuilder<>(IllegalBusinessObject.class)
            .setObjectName("without-assertions");
   }

   static BValidatorBuilder<ExceptionBusinessRuleObject> getExceptionBusinessRuleObjectBValidatorBuilder() {
      return new BValidatorBuilder<>(ExceptionBusinessRuleObject.class)
            .addAssertion(ExceptionBusinessRuleObject::isThrowingAnException, "To test InvocationException");
   }

   static BValidatorBuilder<ExceptionBusinessMemberObject> getExceptionBusinessMemberObjectBValidatorBuilder() {
      return new BValidatorBuilder<>(ExceptionBusinessMemberObject.class)
            .addMember("member-throwing-an-exception", ExceptionBusinessMemberObject::getMemberException);
   }
}
