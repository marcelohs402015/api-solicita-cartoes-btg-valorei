package com.btg.proposals.controller;

import com.btg.proposals.dto.email.EmailDisparoDTO;
import com.btg.proposals.service.EmailWorker;
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
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Tag(name = "Emails", description = "Emails gerados pelo worker de notificacoes")
@SecurityRequirement(name = "basicAuth")
public class EmailController {

    private final EmailWorker emailWorker;

    @GetMapping
    @Operation(summary = "Listar emails disparados")
    public List<EmailDisparoDTO> list(@RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return emailWorker.findRecent(safeLimit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar email e template")
    public EmailDisparoDTO getById(@PathVariable UUID id) {
        return emailWorker.findById(id);
    }
}
