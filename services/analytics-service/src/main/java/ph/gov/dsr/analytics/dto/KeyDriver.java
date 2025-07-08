package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Key Driver DTO for key performance driver analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class KeyDriver {
    private String driverId;
    private String driverName;
    private String description;
    private String category; // INTERNAL, EXTERNAL, CONTROLLABLE, UNCONTROLLABLE
    private String type; // LEADING, LAGGING, COINCIDENT
    
    // Impact analysis
    private Double impactScore;
    private String impactLevel; // CRITICAL, HIGH, MEDIUM, LOW, MINIMAL
    private Double correlation;
    private String correlationType; // POSITIVE, NEGATIVE, NEUTRAL
    private Double elasticity;
    private String significance; // HIGHLY_SIGNIFICANT, SIGNIFICANT, MODERATE, LOW, NOT_SIGNIFICANT
    
    // Statistical measures
    private Double coefficient;
    private Double standardError;
    private Double tStatistic;
    private Double pValue;
    private Double confidenceInterval;
    private Double rSquared;
    private Double adjustedRSquared;
    
    // Trend analysis
    private String trendDirection; // INCREASING, DECREASING, STABLE, VOLATILE
    private Double trendStrength;
    private String trendConsistency; // CONSISTENT, VARIABLE, ERRATIC
    private List<TrendPoint> historicalTrend;
    
    // Performance metrics
    private Double currentValue;
    private Double previousValue;
    private Double changeAmount;
    private Double changePercentage;
    private String performanceRating; // EXCELLENT, GOOD, AVERAGE, POOR, CRITICAL
    
    // Thresholds and targets
    private Double targetValue;
    private Double warningThreshold;
    private Double criticalThreshold;
    private String thresholdStatus; // NORMAL, WARNING, CRITICAL
    private Double targetAchievement;
    
    // Time context
    private LocalDateTime analysisDate;
    private String timePeriod;
    private String timeHorizon; // SHORT_TERM, MEDIUM_TERM, LONG_TERM
    private String frequency; // REAL_TIME, DAILY, WEEKLY, MONTHLY, QUARTERLY
    
    // Controllability
    private Boolean isControllable;
    private String controlLevel; // FULL, PARTIAL, LIMITED, NONE
    private List<String> controlMechanisms;
    private List<String> influencingFactors;
    
    // Predictive power
    private Double predictivePower;
    private String predictiveHorizon;
    private Double forecastAccuracy;
    private Boolean isLeadingIndicator;
    private Integer leadTime; // Days ahead it predicts
    
    // Business context
    private String businessFunction; // SALES, MARKETING, OPERATIONS, FINANCE, HR
    private String businessProcess;
    private List<String> affectedMetrics;
    private String businessOwner;
    private String dataOwner;
    
    // Actionability
    private Boolean isActionable;
    private List<String> recommendedActions;
    private List<String> improvementOpportunities;
    private String actionPriority; // IMMEDIATE, HIGH, MEDIUM, LOW
    private Double actionImpact;
    
    // Data quality
    private String dataQuality; // HIGH, MEDIUM, LOW
    private Double reliability;
    private Double availability;
    private String dataSource;
    private LocalDateTime lastUpdated;
    private String updateFrequency;
    
    // Relationships
    private List<String> relatedDrivers;
    private List<String> dependentMetrics;
    private Map<String, Double> driverInteractions;
    private String primaryRelationship;
    
    // Seasonality and patterns
    private Boolean hasSeasonality;
    private String seasonalPattern;
    private Map<String, Double> seasonalFactors;
    private List<String> identifiedPatterns;
    
    // Risk factors
    private List<String> riskFactors;
    private String riskLevel; // HIGH, MEDIUM, LOW
    private List<String> mitigationStrategies;
    private Double volatility;
    
    // Benchmarking
    private Double industryBenchmark;
    private Double bestPractice;
    private String benchmarkSource;
    private String competitivePosition; // LEADER, AVERAGE, LAGGARD
    
    // Metadata
    private Map<String, Object> metadata;
    private String analysisMethod;
    private String modelType;
    private Map<String, Object> modelParameters;
    private String analyst;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    public static class TrendPoint {
        private LocalDateTime date;
        private Double value;
        private Double impact;
        private String trendDirection;
    }
}
