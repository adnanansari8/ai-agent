package com.example.aiagent.prompt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptManager {

    private final PromptRepository promptRepository;

    public Prompt getPrompt(String templateName, Map<String, Object> variables) {
        PromptTemplate template = promptRepository
                .findByNameAndActiveTrue(templateName)
                .orElseThrow(() -> new RuntimeException(
                        "Prompt template not found: " + templateName));

        // Use Spring AI's template engine
        SystemPromptTemplate systemTemplate =
                new SystemPromptTemplate(template.getTemplate());

        org.springframework.ai.chat.prompt.Prompt prompt =
                systemTemplate.create(variables);

        log.debug("Loaded prompt template: {}, version: {}",
                templateName, template.getVersion());

        return prompt;
    }

    public Prompt getPromptWithOverride(String templateName,
                                        Map<String, Object> variables,
                                        Integer maxTokens,
                                        Double temperature) {
        PromptTemplate template = promptRepository
                .findByNameAndActiveTrue(templateName)
                .orElseThrow(() -> new RuntimeException(
                        "Prompt template not found: " + templateName));

        SystemPromptTemplate systemTemplate =
                new SystemPromptTemplate(template.getTemplate());

        // Apply overrides or use template defaults
        int finalMaxTokens = maxTokens != null ? maxTokens : template.getMaxTokens();
        double finalTemp = temperature != null ? temperature : template.getTemperature();

        return systemTemplate.create(variables);
    }

    public String renderPrompt(String templateName, Map<String, Object> variables) {
        PromptTemplate template = promptRepository
                .findByNameAndActiveTrue(templateName)
                .orElseThrow(() -> new RuntimeException(
                        "Prompt template not found: " + templateName));

        String rendered = template.getTemplate();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            rendered = rendered.replace(
                    "{" + entry.getKey() + "}",
                    String.valueOf(entry.getValue())
            );
        }
        return rendered;
    }
}