package com.btg.proposals.controller;

import com.btg.proposals.dto.queue.QueueStatusDTO;
import com.btg.proposals.service.QueueStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
@Tag(name = "Queues", description = "Status das filas Kafka e workers")
@SecurityRequirement(name = "basicAuth")
public class QueueController {

    private final QueueStatusService queueStatusService;

    @GetMapping("/status")
    @Operation(summary = "Status do topico Kafka e consumers")
    public QueueStatusDTO status() {
        return queueStatusService.getStatus();
    }
}
