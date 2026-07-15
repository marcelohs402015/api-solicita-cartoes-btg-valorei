package com.btg.proposals.service;

import com.btg.proposals.config.AppProperties;
import com.btg.proposals.repository.EmailDisparoRepository;
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

    @Mock
    private EmailDisparoRepository emailDisparoRepository;

    @InjectMocks
    private QueueStatusService queueStatusService;

    @Test
    void shouldReturnQueueStatus() {
        AppProperties.Kafka kafka = new AppProperties.Kafka();
        kafka.setTopic("proposals.events");
        when(appProperties.getKafka()).thenReturn(kafka);
        when(historicoRepository.countByEvento("STATUS_ALTERADO_KAFKA")).thenReturn(5L);
        when(emailDisparoRepository.count()).thenReturn(2L);

        var status = queueStatusService.getStatus();

        assertEquals("proposals.events", status.getTopic());
        assertEquals(2, status.getConsumers().size());
        assertEquals(5, status.getRecentMessagesCount());
        assertEquals("ACTIVE", status.getConsumers().getFirst().getStatus());
        assertEquals("ACTIVE", status.getConsumers().get(1).getStatus());
    }

    @Test
    void shouldReturnIdleWhenNoMessagesProcessed() {
        AppProperties.Kafka kafka = new AppProperties.Kafka();
        kafka.setTopic("proposals.events");
        when(appProperties.getKafka()).thenReturn(kafka);
        when(historicoRepository.countByEvento("STATUS_ALTERADO_KAFKA")).thenReturn(0L);
        when(emailDisparoRepository.count()).thenReturn(0L);

        var status = queueStatusService.getStatus();

        assertEquals("IDLE", status.getConsumers().getFirst().getStatus());
        assertEquals("IDLE", status.getConsumers().get(1).getStatus());
    }
}
