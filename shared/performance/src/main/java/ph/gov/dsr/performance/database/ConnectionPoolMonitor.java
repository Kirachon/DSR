package ph.gov.dsr.performance.database;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Connection pool monitoring and optimization
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionPoolMonitor {

    @Autowired(required = false)
    private DataSource dataSource;

    private final List<ConnectionPoolSnapshot> snapshots = new ArrayList<>();
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    private final AtomicLong connectionRequests = new AtomicLong(0);

    /**
     * Monitor connection pool metrics
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void monitorConnectionPool() {
        if (dataSource == null) {
            return;
        }

        try {
            ConnectionPoolMetrics metrics = getCurrentMetrics();
            
            // Create snapshot
            ConnectionPoolSnapshot snapshot = ConnectionPoolSnapshot.builder()
                .timestamp(LocalDateTime.now())
                .activeConnections(metrics.getActiveConnections())
                .idleConnections(metrics.getIdleConnections())
                .totalConnections(metrics.getTotalConnections())
                .threadsAwaitingConnection(metrics.getThreadsAwaitingConnection())
                .maxPoolSize(metrics.getMaxPoolSize())
                .minPoolSize(metrics.getMinPoolSize())
                .build();

            // Store snapshot (keep last 100)
            synchronized (snapshots) {
                snapshots.add(snapshot);
                if (snapshots.size() > 100) {
                    snapshots.remove(0);
                }
            }

            // Log warnings for potential issues
            checkForIssues(metrics);

        } catch (Exception e) {
            log.error("Failed to monitor connection pool", e);
        }
    }

    /**
     * Get current connection pool metrics
     */
    public ConnectionPoolMetrics getCurrentMetrics() {
        if (dataSource == null || !(dataSource instanceof HikariDataSource)) {
            return ConnectionPoolMetrics.builder()
                .healthy(false)
                .errorMessage("DataSource not available or not HikariCP")
                .build();
        }

        try {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();

            if (poolBean == null) {
                return ConnectionPoolMetrics.builder()
                    .healthy(false)
                    .errorMessage("HikariPoolMXBean not available")
                    .build();
            }

            return ConnectionPoolMetrics.builder()
                .healthy(true)
                .activeConnections(poolBean.getActiveConnections())
                .idleConnections(poolBean.getIdleConnections())
                .totalConnections(poolBean.getTotalConnections())
                .threadsAwaitingConnection(poolBean.getThreadsAwaitingConnection())
                .maxPoolSize(hikariDataSource.getMaximumPoolSize())
                .minPoolSize(hikariDataSource.getMinimumIdle())
                .connectionTimeout(hikariDataSource.getConnectionTimeout())
                .idleTimeout(hikariDataSource.getIdleTimeout())
                .maxLifetime(hikariDataSource.getMaxLifetime())
                .leakDetectionThreshold(hikariDataSource.getLeakDetectionThreshold())
                .averageActiveConnections(calculateAverageActiveConnections())
                .averageWaitTime(calculateAverageWaitTime())
                .poolUtilization(calculatePoolUtilization(poolBean))
                .build();

        } catch (Exception e) {
            log.error("Failed to get connection pool metrics", e);
            return ConnectionPoolMetrics.builder()
                .healthy(false)
                .errorMessage("Error retrieving metrics: " + e.getMessage())
                .build();
        }
    }

    /**
     * Get connection pool performance history
     */
    public List<ConnectionPoolSnapshot> getPerformanceHistory() {
        synchronized (snapshots) {
            return new ArrayList<>(snapshots);
        }
    }

    /**
     * Get connection pool recommendations
     */
    public List<String> getOptimizationRecommendations() {
        List<String> recommendations = new ArrayList<>();
        ConnectionPoolMetrics metrics = getCurrentMetrics();

        if (!metrics.isHealthy()) {
            recommendations.add("Connection pool is not healthy: " + metrics.getErrorMessage());
            return recommendations;
        }

        // High utilization
        if (metrics.getPoolUtilization() > 0.8) {
            recommendations.add("Pool utilization is high (" + 
                String.format("%.1f", metrics.getPoolUtilization() * 100) + 
                "%). Consider increasing maximum pool size.");
        }

        // Threads waiting
        if (metrics.getThreadsAwaitingConnection() > 0) {
            recommendations.add("Threads are waiting for connections (" + 
                metrics.getThreadsAwaitingConnection() + 
                "). Consider increasing pool size or optimizing query performance.");
        }

        // High average wait time
        if (metrics.getAverageWaitTime() > 100) {
            recommendations.add("Average connection wait time is high (" + 
                metrics.getAverageWaitTime() + "ms). Consider tuning pool configuration.");
        }

        // Low utilization
        if (metrics.getPoolUtilization() < 0.2 && metrics.getMaxPoolSize() > 5) {
            recommendations.add("Pool utilization is low (" + 
                String.format("%.1f", metrics.getPoolUtilization() * 100) + 
                "%). Consider reducing maximum pool size to save resources.");
        }

        // Connection timeout issues
        if (metrics.getConnectionTimeout() < 10000) {
            recommendations.add("Connection timeout is low (" + 
                metrics.getConnectionTimeout() + "ms). Consider increasing for better reliability.");
        }

        // Leak detection
        if (metrics.getLeakDetectionThreshold() == 0) {
            recommendations.add("Connection leak detection is disabled. Enable it for better monitoring.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Connection pool configuration appears optimal.");
        }

        return recommendations;
    }

    /**
     * Record connection request
     */
    public void recordConnectionRequest(long waitTimeMs) {
        connectionRequests.incrementAndGet();
        totalWaitTime.addAndGet(waitTimeMs);
    }

    private void checkForIssues(ConnectionPoolMetrics metrics) {
        // High utilization warning
        if (metrics.getPoolUtilization() > 0.9) {
            log.warn("Connection pool utilization is very high: {:.1f}%", 
                metrics.getPoolUtilization() * 100);
        }

        // Threads waiting warning
        if (metrics.getThreadsAwaitingConnection() > 5) {
            log.warn("Many threads waiting for connections: {}", 
                metrics.getThreadsAwaitingConnection());
        }

        // No idle connections warning
        if (metrics.getIdleConnections() == 0 && metrics.getActiveConnections() > 0) {
            log.warn("No idle connections available in pool");
        }
    }

    private double calculateAverageActiveConnections() {
        synchronized (snapshots) {
            if (snapshots.isEmpty()) {
                return 0.0;
            }

            return snapshots.stream()
                .mapToInt(ConnectionPoolSnapshot::getActiveConnections)
                .average()
                .orElse(0.0);
        }
    }

    private long calculateAverageWaitTime() {
        long requests = connectionRequests.get();
        return requests > 0 ? totalWaitTime.get() / requests : 0;
    }

    private double calculatePoolUtilization(HikariPoolMXBean poolBean) {
        int maxSize = poolBean.getTotalConnections();
        int active = poolBean.getActiveConnections();
        return maxSize > 0 ? (double) active / maxSize : 0.0;
    }

    /**
     * Reset monitoring statistics
     */
    public void resetStatistics() {
        synchronized (snapshots) {
            snapshots.clear();
        }
        totalConnections.set(0);
        totalWaitTime.set(0);
        connectionRequests.set(0);
        log.info("Connection pool monitoring statistics reset");
    }
}
