package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;

import java.time.Instant;

public record ProviderDataHealthResponse(

        String agentCode,
        String providerCode,
        String providerDisplayName,
        ProviderDataHealthStatus status,
        Instant lastSuccessfulUpdateAt,
        int delayMinutes,
        String conflictDescription,
        Instant updatedAt

) {
}
