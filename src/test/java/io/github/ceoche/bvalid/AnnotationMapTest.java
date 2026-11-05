package io.github.ceoche.bvalid;

public class AnnotationMapTest extends ManualMapTest {

   @Override
   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }
}
