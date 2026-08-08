package com.example.aiagent.service;

import com.example.aiagent.dto.AgentRequest;
import com.example.aiagent.dto.AgentResponse;
import com.example.aiagent.guardrail.GuardrailResult;
import com.example.aiagent.cost.CostTracker;
import com.example.aiagent.guardrail.GuardrailFilter;
import com.example.aiagent.prompt.PromptManager;
import com.example.aiagent.cost.TokenCounter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AiAgentService {


    private ChatClient chatClient;
    private PromptManager promptManager;
    private GuardrailFilter guardrailFilter;
    private CostTracker costTracker;
    private TokenCounter tokenCounter;

    public AiAgentService(@Autowired PromptManager promptManager, @Autowired GuardrailFilter guardrailFilter, @Autowired CostTracker costTracker, @Autowired TokenCounter tokenCounter)
    {
         // this.chatClient = chatClient;
          this.promptManager = promptManager;
          this.guardrailFilter = guardrailFilter;
          this.costTracker = costTracker;
          this.tokenCounter = tokenCounter;
    }


    public AgentResponse processRequest(AgentRequest request) {
        String requestId = UUID.randomUUID().toString();
        String userId = getCurrentUserId();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Apply guardrails
            GuardrailResult guardrailResult = guardrailFilter.apply(request.getInput());
            if (!guardrailResult.isAllowed()) {
                return AgentResponse.builder()
                        .requestId(requestId)
                        .success(false)
                        .error(guardrailResult.getReason())
                        .detectedIssues(guardrailResult.getDetectedIssues())
                        .build();
            }

            String sanitizedInput = guardrailResult.getSanitizedInput();

            // Step 2: Load prompt template
            Prompt prompt = promptManager.getPrompt(
                    request.getTemplateName(),
                    Map.of(
                            "query", sanitizedInput,
                            "userId", userId,
                            "context", request.getContext() != null ? request.getContext() : ""
                    )
            );

            // Step 3: Call AI model
            ChatResponse chatResponse = (ChatResponse) chatClient.prompt(prompt);
            String aiOutput = chatResponse.getResult().getOutput().getText();

            // Step 4: Track costs
            int inputTokens = tokenCounter.estimateTokensV2(sanitizedInput);
            int outputTokens = tokenCounter.estimateTokensV2(aiOutput);

            costTracker.trackUsage(
                    userId,
                    requestId,
                    request.getModelName() != null ? request.getModelName() : "gpt-4",
                    request.getTemplateName(),
                    inputTokens,
                    outputTokens,
                    System.currentTimeMillis() - startTime,
                    false
            );

            return AgentResponse.builder()
                    .requestId(requestId)
                    .success(true)
                    .output(aiOutput)
                    .sanitizedInput(sanitizedInput)
                    .riskLevel(guardrailResult.getRiskLevel().name())
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("Error processing request: {}", requestId, e);
            return AgentResponse.builder()
                    .requestId(requestId)
                    .success(false)
                    .error("Internal error: " + e.getMessage())
                    .build();
        }
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails user) {
            return user.getUsername();
        }
        return "anonymous";
    }
}