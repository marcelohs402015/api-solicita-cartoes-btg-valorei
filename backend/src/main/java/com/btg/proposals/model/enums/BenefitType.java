package com.btg.proposals.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BenefitType {
    CASHBACK,
    SEGURO_VIAGEM,
    SALA_VIP,
    PONTOS;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static BenefitType fromValue(String value) {
        return BenefitType.valueOf(value.toUpperCase());
    }
}
