package com.example._Do.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;


/**
 * Configuration class for Distributed Rate Limiting using Bucket4j and Redis.
 * This configuration ensures that rate limit buckets are synchronized across
 * multiple application instances (horizontally scaled) by using Redis as the
 * centralized state store.
 */
@Configuration
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled}")
    private boolean redisSsl;

    /**
     * Initializes the RedisClient with SSL support, tailored for cloud providers
     * like Azure Cache for Redis.
     *
     * @return Configured RedisClient for establishing connections to the Redis cluster.
     */
    @Bean
    public RedisClient redisClient() {
        RedisURI.Builder builder = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withSsl(redisSsl);

        if (redisPassword != null && !redisPassword.isBlank()) {
            builder.withPassword(redisPassword.toCharArray());
        }

        return RedisClient.create(builder.build());
    }

    /**
     * Configures the ProxyManager to handle distributed bucket state.
     * Uses Lettuce as the underlying Redis driver and implements an
     * expiration strategy to optimize Redis memory usage. Buckets are
     * automatically evicted from Redis after 1 hour of inactivity to prevent
     * state bloat.
     *
     * @param redisClient The primary Redis client.
     * @return ProxyManager configured with Lettuce-based CAS (Compare-And-Swap) operations.
     */
    @Bean
    public ProxyManager<String> proxyManager(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );

        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault().
                withExpirationAfterWriteStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                        Duration.ofHours(1)
                ));

        return LettuceBasedProxyManager.builderFor(connection)
                .withClientSideConfig(clientSideConfig)
                .build();

    }

}
