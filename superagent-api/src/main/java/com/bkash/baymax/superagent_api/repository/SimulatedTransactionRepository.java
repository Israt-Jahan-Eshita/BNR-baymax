package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
