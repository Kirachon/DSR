package ph.gov.dsr.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.payment.dto.*;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.entity.PaymentAuditLog;
import ph.gov.dsr.payment.entity.PaymentBatch;
import ph.gov.dsr.payment.repository.PaymentAuditLogRepository;
import ph.gov.dsr.payment.repository.PaymentBatchRepository;
import ph.gov.dsr.payment.repository.PaymentRepository;
import ph.gov.dsr.payment.service.PaymentBatchService;
import ph.gov.dsr.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentBatchService
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentBatchServiceImpl implements PaymentBatchService {

    private final PaymentBatchRepository batchRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository auditLogRepository;
    private final PaymentService paymentService;

    @Override
    public PaymentBatchResponse createPaymentBatch(PaymentBatchRequest request, String createdBy) {
        log.info("Creating payment batch for program: {}, payments: {}", 
                request.getProgramId(), request.getPaymentRequests().size());

        try {
            // Validate request
            validateBatchRequest(request);

            // Generate batch number
            String batchNumber = generateBatchNumber();

            // Calculate total amount
            BigDecimal totalAmount = request.getPaymentRequests().stream()
                .map(PaymentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create batch entity
            PaymentBatch batch = PaymentBatch.builder()
                .batchNumber(batchNumber)
                .programId(request.getProgramId())
                .programName(request.getProgramName())
                .status(PaymentBatch.BatchStatus.PENDING)
                .totalAmount(totalAmount)
                .currency("PHP")
                .totalPayments(request.getPaymentRequests().size())
                .successfulPayments(0)
                .failedPayments(0)
                .pendingPayments(request.getPaymentRequests().size())
                .scheduledDate(request.getScheduledDate())
                .description(request.getDescription())
                .metadata(request.getMetadata() != null ? convertMetadataToJson(request.getMetadata()) : null)
                .createdBy(createdBy)
                .build();

            // Save batch
            batch = batchRepository.save(batch);

            // Create individual payments
            List<Payment> payments = new ArrayList<>();
            for (PaymentRequest paymentRequest : request.getPaymentRequests()) {
                Payment payment = createPaymentFromRequest(paymentRequest, batch, createdBy);
                payments.add(payment);
            }

            // Save all payments
            payments = paymentRepository.saveAll(payments);
            batch.setPayments(payments);

            // Log audit event
            auditLogRepository.save(PaymentAuditLog.builder()
                .batchId(batch.getBatchId())
                .eventType(PaymentAuditLog.EventType.BATCH_CREATED)
                .description("Payment batch created with " + payments.size() + " payments")
                .userId(createdBy)
                .build());

            log.info("Payment batch created successfully with ID: {} and number: {}", 
                    batch.getBatchId(), batch.getBatchNumber());

            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error creating payment batch for program: {}", request.getProgramId(), e);
            throw new RuntimeException("Failed to create payment batch: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentBatchResponse getPaymentBatchById(UUID batchId) {
        log.debug("Getting payment batch by ID: {}", batchId);

        PaymentBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

        return mapToResponse(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentBatchResponse getPaymentBatchByNumber(String batchNumber) {
        log.debug("Getting payment batch by number: {}", batchNumber);

        PaymentBatch batch = batchRepository.findByBatchNumber(batchNumber)
            .orElseThrow(() -> new RuntimeException("Payment batch not found with number: " + batchNumber));

        return mapToResponse(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentBatchResponse> getPaymentBatchesByProgramId(UUID programId, Pageable pageable) {
        log.debug("Getting payment batches for program: {}", programId);

        Page<PaymentBatch> batches = batchRepository.findByProgramId(programId, pageable);
        return batches.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentBatchResponse> getPaymentBatchesByStatus(PaymentBatch.BatchStatus status, Pageable pageable) {
        log.debug("Getting payment batches with status: {}", status);

        Page<PaymentBatch> batches = batchRepository.findByStatus(status, pageable);
        return batches.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentBatchResponse> searchPaymentBatches(UUID programId, PaymentBatch.BatchStatus status,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          Pageable pageable) {
        log.debug("Searching payment batches with criteria - program: {}, status: {}", programId, status);

        Page<PaymentBatch> batches = batchRepository.findByCriteria(
            programId, status, startDate, endDate, pageable);
        
        return batches.map(this::mapToResponse);
    }

    @Override
    public PaymentBatchResponse startBatchProcessing(UUID batchId, String startedBy) {
        log.info("Starting batch processing: {}", batchId);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            if (!canStartBatch(batchId)) {
                throw new RuntimeException("Batch cannot be started in current state: " + batch.getStatus());
            }

            // Update batch status
            PaymentBatch.BatchStatus oldStatus = batch.getStatus();
            batch.setStatus(PaymentBatch.BatchStatus.PROCESSING);
            batch.setStartedDate(LocalDateTime.now());
            batch.setUpdatedBy(startedBy);
            batch = batchRepository.save(batch);

            // Log status change
            auditLogRepository.save(PaymentAuditLog.builder()
                .batchId(batchId)
                .eventType(PaymentAuditLog.EventType.BATCH_STARTED)
                .oldStatus(oldStatus.name())
                .newStatus(batch.getStatus().name())
                .description("Batch processing started")
                .userId(startedBy)
                .build());

            // Process payments asynchronously
            processPaymentsInBatch(batch, startedBy);

            log.info("Batch processing started successfully: {}", batchId);
            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error starting batch processing: {}", batchId, e);
            throw new RuntimeException("Failed to start batch processing: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentBatchResponse cancelPaymentBatch(UUID batchId, String reason, String cancelledBy) {
        log.info("Cancelling payment batch: {} with reason: {}", batchId, reason);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            if (batch.getStatus() == PaymentBatch.BatchStatus.COMPLETED) {
                throw new RuntimeException("Cannot cancel completed batch");
            }

            PaymentBatch.BatchStatus oldStatus = batch.getStatus();
            batch.setStatus(PaymentBatch.BatchStatus.CANCELLED);
            batch.setUpdatedBy(cancelledBy);
            batch = batchRepository.save(batch);

            // Cancel all pending payments in the batch
            List<Payment> payments = paymentRepository.findByBatch_BatchId(batchId);
            for (Payment payment : payments) {
                if (payment.getStatus() == Payment.PaymentStatus.PENDING || 
                    payment.getStatus() == Payment.PaymentStatus.PROCESSING) {
                    try {
                        paymentService.cancelPayment(payment.getPaymentId(), reason, cancelledBy);
                    } catch (Exception e) {
                        log.warn("Error cancelling payment in batch: {}", payment.getPaymentId(), e);
                    }
                }
            }

            // Log status change
            auditLogRepository.save(PaymentAuditLog.builder()
                .batchId(batchId)
                .eventType(PaymentAuditLog.EventType.BATCH_CANCELLED)
                .oldStatus(oldStatus.name())
                .newStatus(batch.getStatus().name())
                .description("Batch cancelled: " + reason)
                .userId(cancelledBy)
                .build());

            log.info("Payment batch cancelled successfully: {}", batchId);
            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error cancelling payment batch: {}", batchId, e);
            throw new RuntimeException("Failed to cancel payment batch: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentBatchResponse> processScheduledBatches() {
        log.info("Processing scheduled batches");

        try {
            List<PaymentBatch> scheduledBatches = batchRepository.findScheduledBatches(LocalDateTime.now());
            List<PaymentBatchResponse> processedBatches = new ArrayList<>();

            for (PaymentBatch batch : scheduledBatches) {
                try {
                    PaymentBatchResponse response = startBatchProcessing(batch.getBatchId(), "SYSTEM");
                    processedBatches.add(response);
                } catch (Exception e) {
                    log.error("Error processing scheduled batch: {}", batch.getBatchId(), e);
                }
            }

            log.info("Processed {} scheduled batches", processedBatches.size());
            return processedBatches;

        } catch (Exception e) {
            log.error("Error processing scheduled batches", e);
            throw new RuntimeException("Failed to process scheduled batches: " + e.getMessage(), e);
        }
    }

    // Helper methods
    private String generateBatchNumber() {
        return "BATCH-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%06d", new Random().nextInt(999999));
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        // Simple JSON conversion - in production, use proper JSON library
        return metadata.toString();
    }

    private Payment createPaymentFromRequest(PaymentRequest request, PaymentBatch batch, String createdBy) {
        String internalRefNumber = "PAY-" + LocalDateTime.now().getYear() + "-" + 
                                  String.format("%06d", new Random().nextInt(999999));

        return Payment.builder()
            .householdId(request.getHouseholdId())
            .programId(request.getProgramId())
            .beneficiaryId(request.getBeneficiaryId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .status(Payment.PaymentStatus.PENDING)
            .paymentMethod(request.getPaymentMethod())
            .fspCode(request.getFspCode())
            .internalReferenceNumber(internalRefNumber)
            .recipientAccountNumber(request.getRecipientAccountNumber())
            .recipientAccountName(request.getRecipientAccountName())
            .recipientMobileNumber(request.getRecipientMobileNumber())
            .scheduledDate(request.getScheduledDate() != null ? request.getScheduledDate() : LocalDateTime.now())
            .maxRetryCount(request.getMaxRetryCount())
            .retryCount(0)
            .batch(batch)
            .metadata(request.getMetadata() != null ? convertMetadataToJson(request.getMetadata()) : null)
            .createdBy(createdBy)
            .build();
    }

    private void processPaymentsInBatch(PaymentBatch batch, String processedBy) {
        // This would typically be done asynchronously
        // For now, we'll just update the batch status
        log.info("Processing payments in batch: {}", batch.getBatchId());
        
        // In a real implementation, this would:
        // 1. Process payments in parallel
        // 2. Update batch progress
        // 3. Handle failures and retries
        // 4. Complete the batch when all payments are processed
    }

    private PaymentBatchResponse mapToResponse(PaymentBatch batch) {
        PaymentBatchResponse response = PaymentBatchResponse.builder()
            .batchId(batch.getBatchId())
            .batchNumber(batch.getBatchNumber())
            .programId(batch.getProgramId())
            .programName(batch.getProgramName())
            .status(batch.getStatus())
            .totalAmount(batch.getTotalAmount())
            .currency(batch.getCurrency())
            .totalPayments(batch.getTotalPayments())
            .successfulPayments(batch.getSuccessfulPayments())
            .failedPayments(batch.getFailedPayments())
            .pendingPayments(batch.getPendingPayments())
            .scheduledDate(batch.getScheduledDate())
            .startedDate(batch.getStartedDate())
            .completedDate(batch.getCompletedDate())
            .description(batch.getDescription())
            .createdBy(batch.getCreatedBy())
            .updatedBy(batch.getUpdatedBy())
            .createdAt(batch.getCreatedAt())
            .updatedAt(batch.getUpdatedAt())
            .version(batch.getVersion())
            .build();

        response.setComputedFields();
        return response;
    }

    @Override
    public PaymentBatchResponse pauseBatchProcessing(UUID batchId, String pausedBy) {
        log.info("Pausing batch processing: {}", batchId);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            if (batch.getStatus() != PaymentBatch.BatchStatus.PROCESSING) {
                throw new RuntimeException("Can only pause processing batches");
            }

            PaymentBatch.BatchStatus oldStatus = batch.getStatus();
            batch.setStatus(PaymentBatch.BatchStatus.PAUSED);
            batch.setUpdatedBy(pausedBy);
            batch = batchRepository.save(batch);

            // Log status change
            auditLogRepository.save(PaymentAuditLog.builder()
                .batchId(batchId)
                .eventType(PaymentAuditLog.EventType.BATCH_CANCELLED) // Using CANCELLED for pause
                .oldStatus(oldStatus.name())
                .newStatus(batch.getStatus().name())
                .description("Batch processing paused")
                .userId(pausedBy)
                .build());

            log.info("Batch processing paused successfully: {}", batchId);
            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error pausing batch processing: {}", batchId, e);
            throw new RuntimeException("Failed to pause batch processing: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentBatchResponse resumeBatchProcessing(UUID batchId, String resumedBy) {
        log.info("Resuming batch processing: {}", batchId);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            if (batch.getStatus() != PaymentBatch.BatchStatus.PAUSED) {
                throw new RuntimeException("Can only resume paused batches");
            }

            PaymentBatch.BatchStatus oldStatus = batch.getStatus();
            batch.setStatus(PaymentBatch.BatchStatus.PROCESSING);
            batch.setUpdatedBy(resumedBy);
            batch = batchRepository.save(batch);

            // Log status change
            auditLogRepository.save(PaymentAuditLog.builder()
                .batchId(batchId)
                .eventType(PaymentAuditLog.EventType.BATCH_STARTED)
                .oldStatus(oldStatus.name())
                .newStatus(batch.getStatus().name())
                .description("Batch processing resumed")
                .userId(resumedBy)
                .build());

            log.info("Batch processing resumed successfully: {}", batchId);
            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error resuming batch processing: {}", batchId, e);
            throw new RuntimeException("Failed to resume batch processing: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentBatchResponse updateBatchStatus(UUID batchId, PaymentBatch.BatchStatus status,
                                                 String reason, String updatedBy) {
        log.info("Updating batch status: {} to {}", batchId, status);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            PaymentBatch.BatchStatus oldStatus = batch.getStatus();
            batch.setStatus(status);
            batch.setUpdatedBy(updatedBy);

            if (status == PaymentBatch.BatchStatus.COMPLETED) {
                batch.setCompletedDate(LocalDateTime.now());
                batch.updatePaymentCounts(); // Update payment counts
            }

            batch = batchRepository.save(batch);

            // Log status change
            auditLogRepository.save(PaymentAuditLog.builder()
                .batchId(batchId)
                .eventType(PaymentAuditLog.EventType.BATCH_COMPLETED)
                .oldStatus(oldStatus.name())
                .newStatus(status.name())
                .description("Batch status updated: " + reason)
                .userId(updatedBy)
                .build());

            log.info("Batch status updated successfully: {} from {} to {}",
                    batchId, oldStatus, status);

            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error updating batch status: {}", batchId, e);
            throw new RuntimeException("Failed to update batch status: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentBatchResponse monitorBatchProgress(UUID batchId) {
        log.debug("Monitoring batch progress: {}", batchId);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            // Update payment counts from actual payments
            batch.updatePaymentCounts();
            batch = batchRepository.save(batch);

            // Check if batch should be completed
            if (batch.getStatus() == PaymentBatch.BatchStatus.PROCESSING &&
                batch.getPendingPayments() == 0) {
                batch.setStatus(PaymentBatch.BatchStatus.COMPLETED);
                batch.setCompletedDate(LocalDateTime.now());
                batch = batchRepository.save(batch);
            }

            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error monitoring batch progress: {}", batchId, e);
            throw new RuntimeException("Failed to monitor batch progress: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBatchStatistics() {
        log.debug("Getting batch statistics");

        try {
            List<Object[]> stats = batchRepository.getBatchStatistics();
            Map<String, Object> result = new HashMap<>();

            for (Object[] stat : stats) {
                PaymentBatch.BatchStatus status = (PaymentBatch.BatchStatus) stat[0];
                Long count = (Long) stat[1];
                BigDecimal amount = (BigDecimal) stat[2];

                Map<String, Object> statusStats = new HashMap<>();
                statusStats.put("count", count);
                statusStats.put("totalAmount", amount);
                result.put(status.name(), statusStats);
            }

            return result;

        } catch (Exception e) {
            log.error("Error getting batch statistics", e);
            throw new RuntimeException("Failed to get batch statistics: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBatchStatisticsByProgram() {
        log.debug("Getting batch statistics by program");

        try {
            List<Object[]> stats = batchRepository.getBatchStatisticsByProgram();
            Map<String, Object> result = new HashMap<>();

            for (Object[] stat : stats) {
                UUID programId = (UUID) stat[0];
                String programName = (String) stat[1];
                Long count = (Long) stat[2];
                BigDecimal amount = (BigDecimal) stat[3];

                Map<String, Object> programStats = new HashMap<>();
                programStats.put("programName", programName);
                programStats.put("count", count);
                programStats.put("totalAmount", amount);
                result.put(programId.toString(), programStats);
            }

            return result;

        } catch (Exception e) {
            log.error("Error getting batch statistics by program", e);
            throw new RuntimeException("Failed to get batch statistics by program: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentBatchResponse> getRecentBatches(Pageable pageable) {
        log.debug("Getting recent batches");

        Page<PaymentBatch> batches = batchRepository.findRecentBatches(pageable);
        return batches.map(this::mapToResponse);
    }

    @Override
    public void validateBatchRequest(PaymentBatchRequest request) {
        if (request.getProgramId() == null) {
            throw new IllegalArgumentException("Program ID is required");
        }

        if (request.getProgramName() == null || request.getProgramName().trim().isEmpty()) {
            throw new IllegalArgumentException("Program name is required");
        }

        if (request.getScheduledDate() == null) {
            throw new IllegalArgumentException("Scheduled date is required");
        }

        if (request.getPaymentRequests() == null || request.getPaymentRequests().isEmpty()) {
            throw new IllegalArgumentException("Payment requests cannot be empty");
        }

        // Validate each payment request
        for (PaymentRequest paymentRequest : request.getPaymentRequests()) {
            try {
                paymentService.validatePaymentRequest(paymentRequest);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid payment request: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean canStartBatch(UUID batchId) {
        PaymentBatch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            return false;
        }

        return batch.getStatus() == PaymentBatch.BatchStatus.PENDING;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBatchCompletionEstimate(UUID batchId) {
        log.debug("Getting batch completion estimate: {}", batchId);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            Map<String, Object> estimate = new HashMap<>();
            estimate.put("batchId", batchId);
            estimate.put("totalPayments", batch.getTotalPayments());
            estimate.put("completedPayments", batch.getSuccessfulPayments() + batch.getFailedPayments());
            estimate.put("remainingPayments", batch.getPendingPayments());

            if (batch.getStartedDate() != null && batch.getSuccessfulPayments() > 0) {
                long elapsedMinutes = java.time.Duration.between(batch.getStartedDate(), LocalDateTime.now()).toMinutes();
                double completionRate = (double) (batch.getSuccessfulPayments() + batch.getFailedPayments()) / batch.getTotalPayments();

                if (completionRate > 0) {
                    long estimatedTotalMinutes = (long) (elapsedMinutes / completionRate);
                    long estimatedRemainingMinutes = estimatedTotalMinutes - elapsedMinutes;

                    estimate.put("estimatedCompletionTime", LocalDateTime.now().plusMinutes(estimatedRemainingMinutes));
                    estimate.put("estimatedRemainingMinutes", estimatedRemainingMinutes);
                }
            }

            return estimate;

        } catch (Exception e) {
            log.error("Error getting batch completion estimate: {}", batchId, e);
            throw new RuntimeException("Failed to get batch completion estimate: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentBatchResponse retryFailedPaymentsInBatch(UUID batchId, String retriedBy) {
        log.info("Retrying failed payments in batch: {}", batchId);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            List<Payment> failedPayments = paymentRepository.findByBatch_BatchId(batchId).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.FAILED && p.canRetry())
                .collect(Collectors.toList());

            int retriedCount = 0;
            for (Payment payment : failedPayments) {
                try {
                    paymentService.retryPayment(payment.getPaymentId(), retriedBy);
                    retriedCount++;
                } catch (Exception e) {
                    log.warn("Error retrying payment in batch: {}", payment.getPaymentId(), e);
                }
            }

            // Update batch counts
            batch.updatePaymentCounts();
            batch = batchRepository.save(batch);

            log.info("Retried {} failed payments in batch: {}", retriedCount, batchId);
            return mapToResponse(batch);

        } catch (Exception e) {
            log.error("Error retrying failed payments in batch: {}", batchId, e);
            throw new RuntimeException("Failed to retry failed payments: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateBatchReport(UUID batchId) {
        log.info("Generating batch report: {}", batchId);

        try {
            PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payment batch not found with ID: " + batchId));

            List<Payment> payments = paymentRepository.findByBatch_BatchId(batchId);

            Map<String, Object> report = new HashMap<>();
            report.put("batchInfo", mapToResponse(batch));
            report.put("paymentSummary", generatePaymentSummary(payments));
            report.put("fspBreakdown", generateFspBreakdown(payments));
            report.put("generatedAt", LocalDateTime.now());

            return report;

        } catch (Exception e) {
            log.error("Error generating batch report: {}", batchId, e);
            throw new RuntimeException("Failed to generate batch report: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> generatePaymentSummary(List<Payment> payments) {
        Map<String, Object> summary = new HashMap<>();

        Map<Payment.PaymentStatus, Long> statusCounts = payments.stream()
            .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));

        Map<Payment.PaymentStatus, BigDecimal> statusAmounts = payments.stream()
            .collect(Collectors.groupingBy(Payment::getStatus,
                Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        summary.put("statusCounts", statusCounts);
        summary.put("statusAmounts", statusAmounts);

        return summary;
    }

    private Map<String, Object> generateFspBreakdown(List<Payment> payments) {
        Map<String, Object> breakdown = new HashMap<>();

        Map<String, Long> fspCounts = payments.stream()
            .collect(Collectors.groupingBy(Payment::getFspCode, Collectors.counting()));

        Map<String, BigDecimal> fspAmounts = payments.stream()
            .collect(Collectors.groupingBy(Payment::getFspCode,
                Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        breakdown.put("fspCounts", fspCounts);
        breakdown.put("fspAmounts", fspAmounts);

        return breakdown;
    }
}
