package com.example.aiagent.guardrail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PiiDetector {

    // Regex patterns for common PII
    private static final Pattern SSN_PATTERN =
            Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(?:\\d{4}[- ]?){3}\\d{4}\\b");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b\\+?\\d{1,3}[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");
    private static final Pattern API_KEY_PATTERN =
            Pattern.compile("\\b(?:api[_-]?key|token|secret)\\s*[:=]\\s*['\"]?\\w{16,}['\"]?\\b",
                    Pattern.CASE_INSENSITIVE);

    public GuardrailResult scan(String input) {
        List<String> detectedPii = new ArrayList<>();
        String sanitized = input;

        if (SSN_PATTERN.matcher(input).find()) {
            detectedPii.add("SSN detected");
            sanitized = SSN_PATTERN.matcher(sanitized).replaceAll("[REDACTED-SSN]");
        }
        if (CREDIT_CARD_PATTERN.matcher(input).find()) {
            detectedPii.add("Credit card number detected");
            sanitized = CREDIT_CARD_PATTERN.matcher(sanitized).replaceAll("[REDACTED-CC]");
        }
        if (EMAIL_PATTERN.matcher(input).find()) {
            detectedPii.add("Email address detected");
            sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[REDACTED-EMAIL]");
        }
        if (PHONE_PATTERN.matcher(input).find()) {
            detectedPii.add("Phone number detected");
            sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[REDACTED-PHONE]");
        }
        if (API_KEY_PATTERN.matcher(input).find()) {
            detectedPii.add("API key/token detected");
            sanitized = API_KEY_PATTERN.matcher(sanitized).replaceAll("[REDACTED-KEY]");
        }

        if (!detectedPii.isEmpty()) {
            log.warn("PII detected in input: {}", detectedPii);
            return GuardrailResult.builder()
                    .allowed(true)  // Allow but sanitize
                    .sanitizedInput(sanitized)
                    .detectedIssues(detectedPii)
                    .riskLevel(GuardrailResult.RiskLevel.MEDIUM)
                    .build();
        }

        return GuardrailResult.safe(input);
    }
}