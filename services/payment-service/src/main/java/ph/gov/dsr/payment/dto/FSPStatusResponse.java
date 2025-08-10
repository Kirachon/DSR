package ph.gov.dsr.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * FSP status response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FSPStatusResponse {

    private boolean success;
    private String fspReferenceNumber;
    private FSPPaymentResponse.FSPPaymentStatus status;
    private String statusMessage;
    private String errorCode;
    private String errorMessage;
    private BigDecimal amount;
    private String currency;
    private BigDecimal fee;
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private String receiptNumber;
    private String transactionId;
    private Map<String, Object> additionalData;

    // Helper methods
    public boolean isCompleted() {
        return success && status == FSPPaymentResponse.FSPPaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return !success || status == FSPPaymentResponse.FSPPaymentStatus.FAILED || 
               status == FSPPaymentResponse.FSPPaymentStatus.REJECTED;
    }

    public boolean isPending() {
        return success && (status == FSPPaymentResponse.FSPPaymentStatus.SUBMITTED || 
                          status == FSPPaymentResponse.FSPPaymentStatus.PROCESSING || 
                          status == FSPPaymentResponse.FSPPaymentStatus.PENDING_APPROVAL);
    }
}
