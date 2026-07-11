package com.bkash.baymax.superagent_api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AgentBalanceResponse(

        String agentCode,
        BigDecimal physicalCashBalance,
        List<ProviderBalanceResponse> providerBalances,
        Instant generatedAt

) {
}
