package ph.gov.dsr.performance.monitoring;

import lombok.Builder;
import lombok.Data;
import ph.gov.dsr.performance.cache.CacheStatistics;
import ph.gov.dsr.performance.cache.ClusterInfo;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Comprehensive Redis cluster metrics
 */
@Data
@Builder
public class ClusterMetrics {
    private boolean healthy;
    private ClusterInfo clusterInfo;
    private CacheStatistics cacheStatistics;
    private Map<String, RedisClusterMonitoringService.NodeMetrics> nodeMetrics;
    private LocalDateTime lastCheck;
    private long totalOperations;
    private long failedOperations;
    private double successRate;
    
    public boolean hasIssues() {
        return !healthy || 
               (cacheStatistics != null && cacheStatistics.isMemoryPressure()) ||
               (cacheStatistics != null && cacheStatistics.isLowHitRate()) ||
               successRate < 0.95;
    }
    
    public String getHealthStatus() {
        if (!healthy) return "CRITICAL";
        if (hasIssues()) return "WARNING";
        return "HEALTHY";
    }
    
    public int getActiveNodes() {
        return nodeMetrics != null ? 
            (int) nodeMetrics.values().stream().filter(m -> m.isConnected()).count() : 0;
    }
    
    public int getMasterNodes() {
        return nodeMetrics != null ? 
            (int) nodeMetrics.values().stream().filter(m -> m.isMaster() && m.isConnected()).count() : 0;
    }
    
    public int getSlaveNodes() {
        return getActiveNodes() - getMasterNodes();
    }
}
