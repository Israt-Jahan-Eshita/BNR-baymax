package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.analytics.DetectedSignal;
import com.bkash.baymax.superagent_api.dto.response.AlertDetailResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.model.enums.SignalConfidence;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-11T10:00:00Z");

    @Mock
    private AlertRepository alertRepository;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;

    @Test
    void shouldCreateNewAlertIfFingerprintNotExists() {
        Agent agent = Agent.builder().agentCode("AGT-001").build();
        Provider provider = Provider.builder().providerCode("BKASH").build();

        DetectedSignal signal = new DetectedSignal(
                agent, provider, AlertType.CASH_OUT_VELOCITY_SPIKE, AlertSeverity.HIGH,
                SignalConfidence.MEDIUM, 60, "Spike", "Summary", List.of("Ev1"),
                "Normal", "Uncertain", "Step", NOW.minusSeconds(900), NOW
        );

        when(alertRepository.existsByAlertFingerprint(anyString())).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertService service = new AlertService(alertRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        Optional<AlertDetailResponse> response = service.createIfAbsent(signal);

        assertTrue(response.isPresent());
        verify(alertRepository).save(alertCaptor.capture());
        
        Alert savedAlert = alertCaptor.getValue();
        assertEquals("AGT-001", savedAlert.getAgent().getAgentCode());
        assertEquals(AlertType.CASH_OUT_VELOCITY_SPIKE, savedAlert.getAlertType());
    }

    @Test
    void shouldSkipIfFingerprintExists() {
        Agent agent = Agent.builder().agentCode("AGT-001").build();
        DetectedSignal signal = new DetectedSignal(
                agent, null, AlertType.CASH_OUT_VELOCITY_SPIKE, AlertSeverity.HIGH,
                SignalConfidence.MEDIUM, 60, "Spike", "Summary", List.of("Ev1"),
                "Normal", "Uncertain", "Step", NOW.minusSeconds(900), NOW
        );

        when(alertRepository.existsByAlertFingerprint(anyString())).thenReturn(true);

        AlertService service = new AlertService(alertRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        Optional<AlertDetailResponse> response = service.createIfAbsent(signal);

        assertFalse(response.isPresent());
    }
}
