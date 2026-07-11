package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.ForecastConfidence;
import com.bkash.baymax.superagent_api.model.enums.LiquidityPressureStatus;
import com.bkash.baymax.superagent_api.model.enums.LiquidityResourceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LiquidityResourceForecastResponse(

        LiquidityResourceType resourceType,
        String providerCode,
        String resourceDisplayName,
        BigDecimal currentBalance,
        BigDecimal rate15,
        BigDecimal rate30,
        BigDecimal rate60,
        BigDecimal weightedConsumptionPerMinute,
        BigDecimal projectedRunwayMinutes,
        Instant estimatedShortageAt,
        LiquidityPressureStatus status,
        ForecastConfidence confidence,
        int confidenceScore,
        int recentTransactionCount,
        List<String> explanation

) {
}
