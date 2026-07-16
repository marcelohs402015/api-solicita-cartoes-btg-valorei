package com.btg.proposals.rule;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.rule.dto.ProposalContext;
import com.btg.proposals.rule.impl.OfferFinancialEligibilityRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfferFinancialEligibilityRuleTest {

    private OfferFinancialEligibilityRule rule;

    @BeforeEach
    void setUp() {
        rule = new OfferFinancialEligibilityRule();
    }

    @Test
    void shouldApproveOfferAWhenIncomeAboveMinimum() {
        ProposalContext context = new ProposalContext(
                new BigDecimal("1500"),
                BigDecimal.ZERO,
                0,
                OfferType.A,
                Collections.emptyList()
        );

        assertTrue(rule.validate(context).isEmpty());
    }

    @Test
    void shouldRejectOfferAWhenIncomeAtOrBelowMinimum() {
        ProposalContext context = new ProposalContext(
                new BigDecimal("1000"),
                BigDecimal.ZERO,
                0,
                OfferType.A,
                Collections.emptyList()
        );

        assertFalse(rule.validate(context).isEmpty());
    }

    @Test
    void shouldApproveOfferBWhenRequirementsMet() {
        ProposalContext context = new ProposalContext(
                new BigDecimal("20000"),
                new BigDecimal("6000"),
                1,
                OfferType.B,
                Collections.emptyList()
        );

        assertTrue(rule.validate(context).isEmpty());
    }

    @Test
    void shouldRejectOfferBWhenInvestmentsInsufficient() {
        ProposalContext context = new ProposalContext(
                new BigDecimal("20000"),
                new BigDecimal("5000"),
                1,
                OfferType.B,
                Collections.emptyList()
        );

        assertFalse(rule.validate(context).isEmpty());
    }

    @Test
    void shouldApproveOfferCWhenRequirementsMet() {
        ProposalContext context = new ProposalContext(
                new BigDecimal("60000"),
                BigDecimal.ZERO,
                3,
                OfferType.C,
                Collections.emptyList()
        );

        assertTrue(rule.validate(context).isEmpty());
    }

    @Test
    void shouldRejectOfferCWhenAccountYearsInsufficient() {
        ProposalContext context = new ProposalContext(
                new BigDecimal("60000"),
                BigDecimal.ZERO,
                2,
                OfferType.C,
                Collections.emptyList()
        );

        assertFalse(rule.validate(context).isEmpty());
    }
}
