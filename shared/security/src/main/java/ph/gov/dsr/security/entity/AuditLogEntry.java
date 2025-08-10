package ph.gov.dsr.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLogEntry entity for detailed audit trail entries
 * This is a simplified view of audit logs for reporting and analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Entity
@Table(name = "audit_log_entries", schema = "dsr_security")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_log_id", nullable = false)
    private AuditLog auditLog;

    @NotBlank
    @Column(name = "entry_type", nullable = false, length = 100)
    private String entryType; // FIELD_CHANGE, STATUS_CHANGE, COMMENT, ATTACHMENT

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "entry_details", columnDefinition = "TEXT")
    private String entryDetails;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "sequence_number")
    private Integer sequenceNumber; // Order within the audit log

    @Column(name = "severity", length = 20)
    private String severity; // INFO, WARNING, ERROR, CRITICAL

    @Column(name = "category", length = 50)
    private String category; // DATA_CHANGE, SECURITY_EVENT, SYSTEM_EVENT

    @Column(name = "compliance_relevant", nullable = false)
    @Builder.Default
    private Boolean complianceRelevant = false;

    @Column(name = "retention_period_days")
    private Integer retentionPeriodDays;

    @Column(name = "encrypted", nullable = false)
    @Builder.Default
    private Boolean encrypted = false;

    @Column(name = "hash_value", length = 255)
    private String hashValue; // For integrity verification

    @Column(name = "parent_entry_id", columnDefinition = "UUID")
    private UUID parentEntryId; // For hierarchical entries

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "business_context", columnDefinition = "TEXT")
    private String businessContext; // Business-relevant context

    @Column(name = "technical_context", columnDefinition = "TEXT")
    private String technicalContext; // Technical details

    @Column(name = "tags", length = 500)
    private String tags; // JSON array of tags for categorization

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // Additional metadata as JSON

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate; // When the change took effect

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "external_reference", length = 255)
    private String externalReference;

    @Column(name = "verification_status", length = 50)
    private String verificationStatus; // PENDING, VERIFIED, FAILED

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by", columnDefinition = "UUID")
    private UUID verifiedBy;

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    protected void onCreate() {
        if (severity == null) {
            severity = "INFO";
        }
        if (category == null) {
            category = "DATA_CHANGE";
        }
        if (retentionPeriodDays == null) {
            retentionPeriodDays = 2555; // 7 years default
        }
        if (verificationStatus == null) {
            verificationStatus = "PENDING";
        }
    }
}
