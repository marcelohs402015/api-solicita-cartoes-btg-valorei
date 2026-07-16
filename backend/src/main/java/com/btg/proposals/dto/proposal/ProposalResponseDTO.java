package com.btg.proposals.dto.proposal;

import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponseDTO {

    private UUID proposalId;
    private ProposalStatus status;
    private List<String> motivosRejeicao;
    private CardAccountDTO cardAccount;
    private List<BenefitType> activatedBenefits;
}
