package ph.gov.dsr.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
 * Payment request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment creation request")
public class PaymentRequest {

    @NotNull(message = "Household ID is required")
    @Schema(description = "Household ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID householdId;

    @NotNull(message = "Program ID is required")
    @Schema(description = "Program ID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID programId;

    @NotNull(message = "Beneficiary ID is required")
    @Schema(description = "Beneficiary ID", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID beneficiaryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Amount exceeds maximum limit")
    @Digits(integer = 12, fraction = 2, message = "Amount format is invalid")
    @Schema(description = "Payment amount", example = "5000.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "Currency code", example = "PHP")
    private String currency = "PHP";

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method")
    private Payment.PaymentMethod paymentMethod;

    @NotBlank(message = "FSP code is required")
    @Size(max = 20, message = "FSP code cannot exceed 20 characters")
    @Schema(description = "Financial Service Provider code", example = "GCASH")
    private String fspCode;

    @Size(max = 100, message = "Recipient account number cannot exceed 100 characters")
    @Schema(description = "Recipient account number", example = "09171234567")
    private String recipientAccountNumber;

    @Size(max = 200, message = "Recipient account name cannot exceed 200 characters")
    @Schema(description = "Recipient account name", example = "Juan Dela Cruz")
    private String recipientAccountName;

    @Pattern(regexp = "^(\\+63|0)?9\\d{9}$", message = "Invalid Philippine mobile number format")
    @Schema(description = "Recipient mobile number", example = "09171234567")
    private String recipientMobileNumber;

    @Future(message = "Scheduled date must be in the future")
    @Schema(description = "Scheduled payment date")
    private LocalDateTime scheduledDate;

    @Min(value = 1, message = "Max retry count must be at least 1")
    @Max(value = 10, message = "Max retry count cannot exceed 10")
    @Schema(description = "Maximum retry attempts", example = "3")
    private Integer maxRetryCount = 3;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Payment description")
    private String description;

    @Schema(description = "Priority level", example = "NORMAL")
    private PaymentPriority priority = PaymentPriority.NORMAL;

    /**
     * Payment priority enumeration
     */
    public enum PaymentPriority {
        LOW("Low priority"),
        NORMAL("Normal priority"),
        HIGH("High priority"),
        URGENT("Urgent priority");

        private final String description;

        PaymentPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
