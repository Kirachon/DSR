package ph.gov.dsr.eligibility.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.service.EligibilityAssessmentService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for eligibility assessment operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/api/v1/eligibility")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Eligibility Assessment", description = "APIs for eligibility assessment and management")
@SecurityRequirement(name = "bearerAuth")
public class EligibilityController {

    private final EligibilityAssessmentService eligibilityAssessmentService;

    @Operation(summary = "Assess eligibility for a program", 
               description = "Evaluate household eligibility for a specific social protection program")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eligibility assessment completed successfully",
                    content = @Content(schema = @Schema(implementation = EligibilityResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/assess")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<EligibilityResponse> assessEligibility(
            @Valid @RequestBody EligibilityRequest request) {
        
        log.info("Assessing eligibility for PSN: {} and program: {}", 
                request.getPsn(), request.getProgramCode());
        
        try {
            EligibilityResponse response = eligibilityAssessmentService.assessEligibility(request);
            
            log.info("Eligibility assessment completed for PSN: {}, Status: {}", 
                    request.getPsn(), response.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error assessing eligibility for PSN: {}", request.getPsn(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Assess eligibility for multiple programs", 
               description = "Evaluate household eligibility for multiple social protection programs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Multiple eligibility assessments completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/assess-multiple")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, EligibilityResponse>> assessMultiplePrograms(
            @Parameter(description = "Philippine Statistical Number", required = true)
            @RequestParam String psn,
            @Parameter(description = "List of program codes to assess", required = true)
            @RequestParam List<String> programCodes,
            @Parameter(description = "Force reassessment even if recent assessment exists")
            @RequestParam(defaultValue = "false") boolean forceReassessment) {
        
        log.info("Assessing multiple programs for PSN: {}, Programs: {}", psn, programCodes);
        
        try {
            Map<String, EligibilityResponse> responses = eligibilityAssessmentService
                    .assessMultiplePrograms(psn, programCodes, forceReassessment);
            
            log.info("Multiple eligibility assessments completed for PSN: {}", psn);
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error assessing multiple programs for PSN: {}", psn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get eligibility history", 
               description = "Retrieve historical eligibility assessments for a beneficiary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eligibility history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "No eligibility history found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/history/{psn}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<EligibilityResponse>> getEligibilityHistory(
            @Parameter(description = "Philippine Statistical Number", required = true)
            @PathVariable String psn,
            @Parameter(description = "Program code (optional, null for all programs)")
            @RequestParam(required = false) String programCode) {
        
        log.info("Getting eligibility history for PSN: {}, Program: {}", psn, programCode);
        
        try {
            List<EligibilityResponse> history = eligibilityAssessmentService
                    .getEligibilityHistory(psn, programCode);
            
            if (history.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            log.info("Retrieved {} eligibility records for PSN: {}", history.size(), psn);
            
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("Error getting eligibility history for PSN: {}", psn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Check assessment validity", 
               description = "Check if an eligibility assessment is still valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessment validity checked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/validity/{psn}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkAssessmentValidity(
            @Parameter(description = "Philippine Statistical Number", required = true)
            @PathVariable String psn,
            @Parameter(description = "Program code", required = true)
            @RequestParam String programCode) {
        
        log.info("Checking assessment validity for PSN: {}, Program: {}", psn, programCode);
        
        try {
            boolean isValid = eligibilityAssessmentService.isAssessmentValid(psn, programCode);
            
            Map<String, Boolean> result = Map.of("isValid", isValid);
            
            log.info("Assessment validity for PSN: {} is {}", psn, isValid);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error checking assessment validity for PSN: {}", psn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Invalidate eligibility assessment", 
               description = "Invalidate an eligibility assessment to force reassessment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assessment invalidated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/invalidate/{psn}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> invalidateAssessment(
            @Parameter(description = "Philippine Statistical Number", required = true)
            @PathVariable String psn,
            @Parameter(description = "Program code", required = true)
            @RequestParam String programCode,
            @Parameter(description = "Reason for invalidation", required = true)
            @RequestParam String reason) {
        
        log.info("Invalidating assessment for PSN: {}, Program: {}, Reason: {}", 
                psn, programCode, reason);
        
        try {
            eligibilityAssessmentService.invalidateAssessment(psn, programCode, reason);
            
            Map<String, String> result = Map.of(
                "message", "Assessment invalidated successfully",
                "psn", psn,
                "programCode", programCode,
                "reason", reason
            );
            
            log.info("Assessment invalidated for PSN: {}", psn);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error invalidating assessment for PSN: {}", psn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get eligibility statistics", 
               description = "Retrieve eligibility statistics for a program")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics/{programCode}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getEligibilityStatistics(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode) {
        
        log.info("Getting eligibility statistics for program: {}", programCode);
        
        try {
            Map<String, Object> statistics = eligibilityAssessmentService
                    .getEligibilityStatistics(programCode);
            
            log.info("Retrieved eligibility statistics for program: {}", programCode);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Error getting eligibility statistics for program: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Batch assess eligibility", 
               description = "Assess eligibility for multiple households in batch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Batch assessment completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/batch-assess")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<EligibilityResponse>> batchAssessEligibility(
            @Valid @RequestBody List<EligibilityRequest> requests) {
        
        log.info("Batch assessing eligibility for {} requests", requests.size());
        
        try {
            List<EligibilityResponse> responses = eligibilityAssessmentService
                    .batchAssessEligibility(requests);
            
            log.info("Batch eligibility assessment completed for {} requests", requests.size());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error in batch eligibility assessment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update eligibility status",
               description = "Manually update eligibility status (for overrides)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eligibility status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/status/{psn}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<EligibilityResponse> updateEligibilityStatus(
            @Parameter(description = "Philippine Statistical Number", required = true)
            @PathVariable String psn,
            @Parameter(description = "Program code", required = true)
            @RequestParam String programCode,
            @Parameter(description = "New eligibility status", required = true)
            @RequestParam EligibilityResponse.EligibilityStatus status,
            @Parameter(description = "Reason for status change", required = true)
            @RequestParam String reason,
            @Parameter(description = "User making the change", required = true)
            @RequestParam String updatedBy) {

        log.info("Updating eligibility status for PSN: {}, Program: {}, Status: {}",
                psn, programCode, status);

        try {
            EligibilityResponse response = eligibilityAssessmentService
                    .updateEligibilityStatus(psn, programCode, status, reason, updatedBy);

            log.info("Eligibility status updated for PSN: {}", psn);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating eligibility status for PSN: {}", psn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get pending reviews",
               description = "Retrieve eligibility assessments requiring manual review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending reviews retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/pending-reviews")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<EligibilityResponse>> getPendingReviews(
            @Parameter(description = "Program code (optional)")
            @RequestParam(required = false) String programCode,
            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "50") int limit) {

        log.info("Getting pending reviews for program: {}, limit: {}", programCode, limit);

        try {
            List<EligibilityResponse> pendingReviews = eligibilityAssessmentService
                    .getPendingReviews(programCode, limit);

            log.info("Retrieved {} pending reviews", pendingReviews.size());

            return ResponseEntity.ok(pendingReviews);

        } catch (Exception e) {
            log.error("Error getting pending reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Calculate eligibility score",
               description = "Calculate detailed eligibility score breakdown for debugging")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Score calculation completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/calculate-score")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> calculateEligibilityScore(
            @Valid @RequestBody EligibilityRequest request) {

        log.info("Calculating eligibility score for PSN: {}", request.getPsn());

        try {
            Map<String, Object> scoreBreakdown = eligibilityAssessmentService
                    .calculateEligibilityScore(request);

            log.info("Eligibility score calculated for PSN: {}", request.getPsn());

            return ResponseEntity.ok(scoreBreakdown);

        } catch (Exception e) {
            log.error("Error calculating eligibility score for PSN: {}", request.getPsn(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
