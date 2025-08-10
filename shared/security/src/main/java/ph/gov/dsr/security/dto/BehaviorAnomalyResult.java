package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Behavior Anomaly Result DTO
 * Contains results of behavioral anomaly detection analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnomalyResult {

    /**
     * Result ID
     */
    private String resultId;

    /**
     * User ID analyzed
     */
    private String userId;

    /**
     * Whether anomalies were detected
     */
    private Boolean anomaliesDetected;

    /**
     * Overall anomaly score (0-100)
     */
    private Integer overallAnomalyScore;

    /**
     * Confidence in results (0-100)
     */
    private Integer confidence;

    /**
     * List of detected anomalies
     */
    private List<BehaviorAnomaly> detectedAnomalies;

    /**
     * List of anomalies (alternative field name for compatibility)
     */
    private List<BehaviorAnomaly> anomalies;

    /**
     * Whether anomalies were detected (alternative field name for compatibility)
     */
    private Boolean hasAnomalies;

    /**
     * Anomaly categories
     */
    private List<String> anomalyCategories;

    /**
     * Risk factors identified
     */
    private List<String> riskFactors;

    /**
     * Analysis details
     */
    private Map<String, Object> analysisDetails;

    /**
     * Baseline comparison results
     */
    private Map<String, Object> baselineComparison;

    /**
     * Analysis timestamp
     */
    private LocalDateTime analyzedAt;

    /**
     * Analysis duration in milliseconds
     */
    private Long analysisDurationMs;

    /**
     * Recommended actions
     */
    private List<String> recommendedActions;

    /**
     * Check if immediate action is required
     */
    public boolean requiresImmediateAction() {
        return anomaliesDetected && overallAnomalyScore != null && overallAnomalyScore >= 80;
    }

    /**
     * Check if high-severity anomalies exist
     */
    public boolean hasHighSeverityAnomalies() {
        return detectedAnomalies != null && 
               detectedAnomalies.stream().anyMatch(BehaviorAnomaly::isHighSeverity);
    }

    /**
     * Get number of detected anomalies
     */
    public int getAnomalyCount() {
        return detectedAnomalies != null ? detectedAnomalies.size() : 0;
    }

    /**
     * Get analysis duration in seconds
     */
    public long getAnalysisDurationSeconds() {
        return analysisDurationMs != null ? analysisDurationMs / 1000 : 0;
    }

    /**
     * Create result with anomalies
     */
    public static BehaviorAnomalyResult withAnomalies(String userId, List<BehaviorAnomaly> anomalies, int score) {
        return BehaviorAnomalyResult.builder()
                .resultId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .anomaliesDetected(true)
                .overallAnomalyScore(score)
                .confidence(80)
                .detectedAnomalies(anomalies)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create result with no anomalies
     */
    public static BehaviorAnomalyResult noAnomalies(String userId) {
        return BehaviorAnomalyResult.builder()
                .resultId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .anomaliesDetected(false)
                .overallAnomalyScore(15)
                .confidence(85)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create error result
     */
    public static BehaviorAnomalyResult error(String userId, String errorMessage) {
        return BehaviorAnomalyResult.builder()
                .resultId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .anomaliesDetected(false)
                .overallAnomalyScore(0)
                .confidence(0)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate result consistency
     */
    public boolean isValid() {
        return resultId != null && !resultId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               anomaliesDetected != null &&
               analyzedAt != null &&
               (overallAnomalyScore == null || (overallAnomalyScore >= 0 && overallAnomalyScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (analysisDurationMs == null || analysisDurationMs >= 0);
    }
}
