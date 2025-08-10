package ph.gov.dsr.datamanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache configuration for the Data Management Service.
 * Supports both Redis cluster and in-memory caching.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.data.redis.cluster.nodes:}")
    private String clusterNodes;

    @Value("${spring.data.redis.cluster.max-redirects:3}")
    private int maxRedirects;

    @Value("${dsr.cache.enabled:true}")
    private boolean cacheEnabled;

    /**
     * Redis cluster cache manager for production.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "dsr.cache.type", havingValue = "redis", matchIfMissing = false)
    public CacheManager redisCacheManager() {
        if (!cacheEnabled || clusterNodes.isEmpty()) {
            return fallbackCacheManager();
        }

        try {
            // Configure Redis Cluster
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();
            String[] nodes = clusterNodes.split(",");
            for (String node : nodes) {
                String[] parts = node.trim().split(":");
                clusterConfig.clusterNode(parts[0], Integer.parseInt(parts[1]));
            }
            clusterConfig.setMaxRedirects(maxRedirects);

            // Create connection factory
            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(clusterConfig);
            connectionFactory.afterPropertiesSet();

            // Configure cache with different TTLs for different data types
            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

            return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("data-validation",
                    defaultConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("deduplication-results",
                    defaultConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("philsys-verification",
                    defaultConfig.entryTtl(Duration.ofHours(24)))
                .withCacheConfiguration("data-ingestion-batches",
                    defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("legacy-data-parsing",
                    defaultConfig.entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration("user-sessions",
                    defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .build();

        } catch (Exception e) {
            // Fallback to in-memory cache if Redis cluster fails
            return fallbackCacheManager();
        }
    }

    /**
     * In-memory cache manager for development and fallback.
     */
    @Bean
    @ConditionalOnProperty(name = "dsr.cache.type", havingValue = "simple", matchIfMissing = true)
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager(
            "data-validation",
            "deduplication-results",
            "philsys-verification",
            "data-ingestion-batches",
            "legacy-data-parsing",
            "user-sessions"
        );
    }

    /**
     * Redis template for direct Redis operations.
     */
    @Bean
    @ConditionalOnProperty(name = "dsr.cache.type", havingValue = "redis")
    public RedisTemplate<String, Object> redisTemplate() {
        if (clusterNodes.isEmpty()) {
            return null;
        }

        try {
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();
            String[] nodes = clusterNodes.split(",");
            for (String node : nodes) {
                String[] parts = node.trim().split(":");
                clusterConfig.clusterNode(parts[0], Integer.parseInt(parts[1]));
            }
            clusterConfig.setMaxRedirects(maxRedirects);

            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(clusterConfig);
            connectionFactory.afterPropertiesSet();

            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.afterPropertiesSet();

            return template;
        } catch (Exception e) {
            return null;
        }
    }
}
