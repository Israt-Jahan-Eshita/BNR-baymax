package com.bkash.baymax.superagent_api.dto.response;

import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.model.enums.SignalConfidence;

import java.time.Instant;

public record AlertSummaryResponse(

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
        Double mlReviewProbability,
        Boolean mlRequiresReview,
        String mlModelVersion,
        Instant detectedAt

) {
}
