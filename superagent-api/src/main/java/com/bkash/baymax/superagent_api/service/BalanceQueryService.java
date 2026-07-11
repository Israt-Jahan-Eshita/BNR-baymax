package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.AgentBalanceResponse;
import com.bkash.baymax.superagent_api.dto.response.ProviderBalanceResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import com.bkash.baymax.superagent_api.model.ProviderBalance;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.PhysicalCashPositionRepository;
import com.bkash.baymax.superagent_api.repository.ProviderBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BalanceQueryService {

    private final AgentRepository agentRepository;
    private final PhysicalCashPositionRepository physicalCashPositionRepository;
    private final ProviderBalanceRepository providerBalanceRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public AgentBalanceResponse getAgentBalances(
            String requestedAgentCode
    ) {
        String agentCode = normalizeCode(requestedAgentCode);

        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException(
                    "Agent not found: " + agentCode
            );
        }

        PhysicalCashPosition physicalCashPosition =
                physicalCashPositionRepository
                        .findByAgentAgentCode(agentCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Physical cash position not found for agent "
                                                + agentCode
                                )
                        );

        List<ProviderBalanceResponse> providerBalances =
                providerBalanceRepository
                        .findAllByAgentAgentCode(agentCode)
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        providerBalance ->
                                                providerBalance
                                                        .getProvider()
                                                        .getProviderCode()
                                )
                        )
                        .map(this::toProviderBalanceResponse)
                        .toList();

        return new AgentBalanceResponse(
                agentCode,
                physicalCashPosition.getCashBalance(),
                providerBalances,
                Instant.now(clock)
        );
    }

    private ProviderBalanceResponse toProviderBalanceResponse(
            ProviderBalance providerBalance
    ) {
        return new ProviderBalanceResponse(
                providerBalance.getProvider().getProviderCode(),
                providerBalance.getProvider().getDisplayName(),
                providerBalance.getEMoneyBalance(),
                providerBalance.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Agent code is required"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }
}
