package com.bkash.baymax.superagent_api.dto.response;

import java.time.Instant;
import java.util.List;

public record DashboardAggregateResponse(
        String agentCode,
        AgentBalanceResponse balances,
        LiquidityForecastResponse forecast,
        List<ProviderDataHealthResponse> dataHealth,
        AlertPageResponse recentAlerts,
        OperationalCasePageResponse recentCases,
        ScenarioRunPageResponse recentScenarioRuns,
        Instant aggregatedAt
) {
}
