package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.internal.ScenarioTransactionCommand;
import com.bkash.baymax.superagent_api.dto.response.ScenarioDefinitionResponse;
import com.bkash.baymax.superagent_api.dto.response.ScenarioRunDetailResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.ScenarioRun;
import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ScenarioRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRunRepository scenarioRunRepository;
    private final AgentRepository agentRepository;
    private final TransactionService transactionService;
    private final ProviderDataHealthService providerDataHealthService;
    private final ScenarioDefinitionService scenarioDefinitionService;
    private final ScenarioRunPersistenceService scenarioRunPersistenceService;
    private final Clock clock;

    public ScenarioRunDetailResponse runScenario(String requestedAgentCode, ScenarioType scenarioType) {
        String agentCode = requestedAgentCode.trim().toUpperCase(Locale.ROOT);

        Agent agent = agentRepository.findByAgentCode(agentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentCode));

        String scenarioRunId = "SCN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        Instant startedAt = Instant.now(clock);

        String summary = scenarioDefinitionService.getDefinitions().stream()
                .filter(d -> d.scenarioType() == scenarioType)
                .map(ScenarioDefinitionResponse::description)
                .findFirst().orElse("Scenario execution");

        ScenarioRun initialRun = ScenarioRun.builder()
                .scenarioRunId(scenarioRunId)
                .scenarioType(scenarioType)
                .agent(agent)
                .status(ScenarioRunStatus.RUNNING)
                .committedTransactionCount(0)
                .summary(summary)
                .startedAt(startedAt)
                .build();

        scenarioRunPersistenceService.createRun(initialRun);

        int committedCount = 0;
        try {
            if (scenarioType == ScenarioType.PROVIDER_FEED_DELAY) {
                providerDataHealthService.updateSyntheticHealth(
                        agentCode, "BKASH", ProviderDataHealthStatus.DELAYED, 30, null
                );
            } else if (scenarioType == ScenarioType.CONFLICTING_BALANCE_DATA) {
                providerDataHealthService.updateSyntheticHealth(
                        agentCode, "ROCKET", ProviderDataHealthStatus.CONFLICTING, 0,
                        "Provider feed balance snapshot conflicts with the latest locally derived synthetic transaction position."
                );
            } else {
                List<ScenarioTransactionCommand> commands = scenarioDefinitionService.buildCommands(
                        scenarioType, agentCode, scenarioRunId
                );
                
                for (ScenarioTransactionCommand command : commands) {
                    transactionService.createScenarioTransaction(command);
                    committedCount++;
                    scenarioRunPersistenceService.updateProgress(scenarioRunId, committedCount);
                }
            }

            ScenarioRun completedRun = scenarioRunPersistenceService.completeRun(scenarioRunId, Instant.now(clock));
            return toDetailResponse(completedRun);

        } catch (RuntimeException ex) {
            ScenarioRun failedRun = scenarioRunPersistenceService.failRun(
                    scenarioRunId, committedCount, ex.getMessage(), Instant.now(clock)
            );
            return toDetailResponse(failedRun);
        }
    }

    private ScenarioRunDetailResponse toDetailResponse(ScenarioRun run) {
        return new ScenarioRunDetailResponse(
                run.getScenarioRunId(),
                run.getScenarioType(),
                run.getAgent().getAgentCode(),
                run.getAgent().getDisplayName(),
                run.getStatus(),
                run.getCommittedTransactionCount(),
                run.getSummary(),
                run.getFailureMessage(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }
}
