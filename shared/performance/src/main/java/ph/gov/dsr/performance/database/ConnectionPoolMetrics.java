package ph.gov.dsr.performance.database;

import lombok.Builder;
import lombok.Data;

/**
 * Connection pool performance metrics
 */
@Data
@Builder
public class ConnectionPoolMetrics {
    private boolean healthy;
    private String errorMessage;
    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private int threadsAwaitingConnection;
    private int maxPoolSize;
    private int minPoolSize;
    private long connectionTimeout;
    private long idleTimeout;
    private long maxLifetime;
    private long leakDetectionThreshold;
    private double averageActiveConnections;
    private long averageWaitTime;
    private double poolUtilization;
    
    public String getHealthStatus() {
        if (!healthy) return "UNHEALTHY";
        
        if (poolUtilization > 0.9) return "CRITICAL";
        if (poolUtilization > 0.8 || threadsAwaitingConnection > 0) return "WARNING";
        return "HEALTHY";
    }
    
    public String getUtilizationLevel() {
        if (poolUtilization > 0.8) return "High";
        if (poolUtilization > 0.5) return "Medium";
        if (poolUtilization > 0.2) return "Low";
        return "Very Low";
    }
    
    public boolean hasPerformanceIssues() {
        return !healthy || 
               poolUtilization > 0.9 || 
               threadsAwaitingConnection > 5 || 
               averageWaitTime > 1000;
    }
    
    public String getFormattedUtilization() {
        return String.format("%.1f%%", poolUtilization * 100);
    }
    
    public String getConnectionSummary() {
        return String.format("%d/%d active, %d idle, %d waiting", 
            activeConnections, maxPoolSize, idleConnections, threadsAwaitingConnection);
    }
}
