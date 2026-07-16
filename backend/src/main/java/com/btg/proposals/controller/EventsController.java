package com.btg.proposals.controller;

import com.btg.proposals.dto.historico.HistoricoDTO;
import com.btg.proposals.service.HistoricoWorker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Eventos Kafka consumidos pelos workers")
@SecurityRequirement(name = "basicAuth")
public class EventsController {

    private final HistoricoWorker historicoWorker;

    @GetMapping
    @Operation(summary = "Listar eventos recentes do Kafka")
    public List<HistoricoDTO> getRecentEvents(@RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return historicoWorker.findRecent(safeLimit).stream()
                .filter(h -> "STATUS_ALTERADO_KAFKA".equals(h.getEvento()))
                .toList();
    }
}
