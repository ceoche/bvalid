/*
 * Copyright 2021 Cédric Eoche-Duval
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
package fr.ceoche.bvalid;

import java.util.Objects;

public class RuleResult {

   private final String id ;
   private final String description;
   private final boolean result;

   public RuleResult(String description, boolean result) {
      this("", description, result);
   }

   public RuleResult(String id, String description, boolean result) {
      this.id = id;
      this.description = description;
      this.result = result;
   }

   public String getId() {
      return id;
   }

   public String getDescription() {
      return description;
   }

   public boolean isValid() {
      return result;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof RuleResult)) {
         return false;
      }
      RuleResult that = (RuleResult) o;
      return result == that.result && Objects.equals(id, that.id) && Objects.equals(description,
            that.description);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, description, result);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      if(id != null && !id.isEmpty()) {
         sb.append("[").append(id).append("] ");
      }
      return sb.append(description).append(" => ").append(result).toString();
   }
}
