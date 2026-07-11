package com.bkash.baymax.superagent_api.dto.response;

import java.time.Instant;
import java.util.List;

public record LiquidityForecastResponse(

        String agentCode,
        Instant generatedAt,
        List<LiquidityResourceForecastResponse> resources

) {
}
