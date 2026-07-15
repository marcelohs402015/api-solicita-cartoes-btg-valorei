package com.btg.proposals.rule;

import java.util.List;

public interface EligibilityRule {

    List<String> validate(ProposalContext context);
}
