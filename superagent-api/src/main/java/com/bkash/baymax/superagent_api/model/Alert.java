package com.bkash.baymax.superagent_api.model;

import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import com.bkash.baymax.superagent_api.model.enums.SignalConfidence;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "alerts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_alert_code",
                        columnNames = "alert_code"
                ),
                @UniqueConstraint(
                        name = "uk_alert_fingerprint",
                        columnNames = "alert_fingerprint"
                )
        },
        indexes = {
                @Index(
                        name = "idx_alert_agent_detected_at",
                        columnList = "agent_id, detected_at"
                ),
                @Index(
                        name = "idx_alert_provider_detected_at",
                        columnList = "provider_id, detected_at"
                ),
                @Index(
                        name = "idx_alert_type",
                        columnList = "alert_type"
                ),
                @Index(
                        name = "idx_alert_severity",
                        columnList = "severity"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "alert_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String alertCode;

    @Column(
            name = "alert_fingerprint",
            nullable = false,
            unique = true,
            length = 64
    )
    private String alertFingerprint;

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
            name = "alert_type",
            nullable = false,
            length = 60
    )
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "severity",
            nullable = false,
            length = 20
    )
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "confidence",
            nullable = false,
            length = 20
    )
    private SignalConfidence confidence;

    @Column(
            name = "confidence_score",
            nullable = false
    )
    private int confidenceScore;

    @Column(
            name = "title",
            nullable = false,
            length = 250
    )
    private String title;

    @Column(
            name = "summary",
            nullable = false,
            length = 1000
    )
    private String summary;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "alert_evidence",
            joinColumns = @JoinColumn(name = "alert_id")
    )
    @Column(
            name = "evidence",
            nullable = false,
            length = 1000
    )
    @Builder.Default
    private List<String> evidence = new ArrayList<>();

    @Column(
            name = "possible_normal_explanation",
            nullable = false,
            length = 1000
    )
    private String possibleNormalExplanation;

    @Column(
            name = "uncertainty",
            nullable = false,
            length = 1000
    )
    private String uncertainty;

    @Column(
            name = "safe_next_step",
            nullable = false,
            length = 1000
    )
    private String safeNextStep;

    @Column(
            name = "window_start",
            nullable = false
    )
    private Instant windowStart;

    @Column(
            name = "window_end",
            nullable = false
    )
    private Instant windowEnd;

    @Column(
            name = "detected_at",
            nullable = false
    )
    private Instant detectedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        validateAlert();

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private void validateAlert() {
        if (alertCode == null || alertCode.isBlank()) {
            throw new IllegalStateException(
                    "Alert code is required"
            );
        }

        if (
                alertFingerprint == null
                || alertFingerprint.isBlank()
        ) {
            throw new IllegalStateException(
                    "Alert fingerprint is required"
            );
        }

        if (alertType == null) {
            throw new IllegalStateException(
                    "Alert type is required"
            );
        }

        if (severity == null) {
            throw new IllegalStateException(
                    "Alert severity is required"
            );
        }

        if (confidence == null) {
            throw new IllegalStateException(
                    "Signal confidence is required"
            );
        }

        if (
                confidenceScore < 0
                || confidenceScore > 100
        ) {
            throw new IllegalStateException(
                    "Signal confidence score must be between 0 and 100"
            );
        }

        if (windowStart == null || windowEnd == null) {
            throw new IllegalStateException(
                    "Alert analysis window is required"
            );
        }

        if (windowEnd.isBefore(windowStart)) {
            throw new IllegalStateException(
                    "Alert window end cannot be before window start"
            );
        }

        if (detectedAt == null) {
            throw new IllegalStateException(
                    "Alert detection time is required"
            );
        }
    }
}
