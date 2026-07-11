package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SimulatedTransactionRepository
        extends JpaRepository<SimulatedTransaction, Long> {

    Optional<SimulatedTransaction> findByTransactionReference(
            String transactionReference
    );

    boolean existsByTransactionReference(
            String transactionReference
    );

    Page<SimulatedTransaction> findAllByAgentAgentCode(
            String agentCode,
            Pageable pageable
    );

    Page<SimulatedTransaction>
    findAllByAgentAgentCodeAndProviderProviderCode(
            String agentCode,
            String providerCode,
            Pageable pageable
    );

    @Query("""
            SELECT transaction
            FROM SimulatedTransaction transaction
            WHERE transaction.agent.agentCode = :agentCode
              AND (
                    :providerCode IS NULL
                    OR transaction.provider.providerCode = :providerCode
                  )
              AND (
                    :transactionType IS NULL
                    OR transaction.transactionType = :transactionType
                  )
            """)
    Page<SimulatedTransaction> findTransactions(
            @Param("agentCode")
            String agentCode,

            @Param("providerCode")
            String providerCode,

            @Param("transactionType")
            TransactionType transactionType,

            Pageable pageable
    );

    List<SimulatedTransaction>
    findAllByAgentAgentCodeAndOccurredAtBetweenOrderByOccurredAtAsc(
            String agentCode,
            Instant from,
            Instant to
    );

    List<SimulatedTransaction>
    findAllByAgentAgentCodeAndProviderProviderCodeAndOccurredAtBetweenOrderByOccurredAtAsc(
            String agentCode,
            String providerCode,
            Instant from,
            Instant to
    );

    List<SimulatedTransaction>
    findAllByAgentAgentCodeAndTransactionTypeAndOccurredAtBetweenOrderByOccurredAtAsc(
            String agentCode,
            TransactionType transactionType,
            Instant from,
            Instant to
    );
}
