package com.btg.proposals.dto.proposal;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalRequestDTO {

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal renda;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal investimentos;

    @NotNull
    @Min(0)
    private Integer tempoContaAnos;

    @NotNull
    private OfferType tipoOferta;

    @NotNull
    private List<BenefitType> beneficios;
}
