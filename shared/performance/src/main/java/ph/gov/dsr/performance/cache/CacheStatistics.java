package ph.gov.dsr.performance.cache;

import lombok.Builder;
import lombok.Data;

/**
 * Cache performance statistics
 */
@Data
@Builder
public class CacheStatistics {
    private long usedMemory;
    private long maxMemory;
    private int totalKeys;
    private double hitRate;
    private long evictionCount;
    private long timestamp;
    
    public static class CacheStatisticsBuilder {
        public CacheStatisticsBuilder() {
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public double getMemoryUsagePercentage() {
        return maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0.0;
    }
    
    public String getFormattedMemoryUsage() {
        return formatBytes(usedMemory) + " / " + formatBytes(maxMemory);
    }
    
    public boolean isMemoryPressure() {
        return getMemoryUsagePercentage() > 80.0;
    }
    
    public boolean isLowHitRate() {
        return hitRate < 0.8; // Less than 80% hit rate
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
