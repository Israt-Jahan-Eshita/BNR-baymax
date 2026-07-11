package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.request.CreateSimulatedTransactionRequest;
import com.bkash.baymax.superagent_api.dto.response.SimulatedTransactionResponse;
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
    private final ProviderBalanceRepository providerBalanceRepository;
    private final PhysicalCashPositionRepository physicalCashPositionRepository;
    private final SimulatedTransactionRepository simulatedTransactionRepository;
    private final Clock clock;

    @Transactional
    public SimulatedTransactionResponse createManualTransaction(
            CreateSimulatedTransactionRequest request
    ) {
        String agentCode = normalizeCode(request.agentCode());
        String providerCode = normalizeCode(request.providerCode());

        Agent agent = findActiveAgent(agentCode);
        Provider provider = findActiveProvider(providerCode);

        PhysicalCashPosition cashPosition =
                physicalCashPositionRepository
                        .findByAgentAgentCode(agentCode)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Physical cash position not found for agent "
                                        + agentCode
                        ));

        ProviderBalance providerBalance =
                providerBalanceRepository
                        .findByAgentAgentCodeAndProviderProviderCode(
                                agentCode,
                                providerCode
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Provider balance not found for agent "
                                        + agentCode
                                        + " and provider "
                                        + providerCode
                        ));

        BigDecimal amount = request.amount();

        applyBalanceMovement(
                request.type(),
                amount,
                cashPosition,
                providerBalance
        );

        SimulatedTransaction transaction =
                SimulatedTransaction.builder()
                        .transactionReference(
                                generateTransactionReference()
                        )
                        .agent(agent)
                        .provider(provider)
                        .transactionType(request.type())
                        .amount(amount)
                        .occurredAt(Instant.now(clock))
                        .syntheticAccountId(
                                request.syntheticAccountId().trim()
                        )
                        .source(TransactionSource.MANUAL_SIMULATION)
                        .build();

        physicalCashPositionRepository.save(cashPosition);
        providerBalanceRepository.save(providerBalance);

        SimulatedTransaction savedTransaction =
                simulatedTransactionRepository.save(transaction);

        return toResponse(
                savedTransaction,
                cashPosition,
                providerBalance
        );
    }

    private Agent findActiveAgent(String agentCode) {
        Agent agent = agentRepository.findByAgentCode(agentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agent not found: " + agentCode
                ));

        if (!agent.isActive()) {
            throw new InactiveResourceException(
                    "Agent is inactive: " + agentCode
            );
        }

        return agent;
    }

    private Provider findActiveProvider(String providerCode) {
        Provider provider =
                providerRepository.findByProviderCode(providerCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Provider not found: "
                                                + providerCode
                                )
                        );

        if (!provider.isActive()) {
            throw new InactiveResourceException(
                    "Provider is inactive: " + providerCode
            );
        }

        return provider;
    }

    private void applyBalanceMovement(
            TransactionType transactionType,
            BigDecimal amount,
            PhysicalCashPosition cashPosition,
            ProviderBalance providerBalance
    ) {
        switch (transactionType) {
            case CASH_OUT -> applyCashOut(
                    amount,
                    cashPosition,
                    providerBalance
            );

            case CASH_IN -> applyCashIn(
                    amount,
                    cashPosition,
                    providerBalance
            );
        }
    }

    private void applyCashOut(
            BigDecimal amount,
            PhysicalCashPosition cashPosition,
            ProviderBalance providerBalance
    ) {
        if (cashPosition.getCashBalance().compareTo(amount) < 0) {
            throw new InsufficientLiquidityException(
                    "Insufficient physical cash for CASH_OUT. "
                            + "Available physical cash: "
                            + cashPosition.getCashBalance()
            );
        }

        cashPosition.setCashBalance(
                cashPosition.getCashBalance().subtract(amount)
        );

        providerBalance.setEMoneyBalance(
                providerBalance.getEMoneyBalance().add(amount)
        );
    }

    private void applyCashIn(
            BigDecimal amount,
            PhysicalCashPosition cashPosition,
            ProviderBalance providerBalance
    ) {
        if (providerBalance.getEMoneyBalance().compareTo(amount) < 0) {
            throw new InsufficientLiquidityException(
                    "Insufficient provider e-money for CASH_IN. "
                            + "Available provider e-money: "
                            + providerBalance.getEMoneyBalance()
            );
        }

        cashPosition.setCashBalance(
                cashPosition.getCashBalance().add(amount)
        );

        providerBalance.setEMoneyBalance(
                providerBalance.getEMoneyBalance().subtract(amount)
        );
    }

    private SimulatedTransactionResponse toResponse(
            SimulatedTransaction transaction,
            PhysicalCashPosition cashPosition,
            ProviderBalance providerBalance
    ) {
        return new SimulatedTransactionResponse(
                transaction.getTransactionReference(),
                transaction.getAgent().getAgentCode(),
                transaction.getProvider().getProviderCode(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getSyntheticAccountId(),
                transaction.getOccurredAt(),
                transaction.getSource(),
                cashPosition.getCashBalance(),
                providerBalance.getEMoneyBalance()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String generateTransactionReference() {
        return "SIM-"
                + UUID.randomUUID()
                .toString()
                .toUpperCase(Locale.ROOT);
    }
}
