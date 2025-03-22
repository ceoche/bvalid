package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.BusinessObjectMocks.DefaultValidableMock;
import io.github.ceoche.bvalid.BusinessObjectMocks.OnlyBusinessMembers;
import io.github.ceoche.bvalid.BusinessObjectMocks.WithInheritance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.ceoche.bvalid.Assertions4BValid.assertReportContains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class InheritenceTest {

   @Test
   void testParentIsBusinessObjectValid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateInheritanceWithoutAnnotationValid();
      BReport report = buildObjectValidator(DefaultValidableMock.class).validate(object);
      assertTrue(report.isValid(), "the business object must be valid");
   }

   @Test
   void testParentInvalid() {
      WithInheritance object = BusinessObjectMocks.instantiateInheritanceWithInvalidParent();
      BReport report = buildObjectValidator(WithInheritance.class).validate(object);
      assertFalse(report.isValid(), "The object must be invalid");

      assertReportContains(
            new Object[][]{
                  {"With-inheritance", "", "Child attribute must be defined.", true},
                  {"With-inheritance", "rule01", "mandatoryAttribute must be defined.", false},
                  {"With-inheritance", "", "optionalAttribute must be defined if present.", true},
                  {"With-inheritance", "", "oneOrMoreAssociation must have at least one element.",
                        false}
            },
            report);
   }

   @Test
   void testBusinessObjectWithBusinessRuleOnSuperClass() {
      BusinessObjectMocks.BusinessObjectWithNoAnnotation object = BusinessObjectMocks.instantiateBusinessObjectWithNoAnnotation();
      BReport report = buildObjectValidator(BusinessObjectMocks.BusinessObjectWithNoAnnotation.class).validate(object);
      assertTrue(report.isValid());
   }

   @Test
   void testMemberSubType() {
      BValidatorBuilder<OnlyBusinessMembers> builder = new AnnotationResolver<>(OnlyBusinessMembers.class)
            .addMemberSubTypes(DefaultValidableMock.class, WithInheritance.class)
            .getBuilder();

      OnlyBusinessMembers onlyBusinessMembers = new OnlyBusinessMembers();
      onlyBusinessMembers.setValidableMock(new WithInheritance()
            .setMandatoryAttribute("attr")
            .setOneOrMoreAssociation(List.of("Asso")));

      BReport report = builder.build().validate(onlyBusinessMembers);
      assertFalse(report.isValid());
      assertReportContains(
            new Object[][]{
                  {"my-only-member", "", "Child attribute must be defined.", false},
                  {"my-only-member", "rule01", "mandatoryAttribute must be defined.", true},
                  {"my-only-member", "", "optionalAttribute must be defined if present.", true},
                  {"my-only-member", "", "oneOrMoreAssociation must have at least one element.",true}
            },
            report);
   }

   @Test
   void testManualBuilderMemberSubType() {
      BValidatorBuilder<OnlyBusinessMembers> builder = new AnnotationResolver<>(OnlyBusinessMembers.class).getBuilder();
      builder.addMemberSubTypes("my-only-member", new AnnotationResolver<>(WithInheritance.class).getBuilder());

      OnlyBusinessMembers onlyBusinessMembers = new OnlyBusinessMembers();
      onlyBusinessMembers.setValidableMock(new WithInheritance()
            .setMandatoryAttribute("attr")
            .setOneOrMoreAssociation(List.of("Asso")));

      BReport report = builder.build().validate(onlyBusinessMembers);
      assertFalse(report.isValid());
      assertReportContains(
            new Object[][]{
                  {"my-only-member", "", "Child attribute must be defined.", false},
                  {"my-only-member", "rule01", "mandatoryAttribute must be defined.", true},
                  {"my-only-member", "", "optionalAttribute must be defined if present.", true},
                  {"my-only-member", "", "oneOrMoreAssociation must have at least one element.",true}
            },
            report);
   }

   private <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }

}
