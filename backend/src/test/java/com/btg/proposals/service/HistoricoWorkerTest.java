package com.btg.proposals.service;

import com.btg.proposals.dto.ProposalEventDTO;
import com.btg.proposals.model.enums.ProposalStatus;
import com.btg.proposals.repository.HistoricoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HistoricoWorkerTest {

    @Mock
    private HistoricoRepository historicoRepository;

    private HistoricoWorker historicoWorker;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        historicoWorker = new HistoricoWorker(historicoRepository, objectMapper);
    }

    @Test
    void shouldRegisterSyncHistorico() {
        UUID proposalId = UUID.randomUUID();
        ProposalEventDTO event = ProposalEventDTO.builder()
                .proposalId(proposalId)
                .status(ProposalStatus.APPROVED)
                .timestamp(Instant.now())
                .build();

        historicoWorker.registerSync(proposalId, "PROPOSTA_PERSISTIDA", event);

        ArgumentCaptor<com.btg.proposals.model.entity.HistoricoEntity> captor =
                ArgumentCaptor.forClass(com.btg.proposals.model.entity.HistoricoEntity.class);
        verify(historicoRepository).save(captor.capture());
        assertEquals("PROPOSTA_PERSISTIDA", captor.getValue().getEvento());
        assertEquals(proposalId, captor.getValue().getPropostaId());
    }
}
