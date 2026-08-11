package com.example.aiagent.repository;


import com.example.aiagent.model.UsageMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface UsageMetricsRepository extends JpaRepository<UsageMetrics, String> {
    BigDecimal sumTotalCostByUserAndPeriod(String userId, LocalDateTime start, LocalDateTime end);
}
