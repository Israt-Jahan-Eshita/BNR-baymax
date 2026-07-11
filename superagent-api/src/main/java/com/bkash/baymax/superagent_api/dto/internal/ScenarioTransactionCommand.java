package com.bkash.baymax.superagent_api.dto.internal;

import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ScenarioTransactionCommand(
        @NotBlank
        String agentCode,

        @NotBlank
        String providerCode,

        @NotNull
        TransactionType type,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotBlank
        String syntheticAccountId,

        @NotBlank
        String scenarioRunId
) {
}
