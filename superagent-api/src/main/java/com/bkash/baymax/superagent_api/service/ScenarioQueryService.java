package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.ScenarioDefinitionResponse;
import com.bkash.baymax.superagent_api.dto.response.ScenarioRunDetailResponse;
import com.bkash.baymax.superagent_api.dto.response.ScenarioRunPageResponse;
import com.bkash.baymax.superagent_api.dto.response.ScenarioRunSummaryResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.ScenarioRun;
import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ScenarioRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ScenarioQueryService {

    private final ScenarioDefinitionService scenarioDefinitionService;
    private final ScenarioRunRepository scenarioRunRepository;
    private final AgentRepository agentRepository;

    public List<ScenarioDefinitionResponse> getDefinitions() {
        return scenarioDefinitionService.getDefinitions();
    }

    @Transactional(readOnly = true)
    public ScenarioRunPageResponse getRuns(
            String requestedAgentCode,
            ScenarioType scenarioType,
            ScenarioRunStatus status,
            Pageable pageable
    ) {
        String agentCode = requestedAgentCode.trim().toUpperCase(Locale.ROOT);

        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException("Agent not found: " + agentCode);
        }

        Page<ScenarioRun> runPage = scenarioRunRepository.findRuns(
                agentCode, scenarioType, status, pageable
        );

        return new ScenarioRunPageResponse(
                runPage.getContent().stream().map(this::toSummaryResponse).toList(),
                runPage.getNumber(),
                runPage.getSize(),
                runPage.getTotalElements(),
                runPage.getTotalPages(),
                runPage.isFirst(),
                runPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ScenarioRunDetailResponse getRun(String requestedScenarioRunId) {
        String scenarioRunId = requestedScenarioRunId.trim().toUpperCase(Locale.ROOT);
        ScenarioRun run = scenarioRunRepository.findByScenarioRunId(scenarioRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario Run not found: " + scenarioRunId));
        return toDetailResponse(run);
    }

    private ScenarioRunSummaryResponse toSummaryResponse(ScenarioRun run) {
        return new ScenarioRunSummaryResponse(
                run.getScenarioRunId(),
                run.getScenarioType(),
                run.getAgent().getAgentCode(),
                run.getStatus(),
                run.getCommittedTransactionCount(),
                run.getSummary(),
                run.getStartedAt(),
                run.getCompletedAt()
        );
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
