package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.PhysicalCashPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhysicalCashPositionRepository
        extends JpaRepository<PhysicalCashPosition, Long> {

    Optional<PhysicalCashPosition> findByAgentAgentCode(
            String agentCode
    );

    boolean existsByAgentId(
            Long agentId
    );
}
