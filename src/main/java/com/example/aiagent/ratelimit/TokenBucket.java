package com.example.aiagent.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TokenBucket {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, long capacity, long refillTokens) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(capacity, refillTokens));
        return bucket.tryConsume(1);
    }

    public boolean tryConsume(String key, long capacity, long refillTokens, int tokens) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(capacity, refillTokens));
        return bucket.tryConsume(tokens);
    }

    private Bucket createBucket(long capacity, long refillTokens) {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.intervally(refillTokens, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public void removeBucket(String key) {
        buckets.remove(key);
    }
}