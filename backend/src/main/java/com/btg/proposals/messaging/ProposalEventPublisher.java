package com.btg.proposals.messaging;

import com.btg.proposals.dto.ProposalEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProposalEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    public void publish(ProposalEventDTO event) {
        kafkaTemplate.send(topic, event.getProposalId().toString(), event);
    }
}
