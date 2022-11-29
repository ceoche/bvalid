/*
 * Copyright 2022 Cédric Eoche-Duval
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertAPITest {

   private final BValidator validator = new BValidator();

   @Test
   void testAssertAPIInvalid() {
      Object object = BusinessObjectMocks.instantiateBusinessMemberArray();
      Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(object).assertValidOrThrow(IllegalArgumentException::new)
      );
   }

   @Test
   void testAssertAPIValid() {
      Object object = BusinessObjectMocks.instantiateValid();
      Assertions.assertDoesNotThrow(
            () -> validator.validate(object).assertValidOrThrow(IllegalArgumentException::new)
      );
   }

}
