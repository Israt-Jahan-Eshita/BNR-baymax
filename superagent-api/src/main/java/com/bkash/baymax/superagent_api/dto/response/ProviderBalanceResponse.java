package com.bkash.baymax.superagent_api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProviderBalanceResponse(

        String providerCode,
        String displayName,
        BigDecimal eMoneyBalance,
        Instant updatedAt

) {
}
