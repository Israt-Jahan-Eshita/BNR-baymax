package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findByAgentCode(String agentCode);

    boolean existsByAgentCode(String agentCode);
}
