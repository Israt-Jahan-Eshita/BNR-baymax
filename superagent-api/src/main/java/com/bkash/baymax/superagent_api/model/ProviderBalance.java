package com.bkash.baymax.superagent_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "provider_balances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_balance_agent_provider",
                        columnNames = {
                                "agent_id",
                                "provider_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_provider_balance_agent",
                        columnList = "agent_id"
                ),
                @Index(
                        name = "idx_provider_balance_provider",
                        columnList = "provider_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderBalance {

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

    @Column(
            name = "e_money_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal eMoneyBalance = BigDecimal.ZERO;

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
        Instant now = Instant.now();

        validateBalance();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        validateBalance();
        updatedAt = Instant.now();
    }

    private void validateBalance() {
        if (eMoneyBalance == null) {
            eMoneyBalance = BigDecimal.ZERO;
        }

        if (eMoneyBalance.signum() < 0) {
            throw new IllegalStateException(
                    "Provider e-money balance cannot be negative"
            );
        }
    }
}
