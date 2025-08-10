package ph.gov.dsr.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.payment.entity.PaymentBatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment batch response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment batch response")
public class PaymentBatchResponse {

    @Schema(description = "Batch ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID batchId;

    @Schema(description = "Batch number", example = "BATCH-2024-001234")
    private String batchNumber;

    @Schema(description = "Program ID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID programId;

    @Schema(description = "Program name", example = "4Ps Cash Transfer Program")
    private String programName;

    @Schema(description = "Batch status")
    private PaymentBatch.BatchStatus status;

    @Schema(description = "Total batch amount", example = "500000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Currency code", example = "PHP")
    private String currency;

    @Schema(description = "Total number of payments", example = "100")
    private Integer totalPayments;

    @Schema(description = "Number of successful payments", example = "95")
    private Integer successfulPayments;

    @Schema(description = "Number of failed payments", example = "3")
    private Integer failedPayments;

    @Schema(description = "Number of pending payments", example = "2")
    private Integer pendingPayments;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Scheduled processing date")
    private LocalDateTime scheduledDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date when batch processing started")
    private LocalDateTime startedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date when batch processing completed")
    private LocalDateTime completedDate;

    @Schema(description = "Batch description")
    private String description;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;

    @Schema(description = "User who created the batch")
    private String createdBy;

    @Schema(description = "User who last updated the batch")
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
    @Schema(description = "Success rate percentage", example = "95.0")
    private Double successRate;

    @Schema(description = "Whether batch is completed")
    private Boolean isCompleted;

    @Schema(description = "Whether batch is processing")
    private Boolean isProcessing;

    @Schema(description = "Whether batch can be started")
    private Boolean canStart;

    @Schema(description = "Status description")
    private String statusDescription;

    @Schema(description = "Processing duration in minutes")
    private Long processingDurationMinutes;

    @Schema(description = "List of payments in the batch (optional)")
    private List<PaymentResponse> payments;

    // Helper method to set computed fields
    public void setComputedFields() {
        if (totalPayments != null && totalPayments > 0) {
            this.successRate = (double) (successfulPayments != null ? successfulPayments : 0) / totalPayments * 100.0;
        } else {
            this.successRate = 0.0;
        }

        this.isCompleted = status == PaymentBatch.BatchStatus.COMPLETED;
        this.isProcessing = status == PaymentBatch.BatchStatus.PROCESSING;
        this.canStart = status == PaymentBatch.BatchStatus.PENDING;

        if (status != null) {
            this.statusDescription = status.getDescription();
        }

        if (startedDate != null && completedDate != null) {
            this.processingDurationMinutes = java.time.Duration.between(startedDate, completedDate).toMinutes();
        }
    }
}
