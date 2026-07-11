package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.response.ProviderDataHealthResponse;
import com.bkash.baymax.superagent_api.exception.ResourceNotFoundException;
import com.bkash.baymax.superagent_api.model.Agent;
import com.bkash.baymax.superagent_api.model.Provider;
import com.bkash.baymax.superagent_api.model.ProviderDataHealth;
import com.bkash.baymax.superagent_api.model.enums.ProviderDataHealthStatus;
import com.bkash.baymax.superagent_api.repository.AgentRepository;
import com.bkash.baymax.superagent_api.repository.ProviderDataHealthRepository;
import com.bkash.baymax.superagent_api.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProviderDataHealthService {

    private final AgentRepository agentRepository;
    private final ProviderRepository providerRepository;
    private final ProviderDataHealthRepository
            providerDataHealthRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<ProviderDataHealthResponse> getDataHealth(
            String requestedAgentCode
    ) {
        String agentCode = normalizeCode(
                requestedAgentCode,
                "Agent code"
        );

        if (!agentRepository.existsByAgentCode(agentCode)) {
            throw new ResourceNotFoundException(
                    "Agent not found: " + agentCode
            );
        }

        return providerDataHealthRepository
                .findAllByAgentAgentCode(agentCode)
                .stream()
                .sorted(
                        Comparator.comparing(
                                health ->
                                        health.getProvider()
                                                .getProviderCode()
                        )
                )
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProviderDataHealthResponse updateSyntheticHealth(
            String requestedAgentCode,
            String requestedProviderCode,
            ProviderDataHealthStatus status,
            int delayMinutes,
            String conflictDescription
    ) {
        String agentCode = normalizeCode(
                requestedAgentCode,
                "Agent code"
        );

        String providerCode = normalizeCode(
                requestedProviderCode,
                "Provider code"
        );

        Agent agent = agentRepository
                .findByAgentCode(agentCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agent not found: " + agentCode
                        )
                );

        Provider provider = providerRepository
                .findByProviderCode(providerCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Provider not found: "
                                        + providerCode
                        )
                );

        ProviderDataHealth health =
                providerDataHealthRepository
                        .findByAgentAgentCodeAndProviderProviderCode(
                                agentCode,
                                providerCode
                        )
                        .orElseGet(() ->
                                ProviderDataHealth.builder()
                                        .agent(agent)
                                        .provider(provider)
                                        .status(
                                                ProviderDataHealthStatus.LIVE
                                        )
                                        .lastSuccessfulUpdateAt(
                                                Instant.now(clock)
                                        )
                                        .delayMinutes(0)
                                        .build()
                        );

        applyStatus(
                health,
                status,
                delayMinutes,
                conflictDescription
        );

        ProviderDataHealth saved =
                providerDataHealthRepository.save(health);

        return toResponse(saved);
    }

    private void applyStatus(
            ProviderDataHealth health,
            ProviderDataHealthStatus status,
            int delayMinutes,
            String conflictDescription
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Provider data health status is required"
            );
        }

        Instant now = Instant.now(clock);

        switch (status) {
            case LIVE -> {
                health.setStatus(
                        ProviderDataHealthStatus.LIVE
                );

                health.setDelayMinutes(0);
                health.setConflictDescription(null);
                health.setLastSuccessfulUpdateAt(now);
            }

            case DELAYED -> {
                if (delayMinutes <= 0) {
                    throw new IllegalArgumentException(
                            "Delayed provider data requires a positive delay"
                    );
                }

                health.setStatus(
                        ProviderDataHealthStatus.DELAYED
                );

                health.setDelayMinutes(delayMinutes);
                health.setConflictDescription(null);

                health.setLastSuccessfulUpdateAt(
                        now.minus(
                                delayMinutes,
                                ChronoUnit.MINUTES
                        )
                );
            }

            case MISSING -> {
                health.setStatus(
                        ProviderDataHealthStatus.MISSING
                );

                health.setDelayMinutes(0);
                health.setConflictDescription(null);
                health.setLastSuccessfulUpdateAt(null);
            }

            case CONFLICTING -> {
                if (
                        conflictDescription == null
                        || conflictDescription.isBlank()
                ) {
                    throw new IllegalArgumentException(
                            "Conflicting data requires a description"
                    );
                }

                health.setStatus(
                        ProviderDataHealthStatus.CONFLICTING
                );

                health.setDelayMinutes(0);

                health.setConflictDescription(
                        conflictDescription.trim()
                );
            }
        }
    }

    private ProviderDataHealthResponse toResponse(
            ProviderDataHealth health
    ) {
        return new ProviderDataHealthResponse(
                health.getAgent().getAgentCode(),
                health.getProvider().getProviderCode(),
                health.getProvider().getDisplayName(),
                health.getStatus(),
                health.getLastSuccessfulUpdateAt(),
                health.getDelayMinutes(),
                health.getConflictDescription(),
                health.getUpdatedAt()
        );
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
