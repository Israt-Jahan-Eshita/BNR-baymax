package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.model.ScenarioRun;
import com.bkash.baymax.superagent_api.repository.ScenarioRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScenarioRunPersistenceService {

    private final ScenarioRunRepository scenarioRunRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScenarioRun createRun(ScenarioRun scenarioRun) {
        return scenarioRunRepository.save(scenarioRun);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScenarioRun updateProgress(String scenarioRunId, int committedTransactionCount) {
        ScenarioRun run = scenarioRunRepository.findByScenarioRunId(scenarioRunId)
                .orElseThrow(() -> new IllegalStateException("ScenarioRun not found: " + scenarioRunId));
        run.setCommittedTransactionCount(committedTransactionCount);
        return scenarioRunRepository.save(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScenarioRun completeRun(String scenarioRunId, java.time.Instant completedAt) {
        ScenarioRun run = scenarioRunRepository.findByScenarioRunId(scenarioRunId)
                .orElseThrow(() -> new IllegalStateException("ScenarioRun not found: " + scenarioRunId));
        run.setStatus(com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus.COMPLETED);
        run.setCompletedAt(completedAt);
        return scenarioRunRepository.save(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScenarioRun failRun(String scenarioRunId, int committedTransactionCount, String failureMessage, java.time.Instant completedAt) {
        ScenarioRun run = scenarioRunRepository.findByScenarioRunId(scenarioRunId)
                .orElseThrow(() -> new IllegalStateException("ScenarioRun not found: " + scenarioRunId));
        run.setStatus(com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus.FAILED);
        run.setCommittedTransactionCount(committedTransactionCount);
        run.setFailureMessage(failureMessage);
        run.setCompletedAt(completedAt);
        return scenarioRunRepository.save(run);
    }
}
