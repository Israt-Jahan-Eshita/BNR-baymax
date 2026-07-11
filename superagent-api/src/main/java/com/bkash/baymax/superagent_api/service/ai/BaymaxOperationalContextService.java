package com.bkash.baymax.superagent_api.service.ai;

import com.bkash.baymax.superagent_api.dto.ai.BaymaxOperationalContext;
import com.bkash.baymax.superagent_api.dto.response.DashboardAggregateResponse;
import com.bkash.baymax.superagent_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaymaxOperationalContextService {

    private final DashboardService dashboardService;

    @Transactional(readOnly = true)
    public BaymaxOperationalContext buildContext(String agentCode) {
        DashboardAggregateResponse dashboard = dashboardService.getDashboardAggregate(agentCode);

        var agentContext = new BaymaxOperationalContext.AgentContext(dashboard.agentCode());
        var sharedPhysicalCash = dashboard.balances().physicalCashBalance();

        var providers = dashboard.balances().providerBalances().stream()
                .map(balance -> {
                    var forecastOpt = dashboard.forecast().resources().stream()
                            .filter(f -> f.providerCode().equals(balance.providerCode()))
                            .findFirst();
                    
                    return new BaymaxOperationalContext.ProviderContext(
                            balance.providerCode(),
                            balance.eMoneyBalance(),
                            forecastOpt.map(f -> f.status().name()).orElse("UNKNOWN"),
                            forecastOpt.map(f -> f.projectedRunwayMinutes() != null ? f.projectedRunwayMinutes().intValue() : null).orElse(null),
                            forecastOpt.map(f -> f.confidence().name()).orElse("UNKNOWN")
                    );
                })
                .collect(Collectors.toList());

        var activeAlerts = dashboard.recentAlerts().alerts().stream()
                .map(alert -> new BaymaxOperationalContext.AlertContext(
                        alert.alertCode(),
                        alert.alertType().name(),
                        alert.severity().name(),
                        alert.providerCode(),
                        alert.title(),
                        alert.summary()
                ))
                .collect(Collectors.toList());

        var providerDataHealth = dashboard.dataHealth().stream()
                .map(health -> new BaymaxOperationalContext.ProviderDataHealthContext(
                        health.providerCode(),
                        health.status().name(),
                        health.conflictDescription()
                ))
                .collect(Collectors.toList());

        var activeCases = dashboard.recentCases().cases().stream()
                .map(c -> new BaymaxOperationalContext.CaseContext(
                        c.caseCode(),
                        c.status().name(),
                        null,
                        c.providerCode(),
                        null
                ))
                .collect(Collectors.toList());

        return new BaymaxOperationalContext(
                agentContext,
                sharedPhysicalCash,
                providers,
                activeAlerts,
                providerDataHealth,
                activeCases,
                dashboard.aggregatedAt()
        );
    }
}
