package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.ProviderDataHealth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderDataHealthRepository
        extends JpaRepository<ProviderDataHealth, Long> {

    Optional<ProviderDataHealth>
    findByAgentAgentCodeAndProviderProviderCode(
            String agentCode,
            String providerCode
    );

    List<ProviderDataHealth> findAllByAgentAgentCode(
            String agentCode
    );

    boolean existsByAgentIdAndProviderId(
            Long agentId,
            Long providerId
    );
}
