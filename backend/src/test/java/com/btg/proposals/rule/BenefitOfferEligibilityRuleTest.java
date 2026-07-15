package com.btg.proposals.rule;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenefitOfferEligibilityRuleTest {

    private BenefitOfferEligibilityRule rule;

    @BeforeEach
    void setUp() {
        rule = new BenefitOfferEligibilityRule();
    }

    @Test
    void shouldRejectSeguroViagemForOfferA() {
        ProposalContext context = new ProposalContext(
                BigDecimal.TEN,
                BigDecimal.ZERO,
                0,
                OfferType.A,
                List.of(BenefitType.SEGURO_VIAGEM)
        );

        assertFalse(rule.validate(context).isEmpty());
    }

    @Test
    void shouldApproveSeguroViagemForOfferC() {
        ProposalContext context = new ProposalContext(
                BigDecimal.TEN,
                BigDecimal.ZERO,
                0,
                OfferType.C,
                List.of(BenefitType.SEGURO_VIAGEM)
        );

        assertTrue(rule.validate(context).isEmpty());
    }

    @Test
    void shouldRejectSalaVipForOfferA() {
        ProposalContext context = new ProposalContext(
                BigDecimal.TEN,
                BigDecimal.ZERO,
                0,
                OfferType.A,
                List.of(BenefitType.SALA_VIP)
        );

        assertFalse(rule.validate(context).isEmpty());
    }

    @Test
    void shouldApproveSalaVipForOfferB() {
        ProposalContext context = new ProposalContext(
                BigDecimal.TEN,
                BigDecimal.ZERO,
                0,
                OfferType.B,
                List.of(BenefitType.SALA_VIP)
        );

        assertTrue(rule.validate(context).isEmpty());
    }
}
