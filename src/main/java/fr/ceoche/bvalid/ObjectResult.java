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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aggregate all {@link BusinessRule} and {@link BusinessMember} test results of a
 * {@link BusinessObject} .
 *
 * @author ceoche
 */
public class ObjectResult {

   private final String businessObjectName;
   private final List<RuleResult> RuleResults = new ArrayList<>();
   private final List<ObjectResult> memberResults = new ArrayList<>();

   protected ObjectResult() {
      this("");
   }

   protected ObjectResult(String businessObjectName) {
      this.businessObjectName = businessObjectName;
   }

   protected void addRuleResults(List<RuleResult> RuleResults) {
      this.RuleResults.addAll(RuleResults);
   }

   protected void addMemberResults(List<ObjectResult> memberResults) {
      this.memberResults.addAll(memberResults);
   }

   /**
    * Return the overall business rules and members result.
    *
    * @return true if all rules and members are valid, false otherwise.
    */
   public boolean isValid() {
      for (RuleResult RuleResult : RuleResults) {
         if (!RuleResult.isValid()) {
            return false;
         }
      }
      for (ObjectResult memberResult : memberResults) {
         if (!memberResult.isValid()) {
            return false;
         }
      }
      return true;
   }

   public String getBusinessObjectName() {
      return businessObjectName;
   }

   public int getNbOfTests() {
      int sum = 0;
      sum += RuleResults.size();
      for (ObjectResult memberResult : memberResults) {
         sum += memberResult.getNbOfTests();
      }
      return sum;
   }

   /**
    * Get a detail list of business rule tests.
    *
    * @return a {@link Map} of the result, where the key is the description and the value a
    * boolean representing the
    * result, true for valid and false otherwise.
    */
   public List<RuleResult> getRuleResults() {
      return new ArrayList<>(RuleResults);
   }

   public List<ObjectResult> getMemberResults() {
      return new ArrayList<>(memberResults);
   }

   /**
    * Get a detail list of failed business rules. Does not include failures of members.
    *
    * @return a {@link List} of the failed test rules.
    */
   // FIXME Maybe should return failures of members ?
   public List<RuleResult> getRuleFailures() {
      List<RuleResult> testFailures = new ArrayList<>();
      for (RuleResult testEntry : RuleResults) {
         if (!testEntry.isValid()) {
            testFailures.add(testEntry);
         }
      }
      return testFailures;
   }

   @Override
   public String toString() {
      return toString("");
   }

   private String toString(final String prefix) {
      StringBuilder sb = new StringBuilder();
      for (RuleResult RuleResult : RuleResults) {
         sb.append(prefix).append(businessObjectName).append(" ").append(RuleResult.toString()).append(System.lineSeparator());
      }
      if (!memberResults.isEmpty()) {
         String subPrefix = prefix + businessObjectName + ".";
         for (ObjectResult objectResult : memberResults) {
            sb.append(objectResult.toString(subPrefix));
         }
      }
      return sb.toString();
   }
}
