package com.example._Do.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service responsible for managing the lifecycle of invalidated JWT tokens.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class JwtBlacklistService {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String JWT_BLACKLIST_PREFIX = "jwt_blacklist:";

    /**
     * Adds a token to the blacklist with a specific expiration time.
     * @param token The JWT token to blacklist
     * @param duration How long the token should stay in the blacklist (usually until its expiration)
     */
    public void blacklistToken(String token, Duration duration) {
        stringRedisTemplate.opsForValue().set(JWT_BLACKLIST_PREFIX + token, "true", duration);
    }

    /**
     * Checks if a token is present in the blacklist.
     * @param token The JWT token to check
     * @return true if blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        return stringRedisTemplate.hasKey(JWT_BLACKLIST_PREFIX + token);
    }
}
