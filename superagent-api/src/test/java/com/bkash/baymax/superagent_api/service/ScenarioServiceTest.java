package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.ScenarioRunDetailResponse;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.ScenarioRun;
import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ScenarioRunRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

    @Mock
    private ScenarioRunRepository scenarioRunRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ProviderDataHealthService providerDataHealthService;
    @Mock
    private ScenarioRunPersistenceService scenarioRunPersistenceService;

    private ScenarioService scenarioService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-11T12:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        ScenarioDefinitionService definitionService = new ScenarioDefinitionService();
        scenarioService = new ScenarioService(
                scenarioRunRepository, agentRepository, transactionService,
                providerDataHealthService, definitionService, scenarioRunPersistenceService, clock
        );
    }

    @Test
    void repeatedAmountClusterInjectsTransactions() {
        String agentCode = "AGT-001";
        Agent agent = Agent.builder().agentCode(agentCode).build();
        when(agentRepository.findByAgentCode(agentCode)).thenReturn(Optional.of(agent));

        when(scenarioRunPersistenceService.completeRun(anyString(), any(Instant.class)))
                .thenAnswer(inv -> ScenarioRun.builder().scenarioRunId(inv.getArgument(0)).status(ScenarioRunStatus.COMPLETED).agent(agent).committedTransactionCount(7).build());

        ScenarioRunDetailResponse response = scenarioService.runScenario(agentCode, ScenarioType.REPEATED_AMOUNT_CLUSTER);

        assertEquals(ScenarioRunStatus.COMPLETED, response.status());
        assertEquals(7, response.committedTransactionCount());
        
        verify(transactionService, times(7)).createScenarioTransaction(any());
        verify(scenarioRunPersistenceService, times(7)).updateProgress(anyString(), anyInt());
    }

    @Test
    void cashOutVelocitySpikeInjectsTransactions() {
        String agentCode = "AGT-001";
        Agent agent = Agent.builder().agentCode(agentCode).build();
        when(agentRepository.findByAgentCode(agentCode)).thenReturn(Optional.of(agent));

        when(scenarioRunPersistenceService.completeRun(anyString(), any(Instant.class)))
                .thenAnswer(inv -> ScenarioRun.builder().scenarioRunId(inv.getArgument(0)).status(ScenarioRunStatus.COMPLETED).agent(agent).committedTransactionCount(12).build());

        ScenarioRunDetailResponse response = scenarioService.runScenario(agentCode, ScenarioType.CASH_OUT_VELOCITY_SPIKE);

        assertEquals(ScenarioRunStatus.COMPLETED, response.status());
        assertEquals(12, response.committedTransactionCount());
        verify(transactionService, times(12)).createScenarioTransaction(any());
    }

    @Test
    void providerFeedDelayDoesNotInjectTransactions() {
        String agentCode = "AGT-001";
        Agent agent = Agent.builder().agentCode(agentCode).build();
        when(agentRepository.findByAgentCode(agentCode)).thenReturn(Optional.of(agent));

        when(scenarioRunPersistenceService.completeRun(anyString(), any(Instant.class)))
                .thenAnswer(inv -> ScenarioRun.builder().scenarioRunId(inv.getArgument(0)).status(ScenarioRunStatus.COMPLETED).agent(agent).build());

        scenarioService.runScenario(agentCode, ScenarioType.PROVIDER_FEED_DELAY);

        verify(transactionService, never()).createScenarioTransaction(any());
        verify(providerDataHealthService).updateSyntheticHealth(
                agentCode, "BKASH", ProviderDataHealthStatus.DELAYED, 30, null
        );
    }

    @Test
    void conflictingBalanceDataIncludesDescription() {
        String agentCode = "AGT-001";
        Agent agent = Agent.builder().agentCode(agentCode).build();
        when(agentRepository.findByAgentCode(agentCode)).thenReturn(Optional.of(agent));

        when(scenarioRunPersistenceService.completeRun(anyString(), any(Instant.class)))
                .thenAnswer(inv -> ScenarioRun.builder().scenarioRunId(inv.getArgument(0)).status(ScenarioRunStatus.COMPLETED).agent(agent).build());

        scenarioService.runScenario(agentCode, ScenarioType.CONFLICTING_BALANCE_DATA);

        verify(transactionService, never()).createScenarioTransaction(any());
        verify(providerDataHealthService).updateSyntheticHealth(
                eq(agentCode), eq("ROCKET"), eq(ProviderDataHealthStatus.CONFLICTING), eq(0), anyString()
        );
    }

    @Test
    void failedRunCapturesErrorAndCommittedCount() {
        String agentCode = "AGT-001";
        Agent agent = Agent.builder().agentCode(agentCode).build();
        when(agentRepository.findByAgentCode(agentCode)).thenReturn(Optional.of(agent));

        // Let it fail on the 3rd transaction
        doThrow(new RuntimeException("Simulated Failure"))
                .when(transactionService).createScenarioTransaction(any());

        when(scenarioRunPersistenceService.failRun(anyString(), anyInt(), anyString(), any(Instant.class)))
                .thenAnswer(inv -> ScenarioRun.builder()
                        .scenarioRunId(inv.getArgument(0))
                        .status(ScenarioRunStatus.FAILED)
                        .committedTransactionCount(inv.getArgument(1))
                        .failureMessage(inv.getArgument(2))
                        .agent(agent)
                        .build());

        ScenarioRunDetailResponse response = scenarioService.runScenario(agentCode, ScenarioType.REPEATED_AMOUNT_CLUSTER);

        assertEquals(ScenarioRunStatus.FAILED, response.status());
        assertEquals(0, response.committedTransactionCount());
        assertNotNull(response.failureMessage());
        assertEquals("Simulated Failure", response.failureMessage());
    }
}
