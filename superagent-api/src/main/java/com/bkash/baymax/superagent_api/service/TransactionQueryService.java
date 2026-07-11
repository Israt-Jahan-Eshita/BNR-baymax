package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.TransactionPageResponse;
import com.bkash.baymax.superagent_api.dto.response.TransactionSummaryResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import com.bkash.baymax.superagent_api.repository.SimulatedTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final AgentRepository agentRepository;
    private final ProviderRepository providerRepository;
    private final SimulatedTransactionRepository simulatedTransactionRepository;

    @Transactional(readOnly = true)
    public TransactionPageResponse getTransactions(
            String requestedAgentCode,
            String requestedProviderCode,
            TransactionType transactionType,
            Pageable pageable
    ) {
        String agentCode = normalizeRequiredCode(
                requestedAgentCode,
                "Agent code"
        );

        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException(
                    "Agent not found: " + agentCode
            );
        }

        String providerCode =
                normalizeOptionalCode(requestedProviderCode);

        if (
                providerCode != null
                && !providerRepository.existsByProviderCode(providerCode)
        ) {
            throw new ResourceNotFoundException(
                    "Provider not found: " + providerCode
            );
        }

        Page<SimulatedTransaction> transactionPage =
                simulatedTransactionRepository.findTransactions(
                        agentCode,
                        providerCode,
                        transactionType,
                        pageable
                );

        return new TransactionPageResponse(
                transactionPage
                        .getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isFirst(),
                transactionPage.isLast()
        );
    }

    private TransactionSummaryResponse toResponse(
            SimulatedTransaction transaction
    ) {
        return new TransactionSummaryResponse(
                transaction.getTransactionReference(),
                transaction.getAgent().getAgentCode(),
                transaction.getProvider().getProviderCode(),
                transaction.getProvider().getDisplayName(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getSyntheticAccountId(),
                transaction.getScenarioRunId(),
                transaction.getSource(),
                transaction.getOccurredAt()
        );
    }

    private String normalizeRequiredCode(
            String code,
            String fieldName
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }
}
