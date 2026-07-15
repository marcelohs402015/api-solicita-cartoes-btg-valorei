package com.btg.proposals.dto;

import com.btg.proposals.model.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoDTO {

    private UUID id;
    private UUID propostaId;
    private String evento;
    private ProposalStatus status;
    private Map<String, Object> payload;
    private Instant criadoEm;
}
