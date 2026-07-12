package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.ValidationMetricsResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.ScenarioRun;
import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import com.bkash.baymax.superagent_api.repository.ScenarioRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private ScenarioRunRepository scenarioRunRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private ValidationService validationService;

    private Agent agent;

    @BeforeEach
    void setUp() {
        agent = new Agent();
        agent.setAgentCode("agent-001");
    }

    @Test
    void getValidationMetrics_withNoRuns() {
        when(scenarioRunRepository.findAllByStatus(ScenarioRunStatus.COMPLETED)).thenReturn(Collections.emptyList());

        ValidationMetricsResponse response = validationService.getValidationMetrics();

        assertEquals(0, response.getEvaluatedScenarioCount());
        assertEquals(0, response.getTruePositiveCount());
        assertEquals(0.0, response.getPrecision());
    }

    @Test
    void getValidationMetrics_withTpAndTn() {
        // True Positive
        ScenarioRun tpRun = ScenarioRun.builder()
            .agent(agent)
            .scenarioType(ScenarioType.CASH_OUT_VELOCITY_SPIKE)
            .startedAt(Instant.parse("2024-01-01T10:00:00Z"))
            .completedAt(Instant.parse("2024-01-01T10:05:00Z"))
            .build();

        Alert alert = new Alert();
        alert.setDetectedAt(Instant.parse("2024-01-01T10:02:00Z"));

        // True Negative
        ScenarioRun tnRun = ScenarioRun.builder()
            .agent(agent)
            .scenarioType(ScenarioType.NORMAL)
            .startedAt(Instant.parse("2024-01-01T11:00:00Z"))
            .completedAt(Instant.parse("2024-01-01T11:05:00Z"))
            .build();

        when(scenarioRunRepository.findAllByStatus(ScenarioRunStatus.COMPLETED)).thenReturn(List.of(tpRun, tnRun));

        when(alertRepository.findAlertsInWindow("agent-001", tpRun.getStartedAt(), tpRun.getCompletedAt()))
                .thenReturn(List.of(alert));

        when(alertRepository.findAlertsInWindow("agent-001", tnRun.getStartedAt(), tnRun.getCompletedAt()))
                .thenReturn(Collections.emptyList());

        ValidationMetricsResponse response = validationService.getValidationMetrics();

        assertEquals(2, response.getEvaluatedScenarioCount());
        assertEquals(1, response.getTruePositiveCount());
        assertEquals(1, response.getTrueNegativeCount());
        assertEquals(0, response.getFalsePositiveCount());
        assertEquals(0, response.getFalseNegativeCount());
        assertEquals(1.0, response.getPrecision());
        assertEquals(1.0, response.getRecall());
        assertEquals(0.0, response.getFalsePositiveRate());
        assertEquals(1.0, response.getAccuracy());
        assertEquals(120000, response.getAverageDetectionLatencyMilliseconds());
    }
}
