package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Trend Analysis Request DTO for trend analysis requests
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class TrendAnalysisRequest {
    private String metricName;
    private String analysisType; // LINEAR, EXPONENTIAL, POLYNOMIAL, SEASONAL, CYCLICAL
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String timeGranularity; // HOUR, DAY, WEEK, MONTH, QUARTER, YEAR
    private String aggregation; // SUM, AVG, COUNT, MIN, MAX
    private Map<String, Object> filters;
    private List<String> groupByDimensions;
    private String trendMethod; // LEAST_SQUARES, MOVING_AVERAGE, EXPONENTIAL_SMOOTHING
    private Integer movingAverageWindow;
    private Double smoothingFactor;
    private Boolean detectSeasonality;
    private Boolean detectOutliers;
    private Boolean detectChangePoints;
    private Integer forecastHorizon;
    private String forecastMethod; // ARIMA, EXPONENTIAL_SMOOTHING, LINEAR_REGRESSION
    private Double confidenceLevel; // 0.90, 0.95, 0.99
    private Boolean includeStatistics;
    private Boolean includeVisualization;
    private String outputFormat; // JSON, CSV, CHART
    private String userId;
    private Map<String, Object> parameters;

    // Additional fields for BusinessIntelligenceService compatibility
    private List<String> metrics;
    private String timeRange;
    private Map<String, Object> trendDetectionParameters;
    private Map<String, Object> seasonalityParameters;
    private Map<String, Object> forecastParameters;
}
