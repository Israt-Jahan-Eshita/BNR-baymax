package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.DashboardAggregateResponse;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private AgentRepository agentRepository;
    @Mock private BalanceQueryService balanceQueryService;
    @Mock private LiquidityForecastService liquidityForecastService;
    @Mock private ProviderDataHealthService providerDataHealthService;
    @Mock private AlertQueryService alertQueryService;
    @Mock private OperationalCaseQueryService operationalCaseQueryService;
    @Mock private ScenarioQueryService scenarioQueryService;

    private DashboardService dashboardService;
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-11T12:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                agentRepository,
                balanceQueryService,
                liquidityForecastService,
                providerDataHealthService,
                alertQueryService,
                operationalCaseQueryService,
                scenarioQueryService,
                clock
        );
    }

    @Test
    void getDashboardAggregateReturnsAggregatedData() {
        String agentCode = "AGT-001";
        when(agentRepository.existsByAgentCode(agentCode)).thenReturn(true);

        DashboardAggregateResponse response = dashboardService.getDashboardAggregate(agentCode);

        assertNotNull(response);
        assertEquals(agentCode, response.agentCode());
        assertNotNull(response.aggregatedAt());

        verify(balanceQueryService).getAgentBalances(agentCode);
        verify(liquidityForecastService).getForecast(agentCode);
        verify(providerDataHealthService).getDataHealth(agentCode);
        verify(alertQueryService).getAlerts(eq(agentCode), eq(null), eq(null), eq(null), any(Pageable.class));
        verify(operationalCaseQueryService).getCases(eq(agentCode), eq(null), eq(null), eq(null), any(Pageable.class));
        verify(scenarioQueryService).getRuns(eq(agentCode), eq(null), eq(null), any(Pageable.class));
    }
}
