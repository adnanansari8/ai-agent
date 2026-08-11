package com.example.aiagent.cost;

import com.example.aiagent.model.UsageMetrics;
import com.example.aiagent.repository.UsageMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CostTracker {

    private final UsageMetricsRepository metricsRepository;

    @Value("#{${cost.models}}")
    private Map<String, ModelPricing> pricingConfig;

    // In-memory cache for real-time cost tracking
    private final ConcurrentHashMap<String, UserCostSummary> userCostCache =
            new ConcurrentHashMap<>();

    public void trackUsage(String userId, String requestId, String modelName,
                           String promptTemplate, int inputTokens, int outputTokens,
                           long responseTimeMs, boolean cached) {

        ModelPricing pricing = pricingConfig.get(modelName);
        if (pricing == null) {
            pricing = new ModelPricing(new BigDecimal("0.03"), new BigDecimal("0.06"));
            log.warn("No pricing config for model: {}, using defaults", modelName);
        }

        // Calculate costs (per 1K tokens)
        BigDecimal inputCost = pricing.inputPrice()
                .multiply(BigDecimal.valueOf(inputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal outputCost = pricing.outputPrice()
                .multiply(BigDecimal.valueOf(outputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        UsageMetrics metrics = UsageMetrics.builder()
                .userId(userId)
                .requestId(requestId)
                .modelName(modelName)
                .promptTemplate(promptTemplate)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(inputTokens + outputTokens)
                .inputCost(inputCost)
                .outputCost(outputCost)
                .totalCost(inputCost.add(outputCost))
                .cached(cached)
                .responseTimeMs(responseTimeMs)
                .build();

        metricsRepository.save(metrics);
        updateUserCostCache(userId, metrics.getTotalCost());

        log.info("Cost tracked: user={}, model={}, inputTokens={}, outputTokens={}, totalCost=${}",
                userId, modelName, inputTokens, outputTokens, metrics.getTotalCost());
    }

    public UserCostSummary getUserCostSummary(String userId) {
        return userCostCache.getOrDefault(userId, new UserCostSummary(BigDecimal.ZERO, 0, BigDecimal.ZERO));
    }

    public BigDecimal getTotalCostForPeriod(String userId, LocalDateTime start, LocalDateTime end) {
        return metricsRepository.sumTotalCostByUserAndPeriod(userId, start, end);
    }

    private void updateUserCostCache(String userId, BigDecimal cost) {
        userCostCache.compute(userId, (k, v) -> {
            if (v == null) {
                return new UserCostSummary(cost, 1, cost);
            }
            return new UserCostSummary(
                    v.totalCost().add(cost),
                    v.requestCount() + 1,
                    v.totalCost().add(cost).divide(
                            BigDecimal.valueOf(v.requestCount() + 1), 6, RoundingMode.HALF_UP)
            );
        });
    }

    public record ModelPricing(BigDecimal inputPrice, BigDecimal outputPrice) {}
    public record UserCostSummary(BigDecimal totalCost, int requestCount, BigDecimal avgCost) {}
}
