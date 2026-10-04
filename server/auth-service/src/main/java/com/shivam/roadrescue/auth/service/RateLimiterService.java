package com.shivam.roadrescue.auth.service;

import com.shivam.roadrescue.shared.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);
    private static final String RATE_LIMIT_PREFIX = "ratelimit:login:";

    private final StringRedisTemplate redisTemplate;
    private final int maxAttempts;
    private final long windowSeconds;

    public RateLimiterService(
            StringRedisTemplate redisTemplate,
            @Value("${app.security.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${app.security.rate-limit.window-seconds:60}") long windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
    }

    public void checkAndIncrement(String identifier) {
        String key = RATE_LIMIT_PREFIX + identifier;
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount != null && currentCount == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }

            if (currentCount != null && currentCount > maxAttempts) {
                log.warn("Rate limit exceeded for identifier: {} (attempts: {})", identifier, currentCount);
                throw new RateLimitExceededException("Too many login attempts. Please wait " + windowSeconds + " seconds before trying again.");
            }
        } catch (RateLimitExceededException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to execute rate limiter in Redis, allowing request as fallback", ex);
        }
    }

    public void reset(String identifier) {
        String key = RATE_LIMIT_PREFIX + identifier;
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.error("Failed to reset rate limit for key: {}", key, ex);
        }
    }
}
