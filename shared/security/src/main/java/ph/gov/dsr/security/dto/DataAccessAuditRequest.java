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
 * Request DTO for data access audit operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataAccessAuditRequest {

    private String eventType; // DATA_ACCESS, DATA_EXPORT, DATA_IMPORT, DATA_MODIFICATION, DATA_DELETION

    private UUID userId;

    private String username;

    private String userRole;

    private String resourceType; // HOUSEHOLD, MEMBER, PAYMENT, DOCUMENT

    private String resourceId;

    private String action; // CREATE, READ, UPDATE, DELETE, EXPORT, IMPORT

    private LocalDateTime timestamp;

    private String result; // SUCCESS, FAILURE, PARTIAL

    private String ipAddress;

    private String userAgent;

    private String sessionId;

    private String requestId;

    private String dataClassification; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED

    private String sensitivityLevel; // LOW, MEDIUM, HIGH, CRITICAL

    private Boolean containsPii; // Contains Personally Identifiable Information

    private Boolean containsPhi; // Contains Protected Health Information

    private Boolean containsFinancialData;

    private String recordCount; // Number of records accessed

    private String dataSize; // Size of data accessed (in bytes)

    private String fieldsAccessed; // JSON array of field names accessed

    private String filterCriteria; // Search/filter criteria used

    private String exportFormat; // CSV, PDF, JSON, XML

    private String exportDestination; // File path or system destination

    private String dataBefore; // JSON representation of data before change

    private String dataAfter; // JSON representation of data after change

    private String changeReason;

    private String businessJustification;

    private String approvalRequired;

    private UUID approvedBy;

    private LocalDateTime approvedAt;

    private String complianceFrameworks; // JSON array of applicable compliance frameworks

    private String retentionPeriod; // Data retention requirements

    private String accessMethod; // API, UI, BATCH, DIRECT_DB

    private String clientApplication;

    private String apiEndpoint;

    private String queryExecuted; // SQL query or API call made

    private Long executionTimeMs;

    private String errorMessage;

    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    private Double riskScore; // 0.0 to 100.0

    private String threatIndicators; // JSON array of threat indicators

    private Boolean suspiciousActivity;

    private String suspiciousActivityReason;

    private Boolean dataLeakagePrevention; // DLP policy applied

    private String dlpPolicyName;

    private String dlpAction; // ALLOW, BLOCK, QUARANTINE, ENCRYPT

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
        return eventType != null && !eventType.trim().isEmpty() &&
               userId != null &&
               username != null && !username.trim().isEmpty() &&
               resourceType != null && !resourceType.trim().isEmpty() &&
               resourceId != null && !resourceId.trim().isEmpty() &&
               action != null && !action.trim().isEmpty() &&
               timestamp != null;
    }

    /**
     * Check if this involves sensitive data
     */
    public boolean involvesSensitiveData() {
        return Boolean.TRUE.equals(containsPii) ||
               Boolean.TRUE.equals(containsPhi) ||
               Boolean.TRUE.equals(containsFinancialData) ||
               "CONFIDENTIAL".equals(dataClassification) ||
               "RESTRICTED".equals(dataClassification);
    }

    /**
     * Check if this is a high-risk operation
     */
    public boolean isHighRiskOperation() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel) ||
               (riskScore != null && riskScore >= 70.0) ||
               Boolean.TRUE.equals(suspiciousActivity) ||
               "DELETE".equals(action) ||
               "EXPORT".equals(action);
    }

    /**
     * Get operation type for audit logging
     */
    public String getOperation() {
        return action;
    }

    /**
     * Check if the operation was successful
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

    /**
     * Check if this operation requires approval
     */
    public boolean requiresApproval() {
        return Boolean.TRUE.equals(Boolean.valueOf(approvalRequired)) ||
               involvesSensitiveData() ||
               isHighRiskOperation();
    }

    /**
     * Check if this is a data modification operation
     */
    public boolean isDataModification() {
        return "CREATE".equals(action) ||
               "UPDATE".equals(action) ||
               "DELETE".equals(action) ||
               "IMPORT".equals(action);
    }
}
