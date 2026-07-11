package com.bkash.baymax.superagent_api.repository;

import com.bkash.baymax.superagent_api.model.CaseAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseAuditEventRepository extends JpaRepository<CaseAuditEvent, Long> {

    List<CaseAuditEvent> findAllByOperationalCaseCaseCodeOrderByOccurredAtAsc(String caseCode);
}
