package com.btg.proposals.rule.impl;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.rule.EligibilityRule;
import com.btg.proposals.rule.dto.ProposalContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BenefitOfferEligibilityRule implements EligibilityRule {

    @Override
    public List<String> validate(ProposalContext context) {
        List<String> reasons = new ArrayList<>();

        if (context.beneficios().contains(BenefitType.SEGURO_VIAGEM) && context.tipoOferta() != OfferType.C) {
            reasons.add("Beneficio SEGURO_VIAGEM disponivel apenas para Oferta C");
        }

        if (context.beneficios().contains(BenefitType.SALA_VIP)
                && context.tipoOferta() != OfferType.B
                && context.tipoOferta() != OfferType.C) {
            reasons.add("Beneficio SALA_VIP disponivel apenas para Ofertas B e C");
        }

        return reasons;
    }
}
