package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.DashboardAggregateResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AgentRepository agentRepository;
    private final BalanceQueryService balanceQueryService;
    private final LiquidityForecastService liquidityForecastService;
    private final ProviderDataHealthService providerDataHealthService;
    private final AlertQueryService alertQueryService;
    private final OperationalCaseQueryService operationalCaseQueryService;
    private final ScenarioQueryService scenarioQueryService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DashboardAggregateResponse getDashboardAggregate(String requestedAgentCode) {
        String agentCode = requestedAgentCode.trim().toUpperCase(Locale.ROOT);

        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException("Agent not found: " + agentCode);
        }

        PageRequest recentAlertsRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRequest recentCasesRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRequest recentScenarioRunsRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "startedAt"));

        return new DashboardAggregateResponse(
                agentCode,
                balanceQueryService.getAgentBalances(agentCode),
                liquidityForecastService.getForecast(agentCode),
                providerDataHealthService.getDataHealth(agentCode),
                alertQueryService.getAlerts(agentCode, null, null, null, recentAlertsRequest),
                operationalCaseQueryService.getCases(agentCode, null, null, null, recentCasesRequest),
                scenarioQueryService.getRuns(agentCode, null, null, recentScenarioRunsRequest),
                Instant.now(clock)
        );
    }
}
