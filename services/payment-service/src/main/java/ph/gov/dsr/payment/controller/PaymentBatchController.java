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
import ph.gov.dsr.payment.dto.PaymentBatchRequest;
import ph.gov.dsr.payment.dto.PaymentBatchResponse;
import ph.gov.dsr.payment.entity.PaymentBatch;
import ph.gov.dsr.payment.service.PaymentBatchService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for payment batch operations
 */
@RestController
@RequestMapping("/api/v1/payment-batches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Batch Management", description = "APIs for managing payment batches")
public class PaymentBatchController {

    private final PaymentBatchService paymentBatchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Create a new payment batch", description = "Creates a new payment batch with multiple payments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment batch created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid batch request"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentBatchResponse> createPaymentBatch(
            @Valid @RequestBody PaymentBatchRequest request,
            @Parameter(description = "User creating the batch", required = true)
            @RequestParam String createdBy) {
        
        log.info("Creating payment batch for program: {}, payments: {}", 
                request.getProgramId(), request.getPaymentRequests().size());
        
        try {
            PaymentBatchResponse response = paymentBatchService.createPaymentBatch(request, createdBy);
            
            log.info("Payment batch created successfully with ID: {}", response.getBatchId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating payment batch for program: {}", request.getProgramId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{batchId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payment batch by ID", description = "Retrieves a payment batch by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment batch found"),
        @ApiResponse(responseCode = "404", description = "Payment batch not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PaymentBatchResponse> getPaymentBatchById(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId) {
        
        log.debug("Getting payment batch by ID: {}", batchId);
        
        try {
            PaymentBatchResponse response = paymentBatchService.getPaymentBatchById(batchId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Payment batch not found: {}", batchId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting payment batch: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/number/{batchNumber}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payment batch by number", description = "Retrieves a payment batch by its batch number")
    public ResponseEntity<PaymentBatchResponse> getPaymentBatchByNumber(
            @Parameter(description = "Batch number", required = true)
            @PathVariable String batchNumber) {
        
        log.debug("Getting payment batch by number: {}", batchNumber);
        
        try {
            PaymentBatchResponse response = paymentBatchService.getPaymentBatchByNumber(batchNumber);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Payment batch not found with number: {}", batchNumber);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting payment batch by number: {}", batchNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/program/{programId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payment batches by program ID", description = "Retrieves all payment batches for a specific program")
    public ResponseEntity<Page<PaymentBatchResponse>> getPaymentBatchesByProgramId(
            @Parameter(description = "Program ID", required = true)
            @PathVariable UUID programId,
            Pageable pageable) {
        
        log.debug("Getting payment batches for program: {}", programId);
        
        try {
            Page<PaymentBatchResponse> batches = paymentBatchService.getPaymentBatchesByProgramId(programId, pageable);
            return ResponseEntity.ok(batches);
            
        } catch (Exception e) {
            log.error("Error getting payment batches for program: {}", programId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get payment batches by status", description = "Retrieves all payment batches with a specific status")
    public ResponseEntity<Page<PaymentBatchResponse>> getPaymentBatchesByStatus(
            @Parameter(description = "Batch status", required = true)
            @PathVariable PaymentBatch.BatchStatus status,
            Pageable pageable) {
        
        log.debug("Getting payment batches with status: {}", status);
        
        try {
            Page<PaymentBatchResponse> batches = paymentBatchService.getPaymentBatchesByStatus(status, pageable);
            return ResponseEntity.ok(batches);
            
        } catch (Exception e) {
            log.error("Error getting payment batches by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Search payment batches", description = "Search payment batches by multiple criteria")
    public ResponseEntity<Page<PaymentBatchResponse>> searchPaymentBatches(
            @Parameter(description = "Program ID") @RequestParam(required = false) UUID programId,
            @Parameter(description = "Batch status") @RequestParam(required = false) PaymentBatch.BatchStatus status,
            @Parameter(description = "Start date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        log.debug("Searching payment batches with criteria");
        
        try {
            Page<PaymentBatchResponse> batches = paymentBatchService.searchPaymentBatches(
                programId, status, startDate, endDate, pageable);
            return ResponseEntity.ok(batches);
            
        } catch (Exception e) {
            log.error("Error searching payment batches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{batchId}/start")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Start batch processing", description = "Start processing a payment batch")
    public ResponseEntity<PaymentBatchResponse> startBatchProcessing(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId,
            @Parameter(description = "User starting the batch", required = true)
            @RequestParam String startedBy) {
        
        log.info("Starting batch processing: {}", batchId);
        
        try {
            PaymentBatchResponse response = paymentBatchService.startBatchProcessing(batchId, startedBy);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error starting batch processing: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{batchId}/pause")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Pause batch processing", description = "Pause processing of a payment batch")
    public ResponseEntity<PaymentBatchResponse> pauseBatchProcessing(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId,
            @Parameter(description = "User pausing the batch", required = true)
            @RequestParam String pausedBy) {
        
        log.info("Pausing batch processing: {}", batchId);
        
        try {
            PaymentBatchResponse response = paymentBatchService.pauseBatchProcessing(batchId, pausedBy);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error pausing batch processing: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{batchId}/resume")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Resume batch processing", description = "Resume processing of a paused payment batch")
    public ResponseEntity<PaymentBatchResponse> resumeBatchProcessing(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId,
            @Parameter(description = "User resuming the batch", required = true)
            @RequestParam String resumedBy) {
        
        log.info("Resuming batch processing: {}", batchId);
        
        try {
            PaymentBatchResponse response = paymentBatchService.resumeBatchProcessing(batchId, resumedBy);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error resuming batch processing: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{batchId}/cancel")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Cancel payment batch", description = "Cancel a payment batch and all its pending payments")
    public ResponseEntity<PaymentBatchResponse> cancelPaymentBatch(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId,
            @Parameter(description = "Cancellation reason", required = true)
            @RequestParam String reason,
            @Parameter(description = "User cancelling the batch", required = true)
            @RequestParam String cancelledBy) {
        
        log.info("Cancelling payment batch: {} with reason: {}", batchId, reason);
        
        try {
            PaymentBatchResponse response = paymentBatchService.cancelPaymentBatch(batchId, reason, cancelledBy);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling payment batch: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{batchId}/status")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Update batch status", description = "Update the status of a payment batch")
    public ResponseEntity<PaymentBatchResponse> updateBatchStatus(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId,
            @Parameter(description = "New batch status", required = true)
            @RequestParam PaymentBatch.BatchStatus status,
            @Parameter(description = "Status change reason")
            @RequestParam(required = false) String reason,
            @Parameter(description = "User updating the status", required = true)
            @RequestParam String updatedBy) {

        log.info("Updating batch status: {} to {}", batchId, status);

        try {
            PaymentBatchResponse response = paymentBatchService.updateBatchStatus(batchId, status, reason, updatedBy);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating batch status: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{batchId}/progress")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Monitor batch progress", description = "Get the current progress of a payment batch")
    public ResponseEntity<PaymentBatchResponse> monitorBatchProgress(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId) {

        log.debug("Monitoring batch progress: {}", batchId);

        try {
            PaymentBatchResponse response = paymentBatchService.monitorBatchProgress(batchId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error monitoring batch progress: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get batch statistics", description = "Get overall batch statistics")
    public ResponseEntity<Map<String, Object>> getBatchStatistics() {

        log.debug("Getting batch statistics");

        try {
            Map<String, Object> statistics = paymentBatchService.getBatchStatistics();
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error getting batch statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics/program")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get batch statistics by program", description = "Get batch statistics grouped by program")
    public ResponseEntity<Map<String, Object>> getBatchStatisticsByProgram() {

        log.debug("Getting batch statistics by program");

        try {
            Map<String, Object> statistics = paymentBatchService.getBatchStatisticsByProgram();
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error getting batch statistics by program", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get recent batches", description = "Get recent payment batches for dashboard")
    public ResponseEntity<Page<PaymentBatchResponse>> getRecentBatches(Pageable pageable) {

        log.debug("Getting recent batches");

        try {
            Page<PaymentBatchResponse> batches = paymentBatchService.getRecentBatches(pageable);
            return ResponseEntity.ok(batches);

        } catch (Exception e) {
            log.error("Error getting recent batches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{batchId}/completion-estimate")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get batch completion estimate", description = "Get estimated completion time for a batch")
    public ResponseEntity<Map<String, Object>> getBatchCompletionEstimate(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId) {

        log.debug("Getting batch completion estimate: {}", batchId);

        try {
            Map<String, Object> estimate = paymentBatchService.getBatchCompletionEstimate(batchId);
            return ResponseEntity.ok(estimate);

        } catch (Exception e) {
            log.error("Error getting batch completion estimate: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{batchId}/retry-failed")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Retry failed payments in batch", description = "Retry all failed payments in a batch")
    public ResponseEntity<PaymentBatchResponse> retryFailedPaymentsInBatch(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId,
            @Parameter(description = "User retrying the payments", required = true)
            @RequestParam String retriedBy) {

        log.info("Retrying failed payments in batch: {}", batchId);

        try {
            PaymentBatchResponse response = paymentBatchService.retryFailedPaymentsInBatch(batchId, retriedBy);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrying failed payments in batch: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{batchId}/report")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    @Operation(summary = "Generate batch report", description = "Generate a detailed report for a payment batch")
    public ResponseEntity<Map<String, Object>> generateBatchReport(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable UUID batchId) {

        log.info("Generating batch report: {}", batchId);

        try {
            Map<String, Object> report = paymentBatchService.generateBatchReport(batchId);
            return ResponseEntity.ok(report);

        } catch (Exception e) {
            log.error("Error generating batch report: {}", batchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/process-scheduled")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Process scheduled batches", description = "Process all scheduled payment batches (system operation)")
    public ResponseEntity<List<PaymentBatchResponse>> processScheduledBatches() {

        log.info("Processing scheduled batches");

        try {
            List<PaymentBatchResponse> processedBatches = paymentBatchService.processScheduledBatches();
            return ResponseEntity.ok(processedBatches);

        } catch (Exception e) {
            log.error("Error processing scheduled batches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
