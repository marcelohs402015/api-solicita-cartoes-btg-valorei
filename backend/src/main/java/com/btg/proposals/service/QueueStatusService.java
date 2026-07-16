package com.btg.proposals.service;

import com.btg.proposals.config.AppProperties;
import com.btg.proposals.dto.queue.QueueStatusDTO;
import com.btg.proposals.repository.EmailDisparoRepository;
import com.btg.proposals.repository.HistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueStatusService {

    private static final String HISTORICO_KAFKA_EVENT = "STATUS_ALTERADO_KAFKA";

    private final AppProperties appProperties;
    private final HistoricoRepository historicoRepository;
    private final EmailDisparoRepository emailDisparoRepository;

    public QueueStatusDTO getStatus() {
        long historicoProcessed = historicoRepository.countByEvento(HISTORICO_KAFKA_EVENT);
        long emailsProcessed = emailDisparoRepository.count();

        return QueueStatusDTO.builder()
                .topic(appProperties.getKafka().getTopic())
                .consumers(List.of(
                        QueueStatusDTO.ConsumerStatusDTO.builder()
                                .name("Worker Historico")
                                .groupId("historico-worker-group")
                                .status(historicoProcessed > 0 ? "ACTIVE" : "IDLE")
                                .build(),
                        QueueStatusDTO.ConsumerStatusDTO.builder()
                                .name("Worker Email")
                                .groupId("email-worker-group")
                                .status(emailsProcessed > 0 ? "ACTIVE" : "IDLE")
                                .build()
                ))
                .recentMessagesCount((int) historicoProcessed)
                .build();
    }
}
