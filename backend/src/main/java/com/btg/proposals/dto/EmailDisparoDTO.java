package com.btg.proposals.dto;

import com.btg.proposals.model.enums.EmailStatus;
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
public class EmailDisparoDTO {

    private UUID id;
    private UUID propostaId;
    private String destinatario;
    private String assunto;
    private Map<String, Object> templateJson;
    private EmailStatus status;
    private Instant criadoEm;
}
