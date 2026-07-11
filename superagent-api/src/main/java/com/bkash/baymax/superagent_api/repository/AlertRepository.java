package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import com.bkash.baymax.superagent_api.model.enums.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlertRepository
        extends JpaRepository<Alert, Long> {

    Optional<Alert> findByAlertCode(
            String alertCode
    );

    boolean existsByAlertFingerprint(
            String alertFingerprint
    );

    @Query("""
            SELECT alert
            FROM Alert alert
            WHERE alert.agent.agentCode = :agentCode
              AND (
                    :providerCode IS NULL
                    OR alert.provider.providerCode = :providerCode
                  )
              AND (
                    :alertType IS NULL
                    OR alert.alertType = :alertType
                  )
              AND (
                    :severity IS NULL
                    OR alert.severity = :severity
                  )
            """)
    Page<Alert> findAlerts(
            @Param("agentCode")
            String agentCode,

            @Param("providerCode")
            String providerCode,

            @Param("alertType")
            AlertType alertType,

            @Param("severity")
            AlertSeverity severity,

            Pageable pageable
    );
}
