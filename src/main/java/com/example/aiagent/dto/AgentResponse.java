package com.example.aiagent.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AgentResponse {
    private String requestId;
    private boolean success;
    private String output;
    private String error;
    private String sanitizedInput;
    private String riskLevel;
    private List<String> detectedIssues;
    private int inputTokens;
    private int outputTokens;
    private long processingTimeMs;
}

