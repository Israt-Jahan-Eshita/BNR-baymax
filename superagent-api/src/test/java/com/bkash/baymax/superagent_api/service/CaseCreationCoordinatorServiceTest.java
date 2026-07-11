package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.OperationalCaseDetailResponse;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.policy.CaseCreationDecision;
import com.bkash.baymax.superagent_api.policy.CaseCreationPolicyService;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseCreationCoordinatorServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private CaseCreationPolicyService caseCreationPolicyService;

    @Mock
    private OperationalCaseService operationalCaseService;

    @InjectMocks
    private CaseCreationCoordinatorService coordinatorService;

    @Test
    void highAlertShouldCallCreateFromAlertIfAbsent() {
        String alertCode = "ALT-123";
        Alert alert = Alert.builder().alertCode(alertCode).build();
        when(alertRepository.findByAlertCode(alertCode)).thenReturn(Optional.of(alert));

        CaseCreationDecision decision = new CaseCreationDecision(true, "Reason");
        when(caseCreationPolicyService.evaluate(alert)).thenReturn(decision);

        OperationalCaseDetailResponse responseMock = mock(OperationalCaseDetailResponse.class);
        when(operationalCaseService.createFromAlertIfAbsent(alert, decision))
                .thenReturn(Optional.of(responseMock));

        Optional<OperationalCaseDetailResponse> response = coordinatorService.evaluateAlert(alertCode);

        assertTrue(response.isPresent());
        verify(operationalCaseService).createFromAlertIfAbsent(alert, decision);
    }

    @Test
    void mediumAlertShouldNotCallCreateFromAlertIfAbsent() {
        String alertCode = "ALT-456";
        Alert alert = Alert.builder().alertCode(alertCode).build();
        when(alertRepository.findByAlertCode(alertCode)).thenReturn(Optional.of(alert));

        CaseCreationDecision decision = new CaseCreationDecision(false, "Reason");
        when(caseCreationPolicyService.evaluate(alert)).thenReturn(decision);

        Optional<OperationalCaseDetailResponse> response = coordinatorService.evaluateAlert(alertCode);

        assertTrue(response.isEmpty());
        verify(operationalCaseService, never()).createFromAlertIfAbsent(any(), any());
    }
}
