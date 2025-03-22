package io.github.ceoche.bvalid;

class AnnotationRecursiveTest extends ManualRecursiveTest {

   @Override
   protected <T> BValidator<T> buildObjectValidator(Class<T> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }
}
