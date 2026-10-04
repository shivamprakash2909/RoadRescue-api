package com.shivam.roadrescue.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:token:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long ttlMillis) {
        if (!StringUtils.hasText(token)) {
            return;
        }

        String rawToken = extractRawToken(token);
        String key = BLACKLIST_KEY_PREFIX + rawToken;
        long effectiveTtl = Math.max(ttlMillis, 1000L);

        try {
            redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(effectiveTtl));
            log.info("Token blacklisted with TTL {} ms", effectiveTtl);
        } catch (Exception ex) {
            log.error("Failed to blacklist token in Redis", ex);
        }
    }

    public boolean isBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        String rawToken = extractRawToken(token);
        String key = BLACKLIST_KEY_PREFIX + rawToken;

        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception ex) {
            log.error("Failed to check token blacklist in Redis, falling back to false", ex);
            return false;
        }
    }

    private String extractRawToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }
        return token.trim();
    }
}
