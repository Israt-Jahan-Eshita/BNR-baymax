package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.request.CreateManualCaseRequest;
import com.bkash.baymax.superagent_api.dto.response.OperationalCaseDetailResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.OperationalCase;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CasePriority;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;
import com.bkash.baymax.superagent_api.policy.CaseCreationDecision;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.OperationalCaseRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalCaseServiceTest {

    @Mock
    private OperationalCaseRepository operationalCaseRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private CaseAuditService caseAuditService;

    private Clock clock;
    private OperationalCaseService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-07-11T10:00:00Z"), ZoneId.of("UTC"));
        service = new OperationalCaseService(
                operationalCaseRepository,
                agentRepository,
                providerRepository,
                caseAuditService,
                clock
        );
    }

    @Test
    void createFromAlertIfAbsentShouldCreateCaseAndAudit() {
        Alert alert = Alert.builder()
                .id(1L)
                .alertCode("ALT-123")
                .severity(AlertSeverity.HIGH)
                .title("Test Alert")
                .summary("Alert Summary")
                .safeNextStep("Next Step")
                .agent(Agent.builder().agentCode("AGT-001").build())
                .provider(Provider.builder().providerCode("BKASH").build())
                .build();

        when(operationalCaseRepository.existsBySourceAlertId(alert.getId())).thenReturn(false);

        OperationalCase savedCase = OperationalCase.builder()
                .caseCode("CASE-123")
                .agent(alert.getAgent())
                .provider(alert.getProvider())
                .build();
        when(operationalCaseRepository.save(any())).thenReturn(savedCase);

        CaseCreationDecision decision = new CaseCreationDecision(true, "High Severity");
        Optional<OperationalCaseDetailResponse> response = service.createFromAlertIfAbsent(alert, decision);

        assertTrue(response.isPresent());

        ArgumentCaptor<OperationalCase> caseCaptor = ArgumentCaptor.forClass(OperationalCase.class);
        verify(operationalCaseRepository).save(caseCaptor.capture());

        OperationalCase capturedCase = caseCaptor.getValue();
        assertEquals(CaseCreationSource.AUTO_ALERT_POLICY, capturedCase.getCreationSource());
        assertEquals(alert, capturedCase.getSourceAlert());
        assertEquals(CasePriority.HIGH, capturedCase.getPriority());
        assertEquals(CaseStatus.OPEN, capturedCase.getStatus());
        assertEquals(alert.getSafeNextStep(), capturedCase.getRecommendedNextStep());

        verify(caseAuditService).recordAutomaticCaseCreation(savedCase, decision.reason());
    }

    @Test
    void createFromAlertIfAbsentShouldReturnEmptyIfDuplicateAlert() {
        Alert alert = Alert.builder().id(1L).build();
        when(operationalCaseRepository.existsBySourceAlertId(alert.getId())).thenReturn(true);

        CaseCreationDecision decision = new CaseCreationDecision(true, "High Severity");
        Optional<OperationalCaseDetailResponse> response = service.createFromAlertIfAbsent(alert, decision);

        assertTrue(response.isEmpty());
        verify(operationalCaseRepository, never()).save(any());
    }

    @Test
    void createManualCaseShouldCreateCaseAndAudit() {
        CreateManualCaseRequest request = new CreateManualCaseRequest(
                "agt-001",
                "bkash",
                CasePriority.MEDIUM,
                "Title",
                "Desc",
                "Next Step",
                "OPS-01"
        );

        Agent agent = Agent.builder().agentCode("AGT-001").build();
        when(agentRepository.findByAgentCode("AGT-001")).thenReturn(Optional.of(agent));

        Provider provider = Provider.builder().providerCode("BKASH").build();
        when(providerRepository.findByProviderCode("BKASH")).thenReturn(Optional.of(provider));

        OperationalCase savedCase = OperationalCase.builder()
                .caseCode("CASE-456")
                .agent(agent)
                .provider(provider)
                .build();
        when(operationalCaseRepository.save(any())).thenReturn(savedCase);

        service.createManualCase(request);

        ArgumentCaptor<OperationalCase> caseCaptor = ArgumentCaptor.forClass(OperationalCase.class);
        verify(operationalCaseRepository).save(caseCaptor.capture());

        OperationalCase capturedCase = caseCaptor.getValue();
        assertEquals(CaseCreationSource.MANUAL_OPERATOR, capturedCase.getCreationSource());
        assertNull(capturedCase.getSourceAlert());
        assertEquals(CasePriority.MEDIUM, capturedCase.getPriority());
        assertEquals(CaseStatus.OPEN, capturedCase.getStatus());

        verify(caseAuditService).recordManualCaseCreation(savedCase, "OPS-01");
    }
}
