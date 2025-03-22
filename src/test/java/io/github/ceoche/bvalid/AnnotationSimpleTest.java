package io.github.ceoche.bvalid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AnnotationSimpleTest extends ManualSimpleTest {

   @Test
   void testNotABusinessObjectError() {
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(String.class));
   }

   @Test
   void testIllegalBusinessRuleError() {
      ObjectMocks.IllegalBusinessRuleObject illegalBusinessRule = ObjectMocks.instantiateIllegalBusinessRule();
      BValidator<ObjectMocks.IllegalBusinessRuleObject> validator = buildObjectValidator(
            ObjectMocks.IllegalBusinessRuleObject.class);
      assertThrows(InvocationException.class,
            () -> validator.validate(illegalBusinessRule));
   }

   @Test
   void testIllegalBusinessMemberError() {
      assertThrows(IllegalBusinessObjectException.class,
            () -> buildObjectValidator(ObjectMocks.IllegalBusinessMemberObject.class));
   }

   @Override
   void testMemberNoValidatorFound() {
      assertThrows(IllegalBusinessObjectException.class,
            super::testMemberNoValidatorFound,
            "This case cannot happen with Annotation, because the resolver is already checking if members are " +
                  "business objects");
   }

   @Override
   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      // we do not call super to completly change the behavior of that method.
      return new AnnotationResolver<>(clazz).buildValidator();
   }
}
