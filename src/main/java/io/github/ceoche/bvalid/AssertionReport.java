/*
 * Copyright 2022-2023 Cédric Eoche-Duval
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

import java.util.Objects;

/**
 * Report of a tested business assertion.
 */
public class AssertionReport {

    private final String id;
    private final String description;
    private final boolean valid;

    /**
     * Hidden constructor without rule id.
     *
     * @param description description of the rule
     * @param valid       result of the test (true for valid, false for invalid)
     */
    AssertionReport(String description, boolean valid) {
        this("", description, valid);
    }

    /**
     * Hidden constructor with rule id.
     *
     * @param id          requirement or assertion id of the business assertion.
     * @param description description of the rule.
     * @param valid       result of the test (true for valid, false for invalid).
     */
    AssertionReport(String id, String description, boolean valid) {
        this.id = id;
        this.description = description;
        this.valid = valid;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssertionReport)) {
            return false;
        }
        AssertionReport that = (AssertionReport) o;
        return valid == that.valid && Objects.equals(id, that.id) && Objects.equals(description,
                that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, valid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (id != null && !id.isEmpty()) {
            sb.append("[").append(id).append("] ");
        }
        return sb.append(description).append(" => ").append(asResultString(valid)).toString();
    }

    private String asResultString(boolean result) {
        return result ? "valid" : "invalid";
    }
}
