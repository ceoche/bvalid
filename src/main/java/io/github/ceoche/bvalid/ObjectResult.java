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
import java.util.function.Function;

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

    ObjectResult() {
        this("");
    }

    ObjectResult(String businessObjectName) {
        this.businessObjectName = businessObjectName;
    }

    void addRuleResults(List<RuleResult> RuleResults) {
        this.ruleResults.addAll(RuleResults);
    }

    void addMemberResults(List<ObjectResult> memberResults) {
        this.memberResults.addAll(memberResults);
    }

    /**
     * Get the validation result.
     *
     * @return true if all contained rules and members are valid, false otherwise.
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

    /**
     * <p>Assert that the result is valid or throw a Throwable that contains a detailed report using
     * the given builder.</p>
     * <p>Example:</p>
     * <pre>{@code
     * bValidator.validate(object).assertValidOrThrow(IllegalArgumentException::new);
     * }</pre>
     *
     * @param exceptionBuilder {@link Function} that takes the detailed report in {@link String} as
     *                         input and return a {@link Throwable}.
     * @param <T>              Type of the exception to throw.
     * @throws T throws the Exception built by the given builder if the result is invalid.
     */
    public <T extends Throwable> void assertValidOrThrow(Function<String, T> exceptionBuilder) throws T {
        if (!isValid())
            throw exceptionBuilder.apply(this.toString());
    }


    /**
     * Get the name of the business object concerned by this result.
     *
     * @return the name of the business object.
     */
    public String getBusinessObjectName() {
        return businessObjectName;
    }

    /**
     * Get the number of rules tested.
     *
     * @return the number of rules tested.
     */
    public int getNbOfTests() {
        int sum = 0;
        sum += ruleResults.size();
        for (ObjectResult memberResult : memberResults) {
            sum += memberResult.getNbOfTests();
        }
        return sum;
    }

    /**
     * Get a detailed list of tested business rules.
     *
     * @return a list of the {@link RuleResult}
     */
    public List<RuleResult> getRuleResults() {
        return new ArrayList<>(ruleResults);
    }

    /**
     * Get a detailed list of tested members.
     *
     * @return a list of {@link ObjectResult}
     */
    public List<ObjectResult> getMemberResults() {
        return new ArrayList<>(memberResults);
    }

    /**
     * Get a detail list of failed business rules. Does not include failures of members.
     *
     * @return a {@link List} of the failed test rules.
     */
    public List<RuleResult> getInvalidRules() {
        List<RuleResult> invalidRules = new ArrayList<>();
        for (RuleResult testEntry : ruleResults) {
            if (!testEntry.isValid()) {
                invalidRules.add(testEntry);
            }
        }
        for (ObjectResult memberResult : memberResults) {
            invalidRules.addAll(memberResult.getInvalidRules());
        }
        return invalidRules;
    }

    @Override
    public String toString() {
        return toString("");
    }

    private String toString(final String prefix) {
        StringBuilder sb = new StringBuilder();
        for (RuleResult ruleResult : ruleResults) {
            sb.append(prefix).append(businessObjectName).append(" ").append(ruleResult.toString()).append(System.lineSeparator());
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
