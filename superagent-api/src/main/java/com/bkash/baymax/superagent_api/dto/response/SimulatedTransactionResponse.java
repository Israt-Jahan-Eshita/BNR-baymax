package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.TransactionSource;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record SimulatedTransactionResponse(

        String transactionReference,
        String agentCode,
        String providerCode,
        TransactionType type,
        BigDecimal amount,
        String syntheticAccountId,
        Instant occurredAt,
        TransactionSource source,
        BigDecimal physicalCashBalance,
        BigDecimal providerEMoneyBalance

) {
}
