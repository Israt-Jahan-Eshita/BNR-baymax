package com.bkash.baymax.superagent_api.model;

import com.bkash.baymax.superagent_api.model.enums.ScenarioRunStatus;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "scenario_runs",
        indexes = {
                @Index(name = "idx_scenario_run_agent_started", columnList = "agent_id, started_at"),
                @Index(name = "idx_scenario_run_type", columnList = "scenario_type"),
                @Index(name = "idx_scenario_run_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScenarioRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenario_run_id", nullable = false, unique = true, length = 100)
    private String scenarioRunId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_type", nullable = false, length = 50)
    private ScenarioType scenarioType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ScenarioRunStatus status;

    @Column(name = "committed_transaction_count", nullable = false)
    private int committedTransactionCount;

    @Column(name = "summary", nullable = false, length = 1500)
    private String summary;

    @Column(name = "failure_message", length = 2000)
    private String failureMessage;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.committedTransactionCount < 0) {
            this.committedTransactionCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        if (this.committedTransactionCount < 0) {
            this.committedTransactionCount = 0;
        }
    }
}
