package com.example._Do.auth;

import com.example._Do.exception.RateLimitExceededException;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * Interceptor for Distributed Rate Limiting.
 * <p>
 * This interceptor monitors incoming HTTP requests and enforces rate limits
 * based on the client's IP address. It leverages a distributed Redis store
 * via {@link ProxyManager} to ensure consistency across multiple application instances.
 * </p>
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ProxyManager<String> proxyManager;

    /**
     * Intercepts requests before they reach the controller to verify rate limit quotas.
     *
     * @param request  The incoming HttpServletRequest
     * @param response The outgoing HttpServletResponse
     * @param handler  The target handler (controller method)
     * @return {@code true} if the request is within limits; {@code false} otherwise.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Resolve the real client IP address.
        // In cloud environments (Azure, AWS), the request often passes through a Load Balancer or Gateway.
        // 'X-Forwarded-For' ensures we track the actual user, not the proxy's internal IP.
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null ||remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        }

        // Define the Rate Limiting Strategy:
        // Capacity: 10 tokens per bucket.
        // Refill: Greedy refill of 10 tokens every 1 minute.
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(10).refillGreedy(10 , Duration.ofMinutes(1)))
                .build();

        // Retrieve or initialize the bucket from Redis for the resolved IP.
        // Using a Supplier (() -> config) is highly efficient as it avoids redundant
        // configuration object processing if the bucket already exists in the cache.
        BucketProxy bucket = proxyManager.builder().build(remoteAddr, () -> config);

        // Attempt to consume a single token for the current request.
        if (bucket.tryConsume(1)){
            return true; // Limit not exceeded, allow request to proceed.
        } else {
            log.warn("Rate limit exceeded for client identifier: {}", remoteAddr);

            throw new RateLimitExceededException("Too many requests. Please try again after 1 minute.");
        }

    }

}
