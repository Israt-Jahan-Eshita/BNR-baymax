package com.bkash.baymax.superagent_api.policy;

public record CaseCreationDecision(
        boolean shouldCreateCase,
        String reason
) {
    public CaseCreationDecision {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Decision reason cannot be null or blank");
        }
    }
}
