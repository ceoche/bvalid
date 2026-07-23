/*
 * Copyright 2022-2025. Cédric Eoche-Duval
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

class BAssertion<T> {

   private final String id;

   private final String description;

   private final Predicate<T> predicate;

   private final Set<ActualValueSupplier<T>> actualValueSuppliers;

   BAssertion(String id, Predicate<T> predicate, String description, Set<ActualValueSupplier<T>> actualValueSuppliers) {
      this.id = id != null ? id : "";
      this.description = description;
      this.predicate = predicate;
      this.actualValueSuppliers = actualValueSuppliers;
   }

   String getId() {
      return id;
   }

   String getDescription() {
      return description;
   }

   Boolean apply(T object) {
      try {
         return predicate.test(object);
      } catch (InvocationException e) {
         throw e;
      } catch (Exception e) {
         throw new InvocationException(e);
      }
   }

   Map<String, String> resolveActualValues(T object) {
      try {
         Map<String, String> actualValues = new LinkedHashMap<>();
         for (ActualValueSupplier<T> actualValueSupplier : actualValueSuppliers) {
            String value = actualValueSupplier.actualValue().apply(object).toString();
            actualValues.put(actualValueSupplier.name(), value);
         }
         return actualValues;
      } catch (InvocationException e) {
         throw e;
      } catch (Exception e) {
         throw new InvocationException(e);
      }
   }
}
