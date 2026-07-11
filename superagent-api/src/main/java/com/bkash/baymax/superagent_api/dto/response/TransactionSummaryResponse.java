package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.TransactionSource;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionSummaryResponse(

        String transactionReference,
        String agentCode,
        String providerCode,
        String providerDisplayName,
        TransactionType type,
        BigDecimal amount,
        String syntheticAccountId,
        String scenarioRunId,
        TransactionSource source,
        Instant occurredAt

) {
}
