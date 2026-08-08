package com.example.aiagent.guardrail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ContentModerator {

    @Value("${guardrail.max-input-length:10000}")
    private int maxInputLength;

    @Value("${guardrail.blocked-words}")
    private String blockedWordsConfig;

    private List<String> blockedWords;

    @jakarta.annotation.PostConstruct
    public void init() {
        blockedWords = Arrays.stream(blockedWordsConfig.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public GuardrailResult moderate(String input) {
        // Check length
        if (input.length() > maxInputLength) {
            return GuardrailResult.blocked(
                    "Input exceeds maximum length of " + maxInputLength + " characters",
                    List.of("Input too long: " + input.length() + " characters")
            );
        }

        // Check blocked words
        String lowerInput = input.toLowerCase();
        List<String> detectedBlocked = blockedWords.stream()
                .filter(lowerInput::contains)
                .toList();

        if (!detectedBlocked.isEmpty()) {
            log.warn("Blocked content detected: {}", detectedBlocked);
            return GuardrailResult.blocked(
                    "Content contains prohibited terms",
                    detectedBlocked
            );
        }

        // Check for prompt injection attempts
        List<String> injectionPatterns = List.of(
                "ignore previous instructions",
                "disregard all prior",
                "system prompt",
                "you are now",
                "DAN mode",
                "jailbreak"
        );

        List<String> detectedInjection = injectionPatterns.stream()
                .filter(lowerInput::contains)
                .toList();

        if (!detectedInjection.isEmpty()) {
            log.warn("Prompt injection attempt detected: {}", detectedInjection);
            return GuardrailResult.blocked(
                    "Potential prompt injection detected",
                    detectedInjection
            );
        }

        return GuardrailResult.safe(input);
    }
}