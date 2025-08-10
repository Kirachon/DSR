package ph.gov.dsr.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Result of trend detection analysis
 * 
 * @author DSR Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class TrendDetectionResult {
    
    /**
     * Metric being analyzed
     */
    private String metric;
    
    /**
     * Overall trend direction
     */
    private TrendDirection direction;
    
    /**
     * Trend strength (0-1)
     */
    private double strength;
    
    /**
     * Trend significance (p-value)
     */
    private double significance;
    
    /**
     * Confidence level
     */
    private double confidence;
    
    /**
     * Detected change points
     */
    private List<ChangePoint> changePoints;
    
    /**
     * Trend segments
     */
    private List<TrendSegment> segments;
    
    /**
     * Analysis timestamp
     */
    private LocalDateTime analyzedAt;
    
    /**
     * Statistical details
     */
    private Map<String, Object> statistics;
    
    /**
     * Trend direction enumeration
     */
    public enum TrendDirection {
        INCREASING,
        DECREASING,
        STABLE,
        VOLATILE,
        UNKNOWN
    }
    
    /**
     * Change point in the time series
     */
    @Data
    @Builder
    public static class ChangePoint {
        private LocalDateTime timestamp;
        private double confidence;
        private String changeType; // LEVEL, TREND, VARIANCE
        private double magnitude;
        private String description;
    }
    
    /**
     * Trend segment between change points
     */
    @Data
    @Builder
    public static class TrendSegment {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private TrendDirection direction;
        private double slope;
        private double rSquared;
        private double confidence;
    }
    
    /**
     * Check if the trend is statistically significant
     */
    public boolean isSignificant() {
        return significance < 0.05; // p-value < 0.05
    }
    
    /**
     * Check if the trend is strong
     */
    public boolean isStrongTrend() {
        return strength > 0.7 && isSignificant();
    }
    
    /**
     * Get the most recent trend direction
     */
    public TrendDirection getRecentTrend() {
        if (segments == null || segments.isEmpty()) {
            return direction;
        }
        
        return segments.get(segments.size() - 1).getDirection();
    }
    
    /**
     * Get trend summary description
     */
    public String getTrendSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(direction.name().toLowerCase());
        
        if (isStrongTrend()) {
            summary.append(" (strong)");
        } else if (isSignificant()) {
            summary.append(" (moderate)");
        } else {
            summary.append(" (weak)");
        }
        
        return summary.toString();
    }
}
