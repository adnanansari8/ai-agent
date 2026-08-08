package com.example.aiagent.ratelimit;

import com.example.aiagent.ratelimit.TokenBucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RateLimitService {

    @Value("${rate-limit.default.capacity}")
    private long defaultCapacity;

    @Value("${rate-limit.default.refill}")
    private long defaultRefill;

    @Value("${rate-limit.premium.capacity}")
    private long premiumCapacity;

    @Value("${rate-limit.premium.refill}")
    private long premiumRefill;

    private final StringRedisTemplate redisTemplate;
    private final TokenBucket localBucket;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localBucket = new TokenBucket();
    }

    public boolean isAllowed(String userId, String tier) {
        String key = "ratelimit:" + userId;

        long capacity = "premium".equals(tier) ? premiumCapacity : defaultCapacity;
        long refill = "premium".equals(tier) ? premiumRefill : defaultRefill;

        // Try distributed rate limit (Redis) first
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        if (current != null && current > capacity) {
            log.warn("Rate limit exceeded for user: {}, tier: {}", userId, tier);
            return false;
        }

        // Fallback to local token bucket for burst protection
        return localBucket.tryConsume(userId, capacity, refill);
    }

    public RateLimitStatus getStatus(String userId, String tier) {
        String key = "ratelimit:" + userId;
        String currentStr = redisTemplate.opsForValue().get(key);
        long current = currentStr != null ? Long.parseLong(currentStr) : 0;

        long capacity = "premium".equals(tier) ? premiumCapacity : defaultCapacity;

        return new RateLimitStatus(current, capacity, capacity - current);
    }

    public record RateLimitStatus(long used, long limit, long remaining) {}
}