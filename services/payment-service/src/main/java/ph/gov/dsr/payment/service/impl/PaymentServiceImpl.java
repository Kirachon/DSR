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
import ph.gov.dsr.payment.repository.PaymentAuditLogRepository;
import ph.gov.dsr.payment.repository.PaymentRepository;
import ph.gov.dsr.payment.service.FSPServiceRegistry;
import ph.gov.dsr.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentService
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository auditLogRepository;
    private final FSPServiceRegistry fspServiceRegistry;

    @Override
    public PaymentResponse createPayment(PaymentRequest request, String createdBy) {
        log.info("Creating payment for household: {}, program: {}, amount: {}", 
                request.getHouseholdId(), request.getProgramId(), request.getAmount());

        try {
            // Validate request
            validatePaymentRequest(request);

            // Generate internal reference number
            String internalRefNumber = generateInternalReferenceNumber();

            // Create payment entity
            Payment payment = Payment.builder()
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
                .metadata(request.getMetadata() != null ? convertMetadataToJson(request.getMetadata()) : null)
                .createdBy(createdBy)
                .build();

            // Save payment
            payment = paymentRepository.save(payment);

            // Log audit event
            auditLogRepository.save(PaymentAuditLog.paymentCreated(payment.getPaymentId(), createdBy));

            log.info("Payment created successfully with ID: {} and reference: {}", 
                    payment.getPaymentId(), payment.getInternalReferenceNumber());

            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error creating payment for household: {}", request.getHouseholdId(), e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        log.debug("Getting payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReferenceNumber(String referenceNumber) {
        log.debug("Getting payment by reference number: {}", referenceNumber);

        Payment payment = paymentRepository.findByInternalReferenceNumber(referenceNumber)
            .orElseThrow(() -> new RuntimeException("Payment not found with reference: " + referenceNumber));

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByHouseholdId(UUID householdId, Pageable pageable) {
        log.debug("Getting payments for household: {}", householdId);

        Page<Payment> payments = paymentRepository.findByHouseholdId(householdId, pageable);
        return payments.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByProgramId(UUID programId, Pageable pageable) {
        log.debug("Getting payments for program: {}", programId);

        Page<Payment> payments = paymentRepository.findByProgramId(programId, pageable);
        return payments.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable) {
        log.debug("Getting payments with status: {}", status);

        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
        return payments.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> searchPayments(UUID householdId, UUID programId, 
                                               Payment.PaymentStatus status, String fspCode,
                                               LocalDateTime startDate, LocalDateTime endDate,
                                               Pageable pageable) {
        log.debug("Searching payments with criteria - household: {}, program: {}, status: {}, fsp: {}", 
                householdId, programId, status, fspCode);

        Page<Payment> payments = paymentRepository.findByCriteria(
            householdId, programId, status, fspCode, startDate, endDate, pageable);
        
        return payments.map(this::mapToResponse);
    }

    @Override
    public PaymentResponse processPayment(UUID paymentId, String processedBy) {
        log.info("Processing payment: {}", paymentId);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

            if (!canProcessPayment(paymentId)) {
                throw new RuntimeException("Payment cannot be processed in current state: " + payment.getStatus());
            }

            // Update status to processing
            Payment.PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            payment.setProcessedDate(LocalDateTime.now());
            payment.setUpdatedBy(processedBy);
            payment = paymentRepository.save(payment);

            // Log status change
            auditLogRepository.save(PaymentAuditLog.statusChanged(
                paymentId, oldStatus.name(), payment.getStatus().name(), processedBy));

            // Submit to FSP
            FSPPaymentResponse fspResponse = submitToFSP(payment);

            // Update payment based on FSP response
            updatePaymentFromFSPResponse(payment, fspResponse, processedBy);

            log.info("Payment processed successfully: {}, FSP status: {}", 
                    paymentId, fspResponse.getStatus());

            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error processing payment: {}", paymentId, e);
            
            // Update payment status to failed
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(e.getMessage());
                payment.setUpdatedBy(processedBy);
                paymentRepository.save(payment);

                // Log error
                auditLogRepository.save(PaymentAuditLog.error(
                    paymentId, "PROCESSING_ERROR", e.getMessage(), processedBy));
            }

            throw new RuntimeException("Failed to process payment: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse cancelPayment(UUID paymentId, String reason, String cancelledBy) {
        log.info("Cancelling payment: {} with reason: {}", paymentId, reason);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

            if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
                throw new RuntimeException("Cannot cancel completed payment");
            }

            Payment.PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(Payment.PaymentStatus.CANCELLED);
            payment.setFailureReason(reason);
            payment.setUpdatedBy(cancelledBy);
            payment = paymentRepository.save(payment);

            // Log status change
            auditLogRepository.save(PaymentAuditLog.statusChanged(
                paymentId, oldStatus.name(), payment.getStatus().name(), cancelledBy));

            // Try to cancel with FSP if it was already submitted
            if (payment.getFspReferenceNumber() != null) {
                try {
                    fspServiceRegistry.cancelPayment(payment.getFspCode(), 
                        payment.getFspReferenceNumber());
                } catch (Exception e) {
                    log.warn("Failed to cancel payment with FSP: {}", e.getMessage());
                }
            }

            log.info("Payment cancelled successfully: {}", paymentId);
            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error cancelling payment: {}", paymentId, e);
            throw new RuntimeException("Failed to cancel payment: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse retryPayment(UUID paymentId, String retriedBy) {
        log.info("Retrying payment: {}", paymentId);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

            if (!payment.canRetry()) {
                throw new RuntimeException("Payment cannot be retried - max retries exceeded or invalid status");
            }

            payment.incrementRetryCount();
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setFailureReason(null);
            payment.setUpdatedBy(retriedBy);
            payment = paymentRepository.save(payment);

            // Log retry attempt
            auditLogRepository.save(PaymentAuditLog.builder()
                .paymentId(paymentId)
                .eventType(PaymentAuditLog.EventType.PAYMENT_RETRY)
                .description("Payment retry attempt #" + payment.getRetryCount())
                .userId(retriedBy)
                .build());

            log.info("Payment retry initiated: {}, attempt: {}", paymentId, payment.getRetryCount());
            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error retrying payment: {}", paymentId, e);
            throw new RuntimeException("Failed to retry payment: " + e.getMessage(), e);
        }
    }

    // Helper methods
    private String generateInternalReferenceNumber() {
        return "PAY-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%06d", new Random().nextInt(999999));
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        // Simple JSON conversion - in production, use proper JSON library
        return metadata.toString();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = PaymentResponse.builder()
            .paymentId(payment.getPaymentId())
            .householdId(payment.getHouseholdId())
            .programId(payment.getProgramId())
            .beneficiaryId(payment.getBeneficiaryId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .status(payment.getStatus())
            .paymentMethod(payment.getPaymentMethod())
            .fspCode(payment.getFspCode())
            .fspReferenceNumber(payment.getFspReferenceNumber())
            .internalReferenceNumber(payment.getInternalReferenceNumber())
            .recipientAccountNumber(payment.getRecipientAccountNumber())
            .recipientAccountName(payment.getRecipientAccountName())
            .recipientMobileNumber(payment.getRecipientMobileNumber())
            .scheduledDate(payment.getScheduledDate())
            .processedDate(payment.getProcessedDate())
            .completedDate(payment.getCompletedDate())
            .failureReason(payment.getFailureReason())
            .retryCount(payment.getRetryCount())
            .maxRetryCount(payment.getMaxRetryCount())
            .batchId(payment.getBatch() != null ? payment.getBatch().getBatchId() : null)
            .createdBy(payment.getCreatedBy())
            .updatedBy(payment.getUpdatedBy())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .version(payment.getVersion())
            .build();

        response.setComputedFields();
        return response;
    }

    @Override
    public PaymentResponse updatePaymentStatus(UUID paymentId, Payment.PaymentStatus status,
                                             String reason, String updatedBy) {
        log.info("Updating payment status: {} to {}", paymentId, status);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

            Payment.PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(status);
            payment.setFailureReason(reason);
            payment.setUpdatedBy(updatedBy);

            if (status == Payment.PaymentStatus.COMPLETED) {
                payment.setCompletedDate(LocalDateTime.now());
            }

            payment = paymentRepository.save(payment);

            // Log status change
            auditLogRepository.save(PaymentAuditLog.statusChanged(
                paymentId, oldStatus.name(), status.name(), updatedBy));

            log.info("Payment status updated successfully: {} from {} to {}",
                    paymentId, oldStatus, status);

            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error updating payment status: {}", paymentId, e);
            throw new RuntimeException("Failed to update payment status: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse checkPaymentStatusWithFSP(UUID paymentId) {
        log.info("Checking payment status with FSP: {}", paymentId);

        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

            if (payment.getFspReferenceNumber() == null) {
                throw new RuntimeException("Payment has not been submitted to FSP yet");
            }

            FSPStatusResponse fspStatus = fspServiceRegistry.checkPaymentStatus(
                payment.getFspCode(), payment.getFspReferenceNumber());

            // Update payment status based on FSP response
            if (fspStatus.isCompleted() && payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setCompletedDate(LocalDateTime.now());
                paymentRepository.save(payment);
            } else if (fspStatus.isFailed() && payment.getStatus() != Payment.PaymentStatus.FAILED) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(fspStatus.getErrorMessage());
                paymentRepository.save(payment);
            }

            log.info("Payment status checked with FSP: {}, status: {}", paymentId, fspStatus.getStatus());
            return mapToResponse(payment);

        } catch (Exception e) {
            log.error("Error checking payment status with FSP: {}", paymentId, e);
            throw new RuntimeException("Failed to check payment status: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> processScheduledPayments() {
        log.info("Processing scheduled payments");

        try {
            List<Payment> scheduledPayments = paymentRepository.findScheduledPayments(LocalDateTime.now());
            List<PaymentResponse> processedPayments = new ArrayList<>();

            for (Payment payment : scheduledPayments) {
                try {
                    PaymentResponse response = processPayment(payment.getPaymentId(), "SYSTEM");
                    processedPayments.add(response);
                } catch (Exception e) {
                    log.error("Error processing scheduled payment: {}", payment.getPaymentId(), e);
                }
            }

            log.info("Processed {} scheduled payments", processedPayments.size());
            return processedPayments;

        } catch (Exception e) {
            log.error("Error processing scheduled payments", e);
            throw new RuntimeException("Failed to process scheduled payments: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> processRetryPayments() {
        log.info("Processing retry payments");

        try {
            List<Payment> retryPayments = paymentRepository.findFailedPaymentsForRetry(
                LocalDateTime.now().minusMinutes(30)); // Retry after 30 minutes
            List<PaymentResponse> processedPayments = new ArrayList<>();

            for (Payment payment : retryPayments) {
                try {
                    PaymentResponse response = retryPayment(payment.getPaymentId(), "SYSTEM");
                    processedPayments.add(response);
                } catch (Exception e) {
                    log.error("Error retrying payment: {}", payment.getPaymentId(), e);
                }
            }

            log.info("Processed {} retry payments", processedPayments.size());
            return processedPayments;

        } catch (Exception e) {
            log.error("Error processing retry payments", e);
            throw new RuntimeException("Failed to process retry payments: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatistics() {
        log.debug("Getting payment statistics");

        try {
            List<Object[]> stats = paymentRepository.getPaymentStatistics();
            Map<String, Object> result = new HashMap<>();

            for (Object[] stat : stats) {
                Payment.PaymentStatus status = (Payment.PaymentStatus) stat[0];
                Long count = (Long) stat[1];
                BigDecimal amount = (BigDecimal) stat[2];

                Map<String, Object> statusStats = new HashMap<>();
                statusStats.put("count", count);
                statusStats.put("totalAmount", amount);
                result.put(status.name(), statusStats);
            }

            return result;

        } catch (Exception e) {
            log.error("Error getting payment statistics", e);
            throw new RuntimeException("Failed to get payment statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (request.getHouseholdId() == null) {
            throw new IllegalArgumentException("Household ID is required");
        }

        if (request.getProgramId() == null) {
            throw new IllegalArgumentException("Program ID is required");
        }

        if (request.getBeneficiaryId() == null) {
            throw new IllegalArgumentException("Beneficiary ID is required");
        }

        if (request.getFspCode() == null || request.getFspCode().trim().isEmpty()) {
            throw new IllegalArgumentException("FSP code is required");
        }

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // Validate FSP supports the payment method and amount
        if (!fspServiceRegistry.supportsPaymentMethod(request.getFspCode(), request.getPaymentMethod())) {
            throw new IllegalArgumentException("FSP does not support the specified payment method");
        }

        if (!fspServiceRegistry.supportsAmount(request.getFspCode(), request.getAmount())) {
            throw new IllegalArgumentException("Payment amount is outside FSP limits");
        }
    }

    @Override
    public boolean canProcessPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return false;
        }

        return payment.getStatus() == Payment.PaymentStatus.PENDING;
    }

    private FSPPaymentResponse submitToFSP(Payment payment) {
        FSPPaymentRequest fspRequest = FSPPaymentRequest.builder()
            .paymentId(payment.getPaymentId())
            .internalReferenceNumber(payment.getInternalReferenceNumber())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .paymentMethod(payment.getPaymentMethod())
            .recipientAccountNumber(payment.getRecipientAccountNumber())
            .recipientAccountName(payment.getRecipientAccountName())
            .recipientMobileNumber(payment.getRecipientMobileNumber())
            .description("DSR Payment - " + payment.getInternalReferenceNumber())
            .correlationId(UUID.randomUUID().toString())
            .build();

        return fspServiceRegistry.submitPayment(payment.getFspCode(), fspRequest);
    }

    private void updatePaymentFromFSPResponse(Payment payment, FSPPaymentResponse fspResponse, String updatedBy) {
        payment.setFspReferenceNumber(fspResponse.getFspReferenceNumber());
        payment.setUpdatedBy(updatedBy);

        if (fspResponse.isSuccessful()) {
            if (fspResponse.getStatus() == FSPPaymentResponse.FSPPaymentStatus.COMPLETED) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setCompletedDate(LocalDateTime.now());
            } else {
                payment.setStatus(Payment.PaymentStatus.PROCESSING);
            }
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(fspResponse.getErrorMessage());
        }

        paymentRepository.save(payment);

        // Log FSP response
        auditLogRepository.save(PaymentAuditLog.fspResponse(
            payment.getPaymentId(), fspResponse.toString(), fspResponse.getFspReferenceNumber()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatisticsByFsp() {
        log.debug("Getting payment statistics by FSP");

        try {
            List<Object[]> stats = paymentRepository.getPaymentStatisticsByFsp();
            Map<String, Object> result = new HashMap<>();

            for (Object[] stat : stats) {
                String fspCode = (String) stat[0];
                Long count = (Long) stat[1];
                BigDecimal amount = (BigDecimal) stat[2];

                Map<String, Object> fspStats = new HashMap<>();
                fspStats.put("count", count);
                fspStats.put("totalAmount", amount);
                result.put(fspCode, fspStats);
            }

            return result;

        } catch (Exception e) {
            log.error("Error getting payment statistics by FSP", e);
            throw new RuntimeException("Failed to get payment statistics by FSP: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyPaymentVolume(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting daily payment volume from {} to {}", startDate, endDate);

        try {
            List<Object[]> volumes = paymentRepository.getDailyPaymentVolume(startDate, endDate);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Object[] volume : volumes) {
                Map<String, Object> dailyVolume = new HashMap<>();
                dailyVolume.put("date", volume[0]);
                dailyVolume.put("count", volume[1]);
                dailyVolume.put("totalAmount", volume[2]);
                result.add(dailyVolume);
            }

            return result;

        } catch (Exception e) {
            log.error("Error getting daily payment volume", e);
            throw new RuntimeException("Failed to get daily payment volume: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> reconcilePayments(String fspCode, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Reconciling payments for FSP: {} from {} to {}", fspCode, startDate, endDate);

        try {
            // Get payments requiring reconciliation
            List<Payment> paymentsToReconcile = paymentRepository.findPaymentsRequiringReconciliation(
                LocalDateTime.now().minusHours(24)); // Payments older than 24 hours

            Map<String, Object> reconciliationResult = new HashMap<>();
            int reconciledCount = 0;
            int discrepancyCount = 0;
            List<Map<String, Object>> discrepancies = new ArrayList<>();

            for (Payment payment : paymentsToReconcile) {
                if (!fspCode.equals(payment.getFspCode())) {
                    continue;
                }

                try {
                    FSPStatusResponse fspStatus = fspServiceRegistry.checkPaymentStatus(
                        payment.getFspCode(), payment.getFspReferenceNumber());

                    if (fspStatus.isCompleted() && payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
                        // Discrepancy found - FSP says completed but our record shows otherwise
                        Map<String, Object> discrepancy = new HashMap<>();
                        discrepancy.put("paymentId", payment.getPaymentId());
                        discrepancy.put("internalStatus", payment.getStatus());
                        discrepancy.put("fspStatus", fspStatus.getStatus());
                        discrepancy.put("amount", payment.getAmount());
                        discrepancies.add(discrepancy);
                        discrepancyCount++;

                        // Auto-reconcile if configured
                        payment.setStatus(Payment.PaymentStatus.COMPLETED);
                        payment.setCompletedDate(LocalDateTime.now());
                        paymentRepository.save(payment);
                    }

                    reconciledCount++;

                } catch (Exception e) {
                    log.warn("Error reconciling payment: {}", payment.getPaymentId(), e);
                }
            }

            reconciliationResult.put("fspCode", fspCode);
            reconciliationResult.put("reconciledCount", reconciledCount);
            reconciliationResult.put("discrepancyCount", discrepancyCount);
            reconciliationResult.put("discrepancies", discrepancies);
            reconciliationResult.put("reconciliationDate", LocalDateTime.now());

            log.info("Reconciliation completed for FSP: {}, reconciled: {}, discrepancies: {}",
                    fspCode, reconciledCount, discrepancyCount);

            return reconciliationResult;

        } catch (Exception e) {
            log.error("Error reconciling payments for FSP: {}", fspCode, e);
            throw new RuntimeException("Failed to reconcile payments: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatusAndDateRange(Payment.PaymentStatus status,
                                                        LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting total amount for status: {} from {} to {}", status, startDate, endDate);

        try {
            BigDecimal total = paymentRepository.getTotalAmountByStatusAndDateRange(status, startDate, endDate);
            return total != null ? total : BigDecimal.ZERO;

        } catch (Exception e) {
            log.error("Error getting total amount by status and date range", e);
            throw new RuntimeException("Failed to get total amount: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countPaymentsByStatus(Payment.PaymentStatus status) {
        log.debug("Counting payments by status: {}", status);

        try {
            return paymentRepository.countByStatus(status);

        } catch (Exception e) {
            log.error("Error counting payments by status", e);
            throw new RuntimeException("Failed to count payments: " + e.getMessage(), e);
        }
    }
}
