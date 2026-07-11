package com.bkash.baymax.superagent_api.model;

import com.bkash.baymax.superagent_api.model.enums.TransactionSource;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "simulated_transactions",
        indexes = {
                @Index(
                        name = "idx_simulated_transaction_reference",
                        columnList = "transaction_reference",
                        unique = true
                ),
                @Index(
                        name = "idx_simulated_transaction_agent_time",
                        columnList = "agent_id, occurred_at"
                ),
                @Index(
                        name = "idx_simulated_transaction_provider_time",
                        columnList = "provider_id, occurred_at"
                ),
                @Index(
                        name = "idx_simulated_transaction_scenario_run",
                        columnList = "scenario_run_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "transaction_reference",
            nullable = false,
            unique = true,
            length = 100
    )
    private String transactionReference;

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
            name = "transaction_type",
            nullable = false,
            length = 30
    )
    private TransactionType transactionType;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "occurred_at",
            nullable = false
    )
    private Instant occurredAt;

    @Column(
            name = "synthetic_account_id",
            length = 100
    )
    private String syntheticAccountId;

    @Column(
            name = "scenario_run_id",
            length = 100
    )
    private String scenarioRunId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "source",
            nullable = false,
            length = 40
    )
    private TransactionSource source;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        validateTransaction();

        if (occurredAt == null) {
            occurredAt = Instant.now();
        }

        createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        validateTransaction();
    }

    private void validateTransaction() {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException(
                    "Simulated transaction amount must be greater than zero"
            );
        }

        if (transactionType == null) {
            throw new IllegalStateException(
                    "Simulated transaction type is required"
            );
        }

        if (source == null) {
            throw new IllegalStateException(
                    "Simulated transaction source is required"
            );
        }
    }
}
