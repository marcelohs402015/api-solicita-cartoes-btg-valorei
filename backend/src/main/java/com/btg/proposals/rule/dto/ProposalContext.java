package com.btg.proposals.rule.dto;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;

import java.math.BigDecimal;
import java.util.List;

public record ProposalContext(
        BigDecimal renda,
        BigDecimal investimentos,
        Integer tempoContaAnos,
        OfferType tipoOferta,
        List<BenefitType> beneficios
) {
}
