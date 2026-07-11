package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.analytics.AnomalyDetectionService;
import com.bkash.baymax.superagent_api.analytics.DetectedSignal;
import com.bkash.baymax.superagent_api.dto.response.AlertDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsCoordinatorService {

    private final AnomalyDetectionService
            anomalyDetectionService;

    private final AlertService alertService;

    public List<AlertDetailResponse> evaluateAnomalies(
            String agentCode
    ) {
        List<DetectedSignal> signals =
                anomalyDetectionService.detect(agentCode);

        List<AlertDetailResponse> createdAlerts =
                new ArrayList<>();

        for (DetectedSignal signal : signals) {
            alertService
                    .createIfAbsent(signal)
                    .ifPresent(createdAlerts::add);
        }

        return createdAlerts;
    }
}
