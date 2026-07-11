package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiExplanationService {

    private final OpenAiService openAiService;
    private final AlertRepository alertRepository;

    @Async
    @Transactional
    public void enrichAlertWithAi(String alertCode) {
        alertRepository.findByAlertCode(alertCode).ifPresent(alert -> {
            try {
                String systemPrompt = "You are BNR Baymax, a liquidity risk analysis AI for bKash, Nagad, and Rocket super-agents in Bangladesh. "
                        + "Your job is to enrich an alert detected by the deterministic backend. "
                        + "Output exactly 3 lines separated by newline (no formatting, no markdown):\n"
                        + "Line 1: A rich natural language explanation (in English) of what happened and why it's a risk.\n"
                        + "Line 2: A short risk assessment (e.g. 'HIGH RISK - Potential liquidity drain').\n"
                        + "Line 3: Recommended action for the agent operator.";

                String userPrompt = "Alert Type: " + alert.getAlertType() + "\n"
                        + "Title: " + alert.getTitle() + "\n"
                        + "Summary: " + alert.getSummary() + "\n"
                        + "Evidence: " + String.join("; ", alert.getEvidence());

                String response = openAiService.getChatCompletion(systemPrompt, userPrompt);
                
                if (response != null) {
                    String[] lines = response.split("\n");
                    if (lines.length >= 3) {
                        alert.setAiExplanation(lines[0].trim());
                        alert.setAiRiskAssessment(lines[1].trim());
                        alert.setAiRecommendedAction(lines[2].trim());
                        alertRepository.save(alert);
                        log.info("Successfully enriched alert {} with AI", alertCode);
                        return;
                    }
                }
                log.warn("Failed to parse OpenAI response for alert {}", alertCode);
            } catch (Exception e) {
                log.error("Failed to enrich alert {} with AI", alertCode, e);
            }
        });
    }
}
