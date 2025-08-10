package ph.gov.dsr.interoperability.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Compliance Record entity for tracking compliance checks and results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Entity
@Table(name = "compliance_records", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class ComplianceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "standard", nullable = false, length = 50)
    private String standard; // FHIR, OIDC, GDPR, etc.

    @NotBlank
    @Column(name = "entity", nullable = false, length = 200)
    private String entity; // Entity being checked for compliance

    @NotNull
    @Column(name = "compliant", nullable = false)
    private Boolean compliant;

    @Column(name = "compliance_score")
    private Double complianceScore;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "validation_results", columnDefinition = "JSONB")
    private String validationResults;

    @Column(name = "recommendations", columnDefinition = "JSONB")
    private String recommendations;

    @Column(name = "severity", length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "category", length = 50)
    private String category; // STRUCTURE, TERMINOLOGY, PROFILE, SECURITY, etc.

    @NotNull
    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "remediation_status", length = 30)
    private String remediationStatus; // PENDING, IN_PROGRESS, COMPLETED, FAILED

    @Column(name = "remediation_date")
    private LocalDateTime remediationDate;

    @Column(name = "remediation_notes", columnDefinition = "TEXT")
    private String remediationNotes;

    @Column(name = "checked_by", length = 100)
    private String checkedBy;

    @Column(name = "remediated_by", length = 100)
    private String remediatedBy;

    @Column(name = "reference_id", length = 100)
    private String referenceId; // External reference ID

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Enums
    public enum ComplianceStandard {
        FHIR("Fast Healthcare Interoperability Resources"),
        OIDC("OpenID Connect"),
        GDPR("General Data Protection Regulation"),
        ISO_27001("ISO 27001 Information Security"),
        NIST("NIST Cybersecurity Framework"),
        DPA("Data Privacy Act"),
        HIPAA("Health Insurance Portability and Accountability Act"),
        SOX("Sarbanes-Oxley Act"),
        PCI_DSS("Payment Card Industry Data Security Standard");

        private final String description;

        ComplianceStandard(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ComplianceSeverity {
        LOW("Low priority compliance issue"),
        MEDIUM("Medium priority compliance issue"),
        HIGH("High priority compliance issue"),
        CRITICAL("Critical compliance issue requiring immediate attention");

        private final String description;

        ComplianceSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RemediationStatus {
        PENDING("Remediation pending"),
        IN_PROGRESS("Remediation in progress"),
        COMPLETED("Remediation completed"),
        FAILED("Remediation failed"),
        NOT_REQUIRED("No remediation required");

        private final String description;

        RemediationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public ComplianceRecord() {}

    public ComplianceRecord(String standard, String entity, Boolean compliant) {
        this.standard = standard;
        this.entity = entity;
        this.compliant = compliant;
        this.checkedAt = LocalDateTime.now();
    }

    // Helper methods
    
    /**
     * Check if compliance record is expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if remediation is required
     */
    public boolean requiresRemediation() {
        return !compliant && (remediationStatus == null || 
                             "PENDING".equals(remediationStatus) || 
                             "FAILED".equals(remediationStatus));
    }

    /**
     * Mark as remediated
     */
    public void markAsRemediated(String remediatedBy, String notes) {
        this.remediationStatus = "COMPLETED";
        this.remediationDate = LocalDateTime.now();
        this.remediatedBy = remediatedBy;
        this.remediationNotes = notes;
        this.compliant = true; // Assume remediation fixes compliance
    }

    /**
     * Set expiry based on standard requirements
     */
    public void setExpiryFromStandard() {
        switch (standard.toUpperCase()) {
            case "FHIR":
                this.expiresAt = checkedAt.plusMonths(6);
                break;
            case "OIDC":
                this.expiresAt = checkedAt.plusMonths(3);
                break;
            case "GDPR":
                this.expiresAt = checkedAt.plusYears(1);
                break;
            default:
                this.expiresAt = checkedAt.plusMonths(12);
                break;
        }
    }

    /**
     * Calculate compliance age in days
     */
    public long getComplianceAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(checkedAt, LocalDateTime.now());
    }

    /**
     * Check if compliance check is recent (within 30 days)
     */
    public boolean isRecentCheck() {
        return getComplianceAgeInDays() <= 30;
    }
}
