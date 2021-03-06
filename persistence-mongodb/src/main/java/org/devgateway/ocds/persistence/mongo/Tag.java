
package org.devgateway.ocds.persistence.mongo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum Tag {

    planning("planning"),
    planningUpdate("planningUpdate"),
    tender("tender"),
    tenderAmendment("tenderAmendment"),
    tenderUpdate("tenderUpdate"),
    tenderCancellation("tenderCancellation"),
    award("award"),
    awardUpdate("awardUpdate"),
    awardCancellation("awardCancellation"),
    contract("contract"),
    contractUpdate("contractUpdate"),
    contractAmendment("contractAmendment"),
    implementation("implementation"),
    implementationUpdate("implementationUpdate"),
    contractTermination("contractTermination"),
    compiled("compiled");
    private final String value;
    private static final Map<String, Tag> CONSTANTS = new HashMap<String, Tag>();

    static {
        for (Tag c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    Tag(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Tag fromValue(String value) {
        Tag constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
