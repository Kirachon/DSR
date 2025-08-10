package ph.gov.dsr.grievance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.grievance.dto.*;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.service.GrievanceCaseService;
import ph.gov.dsr.grievance.service.MultiChannelCaseManagementService;
import ph.gov.dsr.grievance.service.GrievanceAnalyticsService;
import ph.gov.dsr.grievance.service.EscalationWorkflowEngine;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Grievance Case Management
 * Handles all grievance-related operations including case submission, tracking, and resolution
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@RestController
@RequestMapping("/api/v1/grievances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Grievance Management", description = "Grievance case management operations")
public class GrievanceController {

    private final GrievanceCaseService grievanceCaseService;
    private final MultiChannelCaseManagementService caseManagementService;
    private final GrievanceAnalyticsService analyticsService;
    private final EscalationWorkflowEngine escalationEngine;

    @Operation(summary = "Get all cases with filters", 
               description = "Retrieves paginated list of grievance cases with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/cases")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Page<GrievanceResponse>> getCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submissionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean isUrgent) {
        
        log.info("Getting cases with filters - page: {}, size: {}, status: {}, category: {}", 
                page, size, status, category);
        
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Parse date parameters
            LocalDateTime startDateTime = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime endDateTime = endDate != null ? LocalDateTime.parse(endDate) : null;
            
            // Convert string parameters to enums
            GrievanceCase.CaseStatus statusEnum = status != null ? 
                GrievanceCase.CaseStatus.valueOf(status.toUpperCase()) : null;
            GrievanceCase.Priority priorityEnum = priority != null ? 
                GrievanceCase.Priority.valueOf(priority.toUpperCase()) : null;
            GrievanceCase.GrievanceCategory categoryEnum = category != null ? 
                GrievanceCase.GrievanceCategory.valueOf(category.toUpperCase()) : null;
            
            Page<GrievanceCase> cases = grievanceCaseService.getCasesByCriteria(
                statusEnum, priorityEnum, categoryEnum, assignedTo, 
                startDateTime, endDateTime, pageable);
            
            Page<GrievanceResponse> response = cases.map(GrievanceResponse::fromEntity);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving cases: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get case by ID", 
               description = "Retrieves a specific grievance case by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case found"),
        @ApiResponse(responseCode = "404", description = "Case not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/cases/{caseId}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN')")
    public ResponseEntity<GrievanceResponse> getCase(
            @Parameter(description = "Case ID") @PathVariable UUID caseId) {
        
        log.info("Getting case by ID: {}", caseId);
        
        try {
            Optional<GrievanceCase> caseOpt = grievanceCaseService.getCaseById(caseId);
            
            if (caseOpt.isPresent()) {
                GrievanceResponse response = GrievanceResponse.fromEntity(caseOpt.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error retrieving case {}: {}", caseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Create new case", 
               description = "Creates a new grievance case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Case created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/cases")
    @PreAuthorize("hasAnyRole('CITIZEN', 'DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN')")
    public ResponseEntity<GrievanceResponse> createCase(
            @Valid @RequestBody CaseSubmissionRequest request) {
        
        log.info("Creating new case for complainant: {}", request.getComplainantPsn());
        
        try {
            GrievanceCase createdCase = caseManagementService.submitCaseFromWebPortal(request);
            GrievanceResponse response = GrievanceResponse.fromEntity(createdCase);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            log.error("Error creating case: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update case status", 
               description = "Updates the status of a grievance case")
    @PostMapping("/cases/{caseId}/status")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN')")
    public ResponseEntity<GrievanceResponse> updateCaseStatus(
            @Parameter(description = "Case ID") @PathVariable UUID caseId,
            @Valid @RequestBody UpdateCaseStatusRequest request) {
        
        log.info("Updating status for case {}: {}", caseId, request.getStatus());
        
        try {
            GrievanceCase.CaseStatus newStatus = GrievanceCase.CaseStatus.valueOf(request.getStatus().toUpperCase());
            GrievanceCase updatedCase = grievanceCaseService.updateCaseStatus(caseId, newStatus, 
                request.getReason(), request.getUpdatedBy());
            
            GrievanceResponse response = GrievanceResponse.fromEntity(updatedCase);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", request.getStatus());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating case status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Assign case to staff", 
               description = "Assigns a grievance case to a staff member")
    @PostMapping("/cases/{caseId}/assign")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<GrievanceResponse> assignCase(
            @Parameter(description = "Case ID") @PathVariable UUID caseId,
            @Valid @RequestBody AssignCaseRequest request) {
        
        log.info("Assigning case {} to {}", caseId, request.getAssignedTo());
        
        try {
            GrievanceCase assignedCase = grievanceCaseService.assignCase(caseId, 
                request.getAssignedTo(), request.getAssignedBy());
            
            GrievanceResponse response = GrievanceResponse.fromEntity(assignedCase);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error assigning case: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Add comment to case",
               description = "Adds a comment or note to a grievance case")
    @PostMapping("/cases/{caseId}/comments")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN')")
    public ResponseEntity<GrievanceResponse> addCaseComment(
            @Parameter(description = "Case ID") @PathVariable UUID caseId,
            @Valid @RequestBody AddCaseCommentRequest request) {

        log.info("Adding comment to case {}", caseId);

        try {
            GrievanceCase updatedCase = grievanceCaseService.addComment(caseId,
                request.getComment(), request.getAuthor(), request.getIsInternal());

            GrievanceResponse response = GrievanceResponse.fromEntity(updatedCase);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error adding comment to case: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get my cases",
               description = "Retrieves cases assigned to the current user")
    @GetMapping("/my-cases")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER')")
    public ResponseEntity<Page<GrievanceResponse>> getMyCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submissionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignedTo) {

        log.info("Getting my cases for user: {}", assignedTo);

        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<GrievanceCase> cases = grievanceCaseService.getCasesAssignedTo(assignedTo, pageable);
            Page<GrievanceResponse> response = cases.map(GrievanceResponse::fromEntity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving my cases: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get case statistics",
               description = "Retrieves statistical information about grievance cases")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<CaseStatisticsResponse> getCaseStatistics() {

        log.info("Getting case statistics");

        try {
            Object[] stats = grievanceCaseService.getCaseStatistics();

            // Build statistics response from the array
            CaseStatisticsResponse response = CaseStatisticsResponse.builder()
                .totalCases((Long) stats[0])
                .openCases((Long) stats[1])
                .resolvedCases((Long) stats[2])
                .overdueCase((Long) stats[3])
                .urgentCases((Long) stats[4])
                .escalatedCases((Long) stats[5])
                .averageResolutionTime((Double) stats[6])
                .satisfactionScore((Double) stats[7])
                .lastUpdated(LocalDateTime.now())
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving case statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Search cases",
               description = "Searches cases by text content")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Page<GrievanceResponse>> searchCases(
            @RequestParam String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching cases with text: {}", searchText);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<GrievanceCase> cases = grievanceCaseService.searchCases(searchText, pageable);
            Page<GrievanceResponse> response = cases.map(GrievanceResponse::fromEntity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching cases: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Resolve case",
               description = "Marks a case as resolved with resolution details")
    @PostMapping("/cases/{caseId}/resolve")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN')")
    public ResponseEntity<GrievanceResponse> resolveCase(
            @Parameter(description = "Case ID") @PathVariable UUID caseId,
            @RequestParam String resolutionSummary,
            @RequestParam String resolutionActions,
            @RequestParam String resolvedBy) {

        log.info("Resolving case {}", caseId);

        try {
            GrievanceCase resolvedCase = grievanceCaseService.resolveCase(caseId,
                resolutionSummary, resolutionActions, resolvedBy);

            GrievanceResponse response = GrievanceResponse.fromEntity(resolvedCase);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error resolving case: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get case types",
               description = "Retrieves available grievance case types/categories")
    @GetMapping("/types")
    public ResponseEntity<List<String>> getCaseTypes() {

        log.info("Getting case types");

        try {
            List<String> types = List.of(GrievanceCase.GrievanceCategory.values())
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());

            return ResponseEntity.ok(types);

        } catch (Exception e) {
            log.error("Error retrieving case types: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get performance dashboard",
               description = "Retrieves comprehensive performance analytics dashboard")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/analytics/dashboard")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getPerformanceDashboard(
            @Parameter(description = "Time range (7d, 30d, 90d, 365d)")
            @RequestParam(defaultValue = "30d") String timeRange) {

        log.info("Getting performance dashboard for timeRange: {}", timeRange);

        try {
            Map<String, Object> dashboard = analyticsService.generatePerformanceDashboard(timeRange);
            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            log.error("Error retrieving performance dashboard: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get real-time analytics",
               description = "Retrieves real-time analytics summary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Real-time analytics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/analytics/realtime")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getRealTimeAnalytics() {

        log.info("Getting real-time analytics");

        try {
            Map<String, Object> analytics = analyticsService.getRealTimeAnalytics();
            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            log.error("Error retrieving real-time analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get escalation analytics",
               description = "Retrieves escalation analytics and trends")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Escalation analytics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/analytics/escalations")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getEscalationAnalytics() {

        log.info("Getting escalation analytics");

        try {
            Map<String, Object> analytics = escalationEngine.getEscalationAnalytics();
            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            log.error("Error retrieving escalation analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Escalate case",
               description = "Manually escalate a case with specified reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case escalated successfully"),
        @ApiResponse(responseCode = "404", description = "Case not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{caseId}/escalate")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> escalateCase(
            @Parameter(description = "Case ID") @PathVariable UUID caseId,
            @Parameter(description = "Escalation reason") @RequestParam String reason,
            @Parameter(description = "Escalation trigger type") @RequestParam(defaultValue = "CUSTOMER_COMPLAINT") String triggerType) {

        log.info("Escalating case {} with reason: {}", caseId, reason);

        try {
            EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
                caseId, triggerType, reason, "MANUAL");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("escalationResult", result);
            response.put("message", "Case escalated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error escalating case {}: {}", caseId, e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
