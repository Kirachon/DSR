package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Machine Learning Analysis Result DTO
 * Contains results of ML-based security analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLAnalysisResult {

    /**
     * Analysis ID
     */
    private String analysisId;

    /**
     * Model used for analysis
     */
    private String modelName;

    /**
     * Model version
     */
    private String modelVersion;

    /**
     * Prediction score (0-100)
     */
    private Integer predictionScore;

    /**
     * Confidence level (0-100)
     */
    private Integer confidence;

    /**
     * Predicted class/category
     */
    private String predictedClass;

    /**
     * Class probabilities
     */
    private Map<String, Double> classProbabilities;

    /**
     * Feature importance scores
     */
    private Map<String, Double> featureImportance;

    /**
     * Model predictions
     */
    private List<String> predictions;

    /**
     * Analysis features
     */
    private Map<String, Object> features;

    /**
     * Model performance metrics
     */
    private Map<String, Double> performanceMetrics;

    /**
     * Analysis timestamp
     */
    private LocalDateTime analyzedAt;

    /**
     * Analysis duration in milliseconds
     */
    private Long analysisDurationMs;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if prediction is high confidence
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 80;
    }

    /**
     * Check if prediction indicates threat
     */
    public boolean indicatesThreat() {
        return predictionScore != null && predictionScore >= 70;
    }

    /**
     * Get analysis duration in seconds
     */
    public long getAnalysisDurationSeconds() {
        return analysisDurationMs != null ? analysisDurationMs / 1000 : 0;
    }

    /**
     * Create high-threat ML result
     */
    public static MLAnalysisResult highThreat(String modelName, String predictedClass, int score) {
        return MLAnalysisResult.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .modelName(modelName)
                .modelVersion("3.0.0")
                .predictionScore(score)
                .confidence(85)
                .predictedClass(predictedClass)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create low-threat ML result
     */
    public static MLAnalysisResult lowThreat(String modelName, String predictedClass) {
        return MLAnalysisResult.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .modelName(modelName)
                .modelVersion("3.0.0")
                .predictionScore(25)
                .confidence(80)
                .predictedClass(predictedClass)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create disabled result
     */
    public static MLAnalysisResult disabled() {
        return MLAnalysisResult.builder()
                .analysisId("DISABLED")
                .modelName("DISABLED")
                .modelVersion("N/A")
                .predictionScore(0)
                .confidence(0)
                .predictedClass("DISABLED")
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate ML analysis result consistency
     */
    public boolean isValid() {
        return analysisId != null && !analysisId.trim().isEmpty() &&
               modelName != null && !modelName.trim().isEmpty() &&
               analyzedAt != null &&
               (predictionScore == null || (predictionScore >= 0 && predictionScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (analysisDurationMs == null || analysisDurationMs >= 0);
    }
}
