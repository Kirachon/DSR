package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// import ph.gov.dsr.security.zerotrust.model.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Access Decision DTO
 * Contains the result of zero-trust access evaluation including decision, risk assessment, and required actions
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessDecision {

    /**
     * Whether access is allowed or denied
     */
    private Boolean allowed;

    /**
     * Trust score that led to this decision (0-100)
     */
    private Integer trustScore;

    /**
     * Human-readable reason for the decision
     */
    private String reason;

    /**
     * Risk level assessment
     */
    private RiskLevel riskLevel;

    /**
     * Whether additional verification is required
     */
    private Boolean additionalVerificationRequired;

    /**
     * Session duration in minutes (if access granted)
     */
    private Integer sessionDuration;

    /**
     * List of required actions before access can be granted
     */
    private List<String> requiredActions;

    /**
     * Timestamp when decision was made
     */
    @Builder.Default
    private LocalDateTime decisionTime = LocalDateTime.now();

    /**
     * User ID for whom the decision was made
     */
    private String userId;

    /**
     * Device ID used for the access request
     */
    private String deviceId;

    /**
     * Resource ID that was requested
     */
    private String resourceId;

    /**
     * Session ID associated with the request
     */
    private String sessionId;

    /**
     * Source IP address of the request
     */
    private String sourceIp;

    /**
     * Detailed risk factors that influenced the decision
     */
    private Map<String, Object> riskFactors;

    /**
     * Policy violations detected during evaluation
     */
    private List<String> policyViolations;

    /**
     * Recommended security actions
     */
    private List<String> recommendedActions;

    /**
     * Time until decision expires (for temporary access)
     */
    private LocalDateTime expiresAt;

    /**
     * Whether this decision can be appealed
     */
    @Builder.Default
    private Boolean appealable = false;

    /**
     * Confidence level of the decision (0-100)
     */
    @Builder.Default
    private Integer confidenceLevel = 100;

    /**
     * Additional context information
     */
    private Map<String, Object> context;

    /**
     * Check if access is granted
     */
    public boolean isAccessGranted() {
        return Boolean.TRUE.equals(allowed);
    }

    /**
     * Check if access is denied
     */
    public boolean isAccessDenied() {
        return Boolean.FALSE.equals(allowed);
    }

    /**
     * Check if decision is high confidence
     */
    public boolean isHighConfidence() {
        return confidenceLevel != null && confidenceLevel >= 90;
    }

    /**
     * Check if decision is low confidence
     */
    public boolean isLowConfidence() {
        return confidenceLevel != null && confidenceLevel < 70;
    }

    /**
     * Check if decision has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if additional verification is needed
     */
    public boolean needsAdditionalVerification() {
        return Boolean.TRUE.equals(additionalVerificationRequired);
    }

    /**
     * Check if there are policy violations
     */
    public boolean hasPolicyViolations() {
        return policyViolations != null && !policyViolations.isEmpty();
    }

    /**
     * Check if there are required actions
     */
    public boolean hasRequiredActions() {
        return requiredActions != null && !requiredActions.isEmpty();
    }

    /**
     * Get decision summary for logging
     */
    public String getDecisionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Access ").append(isAccessGranted() ? "GRANTED" : "DENIED");
        summary.append(" for user ").append(userId);
        summary.append(" to resource ").append(resourceId);
        summary.append(" (Trust Score: ").append(trustScore);
        summary.append(", Risk: ").append(riskLevel);
        summary.append(", Confidence: ").append(confidenceLevel).append("%)");
        
        if (needsAdditionalVerification()) {
            summary.append(" - Additional verification required");
        }
        
        return summary.toString();
    }

    /**
     * Get risk assessment summary
     */
    public String getRiskAssessmentSummary() {
        StringBuilder assessment = new StringBuilder();
        assessment.append("Risk Level: ").append(riskLevel);
        assessment.append(", Trust Score: ").append(trustScore);
        
        if (hasPolicyViolations()) {
            assessment.append(", Policy Violations: ").append(policyViolations.size());
        }
        
        if (riskFactors != null && !riskFactors.isEmpty()) {
            assessment.append(", Risk Factors: ").append(riskFactors.size());
        }
        
        return assessment.toString();
    }

    /**
     * Create a denied access decision
     */
    public static AccessDecision denied(String reason, RiskLevel riskLevel) {
        return AccessDecision.builder()
            .allowed(false)
            .reason(reason)
            .riskLevel(riskLevel)
            .trustScore(0)
            .additionalVerificationRequired(true)
            .decisionTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a granted access decision
     */
    public static AccessDecision granted(String reason, int trustScore, RiskLevel riskLevel, int sessionDuration) {
        return AccessDecision.builder()
            .allowed(true)
            .reason(reason)
            .trustScore(trustScore)
            .riskLevel(riskLevel)
            .sessionDuration(sessionDuration)
            .additionalVerificationRequired(false)
            .decisionTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate decision consistency
     */
    public boolean isValid() {
        return allowed != null &&
               reason != null && !reason.trim().isEmpty() &&
               riskLevel != null &&
               trustScore != null && trustScore >= 0 && trustScore <= 100 &&
               confidenceLevel != null && confidenceLevel >= 0 && confidenceLevel <= 100 &&
               decisionTime != null &&
               (sessionDuration == null || sessionDuration >= 0);
    }
}
