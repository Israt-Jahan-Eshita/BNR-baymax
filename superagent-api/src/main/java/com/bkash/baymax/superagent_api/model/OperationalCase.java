package com.bkash.baymax.superagent_api.model;

import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CasePriority;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "operational_cases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_case_code",
                        columnNames = "case_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_case_agent_opened_at",
                        columnList = "agent_id, opened_at"
                ),
                @Index(
                        name = "idx_case_provider_opened_at",
                        columnList = "provider_id, opened_at"
                ),
                @Index(
                        name = "idx_case_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_case_priority",
                        columnList = "priority"
                ),
                @Index(
                        name = "idx_case_creation_source",
                        columnList = "creation_source"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "case_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String caseCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "creation_source",
            nullable = false,
            length = 30
    )
    private CaseCreationSource creationSource;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_alert_id",
            unique = true
    )
    private Alert sourceAlert;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false,
            length = 20
    )
    private CasePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private CaseStatus status;

    @Column(
            name = "title",
            nullable = false,
            length = 250
    )
    private String title;

    @Column(
            name = "description",
            nullable = false,
            length = 2000
    )
    private String description;

    @Column(
            name = "recommended_next_step",
            nullable = false,
            length = 1500
    )
    private String recommendedNextStep;

    @Column(
            name = "opened_at",
            nullable = false
    )
    private Instant openedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        if (status == null) {
            status = CaseStatus.OPEN;
        }

        if (openedAt == null) {
            openedAt = Instant.now();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        validateEntity();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
        validateEntity();
    }

    private void validateEntity() {
        if (caseCode == null || caseCode.isBlank()) {
            throw new IllegalStateException("Case code is required");
        }

        if (creationSource == null) {
            throw new IllegalStateException("Creation source is required");
        }

        if (agent == null) {
            throw new IllegalStateException("Agent is required");
        }

        if (priority == null) {
            throw new IllegalStateException("Priority is required");
        }

        if (status == null) {
            throw new IllegalStateException("Status is required");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalStateException("Title is required");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalStateException("Description is required");
        }

        if (recommendedNextStep == null || recommendedNextStep.isBlank()) {
            throw new IllegalStateException("Recommended next step is required");
        }

        if (openedAt == null) {
            throw new IllegalStateException("Opened at is required");
        }
    }
}
