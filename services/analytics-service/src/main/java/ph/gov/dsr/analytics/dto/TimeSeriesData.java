package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Time Series Data DTO for temporal data analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class TimeSeriesData {
    private String seriesId;
    private String seriesName;
    private String description;
    private String metricName;
    private String unit;
    private String dataType; // NUMERIC, CATEGORICAL, BOOLEAN
    
    // Time configuration
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String frequency; // SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, QUARTER, YEAR
    private String timeZone;
    private Integer intervalMinutes;
    
    // Data points
    private List<TimeSeriesPoint> dataPoints;
    private Integer totalPoints;
    private Integer validPoints;
    private Integer missingPoints;
    private Double completeness;
    
    // Statistical summary
    private Double minimum;
    private Double maximum;
    private Double mean;
    private Double median;
    private Double standardDeviation;
    private Double variance;
    private Double sum;
    private Double range;
    private Double firstValue;
    private Double lastValue;
    
    // Trend analysis
    private String trendDirection; // INCREASING, DECREASING, STABLE, VOLATILE
    private Double trendSlope;
    private Double trendCorrelation;
    private String trendStrength; // STRONG, MODERATE, WEAK, NONE
    
    // Seasonality
    private Boolean hasSeasonality;
    private String seasonalPattern; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Double seasonalStrength;
    private Map<String, Double> seasonalFactors;
    
    // Change analysis
    private Double totalChange;
    private Double percentageChange;
    private Double averageChange;
    private Double changeRate;
    private List<ChangePoint> significantChanges;
    
    // Anomaly detection
    private List<Anomaly> anomalies;
    private Integer anomalyCount;
    private Double anomalyThreshold;
    private String anomalyMethod;
    
    // Data quality
    private String dataQuality; // HIGH, MEDIUM, LOW
    private List<String> qualityIssues;
    private Double outlierPercentage;
    private List<Gap> dataGaps;
    
    // Aggregation options
    private Map<String, Object> aggregatedData;
    private String aggregationMethod; // SUM, AVERAGE, COUNT, MIN, MAX
    private String aggregationPeriod;
    
    // Forecasting support
    private Boolean supportsForecast;
    private String forecastMethod;
    private Integer forecastHorizon;
    private List<ForecastPoint> forecast;
    
    // Comparison data
    private TimeSeriesData comparisonSeries;
    private String comparisonType; // PREVIOUS_PERIOD, SAME_PERIOD_LAST_YEAR, BASELINE
    private Double correlation;
    private Map<String, Object> comparisonMetrics;
    
    // Metadata
    private Map<String, Object> metadata;
    private String dataSource;
    private String query;
    private Map<String, Object> filters;
    private LocalDateTime generatedAt;
    private String generatedBy;
    
    @Data
    @Builder
    public static class TimeSeriesPoint {
        private LocalDateTime timestamp;
        private Double value;
        private String formattedValue;
        private Boolean isValid;
        private Boolean isMissing;
        private Boolean isOutlier;
        private Boolean isAnomaly;
        private Map<String, Object> attributes;
        private String quality; // GOOD, QUESTIONABLE, BAD
    }
    
    @Data
    @Builder
    public static class ChangePoint {
        private LocalDateTime timestamp;
        private Double beforeValue;
        private Double afterValue;
        private Double changeAmount;
        private Double changePercentage;
        private String changeType; // LEVEL_SHIFT, TREND_CHANGE, VARIANCE_CHANGE
        private Double confidence;
        private String significance; // HIGH, MEDIUM, LOW
    }
    
    @Data
    @Builder
    public static class Anomaly {
        private LocalDateTime timestamp;
        private Double value;
        private Double expectedValue;
        private Double deviation;
        private Double severity;
        private String type; // POINT, CONTEXTUAL, COLLECTIVE
        private String description;
        private Double confidence;
    }
    
    @Data
    @Builder
    public static class Gap {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer missingPoints;
        private String gapType; // MISSING, INVALID, CORRUPTED
        private String reason;
    }
    
    @Data
    @Builder
    public static class ForecastPoint {
        private LocalDateTime timestamp;
        private Double forecastValue;
        private Double lowerBound;
        private Double upperBound;
        private Double confidence;
        private String method;
    }
}
