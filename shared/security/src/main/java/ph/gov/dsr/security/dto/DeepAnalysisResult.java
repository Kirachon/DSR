package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Deep Analysis Result DTO
 * Contains results of deep security analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepAnalysisResult {

    /**
     * Analysis ID
     */
    private String analysisId;

    /**
     * Analysis type
     */
    private String analysisType;

    /**
     * Target of analysis
     */
    private String analysisTarget;

    /**
     * Overall risk score (0-100)
     */
    private Integer riskScore;

    /**
     * Confidence level (0-100)
     */
    private Integer confidence;

    /**
     * Analysis findings
     */
    private List<String> findings;

    /**
     * Threat indicators discovered
     */
    private List<String> threatIndicators;

    /**
     * Vulnerabilities identified
     */
    private List<String> vulnerabilities;

    /**
     * Attack vectors identified
     */
    private List<String> attackVectors;

    /**
     * Analysis details
     */
    private Map<String, Object> analysisDetails;

    /**
     * Evidence collected
     */
    private Map<String, Object> evidence;

    /**
     * Recommended actions
     */
    private List<String> recommendedActions;

    /**
     * Analysis start time
     */
    private LocalDateTime analysisStartTime;

    /**
     * Analysis completion time
     */
    private LocalDateTime analysisCompletionTime;

    /**
     * Analysis duration in milliseconds
     */
    private Long analysisDurationMs;

    /**
     * Analysis methodology
     */
    private String methodology;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if analysis is complete
     */
    public boolean isComplete() {
        return analysisCompletionTime != null;
    }

    /**
     * Check if high-risk findings exist
     */
    public boolean hasHighRiskFindings() {
        return riskScore != null && riskScore >= 70;
    }

    /**
     * Get analysis duration in seconds
     */
    public long getAnalysisDurationSeconds() {
        return analysisDurationMs != null ? analysisDurationMs / 1000 : 0;
    }

    /**
     * Get number of findings
     */
    public int getFindingCount() {
        return findings != null ? findings.size() : 0;
    }

    /**
     * Create high-risk analysis result
     */
    public static DeepAnalysisResult highRisk(String target, String type, List<String> findings) {
        return DeepAnalysisResult.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .analysisTarget(target)
                .analysisType(type)
                .riskScore(85)
                .confidence(80)
                .findings(findings)
                .analysisStartTime(LocalDateTime.now())
                .methodology("COMPREHENSIVE_DEEP_ANALYSIS")
                .build();
    }

    /**
     * Create low-risk analysis result
     */
    public static DeepAnalysisResult lowRisk(String target, String type) {
        return DeepAnalysisResult.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .analysisTarget(target)
                .analysisType(type)
                .riskScore(25)
                .confidence(75)
                .analysisStartTime(LocalDateTime.now())
                .methodology("STANDARD_ANALYSIS")
                .build();
    }

    /**
     * Validate analysis result consistency
     */
    public boolean isValid() {
        return analysisId != null && !analysisId.trim().isEmpty() &&
               analysisType != null && !analysisType.trim().isEmpty() &&
               analysisTarget != null && !analysisTarget.trim().isEmpty() &&
               analysisStartTime != null &&
               (riskScore == null || (riskScore >= 0 && riskScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (analysisDurationMs == null || analysisDurationMs >= 0);
    }
}
