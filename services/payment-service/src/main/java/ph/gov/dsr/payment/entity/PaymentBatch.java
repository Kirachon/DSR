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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PaymentBatch entity representing batch payment processing
 */
@Entity
@Table(name = "payment_batches", indexes = {
    @Index(name = "idx_batch_status", columnList = "status"),
    @Index(name = "idx_batch_program_id", columnList = "program_id"),
    @Index(name = "idx_batch_created_at", columnList = "created_at"),
    @Index(name = "idx_batch_scheduled_date", columnList = "scheduled_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "batch_number", nullable = false, unique = true, length = 50)
    private String batchNumber;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "program_name", nullable = false, length = 200)
    private String programName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "PHP";

    @Column(name = "total_payments", nullable = false)
    private Integer totalPayments;

    @Column(name = "successful_payments", nullable = false)
    private Integer successfulPayments = 0;

    @Column(name = "failed_payments", nullable = false)
    private Integer failedPayments = 0;

    @Column(name = "pending_payments", nullable = false)
    private Integer pendingPayments = 0;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "started_date")
    private LocalDateTime startedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

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
    public void updatePaymentCounts() {
        successfulPayments = (int) payments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .count();
        
        failedPayments = (int) payments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.FAILED)
            .count();
        
        pendingPayments = (int) payments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING || 
                        p.getStatus() == Payment.PaymentStatus.PROCESSING)
            .count();
    }

    public boolean isCompleted() {
        return status == BatchStatus.COMPLETED;
    }

    public boolean isProcessing() {
        return status == BatchStatus.PROCESSING;
    }

    public boolean canStart() {
        return status == BatchStatus.PENDING;
    }

    public double getSuccessRate() {
        if (totalPayments == 0) return 0.0;
        return (double) successfulPayments / totalPayments * 100.0;
    }

    /**
     * Batch status enumeration
     */
    public enum BatchStatus {
        PENDING("Batch is pending processing"),
        PROCESSING("Batch is being processed"),
        COMPLETED("Batch processing completed"),
        FAILED("Batch processing failed"),
        CANCELLED("Batch was cancelled"),
        PAUSED("Batch processing paused");

        private final String description;

        BatchStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
