package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.OperationalCase;
import com.bkash.baymax.superagent_api.model.enums.CaseCreationSource;
import com.bkash.baymax.superagent_api.model.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OperationalCaseRepository extends JpaRepository<OperationalCase, Long> {

    Optional<OperationalCase> findByCaseCode(String caseCode);

    boolean existsBySourceAlertId(Long alertId);

    @Query("SELECT oc FROM OperationalCase oc " +
            "WHERE oc.agent.agentCode = :agentCode " +
            "AND (:providerCode IS NULL OR oc.provider.providerCode = :providerCode) " +
            "AND (:status IS NULL OR oc.status = :status) " +
            "AND (:creationSource IS NULL OR oc.creationSource = :creationSource)")
    Page<OperationalCase> findCases(
            @Param("agentCode") String agentCode,
            @Param("providerCode") String providerCode,
            @Param("status") CaseStatus status,
            @Param("creationSource") CaseCreationSource creationSource,
            Pageable pageable
    );
}
