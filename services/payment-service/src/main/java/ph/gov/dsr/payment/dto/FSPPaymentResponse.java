package ph.gov.dsr.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * FSP payment response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FSPPaymentResponse {

    private boolean success;
    private String fspReferenceNumber;
    private String internalReferenceNumber;
    private FSPPaymentStatus status;
    private String statusMessage;
    private String errorCode;
    private String errorMessage;
    private BigDecimal amount;
    private String currency;
    private BigDecimal fee;
    private LocalDateTime processedAt;
    private LocalDateTime estimatedCompletionTime;
    private Map<String, Object> additionalData;
    private String transactionId;
    private String receiptNumber;

    /**
     * FSP payment status enumeration
     */
    public enum FSPPaymentStatus {
        SUBMITTED("Payment submitted to FSP"),
        PROCESSING("Payment is being processed by FSP"),
        COMPLETED("Payment completed successfully"),
        FAILED("Payment failed"),
        CANCELLED("Payment was cancelled"),
        PENDING_APPROVAL("Payment pending approval"),
        REJECTED("Payment was rejected"),
        EXPIRED("Payment expired"),
        REFUNDED("Payment was refunded");

        private final String description;

        FSPPaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isSuccessful() {
        return success && (status == FSPPaymentStatus.COMPLETED || status == FSPPaymentStatus.PROCESSING);
    }

    public boolean isFailed() {
        return !success || status == FSPPaymentStatus.FAILED || status == FSPPaymentStatus.REJECTED;
    }

    public boolean isPending() {
        return success && (status == FSPPaymentStatus.SUBMITTED || 
                          status == FSPPaymentStatus.PROCESSING || 
                          status == FSPPaymentStatus.PENDING_APPROVAL);
    }
}
