package io.github.ceoche.bvalid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManualMapTest {

   private final ManualSimpleTest manualSimpleTest = new ManualSimpleTest();

   @Test
   void testBusinessObjectValidMapMember() {
      ObjectMocks.MapBusinessMember object = ObjectMocks.instantiateMapBusinessMember();
      BReport report = buildObjectValidator(ObjectMocks.MapBusinessMember.class).validate(object);
      assertTrue(report.isValid());
   }

   @Test
   void testBusinessObjectInvalidMapMember() {
      ObjectMocks.MapBusinessMember object = ObjectMocks.instantiateMapBusinessMember();
      object.addValidableMockMap("invalid", ObjectMocks.instantiateInvalid());
      BReport report = buildObjectValidator(ObjectMocks.MapBusinessMember.class).validate(object);
      assertFalse(report.isValid());
   }

   @SuppressWarnings("unchecked")
   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      if (clazz.equals(ObjectMocks.MapBusinessMember.class)) {
         return (BValidator<R>) getMapBusinessMemberBValidatorBuilder().build();
      } else {
         return manualSimpleTest.buildObjectValidator(clazz);
      }
   }

   private static BValidatorBuilder<ObjectMocks.MapBusinessMember> getMapBusinessMemberBValidatorBuilder() {
      return new BValidatorBuilder<>(ObjectMocks.MapBusinessMember.class)
            .addMember("map", ObjectMocks.MapBusinessMember::getValidableMockMap,
                  ManualSimpleTest.getDefaultValidableMockBValidatorBuilder());
   }
}
