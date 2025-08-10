package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.ThreatLevel;

import java.time.LocalDateTime;

/**
 * Zero Trust Security Metrics DTO
 * Contains comprehensive metrics for zero trust security monitoring
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZeroTrustMetrics {

    /**
     * Number of active users in the system
     */
    private int activeUsers;

    /**
     * Number of active devices registered
     */
    private int activeDevices;

    /**
     * Number of active sessions
     */
    private int activeSessions;

    /**
     * Total number of security events recorded
     */
    private long totalSecurityEvents;

    /**
     * Average trust score across all active sessions (0-100)
     */
    private double averageTrustScore;

    /**
     * Number of high-risk sessions requiring attention
     */
    private int highRiskSessions;

    /**
     * Number of blocked access attempts
     */
    private long blockedAccessAttempts;

    /**
     * Whether adaptive security is currently enabled
     */
    private boolean adaptiveSecurityEnabled;

    /**
     * Current threat level assessment
     */
    private ThreatLevel currentThreatLevel;

    /**
     * Timestamp when metrics were collected
     */
    private LocalDateTime timestamp;

    /**
     * Number of continuous verification checks performed
     */
    private long continuousVerificationChecks;

    /**
     * Number of failed verification attempts
     */
    private long failedVerificationAttempts;

    /**
     * Number of step-up authentication requests
     */
    private long stepUpAuthRequests;

    /**
     * Number of device registration attempts
     */
    private long deviceRegistrationAttempts;

    /**
     * Number of successful device registrations
     */
    private long successfulDeviceRegistrations;

    /**
     * Number of network segmentation violations detected
     */
    private long segmentationViolations;

    /**
     * Average session duration in minutes
     */
    private double averageSessionDuration;

    /**
     * Number of policy violations detected
     */
    private long policyViolations;

    /**
     * Calculate verification success rate as percentage
     */
    public double getVerificationSuccessRate() {
        if (continuousVerificationChecks == 0) {
            return 100.0; // No checks means 100% success
        }
        long successfulChecks = continuousVerificationChecks - failedVerificationAttempts;
        return (double) successfulChecks / continuousVerificationChecks * 100.0;
    }

    /**
     * Calculate device registration success rate as percentage
     */
    public double getDeviceRegistrationSuccessRate() {
        if (deviceRegistrationAttempts == 0) {
            return 0.0; // No attempts means 0% success
        }
        return (double) successfulDeviceRegistrations / deviceRegistrationAttempts * 100.0;
    }

    /**
     * Check if system is in high-risk state
     */
    public boolean isHighRiskState() {
        return currentThreatLevel == ThreatLevel.HIGH || 
               currentThreatLevel == ThreatLevel.CRITICAL ||
               averageTrustScore < 60.0 ||
               getVerificationSuccessRate() < 90.0;
    }

    /**
     * Check if immediate attention is required
     */
    public boolean requiresImmediateAttention() {
        return currentThreatLevel.requiresImmediateAttention() ||
               averageTrustScore < 50.0 ||
               getVerificationSuccessRate() < 80.0 ||
               highRiskSessions > (activeSessions * 0.2); // More than 20% high-risk sessions
    }

    /**
     * Get overall security health status
     */
    public String getSecurityHealthStatus() {
        if (requiresImmediateAttention()) {
            return "CRITICAL";
        } else if (isHighRiskState()) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Validate metrics consistency
     */
    public boolean isValid() {
        return activeUsers >= 0 &&
               activeDevices >= 0 &&
               activeSessions >= 0 &&
               averageTrustScore >= 0 && averageTrustScore <= 100 &&
               highRiskSessions <= activeSessions &&
               successfulDeviceRegistrations <= deviceRegistrationAttempts &&
               failedVerificationAttempts <= continuousVerificationChecks &&
               currentThreatLevel != null &&
               timestamp != null;
    }
}
