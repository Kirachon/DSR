package ph.gov.dsr.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment entity representing individual payment transactions
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_household_id", columnList = "household_id"),
    @Index(name = "idx_payment_program_id", columnList = "program_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_fsp_reference", columnList = "fsp_reference_number"),
    @Index(name = "idx_payment_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "beneficiary_id", nullable = false)
    private UUID beneficiaryId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "PHP";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "fsp_code", nullable = false, length = 20)
    private String fspCode;

    @Column(name = "fsp_reference_number", length = 100)
    private String fspReferenceNumber;

    @Column(name = "internal_reference_number", nullable = false, unique = true, length = 50)
    private String internalReferenceNumber;

    @Column(name = "recipient_account_number", length = 100)
    private String recipientAccountNumber;

    @Column(name = "recipient_account_name", length = 200)
    private String recipientAccountName;

    @Column(name = "recipient_mobile_number", length = 20)
    private String recipientMobileNumber;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "max_retry_count", nullable = false)
    private Integer maxRetryCount = 3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private PaymentBatch batch;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Business methods
    public boolean canRetry() {
        return retryCount < maxRetryCount && 
               (status == PaymentStatus.FAILED || status == PaymentStatus.PENDING);
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isProcessing() {
        return status == PaymentStatus.PROCESSING;
    }

    /**
     * Payment status enumeration
     */
    public enum PaymentStatus {
        PENDING("Payment is pending processing"),
        PROCESSING("Payment is being processed"),
        COMPLETED("Payment completed successfully"),
        FAILED("Payment failed"),
        CANCELLED("Payment was cancelled"),
        REFUNDED("Payment was refunded"),
        EXPIRED("Payment expired");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Payment method enumeration
     */
    public enum PaymentMethod {
        BANK_TRANSFER("Bank Transfer"),
        E_WALLET("E-Wallet"),
        CASH_PICKUP("Cash Pickup"),
        CHECK("Check"),
        PREPAID_CARD("Prepaid Card");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
