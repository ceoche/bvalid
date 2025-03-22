package io.github.ceoche.bvalid;

class AnnotationCollectionAndArrayTest extends ManualCollectionAndArrayTest {

   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }

}
