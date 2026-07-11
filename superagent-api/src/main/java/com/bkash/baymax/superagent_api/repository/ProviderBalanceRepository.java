package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.ProviderBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderBalanceRepository
        extends JpaRepository<ProviderBalance, Long> {

    Optional<ProviderBalance> findByAgentAgentCodeAndProviderProviderCode(
            String agentCode,
            String providerCode
    );

    List<ProviderBalance> findAllByAgentAgentCode(
            String agentCode
    );

    boolean existsByAgentIdAndProviderId(
            Long agentId,
            Long providerId
    );
}
