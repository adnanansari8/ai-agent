package com.example.aiagent.dto;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AgentRequest {
    private String input;
    private String templateName;
    private String modelName;
    private String context;
    private Integer maxTokens;
    private Double temperature;
}
