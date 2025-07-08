package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Anomaly Detection Result DTO
 * Contains comprehensive anomaly detection operation results including anomaly details and recommendations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionResult {

    /**
     * Unique detection result ID
     */
    private String detectionResultId;

    /**
     * Event ID that was analyzed
     */
    private String eventId;

    /**
     * Whether anomaly was detected
     */
    private Boolean anomalyDetected;

    /**
     * Anomaly score (0-100, where 100 is highest anomaly)
     */
    private Integer anomalyScore;

    /**
     * Detection confidence level (0-100)
     */
    @Builder.Default
    private Integer confidenceLevel = 100;

    /**
     * Detection timestamp
     */
    @Builder.Default
    private LocalDateTime detectionTime = LocalDateTime.now();

    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;

    /**
     * Primary anomaly type detected
     */
    private String primaryAnomalyType;

    /**
     * Secondary anomaly types detected
     */
    private List<String> secondaryAnomalyTypes;

    /**
     * Anomaly severity level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String anomalySeverity;

    /**
     * Anomaly description
     */
    private String anomalyDescription;

    /**
     * Baseline values used for comparison
     */
    private Map<String, Object> baselineValues;

    /**
     * Observed values that triggered the anomaly
     */
    private Map<String, Object> observedValues;

    /**
     * Deviation metrics from baseline
     */
    private Map<String, Double> deviationMetrics;

    /**
     * Statistical measures used in detection
     */
    private Map<String, Double> statisticalMeasures;

    /**
     * Detection algorithm used
     */
    private String detectionAlgorithm;

    /**
     * Algorithm parameters used
     */
    private Map<String, Object> algorithmParameters;

    /**
     * Contributing factors to the anomaly
     */
    private List<String> contributingFactors;

    /**
     * Recommended actions based on anomaly
     */
    private List<String> recommendedActions;

    /**
     * Whether immediate attention is required
     */
    @Builder.Default
    private Boolean immediateAttentionRequired = false;

    /**
     * Whether automated response was triggered
     */
    @Builder.Default
    private Boolean automatedResponseTriggered = false;

    /**
     * Whether manual investigation is recommended
     */
    @Builder.Default
    private Boolean manualInvestigationRecommended = false;

    /**
     * User ID associated with the anomaly (if applicable)
     */
    private String userId;

    /**
     * Device ID associated with the anomaly (if applicable)
     */
    private String deviceId;

    /**
     * Source IP address (if applicable)
     */
    private String sourceIpAddress;

    /**
     * Geographic location (if applicable)
     */
    private String geographicLocation;

    /**
     * Time window analyzed
     */
    private String timeWindowAnalyzed;

    /**
     * Historical context used in analysis
     */
    private Map<String, Object> historicalContext;

    /**
     * Related anomalies detected in the same time period
     */
    private List<String> relatedAnomalies;

    /**
     * Pattern analysis results
     */
    private Map<String, Object> patternAnalysisResults;

    /**
     * Additional context information
     */
    private Map<String, Object> additionalContext;

    /**
     * Check if anomaly was detected
     */
    public boolean isAnomalyDetected() {
        return Boolean.TRUE.equals(anomalyDetected);
    }

    /**
     * Check if immediate attention is required
     */
    public boolean needsImmediateAttention() {
        return Boolean.TRUE.equals(immediateAttentionRequired);
    }

    /**
     * Check if automated response was triggered
     */
    public boolean hasAutomatedResponse() {
        return Boolean.TRUE.equals(automatedResponseTriggered);
    }

    /**
     * Check if manual investigation is recommended
     */
    public boolean needsManualInvestigation() {
        return Boolean.TRUE.equals(manualInvestigationRecommended);
    }

    /**
     * Check if anomaly is high severity
     */
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(anomalySeverity) || "CRITICAL".equalsIgnoreCase(anomalySeverity);
    }

    /**
     * Check if anomaly is critical
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(anomalySeverity);
    }

    /**
     * Check if detection is high confidence
     */
    public boolean isHighConfidence() {
        return confidenceLevel >= 90;
    }

    /**
     * Check if detection is low confidence
     */
    public boolean isLowConfidence() {
        return confidenceLevel < 70;
    }

    /**
     * Check if there are contributing factors
     */
    public boolean hasContributingFactors() {
        return contributingFactors != null && !contributingFactors.isEmpty();
    }

    /**
     * Check if there are recommended actions
     */
    public boolean hasRecommendedActions() {
        return recommendedActions != null && !recommendedActions.isEmpty();
    }

    /**
     * Check if there are related anomalies
     */
    public boolean hasRelatedAnomalies() {
        return relatedAnomalies != null && !relatedAnomalies.isEmpty();
    }

    /**
     * Get total anomaly type count
     */
    public int getTotalAnomalyTypeCount() {
        int count = primaryAnomalyType != null ? 1 : 0;
        if (secondaryAnomalyTypes != null) {
            count += secondaryAnomalyTypes.size();
        }
        return count;
    }

    /**
     * Get contributing factor count
     */
    public int getContributingFactorCount() {
        return contributingFactors != null ? contributingFactors.size() : 0;
    }

    /**
     * Get recommended action count
     */
    public int getRecommendedActionCount() {
        return recommendedActions != null ? recommendedActions.size() : 0;
    }

    /**
     * Get processing time in a human-readable format
     */
    public String getFormattedProcessingTime() {
        if (processingTimeMs == null) {
            return "Unknown";
        }
        
        if (processingTimeMs < 1000) {
            return processingTimeMs + "ms";
        } else if (processingTimeMs < 60000) {
            return String.format("%.1fs", processingTimeMs / 1000.0);
        } else {
            long minutes = processingTimeMs / 60000;
            long seconds = (processingTimeMs % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Get maximum deviation from baseline
     */
    public Double getMaxDeviation() {
        if (deviationMetrics == null || deviationMetrics.isEmpty()) {
            return null;
        }
        
        return deviationMetrics.values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0.0);
    }

    /**
     * Get anomaly detection summary
     */
    public String getAnomalyDetectionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Anomaly Detection - ");
        
        if (isAnomalyDetected()) {
            summary.append("ANOMALY DETECTED");
            summary.append(", Type: ").append(primaryAnomalyType != null ? primaryAnomalyType : "Unknown");
            summary.append(", Score: ").append(anomalyScore);
            summary.append(", Severity: ").append(anomalySeverity);
            summary.append(", Confidence: ").append(confidenceLevel).append("%");
            
            if (isCritical()) {
                summary.append(" [CRITICAL]");
            }
            
            if (needsImmediateAttention()) {
                summary.append(" [IMMEDIATE ATTENTION]");
            }
        } else {
            summary.append("NO ANOMALY DETECTED");
            summary.append(", Score: ").append(anomalyScore);
            summary.append(", Confidence: ").append(confidenceLevel).append("%");
        }
        
        return summary.toString();
    }

    /**
     * Get analysis details summary
     */
    public String getAnalysisDetailsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Analysis Details - ");
        summary.append("Algorithm: ").append(detectionAlgorithm != null ? detectionAlgorithm : "Unknown");
        summary.append(", Processing Time: ").append(getFormattedProcessingTime());
        summary.append(", Time Window: ").append(timeWindowAnalyzed != null ? timeWindowAnalyzed : "Unknown");
        
        if (getMaxDeviation() != null) {
            summary.append(", Max Deviation: ").append(String.format("%.2f", getMaxDeviation()));
        }
        
        if (hasContributingFactors()) {
            summary.append(", Contributing Factors: ").append(getContributingFactorCount());
        }
        
        return summary.toString();
    }

    /**
     * Create an anomaly detected result
     */
    public static AnomalyDetectionResult anomalyDetected(String detectionResultId, String eventId, 
                                                       int anomalyScore, String anomalyType, String severity) {
        return AnomalyDetectionResult.builder()
            .detectionResultId(detectionResultId)
            .eventId(eventId)
            .anomalyDetected(true)
            .anomalyScore(anomalyScore)
            .primaryAnomalyType(anomalyType)
            .anomalySeverity(severity)
            .immediateAttentionRequired(anomalyScore >= 80)
            .manualInvestigationRecommended(anomalyScore >= 70)
            .detectionTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a no anomaly result
     */
    public static AnomalyDetectionResult noAnomalyDetected(String detectionResultId, String eventId, int score) {
        return AnomalyDetectionResult.builder()
            .detectionResultId(detectionResultId)
            .eventId(eventId)
            .anomalyDetected(false)
            .anomalyScore(score)
            .anomalySeverity("LOW")
            .immediateAttentionRequired(false)
            .manualInvestigationRecommended(false)
            .detectionTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create an error result
     */
    public static AnomalyDetectionResult error(String eventId, String errorMessage) {
        return AnomalyDetectionResult.builder()
            .detectionResultId("ERROR_" + System.currentTimeMillis())
            .eventId(eventId)
            .anomalyDetected(false)
            .anomalyScore(0)
            .anomalySeverity("LOW")
            .anomalyDescription("Error during anomaly detection: " + errorMessage)
            .immediateAttentionRequired(false)
            .manualInvestigationRecommended(false)
            .detectionTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a disabled result
     */
    public static AnomalyDetectionResult disabled() {
        return AnomalyDetectionResult.builder()
            .detectionResultId("DISABLED")
            .eventId("N/A")
            .anomalyDetected(false)
            .anomalyScore(0)
            .anomalySeverity("LOW")
            .anomalyDescription("Anomaly detection is disabled")
            .immediateAttentionRequired(false)
            .manualInvestigationRecommended(false)
            .detectionTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate detection result consistency
     */
    public boolean isValid() {
        return detectionResultId != null && !detectionResultId.trim().isEmpty() &&
               eventId != null && !eventId.trim().isEmpty() &&
               anomalyDetected != null &&
               anomalyScore != null && anomalyScore >= 0 && anomalyScore <= 100 &&
               confidenceLevel != null && confidenceLevel >= 0 && confidenceLevel <= 100 &&
               detectionTime != null &&
               (processingTimeMs == null || processingTimeMs >= 0);
    }
}
