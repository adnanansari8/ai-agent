package com.example.aiagent.prompt;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false, length = 5000)
    private String template;

    private String description;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private String category;  // customer-support, code-generation, etc.

    @Column(nullable = false)
    private int maxTokens;

    @Column(nullable = false)
    private double temperature;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}