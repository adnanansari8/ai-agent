package com.example.aiagent.guardrail;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GuardrailResult {
    private boolean allowed;
    private String reason;
    private String sanitizedInput;
    private List<String> detectedIssues;
    private RiskLevel riskLevel;

    public enum RiskLevel {
        SAFE, LOW, MEDIUM, HIGH, CRITICAL
    }

    public static GuardrailResult safe(String sanitized) {
        return GuardrailResult.builder()
                .allowed(true)
                .sanitizedInput(sanitized)
                .riskLevel(RiskLevel.SAFE)
                .build();
    }

    public static GuardrailResult blocked(String reason, List<String> issues) {
        return GuardrailResult.builder()
                .allowed(false)
                .reason(reason)
                .detectedIssues(issues)
                .riskLevel(RiskLevel.CRITICAL)
                .build();
    }
}