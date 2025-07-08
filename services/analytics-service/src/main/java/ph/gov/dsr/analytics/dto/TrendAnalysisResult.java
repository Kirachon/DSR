package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Trend Analysis Result DTO for trend analysis and forecasting
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class TrendAnalysisResult {
    private String analysisId;
    private String metricName;
    private String analysisType; // LINEAR, EXPONENTIAL, POLYNOMIAL, SEASONAL, CYCLICAL
    private LocalDateTime analysisDate;
    private String timePeriod;
    private String timeGranularity;

    // Additional fields for BusinessIntelligenceService compatibility
    private List<String> metrics;
    private Map<String, TimeSeriesData> historicalData;
    private Map<String, TrendDetectionResult> trendDetection;
    private Map<String, SeasonalityAnalysis> seasonalityAnalysis;
    private Map<String, ForecastResult> forecasts;
    private Map<String, ForecastAccuracy> forecastAccuracy;
    private LocalDateTime analysisTimestamp;
    
    // Trend characteristics
    private String trendDirection; // UPWARD, DOWNWARD, STABLE, VOLATILE
    private Double trendSlope;
    private Double trendIntercept;
    private String trendStrength; // STRONG, MODERATE, WEAK, NONE
    private Double trendCorrelation;
    private Double rSquared;
    private String trendEquation;
    
    // Statistical measures
    private Double mean;
    private Double median;
    private Double standardDeviation;
    private Double variance;
    private Double minimum;
    private Double maximum;
    private Double range;
    private Double skewness;
    private Double kurtosis;
    
    // Trend components
    private TrendComponent trendComponent;
    private SeasonalComponent seasonalComponent;
    private CyclicalComponent cyclicalComponent;
    private IrregularComponent irregularComponent;
    
    // Change analysis
    private Double totalChange;
    private Double averageChange;
    private Double changeRate;
    private Double accelerationRate;
    private List<ChangePoint> changePoints;
    private List<Outlier> outliers;
    
    // Seasonality analysis
    private Boolean hasSeasonality;
    private String seasonalPattern; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Double seasonalStrength;
    private Map<String, Double> seasonalFactors;
    private List<SeasonalPeak> seasonalPeaks;
    
    // Forecasting
    private List<ForecastPoint> forecast;
    private Integer forecastHorizon;
    private String forecastMethod;
    private Double overallForecastAccuracy;
    private Double confidenceLevel;
    private Map<String, Object> forecastMetrics;
    
    // Pattern detection
    private List<Pattern> detectedPatterns;
    private List<Anomaly> anomalies;
    private List<String> trendBreaks;
    private String dominantPattern;
    
    // Performance metrics
    private Double modelAccuracy;
    private Double meanAbsoluteError;
    private Double meanSquaredError;
    private Double rootMeanSquaredError;
    private Double meanAbsolutePercentageError;
    
    // Data quality
    private String dataQuality;
    private Double completeness;
    private Integer totalDataPoints;
    private Integer validDataPoints;
    private Integer missingDataPoints;
    private List<String> dataIssues;
    
    // Recommendations
    private List<String> insights;
    private List<String> recommendations;
    private String trendSummary;
    private String businessImplication;
    private List<String> actionItems;
    
    // Metadata
    private Map<String, Object> metadata;
    private String analysisMethod;
    private Map<String, Object> parameters;
    private List<String> dataSources;
    private String analyst;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    public static class TrendComponent {
        private String type;
        private Double slope;
        private Double intercept;
        private Double strength;
        private String equation;
    }
    
    @Data
    @Builder
    public static class SeasonalComponent {
        private String pattern;
        private Double strength;
        private Map<String, Double> factors;
        private Integer period;
    }
    
    @Data
    @Builder
    public static class CyclicalComponent {
        private Double period;
        private Double amplitude;
        private Double phase;
        private Double strength;
    }
    
    @Data
    @Builder
    public static class IrregularComponent {
        private Double variance;
        private List<LocalDateTime> spikes;
        private List<LocalDateTime> dips;
    }
    
    @Data
    @Builder
    public static class ChangePoint {
        private LocalDateTime date;
        private Double beforeValue;
        private Double afterValue;
        private Double changeAmount;
        private String changeType;
        private Double confidence;
    }
    
    @Data
    @Builder
    public static class Outlier {
        private LocalDateTime date;
        private Double value;
        private Double expectedValue;
        private Double deviation;
        private String type;
        private Double severity;
    }
    
    @Data
    @Builder
    public static class SeasonalPeak {
        private String period;
        private Double value;
        private String description;
    }
    
    @Data
    @Builder
    public static class ForecastPoint {
        private LocalDateTime date;
        private Double forecastValue;
        private Double lowerBound;
        private Double upperBound;
        private Double confidence;
    }
    
    @Data
    @Builder
    public static class Pattern {
        private String type;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Double strength;
        private Double confidence;
    }
    
    @Data
    @Builder
    public static class Anomaly {
        private LocalDateTime date;
        private Double value;
        private Double expectedValue;
        private String type;
        private Double severity;
        private String description;
    }
}
