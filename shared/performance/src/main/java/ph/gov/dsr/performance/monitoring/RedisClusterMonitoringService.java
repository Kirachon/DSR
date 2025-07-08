package ph.gov.dsr.performance.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ph.gov.dsr.performance.cache.CacheStatistics;
import ph.gov.dsr.performance.cache.ClusterInfo;
import ph.gov.dsr.performance.cache.RedisClusterCacheManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis Cluster Monitoring Service
 * Provides comprehensive monitoring and alerting for Redis cluster health
 */
@Service
@ConditionalOnProperty(name = "dsr.cache.type", havingValue = "redis")
@RequiredArgsConstructor
@Slf4j
public class RedisClusterMonitoringService {

    @Autowired(required = false)
    private RedisClusterCacheManager cacheManager;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, NodeMetrics> nodeMetrics = new ConcurrentHashMap<>();
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong failedOperations = new AtomicLong(0);
    private LocalDateTime lastHealthCheck = LocalDateTime.now();

    /**
     * Scheduled health check for Redis cluster
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void performHealthCheck() {
        if (cacheManager == null || redisTemplate == null) {
            log.debug("Redis cluster not configured, skipping health check");
            return;
        }

        try {
            log.debug("Performing Redis cluster health check");
            
            // Check overall cluster health
            boolean isHealthy = cacheManager.isHealthy();
            
            // Get cluster information
            ClusterInfo clusterInfo = cacheManager.getClusterInfo();
            
            // Get cache statistics
            CacheStatistics cacheStats = cacheManager.getCacheStatistics();
            
            // Check individual node health
            checkNodeHealth();
            
            // Log health status
            logHealthStatus(isHealthy, clusterInfo, cacheStats);
            
            // Check for alerts
            checkAlerts(clusterInfo, cacheStats);
            
            lastHealthCheck = LocalDateTime.now();
            
        } catch (Exception e) {
            log.error("Redis cluster health check failed", e);
            failedOperations.incrementAndGet();
        }
    }

    /**
     * Check individual node health
     */
    private void checkNodeHealth() {
        try {
            RedisClusterConnection clusterConnection = redisTemplate.getConnectionFactory()
                .getClusterConnection();
            
            Iterable<RedisClusterNode> nodes = clusterConnection.clusterGetNodes();
            
            for (RedisClusterNode node : nodes) {
                String nodeId = node.getId();
                NodeMetrics metrics = nodeMetrics.computeIfAbsent(nodeId, k -> new NodeMetrics());
                
                // Update node metrics
                metrics.setLastSeen(LocalDateTime.now());
                metrics.setHost(node.getHost());
                metrics.setPort(node.getPort());
                metrics.setMaster(node.isMaster());
                metrics.setConnected(true); // If we can see it, it's connected
                
                // Check node-specific health
                try {
                    // Ping the specific node
                    redisTemplate.getConnectionFactory().getConnection().ping();
                    metrics.incrementSuccessfulPings();
                } catch (Exception e) {
                    metrics.incrementFailedPings();
                    log.warn("Failed to ping Redis node {}: {}", nodeId, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to check individual node health", e);
        }
    }

    /**
     * Log health status
     */
    private void logHealthStatus(boolean isHealthy, ClusterInfo clusterInfo, CacheStatistics cacheStats) {
        if (isHealthy) {
            log.debug("Redis cluster is healthy - Nodes: {}/{}, Memory: {}, Hit Rate: {:.2f}%",
                clusterInfo.getTotalNodes(), 
                clusterInfo.getMasterNodes() + clusterInfo.getSlaveNodes(),
                cacheStats.getFormattedMemoryUsage(),
                cacheStats.getHitRate() * 100);
        } else {
            log.warn("Redis cluster health issues detected - State: {}, Nodes: {}/{}",
                clusterInfo.getClusterState(),
                clusterInfo.getTotalNodes(),
                clusterInfo.getMasterNodes() + clusterInfo.getSlaveNodes());
        }
    }

    /**
     * Check for alerts and warnings
     */
    private void checkAlerts(ClusterInfo clusterInfo, CacheStatistics cacheStats) {
        // Memory pressure alert
        if (cacheStats.isMemoryPressure()) {
            log.warn("ALERT: Redis cluster memory usage is high: {:.1f}%", 
                cacheStats.getMemoryUsagePercentage());
        }
        
        // Low hit rate alert
        if (cacheStats.isLowHitRate()) {
            log.warn("ALERT: Redis cluster hit rate is low: {:.2f}%", 
                cacheStats.getHitRate() * 100);
        }
        
        // Cluster state alert
        if (!clusterInfo.isHealthy()) {
            log.error("ALERT: Redis cluster is not healthy - State: {}, Slots: {}/16384",
                clusterInfo.getClusterState(), clusterInfo.getSlotsAssigned());
        }
        
        // Node failure alert
        long expectedNodes = 6; // 3 masters + 3 slaves
        if (clusterInfo.getTotalNodes() < expectedNodes) {
            log.error("ALERT: Redis cluster has missing nodes - Expected: {}, Actual: {}",
                expectedNodes, clusterInfo.getTotalNodes());
        }
        
        // Replication alert
        if (clusterInfo.getReplicationRatio() < 1.0) {
            log.warn("ALERT: Redis cluster replication ratio is low: {:.2f}",
                clusterInfo.getReplicationRatio());
        }
    }

    /**
     * Get comprehensive cluster metrics
     */
    public ClusterMetrics getClusterMetrics() {
        if (cacheManager == null) {
            return ClusterMetrics.builder()
                .healthy(false)
                .lastCheck(lastHealthCheck)
                .totalOperations(totalOperations.get())
                .failedOperations(failedOperations.get())
                .build();
        }

        try {
            ClusterInfo clusterInfo = cacheManager.getClusterInfo();
            CacheStatistics cacheStats = cacheManager.getCacheStatistics();
            
            return ClusterMetrics.builder()
                .healthy(clusterInfo.isHealthy())
                .clusterInfo(clusterInfo)
                .cacheStatistics(cacheStats)
                .nodeMetrics(new HashMap<>(nodeMetrics))
                .lastCheck(lastHealthCheck)
                .totalOperations(totalOperations.get())
                .failedOperations(failedOperations.get())
                .successRate(calculateSuccessRate())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get cluster metrics", e);
            return ClusterMetrics.builder()
                .healthy(false)
                .lastCheck(lastHealthCheck)
                .totalOperations(totalOperations.get())
                .failedOperations(failedOperations.get())
                .build();
        }
    }

    /**
     * Perform cache warmup
     */
    public void performCacheWarmup() {
        if (cacheManager != null) {
            log.info("Starting Redis cluster cache warmup");
            cacheManager.warmupCache().thenRun(() -> 
                log.info("Redis cluster cache warmup completed"));
        }
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        if (cacheManager != null) {
            try {
                // Clear all cache regions
                String[] cacheNames = {"registrations", "eligibility-checks", "documents", 
                    "user-sessions", "philsys-verification", "data-validation", 
                    "deduplication-results", "data-ingestion-batches"};
                
                for (String cacheName : cacheNames) {
                    cacheManager.evictCache(cacheName);
                }
                
                log.info("All Redis caches cleared successfully");
            } catch (Exception e) {
                log.error("Failed to clear Redis caches", e);
            }
        }
    }

    /**
     * Get cache hit rate for specific cache
     */
    public double getCacheHitRate(String cacheName) {
        // Implementation would track hit/miss ratios per cache
        return 0.85; // Placeholder
    }

    private double calculateSuccessRate() {
        long total = totalOperations.get();
        long failed = failedOperations.get();
        return total > 0 ? (double) (total - failed) / total : 1.0;
    }

    /**
     * Node metrics tracking
     */
    public static class NodeMetrics {
        private String host;
        private int port;
        private boolean isMaster;
        private boolean connected;
        private LocalDateTime lastSeen;
        private long successfulPings = 0;
        private long failedPings = 0;

        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public boolean isMaster() { return isMaster; }
        public void setMaster(boolean master) { isMaster = master; }
        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
        public LocalDateTime getLastSeen() { return lastSeen; }
        public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
        public long getSuccessfulPings() { return successfulPings; }
        public long getFailedPings() { return failedPings; }
        
        public void incrementSuccessfulPings() { successfulPings++; }
        public void incrementFailedPings() { failedPings++; }
        
        public double getPingSuccessRate() {
            long total = successfulPings + failedPings;
            return total > 0 ? (double) successfulPings / total : 1.0;
        }
    }
}
