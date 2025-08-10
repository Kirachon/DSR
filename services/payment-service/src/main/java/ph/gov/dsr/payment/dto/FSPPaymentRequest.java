package ph.gov.dsr.payment.dto;

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
 * FSP payment request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FSPPaymentRequest {

    private UUID paymentId;
    private String internalReferenceNumber;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentMethod paymentMethod;
    private String recipientAccountNumber;
    private String recipientAccountName;
    private String recipientMobileNumber;
    private String description;
    private LocalDateTime scheduledDate;
    private Map<String, Object> metadata;
    private String callbackUrl;
    private String correlationId;

    // Beneficiary information
    private UUID beneficiaryId;
    private String beneficiaryName;
    private String beneficiaryEmail;
    private String beneficiaryAddress;

    // Program information
    private UUID programId;
    private String programName;
    private String programCode;

    // Additional FSP-specific fields
    private String purpose;
    private String remarks;
    private Integer priority;
    private String notificationPreference;
}
