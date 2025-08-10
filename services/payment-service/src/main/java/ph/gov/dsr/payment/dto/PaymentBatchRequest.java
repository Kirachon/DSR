package ph.gov.dsr.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment batch request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment batch creation request")
public class PaymentBatchRequest {

    @NotNull(message = "Program ID is required")
    @Schema(description = "Program ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID programId;

    @NotBlank(message = "Program name is required")
    @Size(max = 200, message = "Program name cannot exceed 200 characters")
    @Schema(description = "Program name", example = "4Ps Cash Transfer Program")
    private String programName;

    @NotNull(message = "Scheduled date is required")
    @Future(message = "Scheduled date must be in the future")
    @Schema(description = "Scheduled processing date")
    private LocalDateTime scheduledDate;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Batch description", example = "Monthly cash transfer for Q1 2024")
    private String description;

    @NotEmpty(message = "Payment requests cannot be empty")
    @Valid
    @Schema(description = "List of payment requests in the batch")
    private List<PaymentRequest> paymentRequests;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Processing priority", example = "NORMAL")
    private BatchPriority priority = BatchPriority.NORMAL;

    @Schema(description = "Auto-start processing when scheduled", example = "true")
    private Boolean autoStart = true;

    @Schema(description = "Send notifications on completion", example = "true")
    private Boolean sendNotifications = true;

    /**
     * Batch priority enumeration
     */
    public enum BatchPriority {
        LOW("Low priority"),
        NORMAL("Normal priority"),
        HIGH("High priority"),
        URGENT("Urgent priority");

        private final String description;

        BatchPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
