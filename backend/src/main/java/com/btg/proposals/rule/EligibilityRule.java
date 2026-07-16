package com.btg.proposals.rule;

import com.btg.proposals.rule.dto.ProposalContext;

import java.util.List;

public interface EligibilityRule {

    List<String> validate(ProposalContext context);
}
