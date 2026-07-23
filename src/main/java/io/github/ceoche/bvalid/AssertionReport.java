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
import java.util.Objects;

/**
 * Report of a tested business assertion.
 */
public class AssertionReport {

   private final String id;
   private final String description;
   private final boolean valid;
   private final String location;
   private final Map<String, String> actualValues;

   /**
    * Hidden constructor without id and without actual values
    *
    * @param description description of the rule.
    * @param valid       result of the test (true for valid, false for invalid).
    * @param location    location of the assertion.
    */
   AssertionReport(String description, boolean valid, String location) {
      this(description, valid, location, Map.of());
   }

   /**
    * Hidden constructor with id and without actual values
    *
    * @param id          the identifier of the requirement or assertion for the business rule.
    * @param description a brief description of the business rule or assertion.
    * @param valid       the result of the assertion (true for a valid assertion, false for an invalid one).
    * @param location    the location associated with the assertion (e.g., file path, URL, or other identifier).
    */
   AssertionReport(String id, String description, boolean valid, String location) {
      this(id, description, valid, location, Map.of());
   }

   /**
    * Hidden constructor without rule id and with actualValues.
    *
    * @param description  description of the rule
    * @param valid        result of the test (true for valid, false for invalid)
    * @param actualValues actual values of the assertion.
    * @param location     location of the assertion.
    */
   AssertionReport(String description, boolean valid, String location, Map<String, String> actualValues) {
      this("", description, valid, location, actualValues);
   }

   /**
    * Hidden constructor with rule id and with actualValues.
    *
    * @param id           requirement or assertion id of the business assertion.
    * @param description  description of the rule.
    * @param valid        result of the test (true for valid, false for invalid).
    * @param actualValues actual values of the assertion.
    * @param location     location of the assertion.
    */
   AssertionReport(String id, String description, boolean valid, String location, Map<String, String> actualValues) {
      this.id = id;
      this.description = description;
      this.valid = valid;
      this.location = location;
      this.actualValues = new LinkedHashMap<>(actualValues);
   }

   /**
    * Get the requirement or assertion identifier of the rule (Useful for requirement engineering).
    *
    * @return the id of the rule.
    */
   public String getId() {
      return id;
   }

   /**
    * Get the description of the business assertion.
    *
    * @return the description
    */
   public String getDescription() {
      return description;
   }

   /**
    * Is the business assertion valid.
    *
    * @return true if valid, false otherwise.
    */
   public boolean isValid() {
      return valid;
   }

   /**
    * Retrieves the location associated with the assertion report.
    *
    * @return the location as a string, or null if no location is set.
    */
   public String getLocation() {
      return asLocationString();
   }

   /**
    * Retrieves the actual values associated with the tested business assertion.
    *
    * @return a map containing the actual key-value pairs of the business assertion.
    */
   public Map<String, String> getActualValues() {
      return new LinkedHashMap<>(actualValues);
   }

   @Override
   public final boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof AssertionReport that)) {
         return false;
      }
      return valid == that.valid
            && Objects.equals(id, that.id)
            && Objects.equals(description, that.description)
            && Objects.equals(location, that.location)
            && Objects.equals(actualValues, that.actualValues);
   }

   @Override
   public final int hashCode() {
      return Objects.hash(id, description, valid, location, actualValues);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(asLocationString()).append(" ");
      if (id != null && !id.isEmpty()) {
         sb.append("[").append(id).append("] ");
      }
      sb.append(description);
      if (!actualValues.isEmpty()) {
         sb.append(" - Actual values : ").append(actualValues).append(" - ");
      }
      return sb.append(" => ").append(asResultString(valid)).toString();
   }

   private String asLocationString() {
      StringBuilder sb = new StringBuilder();
      sb.append(location);
      if (actualValues.size() == 1) {
         sb.append(".").append(actualValues.keySet().iterator().next());
      }
      return sb.toString();
   }

   private String asResultString(boolean result) {
      return result ? "valid" : "invalid";
   }
}
