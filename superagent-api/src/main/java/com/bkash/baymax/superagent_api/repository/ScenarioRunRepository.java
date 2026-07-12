package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.ScenarioRun;
import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScenarioRunRepository extends JpaRepository<ScenarioRun, Long> {

    Optional<ScenarioRun> findByScenarioRunId(String scenarioRunId);

    Page<ScenarioRun> findAllByAgentAgentCode(String agentCode, Pageable pageable);

    @Query("SELECT r FROM ScenarioRun r " +
           "WHERE r.agent.agentCode = :agentCode " +
           "AND (:scenarioType IS NULL OR r.scenarioType = :scenarioType) " +
           "AND (:status IS NULL OR r.status = :status)")
    Page<ScenarioRun> findRuns(
            @Param("agentCode") String agentCode,
            @Param("scenarioType") ScenarioType scenarioType,
            @Param("status") ScenarioRunStatus status,
            Pageable pageable
    );

    java.util.List<ScenarioRun> findAllByStatus(ScenarioRunStatus status);
}
