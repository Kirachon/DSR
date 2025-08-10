package ph.gov.dsr.payment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ph.gov.dsr.payment.dto.PaymentBatchRequest;
import ph.gov.dsr.payment.dto.PaymentBatchResponse;
import ph.gov.dsr.payment.entity.PaymentBatch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for payment batch operations
 */
public interface PaymentBatchService {

    /**
     * Create a new payment batch
     */
    PaymentBatchResponse createPaymentBatch(PaymentBatchRequest request, String createdBy);

    /**
     * Get payment batch by ID
     */
    PaymentBatchResponse getPaymentBatchById(UUID batchId);

    /**
     * Get payment batch by batch number
     */
    PaymentBatchResponse getPaymentBatchByNumber(String batchNumber);

    /**
     * Get payment batches by program ID
     */
    Page<PaymentBatchResponse> getPaymentBatchesByProgramId(UUID programId, Pageable pageable);

    /**
     * Get payment batches by status
     */
    Page<PaymentBatchResponse> getPaymentBatchesByStatus(PaymentBatch.BatchStatus status, Pageable pageable);

    /**
     * Search payment batches by criteria
     */
    Page<PaymentBatchResponse> searchPaymentBatches(UUID programId, PaymentBatch.BatchStatus status,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   Pageable pageable);

    /**
     * Start processing a payment batch
     */
    PaymentBatchResponse startBatchProcessing(UUID batchId, String startedBy);

    /**
     * Pause batch processing
     */
    PaymentBatchResponse pauseBatchProcessing(UUID batchId, String pausedBy);

    /**
     * Resume batch processing
     */
    PaymentBatchResponse resumeBatchProcessing(UUID batchId, String resumedBy);

    /**
     * Cancel payment batch
     */
    PaymentBatchResponse cancelPaymentBatch(UUID batchId, String reason, String cancelledBy);

    /**
     * Update batch status
     */
    PaymentBatchResponse updateBatchStatus(UUID batchId, PaymentBatch.BatchStatus status, 
                                          String reason, String updatedBy);

    /**
     * Process scheduled batches
     */
    List<PaymentBatchResponse> processScheduledBatches();

    /**
     * Monitor batch processing progress
     */
    PaymentBatchResponse monitorBatchProgress(UUID batchId);

    /**
     * Get batch processing statistics
     */
    Map<String, Object> getBatchStatistics();

    /**
     * Get batch statistics by program
     */
    Map<String, Object> getBatchStatisticsByProgram();

    /**
     * Get recent batches for dashboard
     */
    Page<PaymentBatchResponse> getRecentBatches(Pageable pageable);

    /**
     * Validate batch request
     */
    void validateBatchRequest(PaymentBatchRequest request);

    /**
     * Check if batch can be started
     */
    boolean canStartBatch(UUID batchId);

    /**
     * Get batch completion estimate
     */
    Map<String, Object> getBatchCompletionEstimate(UUID batchId);

    /**
     * Retry failed payments in batch
     */
    PaymentBatchResponse retryFailedPaymentsInBatch(UUID batchId, String retriedBy);

    /**
     * Generate batch report
     */
    Map<String, Object> generateBatchReport(UUID batchId);
}
