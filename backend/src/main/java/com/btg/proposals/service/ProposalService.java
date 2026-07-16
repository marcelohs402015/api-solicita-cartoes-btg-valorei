package com.btg.proposals.service;

import com.btg.proposals.dto.proposal.CardAccountDTO;
import com.btg.proposals.dto.proposal.ExecutionStepDTO;
import com.btg.proposals.dto.proposal.PropostaSummaryDTO;
import com.btg.proposals.dto.proposal.ProposalEventDTO;
import com.btg.proposals.dto.proposal.ProposalRequestDTO;
import com.btg.proposals.dto.proposal.ProposalResponseDTO;
import com.btg.proposals.messaging.ProposalEventPublisher;
import com.btg.proposals.model.entity.HistoricoEntity;
import com.btg.proposals.model.entity.PropostaEntity;
import com.btg.proposals.model.enums.ProposalStatus;
import com.btg.proposals.repository.EmailDisparoRepository;
import com.btg.proposals.repository.HistoricoRepository;
import com.btg.proposals.repository.PropostaRepository;
import com.btg.proposals.rule.EligibilityRule;
import com.btg.proposals.rule.dto.ProposalContext;
import com.btg.proposals.support.AfterCommitExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProposalService {

    private final List<EligibilityRule> eligibilityRules;
    private final ProposalEventPublisher eventPublisher;
    private final PropostaRepository propostaRepository;
    private final HistoricoWorker historicoWorker;
    private final EmailDisparoRepository emailDisparoRepository;
    private final HistoricoRepository historicoRepository;
    private final AfterCommitExecutor afterCommitExecutor;

    @Transactional
    public ProposalResponseDTO process(ProposalRequestDTO request) {
        UUID proposalId = UUID.randomUUID();
        ProposalContext context = new ProposalContext(
                request.getRenda(),
                request.getInvestimentos(),
                request.getTempoContaAnos(),
                request.getTipoOferta(),
                request.getBeneficios()
        );

        List<String> rejectionReasons = new ArrayList<>();
        for (EligibilityRule rule : eligibilityRules) {
            rejectionReasons.addAll(rule.validate(context));
        }

        ProposalStatus status = rejectionReasons.isEmpty() ? ProposalStatus.APPROVED : ProposalStatus.REJECTED;
        String accountId = null;

        if (status == ProposalStatus.APPROVED) {
            accountId = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Chamando Microsservico de Cartoes: criando conta {} para proposta {}", accountId, proposalId);
        }

        PropostaEntity proposta = PropostaEntity.builder()
                .id(proposalId)
                .renda(request.getRenda())
                .investimentos(request.getInvestimentos())
                .tempoContaAnos(request.getTempoContaAnos())
                .tipoOferta(request.getTipoOferta())
                .beneficios(request.getBeneficios())
                .status(status)
                .motivosRejeicao(rejectionReasons.isEmpty() ? Collections.emptyList() : rejectionReasons)
                .accountId(accountId)
                .criadoEm(Instant.now())
                .build();

        propostaRepository.save(proposta);

        ProposalEventDTO event = ProposalEventDTO.builder()
                .eventId(UUID.randomUUID())
                .proposalId(proposalId)
                .status(status)
                .tipoOferta(request.getTipoOferta())
                .beneficios(request.getBeneficios())
                .motivosRejeicao(proposta.getMotivosRejeicao())
                .timestamp(Instant.now())
                .build();

        historicoWorker.registerSync(proposalId, "PROPOSTA_PERSISTIDA", event);

        afterCommitExecutor.runAfterCommit(() -> eventPublisher.publish(event)
                .whenComplete((ignored, ex) -> {
                    if (ex != null) {
                        log.error("Falha ao publicar evento Kafka para proposta {}", proposalId, ex);
                        historicoWorker.registerSync(proposalId, "EVENTO_KAFKA_FALHOU", event);
                    } else {
                        historicoWorker.registerSync(proposalId, "EVENTO_KAFKA_PUBLICADO", event);
                    }
                }));

        return ProposalResponseDTO.builder()
                .proposalId(proposalId)
                .status(status)
                .motivosRejeicao(proposta.getMotivosRejeicao())
                .cardAccount(CardAccountDTO.builder()
                        .created(status == ProposalStatus.APPROVED)
                        .accountId(accountId)
                        .build())
                .activatedBenefits(status == ProposalStatus.APPROVED ? request.getBeneficios() : Collections.emptyList())
                .build();
    }

    public List<PropostaSummaryDTO> findRecent(int limit) {
        return propostaRepository.findAllByOrderByCriadoEmDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public PropostaSummaryDTO findById(UUID id) {
        return propostaRepository.findById(id)
                .map(this::toSummary)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada"));
    }

    public ExecutionStepDTO.ExecutionFlowDTO getExecutionFlow(UUID propostaId) {
        propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada"));

        List<HistoricoEntity> historico = historicoRepository.findByPropostaIdOrderByCriadoEmAsc(propostaId);

        List<ExecutionStepDTO> steps = new ArrayList<>();
        steps.add(step("PROPOSTA_RECEBIDA", "DONE", "Payload validado"));
        steps.add(step("REGRAS_ELEGIBILIDADE", "DONE", "Motor Strategy executado"));
        steps.add(step("PERSISTENCIA_POSTGRES", "DONE", "Proposta salva no banco"));
        steps.add(step("PUBLICACAO_KAFKA", resolveKafkaPublishStatus(historico), "Evento enviado ao topico"));

        boolean historicoProcessed = historico.stream()
                .anyMatch(h -> "STATUS_ALTERADO_KAFKA".equals(h.getEvento()));
        steps.add(step("WORKER_HISTORICO", historicoProcessed ? "DONE" : "PENDING", "Auditoria assincrona"));

        boolean emailProcessed = emailDisparoRepository.findByPropostaId(propostaId).isPresent();
        steps.add(step("WORKER_EMAIL", emailProcessed ? "DONE" : "SKIPPED_OR_PENDING", "Notificacao mock"));

        return ExecutionStepDTO.ExecutionFlowDTO.builder()
                .proposalId(propostaId.toString())
                .steps(steps)
                .build();
    }

    private String resolveKafkaPublishStatus(List<HistoricoEntity> historico) {
        if (historico.stream().anyMatch(h -> "EVENTO_KAFKA_FALHOU".equals(h.getEvento()))) {
            return "FAILED";
        }
        if (historico.stream().anyMatch(h -> "EVENTO_KAFKA_PUBLICADO".equals(h.getEvento()))) {
            return "DONE";
        }
        return "PENDING";
    }

    private ExecutionStepDTO step(String name, String status, String detail) {
        return ExecutionStepDTO.builder()
                .step(name)
                .status(status)
                .detail(detail)
                .build();
    }

    private PropostaSummaryDTO toSummary(PropostaEntity entity) {
        return PropostaSummaryDTO.builder()
                .id(entity.getId())
                .renda(entity.getRenda())
                .investimentos(entity.getInvestimentos())
                .tempoContaAnos(entity.getTempoContaAnos())
                .tipoOferta(entity.getTipoOferta())
                .beneficios(entity.getBeneficios())
                .status(entity.getStatus())
                .motivosRejeicao(entity.getMotivosRejeicao())
                .accountId(entity.getAccountId())
                .criadoEm(entity.getCriadoEm())
                .build();
    }
}
