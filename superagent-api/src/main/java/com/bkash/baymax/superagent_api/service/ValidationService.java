package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.ValidationMetricsResponse;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.ScenarioRun;
import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import com.bkash.baymax.superagent_api.repository.ScenarioRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final ScenarioRunRepository scenarioRunRepository;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public ValidationMetricsResponse getValidationMetrics() {
        List<ScenarioRun> completedRuns = scenarioRunRepository.findAllByStatus(ScenarioRunStatus.COMPLETED);

        MetricAccumulator accumulator = new MetricAccumulator();
        for (ScenarioRun run : completedRuns) {
            processRun(run, accumulator);
        }

        return buildResponse(accumulator);
    }

    private void processRun(ScenarioRun run, MetricAccumulator accumulator) {
        Instant runStart = run.getStartedAt();
        Instant runEnd = run.getCompletedAt();
        accumulator.updateTimeWindow(runStart, runEnd);

        boolean isAnomalyScenario = (run.getScenarioType() != ScenarioType.NORMAL);
        List<Alert> alertsInWindow = alertRepository.findAlertsInWindow(
                run.getAgent().getAgentCode(), runStart, runEnd
        );
        boolean hasAlert = !alertsInWindow.isEmpty();

        if (isAnomalyScenario && hasAlert) {
            accumulator.tp++;
            Alert firstAlert = alertsInWindow.get(0);
            long latency = firstAlert.getDetectedAt().toEpochMilli() - runStart.toEpochMilli();
            if (latency < 0) latency = 0;
            accumulator.totalLatencyMs += latency;
        } else if (isAnomalyScenario) {
            accumulator.fn++;
        } else if (hasAlert) {
            accumulator.fp++;
        } else {
            accumulator.tn++;
        }
    }

    private ValidationMetricsResponse buildResponse(MetricAccumulator acc) {
        int totalEvaluated = acc.tp + acc.tn + acc.fp + acc.fn;
        double precision = (acc.tp + acc.fp) > 0 ? (double) acc.tp / (acc.tp + acc.fp) : 0.0;
        double recall = (acc.tp + acc.fn) > 0 ? (double) acc.tp / (acc.tp + acc.fn) : 0.0;
        double fpr = (acc.fp + acc.tn) > 0 ? (double) acc.fp / (acc.fp + acc.tn) : 0.0;
        double accuracy = totalEvaluated > 0 ? (double) (acc.tp + acc.tn) / totalEvaluated : 0.0;
        long avgLatency = acc.tp > 0 ? acc.totalLatencyMs / acc.tp : 0;

        return ValidationMetricsResponse.builder()
                .start(acc.earliestStart != null ? acc.earliestStart : Instant.now())
                .end(acc.latestEnd != null ? acc.latestEnd : Instant.now())
                .evaluatedScenarioCount(totalEvaluated)
                .truePositiveCount(acc.tp)
                .trueNegativeCount(acc.tn)
                .falsePositiveCount(acc.fp)
                .falseNegativeCount(acc.fn)
                .precision(precision)
                .recall(recall)
                .falsePositiveRate(fpr)
                .accuracy(accuracy)
                .averageDetectionLatencyMilliseconds(avgLatency)
                .validationScope("SYNTHETIC_EVALUATIONS_VS_LIVE_MODEL")
                .interpretation("Metrics distinguish deterministic synthetic scenario runs from the live model's real-time alert behavior.")
                .build();
    }

    private static class MetricAccumulator {
        int tp = 0;
        int tn = 0;
        int fp = 0;
        int fn = 0;
        long totalLatencyMs = 0;
        Instant earliestStart = null;
        Instant latestEnd = null;

        void updateTimeWindow(Instant runStart, Instant runEnd) {
            if (earliestStart == null || runStart.isBefore(earliestStart)) {
                earliestStart = runStart;
            }
            if (latestEnd == null || runEnd.isAfter(latestEnd)) {
                latestEnd = runEnd;
            }
        }
    }
}
