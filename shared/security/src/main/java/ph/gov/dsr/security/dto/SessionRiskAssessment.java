package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
 * Session Risk Assessment DTO
 * Contains comprehensive risk evaluation for user sessions in continuous authentication
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRiskAssessment {

    /**
     * Session ID being assessed
     */
    private String sessionId;

    /**
     * User ID for the session
     */
    private String userId;

    /**
     * Device ID for the session
     */
    private String deviceId;

    /**
     * Overall risk level assessment
     */
    private RiskLevel riskLevel;

    /**
     * Risk score (0-100, where 100 is highest risk)
     */
    private Integer riskScore;

    /**
     * Trust score (0-100, where 100 is highest trust)
     */
    private Integer trustScore;

    /**
     * Assessment timestamp
     */
    @Builder.Default
    private LocalDateTime assessmentTime = LocalDateTime.now();

    /**
     * Session start time
     */
    private LocalDateTime sessionStartTime;

    /**
     * Last activity timestamp
     */
    private LocalDateTime lastActivityTime;

    /**
     * Session duration in minutes
     */
    private Long sessionDurationMinutes;

    /**
     * Time since last verification in minutes
     */
    private Long timeSinceLastVerificationMinutes;

    /**
     * Behavioral risk factors detected
     */
    private List<String> behavioralRiskFactors;

    /**
     * Device risk factors detected
     */
    private List<String> deviceRiskFactors;

    /**
     * Network risk factors detected
     */
    private List<String> networkRiskFactors;

    /**
     * Location risk factors detected
     */
    private List<String> locationRiskFactors;

    /**
     * Anomalies detected in user behavior
     */
    private List<String> behavioralAnomalies;

    /**
     * Current IP address
     */
    private String currentIpAddress;

    /**
     * Previous IP addresses in session
     */
    private List<String> previousIpAddresses;

    /**
     * Geographic location changes
     */
    private List<String> locationChanges;

    /**
     * Device fingerprint changes
     */
    private List<String> deviceChanges;

    /**
     * Authentication method used
     */
    private String authenticationMethod;

    /**
     * Whether MFA was completed
     */
    private Boolean mfaCompleted;

    /**
     * Time since last MFA in minutes
     */
    private Long timeSinceLastMfaMinutes;

    /**
     * Failed authentication attempts in session
     */
    private Integer failedAuthAttempts;

    /**
     * Suspicious activities detected
     */
    private List<String> suspiciousActivities;

    /**
     * Policy violations in session
     */
    private List<String> policyViolations;

    /**
     * Threat indicators detected
     */
    private List<String> threatIndicators;

    /**
     * Recommended actions based on assessment
     */
    private List<String> recommendedActions;

    /**
     * Whether step-up authentication is required
     */
    @Builder.Default
    private Boolean stepUpAuthRequired = false;

    /**
     * Whether session should be terminated
     */
    @Builder.Default
    private Boolean sessionTerminationRequired = false;

    /**
     * Whether additional monitoring is required
     */
    @Builder.Default
    private Boolean additionalMonitoringRequired = false;

    /**
     * Risk assessment confidence level (0-100)
     */
    @Builder.Default
    private Integer confidenceLevel = 100;

    /**
     * Additional context information
     */
    private Map<String, Object> context;

    /**
     * Risk breakdown by category
     */
    private Map<String, Integer> riskBreakdown;

    /**
     * Check if session is high risk
     */
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }

    /**
     * Check if session is low risk
     */
    public boolean isLowRisk() {
        return riskLevel == RiskLevel.LOW;
    }

    /**
     * Check if session requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return riskLevel == RiskLevel.CRITICAL ||
               Boolean.TRUE.equals(sessionTerminationRequired) ||
               (riskScore != null && riskScore > 80);
    }

    /**
     * Check if step-up authentication is needed
     */
    public boolean needsStepUpAuth() {
        return Boolean.TRUE.equals(stepUpAuthRequired);
    }

    /**
     * Check if session should be terminated
     */
    public boolean shouldTerminateSession() {
        return Boolean.TRUE.equals(sessionTerminationRequired);
    }

    /**
     * Check if additional monitoring is needed
     */
    public boolean needsAdditionalMonitoring() {
        return Boolean.TRUE.equals(additionalMonitoringRequired);
    }

    /**
     * Check if there are behavioral anomalies
     */
    public boolean hasBehavioralAnomalies() {
        return behavioralAnomalies != null && !behavioralAnomalies.isEmpty();
    }

    /**
     * Check if there are threat indicators
     */
    public boolean hasThreatIndicators() {
        return threatIndicators != null && !threatIndicators.isEmpty();
    }

    /**
     * Check if there are policy violations
     */
    public boolean hasPolicyViolations() {
        return policyViolations != null && !policyViolations.isEmpty();
    }

    /**
     * Check if location has changed
     */
    public boolean hasLocationChanges() {
        return locationChanges != null && !locationChanges.isEmpty();
    }

    /**
     * Check if device has changed
     */
    public boolean hasDeviceChanges() {
        return deviceChanges != null && !deviceChanges.isEmpty();
    }

    /**
     * Get total risk factor count
     */
    public int getTotalRiskFactorCount() {
        int count = 0;
        if (behavioralRiskFactors != null) count += behavioralRiskFactors.size();
        if (deviceRiskFactors != null) count += deviceRiskFactors.size();
        if (networkRiskFactors != null) count += networkRiskFactors.size();
        if (locationRiskFactors != null) count += locationRiskFactors.size();
        return count;
    }

    /**
     * Get session age in hours
     */
    public double getSessionAgeHours() {
        if (sessionStartTime == null) {
            return 0.0;
        }
        return sessionDurationMinutes != null ? sessionDurationMinutes / 60.0 : 0.0;
    }

    /**
     * Check if session is stale (no recent activity)
     */
    public boolean isSessionStale(int maxInactiveMinutes) {
        if (lastActivityTime == null) {
            return false;
        }
        return LocalDateTime.now().minusMinutes(maxInactiveMinutes).isAfter(lastActivityTime);
    }

    /**
     * Check if verification is stale
     */
    public boolean isVerificationStale(int maxMinutes) {
        return timeSinceLastVerificationMinutes != null && 
               timeSinceLastVerificationMinutes > maxMinutes;
    }

    /**
     * Check if MFA is stale
     */
    public boolean isMfaStale(int maxMinutes) {
        return timeSinceLastMfaMinutes != null && 
               timeSinceLastMfaMinutes > maxMinutes;
    }

    /**
     * Get risk assessment summary
     */
    public String getRiskAssessmentSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Session ").append(sessionId);
        summary.append(" - Risk: ").append(riskLevel);
        summary.append(" (Score: ").append(riskScore).append(")");
        summary.append(", Trust: ").append(trustScore);
        summary.append(", Factors: ").append(getTotalRiskFactorCount());
        
        if (needsStepUpAuth()) {
            summary.append(" [STEP-UP REQUIRED]");
        }
        
        if (shouldTerminateSession()) {
            summary.append(" [TERMINATE]");
        }
        
        return summary.toString();
    }

    /**
     * Create a low-risk assessment
     */
    public static SessionRiskAssessment lowRisk(String sessionId, String userId, String deviceId, int trustScore) {
        return SessionRiskAssessment.builder()
            .sessionId(sessionId)
            .userId(userId)
            .deviceId(deviceId)
            .riskLevel(RiskLevel.LOW)
            .riskScore(20)
            .trustScore(trustScore)
            .assessmentTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a high-risk assessment
     */
    public static SessionRiskAssessment highRisk(String sessionId, String userId, String deviceId, 
                                               List<String> riskFactors, boolean requiresStepUp) {
        return SessionRiskAssessment.builder()
            .sessionId(sessionId)
            .userId(userId)
            .deviceId(deviceId)
            .riskLevel(RiskLevel.HIGH)
            .riskScore(80)
            .trustScore(30)
            .behavioralRiskFactors(riskFactors)
            .stepUpAuthRequired(requiresStepUp)
            .additionalMonitoringRequired(true)
            .assessmentTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate assessment consistency
     */
    public boolean isValid() {
        return sessionId != null && !sessionId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               deviceId != null && !deviceId.trim().isEmpty() &&
               riskLevel != null &&
               riskScore != null && riskScore >= 0 && riskScore <= 100 &&
               trustScore != null && trustScore >= 0 && trustScore <= 100 &&
               confidenceLevel != null && confidenceLevel >= 0 && confidenceLevel <= 100 &&
               assessmentTime != null;
    }
}
