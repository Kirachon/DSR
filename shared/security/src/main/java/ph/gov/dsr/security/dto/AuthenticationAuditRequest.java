package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for authentication audit operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationAuditRequest {

    private String eventType; // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, PASSWORD_CHANGE

    private String username;

    private UUID userId;

    private String userRole;

    private String ipAddress;

    private String userAgent;

    private String sessionId;

    private String requestId;

    private LocalDateTime timestamp;

    private String result; // SUCCESS, FAILURE, PARTIAL

    private String failureReason;

    private String mfaMethod; // SMS, EMAIL, TOTP, HARDWARE_KEY

    private Boolean mfaRequired;

    private Boolean mfaSuccess;

    private String deviceId;

    private String deviceType; // DESKTOP, MOBILE, TABLET

    private String browserInfo;

    private String operatingSystem;

    private String geolocation;

    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    private Double riskScore; // 0.0 to 100.0

    private String threatIndicators; // JSON array of threat indicators

    private Boolean suspiciousActivity;

    private String suspiciousActivityReason;

    private Boolean newDevice;

    private Boolean newLocation;

    private String previousLoginTime;

    private String previousLoginLocation;

    private Integer failedAttemptCount;

    private String accountStatus; // ACTIVE, LOCKED, SUSPENDED, DISABLED

    private Boolean passwordExpired;

    private Boolean passwordChangeRequired;

    private String complianceFlags; // JSON array of compliance requirements

    private String businessContext;

    private String additionalDetails; // JSON object with additional context

    private String correlationId;

    private String sourceSystem;

    private Boolean requiresNotification;

    private String notificationRecipients; // JSON array of notification recipients

    private Boolean requiresEscalation;

    private String escalationReason;

    private String tags; // JSON array of tags for categorization

    /**
     * Get event subtype for audit logging
     */
    public String getEventSubtype() {
        return eventType;
    }

    /**
     * Check if the authentication was successful
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(result);
    }

    /**
     * Get additional details for audit logging
     */
    public String getDetails() {
        return additionalDetails;
    }

    /**
     * Validation method to check if the request is valid
     */
    public boolean isValid() {
        return eventType != null && !eventType.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               userId != null &&
               ipAddress != null && !ipAddress.trim().isEmpty() &&
               timestamp != null;
    }

    /**
     * Check if this is a successful authentication
     */
    public boolean isSuccessfulAuthentication() {
        return "SUCCESS".equals(result) && 
               ("LOGIN_SUCCESS".equals(eventType) || "MFA_SUCCESS".equals(eventType));
    }

    /**
     * Check if this is a failed authentication
     */
    public boolean isFailedAuthentication() {
        return "FAILURE".equals(result) && 
               ("LOGIN_FAILURE".equals(eventType) || "MFA_FAILURE".equals(eventType));
    }

    /**
     * Check if this event indicates high risk
     */
    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel) ||
               (riskScore != null && riskScore >= 70.0) ||
               Boolean.TRUE.equals(suspiciousActivity);
    }

    /**
     * Check if this event requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return "CRITICAL".equals(riskLevel) ||
               Boolean.TRUE.equals(requiresEscalation) ||
               (failedAttemptCount != null && failedAttemptCount >= 5);
    }
}
