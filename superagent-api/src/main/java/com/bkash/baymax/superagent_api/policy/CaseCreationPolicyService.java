package com.bkash.baymax.superagent_api.policy;

import com.bkash.baymax.superagent_api.model.Alert;
import org.springframework.stereotype.Service;

@Service
public class CaseCreationPolicyService {

    public CaseCreationDecision evaluate(Alert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("Alert cannot be null");
        }

        return switch (alert.getSeverity()) {
            case LOW -> new CaseCreationDecision(
                    false,
                    "LOW severity alerts remain visible without automatic case creation."
            );
            case MEDIUM -> new CaseCreationDecision(
                    false,
                    "MEDIUM severity alerts remain available for manual operational review."
            );
            case HIGH -> new CaseCreationDecision(
                    true,
                    "HIGH severity alerts automatically open an operational case."
            );
            case CRITICAL -> new CaseCreationDecision(
                    true,
                    "CRITICAL severity alerts automatically open an operational case."
            );
        };
    }
}
