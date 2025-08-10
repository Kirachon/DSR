package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * User Behavior Profile DTO
 * Contains comprehensive user behavioral profiling data
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorProfile {

    /**
     * User ID for the behavior profile
     */
    private String userId;

    /**
     * Profile ID
     */
    private String profileId;

    /**
     * Baseline behavior patterns
     */
    private Map<String, Object> baselinePatterns;

    /**
     * Current behavior patterns
     */
    private Map<String, Object> currentPatterns;

    /**
     * Login patterns
     */
    private Map<String, Object> loginPatterns;

    /**
     * Access patterns
     */
    private Map<String, Object> accessPatterns;

    /**
     * Geographic patterns
     */
    private Map<String, Object> geographicPatterns;

    /**
     * Device usage patterns
     */
    private Map<String, Object> devicePatterns;

    /**
     * Time-based patterns
     */
    private Map<String, Object> timePatterns;

    /**
     * Risk score (0-100)
     */
    private Integer riskScore;

    /**
     * Anomaly score (0-100)
     */
    private Integer anomalyScore;

    /**
     * Confidence in profile (0-100)
     */
    private Integer confidence;

    /**
     * Number of sessions analyzed
     */
    private Integer sessionsAnalyzed;

    /**
     * Profile creation date
     */
    private LocalDateTime profileCreated;

    /**
     * Last profile update
     */
    private LocalDateTime lastUpdated;

    /**
     * Last activity timestamp
     */
    private LocalDateTime lastActivity;

    /**
     * Profile status
     */
    private String profileStatus;

    /**
     * Detected anomalies
     */
    private List<String> detectedAnomalies;

    /**
     * Behavior indicators
     */
    private List<String> behaviorIndicators;

    /**
     * Risk factors
     */
    private List<String> riskFactors;

    /**
     * Profile metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if profile indicates high risk
     */
    public boolean isHighRisk() {
        return riskScore != null && riskScore >= 70;
    }

    /**
     * Check if profile has anomalies
     */
    public boolean hasAnomalies() {
        return anomalyScore != null && anomalyScore >= 50;
    }

    /**
     * Get risk category
     */
    public String getRiskCategory() {
        if (riskScore == null) return "UNKNOWN";
        if (riskScore >= 80) return "HIGH";
        if (riskScore >= 60) return "MEDIUM";
        if (riskScore >= 40) return "LOW";
        return "MINIMAL";
    }

    /**
     * Check if profile is stale
     */
    public boolean isStale() {
        if (lastUpdated == null) return true;
        return java.time.Duration.between(lastUpdated, LocalDateTime.now()).toDays() > 7;
    }

    /**
     * Create high-risk profile
     */
    public static UserBehaviorProfile highRisk(String userId, int riskScore) {
        return UserBehaviorProfile.builder()
                .userId(userId)
                .profileId(java.util.UUID.randomUUID().toString())
                .riskScore(riskScore)
                .anomalyScore(75)
                .confidence(80)
                .profileStatus("HIGH_RISK")
                .profileCreated(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Create normal profile
     */
    public static UserBehaviorProfile normal(String userId) {
        return UserBehaviorProfile.builder()
                .userId(userId)
                .profileId(java.util.UUID.randomUUID().toString())
                .riskScore(25)
                .anomalyScore(15)
                .confidence(85)
                .profileStatus("NORMAL")
                .profileCreated(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Validate profile consistency
     */
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty() &&
               profileId != null && !profileId.trim().isEmpty() &&
               profileCreated != null &&
               (riskScore == null || (riskScore >= 0 && riskScore <= 100)) &&
               (anomalyScore == null || (anomalyScore >= 0 && anomalyScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (sessionsAnalyzed == null || sessionsAnalyzed >= 0);
    }
}
