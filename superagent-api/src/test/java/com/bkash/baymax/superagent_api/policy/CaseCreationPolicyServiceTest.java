package com.bkash.baymax.superagent_api.policy;

import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.model.enums.AlertSeverity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseCreationPolicyServiceTest {

    private final CaseCreationPolicyService policyService = new CaseCreationPolicyService();

    @Test
    void lowSeverityShouldNotCreateCase() {
        Alert alert = Alert.builder().severity(AlertSeverity.LOW).build();
        CaseCreationDecision decision = policyService.evaluate(alert);
        assertFalse(decision.shouldCreateCase());
    }

    @Test
    void mediumSeverityShouldNotCreateCase() {
        Alert alert = Alert.builder().severity(AlertSeverity.MEDIUM).build();
        CaseCreationDecision decision = policyService.evaluate(alert);
        assertFalse(decision.shouldCreateCase());
    }

    @Test
    void highSeverityShouldCreateCase() {
        Alert alert = Alert.builder().severity(AlertSeverity.HIGH).build();
        CaseCreationDecision decision = policyService.evaluate(alert);
        assertTrue(decision.shouldCreateCase());
    }

    @Test
    void criticalSeverityShouldCreateCase() {
        Alert alert = Alert.builder().severity(AlertSeverity.CRITICAL).build();
        CaseCreationDecision decision = policyService.evaluate(alert);
        assertTrue(decision.shouldCreateCase());
    }
}
