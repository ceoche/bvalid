/*
 * Copyright 2022-2023 Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * ou may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.BusinessObjectMocks.ArrayBusinessMember;
import io.github.ceoche.bvalid.BusinessObjectMocks.DefaultValidableMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertAPITest {

   @Test
   void testAssertAPIInvalid() {
      ArrayBusinessMember object = BusinessObjectMocks.instantiateBusinessMemberArray();
      BValidator<ArrayBusinessMember> validator = getValidator(ArrayBusinessMember.class);
      Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(object).assertValidOrThrow(IllegalArgumentException::new)
      );
   }

   @Test
   void testAssertAPIValid() {
      DefaultValidableMock object = BusinessObjectMocks.instantiateValid();
      BValidator<DefaultValidableMock> validator = getValidator(DefaultValidableMock.class);
      Assertions.assertDoesNotThrow(
            () -> validator.validate(object).assertValidOrThrow(IllegalArgumentException::new)
      );
   }

   private <T> BValidator<T> getValidator(Class<T> clazz) {
      return new BValidatorAnnotationBuilder<>(clazz).build();
   }

}
