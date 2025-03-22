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
