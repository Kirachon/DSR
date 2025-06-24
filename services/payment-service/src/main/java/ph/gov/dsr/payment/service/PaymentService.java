package ph.gov.dsr.payment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.dto.PaymentResponse;
import ph.gov.dsr.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for payment operations
 */
public interface PaymentService {

    /**
     * Create a new payment
     */
    PaymentResponse createPayment(PaymentRequest request, String createdBy);

    /**
     * Get payment by ID
     */
    PaymentResponse getPaymentById(UUID paymentId);

    /**
     * Get payment by internal reference number
     */
    PaymentResponse getPaymentByReferenceNumber(String referenceNumber);

    /**
     * Get payments by household ID
     */
    Page<PaymentResponse> getPaymentsByHouseholdId(UUID householdId, Pageable pageable);

    /**
     * Get payments by program ID
     */
    Page<PaymentResponse> getPaymentsByProgramId(UUID programId, Pageable pageable);

    /**
     * Get payments by status
     */
    Page<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable);

    /**
     * Search payments by criteria
     */
    Page<PaymentResponse> searchPayments(UUID householdId, UUID programId, 
                                        Payment.PaymentStatus status, String fspCode,
                                        LocalDateTime startDate, LocalDateTime endDate,
                                        Pageable pageable);

    /**
     * Process payment (submit to FSP)
     */
    PaymentResponse processPayment(UUID paymentId, String processedBy);

    /**
     * Cancel payment
     */
    PaymentResponse cancelPayment(UUID paymentId, String reason, String cancelledBy);

    /**
     * Retry failed payment
     */
    PaymentResponse retryPayment(UUID paymentId, String retriedBy);

    /**
     * Update payment status
     */
    PaymentResponse updatePaymentStatus(UUID paymentId, Payment.PaymentStatus status, 
                                       String reason, String updatedBy);

    /**
     * Check payment status with FSP
     */
    PaymentResponse checkPaymentStatusWithFSP(UUID paymentId);

    /**
     * Process scheduled payments
     */
    List<PaymentResponse> processScheduledPayments();

    /**
     * Process retry payments
     */
    List<PaymentResponse> processRetryPayments();

    /**
     * Get payment statistics
     */
    Map<String, Object> getPaymentStatistics();

    /**
     * Get payment statistics by FSP
     */
    Map<String, Object> getPaymentStatisticsByFsp();

    /**
     * Get daily payment volume
     */
    List<Map<String, Object>> getDailyPaymentVolume(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Reconcile payments with FSP
     */
    Map<String, Object> reconcilePayments(String fspCode, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Validate payment request
     */
    void validatePaymentRequest(PaymentRequest request);

    /**
     * Check if payment can be processed
     */
    boolean canProcessPayment(UUID paymentId);

    /**
     * Get total amount by status and date range
     */
    BigDecimal getTotalAmountByStatusAndDateRange(Payment.PaymentStatus status,
                                                 LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count payments by status
     */
    long countPaymentsByStatus(Payment.PaymentStatus status);
}
