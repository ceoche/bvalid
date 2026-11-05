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

import org.opentest4j.AssertionFailedError;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Assertions4BValid {

   static final int LOCATION = 4;
   static final int RESULT = 3;
   static final int DESCRIPTION = 2;
   static final int RULE_ID = 1;
   static final int MEMBER_NAME = 0;

   static void assertReportContains(Object[][] expectedReportMatrix,
                                    BReport actualReport) {

      assertTrue(expectedReportMatrix.length <= actualReport.getNbOfAssertions());
      for (Object[] expectedResultRaw : expectedReportMatrix) {
         AssertionReport expectedAssertionReport = buildExpectedAssertionReport(expectedResultRaw);
         assertTrue(assertMemberReport(actualReport, (String) expectedResultRaw[MEMBER_NAME])
                     .getAssertionReports().contains(expectedAssertionReport),
               "actualResults should contains the entry: '" + expectedAssertionReport + "'");
      }
   }

   private static AssertionReport buildExpectedAssertionReport(Object[] expectedReport) {
      if (((String) expectedReport[RULE_ID]).isEmpty()) {
         return new AssertionReport((String) expectedReport[DESCRIPTION],
               (Boolean) expectedReport[RESULT],
               (String) expectedReport[LOCATION]);
      } else {
         return new AssertionReport((String) expectedReport[RULE_ID],
               (String) expectedReport[DESCRIPTION],
               (Boolean) expectedReport[RESULT],
               (String) expectedReport[LOCATION]);
      }
   }

   static BReport assertMemberReport(BReport report, String memberName) {
      return getMemberReport(report, memberName)
            .orElseThrow(
                  () -> new AssertionFailedError(
                        String.format("Unable to find report with name '%s'", memberName))
            );
   }

   private static Optional<BReport> getMemberReport(BReport report, String memberName) {
      if (report.getObjectName().equals(memberName)) {
         return Optional.of(report);
      } else {
         for (BReport businessMemberReport : report.getMemberReports()) {
            Optional<BReport> foundMemberReport = getMemberReport(businessMemberReport, memberName);
            if (foundMemberReport.isPresent()) {
               return foundMemberReport;
            }
         }
      }
      return Optional.empty();
   }
}
