package com.btg.proposals.service;

import com.btg.proposals.config.AppProperties;
import com.btg.proposals.dto.QueueStatusDTO;
import com.btg.proposals.repository.HistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueStatusService {

    private final AppProperties appProperties;
    private final HistoricoRepository historicoRepository;

    public QueueStatusDTO getStatus() {
        long count = historicoRepository.count();

        return QueueStatusDTO.builder()
                .topic(appProperties.getKafka().getTopic())
                .consumers(List.of(
                        QueueStatusDTO.ConsumerStatusDTO.builder()
                                .name("Worker Historico")
                                .groupId("historico-worker-group")
                                .status("ACTIVE")
                                .build(),
                        QueueStatusDTO.ConsumerStatusDTO.builder()
                                .name("Worker Email")
                                .groupId("email-worker-group")
                                .status("ACTIVE")
                                .build()
                ))
                .recentMessagesCount((int) count)
                .build();
    }
}
