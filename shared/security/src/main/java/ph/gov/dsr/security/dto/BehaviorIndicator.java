package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Behavior Indicator DTO
 * Contains information about behavioral security indicators
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorIndicator {

    /**
     * Indicator ID
     */
    private String indicatorId;

    /**
     * Indicator type
     */
    private String indicatorType;

    /**
     * Indicator name
     */
    private String indicatorName;

    /**
     * Indicator description
     */
    private String description;

    /**
     * Severity level
     */
    private String severity;

    /**
     * Risk score (0-100)
     */
    private Integer riskScore;

    /**
     * Confidence level (0-100)
     */
    private Integer confidence;

    /**
     * Indicator value
     */
    private String indicatorValue;

    /**
     * Detection timestamp
     */
    private LocalDateTime detectedAt;

    /**
     * Indicator metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if indicator is high severity
     */
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity);
    }

    /**
     * Check if indicator is high confidence
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 80;
    }

    /**
     * Get indicator age in hours
     */
    public long getIndicatorAgeHours() {
        if (detectedAt == null) return 0;
        return java.time.Duration.between(detectedAt, LocalDateTime.now()).toHours();
    }

    /**
     * Create high-risk indicator
     */
    public static BehaviorIndicator highRisk(String type, String name, String value) {
        return BehaviorIndicator.builder()
                .indicatorId(java.util.UUID.randomUUID().toString())
                .indicatorType(type)
                .indicatorName(name)
                .indicatorValue(value)
                .severity("HIGH")
                .riskScore(85)
                .confidence(80)
                .detectedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate indicator consistency
     */
    public boolean isValid() {
        return indicatorId != null && !indicatorId.trim().isEmpty() &&
               indicatorType != null && !indicatorType.trim().isEmpty() &&
               indicatorName != null && !indicatorName.trim().isEmpty() &&
               severity != null && !severity.trim().isEmpty() &&
               detectedAt != null &&
               (riskScore == null || (riskScore >= 0 && riskScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100));
    }
}
