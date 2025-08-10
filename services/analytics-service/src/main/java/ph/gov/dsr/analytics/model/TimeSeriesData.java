package ph.gov.dsr.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Time series data model for trend analysis
 * 
 * @author DSR Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class TimeSeriesData {
    
    /**
     * Metric name/identifier
     */
    private String metric;
    
    /**
     * Time series data points
     */
    private List<TimeSeriesPoint> dataPoints;
    
    /**
     * Data frequency (HOURLY, DAILY, WEEKLY, MONTHLY)
     */
    private String frequency;
    
    /**
     * Start time of the series
     */
    private LocalDateTime startTime;
    
    /**
     * End time of the series
     */
    private LocalDateTime endTime;
    
    /**
     * Data source information
     */
    private String dataSource;
    
    /**
     * Metadata about the time series
     */
    private Map<String, Object> metadata;
    
    /**
     * Statistical summary
     */
    private TimeSeriesStatistics statistics;
    
    /**
     * Individual time series data point
     */
    @Data
    @Builder
    public static class TimeSeriesPoint {
        private LocalDateTime timestamp;
        private double value;
        private Map<String, Object> attributes;
        private DataQuality quality;
    }
    
    /**
     * Statistical summary of time series
     */
    @Data
    @Builder
    public static class TimeSeriesStatistics {
        private double mean;
        private double median;
        private double standardDeviation;
        private double variance;
        private double min;
        private double max;
        private int count;
        private double trend; // positive = increasing, negative = decreasing
        private double seasonality; // 0-1 indicating seasonal strength
    }
    
    /**
     * Data quality assessment
     */
    @Data
    @Builder
    public static class DataQuality {
        private boolean isValid;
        private double confidence;
        private List<String> issues;
    }
    
    /**
     * Get the number of data points
     */
    public int getPointCount() {
        return dataPoints != null ? dataPoints.size() : 0;
    }
    
    /**
     * Get data points within a specific time range
     */
    public List<TimeSeriesPoint> getPointsInRange(LocalDateTime start, LocalDateTime end) {
        if (dataPoints == null) return List.of();
        
        return dataPoints.stream()
                .filter(point -> !point.getTimestamp().isBefore(start))
                .filter(point -> !point.getTimestamp().isAfter(end))
                .toList();
    }
    
    /**
     * Get the latest data point
     */
    public TimeSeriesPoint getLatestPoint() {
        if (dataPoints == null || dataPoints.isEmpty()) return null;
        
        return dataPoints.stream()
                .max((p1, p2) -> p1.getTimestamp().compareTo(p2.getTimestamp()))
                .orElse(null);
    }
    
    /**
     * Check if the time series has sufficient data for analysis
     */
    public boolean hasSufficientData() {
        return getPointCount() >= 10; // Minimum 10 points for meaningful analysis
    }
    
    /**
     * Calculate the time span of the series in days
     */
    public long getTimeSpanDays() {
        if (startTime == null || endTime == null) return 0;
        return java.time.Duration.between(startTime, endTime).toDays();
    }
}
