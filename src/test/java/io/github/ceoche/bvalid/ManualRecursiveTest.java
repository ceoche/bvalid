/*
 * Copyright 2025. Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
                  {"recursive", "", "Name must be defined.", false, "recursive"},
                  {"reference", "", "Name must be defined.", true, "recursive.reference"}
            },
            report);
   }

   @SuppressWarnings("unchecked")
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
