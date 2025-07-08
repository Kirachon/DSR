package ph.gov.dsr.security.dto;

// import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// import ph.gov.dsr.security.zerotrust.model.TrustLevel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Device Registration Result DTO
 * Contains the result of device registration operation including success status, trust level, and security details
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationResult {

    /**
     * Whether device registration was successful
     */
    private Boolean success;

    /**
     * Device ID that was registered
     */
    private String deviceId;

    /**
     * User ID who owns the device
     */
    private String userId;

    /**
     * Human-readable message about the registration result
     */
    private String message;

    /**
     * Initial trust level assigned to the device
     */
    private String trustLevel;

    /**
     * Device fingerprint generated during registration
     */
    private String deviceFingerprint;

    /**
     * Registration timestamp
     */
    @Builder.Default
    private LocalDateTime registrationTime = LocalDateTime.now();

    /**
     * Whether device requires verification
     */
    @Builder.Default
    private Boolean requiresVerification = true;

    /**
     * Verification token for device verification process
     */
    private String verificationToken;

    /**
     * Verification expiry time
     */
    private LocalDateTime verificationExpiry;

    /**
     * Device type (mobile, desktop, tablet, etc.)
     */
    private String deviceType;

    /**
     * Operating system information
     */
    private String operatingSystem;

    /**
     * Browser information (if applicable)
     */
    private String browserInfo;

    /**
     * Device security features detected
     */
    private List<String> securityFeatures;

    /**
     * Security warnings or concerns
     */
    private List<String> securityWarnings;

    /**
     * Risk factors identified during registration
     */
    private List<String> riskFactors;

    /**
     * Recommended security actions
     */
    private List<String> recommendedActions;

    /**
     * Device metadata collected during registration
     */
    private Map<String, Object> deviceMetadata;

    /**
     * Registration source (web, mobile app, API, etc.)
     */
    private String registrationSource;

    /**
     * IP address used during registration
     */
    private String registrationIp;

    /**
     * Geographic location during registration
     */
    private String registrationLocation;

    /**
     * Whether device is compliant with security policies
     */
    @Builder.Default
    private Boolean compliant = false;

    /**
     * Compliance issues detected
     */
    private List<String> complianceIssues;

    /**
     * Next steps required for full device activation
     */
    private List<String> nextSteps;

    /**
     * Error code (if registration failed)
     */
    private String errorCode;

    /**
     * Detailed error information (if registration failed)
     */
    private String errorDetails;

    /**
     * Additional context information
     */
    private Map<String, Object> context;

    /**
     * Check if registration was successful
     */
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * Check if registration failed
     */
    public boolean isFailed() {
        return Boolean.FALSE.equals(success);
    }

    /**
     * Check if device requires verification
     */
    public boolean needsVerification() {
        return Boolean.TRUE.equals(requiresVerification);
    }

    /**
     * Check if device is compliant
     */
    public boolean isCompliant() {
        return Boolean.TRUE.equals(compliant);
    }

    /**
     * Check if verification token has expired
     */
    public boolean isVerificationExpired() {
        return verificationExpiry != null && LocalDateTime.now().isAfter(verificationExpiry);
    }

    /**
     * Check if device has security warnings
     */
    public boolean hasSecurityWarnings() {
        return securityWarnings != null && !securityWarnings.isEmpty();
    }

    /**
     * Check if device has risk factors
     */
    public boolean hasRiskFactors() {
        return riskFactors != null && !riskFactors.isEmpty();
    }

    /**
     * Check if device has compliance issues
     */
    public boolean hasComplianceIssues() {
        return complianceIssues != null && !complianceIssues.isEmpty();
    }

    /**
     * Get security score based on features and warnings
     */
    public int getSecurityScore() {
        int score = 50; // Base score
        
        // Add points for security features
        if (securityFeatures != null) {
            score += securityFeatures.size() * 10;
        }
        
        // Subtract points for warnings and risk factors
        if (securityWarnings != null) {
            score -= securityWarnings.size() * 15;
        }
        
        if (riskFactors != null) {
            score -= riskFactors.size() * 10;
        }
        
        // Ensure score is within valid range
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Get registration summary for logging
     */
    public String getRegistrationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Device registration ");
        summary.append(isSuccessful() ? "SUCCESSFUL" : "FAILED");
        summary.append(" for device ").append(deviceId);
        summary.append(" (user: ").append(userId).append(")");
        
        if (trustLevel != null) {
            summary.append(" - Trust Level: ").append(trustLevel);
        }
        
        if (hasSecurityWarnings()) {
            summary.append(" - ").append(securityWarnings.size()).append(" security warnings");
        }
        
        return summary.toString();
    }

    /**
     * Get security assessment summary
     */
    public String getSecurityAssessmentSummary() {
        StringBuilder assessment = new StringBuilder();
        assessment.append("Security Score: ").append(getSecurityScore());
        assessment.append(", Trust Level: ").append(trustLevel);
        assessment.append(", Compliant: ").append(isCompliant() ? "Yes" : "No");
        
        if (hasSecurityWarnings()) {
            assessment.append(", Warnings: ").append(securityWarnings.size());
        }
        
        if (hasRiskFactors()) {
            assessment.append(", Risk Factors: ").append(riskFactors.size());
        }
        
        return assessment.toString();
    }

    /**
     * Create a successful registration result
     */
    public static DeviceRegistrationResult success(String deviceId, String userId, String trustLevel, String message) {
        return DeviceRegistrationResult.builder()
            .success(true)
            .deviceId(deviceId)
            .userId(userId)
            .trustLevel(trustLevel)
            .message(message)
            .registrationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a failed registration result
     */
    public static DeviceRegistrationResult failed(String message) {
        return DeviceRegistrationResult.builder()
            .success(false)
            .message(message)
            .registrationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a failed registration result with error details
     */
    public static DeviceRegistrationResult failed(String message, String errorCode, String errorDetails) {
        return DeviceRegistrationResult.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .errorDetails(errorDetails)
            .registrationTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate result consistency
     */
    public boolean isValid() {
        return success != null &&
               message != null && !message.trim().isEmpty() &&
               registrationTime != null &&
               (isSuccessful() ? (deviceId != null && userId != null) : true) &&
               (verificationExpiry == null || verificationExpiry.isAfter(registrationTime));
    }
}
