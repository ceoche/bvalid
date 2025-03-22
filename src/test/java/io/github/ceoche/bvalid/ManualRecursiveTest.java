package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.ObjectMocks.Recursive;
import org.junit.jupiter.api.Test;

import static io.github.ceoche.bvalid.Assertions4BValid.assertReportContains;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManualRecursiveTest {

   @Test
   void testRecursive() {
      Recursive root = new Recursive();
      Recursive other = new Recursive().setName("other");
      root.setReference(other);
      other.setReference(root);

      BValidator<Recursive> validator = buildObjectValidator(Recursive.class);
      BReport report = validator.validate(root);
      assertFalse(report.isValid());
      assertReportContains(
            new Object[][]{
                  {"recursive", "", "Name must be defined.", false},
                  {"reference", "", "Name must be defined.", true}
            },
            report);
   }

   protected  <T> BValidator<T> buildObjectValidator(Class<T> clazz) {
      if (clazz.equals(Recursive.class)) {
         BValidatorBuilder<Recursive> builder = new BValidatorBuilder<>(Recursive.class)
               .setObjectName("recursive");
         return (BValidator<T>) builder
               .addAssertion(Recursive::isNameDefined, "Name must be defined.")
               .addMember("reference", Recursive::getReference, builder)
               .build();
      } else {
         throw new IllegalArgumentException("Unsupported class: " + clazz);
      }
   }
}
