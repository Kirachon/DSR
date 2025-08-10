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
 * Request DTO for administrative action audit operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionAuditRequest {

    private String actionType; // USER_CREATION, USER_DELETION, ROLE_ASSIGNMENT, SYSTEM_CONFIG

    private UUID adminUserId;

    private String adminUsername;

    private String adminRole;

    private LocalDateTime timestamp;

    private String targetResourceType; // USER, ROLE, SYSTEM, CONFIGURATION

    private String targetResourceId;

    private String targetResourceName;

    private UUID targetUserId; // If action affects a specific user

    private String targetUsername;

    private String action; // CREATE, UPDATE, DELETE, ASSIGN, REVOKE, ENABLE, DISABLE

    private String result; // SUCCESS, FAILURE, PARTIAL

    private String failureReason;

    private String ipAddress;

    private String userAgent;

    private String sessionId;

    private String requestId;

    private String dataBefore; // JSON representation of data before change

    private String dataAfter; // JSON representation of data after change

    private String changeDetails; // Detailed description of what changed

    private String businessJustification;

    private Boolean approvalRequired;

    private UUID approvedBy;

    private LocalDateTime approvedAt;

    private String approvalComments;

    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    private Double riskScore; // 0.0 to 100.0

    private String impactAssessment;

    private String complianceFrameworks; // JSON array of applicable compliance frameworks

    private Boolean sensitiveDataInvolved;

    private String sensitiveDataTypes; // JSON array of sensitive data types

    private Boolean privilegedAction; // Whether this is a privileged administrative action

    private String escalationRequired;

    private String notificationRecipients; // JSON array of notification recipients

    private String auditTrail; // Reference to related audit entries

    private String systemContext; // System state or context information

    private String clientApplication;

    private String apiEndpoint; // If action was performed via API

    private String batchOperation; // If this is part of a batch operation

    private Integer batchSize;

    private String batchId;

    private String geolocation;

    private String deviceId;

    private String networkSegment;

    private String correlationId;

    private String sourceSystem;

    private String additionalContext; // JSON object with additional context

    private String tags; // JSON array of tags for categorization

    /**
     * Validation method to check if the request is valid
     */
    public boolean isValid() {
        return actionType != null && !actionType.trim().isEmpty() &&
               adminUserId != null &&
               adminUsername != null && !adminUsername.trim().isEmpty() &&
               action != null && !action.trim().isEmpty() &&
               timestamp != null;
    }

    /**
     * Check if this is a high-risk administrative action
     */
    public boolean isHighRiskAction() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel) ||
               (riskScore != null && riskScore >= 70.0) ||
               Boolean.TRUE.equals(privilegedAction) ||
               Boolean.TRUE.equals(sensitiveDataInvolved);
    }

    /**
     * Check if this action requires approval
     */
    public boolean requiresApproval() {
        return Boolean.TRUE.equals(approvalRequired) ||
               isHighRiskAction() ||
               "DELETE".equals(action) ||
               "ROLE_ASSIGNMENT".equals(actionType);
    }

    /**
     * Check if this is a user management action
     */
    public boolean isUserManagementAction() {
        return "USER_CREATION".equals(actionType) ||
               "USER_DELETION".equals(actionType) ||
               "USER_MODIFICATION".equals(actionType) ||
               "ROLE_ASSIGNMENT".equals(actionType);
    }

    /**
     * Check if this is a system configuration action
     */
    public boolean isSystemConfigurationAction() {
        return "SYSTEM_CONFIG".equals(actionType) ||
               "SECURITY_POLICY".equals(actionType) ||
               "NETWORK_CONFIG".equals(actionType);
    }

    /**
     * Check if notifications should be sent
     */
    public boolean shouldSendNotifications() {
        return isHighRiskAction() ||
               requiresApproval() ||
               (notificationRecipients != null && !notificationRecipients.trim().isEmpty());
    }

    /**
     * Check if the action was successful
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(result);
    }

    /**
     * Get additional details for audit logging
     */
    public String getDetails() {
        return additionalContext;
    }
}
