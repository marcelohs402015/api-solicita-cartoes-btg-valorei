package com.btg.proposals.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OfferType {
    A,
    B,
    C;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static OfferType fromValue(String value) {
        return OfferType.valueOf(value.toUpperCase());
    }
}
