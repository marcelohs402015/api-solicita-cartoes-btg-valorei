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
public class ExecutionStepDTO {

    private String step;
    private String status;
    private String detail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionFlowDTO {
        private String proposalId;
        private List<ExecutionStepDTO> steps;
    }
}
