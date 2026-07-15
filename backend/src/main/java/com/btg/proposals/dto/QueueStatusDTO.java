package com.btg.proposals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusDTO {

    private String topic;
    private List<ConsumerStatusDTO> consumers;
    private int recentMessagesCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumerStatusDTO {
        private String name;
        private String groupId;
        private String status;
    }
}
