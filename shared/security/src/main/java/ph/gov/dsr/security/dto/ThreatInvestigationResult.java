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
 * Threat Investigation Result DTO
 * Contains comprehensive threat investigation operation results including findings and recommendations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatInvestigationResult {

    /**
     * Unique investigation ID
     */
    private String investigationId;

    /**
     * Threat ID being investigated
     */
    private String threatId;

    /**
     * Investigation status (INITIATED, IN_PROGRESS, COMPLETED, CLOSED)
     */
    private String investigationStatus;

    /**
     * Investigation priority (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String investigationPriority;

    /**
     * Final threat level assessment
     */
    private ThreatLevel finalThreatLevel;

    /**
     * Investigation confidence level (0-100)
     */
    @Builder.Default
    private Integer confidenceLevel = 100;

    /**
     * Investigation start timestamp
     */
    private LocalDateTime investigationStartTime;

    /**
     * Investigation completion timestamp
     */
    private LocalDateTime investigationCompletionTime;

    /**
     * Total investigation duration in hours
     */
    private Double investigationDurationHours;

    /**
     * Investigator ID
     */
    private String investigatorId;

    /**
     * Investigation team members
     */
    private List<String> investigationTeam;

    /**
     * Investigation findings summary
     */
    private String investigationFindings;

    /**
     * Detailed investigation notes
     */
    private String detailedNotes;

    /**
     * Evidence collected during investigation
     */
    private List<String> evidenceCollected;

    /**
     * Threat indicators confirmed
     */
    private List<String> confirmedThreatIndicators;

    /**
     * False positive indicators identified
     */
    private List<String> falsePositiveIndicators;

    /**
     * Attack vectors identified
     */
    private List<String> identifiedAttackVectors;

    /**
     * Affected systems or users
     */
    private List<String> affectedSystems;

    /**
     * Root cause analysis
     */
    private String rootCauseAnalysis;

    /**
     * Impact assessment
     */
    private String impactAssessment;

    /**
     * Containment actions taken
     */
    private List<String> containmentActions;

    /**
     * Remediation actions taken
     */
    private List<String> remediationActions;

    /**
     * Recommended follow-up actions
     */
    private List<String> recommendedFollowUpActions;

    /**
     * Lessons learned
     */
    private String lessonsLearned;

    /**
     * Whether threat was confirmed as malicious
     */
    @Builder.Default
    private Boolean threatConfirmed = false;

    /**
     * Whether threat was contained
     */
    @Builder.Default
    private Boolean threatContained = false;

    /**
     * Whether threat was fully remediated
     */
    @Builder.Default
    private Boolean threatRemediated = false;

    /**
     * Whether incident escalation was required
     */
    @Builder.Default
    private Boolean escalationRequired = false;

    /**
     * Whether law enforcement was notified
     */
    @Builder.Default
    private Boolean lawEnforcementNotified = false;

    /**
     * Whether regulatory reporting was required
     */
    @Builder.Default
    private Boolean regulatoryReportingRequired = false;

    /**
     * Investigation tools used
     */
    private List<String> investigationToolsUsed;

    /**
     * External resources consulted
     */
    private List<String> externalResourcesConsulted;

    /**
     * Related incidents or investigations
     */
    private List<String> relatedIncidents;

    /**
     * Investigation metrics
     */
    private Map<String, Object> investigationMetrics;

    /**
     * Additional investigation context
     */
    private Map<String, Object> additionalContext;

    /**
     * Check if investigation is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(investigationStatus) || 
               "CLOSED".equalsIgnoreCase(investigationStatus);
    }

    /**
     * Check if investigation is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equalsIgnoreCase(investigationStatus);
    }

    /**
     * Check if threat was confirmed
     */
    public boolean isThreatConfirmed() {
        return Boolean.TRUE.equals(threatConfirmed);
    }

    /**
     * Check if threat was contained
     */
    public boolean isThreatContained() {
        return Boolean.TRUE.equals(threatContained);
    }

    /**
     * Check if threat was remediated
     */
    public boolean isThreatRemediated() {
        return Boolean.TRUE.equals(threatRemediated);
    }

    /**
     * Check if escalation was required
     */
    public boolean wasEscalationRequired() {
        return Boolean.TRUE.equals(escalationRequired);
    }

    /**
     * Check if law enforcement was notified
     */
    public boolean wasLawEnforcementNotified() {
        return Boolean.TRUE.equals(lawEnforcementNotified);
    }

    /**
     * Check if regulatory reporting was required
     */
    public boolean wasRegulatoryReportingRequired() {
        return Boolean.TRUE.equals(regulatoryReportingRequired);
    }

    /**
     * Check if investigation is high confidence
     */
    public boolean isHighConfidence() {
        return confidenceLevel >= 90;
    }

    /**
     * Check if investigation is low confidence
     */
    public boolean isLowConfidence() {
        return confidenceLevel < 70;
    }

    /**
     * Check if investigation has evidence
     */
    public boolean hasEvidence() {
        return evidenceCollected != null && !evidenceCollected.isEmpty();
    }

    /**
     * Check if investigation has confirmed indicators
     */
    public boolean hasConfirmedIndicators() {
        return confirmedThreatIndicators != null && !confirmedThreatIndicators.isEmpty();
    }

    /**
     * Check if investigation identified false positives
     */
    public boolean hasFalsePositives() {
        return falsePositiveIndicators != null && !falsePositiveIndicators.isEmpty();
    }

    /**
     * Calculate investigation duration if completed
     */
    public Double calculateInvestigationDuration() {
        if (investigationStartTime == null || investigationCompletionTime == null) {
            return null;
        }
        
        long durationMinutes = java.time.Duration.between(investigationStartTime, investigationCompletionTime).toMinutes();
        return durationMinutes / 60.0; // Convert to hours
    }

    /**
     * Get evidence count
     */
    public int getEvidenceCount() {
        return evidenceCollected != null ? evidenceCollected.size() : 0;
    }

    /**
     * Get confirmed indicator count
     */
    public int getConfirmedIndicatorCount() {
        return confirmedThreatIndicators != null ? confirmedThreatIndicators.size() : 0;
    }

    /**
     * Get false positive count
     */
    public int getFalsePositiveCount() {
        return falsePositiveIndicators != null ? falsePositiveIndicators.size() : 0;
    }

    /**
     * Get investigation outcome
     */
    public String getInvestigationOutcome() {
        if (!isCompleted()) {
            return "ONGOING";
        }
        
        if (isThreatConfirmed()) {
            if (isThreatRemediated()) {
                return "THREAT_CONFIRMED_AND_REMEDIATED";
            } else if (isThreatContained()) {
                return "THREAT_CONFIRMED_AND_CONTAINED";
            } else {
                return "THREAT_CONFIRMED_ACTIVE";
            }
        } else {
            return "FALSE_POSITIVE";
        }
    }

    /**
     * Get investigation summary
     */
    public String getInvestigationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Investigation ").append(investigationId);
        summary.append(" - Status: ").append(investigationStatus);
        summary.append(", Priority: ").append(investigationPriority);
        summary.append(", Threat Level: ").append(finalThreatLevel);
        summary.append(", Outcome: ").append(getInvestigationOutcome());
        summary.append(", Confidence: ").append(confidenceLevel).append("%");
        
        if (investigationDurationHours != null) {
            summary.append(", Duration: ").append(String.format("%.1fh", investigationDurationHours));
        }
        
        if (hasEvidence()) {
            summary.append(", Evidence: ").append(getEvidenceCount()).append(" items");
        }
        
        if (wasEscalationRequired()) {
            summary.append(" [ESCALATED]");
        }
        
        if (wasLawEnforcementNotified()) {
            summary.append(" [LAW ENFORCEMENT]");
        }
        
        return summary.toString();
    }

    /**
     * Get response actions summary
     */
    public String getResponseActionsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Response Actions - ");
        
        if (containmentActions != null && !containmentActions.isEmpty()) {
            summary.append("Containment: ").append(containmentActions.size()).append(" actions, ");
        }
        
        if (remediationActions != null && !remediationActions.isEmpty()) {
            summary.append("Remediation: ").append(remediationActions.size()).append(" actions, ");
        }
        
        if (recommendedFollowUpActions != null && !recommendedFollowUpActions.isEmpty()) {
            summary.append("Follow-up: ").append(recommendedFollowUpActions.size()).append(" recommendations");
        }
        
        return summary.toString();
    }

    /**
     * Create a completed investigation result
     */
    public static ThreatInvestigationResult completed(String investigationId, String threatId, 
                                                    ThreatLevel threatLevel, boolean confirmed, String findings) {
        return ThreatInvestigationResult.builder()
            .investigationId(investigationId)
            .threatId(threatId)
            .investigationStatus("COMPLETED")
            .finalThreatLevel(threatLevel)
            .threatConfirmed(confirmed)
            .investigationFindings(findings)
            .investigationStartTime(LocalDateTime.now().minusHours(2))
            .investigationCompletionTime(LocalDateTime.now())
            .investigationDurationHours(2.0)
            .build();
    }

    /**
     * Create an in-progress investigation result
     */
    public static ThreatInvestigationResult inProgress(String investigationId, String threatId, 
                                                     String priority, String investigatorId) {
        return ThreatInvestigationResult.builder()
            .investigationId(investigationId)
            .threatId(threatId)
            .investigationStatus("IN_PROGRESS")
            .investigationPriority(priority)
            .investigatorId(investigatorId)
            .finalThreatLevel(ThreatLevel.MEDIUM) // Preliminary assessment
            .investigationStartTime(LocalDateTime.now())
            .investigationFindings("Investigation in progress...")
            .build();
    }

    /**
     * Validate investigation result consistency
     */
    public boolean isValid() {
        return investigationId != null && !investigationId.trim().isEmpty() &&
               threatId != null && !threatId.trim().isEmpty() &&
               investigationStatus != null && !investigationStatus.trim().isEmpty() &&
               investigationPriority != null && !investigationPriority.trim().isEmpty() &&
               finalThreatLevel != null &&
               confidenceLevel != null && confidenceLevel >= 0 && confidenceLevel <= 100 &&
               investigationStartTime != null &&
               investigationFindings != null && !investigationFindings.trim().isEmpty() &&
               (investigationDurationHours == null || investigationDurationHours >= 0) &&
               (investigationCompletionTime == null || 
                investigationCompletionTime.isAfter(investigationStartTime));
    }
}
