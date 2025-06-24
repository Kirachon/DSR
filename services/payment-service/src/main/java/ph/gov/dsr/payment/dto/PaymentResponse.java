package ph.gov.dsr.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Payment response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment response")
public class PaymentResponse {

    @Schema(description = "Payment ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID paymentId;

    @Schema(description = "Household ID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID householdId;

    @Schema(description = "Program ID", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID programId;

    @Schema(description = "Beneficiary ID", example = "123e4567-e89b-12d3-a456-426614174003")
    private UUID beneficiaryId;

    @Schema(description = "Payment amount", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "PHP")
    private String currency;

    @Schema(description = "Payment status")
    private Payment.PaymentStatus status;

    @Schema(description = "Payment method")
    private Payment.PaymentMethod paymentMethod;

    @Schema(description = "FSP code", example = "GCASH")
    private String fspCode;

    @Schema(description = "FSP reference number", example = "FSP-REF-123456")
    private String fspReferenceNumber;

    @Schema(description = "Internal reference number", example = "PAY-2024-001234")
    private String internalReferenceNumber;

    @Schema(description = "Recipient account number", example = "09171234567")
    private String recipientAccountNumber;

    @Schema(description = "Recipient account name", example = "Juan Dela Cruz")
    private String recipientAccountName;

    @Schema(description = "Recipient mobile number", example = "09171234567")
    private String recipientMobileNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Scheduled payment date")
    private LocalDateTime scheduledDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date when payment was processed")
    private LocalDateTime processedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date when payment was completed")
    private LocalDateTime completedDate;

    @Schema(description = "Failure reason if payment failed")
    private String failureReason;

    @Schema(description = "Current retry count", example = "0")
    private Integer retryCount;

    @Schema(description = "Maximum retry count", example = "3")
    private Integer maxRetryCount;

    @Schema(description = "Batch ID if payment is part of a batch")
    private UUID batchId;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;

    @Schema(description = "User who created the payment")
    private String createdBy;

    @Schema(description = "User who last updated the payment")
    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Version for optimistic locking")
    private Long version;

    // Computed fields
    @Schema(description = "Whether payment can be retried")
    private Boolean canRetry;

    @Schema(description = "Whether payment is completed")
    private Boolean isCompleted;

    @Schema(description = "Whether payment is failed")
    private Boolean isFailed;

    @Schema(description = "Whether payment is pending")
    private Boolean isPending;

    @Schema(description = "Whether payment is processing")
    private Boolean isProcessing;

    @Schema(description = "Status description")
    private String statusDescription;

    @Schema(description = "Payment method description")
    private String paymentMethodDescription;

    // Helper method to set computed fields
    public void setComputedFields() {
        this.canRetry = retryCount != null && maxRetryCount != null && 
                       retryCount < maxRetryCount && 
                       (status == Payment.PaymentStatus.FAILED || status == Payment.PaymentStatus.PENDING);
        
        this.isCompleted = status == Payment.PaymentStatus.COMPLETED;
        this.isFailed = status == Payment.PaymentStatus.FAILED;
        this.isPending = status == Payment.PaymentStatus.PENDING;
        this.isProcessing = status == Payment.PaymentStatus.PROCESSING;
        
        if (status != null) {
            this.statusDescription = status.getDescription();
        }
        
        if (paymentMethod != null) {
            this.paymentMethodDescription = paymentMethod.getDescription();
        }
    }
}
