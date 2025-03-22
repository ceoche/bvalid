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

import io.github.ceoche.bvalid.ObjectMocks.DefaultValidableMock;
import io.github.ceoche.bvalid.ObjectMocks.OnlyBusinessMember;
import io.github.ceoche.bvalid.ObjectMocks.WithInheritance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationInheritenceTest extends ManualInheritenceTest{

   @Test
   void testBusinessObjectWithBusinessRuleOnSuperClass() {
      ObjectMocks.BusinessObjectWithNoAnnotation object = ObjectMocks.instantiateBusinessObjectWithNoAnnotation();
      BReport report = buildObjectValidator(ObjectMocks.BusinessObjectWithNoAnnotation.class).validate(object);
      assertTrue(report.isValid());
   }

   @Override
   protected BValidatorBuilder<OnlyBusinessMember> getBOWithSubTypesBuilder() {
      return new AnnotationResolver<>(OnlyBusinessMember.class)
            .addMemberSubTypes(DefaultValidableMock.class, WithInheritance.class)
            .getBuilder();
   }

   @Override
   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }

}
