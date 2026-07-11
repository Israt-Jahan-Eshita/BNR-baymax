package com.bkash.baymax.superagent_api.model;

import com.bkash.baymax.superagent_api.model.enums.CaseAuditAction;
import com.bkash.baymax.superagent_api.model.enums.CaseAuditActorType;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "case_audit_events",
        indexes = {
                @Index(
                        name = "idx_case_audit_case_occurred",
                        columnList = "case_id, occurred_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "case_id",
            nullable = false
    )
    private OperationalCase operationalCase;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 50
    )
    private CaseAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "actor_type",
            nullable = false,
            length = 30
    )
    private CaseAuditActorType actorType;

    @Column(
            name = "actor_reference",
            nullable = false,
            length = 150
    )
    private String actorReference;

    @Column(
            name = "details",
            nullable = false,
            length = 1500
    )
    private String details;

    @Column(
            name = "occurred_at",
            nullable = false
    )
    private Instant occurredAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        validateEntity();
    }

    private void validateEntity() {
        if (operationalCase == null) {
            throw new IllegalStateException("Operational case is required");
        }

        if (action == null) {
            throw new IllegalStateException("Action is required");
        }

        if (actorType == null) {
            throw new IllegalStateException("Actor type is required");
        }

        if (actorReference == null || actorReference.isBlank()) {
            throw new IllegalStateException("Actor reference is required");
        }

        if (details == null || details.isBlank()) {
            throw new IllegalStateException("Details are required");
        }
        
        if (occurredAt == null) {
            throw new IllegalStateException("Occurred at is required");
        }
    }
}
