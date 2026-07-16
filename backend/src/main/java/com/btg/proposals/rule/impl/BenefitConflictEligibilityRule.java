package com.btg.proposals.rule.impl;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.rule.EligibilityRule;
import com.btg.proposals.rule.dto.ProposalContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BenefitConflictEligibilityRule implements EligibilityRule {

    @Override
    public List<String> validate(ProposalContext context) {
        List<String> reasons = new ArrayList<>();
        boolean hasCashback = context.beneficios().contains(BenefitType.CASHBACK);
        boolean hasPontos = context.beneficios().contains(BenefitType.PONTOS);

        if (hasCashback && hasPontos) {
            reasons.add("Conflito de beneficios: CASHBACK e PONTOS nao podem ser selecionados juntos");
        }

        return reasons;
    }
}
