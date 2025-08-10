package ph.gov.dsr.interoperability.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service Delivery Record entity for tracking service delivery across systems
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "service_delivery_records", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class ServiceDeliveryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;

    @NotBlank
    @Column(name = "beneficiary_psn", nullable = false, length = 16)
    private String beneficiaryPsn;

    @Column(name = "household_id", columnDefinition = "UUID")
    private UUID householdId;

    @NotBlank
    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType; // CASH_TRANSFER, HEALTH_SERVICE, EDUCATION_ASSISTANCE, etc.

    @NotBlank
    @Column(name = "program_code", nullable = false, length = 50)
    private String programCode;

    @Column(name = "program_name", length = 200)
    private String programName;

    @NotBlank
    @Column(name = "providing_agency", nullable = false, length = 100)
    private String providingAgency; // DSWD, DOH, DepEd, etc.

    @Column(name = "implementing_unit", length = 200)
    private String implementingUnit;

    @NotNull
    @Column(name = "service_date", nullable = false)
    private LocalDateTime serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    private DeliveryStatus deliveryStatus;

    @Column(name = "delivery_method", length = 50)
    private String deliveryMethod; // CASH, BANK_TRANSFER, IN_KIND, VOUCHER, etc.

    @Column(name = "service_amount", precision = 12, scale = 2)
    private BigDecimal serviceAmount;

    @Column(name = "service_quantity")
    private Integer serviceQuantity;

    @Column(name = "service_unit", length = 20)
    private String serviceUnit; // PESO, KILOGRAM, PIECE, etc.

    @Column(name = "delivery_location", length = 200)
    private String deliveryLocation;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "target_system", length = 50)
    private String targetSystem;

    @Column(name = "integration_channel", length = 50)
    private String integrationChannel; // API, FILE_TRANSFER, MANUAL, etc.

    @Column(name = "delivery_confirmation_date")
    private LocalDateTime deliveryConfirmationDate;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    @Column(name = "verification_method", length = 50)
    private String verificationMethod; // BIOMETRIC, OTP, SIGNATURE, etc.

    @Column(name = "service_description", columnDefinition = "TEXT")
    private String serviceDescription;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_retry_date")
    private LocalDateTime lastRetryDate;

    @Column(name = "next_retry_date")
    private LocalDateTime nextRetryDate;

    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    @Column(name = "original_record_id", columnDefinition = "UUID")
    private UUID originalRecordId;

    @Column(name = "reconciliation_status", length = 30)
    private String reconciliationStatus = "PENDING"; // PENDING, RECONCILED, DISCREPANCY

    @Column(name = "reconciliation_date")
    private LocalDateTime reconciliationDate;

    @Column(name = "audit_trail", columnDefinition = "JSONB")
    private String auditTrail;

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
    public enum DeliveryStatus {
        PENDING,
        IN_PROGRESS,
        DELIVERED,
        CONFIRMED,
        FAILED,
        CANCELLED,
        RETURNED,
        DISPUTED
    }

    // Constructors
    public ServiceDeliveryRecord() {}

    public ServiceDeliveryRecord(String transactionId, String beneficiaryPsn, String serviceType, String programCode) {
        this.transactionId = transactionId;
        this.beneficiaryPsn = beneficiaryPsn;
        this.serviceType = serviceType;
        this.programCode = programCode;
        this.serviceDate = LocalDateTime.now();
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    // Helper methods
    
    /**
     * Check if delivery is completed
     */
    public boolean isDelivered() {
        return deliveryStatus == DeliveryStatus.DELIVERED || deliveryStatus == DeliveryStatus.CONFIRMED;
    }

    /**
     * Check if delivery failed
     */
    public boolean isFailed() {
        return deliveryStatus == DeliveryStatus.FAILED || deliveryStatus == DeliveryStatus.CANCELLED;
    }

    /**
     * Check if delivery can be retried
     */
    public boolean canRetry() {
        return deliveryStatus == DeliveryStatus.FAILED && 
               (retryCount == null || retryCount < 3) &&
               (nextRetryDate == null || nextRetryDate.isBefore(LocalDateTime.now()));
    }

    /**
     * Mark as delivered
     */
    public void markAsDelivered(String confirmedBy, String verificationMethod) {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveryConfirmationDate = LocalDateTime.now();
        this.confirmedBy = confirmedBy;
        this.verificationMethod = verificationMethod;
    }

    /**
     * Mark as failed with reason
     */
    public void markAsFailed(String reason) {
        this.deliveryStatus = DeliveryStatus.FAILED;
        this.failureReason = reason;
        this.retryCount = (retryCount != null ? retryCount : 0) + 1;
        this.lastRetryDate = LocalDateTime.now();
        
        // Set next retry date (exponential backoff)
        long delayMinutes = (long) Math.pow(2, retryCount) * 30; // 30min, 1hr, 2hr, 4hr
        this.nextRetryDate = LocalDateTime.now().plusMinutes(delayMinutes);
    }

    /**
     * Get service delivery age in hours
     */
    public long getServiceAgeInHours() {
        return java.time.temporal.ChronoUnit.HOURS.between(serviceDate, LocalDateTime.now());
    }

    /**
     * Check if service is overdue (more than 24 hours pending)
     */
    public boolean isOverdue() {
        return deliveryStatus == DeliveryStatus.PENDING && getServiceAgeInHours() > 24;
    }

    /**
     * Generate transaction summary
     */
    public String getTransactionSummary() {
        return String.format("%s - %s for %s (Amount: %s, Status: %s)", 
                transactionId, serviceType, beneficiaryPsn, 
                serviceAmount != null ? serviceAmount.toString() : "N/A", 
                deliveryStatus);
    }
}
