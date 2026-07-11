package com.bkash.baymax.superagent_api.service.ai;

import com.bkash.baymax.superagent_api.dto.ai.BaymaxOperationalContext;
import com.bkash.baymax.superagent_api.dto.response.*;
import com.bkash.baymax.superagent_api.model.enums.*;
import com.bkash.baymax.superagent_api.service.DashboardService;
import com.bkash.baymax.superagent_api.model.enums.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaymaxOperationalContextServiceTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private BaymaxOperationalContextService contextService;

    @Test
    void buildContext_assemblesDataCorrectly() {
        String agentCode = "AGT-001";
        
        AgentBalanceResponse balances = org.mockito.Mockito.mock(AgentBalanceResponse.class);
        when(balances.physicalCashBalance()).thenReturn(new BigDecimal("50000"));
        
        ProviderBalanceResponse bkashBal = org.mockito.Mockito.mock(ProviderBalanceResponse.class);
        when(bkashBal.providerCode()).thenReturn("BKASH");
        when(bkashBal.eMoneyBalance()).thenReturn(new BigDecimal("10000"));
        
        ProviderBalanceResponse nagadBal = org.mockito.Mockito.mock(ProviderBalanceResponse.class);
        when(nagadBal.providerCode()).thenReturn("NAGAD");
        when(nagadBal.eMoneyBalance()).thenReturn(new BigDecimal("20000"));
        
        when(balances.providerBalances()).thenReturn(List.of(bkashBal, nagadBal));

        LiquidityForecastResponse forecast = org.mockito.Mockito.mock(LiquidityForecastResponse.class);
        LiquidityResourceForecastResponse bkashF = org.mockito.Mockito.mock(LiquidityResourceForecastResponse.class);
        when(bkashF.providerCode()).thenReturn("BKASH");
        when(bkashF.status()).thenReturn(LiquidityPressureStatus.HIGH_PRESSURE);
        when(bkashF.projectedRunwayMinutes()).thenReturn(new BigDecimal("30"));
        when(bkashF.confidence()).thenReturn(ForecastConfidence.HIGH);
        
        LiquidityResourceForecastResponse nagadF = org.mockito.Mockito.mock(LiquidityResourceForecastResponse.class);
        when(nagadF.providerCode()).thenReturn("NAGAD");
        when(nagadF.status()).thenReturn(LiquidityPressureStatus.STABLE);
        when(nagadF.projectedRunwayMinutes()).thenReturn(null);
        when(nagadF.confidence()).thenReturn(ForecastConfidence.MEDIUM);
        
        when(forecast.resources()).thenReturn(List.of(bkashF, nagadF));

        ProviderDataHealthResponse bkashH = org.mockito.Mockito.mock(ProviderDataHealthResponse.class);
        when(bkashH.providerCode()).thenReturn("BKASH");
        when(bkashH.status()).thenReturn(ProviderDataHealthStatus.LIVE);
        when(bkashH.conflictDescription()).thenReturn(null);

        ProviderDataHealthResponse nagadH = org.mockito.Mockito.mock(ProviderDataHealthResponse.class);
        when(nagadH.providerCode()).thenReturn("NAGAD");
        when(nagadH.status()).thenReturn(ProviderDataHealthStatus.DELAYED);
        when(nagadH.conflictDescription()).thenReturn("API Timeout");
        
        List<ProviderDataHealthResponse> dataHealth = List.of(bkashH, nagadH);

        AlertPageResponse alerts = org.mockito.Mockito.mock(AlertPageResponse.class);
        AlertSummaryResponse alert1 = org.mockito.Mockito.mock(AlertSummaryResponse.class);
        when(alert1.alertCode()).thenReturn("ALT-1");
        when(alert1.alertType()).thenReturn(AlertType.CASH_OUT_VELOCITY_SPIKE);
        when(alert1.severity()).thenReturn(AlertSeverity.HIGH);
        when(alert1.providerCode()).thenReturn("BKASH");
        when(alert1.title()).thenReturn("Shortage");
        when(alert1.summary()).thenReturn("Details");
        when(alerts.alerts()).thenReturn(List.of(alert1));

        OperationalCasePageResponse cases = org.mockito.Mockito.mock(OperationalCasePageResponse.class);
        OperationalCaseSummaryResponse case1 = org.mockito.Mockito.mock(OperationalCaseSummaryResponse.class);
        when(case1.caseCode()).thenReturn("CASE-1");
        when(case1.status()).thenReturn(CaseStatus.OPEN);
        when(case1.providerCode()).thenReturn("BKASH");
        when(cases.cases()).thenReturn(List.of(case1));

        DashboardAggregateResponse mockDashboard = org.mockito.Mockito.mock(DashboardAggregateResponse.class);
        when(mockDashboard.agentCode()).thenReturn(agentCode);
        when(mockDashboard.balances()).thenReturn(balances);
        when(mockDashboard.forecast()).thenReturn(forecast);
        when(mockDashboard.dataHealth()).thenReturn(dataHealth);
        when(mockDashboard.recentAlerts()).thenReturn(alerts);
        when(mockDashboard.recentCases()).thenReturn(cases);

        when(dashboardService.getDashboardAggregate(agentCode)).thenReturn(mockDashboard);

        BaymaxOperationalContext context = contextService.buildContext(agentCode);

        assertEquals(agentCode, context.agent().agentCode());
        assertEquals(new BigDecimal("50000"), context.sharedPhysicalCash());
        assertEquals(2, context.providers().size());
        
        // Assert separation of providers
        BaymaxOperationalContext.ProviderContext bkash = context.providers().stream().filter(p -> p.providerCode().equals("BKASH")).findFirst().get();
        assertEquals(new BigDecimal("10000"), bkash.balance());
        assertEquals("HIGH_PRESSURE", bkash.liquidityStatus());
        assertEquals(30, bkash.projectedShortageMinutes());

        BaymaxOperationalContext.ProviderContext nagad = context.providers().stream().filter(p -> p.providerCode().equals("NAGAD")).findFirst().get();
        assertEquals(new BigDecimal("20000"), nagad.balance());
        assertEquals("STABLE", nagad.liquidityStatus());

        assertEquals(1, context.activeAlerts().size());
        assertEquals("ALT-1", context.activeAlerts().get(0).alertCode());

        assertEquals(2, context.providerDataHealth().size());
        BaymaxOperationalContext.ProviderDataHealthContext nagadHealth = context.providerDataHealth().stream().filter(h -> h.providerCode().equals("NAGAD")).findFirst().get();
        assertEquals("DELAYED", nagadHealth.status());

        assertEquals(1, context.activeCases().size());
        assertEquals("CASE-1", context.activeCases().get(0).caseCode());
    }
}
