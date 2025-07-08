package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.ThreatLevel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Threat Analysis Result DTO
 * Contains comprehensive threat analysis results including scores, recommendations, and detailed findings
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatAnalysisResult {

    /**
     * Unique analysis ID
     */
    private String analysisId;

    /**
     * Event ID that was analyzed
     */
    private String eventId;

    /**
     * Overall threat level assessment
     */
    private ThreatLevel threatLevel;

    /**
     * Threat score (0-100, where 100 is highest threat)
     */
    private Integer threatScore;

    /**
     * Confidence level of the analysis (0-100)
     */
    @Builder.Default
    private Integer confidenceLevel = 100;

    /**
     * Analysis timestamp
     */
    @Builder.Default
    private LocalDateTime analysisTime = LocalDateTime.now();

    /**
     * Time taken to complete analysis in milliseconds
     */
    private Long analysisDurationMs;

    /**
     * Primary threat category identified
     */
    private String primaryThreatCategory;

    /**
     * Secondary threat categories identified
     */
    private List<String> secondaryThreatCategories;

    /**
     * Threat indicators detected
     */
    private List<String> threatIndicators;

    /**
     * Attack vectors identified
     */
    private List<String> attackVectors;

    /**
     * Potential impact assessment
     */
    private String potentialImpact;

    /**
     * Recommended immediate actions
     */
    private List<String> immediateActions;

    /**
     * Recommended follow-up actions
     */
    private List<String> followUpActions;

    /**
     * Mitigation strategies
     */
    private List<String> mitigationStrategies;

    /**
     * Whether automated response was triggered
     */
    @Builder.Default
    private Boolean automatedResponseTriggered = false;

    /**
     * Whether manual investigation is required
     */
    @Builder.Default
    private Boolean manualInvestigationRequired = false;

    /**
     * Whether incident escalation is required
     */
    @Builder.Default
    private Boolean escalationRequired = false;

    /**
     * Source IP address (if applicable)
     */
    private String sourceIpAddress;

    /**
     * Target IP address (if applicable)
     */
    private String targetIpAddress;

    /**
     * User ID associated with the threat (if applicable)
     */
    private String userId;

    /**
     * Device ID associated with the threat (if applicable)
     */
    private String deviceId;

    /**
     * Geographic location of the threat source
     */
    private String geographicLocation;

    /**
     * Threat intelligence matches
     */
    private List<String> threatIntelligenceMatches;

    /**
     * Behavioral anomalies detected
     */
    private List<String> behavioralAnomalies;

    /**
     * Network anomalies detected
     */
    private List<String> networkAnomalies;

    /**
     * Risk factors contributing to the threat score
     */
    private Map<String, Integer> riskFactors;

    /**
     * Analysis engine details
     */
    private Map<String, Object> analysisEngineDetails;

    /**
     * Additional context information
     */
    private Map<String, Object> additionalContext;

    /**
     * Related events or incidents
     */
    private List<String> relatedEvents;

    /**
     * Timeline of threat progression
     */
    private List<String> threatTimeline;

    /**
     * Check if threat is critical
     */
    public boolean isCriticalThreat() {
        return threatLevel == ThreatLevel.CRITICAL || threatScore >= 90;
    }

    /**
     * Check if threat is high severity
     */
    public boolean isHighSeverityThreat() {
        return threatLevel == ThreatLevel.HIGH || threatScore >= 70;
    }

    /**
     * Check if analysis is high confidence
     */
    public boolean isHighConfidence() {
        return confidenceLevel >= 90;
    }

    /**
     * Check if analysis is low confidence
     */
    public boolean isLowConfidence() {
        return confidenceLevel < 70;
    }

    /**
     * Check if automated response was triggered
     */
    public boolean hasAutomatedResponse() {
        return Boolean.TRUE.equals(automatedResponseTriggered);
    }

    /**
     * Check if manual investigation is needed
     */
    public boolean needsManualInvestigation() {
        return Boolean.TRUE.equals(manualInvestigationRequired);
    }

    /**
     * Check if escalation is required
     */
    public boolean needsEscalation() {
        return Boolean.TRUE.equals(escalationRequired);
    }

    /**
     * Check if there are threat intelligence matches
     */
    public boolean hasThreatIntelligenceMatches() {
        return threatIntelligenceMatches != null && !threatIntelligenceMatches.isEmpty();
    }

    /**
     * Check if there are behavioral anomalies
     */
    public boolean hasBehavioralAnomalies() {
        return behavioralAnomalies != null && !behavioralAnomalies.isEmpty();
    }

    /**
     * Check if there are network anomalies
     */
    public boolean hasNetworkAnomalies() {
        return networkAnomalies != null && !networkAnomalies.isEmpty();
    }

    /**
     * Check if there are immediate actions required
     */
    public boolean hasImmediateActions() {
        return immediateActions != null && !immediateActions.isEmpty();
    }

    /**
     * Get total number of threat indicators
     */
    public int getThreatIndicatorCount() {
        return threatIndicators != null ? threatIndicators.size() : 0;
    }

    /**
     * Get total number of attack vectors
     */
    public int getAttackVectorCount() {
        return attackVectors != null ? attackVectors.size() : 0;
    }

    /**
     * Calculate overall risk score based on multiple factors
     */
    public double getOverallRiskScore() {
        if (riskFactors == null || riskFactors.isEmpty()) {
            return threatScore;
        }
        
        double totalRisk = riskFactors.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(threatScore);
            
        return Math.min(100.0, totalRisk);
    }

    /**
     * Get analysis duration in a human-readable format
     */
    public String getFormattedAnalysisDuration() {
        if (analysisDurationMs == null) {
            return "Unknown";
        }
        
        if (analysisDurationMs < 1000) {
            return analysisDurationMs + "ms";
        } else if (analysisDurationMs < 60000) {
            return String.format("%.1fs", analysisDurationMs / 1000.0);
        } else {
            long minutes = analysisDurationMs / 60000;
            long seconds = (analysisDurationMs % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Get threat analysis summary
     */
    public String getThreatAnalysisSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat Analysis - Level: ").append(threatLevel);
        summary.append(", Score: ").append(threatScore);
        summary.append(", Confidence: ").append(confidenceLevel).append("%");
        summary.append(", Category: ").append(primaryThreatCategory != null ? primaryThreatCategory : "Unknown");
        summary.append(", Indicators: ").append(getThreatIndicatorCount());
        summary.append(", Attack Vectors: ").append(getAttackVectorCount());
        
        if (isCriticalThreat()) {
            summary.append(" [CRITICAL]");
        }
        
        if (needsEscalation()) {
            summary.append(" [ESCALATION REQUIRED]");
        }
        
        return summary.toString();
    }

    /**
     * Get response requirements summary
     */
    public String getResponseRequirementsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Response Requirements - ");
        
        if (hasAutomatedResponse()) {
            summary.append("Automated Response: Triggered, ");
        }
        
        if (needsManualInvestigation()) {
            summary.append("Manual Investigation: Required, ");
        }
        
        if (needsEscalation()) {
            summary.append("Escalation: Required, ");
        }
        
        if (hasImmediateActions()) {
            summary.append("Immediate Actions: ").append(immediateActions.size()).append(" items");
        }
        
        return summary.toString();
    }

    /**
     * Create a high-threat analysis result
     */
    public static ThreatAnalysisResult highThreat(String analysisId, String eventId, int threatScore, 
                                                String category, List<String> indicators) {
        return ThreatAnalysisResult.builder()
            .analysisId(analysisId)
            .eventId(eventId)
            .threatLevel(ThreatLevel.HIGH)
            .threatScore(threatScore)
            .primaryThreatCategory(category)
            .threatIndicators(indicators)
            .manualInvestigationRequired(true)
            .escalationRequired(threatScore >= 85)
            .analysisTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a low-threat analysis result
     */
    public static ThreatAnalysisResult lowThreat(String analysisId, String eventId, int threatScore) {
        return ThreatAnalysisResult.builder()
            .analysisId(analysisId)
            .eventId(eventId)
            .threatLevel(ThreatLevel.LOW)
            .threatScore(threatScore)
            .manualInvestigationRequired(false)
            .escalationRequired(false)
            .analysisTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate analysis result consistency
     */
    public boolean isValid() {
        return analysisId != null && !analysisId.trim().isEmpty() &&
               eventId != null && !eventId.trim().isEmpty() &&
               threatLevel != null &&
               threatScore != null && threatScore >= 0 && threatScore <= 100 &&
               confidenceLevel != null && confidenceLevel >= 0 && confidenceLevel <= 100 &&
               analysisTime != null &&
               (analysisDurationMs == null || analysisDurationMs >= 0);
    }
}
