package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.analytics.DetectedSignal;
import com.bkash.baymax.superagent_api.dto.response.AlertDetailResponse;
import com.bkash.baymax.superagent_api.model.Alert;
import com.bkash.baymax.superagent_api.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private static final long FIFTEEN_MINUTE_SECONDS =
            15L * 60L;

    private final AlertRepository alertRepository;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AiExplanationService aiExplanationService;

    @Transactional
    public Optional<AlertDetailResponse> createIfAbsent(
            DetectedSignal signal
    ) {
        String fingerprint =
                generateFingerprint(signal);

        if (
                alertRepository.existsByAlertFingerprint(
                        fingerprint
                )
        ) {
            return Optional.empty();
        }

        Alert alert = Alert.builder()
                .alertCode(generateAlertCode())
                .alertFingerprint(fingerprint)
                .agent(signal.agent())
                .provider(signal.provider())
                .alertType(signal.alertType())
                .severity(signal.severity())
                .confidence(signal.confidence())
                .confidenceScore(signal.confidenceScore())
                .title(signal.title())
                .summary(signal.summary())
                .evidence(signal.evidence())
                .possibleNormalExplanation(
                        signal.possibleNormalExplanation()
                )
                .uncertainty(signal.uncertainty())
                .safeNextStep(signal.safeNextStep())
                .windowStart(signal.windowStart())
                .windowEnd(signal.windowEnd())
                .detectedAt(Instant.now(clock))
                .build();

        Alert saved = alertRepository.save(alert);

        applicationEventPublisher.publishEvent(
                new com.bkash.baymax.superagent_api.event.AlertPersistedEvent(
                        saved.getAlertCode()
                )
        );

        // Async AI Enrichment
        aiExplanationService.enrichAlertWithAi(saved.getAlertCode());

        return Optional.of(
                toDetailResponse(saved)
        );
    }

    public AlertDetailResponse toDetailResponse(
            Alert alert
    ) {
        return new AlertDetailResponse(
                alert.getAlertCode(),
                alert.getAgent().getAgentCode(),
                alert.getProvider() == null
                        ? null
                        : alert.getProvider()
                                .getProviderCode(),
                alert.getProvider() == null
                        ? null
                        : alert.getProvider()
                                .getDisplayName(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getConfidence(),
                alert.getConfidenceScore(),
                alert.getTitle(),
                alert.getSummary(),
                List.copyOf(alert.getEvidence()),
                alert.getPossibleNormalExplanation(),
                alert.getUncertainty(),
                alert.getSafeNextStep(),
                alert.getWindowStart(),
                alert.getWindowEnd(),
                alert.getAiExplanation(),
                alert.getAiRiskAssessment(),
                alert.getAiRecommendedAction(),
                alert.getMlReviewProbability(),
                alert.getMlRequiresReview(),
                alert.getMlModelVersion(),
                alert.getMlSelectedThreshold(),
                alert.getEventContextSummary(),
                alert.getDetectedAt(),
                alert.getCreatedAt()
        );
    }

    private String generateFingerprint(
            DetectedSignal signal
    ) {
        Instant bucketStart =
                toFifteenMinuteBucket(
                        signal.windowEnd()
                );

        String providerCode =
                signal.provider() == null
                        ? "SHARED"
                        : signal.provider()
                                .getProviderCode();

        String rawFingerprint =
                signal.agent().getAgentCode()
                        + "|"
                        + providerCode
                        + "|"
                        + signal.alertType().name()
                        + "|"
                        + bucketStart;

        return sha256(rawFingerprint);
    }

    private Instant toFifteenMinuteBucket(
            Instant instant
    ) {
        long epochSecond = instant.getEpochSecond();

        long bucketEpoch =
                epochSecond
                        - (
                                epochSecond
                                % FIFTEEN_MINUTE_SECONDS
                        );

        return Instant.ofEpochSecond(bucketEpoch);
    }

    private String sha256(
            String value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of().formatHex(hash);
        } catch (
                NoSuchAlgorithmException exception
        ) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }

    private String generateAlertCode() {
        return "ALT-"
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase(Locale.ROOT);
    }
}
