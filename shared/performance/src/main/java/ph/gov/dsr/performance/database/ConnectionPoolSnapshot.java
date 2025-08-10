package ph.gov.dsr.performance.database;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Point-in-time snapshot of connection pool state
 */
@Data
@Builder
public class ConnectionPoolSnapshot {
    private LocalDateTime timestamp;
    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private int threadsAwaitingConnection;
    private int maxPoolSize;
    private int minPoolSize;
    
    public double getUtilization() {
        return maxPoolSize > 0 ? (double) activeConnections / maxPoolSize : 0.0;
    }
    
    public String getFormattedTimestamp() {
        return timestamp.toString();
    }
    
    public boolean hasWaitingThreads() {
        return threadsAwaitingConnection > 0;
    }
    
    public boolean isFullyUtilized() {
        return activeConnections >= maxPoolSize;
    }
    
    public int getAvailableConnections() {
        return Math.max(0, maxPoolSize - activeConnections);
    }
}
