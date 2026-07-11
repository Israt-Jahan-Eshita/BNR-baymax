package com.bkash.baymax.superagent_api.listener;

import com.bkash.baymax.superagent_api.event.AlertPersistedEvent;
import com.bkash.baymax.superagent_api.service.CaseCreationCoordinatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertCaseCreationListenerTest {

    @Mock
    private CaseCreationCoordinatorService caseCreationCoordinatorService;

    @InjectMocks
    private AlertCaseCreationListener listener;

    @Test
    void committedAlertCallsEvaluateAlert() {
        String alertCode = "ALT-123";
        AlertPersistedEvent event = new AlertPersistedEvent(alertCode);

        when(caseCreationCoordinatorService.evaluateAlert(alertCode)).thenReturn(Optional.empty());

        listener.onAlertPersisted(event);

        verify(caseCreationCoordinatorService).evaluateAlert(alertCode);
    }

    @Test
    void casePolicyFailureThrowsRuntimeExceptionButDoesNotEscapeListener() {
        String alertCode = "ALT-456";
        AlertPersistedEvent event = new AlertPersistedEvent(alertCode);

        when(caseCreationCoordinatorService.evaluateAlert(alertCode))
                .thenThrow(new RuntimeException("Simulated exception"));

        assertDoesNotThrow(() -> listener.onAlertPersisted(event));
    }
}
