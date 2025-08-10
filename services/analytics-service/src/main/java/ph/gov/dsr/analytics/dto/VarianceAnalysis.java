package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Variance Analysis DTO for statistical variance analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class VarianceAnalysis {
    private String analysisId;
    private String analysisName;
    private String description;
    private String metricName;
    private LocalDateTime analysisDate;
    private String timePeriod;
    
    // Variance calculations
    private Double actualValue;
    private Double expectedValue;
    private Double plannedValue;
    private Double budgetValue;
    private Double forecastValue;
    private Double previousValue;
    
    // Variance amounts
    private Double absoluteVariance;
    private Double relativeVariance;
    private Double percentageVariance;
    private Double standardizedVariance;
    
    // Variance types
    private Double favorableVariance;
    private Double unfavorableVariance;
    private String varianceType; // FAVORABLE, UNFAVORABLE, NEUTRAL
    private String varianceCategory; // BUDGET, FORECAST, PLAN, HISTORICAL
    
    // Statistical measures
    private Double variance;
    private Double standardDeviation;
    private Double coefficientOfVariation;
    private Double zScore;
    private Double confidenceInterval;
    private String significanceLevel; // HIGH, MEDIUM, LOW, NOT_SIGNIFICANT
    
    // Trend analysis
    private String trendDirection; // IMPROVING, DETERIORATING, STABLE
    private Double trendSlope;
    private String trendSignificance;
    private List<VariancePoint> historicalVariances;
    
    // Root cause analysis
    private List<String> contributingFactors;
    private List<String> rootCauses;
    private Map<String, Double> factorImpacts;
    private String primaryCause;
    private String secondaryCause;
    
    // Impact assessment
    private String impactLevel; // CRITICAL, HIGH, MEDIUM, LOW, MINIMAL
    private Double financialImpact;
    private Double operationalImpact;
    private String businessImpact;
    private List<String> affectedAreas;
    
    // Recommendations
    private List<String> recommendations;
    private List<String> actionItems;
    private String correctiveAction;
    private String preventiveAction;
    private LocalDateTime recommendedActionDate;
    
    // Thresholds
    private Double warningThreshold;
    private Double criticalThreshold;
    private String thresholdType; // ABSOLUTE, PERCENTAGE
    private Boolean exceedsWarningThreshold;
    private Boolean exceedsCriticalThreshold;
    
    // Comparison periods
    private String comparisonPeriod; // PREVIOUS_PERIOD, SAME_PERIOD_LAST_YEAR, BUDGET, FORECAST
    private LocalDateTime comparisonStartDate;
    private LocalDateTime comparisonEndDate;
    private Map<String, Object> comparisonData;
    
    // Data quality
    private String dataQuality; // HIGH, MEDIUM, LOW
    private Double confidence;
    private Integer sampleSize;
    private List<String> dataLimitations;
    private List<String> assumptions;
    
    // Metadata
    private Map<String, Object> metadata;
    private String analysisMethod;
    private String calculationFormula;
    private List<String> dataSources;
    private String analyst;
    private String reviewer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    public static class VariancePoint {
        private LocalDateTime date;
        private Double actualValue;
        private Double expectedValue;
        private Double variance;
        private Double percentageVariance;
        private String varianceType;
    }
}
