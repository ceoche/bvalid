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

import io.github.ceoche.bvalid.ObjectMocks.ArrayBusinessMember;
import io.github.ceoche.bvalid.ObjectMocks.CollectionBusinessMembers;
import io.github.ceoche.bvalid.ObjectMocks.DefaultValidableMock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManualCollectionAndArrayTest {

   private final ManualSimpleTest manualSimpleTest = new ManualSimpleTest();

   @Test
   void testCollectionOfBO() {
      List<DefaultValidableMock> objects = new ArrayList<>();
      objects.add(ObjectMocks.instantiateValid());
      objects.add(ObjectMocks.instantiateValid());
      objects.add(ObjectMocks.instantiateValid());

      List<BReport> results = buildObjectValidator(DefaultValidableMock.class).validate(objects);

      assertFalse(results.isEmpty());
      for (int index = 0; index < results.size(); index++) {
         assertTrue(results.get(index).isValid());
         assertEquals("validable-mock[" + index + "]", results.get(index).getBusinessObjectName(),
               "Results' name of BusinessObjects in a collection should be incremented like an " +
                     "array.");
      }
   }

   @Test
   void testBusinessObjectCollectionMember() {
      CollectionBusinessMembers object = ObjectMocks.instantiateBusinessMemberCollection();
      BReport report = buildObjectValidator(CollectionBusinessMembers.class).validate(object);
      assertFalse(report.isValid());
   }

   @Test
   void testArrayOfBO() {
      DefaultValidableMock[] objects = new DefaultValidableMock[]{
            ObjectMocks.instantiateValid(),
            ObjectMocks.instantiateValid()
      };
      List<BReport> results = buildObjectValidator(DefaultValidableMock.class).validate(objects);
      assertFalse(results.isEmpty());
      for (BReport result : results) {
         assertTrue(result.isValid());
      }
   }

   @Test
   void testBusinessObjectArrayMember() {
      ArrayBusinessMember object = ObjectMocks.instantiateBusinessMemberArray();
      BReport report = buildObjectValidator(ArrayBusinessMember.class).validate(object);
      assertFalse(report.isValid());
   }

   protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      if(clazz.equals(CollectionBusinessMembers.class)) {
         return (BValidator<R>) getCollectionBusinessMembersBValidatorBuilder().build();
      } else if (clazz.equals(ArrayBusinessMember.class)) {
         return (BValidator<R>) getArrayBusinessMemberBValidatorBuilder().build();
      } else {
         return manualSimpleTest.buildObjectValidator(clazz);
      }
   }

   static BValidatorBuilder<CollectionBusinessMembers> getCollectionBusinessMembersBValidatorBuilder() {
      return new BValidatorBuilder<>(CollectionBusinessMembers.class)
            .addMember("list", CollectionBusinessMembers::getValidableMockList,
                  ManualSimpleTest.getDefaultValidableMockBValidatorBuilder())
            .addMember("set", CollectionBusinessMembers::getValidableMockSet,
                  ManualSimpleTest.getDefaultValidableMockBValidatorBuilder())
            .addMember("queue", CollectionBusinessMembers::getValidableMockQueue,
                  ManualSimpleTest.getDefaultValidableMockBValidatorBuilder());
   }

   private static BValidatorBuilder<ArrayBusinessMember> getArrayBusinessMemberBValidatorBuilder() {
      return new BValidatorBuilder<>(ArrayBusinessMember.class)
            .addMember("array", ArrayBusinessMember::getValidableMockArray,
                  ManualSimpleTest.getDefaultValidableMockBValidatorBuilder());
   }
}
