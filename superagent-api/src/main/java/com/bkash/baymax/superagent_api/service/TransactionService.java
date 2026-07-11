package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.internal.ScenarioTransactionCommand;
import com.bkash.baymax.superagent_api.dto.request.CreateSimulatedTransactionRequest;
import com.bkash.baymax.superagent_api.dto.response.SimulatedTransactionResponse;
import com.bkash.baymax.superagent_api.event.TransactionPersistedEvent;
import com.bkash.baymax.superagent_api.exception.InactiveResourceException;
import com.bkash.baymax.superagent_api.exception.InsufficientLiquidityException;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.ProviderBalance;
import com.bkash.baymax.superagent_api.model.SimulatedTransaction;
import com.bkash.baymax.superagent_api.model.enums.TransactionSource;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.PhysicalCashPositionRepository;
import com.bkash.baymax.superagent_api.repository.ProviderBalanceRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import com.bkash.baymax.superagent_api.repository.SimulatedTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AgentRepository agentRepository;
    private final ProviderRepository providerRepository;
    private final PhysicalCashPositionRepository
            physicalCashPositionRepository;
    private final ProviderBalanceRepository
            providerBalanceRepository;
    private final SimulatedTransactionRepository
            simulatedTransactionRepository;
    private final Clock clock;
    private final ApplicationEventPublisher
            applicationEventPublisher;

    @Transactional
    public SimulatedTransactionResponse createManualTransaction(
            CreateSimulatedTransactionRequest request
    ) {
        return processTransaction(
                request.agentCode(),
                request.providerCode(),
                request.type(),
                request.amount(),
                request.syntheticAccountId(),
                TransactionSource.MANUAL_SIMULATION,
                null
        );
    }

    @Transactional
    public SimulatedTransactionResponse createScenarioTransaction(
            ScenarioTransactionCommand command
    ) {
        return processTransaction(
                command.agentCode(),
                command.providerCode(),
                command.type(),
                command.amount(),
                command.syntheticAccountId(),
                TransactionSource.SCENARIO,
                command.scenarioRunId()
        );
    }

    private SimulatedTransactionResponse processTransaction(
            String rawAgentCode,
            String rawProviderCode,
            TransactionType type,
            BigDecimal amount,
            String rawSyntheticAccountId,
            TransactionSource source,
            String scenarioRunId
    ) {
        String agentCode =
                normalizeCode(
                        rawAgentCode,
                        "Agent code"
                );

        String providerCode =
                normalizeCode(
                        rawProviderCode,
                        "Provider code"
                );

        Agent agent = agentRepository
                .findByAgentCode(agentCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agent not found: "
                                        + agentCode
                        )
                );

        if (!agent.isActive()) {
            throw new InactiveResourceException(
                    "Agent is inactive: "
                            + agentCode
            );
        }

        Provider provider = providerRepository
                .findByProviderCode(providerCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Provider not found: "
                                        + providerCode
                        )
                );

        if (!provider.isActive()) {
            throw new InactiveResourceException(
                    "Provider is inactive: "
                            + providerCode
            );
        }

        PhysicalCashPosition cashPosition =
                physicalCashPositionRepository
                        .findByAgentAgentCode(agentCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Physical cash position not found for agent "
                                                + agentCode
                                )
                        );

        ProviderBalance providerBalance =
                providerBalanceRepository
                        .findByAgentAgentCodeAndProviderProviderCode(
                                agentCode,
                                providerCode
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Provider balance not found for agent "
                                                + agentCode
                                                + " and provider "
                                                + providerCode
                                )
                        );

        applyLiquidityMovement(
                type,
                amount,
                cashPosition,
                providerBalance,
                providerCode
        );

        Instant occurredAt =
                Instant.now(clock);

        SimulatedTransaction transaction =
                SimulatedTransaction.builder()
                        .transactionReference(
                                generateTransactionReference()
                        )
                        .agent(agent)
                        .provider(provider)
                        .transactionType(type)
                        .amount(amount)
                        .occurredAt(occurredAt)
                        .syntheticAccountId(
                                rawSyntheticAccountId
                                        .trim()
                        )
                        .source(source)
                        .scenarioRunId(scenarioRunId)
                        .build();

        physicalCashPositionRepository.save(
                cashPosition
        );

        providerBalanceRepository.save(
                providerBalance
        );

        SimulatedTransaction savedTransaction =
                simulatedTransactionRepository.save(
                        transaction
                );

        applicationEventPublisher.publishEvent(
                new TransactionPersistedEvent(
                        agentCode,
                        savedTransaction
                                .getTransactionReference(),
                        savedTransaction.getOccurredAt()
                )
        );

        return new SimulatedTransactionResponse(
                savedTransaction
                        .getTransactionReference(),
                agentCode,
                providerCode,
                savedTransaction
                        .getTransactionType(),
                savedTransaction.getAmount(),
                savedTransaction
                        .getSyntheticAccountId(),
                savedTransaction.getOccurredAt(),
                savedTransaction.getSource(),
                cashPosition.getCashBalance(),
                providerBalance.getEMoneyBalance()
        );
    }

    private void applyLiquidityMovement(
            TransactionType transactionType,
            BigDecimal amount,
            PhysicalCashPosition cashPosition,
            ProviderBalance providerBalance,
            String providerCode
    ) {
        switch (transactionType) {
            case CASH_OUT ->
                    applyCashOut(
                            amount,
                            cashPosition,
                            providerBalance
                    );

            case CASH_IN ->
                    applyCashIn(
                            amount,
                            cashPosition,
                            providerBalance,
                            providerCode
                    );
        }
    }

    private void applyCashOut(
            BigDecimal amount,
            PhysicalCashPosition cashPosition,
            ProviderBalance providerBalance
    ) {
        if (
                cashPosition
                        .getCashBalance()
                        .compareTo(amount) < 0
        ) {
            throw new InsufficientLiquidityException(
                    "Insufficient physical cash for CASH_OUT transaction"
            );
        }

        cashPosition.setCashBalance(
                cashPosition
                        .getCashBalance()
                        .subtract(amount)
        );

        providerBalance.setEMoneyBalance(
                providerBalance
                        .getEMoneyBalance()
                        .add(amount)
        );
    }

    private void applyCashIn(
            BigDecimal amount,
            PhysicalCashPosition cashPosition,
            ProviderBalance providerBalance,
            String providerCode
    ) {
        if (
                providerBalance
                        .getEMoneyBalance()
                        .compareTo(amount) < 0
        ) {
            throw new InsufficientLiquidityException(
                    "Insufficient "
                            + providerCode
                            + " e-money for CASH_IN transaction"
            );
        }

        cashPosition.setCashBalance(
                cashPosition
                        .getCashBalance()
                        .add(amount)
        );

        providerBalance.setEMoneyBalance(
                providerBalance
                        .getEMoneyBalance()
                        .subtract(amount)
        );
    }

    private String generateTransactionReference() {
        return "SIM-"
                + UUID.randomUUID()
                        .toString()
                        .toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(
            String code,
            String fieldName
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
