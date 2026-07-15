package com.btg.proposals.rule;

import com.btg.proposals.model.enums.OfferType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class OfferFinancialEligibilityRule implements EligibilityRule {

    private static final BigDecimal OFFER_A_MIN_INCOME = new BigDecimal("1000");
    private static final BigDecimal OFFER_B_MIN_INCOME = new BigDecimal("15000");
    private static final BigDecimal OFFER_B_MIN_INVESTMENTS = new BigDecimal("5000");
    private static final BigDecimal OFFER_C_MIN_INCOME = new BigDecimal("50000");
    private static final int OFFER_C_MIN_ACCOUNT_YEARS = 2;

    @Override
    public List<String> validate(ProposalContext context) {
        List<String> reasons = new ArrayList<>();

        switch (context.tipoOferta()) {
            case A -> validateOfferA(context, reasons);
            case B -> validateOfferB(context, reasons);
            case C -> validateOfferC(context, reasons);
        }

        return reasons;
    }

    private void validateOfferA(ProposalContext context, List<String> reasons) {
        if (context.renda().compareTo(OFFER_A_MIN_INCOME) <= 0) {
            reasons.add("Renda insuficiente para Oferta A: minimo superior a R$ 1.000,00");
        }
    }

    private void validateOfferB(ProposalContext context, List<String> reasons) {
        if (context.renda().compareTo(OFFER_B_MIN_INCOME) <= 0) {
            reasons.add("Renda insuficiente para Oferta B: minimo superior a R$ 15.000,00");
        }
        if (context.investimentos().compareTo(OFFER_B_MIN_INVESTMENTS) <= 0) {
            reasons.add("Investimentos insuficientes para Oferta B: minimo superior a R$ 5.000,00");
        }
    }

    private void validateOfferC(ProposalContext context, List<String> reasons) {
        if (context.renda().compareTo(OFFER_C_MIN_INCOME) <= 0) {
            reasons.add("Renda insuficiente para Oferta C: minimo superior a R$ 50.000,00");
        }
        if (context.tempoContaAnos() <= OFFER_C_MIN_ACCOUNT_YEARS) {
            reasons.add("Tempo de conta corrente insuficiente para Oferta C: minimo superior a 2 anos");
        }
    }
}
