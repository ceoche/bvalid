/*
 * Copyright 2022 Cédric Eoche-Duval
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
package io.github.ceoche.bvalid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Aggregate all {@link BusinessRule} and {@link BusinessMember} test results of a
 * {@link BusinessObject} .
 *
 * @author ceoche
 */
public class ObjectResult {

   private final String businessObjectName;
   private final List<RuleResult> ruleResults = new ArrayList<>();
   private final List<ObjectResult> memberResults = new ArrayList<>();

   protected ObjectResult() {
      this("");
   }

   protected ObjectResult(String businessObjectName) {
      this.businessObjectName = businessObjectName;
   }

   protected void addRuleResults(List<RuleResult> RuleResults) {
      this.ruleResults.addAll(RuleResults);
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
      for (RuleResult RuleResult : ruleResults) {
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
      sum += ruleResults.size();
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
      return new ArrayList<>(ruleResults);
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
      for (RuleResult testEntry : ruleResults) {
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
      for (RuleResult RuleResult : ruleResults) {
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

   // get RuleResult path from root, ex: "person.address.street[streetNameValid]"

   /**
    * Get the path of a {@link RuleResult} from the root of the {@link ObjectResult} tree.
    * @param rulePath Ex: "person.address.street[streetNameValid]"
    *
    * The path is composed of the business object name, followed by the path of
    * the member, followed by the id of the rule.
    *
    *     <ul>
    *         <li>person is the root {@link ObjectResult}</li>
    *         <li>address is the businessObjectName of {@link BusinessMember} person</li>
    *         <li>street is the businessObjectName of {@link BusinessMember} address</li>
    *         <li>streetNameValid is the id of {@link BusinessRule} street</li>
    *     </ul>
    *
    * @return the {@link RuleResult} or null if not found.
    * @throws IllegalArgumentException if a member is not found.
    */
    public RuleResult getRuleResult(String rulePath) {
        String[] path = rulePath.split("[\\.\\s]");
        ObjectResult currentObjectResult = this;
        if(!path[0].equals(businessObjectName)) {
            throw new IllegalArgumentException("Rule path does not start with the root object name");
        }
        if(path.length == 1) {
            throw new IllegalArgumentException("Rule path must contain at least one member");
        }
        if(elementIsRule(path[1])) {
           for (RuleResult ruleResult : currentObjectResult.ruleResults) {
              if(ruleResult.getId().equals(path[1].substring(1, path[1].length() - 1))) {
                 return ruleResult;
              }
           }
        }
        else {
           ObjectResult memberResult = this.memberResults.stream()
                   .filter(objectResult -> objectResult.businessObjectName.equals(path[1]))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Rule path does not match any member"));
           return memberResult.getRuleResult(rulePath.substring(rulePath.indexOf(".") + 1));
        }
        return null;
    }

    private boolean elementIsRule(String element) {
        return element.startsWith("[") && element.endsWith("]");
    }


}
