package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Validation annotations temporarily removed for compilation
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Behavior Analysis Result DTO
 * Contains comprehensive behavioral analysis results for user activity patterns
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysisResult {

    /**
     * Unique identifier for the analysis result
     */
    private String analysisResultId;

    /**
     * Event ID that triggered this analysis
     */
    private String eventId;

    /**
     * User ID being analyzed
     */
    private String userId;

    /**
     * Session ID for the analysis
     */
    private String sessionId;

    /**
     * Whether the behavior is considered normal
     */
    private Boolean normal;

    /**
     * Anomaly score (0-100, where 100 is most anomalous)
     */
    private Integer anomalyScore;

    /**
     * Overall risk score (0-100, where 100 is highest risk)
     */
    private Integer riskScore;

    /**
     * Confidence level of the analysis (0-100)
     */
    private Integer confidence;

    /**
     * Whether velocity violations were detected
     */
    private Boolean hasVelocityViolations;

    /**
     * Whether unusual patterns were detected
     */
    private Boolean hasUnusualPatterns;

    /**
     * Whether impossible travel was detected
     */
    private Boolean hasImpossibleTravel;

    /**
     * Whether unusual time access patterns were detected
     */
    private Boolean hasUnusualTimeAccess;

    /**
     * List of detected anomalies
     */
    private List<String> anomalies;

    /**
     * List of behavior indicators
     */
    private List<String> behaviorIndicators;

    /**
     * Risk factors identified
     */
    private List<String> riskFactors;

    /**
     * Behavioral patterns detected
     */
    private Map<String, Object> behaviorPatterns;

    /**
     * Analysis metadata
     */
    private Map<String, Object> analysisMetadata;

    /**
     * Timestamp when analysis was performed
     */
    private LocalDateTime analyzedAt;

    /**
     * Analysis duration in milliseconds
     */
    private Long analysisDurationMs;

    /**
     * Analysis engine version
     */
    private String engineVersion;

    /**
     * Create result for normal behavior
     */
    public static BehaviorAnalysisResult normal(String userId) {
        return BehaviorAnalysisResult.builder()
                .analysisResultId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .normal(true)
                .anomalyScore(10)
                .hasVelocityViolations(false)
                .hasUnusualPatterns(false)
                .hasImpossibleTravel(false)
                .hasUnusualTimeAccess(false)
                .riskScore(20)
                .confidence(85)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create result for anomalous behavior
     */
    public static BehaviorAnalysisResult anomalous(String userId, List<String> anomalies) {
        return BehaviorAnalysisResult.builder()
                .analysisResultId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .normal(false)
                .anomalyScore(80)
                .hasVelocityViolations(true)
                .hasUnusualPatterns(true)
                .riskScore(90)
                .confidence(75)
                .anomalies(anomalies)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create error result
     */
    public static BehaviorAnalysisResult error(String eventId, String errorMessage) {
        return BehaviorAnalysisResult.builder()
                .analysisResultId(java.util.UUID.randomUUID().toString())
                .eventId(eventId)
                .normal(false)
                .anomalyScore(0)
                .riskScore(0)
                .confidence(0)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if immediate action is required
     */
    public boolean requiresImmediateAction() {
        return !normal && riskScore != null && riskScore >= 80;
    }

    /**
     * Get risk category based on risk score
     */
    public String getRiskCategory() {
        if (riskScore == null) return "UNKNOWN";
        if (riskScore >= 80) return "HIGH";
        if (riskScore >= 60) return "MEDIUM";
        if (riskScore >= 40) return "LOW";
        return "MINIMAL";
    }

    /**
     * Validate analysis result consistency
     */
    public boolean isValid() {
        return analysisResultId != null && !analysisResultId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               normal != null &&
               analyzedAt != null &&
               (anomalyScore == null || (anomalyScore >= 0 && anomalyScore <= 100)) &&
               (riskScore == null || (riskScore >= 0 && riskScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100));
    }
}
