package com.btg.proposals.service;

import com.btg.proposals.config.AppProperties;
import com.btg.proposals.dto.ProposalEventDTO;
import com.btg.proposals.model.entity.EmailDisparoEntity;
import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.EmailStatus;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.model.enums.ProposalStatus;
import com.btg.proposals.repository.EmailDisparoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailWorkerTest {

    @Mock
    private EmailDisparoRepository emailDisparoRepository;

    private EmailWorker emailWorker;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.getEmail().setDefaultRecipient("cliente@teste.com");
        emailWorker = new EmailWorker(emailDisparoRepository, properties);
    }

    @Test
    void shouldCreateEmailWhenProposalApproved() {
        UUID proposalId = UUID.randomUUID();
        ProposalEventDTO event = ProposalEventDTO.builder()
                .eventId(UUID.randomUUID())
                .proposalId(proposalId)
                .status(ProposalStatus.APPROVED)
                .tipoOferta(OfferType.A)
                .beneficios(List.of(BenefitType.CASHBACK))
                .motivosRejeicao(List.of())
                .timestamp(Instant.now())
                .build();

        emailWorker.process(event);

        ArgumentCaptor<EmailDisparoEntity> captor = ArgumentCaptor.forClass(EmailDisparoEntity.class);
        verify(emailDisparoRepository).save(captor.capture());
        EmailDisparoEntity saved = captor.getValue();
        assertEquals("cliente@teste.com", saved.getDestinatario());
        assertEquals(EmailStatus.DISPARADO, saved.getStatus());
        assertEquals(proposalId, saved.getPropostaId());
    }

    @Test
    void shouldSkipDuplicateEmailForSameProposal() {
        UUID proposalId = UUID.randomUUID();
        ProposalEventDTO event = ProposalEventDTO.builder()
                .eventId(UUID.randomUUID())
                .proposalId(proposalId)
                .status(ProposalStatus.APPROVED)
                .tipoOferta(OfferType.A)
                .beneficios(List.of(BenefitType.CASHBACK))
                .motivosRejeicao(List.of())
                .timestamp(Instant.now())
                .build();

        when(emailDisparoRepository.findByPropostaId(proposalId))
                .thenReturn(Optional.of(EmailDisparoEntity.builder().id(UUID.randomUUID()).build()));

        emailWorker.process(event);

        verify(emailDisparoRepository, never()).save(any());
    }

    @Test
    void shouldIgnoreRejectedProposal() {
        ProposalEventDTO event = ProposalEventDTO.builder()
                .proposalId(UUID.randomUUID())
                .status(ProposalStatus.REJECTED)
                .tipoOferta(OfferType.A)
                .beneficios(List.of())
                .motivosRejeicao(List.of("erro"))
                .timestamp(Instant.now())
                .build();

        emailWorker.process(event);

        verify(emailDisparoRepository, never()).save(any());
    }

    @Test
    void shouldFindEmailById() {
        UUID id = UUID.randomUUID();
        when(emailDisparoRepository.findById(id)).thenReturn(Optional.of(
                EmailDisparoEntity.builder()
                        .id(id)
                        .propostaId(UUID.randomUUID())
                        .destinatario("a@b.com")
                        .assunto("Assunto")
                        .templateJson(java.util.Map.of("titulo", "Teste"))
                        .status(EmailStatus.DISPARADO)
                        .criadoEm(Instant.now())
                        .build()
        ));

        var dto = emailWorker.findById(id);

        assertEquals(id, dto.getId());
        assertEquals("Assunto", dto.getAssunto());
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        UUID id = UUID.randomUUID();
        when(emailDisparoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> emailWorker.findById(id));
    }
}
