package com.btg.proposals.dto.proposal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardAccountDTO {

    private boolean created;
    private String accountId;
}
