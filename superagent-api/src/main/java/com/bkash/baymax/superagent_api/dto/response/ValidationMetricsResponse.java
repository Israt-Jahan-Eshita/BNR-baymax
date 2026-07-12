package com.bkash.baymax.superagent_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ValidationMetricsResponse {
    private Instant start;
    private Instant end;
    private int evaluatedScenarioCount;
    private int truePositiveCount;
    private int trueNegativeCount;
    private int falsePositiveCount;
    private int falseNegativeCount;
    private double precision;
    private double recall;
    private double falsePositiveRate;
    private double accuracy;
    private long averageDetectionLatencyMilliseconds;
    private String validationScope;
    private String interpretation;
}
