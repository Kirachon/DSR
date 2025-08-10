package ph.gov.dsr.interoperability.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Sharing Agreement entity for managing data sharing permissions and policies
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "data_sharing_agreements", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class DataSharingAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "agreement_code", unique = true, nullable = false, length = 50)
    private String agreementCode;

    @NotBlank
    @Column(name = "agreement_name", nullable = false, length = 200)
    private String agreementName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(name = "data_provider", nullable = false, length = 200)
    private String dataProvider; // DSWD, DOH, DepEd, etc.

    @NotBlank
    @Column(name = "data_consumer", nullable = false, length = 200)
    private String dataConsumer;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 30)
    private AgreementType agreementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_classification", nullable = false, length = 30)
    private DataClassification dataClassification;

    @Column(name = "data_categories", columnDefinition = "TEXT")
    private String dataCategories; // JSON array of data categories

    @Column(name = "allowed_operations", columnDefinition = "TEXT")
    private String allowedOperations; // JSON array: READ, WRITE, UPDATE, DELETE

    @Column(name = "data_fields", columnDefinition = "TEXT")
    private String dataFields; // JSON array of allowed data fields

    @Column(name = "access_conditions", columnDefinition = "TEXT")
    private String accessConditions;

    @Column(name = "usage_restrictions", columnDefinition = "TEXT")
    private String usageRestrictions;

    @Column(name = "retention_period_days")
    private Integer retentionPeriodDays;

    @Column(name = "anonymization_required")
    private Boolean anonymizationRequired = false;

    @Column(name = "encryption_required")
    private Boolean encryptionRequired = true;

    @Column(name = "audit_logging_required")
    private Boolean auditLoggingRequired = true;

    @NotNull
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AgreementStatus status;

    @Column(name = "legal_basis", columnDefinition = "TEXT")
    private String legalBasis;

    @Column(name = "regulatory_compliance", columnDefinition = "TEXT")
    private String regulatoryCompliance; // JSON array of compliance requirements

    @Column(name = "data_protection_measures", columnDefinition = "TEXT")
    private String dataProtectionMeasures;

    @Column(name = "incident_response_procedure", columnDefinition = "TEXT")
    private String incidentResponseProcedure;

    @Column(name = "contact_person_provider", length = 200)
    private String contactPersonProvider;

    @Column(name = "contact_email_provider", length = 200)
    private String contactEmailProvider;

    @Column(name = "contact_person_consumer", length = 200)
    private String contactPersonConsumer;

    @Column(name = "contact_email_consumer", length = 200)
    private String contactEmailConsumer;

    @Column(name = "technical_specifications", columnDefinition = "JSONB")
    private String technicalSpecifications;

    @Column(name = "sla_requirements", columnDefinition = "JSONB")
    private String slaRequirements;

    @Column(name = "monitoring_requirements", columnDefinition = "JSONB")
    private String monitoringRequirements;

    @Column(name = "approval_workflow", columnDefinition = "TEXT")
    private String approvalWorkflow;

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "review_frequency_months")
    private Integer reviewFrequencyMonths = 12;

    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "amendment_history", columnDefinition = "JSONB")
    private String amendmentHistory;

    @Column(name = "termination_conditions", columnDefinition = "TEXT")
    private String terminationConditions;

    @Column(name = "dispute_resolution", columnDefinition = "TEXT")
    private String disputeResolution;

    @Column(name = "governing_law", length = 200)
    private String governingLaw;

    @Column(name = "document_references", columnDefinition = "TEXT")
    private String documentReferences; // JSON array of related documents

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
    public enum AgreementType {
        BILATERAL,
        MULTILATERAL,
        STANDARD_TEMPLATE,
        CUSTOM,
        EMERGENCY_ACCESS
    }

    public enum DataClassification {
        PUBLIC,
        INTERNAL,
        CONFIDENTIAL,
        RESTRICTED,
        TOP_SECRET
    }

    public enum AgreementStatus {
        DRAFT,
        UNDER_REVIEW,
        APPROVED,
        ACTIVE,
        SUSPENDED,
        EXPIRED,
        TERMINATED,
        AMENDED
    }

    // Constructors
    public DataSharingAgreement() {}

    public DataSharingAgreement(String agreementCode, String agreementName, String dataProvider, String dataConsumer) {
        this.agreementCode = agreementCode;
        this.agreementName = agreementName;
        this.dataProvider = dataProvider;
        this.dataConsumer = dataConsumer;
        this.status = AgreementStatus.DRAFT;
        this.effectiveDate = LocalDate.now();
    }

    // Helper methods
    
    /**
     * Check if agreement is currently active
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status == AgreementStatus.ACTIVE &&
               !effectiveDate.isAfter(today) &&
               (expiryDate == null || !expiryDate.isBefore(today));
    }

    /**
     * Check if agreement is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if agreement needs review
     */
    public boolean needsReview() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDate.now());
    }

    /**
     * Calculate days until expiry
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return -1; // No expiry date
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Check if operation is allowed
     */
    public boolean isOperationAllowed(String operation) {
        if (allowedOperations == null) {
            return false;
        }
        return allowedOperations.contains(operation.toUpperCase());
    }

    /**
     * Check if data field is allowed
     */
    public boolean isDataFieldAllowed(String fieldName) {
        if (dataFields == null) {
            return false;
        }
        return dataFields.contains(fieldName);
    }

    /**
     * Check if agreement is due for renewal
     */
    public boolean isDueForRenewal() {
        if (expiryDate == null) {
            return false;
        }
        // Consider due for renewal if expiring within 30 days
        return getDaysUntilExpiry() <= 30 && getDaysUntilExpiry() > 0;
    }

    /**
     * Activate agreement
     */
    public void activate(String approvedBy) {
        this.status = AgreementStatus.ACTIVE;
        this.approvedBy = approvedBy;
        this.approvalDate = LocalDate.now();
        
        // Set next review date
        if (reviewFrequencyMonths != null) {
            this.nextReviewDate = LocalDate.now().plusMonths(reviewFrequencyMonths);
        }
    }

    /**
     * Suspend agreement
     */
    public void suspend(String reason) {
        this.status = AgreementStatus.SUSPENDED;
        this.notes = (notes != null ? notes + "\n" : "") + 
                    "Suspended on " + LocalDate.now() + ": " + reason;
    }

    /**
     * Terminate agreement
     */
    public void terminate(String reason) {
        this.status = AgreementStatus.TERMINATED;
        this.notes = (notes != null ? notes + "\n" : "") + 
                    "Terminated on " + LocalDate.now() + ": " + reason;
    }

    /**
     * Get agreement summary
     */
    public String getAgreementSummary() {
        return String.format("%s: %s ↔ %s (Status: %s, Valid: %s to %s)", 
                agreementCode, dataProvider, dataConsumer, status,
                effectiveDate, expiryDate != null ? expiryDate.toString() : "No expiry");
    }
}
