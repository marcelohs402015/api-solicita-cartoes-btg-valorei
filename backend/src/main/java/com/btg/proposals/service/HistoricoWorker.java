package com.btg.proposals.service;

import com.btg.proposals.config.AppProperties;
import com.btg.proposals.dto.historico.HistoricoDTO;
import com.btg.proposals.dto.proposal.ProposalEventDTO;
import com.btg.proposals.model.entity.HistoricoEntity;
import com.btg.proposals.model.enums.ProposalStatus;
import com.btg.proposals.repository.HistoricoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class HistoricoWorker {

    private static final String GROUP_ID = "historico-worker-group";
    private static final String KAFKA_EVENT = "STATUS_ALTERADO_KAFKA";

    private final HistoricoRepository historicoRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = GROUP_ID)
    @Transactional
    public void process(ProposalEventDTO event) {
        if (!isValidEvent(event)) {
            log.warn("Worker Historico: evento invalido ignorado");
            return;
        }

        if (historicoRepository.existsBySourceEventId(event.getEventId())) {
            log.info("Worker Historico: evento {} ja processado", event.getEventId());
            return;
        }

        Map<String, Object> payload = objectMapper.convertValue(event, Map.class);

        HistoricoEntity entity = HistoricoEntity.builder()
                .id(UUID.randomUUID())
                .propostaId(event.getProposalId())
                .evento(KAFKA_EVENT)
                .status(event.getStatus())
                .payload(payload)
                .sourceEventId(event.getEventId())
                .criadoEm(Instant.now())
                .build();

        historicoRepository.save(entity);
        log.info("Worker Historico: evento registrado para proposta {}", event.getProposalId());
    }

    public List<HistoricoDTO> findRecent(int limit) {
        return historicoRepository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<HistoricoDTO> findByPropostaId(UUID propostaId) {
        return historicoRepository.findByPropostaIdOrderByCriadoEmAsc(propostaId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void registerSync(UUID propostaId, String evento, ProposalEventDTO event) {
        Map<String, Object> payload = event != null
                ? objectMapper.convertValue(event, Map.class)
                : new HashMap<>();

        HistoricoEntity entity = HistoricoEntity.builder()
                .id(UUID.randomUUID())
                .propostaId(propostaId)
                .evento(evento)
                .status(event != null ? event.getStatus() : ProposalStatus.APPROVED)
                .payload(payload)
                .criadoEm(Instant.now())
                .build();

        historicoRepository.save(entity);
    }

    private boolean isValidEvent(ProposalEventDTO event) {
        return event != null
                && event.getProposalId() != null
                && event.getEventId() != null
                && event.getStatus() != null;
    }

    private HistoricoDTO toDto(HistoricoEntity entity) {
        return HistoricoDTO.builder()
                .id(entity.getId())
                .propostaId(entity.getPropostaId())
                .evento(entity.getEvento())
                .status(entity.getStatus())
                .payload(entity.getPayload())
                .criadoEm(entity.getCriadoEm())
                .build();
    }
}
