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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Aggregate all {@link BusinessAssertion} and {@link BusinessMember} test results of a {@link BusinessObject} .
 *
 * @author ceoche
 */
public class BReport {

   private final String objectName;
   private final List<AssertionReport> assertionReports = new ArrayList<>();
   private final List<BReport> memberReports = new ArrayList<>();

   BReport() {
      this("");
   }

   BReport(String objectName) {
      this.objectName = objectName;
   }

   void addAssertionReports(List<AssertionReport> assertionReports) {
      this.assertionReports.addAll(assertionReports);
   }

   void addMemberReports(List<BReport> memberReports) {
      this.memberReports.addAll(memberReports);
   }

   /**
    * Get the validation result.
    *
    * @return true if all contained rules and members are valid, false otherwise.
    */
   public boolean isValid() {
      for (AssertionReport AssertionReport : assertionReports) {
         if (!AssertionReport.isValid()) {
            return false;
         }
      }
      for (BReport memberReport : memberReports) {
         if (!memberReport.isValid()) {
            return false;
         }
      }
      return true;
   }

   /**
    * <p>Assert that the result is valid or throw a Throwable that contains a detailed report using
    * the given builder.</p>
    * <p>Example:</p>
    * <pre>{@code
    * bValidator.validate(object).orThrow(IllegalArgumentException::new);
    * }</pre>
    *
    * @param exceptionBuilder {@link Function} that takes the detailed report in {@link String} as input and return a
    *                         {@link Throwable}.
    * @param <T>              Type of the exception to throw.
    *
    * @throws T throws the Exception built by the given builder if the result is invalid.
    */
   public <T extends Throwable> void orThrow(Function<String, T> exceptionBuilder) throws T {
      if (!isValid()) {
         throw exceptionBuilder.apply(this.toString());
      }
   }


   /**
    * Get the name of the business object concerned by this report.
    *
    * @return the name of the business object.
    */
   public String getObjectName() {
      return objectName;
   }

   /**
    * Get the number of tested assertions.
    *
    * @return the number of tested assertions.
    */
   public int getNbOfAssertions() {
      return assertionReports.size() + memberReports.stream()
            .mapToInt(BReport::getNbOfAssertions)
            .sum();
   }

   /**
    * Get a detailed list of tested business assertions for this business object. Assertions of members are not
    * included.
    *
    * @return a list of the {@link AssertionReport}
    */
   public List<AssertionReport> getAssertionReports() {
      return new ArrayList<>(assertionReports);
   }

   /**
    * Get a detailed list of all tested business assertions for this business object and its members.
    * <p>
    * Include assertions of members.
    *
    * @return a list of the {@link AssertionReport}
    */
   public List<AssertionReport> getAllAssertionReports() {
      ArrayList<AssertionReport> reports = new ArrayList<>(assertionReports);
      for (BReport memberResult : memberReports) {
         reports.addAll(memberResult.getAllAssertionReports());
      }
      return reports;
   }

   /**
    * Get a detailed list of tested members of this business object.
    *
    * @return a list of {@link BReport}
    */
   public List<BReport> getMemberReports() {
      return new ArrayList<>(memberReports);
   }

   /**
    * Get a detail list of failed business assertions. Do not include failures of members.
    *
    * @return a {@link List} of the failed assertions.
    */
   public List<AssertionReport> getInvalidAssertions() {
      List<AssertionReport> invalidRules = new ArrayList<>();
      for (AssertionReport testEntry : assertionReports) {
         if (!testEntry.isValid()) {
            invalidRules.add(testEntry);
         }
      }
      return invalidRules;
   }

   /**
    * Get a detail list of all failed business assertions. Include failures of members.
    *
    * @return a {@link List} of the failed assertions.
    */
   public List<AssertionReport> getAllInvalidAssertions() {
      List<AssertionReport> invalidRules = new ArrayList<>();
      for (AssertionReport testEntry : getAllAssertionReports()) {
         if (!testEntry.isValid()) {
            invalidRules.add(testEntry);
         }
      }
      return invalidRules;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (AssertionReport assertionReport : assertionReports) {
         sb.append(assertionReport).append(System.lineSeparator());
      }
      if (!memberReports.isEmpty()) {
         for (BReport report : memberReports) {
            sb.append(report.toString());
         }
      }
      return sb.toString();
   }
}
