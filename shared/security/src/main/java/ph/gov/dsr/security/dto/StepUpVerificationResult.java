package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.StepUpAuthType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Step-Up Verification Result DTO
 * Contains the result of step-up authentication verification including validation status and security assessment
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepUpVerificationResult {

    /**
     * Whether verification was successful
     */
    private Boolean verified;

    /**
     * Session ID for the verification request
     */
    private String sessionId;

    /**
     * Challenge ID that was verified
     */
    private String challengeId;

    /**
     * User ID who performed the verification
     */
    private String userId;

    /**
     * Authentication type used for verification
     */
    private StepUpAuthType authType;

    /**
     * Human-readable verification result message
     */
    private String message;

    /**
     * Verification timestamp
     */
    @Builder.Default
    private LocalDateTime verificationTime = LocalDateTime.now();

    /**
     * Challenge creation timestamp
     */
    private LocalDateTime challengeCreatedTime;

    /**
     * Time taken to respond to challenge in seconds
     */
    private Integer responseTimeSeconds;

    /**
     * Verification confidence level (0-100)
     */
    @Builder.Default
    private Integer confidenceLevel = 100;

    /**
     * Security strength of the verification (0-100)
     */
    private Integer securityStrength;

    /**
     * Device ID used for verification
     */
    private String deviceId;

    /**
     * IP address used for verification
     */
    private String ipAddress;

    /**
     * Geographic location during verification
     */
    private String geolocation;

    /**
     * User agent string
     */
    private String userAgent;

    /**
     * Verification token/code provided
     */
    private String verificationToken;

    /**
     * Biometric match score (if biometric verification)
     */
    private Integer biometricMatchScore;

    /**
     * Number of verification attempts made
     */
    @Builder.Default
    private Integer attemptCount = 1;

    /**
     * Whether this was the final attempt allowed
     */
    @Builder.Default
    private Boolean finalAttempt = false;

    /**
     * Remaining attempts (if verification failed)
     */
    private Integer remainingAttempts;

    /**
     * Error code (if verification failed)
     */
    private String errorCode;

    /**
     * Detailed error information (if verification failed)
     */
    private String errorDetails;

    /**
     * Security warnings detected during verification
     */
    private List<String> securityWarnings;

    /**
     * Risk factors identified during verification
     */
    private List<String> riskFactors;

    /**
     * Fraud indicators detected
     */
    private List<String> fraudIndicators;

    /**
     * Anomalies detected during verification
     */
    private List<String> anomalies;

    /**
     * Whether additional verification steps are required
     */
    @Builder.Default
    private Boolean additionalStepsRequired = false;

    /**
     * Next verification methods available
     */
    private List<StepUpAuthType> nextVerificationMethods;

    /**
     * Session trust level after verification
     */
    private String trustLevel;

    /**
     * Verification validity duration in minutes
     */
    private Integer validityDurationMinutes;

    /**
     * Challenge expiry time
     */
    private LocalDateTime challengeExpiryTime;

    /**
     * Additional context information
     */
    private Map<String, Object> context;

    /**
     * Audit trail information
     */
    private Map<String, Object> auditInfo;

    /**
     * Check if verification was successful
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(verified);
    }

    /**
     * Check if verification failed
     */
    public boolean isFailed() {
        return Boolean.FALSE.equals(verified);
    }

    /**
     * Check if this was the final attempt
     */
    public boolean isFinalAttempt() {
        return Boolean.TRUE.equals(finalAttempt);
    }

    /**
     * Check if additional steps are required
     */
    public boolean needsAdditionalSteps() {
        return Boolean.TRUE.equals(additionalStepsRequired);
    }

    /**
     * Check if challenge has expired
     */
    public boolean isChallengeExpired() {
        return challengeExpiryTime != null && LocalDateTime.now().isAfter(challengeExpiryTime);
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
     * Check if anomalies were detected
     */
    public boolean hasAnomalies() {
        return anomalies != null && !anomalies.isEmpty();
    }

    /**
     * Check if verification is high confidence
     */
    public boolean isHighConfidence() {
        return confidenceLevel != null && confidenceLevel >= 90;
    }

    /**
     * Check if verification is low confidence
     */
    public boolean isLowConfidence() {
        return confidenceLevel != null && confidenceLevel < 70;
    }

    /**
     * Check if verification is strong
     */
    public boolean isStrongVerification() {
        return securityStrength != null && securityStrength >= 80;
    }

    /**
     * Check if more attempts are available
     */
    public boolean hasRemainingAttempts() {
        return remainingAttempts != null && remainingAttempts > 0;
    }

    /**
     * Check if next verification methods are available
     */
    public boolean hasNextVerificationMethods() {
        return nextVerificationMethods != null && !nextVerificationMethods.isEmpty();
    }

    /**
     * Check if response was within expected time
     */
    public boolean isResponseTimeNormal() {
        if (responseTimeSeconds == null || authType == null) {
            return true; // Assume normal if no data
        }
        return responseTimeSeconds <= authType.getExpectedResponseTimeSeconds();
    }

    /**
     * Get response time in a human-readable format
     */
    public String getFormattedResponseTime() {
        if (responseTimeSeconds == null) {
            return "Unknown";
        }
        
        if (responseTimeSeconds < 60) {
            return responseTimeSeconds + " seconds";
        } else {
            int minutes = responseTimeSeconds / 60;
            int seconds = responseTimeSeconds % 60;
            return minutes + " minutes " + seconds + " seconds";
        }
    }

    /**
     * Get verification summary for logging
     */
    public String getVerificationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Step-up verification ");
        summary.append(isVerified() ? "SUCCESSFUL" : "FAILED");
        summary.append(" for user ").append(userId);
        summary.append(" using ").append(authType.getDisplayName());
        summary.append(" (attempt ").append(attemptCount).append(")");
        
        if (confidenceLevel != null) {
            summary.append(" - Confidence: ").append(confidenceLevel).append("%");
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
        
        if (securityStrength != null) {
            assessment.append(", Strength: ").append(securityStrength);
        }
        
        if (biometricMatchScore != null) {
            assessment.append(", Biometric Match: ").append(biometricMatchScore).append("%");
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
     * Create a successful verification result
     */
    public static StepUpVerificationResult success(String sessionId, String challengeId, String userId, 
                                                 StepUpAuthType authType, String message) {
        return StepUpVerificationResult.builder()
            .verified(true)
            .sessionId(sessionId)
            .challengeId(challengeId)
            .userId(userId)
            .authType(authType)
            .message(message)
            .securityStrength(authType.getSecurityStrength())
            .verificationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a failed verification result
     */
    public static StepUpVerificationResult failed(String sessionId, String challengeId, String userId, 
                                                StepUpAuthType authType, String message, int remainingAttempts) {
        return StepUpVerificationResult.builder()
            .verified(false)
            .sessionId(sessionId)
            .challengeId(challengeId)
            .userId(userId)
            .authType(authType)
            .message(message)
            .remainingAttempts(remainingAttempts)
            .finalAttempt(remainingAttempts <= 0)
            .verificationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate result consistency
     */
    public boolean isValid() {
        return verified != null &&
               sessionId != null && !sessionId.trim().isEmpty() &&
               challengeId != null && !challengeId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               authType != null &&
               message != null && !message.trim().isEmpty() &&
               verificationTime != null &&
               attemptCount != null && attemptCount > 0 &&
               (confidenceLevel == null || (confidenceLevel >= 0 && confidenceLevel <= 100)) &&
               (securityStrength == null || (securityStrength >= 0 && securityStrength <= 100)) &&
               (biometricMatchScore == null || (biometricMatchScore >= 0 && biometricMatchScore <= 100));
    }
}
