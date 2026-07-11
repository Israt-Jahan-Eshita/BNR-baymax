package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.model.enums.SignalConfidence;

import java.time.Instant;
import java.util.List;

public record AlertDetailResponse(

        String alertCode,
        String agentCode,
        String providerCode,
        String providerDisplayName,
        AlertType alertType,
        AlertSeverity severity,
        SignalConfidence confidence,
        int confidenceScore,
        String title,
        String summary,
        List<String> evidence,
        String possibleNormalExplanation,
        String uncertainty,
        String safeNextStep,
        Instant windowStart,
        Instant windowEnd,
        Instant detectedAt,
        Instant createdAt

) {
}
