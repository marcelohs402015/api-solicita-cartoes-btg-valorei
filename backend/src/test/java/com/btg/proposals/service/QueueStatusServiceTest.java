package com.btg.proposals.service;

import com.btg.proposals.config.AppProperties;
import com.btg.proposals.repository.HistoricoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueStatusServiceTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private HistoricoRepository historicoRepository;

    @InjectMocks
    private QueueStatusService queueStatusService;

    @Test
    void shouldReturnQueueStatus() {
        AppProperties.Kafka kafka = new AppProperties.Kafka();
        kafka.setTopic("proposals.events");
        when(appProperties.getKafka()).thenReturn(kafka);
        when(historicoRepository.count()).thenReturn(5L);

        var status = queueStatusService.getStatus();

        assertEquals("proposals.events", status.getTopic());
        assertEquals(2, status.getConsumers().size());
        assertEquals(5, status.getRecentMessagesCount());
        assertEquals("ACTIVE", status.getConsumers().getFirst().getStatus());
    }
}
