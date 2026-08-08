package com.example.aiagent.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;
    private String requestId;
    private String modelName;
    private String promptTemplate;

    private int inputTokens;
    private int outputTokens;
    private int totalTokens;

    private BigDecimal inputCost;
    private BigDecimal outputCost;
    private BigDecimal totalCost;

    private boolean cached;
    private long responseTimeMs;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}