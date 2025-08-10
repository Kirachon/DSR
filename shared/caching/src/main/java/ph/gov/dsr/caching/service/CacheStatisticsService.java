package ph.gov.dsr.caching.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DSR Cache Statistics Service
 * Comprehensive cache performance monitoring and statistics collection
 * Phase 2.2.2 Implementation - COMPLETED
 * Status: ✅ PRODUCTION READY - All cache statistics implemented
 */
@Service
@Slf4j
public class CacheStatisticsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, CacheStats> cacheStatistics = new ConcurrentHashMap<>();
    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final AtomicLong totalEvictions = new AtomicLong(0);

    public CacheStatisticsService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        initializeCacheStats();
    }

    /**
     * Initialize cache statistics for all cache regions
     */
    private void initializeCacheStats() {
        String[] cacheNames = {
            "registrations", "households", "individuals",
            "philsys-verification", "data-validation", "deduplication-results", "data-ingestion-batches",
            "eligibility-assessments", "pmt-calculations", "program-rules", "eligibility-rules",
            "payments", "payment-batches", "fsp-configurations", "payment-methods",
            "external-system-configs", "api-responses", "service-delivery-records",
            "cases", "case-assignments", "sla-configurations",
            "analytics-reports", "dashboard-data", "kpi-metrics",
            "user-sessions", "jwt-tokens", "user-permissions", "user-profiles",
            "regions", "provinces", "municipalities", "barangays", "programs"
        };

        for (String cacheName : cacheNames) {
            cacheStatistics.put(cacheName, new CacheStats(cacheName));
        }
    }

    /**
     * Record cache hit
     */
    public void recordHit(String cacheName) {
        totalHits.incrementAndGet();
        CacheStats stats = cacheStatistics.get(cacheName);
        if (stats != null) {
            stats.recordHit();
        }
        log.debug("Cache hit recorded for cache: {}", cacheName);
    }

    /**
     * Record cache miss
     */
    public void recordMiss(String cacheName) {
        totalMisses.incrementAndGet();
        CacheStats stats = cacheStatistics.get(cacheName);
        if (stats != null) {
            stats.recordMiss();
        }
        log.debug("Cache miss recorded for cache: {}", cacheName);
    }

    /**
     * Record cache eviction
     */
    public void recordEviction(String cacheName) {
        totalEvictions.incrementAndGet();
        CacheStats stats = cacheStatistics.get(cacheName);
        if (stats != null) {
            stats.recordEviction();
        }
        log.debug("Cache eviction recorded for cache: {}", cacheName);
    }

    /**
     * Get overall cache hit ratio
     */
    public double getOverallHitRatio() {
        long hits = totalHits.get();
        long misses = totalMisses.get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }

    /**
     * Get cache statistics for specific cache
     */
    public CacheStats getCacheStats(String cacheName) {
        return cacheStatistics.get(cacheName);
    }

    /**
     * Get all cache statistics
     */
    public Map<String, CacheStats> getAllCacheStats() {
        return new HashMap<>(cacheStatistics);
    }

    /**
     * Get Redis memory usage information
     */
    public RedisMemoryInfo getRedisMemoryInfo() {
        try {
            Properties info = redisTemplate.getConnectionFactory().getConnection().info("memory");
            
            return RedisMemoryInfo.builder()
                    .usedMemory(Long.parseLong(info.getProperty("used_memory", "0")))
                    .usedMemoryHuman(info.getProperty("used_memory_human", "0B"))
                    .usedMemoryRss(Long.parseLong(info.getProperty("used_memory_rss", "0")))
                    .usedMemoryPeak(Long.parseLong(info.getProperty("used_memory_peak", "0")))
                    .usedMemoryPeakHuman(info.getProperty("used_memory_peak_human", "0B"))
                    .totalSystemMemory(Long.parseLong(info.getProperty("total_system_memory", "0")))
                    .maxMemory(Long.parseLong(info.getProperty("maxmemory", "0")))
                    .maxMemoryHuman(info.getProperty("maxmemory_human", "0B"))
                    .memoryFragmentationRatio(Double.parseDouble(info.getProperty("mem_fragmentation_ratio", "0.0")))
                    .build();
        } catch (Exception e) {
            log.error("Failed to get Redis memory info", e);
            return RedisMemoryInfo.builder().build();
        }
    }

    /**
     * Get Redis connection information
     */
    public RedisConnectionInfo getRedisConnectionInfo() {
        try {
            Properties info = redisTemplate.getConnectionFactory().getConnection().info("clients");
            
            return RedisConnectionInfo.builder()
                    .connectedClients(Integer.parseInt(info.getProperty("connected_clients", "0")))
                    .clientRecentMaxInputBuffer(Long.parseLong(info.getProperty("client_recent_max_input_buffer", "0")))
                    .clientRecentMaxOutputBuffer(Long.parseLong(info.getProperty("client_recent_max_output_buffer", "0")))
                    .blockedClients(Integer.parseInt(info.getProperty("blocked_clients", "0")))
                    .build();
        } catch (Exception e) {
            log.error("Failed to get Redis connection info", e);
            return RedisConnectionInfo.builder().build();
        }
    }

    /**
     * Get cache performance summary
     */
    public CachePerformanceSummary getPerformanceSummary() {
        Map<String, Double> hitRatios = new HashMap<>();
        Map<String, Long> totalOperations = new HashMap<>();
        
        for (Map.Entry<String, CacheStats> entry : cacheStatistics.entrySet()) {
            CacheStats stats = entry.getValue();
            hitRatios.put(entry.getKey(), stats.getHitRatio());
            totalOperations.put(entry.getKey(), stats.getTotalOperations());
        }

        return CachePerformanceSummary.builder()
                .overallHitRatio(getOverallHitRatio())
                .totalHits(totalHits.get())
                .totalMisses(totalMisses.get())
                .totalEvictions(totalEvictions.get())
                .cacheHitRatios(hitRatios)
                .cacheTotalOperations(totalOperations)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Reset all statistics
     */
    public void resetStatistics() {
        totalHits.set(0);
        totalMisses.set(0);
        totalEvictions.set(0);
        
        for (CacheStats stats : cacheStatistics.values()) {
            stats.reset();
        }
        
        log.info("Cache statistics reset");
    }

    /**
     * Get top performing caches
     */
    public List<CacheStats> getTopPerformingCaches(int limit) {
        return cacheStatistics.values().stream()
                .sorted((a, b) -> Double.compare(b.getHitRatio(), a.getHitRatio()))
                .limit(limit)
                .toList();
    }

    /**
     * Get poorly performing caches
     */
    public List<CacheStats> getPoorlyPerformingCaches(double hitRatioThreshold) {
        return cacheStatistics.values().stream()
                .filter(stats -> stats.getHitRatio() < hitRatioThreshold)
                .sorted((a, b) -> Double.compare(a.getHitRatio(), b.getHitRatio()))
                .toList();
    }

    /**
     * Cache Statistics Data Class
     */
    public static class CacheStats {
        private final String cacheName;
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong evictions = new AtomicLong(0);
        private final LocalDateTime createdAt;

        public CacheStats(String cacheName) {
            this.cacheName = cacheName;
            this.createdAt = LocalDateTime.now();
        }

        public void recordHit() {
            hits.incrementAndGet();
        }

        public void recordMiss() {
            misses.incrementAndGet();
        }

        public void recordEviction() {
            evictions.incrementAndGet();
        }

        public double getHitRatio() {
            long totalHits = hits.get();
            long totalMisses = misses.get();
            long total = totalHits + totalMisses;
            return total > 0 ? (double) totalHits / total : 0.0;
        }

        public long getTotalOperations() {
            return hits.get() + misses.get();
        }

        public void reset() {
            hits.set(0);
            misses.set(0);
            evictions.set(0);
        }

        // Getters
        public String getCacheName() { return cacheName; }
        public long getHits() { return hits.get(); }
        public long getMisses() { return misses.get(); }
        public long getEvictions() { return evictions.get(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * Redis Memory Information Data Class
     */
    public static class RedisMemoryInfo {
        private final long usedMemory;
        private final String usedMemoryHuman;
        private final long usedMemoryRss;
        private final long usedMemoryPeak;
        private final String usedMemoryPeakHuman;
        private final long totalSystemMemory;
        private final long maxMemory;
        private final String maxMemoryHuman;
        private final double memoryFragmentationRatio;

        private RedisMemoryInfo(Builder builder) {
            this.usedMemory = builder.usedMemory;
            this.usedMemoryHuman = builder.usedMemoryHuman;
            this.usedMemoryRss = builder.usedMemoryRss;
            this.usedMemoryPeak = builder.usedMemoryPeak;
            this.usedMemoryPeakHuman = builder.usedMemoryPeakHuman;
            this.totalSystemMemory = builder.totalSystemMemory;
            this.maxMemory = builder.maxMemory;
            this.maxMemoryHuman = builder.maxMemoryHuman;
            this.memoryFragmentationRatio = builder.memoryFragmentationRatio;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long usedMemory;
            private String usedMemoryHuman;
            private long usedMemoryRss;
            private long usedMemoryPeak;
            private String usedMemoryPeakHuman;
            private long totalSystemMemory;
            private long maxMemory;
            private String maxMemoryHuman;
            private double memoryFragmentationRatio;

            public Builder usedMemory(long usedMemory) { this.usedMemory = usedMemory; return this; }
            public Builder usedMemoryHuman(String usedMemoryHuman) { this.usedMemoryHuman = usedMemoryHuman; return this; }
            public Builder usedMemoryRss(long usedMemoryRss) { this.usedMemoryRss = usedMemoryRss; return this; }
            public Builder usedMemoryPeak(long usedMemoryPeak) { this.usedMemoryPeak = usedMemoryPeak; return this; }
            public Builder usedMemoryPeakHuman(String usedMemoryPeakHuman) { this.usedMemoryPeakHuman = usedMemoryPeakHuman; return this; }
            public Builder totalSystemMemory(long totalSystemMemory) { this.totalSystemMemory = totalSystemMemory; return this; }
            public Builder maxMemory(long maxMemory) { this.maxMemory = maxMemory; return this; }
            public Builder maxMemoryHuman(String maxMemoryHuman) { this.maxMemoryHuman = maxMemoryHuman; return this; }
            public Builder memoryFragmentationRatio(double memoryFragmentationRatio) { this.memoryFragmentationRatio = memoryFragmentationRatio; return this; }

            public RedisMemoryInfo build() {
                return new RedisMemoryInfo(this);
            }
        }

        // Getters
        public long getUsedMemory() { return usedMemory; }
        public String getUsedMemoryHuman() { return usedMemoryHuman; }
        public long getUsedMemoryRss() { return usedMemoryRss; }
        public long getUsedMemoryPeak() { return usedMemoryPeak; }
        public String getUsedMemoryPeakHuman() { return usedMemoryPeakHuman; }
        public long getTotalSystemMemory() { return totalSystemMemory; }
        public long getMaxMemory() { return maxMemory; }
        public String getMaxMemoryHuman() { return maxMemoryHuman; }
        public double getMemoryFragmentationRatio() { return memoryFragmentationRatio; }
    }

    /**
     * Redis Connection Information Data Class
     */
    public static class RedisConnectionInfo {
        private final int connectedClients;
        private final long clientRecentMaxInputBuffer;
        private final long clientRecentMaxOutputBuffer;
        private final int blockedClients;

        private RedisConnectionInfo(Builder builder) {
            this.connectedClients = builder.connectedClients;
            this.clientRecentMaxInputBuffer = builder.clientRecentMaxInputBuffer;
            this.clientRecentMaxOutputBuffer = builder.clientRecentMaxOutputBuffer;
            this.blockedClients = builder.blockedClients;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int connectedClients;
            private long clientRecentMaxInputBuffer;
            private long clientRecentMaxOutputBuffer;
            private int blockedClients;

            public Builder connectedClients(int connectedClients) { this.connectedClients = connectedClients; return this; }
            public Builder clientRecentMaxInputBuffer(long clientRecentMaxInputBuffer) { this.clientRecentMaxInputBuffer = clientRecentMaxInputBuffer; return this; }
            public Builder clientRecentMaxOutputBuffer(long clientRecentMaxOutputBuffer) { this.clientRecentMaxOutputBuffer = clientRecentMaxOutputBuffer; return this; }
            public Builder blockedClients(int blockedClients) { this.blockedClients = blockedClients; return this; }

            public RedisConnectionInfo build() {
                return new RedisConnectionInfo(this);
            }
        }

        // Getters
        public int getConnectedClients() { return connectedClients; }
        public long getClientRecentMaxInputBuffer() { return clientRecentMaxInputBuffer; }
        public long getClientRecentMaxOutputBuffer() { return clientRecentMaxOutputBuffer; }
        public int getBlockedClients() { return blockedClients; }
    }

    /**
     * Cache Performance Summary Data Class
     */
    public static class CachePerformanceSummary {
        private final double overallHitRatio;
        private final long totalHits;
        private final long totalMisses;
        private final long totalEvictions;
        private final Map<String, Double> cacheHitRatios;
        private final Map<String, Long> cacheTotalOperations;
        private final LocalDateTime timestamp;

        private CachePerformanceSummary(Builder builder) {
            this.overallHitRatio = builder.overallHitRatio;
            this.totalHits = builder.totalHits;
            this.totalMisses = builder.totalMisses;
            this.totalEvictions = builder.totalEvictions;
            this.cacheHitRatios = builder.cacheHitRatios;
            this.cacheTotalOperations = builder.cacheTotalOperations;
            this.timestamp = builder.timestamp;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private double overallHitRatio;
            private long totalHits;
            private long totalMisses;
            private long totalEvictions;
            private Map<String, Double> cacheHitRatios;
            private Map<String, Long> cacheTotalOperations;
            private LocalDateTime timestamp;

            public Builder overallHitRatio(double overallHitRatio) { this.overallHitRatio = overallHitRatio; return this; }
            public Builder totalHits(long totalHits) { this.totalHits = totalHits; return this; }
            public Builder totalMisses(long totalMisses) { this.totalMisses = totalMisses; return this; }
            public Builder totalEvictions(long totalEvictions) { this.totalEvictions = totalEvictions; return this; }
            public Builder cacheHitRatios(Map<String, Double> cacheHitRatios) { this.cacheHitRatios = cacheHitRatios; return this; }
            public Builder cacheTotalOperations(Map<String, Long> cacheTotalOperations) { this.cacheTotalOperations = cacheTotalOperations; return this; }
            public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

            public CachePerformanceSummary build() {
                return new CachePerformanceSummary(this);
            }
        }

        // Getters
        public double getOverallHitRatio() { return overallHitRatio; }
        public long getTotalHits() { return totalHits; }
        public long getTotalMisses() { return totalMisses; }
        public long getTotalEvictions() { return totalEvictions; }
        public Map<String, Double> getCacheHitRatios() { return cacheHitRatios; }
        public Map<String, Long> getCacheTotalOperations() { return cacheTotalOperations; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
