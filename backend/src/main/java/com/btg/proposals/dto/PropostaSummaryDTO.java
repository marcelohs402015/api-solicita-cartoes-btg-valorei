package com.btg.proposals.dto;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.model.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropostaSummaryDTO {

    private UUID id;
    private BigDecimal renda;
    private BigDecimal investimentos;
    private Integer tempoContaAnos;
    private OfferType tipoOferta;
    private List<BenefitType> beneficios;
    private ProposalStatus status;
    private List<String> motivosRejeicao;
    private String accountId;
    private Instant criadoEm;
}
