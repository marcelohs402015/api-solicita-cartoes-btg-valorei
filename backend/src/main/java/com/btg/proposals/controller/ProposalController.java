package com.btg.proposals.controller;

import com.btg.proposals.dto.ProposalRequestDTO;
import com.btg.proposals.dto.ProposalResponseDTO;
import com.btg.proposals.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposals", description = "Operacoes de propostas de cartao de credito")
@SecurityRequirement(name = "basicAuth")
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping
    @Operation(summary = "Submeter proposta de cartao de credito")
    @ApiResponse(responseCode = "201", description = "Proposta processada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisicao invalida")
    public ResponseEntity<ProposalResponseDTO> submit(@Valid @RequestBody ProposalRequestDTO request) {
        ProposalResponseDTO response = proposalService.process(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
