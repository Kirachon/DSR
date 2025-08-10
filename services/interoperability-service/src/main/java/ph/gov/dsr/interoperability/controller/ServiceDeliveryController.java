package ph.gov.dsr.interoperability.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.interoperability.entity.ServiceDeliveryRecord;
import ph.gov.dsr.interoperability.service.ServiceDeliveryService;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for Service Delivery operations
 * Handles service delivery tracking and management
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@RestController
@RequestMapping("/api/v1/interoperability/service-delivery")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Delivery", description = "Service delivery tracking and management operations")
public class ServiceDeliveryController {

    private final ServiceDeliveryService deliveryService;

    @Operation(summary = "Record service delivery", 
               description = "Record a new service delivery transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Service delivery recorded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/record")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ServiceDeliveryRecord> recordServiceDelivery(
            @Valid @RequestBody ServiceDeliveryRecord deliveryRecord) {
        
        log.info("Recording service delivery for beneficiary: {} service: {}",
                deliveryRecord.getBeneficiaryPsn(), deliveryRecord.getServiceType());

        ServiceDeliveryRecord savedRecord = deliveryService.recordServiceDelivery(deliveryRecord);
        return ResponseEntity.status(201).body(savedRecord);
    }

    @Operation(summary = "Get service delivery by transaction ID", 
               description = "Retrieve service delivery record by transaction ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service delivery record retrieved"),
        @ApiResponse(responseCode = "404", description = "Service delivery record not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ServiceDeliveryRecord> getServiceDeliveryByTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        
        log.info("Retrieving service delivery by transaction ID: {}", transactionId);
        Optional<ServiceDeliveryRecord> record = deliveryService.getByTransactionId(transactionId);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get service delivery history by PSN", 
               description = "Retrieve service delivery history for a beneficiary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service delivery history retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/beneficiary/{psn}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ServiceDeliveryRecord>> getServiceDeliveryHistory(
            @Parameter(description = "Beneficiary PSN") @PathVariable String psn) {
        
        log.info("Retrieving service delivery history for PSN: {}", psn);
        List<ServiceDeliveryRecord> records = deliveryService.getDeliveryHistory(psn);
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get service deliveries by household", 
               description = "Retrieve service deliveries for a household")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service deliveries retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ServiceDeliveryRecord>> getServiceDeliveriesByHousehold(
            @Parameter(description = "Household ID") @PathVariable UUID householdId) {
        
        log.info("Retrieving service deliveries for household: {}", householdId);
        List<ServiceDeliveryRecord> records = deliveryService.getDeliveriesByHousehold(householdId);
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get service deliveries by program", 
               description = "Retrieve service deliveries for a specific program")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service deliveries retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/program/{programCode}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ServiceDeliveryRecord>> getServiceDeliveriesByProgram(
            @Parameter(description = "Program code") @PathVariable String programCode) {
        
        log.info("Retrieving service deliveries for program: {}", programCode);
        List<ServiceDeliveryRecord> records = deliveryService.getDeliveriesByProgram(programCode);
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Update delivery status", 
               description = "Update the status of a service delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery status updated"),
        @ApiResponse(responseCode = "404", description = "Service delivery record not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ServiceDeliveryRecord> updateDeliveryStatus(
            @Parameter(description = "Delivery record ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> statusUpdate) {
        
        log.info("Updating delivery status for record: {}", id);

        try {
            ServiceDeliveryRecord updatedRecord;

            if (statusUpdate.containsKey("confirmedBy")) {
                String confirmedBy = (String) statusUpdate.get("confirmedBy");
                String verificationMethod = (String) statusUpdate.get("verificationMethod");
                updatedRecord = deliveryService.markAsDelivered(id, confirmedBy, verificationMethod);
            } else if (statusUpdate.containsKey("failureReason")) {
                String failureReason = (String) statusUpdate.get("failureReason");
                updatedRecord = deliveryService.markAsFailed(id, failureReason);
            } else if (statusUpdate.containsKey("status")) {
                String status = (String) statusUpdate.get("status");
                String notes = (String) statusUpdate.get("notes");
                updatedRecord = deliveryService.updateDeliveryStatus(id,
                    ServiceDeliveryRecord.DeliveryStatus.valueOf(status), notes);
            } else {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(updatedRecord);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get pending deliveries", 
               description = "Retrieve all pending service deliveries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending deliveries retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ServiceDeliveryRecord>> getPendingDeliveries() {
        log.info("Retrieving pending deliveries");
        List<ServiceDeliveryRecord> records = deliveryService.getPendingDeliveries();
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get failed deliveries", 
               description = "Retrieve all failed service deliveries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Failed deliveries retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/failed")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ServiceDeliveryRecord>> getFailedDeliveries() {
        log.info("Retrieving failed deliveries");
        List<ServiceDeliveryRecord> records = deliveryService.getFailedDeliveries();
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get overdue deliveries", 
               description = "Retrieve overdue service deliveries (pending for more than 24 hours)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue deliveries retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ServiceDeliveryRecord>> getOverdueDeliveries() {
        log.info("Retrieving overdue deliveries");
        List<ServiceDeliveryRecord> records = deliveryService.getOverdueDeliveries();
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get delivery statistics", 
               description = "Get service delivery statistics and metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDeliveryStatistics() {
        log.info("Retrieving delivery statistics");
        
        Map<String, Object> statistics = deliveryService.getDeliveryStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Search service deliveries", 
               description = "Search service deliveries with filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Page<ServiceDeliveryRecord>> searchServiceDeliveries(
            @RequestParam(required = false) String beneficiaryPsn,
            @RequestParam(required = false) String programCode,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String deliveryStatus,
            @RequestParam(required = false) String deliveryMethod,
            Pageable pageable) {
        
        log.info("Searching service deliveries with filters");
        
        // This would be implemented with proper search functionality
        // For now, using the service search method
        Page<ServiceDeliveryRecord> results = deliveryService.searchDeliveries(
            beneficiaryPsn, programCode, serviceType,
            deliveryStatus != null ? ServiceDeliveryRecord.DeliveryStatus.valueOf(deliveryStatus) : null,
            deliveryMethod, null, null, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get advanced delivery tracking",
               description = "Get comprehensive tracking information for a specific delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tracking information retrieved"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/tracking/{transactionId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdvancedDeliveryTracking(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {

        log.info("Getting advanced tracking for transaction: {}", transactionId);

        try {
            Map<String, Object> tracking = deliveryService.getAdvancedDeliveryTracking(transactionId);

            if (tracking.containsKey("error")) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(tracking);

        } catch (Exception e) {
            log.error("Error getting advanced tracking for transaction: {}", transactionId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get tracking information");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Generate delivery analytics",
               description = "Generate comprehensive analytics for deliveries within date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analytics generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> generateDeliveryAnalytics(
            @Parameter(description = "Start date (ISO format)") @RequestParam String fromDate,
            @Parameter(description = "End date (ISO format)") @RequestParam String toDate) {

        log.info("Generating delivery analytics from {} to {}", fromDate, toDate);

        try {
            LocalDateTime from = LocalDateTime.parse(fromDate);
            LocalDateTime to = LocalDateTime.parse(toDate);

            if (from.isAfter(to)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid date range");
                errorResponse.put("message", "Start date must be before end date");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> analytics = deliveryService.generateDeliveryAnalytics(from, to);
            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            log.error("Error generating delivery analytics", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate analytics");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get real-time monitoring data",
               description = "Get real-time delivery monitoring dashboard data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Monitoring data retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/monitoring/realtime")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getRealtimeMonitoringData() {

        log.info("Getting real-time delivery monitoring data");

        try {
            Map<String, Object> monitoringData = deliveryService.getRealtimeMonitoringData();
            return ResponseEntity.ok(monitoringData);

        } catch (Exception e) {
            log.error("Error getting real-time monitoring data", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get monitoring data");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
