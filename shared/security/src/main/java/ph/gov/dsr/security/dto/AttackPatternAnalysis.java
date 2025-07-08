package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Attack Pattern Analysis DTO
 * Contains analysis results of attack patterns and techniques
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttackPatternAnalysis {

    /**
     * Analysis ID
     */
    private String analysisId;

    /**
     * Attack pattern type
     */
    private String attackPatternType;

    /**
     * Pattern name
     */
    private String patternName;

    /**
     * Pattern description
     */
    private String description;

    /**
     * Severity level
     */
    private String severity;

    /**
     * Confidence score (0-100)
     */
    private Integer confidence;

    /**
     * Attack techniques identified
     */
    private List<String> attackTechniques;

    /**
     * MITRE ATT&CK techniques
     */
    private List<String> mitreAttackTechniques;

    /**
     * Kill chain phases
     */
    private List<String> killChainPhases;

    /**
     * Indicators of compromise
     */
    private List<String> iocs;

    /**
     * Attack vectors
     */
    private List<String> attackVectors;

    /**
     * Pattern details
     */
    private Map<String, Object> patternDetails;

    /**
     * Evidence collected
     */
    private Map<String, Object> evidence;

    /**
     * Attribution information
     */
    private Map<String, String> attribution;

    /**
     * Analysis timestamp
     */
    private LocalDateTime analyzedAt;

    /**
     * Pattern first observed
     */
    private LocalDateTime firstObserved;

    /**
     * Pattern last observed
     */
    private LocalDateTime lastObserved;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if pattern is high severity
     */
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity);
    }

    /**
     * Check if pattern is high confidence
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 80;
    }

    /**
     * Get number of attack techniques
     */
    public int getAttackTechniqueCount() {
        return attackTechniques != null ? attackTechniques.size() : 0;
    }

    /**
     * Get analysis age in hours
     */
    public long getAnalysisAgeHours() {
        if (analyzedAt == null) return 0;
        return java.time.Duration.between(analyzedAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if pattern is recent (less than 24 hours old)
     */
    public boolean isRecent() {
        return getAnalysisAgeHours() < 24;
    }

    /**
     * Create high-severity attack pattern analysis
     */
    public static AttackPatternAnalysis highSeverity(String patternType, String patternName, 
                                                   List<String> techniques) {
        return AttackPatternAnalysis.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .attackPatternType(patternType)
                .patternName(patternName)
                .severity("HIGH")
                .confidence(85)
                .attackTechniques(techniques)
                .analyzedAt(LocalDateTime.now())
                .firstObserved(LocalDateTime.now())
                .build();
    }

    /**
     * Create low-severity attack pattern analysis
     */
    public static AttackPatternAnalysis lowSeverity(String patternType, String patternName) {
        return AttackPatternAnalysis.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .attackPatternType(patternType)
                .patternName(patternName)
                .severity("LOW")
                .confidence(70)
                .analyzedAt(LocalDateTime.now())
                .firstObserved(LocalDateTime.now())
                .build();
    }

    /**
     * Validate attack pattern analysis consistency
     */
    public boolean isValid() {
        return analysisId != null && !analysisId.trim().isEmpty() &&
               attackPatternType != null && !attackPatternType.trim().isEmpty() &&
               patternName != null && !patternName.trim().isEmpty() &&
               severity != null && !severity.trim().isEmpty() &&
               analyzedAt != null &&
               (confidence == null || (confidence >= 0 && confidence <= 100));
    }
}
