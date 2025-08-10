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
 * User Threat Profile DTO
 * Contains comprehensive user-specific threat profiling and risk assessment data
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserThreatProfile {

    /**
     * User ID for the threat profile
     */
    private String userId;

    /**
     * User's current threat level
     */
    private ThreatLevel currentThreatLevel;

    /**
     * Overall risk score (0-100, where 100 is highest risk)
     */
    private Integer overallRiskScore;

    /**
     * Behavioral risk score (0-100)
     */
    private Integer behavioralRiskScore;

    /**
     * Access pattern risk score (0-100)
     */
    private Integer accessPatternRiskScore;

    /**
     * Geographic risk score (0-100)
     */
    private Integer geographicRiskScore;

    /**
     * Device risk score (0-100)
     */
    private Integer deviceRiskScore;

    /**
     * Profile last updated timestamp
     */
    private LocalDateTime lastUpdated;

    /**
     * Profile creation timestamp
     */
    private LocalDateTime profileCreated;

    /**
     * Number of security incidents associated with user
     */
    private Integer securityIncidents;

    /**
     * Number of failed login attempts in last 24 hours
     */
    private Integer failedLoginAttempts24h;

    /**
     * Number of successful logins in last 24 hours
     */
    private Integer successfulLogins24h;

    /**
     * Number of unusual access patterns detected
     */
    private Integer unusualAccessPatterns;

    /**
     * Number of privilege escalation attempts
     */
    private Integer privilegeEscalationAttempts;

    /**
     * Number of data exfiltration indicators
     */
    private Integer dataExfiltrationIndicators;

    /**
     * Number of impossible travel incidents
     */
    private Integer impossibleTravelIncidents;

    /**
     * Number of velocity violations
     */
    private Integer velocityViolations;

    /**
     * User's typical login locations
     */
    private List<String> typicalLoginLocations;

    /**
     * User's typical access times (hours of day)
     */
    private List<Integer> typicalAccessHours;

    /**
     * User's registered devices
     */
    private List<String> registeredDevices;

    /**
     * Recent suspicious activities
     */
    private List<String> recentSuspiciousActivities;

    /**
     * Active threat indicators
     */
    private List<String> activeThreatIndicators;

    /**
     * Behavioral anomalies detected
     */
    private List<String> behavioralAnomalies;

    /**
     * Risk factors contributing to the profile
     */
    private Map<String, Integer> riskFactors;

    /**
     * User's access patterns summary
     */
    private Map<String, Object> accessPatternsSummary;

    /**
     * Geographic access history
     */
    private Map<String, Integer> geographicAccessHistory;

    /**
     * Device usage patterns
     */
    private Map<String, Object> deviceUsagePatterns;

    /**
     * Whether user is under investigation
     */
    @Builder.Default
    private Boolean underInvestigation = false;

    /**
     * Whether user requires enhanced monitoring
     */
    @Builder.Default
    private Boolean enhancedMonitoringRequired = false;

    /**
     * Whether user access should be restricted
     */
    @Builder.Default
    private Boolean accessRestrictionRequired = false;

    /**
     * Investigation notes (if under investigation)
     */
    private String investigationNotes;

    /**
     * Recommended security actions
     */
    private List<String> recommendedSecurityActions;

    /**
     * Last security training completion date
     */
    private LocalDateTime lastSecurityTraining;

    /**
     * User's security awareness score (0-100)
     */
    private Integer securityAwarenessScore;

    /**
     * Check if user is high risk
     */
    public boolean isHighRisk() {
        return currentThreatLevel == ThreatLevel.HIGH || 
               currentThreatLevel == ThreatLevel.CRITICAL ||
               overallRiskScore >= 70;
    }

    /**
     * Check if user is critical risk
     */
    public boolean isCriticalRisk() {
        return currentThreatLevel == ThreatLevel.CRITICAL || overallRiskScore >= 90;
    }

    /**
     * Check if user is under investigation
     */
    public boolean isUnderInvestigation() {
        return Boolean.TRUE.equals(underInvestigation);
    }

    /**
     * Check if enhanced monitoring is required
     */
    public boolean needsEnhancedMonitoring() {
        return Boolean.TRUE.equals(enhancedMonitoringRequired);
    }

    /**
     * Check if access restriction is required
     */
    public boolean needsAccessRestriction() {
        return Boolean.TRUE.equals(accessRestrictionRequired);
    }

    /**
     * Check if user has recent security incidents
     */
    public boolean hasRecentSecurityIncidents() {
        return securityIncidents != null && securityIncidents > 0;
    }

    /**
     * Check if user has suspicious activities
     */
    public boolean hasSuspiciousActivities() {
        return recentSuspiciousActivities != null && !recentSuspiciousActivities.isEmpty();
    }

    /**
     * Check if user has active threat indicators
     */
    public boolean hasActiveThreatIndicators() {
        return activeThreatIndicators != null && !activeThreatIndicators.isEmpty();
    }

    /**
     * Check if user has behavioral anomalies
     */
    public boolean hasBehavioralAnomalies() {
        return behavioralAnomalies != null && !behavioralAnomalies.isEmpty();
    }

    /**
     * Calculate login success rate in last 24 hours
     */
    public double getLoginSuccessRate24h() {
        int totalAttempts = (successfulLogins24h != null ? successfulLogins24h : 0) + 
                           (failedLoginAttempts24h != null ? failedLoginAttempts24h : 0);
        
        if (totalAttempts == 0) {
            return 100.0; // No attempts means 100% success
        }
        
        return (double) (successfulLogins24h != null ? successfulLogins24h : 0) / totalAttempts * 100.0;
    }

    /**
     * Get total threat indicator count
     */
    public int getTotalThreatIndicatorCount() {
        return activeThreatIndicators != null ? activeThreatIndicators.size() : 0;
    }

    /**
     * Get total anomaly count
     */
    public int getTotalAnomalyCount() {
        int count = 0;
        if (unusualAccessPatterns != null) count += unusualAccessPatterns;
        if (privilegeEscalationAttempts != null) count += privilegeEscalationAttempts;
        if (dataExfiltrationIndicators != null) count += dataExfiltrationIndicators;
        if (impossibleTravelIncidents != null) count += impossibleTravelIncidents;
        if (velocityViolations != null) count += velocityViolations;
        return count;
    }

    /**
     * Check if security training is up to date (within last 6 months)
     */
    public boolean isSecurityTrainingUpToDate() {
        if (lastSecurityTraining == null) {
            return false;
        }
        return lastSecurityTraining.isAfter(LocalDateTime.now().minusMonths(6));
    }

    /**
     * Get risk category based on overall risk score
     */
    public String getRiskCategory() {
        if (overallRiskScore >= 90) {
            return "CRITICAL";
        } else if (overallRiskScore >= 70) {
            return "HIGH";
        } else if (overallRiskScore >= 40) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Get threat profile summary
     */
    public String getThreatProfileSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("User Threat Profile - ID: ").append(userId);
        summary.append(", Risk Level: ").append(currentThreatLevel);
        summary.append(", Risk Score: ").append(overallRiskScore);
        summary.append(", Category: ").append(getRiskCategory());
        summary.append(", Incidents: ").append(securityIncidents);
        summary.append(", Anomalies: ").append(getTotalAnomalyCount());
        summary.append(", Threat Indicators: ").append(getTotalThreatIndicatorCount());
        
        if (isUnderInvestigation()) {
            summary.append(" [UNDER INVESTIGATION]");
        }
        
        if (needsEnhancedMonitoring()) {
            summary.append(" [ENHANCED MONITORING]");
        }
        
        if (needsAccessRestriction()) {
            summary.append(" [ACCESS RESTRICTED]");
        }
        
        return summary.toString();
    }

    /**
     * Get risk breakdown summary
     */
    public String getRiskBreakdownSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Risk Breakdown - ");
        summary.append("Behavioral: ").append(behavioralRiskScore);
        summary.append(", Access Pattern: ").append(accessPatternRiskScore);
        summary.append(", Geographic: ").append(geographicRiskScore);
        summary.append(", Device: ").append(deviceRiskScore);
        summary.append(", Security Awareness: ").append(securityAwarenessScore);
        
        return summary.toString();
    }

    /**
     * Get activity summary
     */
    public String getActivitySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Activity Summary - ");
        summary.append("Successful Logins (24h): ").append(successfulLogins24h);
        summary.append(", Failed Logins (24h): ").append(failedLoginAttempts24h);
        summary.append(", Success Rate: ").append(String.format("%.1f%%", getLoginSuccessRate24h()));
        summary.append(", Unusual Patterns: ").append(unusualAccessPatterns);
        summary.append(", Impossible Travel: ").append(impossibleTravelIncidents);
        
        return summary.toString();
    }

    /**
     * Create a high-risk user profile
     */
    public static UserThreatProfile highRisk(String userId, int riskScore, List<String> threatIndicators) {
        return UserThreatProfile.builder()
            .userId(userId)
            .currentThreatLevel(ThreatLevel.HIGH)
            .overallRiskScore(riskScore)
            .activeThreatIndicators(threatIndicators)
            .enhancedMonitoringRequired(true)
            .accessRestrictionRequired(riskScore >= 85)
            .lastUpdated(LocalDateTime.now())
            .profileCreated(LocalDateTime.now())
            .build();
    }

    /**
     * Create a low-risk user profile
     */
    public static UserThreatProfile lowRisk(String userId, int riskScore) {
        return UserThreatProfile.builder()
            .userId(userId)
            .currentThreatLevel(ThreatLevel.LOW)
            .overallRiskScore(riskScore)
            .enhancedMonitoringRequired(false)
            .accessRestrictionRequired(false)
            .lastUpdated(LocalDateTime.now())
            .profileCreated(LocalDateTime.now())
            .build();
    }

    /**
     * Validate threat profile consistency
     */
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty() &&
               currentThreatLevel != null &&
               overallRiskScore != null && overallRiskScore >= 0 && overallRiskScore <= 100 &&
               (behavioralRiskScore == null || (behavioralRiskScore >= 0 && behavioralRiskScore <= 100)) &&
               (accessPatternRiskScore == null || (accessPatternRiskScore >= 0 && accessPatternRiskScore <= 100)) &&
               (geographicRiskScore == null || (geographicRiskScore >= 0 && geographicRiskScore <= 100)) &&
               (deviceRiskScore == null || (deviceRiskScore >= 0 && deviceRiskScore <= 100)) &&
               (securityAwarenessScore == null || (securityAwarenessScore >= 0 && securityAwarenessScore <= 100)) &&
               lastUpdated != null &&
               profileCreated != null &&
               (securityIncidents == null || securityIncidents >= 0) &&
               (failedLoginAttempts24h == null || failedLoginAttempts24h >= 0) &&
               (successfulLogins24h == null || successfulLogins24h >= 0);
    }
}
