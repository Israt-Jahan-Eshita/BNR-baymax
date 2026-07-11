package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;

import java.time.Instant;

public record ScenarioRunDetailResponse(
        String scenarioRunId,
        ScenarioType scenarioType,
        String agentCode,
        String agentDisplayName,
        ScenarioRunStatus status,
        int committedTransactionCount,
        String summary,
        String failureMessage,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
