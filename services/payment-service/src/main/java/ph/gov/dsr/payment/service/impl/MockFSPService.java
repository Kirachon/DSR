package ph.gov.dsr.payment.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.payment.dto.FSPPaymentRequest;
import ph.gov.dsr.payment.dto.FSPPaymentResponse;
import ph.gov.dsr.payment.dto.FSPStatusResponse;
import ph.gov.dsr.payment.entity.FSPConfiguration;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.service.FSPService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock FSP service implementation for testing and development
 */
@Slf4j
@Service
public class MockFSPService implements FSPService {

    private static final String FSP_CODE = "MOCK";
    private final Map<String, FSPPaymentResponse> mockPayments = new ConcurrentHashMap<>();

    @Override
    public String getFspCode() {
        return FSP_CODE;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public FSPPaymentResponse submitPayment(FSPPaymentRequest request, FSPConfiguration config) {
        log.info("Mock FSP: Submitting payment for amount: {} to account: {}", 
                request.getAmount(), request.getRecipientAccountNumber());

        String fspReferenceNumber = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Simulate different scenarios based on amount
        FSPPaymentResponse response;
        if (request.getAmount().compareTo(BigDecimal.valueOf(1000)) < 0) {
            // Small amounts succeed immediately
            response = FSPPaymentResponse.builder()
                .success(true)
                .fspReferenceNumber(fspReferenceNumber)
                .internalReferenceNumber(request.getInternalReferenceNumber())
                .status(FSPPaymentResponse.FSPPaymentStatus.COMPLETED)
                .statusMessage("Payment completed successfully")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .fee(BigDecimal.valueOf(5.00))
                .processedAt(LocalDateTime.now())
                .transactionId("TXN-" + fspReferenceNumber)
                .receiptNumber("RCP-" + fspReferenceNumber)
                .build();
        } else if (request.getAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
            // Large amounts fail
            response = FSPPaymentResponse.builder()
                .success(false)
                .fspReferenceNumber(fspReferenceNumber)
                .internalReferenceNumber(request.getInternalReferenceNumber())
                .status(FSPPaymentResponse.FSPPaymentStatus.FAILED)
                .statusMessage("Payment failed")
                .errorCode("AMOUNT_LIMIT_EXCEEDED")
                .errorMessage("Amount exceeds daily limit")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .processedAt(LocalDateTime.now())
                .build();
        } else {
            // Medium amounts go to processing
            response = FSPPaymentResponse.builder()
                .success(true)
                .fspReferenceNumber(fspReferenceNumber)
                .internalReferenceNumber(request.getInternalReferenceNumber())
                .status(FSPPaymentResponse.FSPPaymentStatus.PROCESSING)
                .statusMessage("Payment is being processed")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .fee(BigDecimal.valueOf(10.00))
                .processedAt(LocalDateTime.now())
                .estimatedCompletionTime(LocalDateTime.now().plusMinutes(5))
                .transactionId("TXN-" + fspReferenceNumber)
                .build();
        }

        // Store for status checking
        mockPayments.put(fspReferenceNumber, response);
        
        log.info("Mock FSP: Payment submitted with reference: {} and status: {}", 
                fspReferenceNumber, response.getStatus());
        
        return response;
    }

    @Override
    public FSPStatusResponse checkPaymentStatus(String fspReferenceNumber, FSPConfiguration config) {
        log.info("Mock FSP: Checking status for reference: {}", fspReferenceNumber);

        FSPPaymentResponse payment = mockPayments.get(fspReferenceNumber);
        if (payment == null) {
            return FSPStatusResponse.builder()
                .success(false)
                .fspReferenceNumber(fspReferenceNumber)
                .errorCode("PAYMENT_NOT_FOUND")
                .errorMessage("Payment not found")
                .build();
        }

        // Simulate status progression for processing payments
        if (payment.getStatus() == FSPPaymentResponse.FSPPaymentStatus.PROCESSING) {
            // After 5 minutes, mark as completed
            if (payment.getProcessedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
                payment.setStatus(FSPPaymentResponse.FSPPaymentStatus.COMPLETED);
                payment.setStatusMessage("Payment completed successfully");
                mockPayments.put(fspReferenceNumber, payment);
            }
        }

        return FSPStatusResponse.builder()
            .success(payment.isSuccess())
            .fspReferenceNumber(fspReferenceNumber)
            .status(payment.getStatus())
            .statusMessage(payment.getStatusMessage())
            .errorCode(payment.getErrorCode())
            .errorMessage(payment.getErrorMessage())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .fee(payment.getFee())
            .processedAt(payment.getProcessedAt())
            .completedAt(payment.getStatus() == FSPPaymentResponse.FSPPaymentStatus.COMPLETED ? 
                        LocalDateTime.now() : null)
            .transactionId(payment.getTransactionId())
            .receiptNumber(payment.getReceiptNumber())
            .build();
    }

    @Override
    public FSPPaymentResponse cancelPayment(String fspReferenceNumber, FSPConfiguration config) {
        log.info("Mock FSP: Cancelling payment with reference: {}", fspReferenceNumber);

        FSPPaymentResponse payment = mockPayments.get(fspReferenceNumber);
        if (payment == null) {
            return FSPPaymentResponse.builder()
                .success(false)
                .fspReferenceNumber(fspReferenceNumber)
                .status(FSPPaymentResponse.FSPPaymentStatus.FAILED)
                .errorCode("PAYMENT_NOT_FOUND")
                .errorMessage("Payment not found")
                .build();
        }

        if (payment.getStatus() == FSPPaymentResponse.FSPPaymentStatus.COMPLETED) {
            return FSPPaymentResponse.builder()
                .success(false)
                .fspReferenceNumber(fspReferenceNumber)
                .status(FSPPaymentResponse.FSPPaymentStatus.FAILED)
                .errorCode("CANNOT_CANCEL_COMPLETED")
                .errorMessage("Cannot cancel completed payment")
                .build();
        }

        payment.setStatus(FSPPaymentResponse.FSPPaymentStatus.CANCELLED);
        payment.setStatusMessage("Payment cancelled successfully");
        mockPayments.put(fspReferenceNumber, payment);

        return payment;
    }

    @Override
    public boolean validateConfiguration(FSPConfiguration config) {
        return config != null && FSP_CODE.equals(config.getFspCode());
    }

    @Override
    public boolean testConnection(FSPConfiguration config) {
        return true; // Mock always returns true
    }

    @Override
    public Set<Payment.PaymentMethod> getSupportedPaymentMethods() {
        return Set.of(
            Payment.PaymentMethod.E_WALLET,
            Payment.PaymentMethod.BANK_TRANSFER,
            Payment.PaymentMethod.CASH_PICKUP
        );
    }

    @Override
    public BigDecimal getMinimumAmount() {
        return BigDecimal.valueOf(1.00);
    }

    @Override
    public BigDecimal getMaximumAmount() {
        return BigDecimal.valueOf(50000.00);
    }

    @Override
    public void processWebhook(String payload, Map<String, String> headers) {
        log.info("Mock FSP: Processing webhook payload: {}", payload);
        // Mock webhook processing - in real implementation, this would parse the payload
        // and update payment status accordingly
    }
}
