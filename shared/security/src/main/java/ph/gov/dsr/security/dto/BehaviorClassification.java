package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Behavior Classification DTO
 * Contains behavioral classification results and categories
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorClassification {

    /**
     * Classification ID
     */
    private String classificationId;

    /**
     * User ID being classified
     */
    private String userId;

    /**
     * Primary behavior category
     */
    private String primaryCategory;

    /**
     * Secondary behavior categories
     */
    private List<String> secondaryCategories;

    /**
     * Classification confidence (0-100)
     */
    private Integer confidence;

    /**
     * Risk level associated with classification
     */
    private String riskLevel;

    /**
     * Classification score (0-100)
     */
    private Integer classificationScore;

    /**
     * Behavior patterns identified
     */
    private List<String> behaviorPatterns;

    /**
     * Classification features
     */
    private Map<String, Object> features;

    /**
     * Model used for classification
     */
    private String classificationModel;

    /**
     * Model version
     */
    private String modelVersion;

    /**
     * Classification timestamp
     */
    private LocalDateTime classifiedAt;

    /**
     * Classification metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if classification indicates high risk
     */
    public boolean isHighRisk() {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }

    /**
     * Check if classification is high confidence
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 80;
    }

    /**
     * Get classification age in hours
     */
    public long getClassificationAgeHours() {
        if (classifiedAt == null) return 0;
        return java.time.Duration.between(classifiedAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if classification is stale (older than 24 hours)
     */
    public boolean isStale() {
        return getClassificationAgeHours() > 24;
    }

    /**
     * Create high-risk classification
     */
    public static BehaviorClassification highRisk(String userId, String category, int score) {
        return BehaviorClassification.builder()
                .classificationId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .primaryCategory(category)
                .confidence(85)
                .riskLevel("HIGH")
                .classificationScore(score)
                .classificationModel("DSR_BEHAVIOR_CLASSIFIER_V3")
                .modelVersion("3.0.0")
                .classifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create normal classification
     */
    public static BehaviorClassification normal(String userId, String category) {
        return BehaviorClassification.builder()
                .classificationId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .primaryCategory(category)
                .confidence(80)
                .riskLevel("LOW")
                .classificationScore(25)
                .classificationModel("DSR_BEHAVIOR_CLASSIFIER_V3")
                .modelVersion("3.0.0")
                .classifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate classification consistency
     */
    public boolean isValid() {
        return classificationId != null && !classificationId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               primaryCategory != null && !primaryCategory.trim().isEmpty() &&
               classifiedAt != null &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (classificationScore == null || (classificationScore >= 0 && classificationScore <= 100));
    }
}
