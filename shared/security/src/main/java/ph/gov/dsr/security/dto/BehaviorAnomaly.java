package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Behavior Anomaly DTO
 * Contains information about detected behavioral anomalies
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnomaly {

    /**
     * Anomaly ID
     */
    private String anomalyId;

    /**
     * User ID associated with the anomaly
     */
    private String userId;

    /**
     * Anomaly type
     */
    private String anomalyType;

    /**
     * Anomaly description
     */
    private String description;

    /**
     * Severity level
     */
    private String severity;

    /**
     * Anomaly score (0-100)
     */
    private Integer anomalyScore;

    /**
     * Confidence level (0-100)
     */
    private Integer confidence;

    /**
     * Detection timestamp
     */
    private LocalDateTime detectedAt;

    /**
     * Anomaly details
     */
    private Map<String, Object> anomalyDetails;

    /**
     * Baseline comparison
     */
    private Map<String, Object> baselineComparison;

    /**
     * Check if anomaly is high severity
     */
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity);
    }

    /**
     * Check if anomaly requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return isHighSeverity() && anomalyScore != null && anomalyScore >= 80;
    }

    /**
     * Get anomaly age in hours
     */
    public long getAnomalyAgeHours() {
        if (detectedAt == null) return 0;
        return java.time.Duration.between(detectedAt, LocalDateTime.now()).toHours();
    }

    /**
     * Create high-severity anomaly
     */
    public static BehaviorAnomaly highSeverity(String userId, String type, String description) {
        return BehaviorAnomaly.builder()
                .anomalyId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .anomalyType(type)
                .description(description)
                .severity("HIGH")
                .anomalyScore(85)
                .confidence(80)
                .detectedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate anomaly consistency
     */
    public boolean isValid() {
        return anomalyId != null && !anomalyId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               anomalyType != null && !anomalyType.trim().isEmpty() &&
               severity != null && !severity.trim().isEmpty() &&
               detectedAt != null &&
               (anomalyScore == null || (anomalyScore >= 0 && anomalyScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100));
    }
}
