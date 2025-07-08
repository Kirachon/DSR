package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.AnomalySeverity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Anomaly Indicator DTO
 * Represents a specific anomaly detected during security analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyIndicator {

    /**
     * Anomaly type identifier
     */
    private String type;

    /**
     * Anomaly severity level
     */
    private AnomalySeverity severity;

    /**
     * Human-readable description of the anomaly
     */
    private String description;

    /**
     * Confidence level in the anomaly detection (0.0 - 1.0)
     */
    private Double confidence;

    /**
     * Timestamp when the anomaly was detected
     */
    @Builder.Default
    private LocalDateTime detectedAt = LocalDateTime.now();

    /**
     * Source of the anomaly (e.g., user_behavior, network_traffic, system_activity)
     */
    private String source;

    /**
     * Category of the anomaly (e.g., authentication, data_access, network)
     */
    private String category;

    /**
     * Anomaly score (0-100, higher indicates more severe anomaly)
     */
    private Integer anomalyScore;

    /**
     * Baseline value that was expected
     */
    private String baselineValue;

    /**
     * Observed value that triggered the anomaly
     */
    private String observedValue;

    /**
     * Deviation from baseline (percentage or absolute value)
     */
    private Double deviation;

    /**
     * Statistical significance of the anomaly
     */
    private Double statisticalSignificance;

    /**
     * Additional context information
     */
    private Map<String, Object> context;

    /**
     * Whether this anomaly has been investigated
     */
    @Builder.Default
    private Boolean investigated = false;

    /**
     * Whether this anomaly is a false positive
     */
    @Builder.Default
    private Boolean falsePositive = false;

    /**
     * Whether this anomaly requires immediate action
     */
    @Builder.Default
    private Boolean requiresImmediateAction = false;

    /**
     * Check if this is a high confidence anomaly
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 0.8;
    }

    /**
     * Check if this is a low confidence anomaly
     */
    public boolean isLowConfidence() {
        return confidence != null && confidence < 0.5;
    }

    /**
     * Check if this anomaly is critical
     */
    public boolean isCritical() {
        return severity == AnomalySeverity.CRITICAL;
    }

    /**
     * Check if this anomaly requires immediate attention
     */
    public boolean needsImmediateAttention() {
        return Boolean.TRUE.equals(requiresImmediateAction) || 
               severity.requiresImmediateAttention() || 
               isHighConfidence();
    }

    /**
     * Get the risk score for this anomaly
     */
    public int getRiskScore() {
        if (anomalyScore != null) {
            return anomalyScore;
        }
        
        // Calculate based on severity and confidence
        int severityScore = severity.getScore();
        double confidenceMultiplier = confidence != null ? confidence : 0.5;
        
        return (int) Math.round(severityScore * confidenceMultiplier);
    }

    /**
     * Get formatted confidence as percentage
     */
    public String getFormattedConfidence() {
        if (confidence == null) {
            return "Unknown";
        }
        return String.format("%.1f%%", confidence * 100);
    }

    /**
     * Create a high severity anomaly indicator
     */
    public static AnomalyIndicator highSeverity(String type, String description, double confidence) {
        return AnomalyIndicator.builder()
            .type(type)
            .severity(AnomalySeverity.HIGH)
            .description(description)
            .confidence(confidence)
            .requiresImmediateAction(true)
            .build();
    }

    /**
     * Create a critical anomaly indicator
     */
    public static AnomalyIndicator critical(String type, String description, double confidence) {
        return AnomalyIndicator.builder()
            .type(type)
            .severity(AnomalySeverity.CRITICAL)
            .description(description)
            .confidence(confidence)
            .requiresImmediateAction(true)
            .build();
    }

    /**
     * Create a medium severity anomaly indicator
     */
    public static AnomalyIndicator medium(String type, String description, double confidence) {
        return AnomalyIndicator.builder()
            .type(type)
            .severity(AnomalySeverity.MEDIUM)
            .description(description)
            .confidence(confidence)
            .build();
    }

    /**
     * Create a low severity anomaly indicator
     */
    public static AnomalyIndicator low(String type, String description, double confidence) {
        return AnomalyIndicator.builder()
            .type(type)
            .severity(AnomalySeverity.LOW)
            .description(description)
            .confidence(confidence)
            .build();
    }
}
