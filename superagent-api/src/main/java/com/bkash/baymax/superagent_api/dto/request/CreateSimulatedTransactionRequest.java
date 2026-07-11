package com.bkash.baymax.superagent_api.dto.request;

import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateSimulatedTransactionRequest(

        @NotBlank
        @Size(max = 50)
        String agentCode,

        @NotBlank
        @Size(max = 50)
        String providerCode,

        @NotNull
        TransactionType type,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @NotBlank
        @Size(max = 100)
        String syntheticAccountId

) {
}
