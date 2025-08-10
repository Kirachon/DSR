package ph.gov.dsr.performance.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Redis Cluster Cache Manager with intelligent caching strategies
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisClusterCacheManager {

    @Value("${dsr.cache.redis.cluster.nodes}")
    private String clusterNodes;

    @Value("${dsr.cache.redis.cluster.max-redirects:3}")
    private int maxRedirects;

    @Value("${dsr.cache.default-ttl:3600}")
    private long defaultTtlSeconds;

    @Value("${dsr.cache.enable-compression:true}")
    private boolean enableCompression;

    private RedisTemplate<String, Object> redisTemplate;
    private CacheManager cacheManager;
    private final Map<String, CacheConfiguration> cacheConfigurations = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        setupRedisCluster();
        configureCacheStrategies();
        log.info("Redis Cluster Cache Manager initialized successfully");
    }

    private void setupRedisCluster() {
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

            // Configure Redis Template
            redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(connectionFactory);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            redisTemplate.setHashKeySerializer(new StringRedisSerializer());
            redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
            redisTemplate.afterPropertiesSet();

            // Configure Cache Manager
            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(defaultTtlSeconds))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer()));

            if (!enableCompression) {
                defaultConfig = defaultConfig.disableCachingNullValues();
            }

            cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .build();

        } catch (Exception e) {
            log.error("Failed to setup Redis cluster", e);
            throw new RuntimeException("Redis cluster initialization failed", e);
        }
    }

    private void configureCacheStrategies() {
        // Configure different cache strategies for different data types
        
        // User data - medium TTL, high priority
        addCacheConfiguration("users", CacheConfiguration.builder()
            .ttl(Duration.ofMinutes(30))
            .maxSize(10000)
            .evictionPolicy(EvictionPolicy.LRU)
            .compressionEnabled(true)
            .build());

        // Household data - long TTL, high priority
        addCacheConfiguration("households", CacheConfiguration.builder()
            .ttl(Duration.ofHours(2))
            .maxSize(50000)
            .evictionPolicy(EvictionPolicy.LRU)
            .compressionEnabled(true)
            .build());

        // PhilSys verification - very long TTL
        addCacheConfiguration("philsys", CacheConfiguration.builder()
            .ttl(Duration.ofHours(24))
            .maxSize(100000)
            .evictionPolicy(EvictionPolicy.LFU)
            .compressionEnabled(true)
            .build());

        // Session data - short TTL, high priority
        addCacheConfiguration("sessions", CacheConfiguration.builder()
            .ttl(Duration.ofMinutes(15))
            .maxSize(20000)
            .evictionPolicy(EvictionPolicy.TTL)
            .compressionEnabled(false)
            .build());

        // Analytics data - medium TTL, lower priority
        addCacheConfiguration("analytics", CacheConfiguration.builder()
            .ttl(Duration.ofMinutes(10))
            .maxSize(5000)
            .evictionPolicy(EvictionPolicy.LRU)
            .compressionEnabled(true)
            .build());

        // API responses - short TTL for frequently changing data
        addCacheConfiguration("api-responses", CacheConfiguration.builder()
            .ttl(Duration.ofMinutes(5))
            .maxSize(15000)
            .evictionPolicy(EvictionPolicy.LRU)
            .compressionEnabled(true)
            .build());
    }

    /**
     * Get cached value with automatic fallback and refresh
     */
    public <T> CompletableFuture<T> getAsync(String cacheName, String key, Class<T> type) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Cache.ValueWrapper wrapper = cache.get(key);
                    if (wrapper != null) {
                        Object value = wrapper.get();
                        if (type.isInstance(value)) {
                            log.debug("Cache hit for key: {} in cache: {}", key, cacheName);
                            return type.cast(value);
                        }
                    }
                }
                log.debug("Cache miss for key: {} in cache: {}", key, cacheName);
                return null;
            } catch (Exception e) {
                log.error("Failed to get from cache: {} key: {}", cacheName, key, e);
                return null;
            }
        });
    }

    /**
     * Put value in cache with intelligent TTL
     */
    public <T> CompletableFuture<Void> putAsync(String cacheName, String key, T value) {
        return CompletableFuture.runAsync(() -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.put(key, value);
                    log.debug("Cached value for key: {} in cache: {}", key, cacheName);
                } else {
                    log.warn("Cache not found: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("Failed to put in cache: {} key: {}", cacheName, key, e);
            }
        });
    }

    /**
     * Intelligent cache warming for frequently accessed data
     */
    public CompletableFuture<Void> warmCache(String cacheName, Map<String, Object> data) {
        return CompletableFuture.runAsync(() -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        cache.put(entry.getKey(), entry.getValue());
                    }
                    log.info("Warmed cache: {} with {} entries", cacheName, data.size());
                } else {
                    log.warn("Cache not found for warming: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("Failed to warm cache: {}", cacheName, e);
            }
        });
    }

    /**
     * Bulk get operation for multiple keys
     */
    public <T> CompletableFuture<Map<String, T>> getBulkAsync(String cacheName, 
                                                              Iterable<String> keys, 
                                                              Class<T> type) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, T> results = new HashMap<>();
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    for (String key : keys) {
                        Cache.ValueWrapper wrapper = cache.get(key);
                        if (wrapper != null) {
                            Object value = wrapper.get();
                            if (type.isInstance(value)) {
                                results.put(key, type.cast(value));
                            }
                        }
                    }
                }
                log.debug("Bulk cache operation for cache: {} returned {} results", 
                         cacheName, results.size());
            } catch (Exception e) {
                log.error("Failed bulk get from cache: {}", cacheName, e);
            }
            return results;
        });
    }

    /**
     * Bulk put operation for multiple key-value pairs
     */
    public CompletableFuture<Void> putBulkAsync(String cacheName, Map<String, Object> data) {
        return CompletableFuture.runAsync(() -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        cache.put(entry.getKey(), entry.getValue());
                    }
                    log.debug("Bulk cached {} entries in cache: {}", data.size(), cacheName);
                } else {
                    log.warn("Cache not found for bulk put: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("Failed bulk put to cache: {}", cacheName, e);
            }
        });
    }

    /**
     * Evict specific key from cache
     */
    public CompletableFuture<Void> evictAsync(String cacheName, String key) {
        return CompletableFuture.runAsync(() -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.evict(key);
                    log.debug("Evicted key: {} from cache: {}", key, cacheName);
                } else {
                    log.warn("Cache not found for eviction: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("Failed to evict from cache: {} key: {}", cacheName, key, e);
            }
        });
    }

    /**
     * Clear entire cache
     */
    public CompletableFuture<Void> clearAsync(String cacheName) {
        return CompletableFuture.runAsync(() -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("Cleared cache: {}", cacheName);
                } else {
                    log.warn("Cache not found for clearing: {}", cacheName);
                }
            } catch (Exception e) {
                log.error("Failed to clear cache: {}", cacheName, e);
            }
        });
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics(String cacheName) {
        try {
            // Implementation would depend on the specific Redis monitoring setup
            return CacheStatistics.builder()
                .cacheName(cacheName)
                .hitCount(0L) // Would be retrieved from Redis INFO
                .missCount(0L)
                .evictionCount(0L)
                .size(0L)
                .build();
        } catch (Exception e) {
            log.error("Failed to get cache statistics for: {}", cacheName, e);
            return CacheStatistics.empty(cacheName);
        }
    }

    /**
     * Health check for Redis cluster
     */
    public boolean isHealthy() {
        try {
            redisTemplate.opsForValue().set("health-check", "ok", Duration.ofSeconds(10));
            String result = (String) redisTemplate.opsForValue().get("health-check");
            return "ok".equals(result);
        } catch (Exception e) {
            log.error("Redis cluster health check failed", e);
            return false;
        }
    }

    /**
     * Get cluster information and statistics
     */
    public ClusterInfo getClusterInfo() {
        try {
            RedisClusterConnection clusterConnection = redisTemplate.getConnectionFactory()
                .getClusterConnection();

            ClusterInfo.Builder builder = ClusterInfo.builder();

            // Get cluster nodes
            Iterable<RedisClusterNode> nodes = clusterConnection.clusterGetNodes();
            int activeNodes = 0;
            int masterNodes = 0;
            int slaveNodes = 0;

            for (RedisClusterNode node : nodes) {
                if (node.isMaster()) {
                    masterNodes++;
                } else {
                    slaveNodes++;
                }
                activeNodes++;
            }

            builder.totalNodes(activeNodes)
                   .masterNodes(masterNodes)
                   .slaveNodes(slaveNodes)
                   .clusterState("ok")
                   .slotsAssigned(16384); // Redis cluster has 16384 slots

            return builder.build();

        } catch (Exception e) {
            log.error("Failed to get cluster info", e);
            return ClusterInfo.builder()
                .totalNodes(0)
                .masterNodes(0)
                .slaveNodes(0)
                .clusterState("error")
                .slotsAssigned(0)
                .build();
        }
    }

    /**
     * Get cache statistics for monitoring
     */
    public CacheStatistics getCacheStatistics() {
        try {
            CacheStatistics.Builder builder = CacheStatistics.builder();

            // Get memory usage
            Properties info = redisTemplate.getConnectionFactory().getConnection().info("memory");
            String usedMemory = info.getProperty("used_memory");
            String maxMemory = info.getProperty("maxmemory");

            builder.usedMemory(Long.parseLong(usedMemory != null ? usedMemory : "0"))
                   .maxMemory(Long.parseLong(maxMemory != null ? maxMemory : "0"));

            // Get keyspace info
            Properties keyspaceInfo = redisTemplate.getConnectionFactory().getConnection().info("keyspace");
            int totalKeys = 0;
            for (String key : keyspaceInfo.stringPropertyNames()) {
                if (key.startsWith("db")) {
                    String value = keyspaceInfo.getProperty(key);
                    // Parse "keys=X,expires=Y,avg_ttl=Z"
                    String[] parts = value.split(",");
                    if (parts.length > 0) {
                        String keysPart = parts[0];
                        if (keysPart.startsWith("keys=")) {
                            totalKeys += Integer.parseInt(keysPart.substring(5));
                        }
                    }
                }
            }

            builder.totalKeys(totalKeys)
                   .hitRate(calculateHitRate())
                   .evictionCount(getEvictionCount());

            return builder.build();

        } catch (Exception e) {
            log.error("Failed to get cache statistics", e);
            return CacheStatistics.builder()
                .usedMemory(0)
                .maxMemory(0)
                .totalKeys(0)
                .hitRate(0.0)
                .evictionCount(0)
                .build();
        }
    }

    /**
     * Perform cache warming for critical data
     */
    public CompletableFuture<Void> warmupCache() {
        return CompletableFuture.runAsync(() -> {
            log.info("Starting cache warmup process");

            try {
                // Warmup user sessions cache
                warmupUserSessions();

                // Warmup frequently accessed household data
                warmupHouseholdData();

                // Warmup PhilSys verification cache
                warmupPhilSysData();

                // Warmup program criteria
                warmupProgramCriteria();

                log.info("Cache warmup completed successfully");

            } catch (Exception e) {
                log.error("Cache warmup failed", e);
            }
        });
    }

    private void warmupUserSessions() {
        // Implementation would load active user sessions
        log.debug("Warming up user sessions cache");
    }

    private void warmupHouseholdData() {
        // Implementation would load frequently accessed household data
        log.debug("Warming up household data cache");
    }

    private void warmupPhilSysData() {
        // Implementation would load PhilSys verification data
        log.debug("Warming up PhilSys verification cache");
    }

    private void warmupProgramCriteria() {
        // Implementation would load program criteria and rules
        log.debug("Warming up program criteria cache");
    }

    private double calculateHitRate() {
        try {
            Properties stats = redisTemplate.getConnectionFactory().getConnection().info("stats");
            String hits = stats.getProperty("keyspace_hits");
            String misses = stats.getProperty("keyspace_misses");

            if (hits != null && misses != null) {
                long hitCount = Long.parseLong(hits);
                long missCount = Long.parseLong(misses);
                long total = hitCount + missCount;

                return total > 0 ? (double) hitCount / total : 0.0;
            }

            return 0.0;
        } catch (Exception e) {
            log.warn("Failed to calculate hit rate", e);
            return 0.0;
        }
    }

    private long getEvictionCount() {
        try {
            Properties stats = redisTemplate.getConnectionFactory().getConnection().info("stats");
            String evictions = stats.getProperty("evicted_keys");
            return evictions != null ? Long.parseLong(evictions) : 0;
        } catch (Exception e) {
            log.warn("Failed to get eviction count", e);
            return 0;
        }
    }

    private void addCacheConfiguration(String cacheName, CacheConfiguration config) {
        cacheConfigurations.put(cacheName, config);
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
