package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Step-Up Authentication Result DTO
 * Contains the result of step-up authentication operations including success status and security details
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepUpAuthResult {

    /**
     * Whether step-up authentication was successful
     */
    private Boolean success;

    /**
     * Session ID for the authentication request
     */
    private String sessionId;

    /**
     * User ID who performed step-up authentication
     */
    private String userId;

    /**
     * Authentication method used for step-up
     */
    private String authenticationMethod;

    /**
     * Human-readable result message
     */
    private String message;

    /**
     * Authentication timestamp
     */
    @Builder.Default
    private LocalDateTime authenticationTime = LocalDateTime.now();

    /**
     * Challenge ID that was responded to
     */
    private String challengeId;

    /**
     * Device ID used for authentication
     */
    private String deviceId;

    /**
     * IP address used for authentication
     */
    private String ipAddress;

    /**
     * Geographic location during authentication
     */
    private String geolocation;

    /**
     * Authentication token/credential provided
     */
    private String authenticationToken;

    /**
     * Biometric data used (if applicable)
     */
    private String biometricData;

    /**
     * Time taken to complete authentication in seconds
     */
    private Integer authenticationDurationSeconds;

    /**
     * Number of attempts made
     */
    @Builder.Default
    private Integer attemptCount = 1;

    /**
     * Whether this was the final attempt allowed
     */
    @Builder.Default
    private Boolean finalAttempt = false;

    /**
     * Remaining attempts (if failed)
     */
    private Integer remainingAttempts;

    /**
     * Lockout duration in minutes (if account locked)
     */
    private Integer lockoutDurationMinutes;

    /**
     * Error code (if authentication failed)
     */
    private String errorCode;

    /**
     * Detailed error information (if authentication failed)
     */
    private String errorDetails;

    /**
     * Security warnings detected during authentication
     */
    private List<String> securityWarnings;

    /**
     * Risk factors identified during authentication
     */
    private List<String> riskFactors;

    /**
     * Fraud indicators detected
     */
    private List<String> fraudIndicators;

    /**
     * Authentication strength score (0-100)
     */
    private Integer authenticationStrength;

    /**
     * Confidence level of the authentication (0-100)
     */
    @Builder.Default
    private Integer confidenceLevel = 100;

    /**
     * Whether additional verification is still required
     */
    @Builder.Default
    private Boolean additionalVerificationRequired = false;

    /**
     * Next authentication methods available
     */
    private List<String> nextAuthMethods;

    /**
     * Session trust level after authentication
     */
    private String trustLevel;

    /**
     * Session validity duration in minutes
     */
    private Integer sessionValidityMinutes;

    /**
     * Additional context information
     */
    private Map<String, Object> context;

    /**
     * Audit trail information
     */
    private Map<String, Object> auditInfo;

    /**
     * Check if authentication was successful
     */
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * Check if authentication failed
     */
    public boolean isFailed() {
        return Boolean.FALSE.equals(success);
    }

    /**
     * Check if this was the final attempt
     */
    public boolean isFinalAttempt() {
        return Boolean.TRUE.equals(finalAttempt);
    }

    /**
     * Check if additional verification is needed
     */
    public boolean needsAdditionalVerification() {
        return Boolean.TRUE.equals(additionalVerificationRequired);
    }

    /**
     * Check if account is locked out
     */
    public boolean isAccountLockedOut() {
        return lockoutDurationMinutes != null && lockoutDurationMinutes > 0;
    }

    /**
     * Check if there are security warnings
     */
    public boolean hasSecurityWarnings() {
        return securityWarnings != null && !securityWarnings.isEmpty();
    }

    /**
     * Check if there are risk factors
     */
    public boolean hasRiskFactors() {
        return riskFactors != null && !riskFactors.isEmpty();
    }

    /**
     * Check if fraud indicators were detected
     */
    public boolean hasFraudIndicators() {
        return fraudIndicators != null && !fraudIndicators.isEmpty();
    }

    /**
     * Check if authentication is high confidence
     */
    public boolean isHighConfidence() {
        return confidenceLevel != null && confidenceLevel >= 90;
    }

    /**
     * Check if authentication is low confidence
     */
    public boolean isLowConfidence() {
        return confidenceLevel != null && confidenceLevel < 70;
    }

    /**
     * Check if authentication is strong
     */
    public boolean isStrongAuthentication() {
        return authenticationStrength != null && authenticationStrength >= 80;
    }

    /**
     * Check if more attempts are available
     */
    public boolean hasRemainingAttempts() {
        return remainingAttempts != null && remainingAttempts > 0;
    }

    /**
     * Check if next authentication methods are available
     */
    public boolean hasNextAuthMethods() {
        return nextAuthMethods != null && !nextAuthMethods.isEmpty();
    }

    /**
     * Get authentication duration in a human-readable format
     */
    public String getFormattedDuration() {
        if (authenticationDurationSeconds == null) {
            return "Unknown";
        }
        
        if (authenticationDurationSeconds < 60) {
            return authenticationDurationSeconds + " seconds";
        } else {
            int minutes = authenticationDurationSeconds / 60;
            int seconds = authenticationDurationSeconds % 60;
            return minutes + " minutes " + seconds + " seconds";
        }
    }

    /**
     * Get authentication summary for logging
     */
    public String getAuthenticationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Step-up authentication ");
        summary.append(isSuccessful() ? "SUCCESSFUL" : "FAILED");
        summary.append(" for user ").append(userId);
        summary.append(" using ").append(authenticationMethod);
        summary.append(" (attempt ").append(attemptCount).append(")");
        
        if (authenticationStrength != null) {
            summary.append(" - Strength: ").append(authenticationStrength);
        }
        
        if (hasSecurityWarnings()) {
            summary.append(" - ").append(securityWarnings.size()).append(" warnings");
        }
        
        return summary.toString();
    }

    /**
     * Get security assessment summary
     */
    public String getSecurityAssessmentSummary() {
        StringBuilder assessment = new StringBuilder();
        assessment.append("Confidence: ").append(confidenceLevel).append("%");
        
        if (authenticationStrength != null) {
            assessment.append(", Strength: ").append(authenticationStrength);
        }
        
        if (hasRiskFactors()) {
            assessment.append(", Risk Factors: ").append(riskFactors.size());
        }
        
        if (hasFraudIndicators()) {
            assessment.append(", Fraud Indicators: ").append(fraudIndicators.size());
        }
        
        return assessment.toString();
    }

    /**
     * Create a successful authentication result
     */
    public static StepUpAuthResult success(String sessionId, String userId, String authMethod, String message) {
        return StepUpAuthResult.builder()
            .success(true)
            .sessionId(sessionId)
            .userId(userId)
            .authenticationMethod(authMethod)
            .message(message)
            .authenticationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a failed authentication result
     */
    public static StepUpAuthResult failed(String sessionId, String userId, String authMethod, 
                                        String message, int remainingAttempts) {
        return StepUpAuthResult.builder()
            .success(false)
            .sessionId(sessionId)
            .userId(userId)
            .authenticationMethod(authMethod)
            .message(message)
            .remainingAttempts(remainingAttempts)
            .finalAttempt(remainingAttempts <= 0)
            .authenticationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a failed authentication result with lockout
     */
    public static StepUpAuthResult failedWithLockout(String sessionId, String userId, String authMethod, 
                                                   String message, int lockoutMinutes) {
        return StepUpAuthResult.builder()
            .success(false)
            .sessionId(sessionId)
            .userId(userId)
            .authenticationMethod(authMethod)
            .message(message)
            .remainingAttempts(0)
            .finalAttempt(true)
            .lockoutDurationMinutes(lockoutMinutes)
            .authenticationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate result consistency
     */
    public boolean isValid() {
        return success != null &&
               sessionId != null && !sessionId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               authenticationMethod != null && !authenticationMethod.trim().isEmpty() &&
               message != null && !message.trim().isEmpty() &&
               authenticationTime != null &&
               attemptCount != null && attemptCount > 0 &&
               (confidenceLevel == null || (confidenceLevel >= 0 && confidenceLevel <= 100)) &&
               (authenticationStrength == null || (authenticationStrength >= 0 && authenticationStrength <= 100));
    }
}
