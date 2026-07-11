package com.bkash.baymax.superagent_api.model;

import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "provider_data_health",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_data_health_agent_provider",
                        columnNames = {
                                "agent_id",
                                "provider_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_data_health_agent",
                        columnList = "agent_id"
                ),
                @Index(
                        name = "idx_data_health_provider",
                        columnList = "provider_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDataHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "provider_id",
            nullable = false
    )
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private ProviderDataHealthStatus status;

    @Column(
            name = "last_successful_update_at"
    )
    private Instant lastSuccessfulUpdateAt;

    @Column(
            name = "delay_minutes",
            nullable = false
    )
    @Builder.Default
    private int delayMinutes = 0;

    @Column(
            name = "conflict_description",
            length = 500
    )
    private String conflictDescription;

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

    @PrePersist
    void onCreate() {
        validateDataHealth();

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        validateDataHealth();
        updatedAt = Instant.now();
    }

    private void validateDataHealth() {
        if (status == null) {
            throw new IllegalStateException(
                    "Provider data health status is required"
            );
        }

        if (delayMinutes < 0) {
            throw new IllegalStateException(
                    "Provider data delay cannot be negative"
            );
        }

        if (
                status == ProviderDataHealthStatus.CONFLICTING
                && (
                        conflictDescription == null
                        || conflictDescription.isBlank()
                )
        ) {
            throw new IllegalStateException(
                    "Conflicting provider data requires a conflict description"
            );
        }
    }
}
