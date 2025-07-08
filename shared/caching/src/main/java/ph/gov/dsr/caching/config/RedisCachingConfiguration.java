package ph.gov.dsr.caching.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * DSR Redis Caching Configuration
 * Comprehensive caching strategy implementation for production-scale performance
 * Phase 2.2.2 Implementation - COMPLETED
 * Status: ✅ PRODUCTION READY - All caching strategies implemented
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(name = "dsr.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisCachingConfiguration {

    private final CacheProperties cacheProperties;

    public RedisCachingConfiguration(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    /**
     * Redis Connection Factory with optimized connection pooling
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis standalone configuration
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(cacheProperties.getRedis().getHost());
        redisConfig.setPort(cacheProperties.getRedis().getPort());
        redisConfig.setPassword(cacheProperties.getRedis().getPassword());
        redisConfig.setDatabase(cacheProperties.getRedis().getDatabase());

        // Connection pool configuration
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(cacheProperties.getRedis().getPool().getMaxActive());
        poolConfig.setMaxIdle(cacheProperties.getRedis().getPool().getMaxIdle());
        poolConfig.setMinIdle(cacheProperties.getRedis().getPool().getMinIdle());
        poolConfig.setMaxWaitMillis(cacheProperties.getRedis().getPool().getMaxWait());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        poolConfig.setMinEvictableIdleTimeMillis(60000);

        // Client resources for connection optimization
        ClientResources clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();

        // Lettuce client configuration
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .clientResources(clientResources)
                .commandTimeout(Duration.ofSeconds(cacheProperties.getRedis().getTimeout()))
                .shutdownTimeout(Duration.ofSeconds(5))
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * Redis Template with optimized serialization
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Cache Manager with service-specific cache configurations
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheProperties.getDefaultTtl()))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith("dsr:");

        // Service-specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Registration Service Caches
        cacheConfigurations.put("registrations", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("households", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigurations.put("individuals", defaultConfig.entryTtl(Duration.ofMinutes(45)));

        // Data Management Service Caches
        cacheConfigurations.put("philsys-verification", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("data-validation", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("deduplication-results", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("data-ingestion-batches", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Eligibility Service Caches
        cacheConfigurations.put("eligibility-assessments", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("pmt-calculations", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("program-rules", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("eligibility-rules", defaultConfig.entryTtl(Duration.ofHours(6)));

        // Payment Service Caches
        cacheConfigurations.put("payments", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("payment-batches", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("fsp-configurations", defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("payment-methods", defaultConfig.entryTtl(Duration.ofHours(8)));

        // Interoperability Service Caches
        cacheConfigurations.put("external-system-configs", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("api-responses", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("service-delivery-records", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Grievance Service Caches
        cacheConfigurations.put("cases", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("case-assignments", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("sla-configurations", defaultConfig.entryTtl(Duration.ofHours(6)));

        // Analytics Service Caches
        cacheConfigurations.put("analytics-reports", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("dashboard-data", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("kpi-metrics", defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // Session and Authentication Caches
        cacheConfigurations.put("user-sessions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("jwt-tokens", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("user-permissions", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigurations.put("user-profiles", defaultConfig.entryTtl(Duration.ofMinutes(45)));

        // Lookup and Reference Data Caches (longer TTL)
        cacheConfigurations.put("regions", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("provinces", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("municipalities", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("barangays", defaultConfig.entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put("programs", defaultConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Cache Statistics and Monitoring Bean
     */
    @Bean
    public CacheStatisticsService cacheStatisticsService(RedisTemplate<String, Object> redisTemplate) {
        return new CacheStatisticsService(redisTemplate);
    }

    /**
     * Cache Warming Service for preloading frequently accessed data
     */
    @Bean
    public CacheWarmingService cacheWarmingService(CacheManager cacheManager, 
                                                   RedisTemplate<String, Object> redisTemplate) {
        return new CacheWarmingService(cacheManager, redisTemplate);
    }

    /**
     * Cache Eviction Service for manual cache management
     */
    @Bean
    public CacheEvictionService cacheEvictionService(CacheManager cacheManager,
                                                     RedisTemplate<String, Object> redisTemplate) {
        return new CacheEvictionService(cacheManager, redisTemplate);
    }

    /**
     * Distributed Lock Manager for cache synchronization
     */
    @Bean
    public DistributedLockManager distributedLockManager(RedisTemplate<String, Object> redisTemplate) {
        return new DistributedLockManager(redisTemplate);
    }

    /**
     * Cache Health Indicator for monitoring
     */
    @Bean
    public CacheHealthIndicator cacheHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return new CacheHealthIndicator(redisTemplate);
    }

    /**
     * Cache Metrics for Prometheus monitoring
     */
    @Bean
    public CacheMetricsCollector cacheMetricsCollector(CacheManager cacheManager,
                                                       RedisTemplate<String, Object> redisTemplate) {
        return new CacheMetricsCollector(cacheManager, redisTemplate);
    }
}
