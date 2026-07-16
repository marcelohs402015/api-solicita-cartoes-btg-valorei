package com.btg.proposals.rule;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.rule.dto.ProposalContext;
import com.btg.proposals.rule.impl.BenefitConflictEligibilityRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenefitConflictEligibilityRuleTest {

    private BenefitConflictEligibilityRule rule;

    @BeforeEach
    void setUp() {
        rule = new BenefitConflictEligibilityRule();
    }

    @Test
    void shouldRejectWhenCashbackAndPontosTogether() {
        ProposalContext context = new ProposalContext(
                BigDecimal.TEN,
                BigDecimal.ZERO,
                0,
                OfferType.A,
                List.of(BenefitType.CASHBACK, BenefitType.PONTOS)
        );

        assertFalse(rule.validate(context).isEmpty());
    }

    @Test
    void shouldApproveWhenOnlyCashback() {
        ProposalContext context = new ProposalContext(
                BigDecimal.TEN,
                BigDecimal.ZERO,
                0,
                OfferType.A,
                List.of(BenefitType.CASHBACK)
        );

        assertTrue(rule.validate(context).isEmpty());
    }
}
