package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.ObjectMocks.DefaultValidableMock;
import io.github.ceoche.bvalid.ObjectMocks.OnlyBusinessMember;
import io.github.ceoche.bvalid.ObjectMocks.ParentHasMember;
import io.github.ceoche.bvalid.ObjectMocks.WithInheritance;
import io.github.ceoche.bvalid.ObjectMocks.WithInheritanceButWithoutAnnotation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.ceoche.bvalid.Assertions4BValid.assertReportContains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManualInheritenceTest {

   ManualSimpleTest manualSimpleTest = new ManualSimpleTest();

   @Test
   void testParentIsBusinessObjectValid() {
      DefaultValidableMock object = ObjectMocks.instantiateInheritanceWithoutAnnotationValid();
      BReport report = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertTrue(report.isValid(), "the business object must be valid");
   }

   @Test
   void testParentWithMembers() {
      ParentHasMember object = ObjectMocks.instantiateParentHasMember();
      BReport report = buildObjectValidator(ParentHasMember.class).validate(object);
      assertTrue(report.isValid(), "the business object must be valid");
      assertReportContains(
            new Object[][]{
                  {"ParentHasMember", "", "Member must be defined.", true},
                  {"my-only-member", "rule01", "mandatoryAttribute must be defined.", true},
                  {"my-only-member", "", "optionalAttribute must be defined if present.", true},
                  {"my-only-member", "", "oneOrMoreAssociation must have at least one element.", true}
            },
            report
      );
   }

   @Test
   void testMemberHasBusinessObjectParent() {
      OnlyBusinessMember onlyBusinessMember = new OnlyBusinessMember()
            .setValidableMock(
                  new WithInheritanceButWithoutAnnotation()
                        .setMandatoryAttribute("attr")
                        .setOneOrMoreAssociation(List.of("Asso"))
            );

      BReport report = buildObjectValidator(OnlyBusinessMember.class).validate(onlyBusinessMember);
      assertTrue(report.isValid(), "the business object must be valid");
   }

   @Test
   void testParentInvalid() {
      WithInheritance object = ObjectMocks.instantiateInheritanceWithInvalidParent();
      BReport report = buildObjectValidator(WithInheritance.class).validate(object);
      assertFalse(report.isValid(), "The object must be invalid");

      assertReportContains(
            new Object[][]{
                  {"With-inheritance", "", "Child attribute must be defined.", true},
                  {"With-inheritance", "rule01", "mandatoryAttribute must be defined.", false},
                  {"With-inheritance", "", "optionalAttribute must be defined if present.", true},
                  {"With-inheritance", "", "oneOrMoreAssociation must have at least one element.", false}
            },
            report);
   }

   @Test
   void testManualBuilderMemberSubType() {
      BValidatorBuilder<OnlyBusinessMember> builder = getBOWithSubTypesBuilder();

      OnlyBusinessMember onlyBusinessMember = new OnlyBusinessMember();
      onlyBusinessMember.setValidableMock(new WithInheritance()
            .setMandatoryAttribute("attr")
            .setOneOrMoreAssociation(List.of("Asso")));

      BReport report = builder.build().validate(onlyBusinessMember);
      assertFalse(report.isValid());
      assertReportContains(
            new Object[][]{
                  {"my-only-member", "", "Child attribute must be defined.", false},
                  {"my-only-member", "rule01", "mandatoryAttribute must be defined.", true},
                  {"my-only-member", "", "optionalAttribute must be defined if present.", true},
                  {"my-only-member", "", "oneOrMoreAssociation must have at least one element.", true}
            },
            report);
   }

   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      if (WithInheritance.class.equals(clazz)) {
         return (BValidator<R>) getWithInheritanceBValidatorBuilder().build();
      } else if (ParentHasMember.class.equals(clazz)) {
         return (BValidator<R>) getParentHasMemberBValidatorBuilder().build();
      } else {
         return manualSimpleTest.buildObjectValidator(clazz);
      }
   }

   static BValidatorBuilder<WithInheritance> getWithInheritanceBValidatorBuilder() {
      return
            new BValidatorBuilder<>(WithInheritance.class,
                  ManualSimpleTest.getDefaultValidableMockBValidatorBuilder())
                  .setObjectName("With-inheritance")
                  .addAssertion(WithInheritance::isSubtypeValid, "Child attribute must be defined.");
   }

   static BValidatorBuilder<ParentHasMember> getParentHasMemberBValidatorBuilder() {
      return new BValidatorBuilder<>(ParentHasMember.class, ManualSimpleTest.getOnlyBusinessMembersBValidatorBuilder())
            .addAssertion(ParentHasMember::isAttributeDefined, "Member must be defined.");
   }

   protected BValidatorBuilder<OnlyBusinessMember> getBOWithSubTypesBuilder() {
      return ManualSimpleTest.getOnlyBusinessMembersBValidatorBuilder()
            .addMemberSubTypes("my-only-member", getWithInheritanceBValidatorBuilder());
   }
}
