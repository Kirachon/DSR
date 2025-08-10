package ph.gov.dsr.performance.loadbalancer;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service instance performance metrics
 */
@Data
public class ServiceMetrics {
    private final String instanceId;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private volatile long minResponseTime = Long.MAX_VALUE;
    private volatile long maxResponseTime = 0;
    private volatile LocalDateTime lastRequestTime;
    private volatile LocalDateTime firstRequestTime;

    public ServiceMetrics(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Record a request with response time and success status
     */
    public synchronized void recordRequest(long responseTimeMs, boolean success) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTimeMs);
        
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
        
        // Update min/max response times
        if (responseTimeMs < minResponseTime) {
            minResponseTime = responseTimeMs;
        }
        if (responseTimeMs > maxResponseTime) {
            maxResponseTime = responseTimeMs;
        }
        
        lastRequestTime = LocalDateTime.now();
        if (firstRequestTime == null) {
            firstRequestTime = LocalDateTime.now();
        }
    }

    /**
     * Increment active connections
     */
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }

    /**
     * Decrement active connections
     */
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }

    /**
     * Get average response time
     */
    public double getAverageResponseTime() {
        long total = totalRequests.get();
        return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
    }

    /**
     * Get error rate as percentage
     */
    public double getErrorRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) failedRequests.get() / total * 100.0 : 0.0;
    }

    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) successfulRequests.get() / total * 100.0 : 0.0;
    }

    /**
     * Get requests per minute
     */
    public double getRequestsPerMinute() {
        if (firstRequestTime == null || lastRequestTime == null) {
            return 0.0;
        }
        
        long minutes = java.time.Duration.between(firstRequestTime, lastRequestTime).toMinutes();
        return minutes > 0 ? (double) totalRequests.get() / minutes : 0.0;
    }

    /**
     * Get throughput (requests per second)
     */
    public double getThroughput() {
        return getRequestsPerMinute() / 60.0;
    }

    /**
     * Check if instance is performing well
     */
    public boolean isPerformingWell() {
        return getErrorRate() < 5.0 && // Less than 5% error rate
               getAverageResponseTime() < 1000 && // Less than 1 second average response time
               activeConnections.get() < 100; // Less than 100 active connections
    }

    /**
     * Get performance score (0-100, higher is better)
     */
    public double getPerformanceScore() {
        double errorPenalty = getErrorRate() * 2; // 2 points per percent error rate
        double responsePenalty = Math.min(50, getAverageResponseTime() / 20); // Up to 50 points for response time
        double connectionPenalty = Math.min(20, activeConnections.get() / 5); // Up to 20 points for connections
        
        return Math.max(0, 100 - errorPenalty - responsePenalty - connectionPenalty);
    }

    /**
     * Get health status based on metrics
     */
    public String getHealthStatus() {
        double score = getPerformanceScore();
        
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        if (score >= 20) return "POOR";
        return "CRITICAL";
    }

    /**
     * Reset all metrics
     */
    public synchronized void reset() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        activeConnections.set(0);
        minResponseTime = Long.MAX_VALUE;
        maxResponseTime = 0;
        lastRequestTime = null;
        firstRequestTime = null;
    }

    /**
     * Get formatted statistics summary
     */
    public String getStatsSummary() {
        return String.format(
            "Requests: %d, Success Rate: %.1f%%, Avg Response: %.1fms, Active Connections: %d",
            totalRequests.get(),
            getSuccessRate(),
            getAverageResponseTime(),
            activeConnections.get()
        );
    }

    /**
     * Check if metrics are stale (no recent requests)
     */
    public boolean isStale(int minutes) {
        if (lastRequestTime == null) {
            return true;
        }
        
        return LocalDateTime.now().isAfter(lastRequestTime.plusMinutes(minutes));
    }

    /**
     * Get uptime in minutes since first request
     */
    public long getUptimeMinutes() {
        if (firstRequestTime == null) {
            return 0;
        }
        
        return java.time.Duration.between(firstRequestTime, LocalDateTime.now()).toMinutes();
    }
}
