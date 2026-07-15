package com.btg.proposals.dto;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.model.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalEventDTO {

    private UUID eventId;
    private UUID proposalId;
    private ProposalStatus status;
    private OfferType tipoOferta;
    private List<BenefitType> beneficios;
    private List<String> motivosRejeicao;
    private Instant timestamp;
}
