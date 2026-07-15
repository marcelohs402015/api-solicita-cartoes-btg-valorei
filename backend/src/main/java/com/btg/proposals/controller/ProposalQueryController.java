package com.btg.proposals.controller;

import com.btg.proposals.dto.ExecutionStepDTO;
import com.btg.proposals.dto.PropostaSummaryDTO;
import com.btg.proposals.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposals Query", description = "Consulta de propostas persistidas")
@SecurityRequirement(name = "basicAuth")
public class ProposalQueryController {

    private final ProposalService proposalService;

    @GetMapping
    @Operation(summary = "Listar propostas recentes")
    public List<PropostaSummaryDTO> list(@RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return proposalService.findRecent(safeLimit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar proposta por ID")
    public PropostaSummaryDTO getById(@PathVariable UUID id) {
        return proposalService.findById(id);
    }

    @GetMapping("/{id}/execution")
    @Operation(summary = "Fluxo de execucao da proposta")
    public ExecutionStepDTO.ExecutionFlowDTO getExecution(@PathVariable UUID id) {
        return proposalService.getExecutionFlow(id);
    }
}
