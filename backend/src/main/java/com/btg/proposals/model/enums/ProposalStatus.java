package com.btg.proposals.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProposalStatus {
    APPROVED,
    REJECTED;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static ProposalStatus fromValue(String value) {
        return ProposalStatus.valueOf(value.toUpperCase());
    }
}
