package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.ObjectMocks.DefaultValidableMock;
import io.github.ceoche.bvalid.ObjectMocks.OnlyBusinessMember;
import io.github.ceoche.bvalid.ObjectMocks.WithInheritance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationInheritenceTest extends ManualInheritenceTest{

   @Test
   void testBusinessObjectWithBusinessRuleOnSuperClass() {
      ObjectMocks.BusinessObjectWithNoAnnotation object = ObjectMocks.instantiateBusinessObjectWithNoAnnotation();
      BReport report = buildObjectValidator(ObjectMocks.BusinessObjectWithNoAnnotation.class).validate(object);
      assertTrue(report.isValid());
   }

   @Override
   protected BValidatorBuilder<OnlyBusinessMember> getBOWithSubTypesBuilder() {
      return new AnnotationResolver<>(OnlyBusinessMember.class)
            .addMemberSubTypes(DefaultValidableMock.class, WithInheritance.class)
            .getBuilder();
   }

   @Override
   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }

}
