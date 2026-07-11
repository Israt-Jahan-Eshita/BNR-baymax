package com.bkash.baymax.superagent_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "physical_cash_positions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_physical_cash_position_agent",
                        columnNames = "agent_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_physical_cash_position_agent",
                        columnList = "agent_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalCashPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "agent_id",
            nullable = false,
            unique = true
    )
    private Agent agent;

    @Column(
            name = "cash_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

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
        validateCashBalance();

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        validateCashBalance();
        updatedAt = Instant.now();
    }

    private void validateCashBalance() {
        if (cashBalance == null) {
            cashBalance = BigDecimal.ZERO;
        }

        if (cashBalance.signum() < 0) {
            throw new IllegalStateException(
                    "Physical cash balance cannot be negative"
            );
        }
    }
}
