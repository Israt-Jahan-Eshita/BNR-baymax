package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;

import java.time.Instant;

public record ScenarioRunSummaryResponse(
        String scenarioRunId,
        ScenarioType scenarioType,
        String agentCode,
        ScenarioRunStatus status,
        int committedTransactionCount,
        String summary,
        Instant startedAt,
        Instant completedAt
) {
}
