package com.example.aiagent.guardrail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuardrailFilter {

    private final PiiDetector piiDetector;
    private final ContentModerator contentModerator;

    public GuardrailResult apply(String input) {
        List<String> allIssues = new ArrayList<>();

        // Step 1: Content moderation
        GuardrailResult moderationResult = contentModerator.moderate(input);
        if (!moderationResult.isAllowed()) {
            return moderationResult;
        }
        if (moderationResult.getDetectedIssues() != null) {
            allIssues.addAll(moderationResult.getDetectedIssues());
        }

        // Step 2: PII detection
        String contentToScan = moderationResult.getSanitizedInput() != null
                ? moderationResult.getSanitizedInput()
                : input;

        GuardrailResult piiResult = piiDetector.scan(contentToScan);
        if (piiResult.getDetectedIssues() != null) {
            allIssues.addAll(piiResult.getDetectedIssues());
        }

        // Combine results
        String finalSanitized = piiResult.getSanitizedInput() != null
                ? piiResult.getSanitizedInput()
                : contentToScan;

        GuardrailResult.RiskLevel finalRisk = determineFinalRisk(
                moderationResult.getRiskLevel(),
                piiResult.getRiskLevel()
        );

        return GuardrailResult.builder()
                .allowed(true)
                .sanitizedInput(finalSanitized)
                .detectedIssues(allIssues.isEmpty() ? null : allIssues)
                .riskLevel(finalRisk)
                .build();
    }

    private GuardrailResult.RiskLevel determineFinalRisk(
            GuardrailResult.RiskLevel moderationRisk,
            GuardrailResult.RiskLevel piiRisk) {

        if (moderationRisk == GuardrailResult.RiskLevel.CRITICAL ||
                piiRisk == GuardrailResult.RiskLevel.CRITICAL) {
            return GuardrailResult.RiskLevel.CRITICAL;
        }
        if (moderationRisk == GuardrailResult.RiskLevel.HIGH ||
                piiRisk == GuardrailResult.RiskLevel.HIGH) {
            return GuardrailResult.RiskLevel.HIGH;
        }
        if (moderationRisk == GuardrailResult.RiskLevel.MEDIUM ||
                piiRisk == GuardrailResult.RiskLevel.MEDIUM) {
            return GuardrailResult.RiskLevel.MEDIUM;
        }
        return GuardrailResult.RiskLevel.SAFE;
    }
}