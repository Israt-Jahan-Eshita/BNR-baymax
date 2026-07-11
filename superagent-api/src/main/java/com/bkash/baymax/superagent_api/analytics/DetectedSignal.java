package com.bkash.baymax.superagent_api.analytics;

import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.model.enums.SignalConfidence;

import java.time.Instant;
import java.util.List;

public record DetectedSignal(

        Agent agent,
        Provider provider,
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
        Instant windowEnd

) {
}
