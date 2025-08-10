package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Metric Data DTO for analytical metrics and calculations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class MetricData {
    private String metricName;
    private String metricCode;
    private String description;
    private String category; // PERFORMANCE, QUALITY, USAGE, FINANCIAL, OPERATIONAL
    private String type; // COUNT, SUM, AVERAGE, PERCENTAGE, RATIO, RATE
    
    // Current value
    private Object value;
    private String formattedValue;
    private String unit;
    private String format;
    private Integer decimalPlaces;
    
    // Historical values
    private Object previousValue;
    private Object previousPeriodValue;
    private Object yearOverYearValue;
    private List<TimeSeriesPoint> historicalData;
    
    // Change analysis
    private Double changeAmount;
    private Double changePercentage;
    private String changeDirection; // UP, DOWN, STABLE
    private String changeTrend; // INCREASING, DECREASING, STABLE, VOLATILE
    private String changeSignificance; // SIGNIFICANT, MODERATE, MINOR, NEGLIGIBLE
    
    // Comparison data
    private Object targetValue;
    private Object benchmarkValue;
    private Object industryAverage;
    private Double targetAchievement; // Percentage of target achieved
    private String performanceRating; // EXCELLENT, GOOD, AVERAGE, POOR, CRITICAL
    
    // Thresholds and alerts
    private Double warningThreshold;
    private Double criticalThreshold;
    private String thresholdType; // ABOVE, BELOW, BETWEEN, OUTSIDE
    private String alertStatus; // NORMAL, WARNING, CRITICAL
    private Boolean hasAlert;
    private String alertMessage;
    
    // Time context
    private LocalDateTime calculatedAt;
    private LocalDateTime dataAsOf;
    private String timePeriod; // CURRENT, LAST_24H, LAST_7D, LAST_30D, LAST_90D, YTD, MTD
    private String timeGranularity; // REAL_TIME, HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    
    // Data quality
    private Double confidence; // Confidence level in the metric (0-1)
    private String dataQuality; // HIGH, MEDIUM, LOW
    private Integer sampleSize;
    private Double marginOfError;
    private List<String> dataQualityIssues;
    
    // Calculation details
    private String calculationMethod;
    private String formula;
    private Map<String, Object> calculationParameters;
    private List<String> dataSources;
    private Map<String, Object> filters;
    
    // Statistical measures
    private Double standardDeviation;
    private Double variance;
    private Double minimum;
    private Double maximum;
    private Double median;
    private Double percentile25;
    private Double percentile75;
    private Double percentile90;
    private Double percentile95;
    
    // Trend analysis
    private String trendDirection; // UPWARD, DOWNWARD, STABLE, CYCLICAL
    private Double trendSlope;
    private Double trendCorrelation;
    private String seasonality; // NONE, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Map<String, Object> trendAnalysis;
    
    // Drill-down capabilities
    private Boolean supportsDrillDown;
    private List<String> drillDownDimensions;
    private Map<String, Object> drillDownData;
    
    // Metadata
    private Map<String, Object> metadata;
    private String owner;
    private String businessDefinition;
    private String technicalDefinition;
    private List<String> tags;
    private String status; // ACTIVE, DEPRECATED, EXPERIMENTAL
    
    @Data
    @Builder
    public static class TimeSeriesPoint {
        private LocalDateTime timestamp;
        private Object value;
        private String formattedValue;
        private Map<String, Object> metadata;
    }
}
