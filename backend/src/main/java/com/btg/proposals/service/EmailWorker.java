package com.btg.proposals.service;

import com.btg.proposals.config.AppProperties;
import com.btg.proposals.dto.EmailDisparoDTO;
import com.btg.proposals.dto.ProposalEventDTO;
import com.btg.proposals.model.entity.EmailDisparoEntity;
import com.btg.proposals.model.enums.EmailStatus;
import com.btg.proposals.model.enums.ProposalStatus;
import com.btg.proposals.repository.EmailDisparoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailWorker {

    private static final String GROUP_ID = "email-worker-group";

    private final EmailDisparoRepository emailDisparoRepository;
    private final AppProperties appProperties;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = GROUP_ID)
    @Transactional
    public void process(ProposalEventDTO event) {
        if (event.getStatus() != ProposalStatus.APPROVED) {
            return;
        }

        Map<String, Object> template = buildTemplate(event);

        EmailDisparoEntity entity = EmailDisparoEntity.builder()
                .id(UUID.randomUUID())
                .propostaId(event.getProposalId())
                .destinatario(appProperties.getEmail().getDefaultRecipient())
                .assunto("Sua proposta de cartao foi aprovada")
                .templateJson(template)
                .status(EmailStatus.DISPARADO)
                .criadoEm(Instant.now())
                .build();

        emailDisparoRepository.save(entity);
        log.info("Worker Email: email mock disparado para proposta {}", event.getProposalId());
    }

    public List<EmailDisparoDTO> findRecent(int limit) {
        return emailDisparoRepository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    public EmailDisparoDTO findById(UUID id) {
        return emailDisparoRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Email nao encontrado"));
    }

    private Map<String, Object> buildTemplate(ProposalEventDTO event) {
        Map<String, Object> template = new HashMap<>();
        template.put("titulo", "Proposta Aprovada - BTG Valorei");
        template.put("saudacao", "Ola, Cliente Valorei!");
        template.put("mensagem", "Sua proposta de cartao de credito foi aprovada com sucesso.");
        template.put("proposalId", event.getProposalId().toString());
        template.put("tipoOferta", event.getTipoOferta().name());
        template.put("beneficios", event.getBeneficios());
        template.put("rodape", "Este e um email simulado para fins de demonstracao da POC.");
        return template;
    }

    private EmailDisparoDTO toDto(EmailDisparoEntity entity) {
        return EmailDisparoDTO.builder()
                .id(entity.getId())
                .propostaId(entity.getPropostaId())
                .destinatario(entity.getDestinatario())
                .assunto(entity.getAssunto())
                .templateJson(entity.getTemplateJson())
                .status(entity.getStatus())
                .criadoEm(entity.getCriadoEm())
                .build();
    }
}
