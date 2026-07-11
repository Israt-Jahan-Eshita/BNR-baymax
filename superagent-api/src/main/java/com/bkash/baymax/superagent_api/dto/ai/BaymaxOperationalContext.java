package com.bkash.baymax.superagent_api.dto.ai;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BaymaxOperationalContext(
    AgentContext agent,
    BigDecimal sharedPhysicalCash,
    List<ProviderContext> providers,
    List<AlertContext> activeAlerts,
    List<ProviderDataHealthContext> providerDataHealth,
    List<CaseContext> activeCases,
    Instant generatedAt
) {
    public record AgentContext(String agentCode) {}
    public record ProviderContext(
        String providerCode, 
        BigDecimal balance, 
        String liquidityStatus, 
        Integer projectedShortageMinutes, 
        String confidence
    ) {}
    public record AlertContext(
        String alertCode, 
        String type, 
        String priority, 
        String providerCode, 
        String title, 
        String explanation
    ) {}
    public record ProviderDataHealthContext(
        String providerCode,
        String status,
        String reason
    ) {}
    public record CaseContext(
        String caseCode, 
        String status, 
        String owner, 
        String providerCode, 
        String recommendedAction
    ) {}
}
