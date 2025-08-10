package ph.gov.dsr.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.dto.PaymentResponse;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for payment operations
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing individual payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Create a new payment", description = "Creates a new payment for a beneficiary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment request"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @Parameter(description = "User creating the payment", required = true)
            @RequestParam String createdBy) {
        
        log.info("Creating payment for household: {}, amount: {}", 
                request.getHouseholdId(), request.getAmount());
        
        try {
            PaymentResponse response = paymentService.createPayment(request, createdBy);
            
            log.info("Payment created successfully with ID: {}", response.getPaymentId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating payment for household: {}", request.getHouseholdId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN', 'BENEFICIARY')")
    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId) {
        
        log.debug("Getting payment by ID: {}", paymentId);
        
        try {
            PaymentResponse response = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Payment not found: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reference/{referenceNumber}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payment by reference number", description = "Retrieves a payment by its internal reference number")
    public ResponseEntity<PaymentResponse> getPaymentByReferenceNumber(
            @Parameter(description = "Internal reference number", required = true)
            @PathVariable String referenceNumber) {
        
        log.debug("Getting payment by reference: {}", referenceNumber);
        
        try {
            PaymentResponse response = paymentService.getPaymentByReferenceNumber(referenceNumber);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Payment not found with reference: {}", referenceNumber);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting payment by reference: {}", referenceNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN', 'BENEFICIARY')")
    @Operation(summary = "Get payments by household ID", description = "Retrieves all payments for a specific household")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByHouseholdId(
            @Parameter(description = "Household ID", required = true)
            @PathVariable UUID householdId,
            Pageable pageable) {
        
        log.debug("Getting payments for household: {}", householdId);
        
        try {
            Page<PaymentResponse> payments = paymentService.getPaymentsByHouseholdId(householdId, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error getting payments for household: {}", householdId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/program/{programId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payments by program ID", description = "Retrieves all payments for a specific program")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByProgramId(
            @Parameter(description = "Program ID", required = true)
            @PathVariable UUID programId,
            Pageable pageable) {
        
        log.debug("Getting payments for program: {}", programId);
        
        try {
            Page<PaymentResponse> payments = paymentService.getPaymentsByProgramId(programId, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error getting payments for program: {}", programId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payments by status", description = "Retrieves all payments with a specific status")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment status", required = true)
            @PathVariable Payment.PaymentStatus status,
            Pageable pageable) {
        
        log.debug("Getting payments with status: {}", status);
        
        try {
            Page<PaymentResponse> payments = paymentService.getPaymentsByStatus(status, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error getting payments by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Search payments", description = "Search payments by multiple criteria")
    public ResponseEntity<Page<PaymentResponse>> searchPayments(
            @Parameter(description = "Household ID") @RequestParam(required = false) UUID householdId,
            @Parameter(description = "Program ID") @RequestParam(required = false) UUID programId,
            @Parameter(description = "Payment status") @RequestParam(required = false) Payment.PaymentStatus status,
            @Parameter(description = "FSP code") @RequestParam(required = false) String fspCode,
            @Parameter(description = "Start date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        log.debug("Searching payments with criteria");
        
        try {
            Page<PaymentResponse> payments = paymentService.searchPayments(
                householdId, programId, status, fspCode, startDate, endDate, pageable);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error searching payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Process payment", description = "Submit payment to FSP for processing")
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "User processing the payment", required = true)
            @RequestParam String processedBy) {
        
        log.info("Processing payment: {}", paymentId);
        
        try {
            PaymentResponse response = paymentService.processPayment(paymentId, processedBy);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Cancel payment", description = "Cancel a pending or processing payment")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "Cancellation reason", required = true)
            @RequestParam String reason,
            @Parameter(description = "User cancelling the payment", required = true)
            @RequestParam String cancelledBy) {
        
        log.info("Cancelling payment: {} with reason: {}", paymentId, reason);
        
        try {
            PaymentResponse response = paymentService.cancelPayment(paymentId, reason, cancelledBy);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{paymentId}/retry")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Retry payment", description = "Retry a failed payment")
    public ResponseEntity<PaymentResponse> retryPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "User retrying the payment", required = true)
            @RequestParam String retriedBy) {
        
        log.info("Retrying payment: {}", paymentId);
        
        try {
            PaymentResponse response = paymentService.retryPayment(paymentId, retriedBy);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrying payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{paymentId}/status")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Update payment status", description = "Update the status of a payment")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "New payment status", required = true)
            @RequestParam Payment.PaymentStatus status,
            @Parameter(description = "Status change reason")
            @RequestParam(required = false) String reason,
            @Parameter(description = "User updating the status", required = true)
            @RequestParam String updatedBy) {

        log.info("Updating payment status: {} to {}", paymentId, status);

        try {
            PaymentResponse response = paymentService.updatePaymentStatus(paymentId, status, reason, updatedBy);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating payment status: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{paymentId}/check-status")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Check payment status with FSP", description = "Check the current status of a payment with the FSP")
    public ResponseEntity<PaymentResponse> checkPaymentStatusWithFSP(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId) {

        log.info("Checking payment status with FSP: {}", paymentId);

        try {
            PaymentResponse response = paymentService.checkPaymentStatusWithFSP(paymentId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking payment status with FSP: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payment statistics", description = "Get overall payment statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics() {

        log.debug("Getting payment statistics");

        try {
            Map<String, Object> statistics = paymentService.getPaymentStatistics();
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error getting payment statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics/fsp")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payment statistics by FSP", description = "Get payment statistics grouped by FSP")
    public ResponseEntity<Map<String, Object>> getPaymentStatisticsByFsp() {

        log.debug("Getting payment statistics by FSP");

        try {
            Map<String, Object> statistics = paymentService.getPaymentStatisticsByFsp();
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error getting payment statistics by FSP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/volume/daily")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get daily payment volume", description = "Get daily payment volume for a date range")
    public ResponseEntity<List<Map<String, Object>>> getDailyPaymentVolume(
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("Getting daily payment volume from {} to {}", startDate, endDate);

        try {
            List<Map<String, Object>> volume = paymentService.getDailyPaymentVolume(startDate, endDate);
            return ResponseEntity.ok(volume);

        } catch (Exception e) {
            log.error("Error getting daily payment volume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reconcile")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Reconcile payments", description = "Reconcile payments with FSP for a specific date range")
    public ResponseEntity<Map<String, Object>> reconcilePayments(
            @Parameter(description = "FSP code", required = true)
            @RequestParam String fspCode,
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Reconciling payments for FSP: {} from {} to {}", fspCode, startDate, endDate);

        try {
            Map<String, Object> reconciliation = paymentService.reconcilePayments(fspCode, startDate, endDate);
            return ResponseEntity.ok(reconciliation);

        } catch (Exception e) {
            log.error("Error reconciling payments for FSP: {}", fspCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/total-amount")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get total amount by status and date range", description = "Get total payment amount for a specific status and date range")
    public ResponseEntity<BigDecimal> getTotalAmountByStatusAndDateRange(
            @Parameter(description = "Payment status", required = true)
            @RequestParam Payment.PaymentStatus status,
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("Getting total amount for status: {} from {} to {}", status, startDate, endDate);

        try {
            BigDecimal totalAmount = paymentService.getTotalAmountByStatusAndDateRange(status, startDate, endDate);
            return ResponseEntity.ok(totalAmount);

        } catch (Exception e) {
            log.error("Error getting total amount by status and date range", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Count payments by status", description = "Get the count of payments for a specific status")
    public ResponseEntity<Long> countPaymentsByStatus(
            @Parameter(description = "Payment status", required = true)
            @RequestParam Payment.PaymentStatus status) {

        log.debug("Counting payments by status: {}", status);

        try {
            long count = paymentService.countPaymentsByStatus(status);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            log.error("Error counting payments by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
