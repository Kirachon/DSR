package ph.gov.dsr.performance.database;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Query performance metrics tracking
 */
@Data
public class QueryPerformanceMetrics {
    private final String queryId;
    private final String sql;
    private final AtomicInteger executionCount = new AtomicInteger(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong totalRowCount = new AtomicLong(0);
    private volatile long minExecutionTime = Long.MAX_VALUE;
    private volatile long maxExecutionTime = 0;
    private final List<Long> recentExecutionTimes = new ArrayList<>();
    private volatile LocalDateTime firstExecution;
    private volatile LocalDateTime lastExecution;
    
    public QueryPerformanceMetrics(String queryId, String sql) {
        this.queryId = queryId;
        this.sql = sql;
        this.firstExecution = LocalDateTime.now();
    }
    
    public synchronized void addExecution(long executionTimeMs) {
        addExecution(executionTimeMs, 0);
    }
    
    public synchronized void addExecution(long executionTimeMs, int rowCount) {
        executionCount.incrementAndGet();
        totalExecutionTime.addAndGet(executionTimeMs);
        totalRowCount.addAndGet(rowCount);
        
        // Update min/max
        if (executionTimeMs < minExecutionTime) {
            minExecutionTime = executionTimeMs;
        }
        if (executionTimeMs > maxExecutionTime) {
            maxExecutionTime = executionTimeMs;
        }
        
        // Keep recent execution times for trend analysis (last 100)
        recentExecutionTimes.add(executionTimeMs);
        if (recentExecutionTimes.size() > 100) {
            recentExecutionTimes.remove(0);
        }
        
        lastExecution = LocalDateTime.now();
    }
    
    public double getAverageExecutionTime() {
        int count = executionCount.get();
        return count > 0 ? (double) totalExecutionTime.get() / count : 0.0;
    }
    
    public double getAverageRowCount() {
        int count = executionCount.get();
        return count > 0 ? (double) totalRowCount.get() / count : 0.0;
    }
    
    public double getRecentAverageExecutionTime() {
        if (recentExecutionTimes.isEmpty()) {
            return 0.0;
        }
        
        return recentExecutionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
    }
    
    public String getPerformanceTrend() {
        if (recentExecutionTimes.size() < 10) {
            return "Insufficient data";
        }
        
        // Compare first half with second half of recent executions
        int halfSize = recentExecutionTimes.size() / 2;
        double firstHalfAvg = recentExecutionTimes.subList(0, halfSize).stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        double secondHalfAvg = recentExecutionTimes.subList(halfSize, recentExecutionTimes.size()).stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        double changePercent = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100;
        
        if (Math.abs(changePercent) < 5) {
            return "Stable";
        } else if (changePercent > 0) {
            return "Degrading (" + String.format("%.1f", changePercent) + "% slower)";
        } else {
            return "Improving (" + String.format("%.1f", Math.abs(changePercent)) + "% faster)";
        }
    }
    
    public boolean isSlowQuery(long thresholdMs) {
        return getAverageExecutionTime() > thresholdMs;
    }
    
    public boolean isFrequentQuery(int thresholdCount) {
        return executionCount.get() > thresholdCount;
    }
    
    public String getFormattedSql() {
        return sql != null && sql.length() > 200 ? 
            sql.substring(0, 200) + "..." : sql;
    }
}
