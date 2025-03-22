package io.github.ceoche.bvalid;

import org.opentest4j.AssertionFailedError;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Assertions4BValid {

   static final int RESULT = 3;
   static final int DESCRIPTION = 2;
   static final int RULE_ID = 1;
   static final int MEMBER_NAME = 0;

   static void assertReportContains(Object[][] expectedReportMatrix,
                                    BReport actualReport) {

      assertTrue(expectedReportMatrix.length <= actualReport.getNbOfTests());
      for (Object[] expectedResultRaw : expectedReportMatrix) {
         AssertionReport expectedAssertionReport = buildExpectedAssertionReport(expectedResultRaw);
         assertTrue(assertMemberReport(actualReport, (String) expectedResultRaw[MEMBER_NAME])
                     .getRuleResults().contains(expectedAssertionReport),
               "actualResults should contains the entry: " + expectedAssertionReport);
      }
   }

   private static AssertionReport buildExpectedAssertionReport(Object[] expectedReport) {
      if (((String) expectedReport[RULE_ID]).isEmpty()) {
         return new AssertionReport((String) expectedReport[DESCRIPTION],
               (Boolean) expectedReport[RESULT]);
      } else {
         return new AssertionReport((String) expectedReport[RULE_ID],
               (String) expectedReport[DESCRIPTION],
               (Boolean) expectedReport[RESULT]);
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
      if (report.getBusinessObjectName().equals(memberName)) {
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
