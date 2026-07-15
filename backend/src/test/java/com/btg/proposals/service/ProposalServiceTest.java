package com.btg.proposals.service;

import com.btg.proposals.dto.ProposalEventDTO;
import com.btg.proposals.dto.ProposalRequestDTO;
import com.btg.proposals.dto.ProposalResponseDTO;
import com.btg.proposals.messaging.ProposalEventPublisher;
import com.btg.proposals.model.entity.EmailDisparoEntity;
import com.btg.proposals.model.entity.HistoricoEntity;
import com.btg.proposals.model.entity.PropostaEntity;
import com.btg.proposals.model.enums.BenefitType;
import com.btg.proposals.model.enums.OfferType;
import com.btg.proposals.model.enums.ProposalStatus;
import com.btg.proposals.repository.EmailDisparoRepository;
import com.btg.proposals.repository.HistoricoRepository;
import com.btg.proposals.repository.PropostaRepository;
import com.btg.proposals.rule.EligibilityRule;
import com.btg.proposals.rule.OfferFinancialEligibilityRule;
import com.btg.proposals.support.AfterCommitExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalEventPublisher eventPublisher;

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private HistoricoWorker historicoWorker;

    @Mock
    private EmailDisparoRepository emailDisparoRepository;

    @Mock
    private HistoricoRepository historicoRepository;

    @Mock
    private AfterCommitExecutor afterCommitExecutor;

    private ProposalService proposalService;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(afterCommitExecutor).runAfterCommit(any(Runnable.class));

        lenient().when(eventPublisher.publish(any())).thenReturn(CompletableFuture.completedFuture(null));

        List<EligibilityRule> rules = List.of(new OfferFinancialEligibilityRule());
        proposalService = new ProposalService(
                rules,
                eventPublisher,
                propostaRepository,
                historicoWorker,
                emailDisparoRepository,
                historicoRepository,
                afterCommitExecutor
        );
    }

    @Test
    void shouldApproveValidProposal() {
        ProposalRequestDTO request = ProposalRequestDTO.builder()
                .renda(new BigDecimal("5000"))
                .investimentos(BigDecimal.ZERO)
                .tempoContaAnos(1)
                .tipoOferta(OfferType.A)
                .beneficios(List.of(BenefitType.CASHBACK))
                .build();

        ProposalResponseDTO response = proposalService.process(request);

        assertEquals(ProposalStatus.APPROVED, response.getStatus());
        assertTrue(response.getCardAccount().isCreated());
        assertNotNull(response.getCardAccount().getAccountId());
        verify(propostaRepository).save(any(PropostaEntity.class));
        verify(eventPublisher).publish(any(ProposalEventDTO.class));
    }

    @Test
    void shouldRejectInvalidProposal() {
        ProposalRequestDTO request = ProposalRequestDTO.builder()
                .renda(new BigDecimal("500"))
                .investimentos(BigDecimal.ZERO)
                .tempoContaAnos(1)
                .tipoOferta(OfferType.A)
                .beneficios(List.of())
                .build();

        ProposalResponseDTO response = proposalService.process(request);

        assertEquals(ProposalStatus.REJECTED, response.getStatus());
        assertFalse(response.getCardAccount().isCreated());
        assertFalse(response.getMotivosRejeicao().isEmpty());
    }

    @Test
    void shouldBuildExecutionFlowWithPendingWorkers() {
        UUID id = UUID.randomUUID();
        when(propostaRepository.findById(id)).thenReturn(Optional.of(PropostaEntity.builder().id(id).build()));
        when(historicoRepository.findByPropostaIdOrderByCriadoEmAsc(id)).thenReturn(List.of());
        when(emailDisparoRepository.findByPropostaId(id)).thenReturn(Optional.empty());

        var flow = proposalService.getExecutionFlow(id);

        assertEquals(6, flow.getSteps().size());
        assertEquals("PENDING", flow.getSteps().get(4).getStatus());
    }

    @Test
    void shouldBuildExecutionFlowWithCompletedWorkers() {
        UUID id = UUID.randomUUID();
        when(propostaRepository.findById(id)).thenReturn(Optional.of(PropostaEntity.builder().id(id).build()));
        when(historicoRepository.findByPropostaIdOrderByCriadoEmAsc(id)).thenReturn(List.of(
                HistoricoEntity.builder().evento("STATUS_ALTERADO_KAFKA").build()
        ));
        when(emailDisparoRepository.findByPropostaId(id)).thenReturn(Optional.of(
                EmailDisparoEntity.builder().id(UUID.randomUUID()).build()
        ));

        var flow = proposalService.getExecutionFlow(id);

        assertEquals("DONE", flow.getSteps().get(4).getStatus());
        assertEquals("DONE", flow.getSteps().get(5).getStatus());
    }

    @Test
    void shouldFindRecentProposals() {
        UUID id = UUID.randomUUID();
        when(propostaRepository.findAllByOrderByCriadoEmDesc(any())).thenReturn(List.of(
                PropostaEntity.builder()
                        .id(id)
                        .renda(new BigDecimal("5000"))
                        .investimentos(BigDecimal.ZERO)
                        .tempoContaAnos(1)
                        .tipoOferta(OfferType.A)
                        .beneficios(List.of())
                        .status(ProposalStatus.APPROVED)
                        .motivosRejeicao(List.of())
                        .accountId("CARD-123")
                        .criadoEm(Instant.now())
                        .build()
        ));

        var result = proposalService.findRecent(10);

        assertEquals(1, result.size());
        assertEquals(id, result.getFirst().getId());
    }
}
