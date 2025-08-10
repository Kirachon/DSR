package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Threat Score DTO
 * Contains threat scoring information and calculations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatScore {

    /**
     * Overall threat score (0-100)
     */
    private Integer overallScore;

    /**
     * Behavioral threat score component
     */
    private Integer behavioralScore;

    /**
     * Network threat score component
     */
    private Integer networkScore;

    /**
     * Intelligence threat score component
     */
    private Integer intelligenceScore;

    /**
     * Anomaly detection score component
     */
    private Integer anomalyScore;

    /**
     * Machine learning score component
     */
    private Integer mlScore;

    /**
     * Confidence in the threat score (0-100)
     */
    private Integer confidence;

    /**
     * Score calculation weights
     */
    private Map<String, Double> weights;

    /**
     * Score breakdown details
     */
    private Map<String, Object> breakdown;

    /**
     * Timestamp when score was calculated
     */
    private LocalDateTime calculatedAt;

    /**
     * Score calculation method
     */
    private String calculationMethod;

    /**
     * Score version
     */
    private String scoreVersion;

    /**
     * Get threat level based on overall score
     */
    public ThreatLevel getThreatLevel() {
        return ThreatLevel.fromRiskScore(overallScore);
    }

    /**
     * Check if score indicates high threat
     */
    public boolean isHighThreat() {
        return overallScore >= 70;
    }

    /**
     * Check if score indicates critical threat
     */
    public boolean isCriticalThreat() {
        return overallScore >= 85;
    }

    /**
     * Get score category
     */
    public String getScoreCategory() {
        if (overallScore >= 85) return "CRITICAL";
        if (overallScore >= 70) return "HIGH";
        if (overallScore >= 50) return "MEDIUM";
        if (overallScore >= 30) return "LOW";
        return "MINIMAL";
    }

    /**
     * Create high threat score
     */
    public static ThreatScore highThreat(int score) {
        return ThreatScore.builder()
                .overallScore(score)
                .confidence(80)
                .calculatedAt(LocalDateTime.now())
                .calculationMethod("COMPOSITE")
                .scoreVersion("3.0")
                .build();
    }

    /**
     * Create low threat score
     */
    public static ThreatScore lowThreat(int score) {
        return ThreatScore.builder()
                .overallScore(score)
                .confidence(70)
                .calculatedAt(LocalDateTime.now())
                .calculationMethod("COMPOSITE")
                .scoreVersion("3.0")
                .build();
    }

    /**
     * Validate threat score consistency
     */
    public boolean isValid() {
        return overallScore != null && overallScore >= 0 && overallScore <= 100 &&
               calculatedAt != null &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (behavioralScore == null || (behavioralScore >= 0 && behavioralScore <= 100)) &&
               (networkScore == null || (networkScore >= 0 && networkScore <= 100)) &&
               (intelligenceScore == null || (intelligenceScore >= 0 && intelligenceScore <= 100)) &&
               (anomalyScore == null || (anomalyScore >= 0 && anomalyScore <= 100)) &&
               (mlScore == null || (mlScore >= 0 && mlScore <= 100));
    }
}
