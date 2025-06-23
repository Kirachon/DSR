package ph.gov.dsr.eligibility.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.eligibility.dto.ProgramInfo;
import ph.gov.dsr.eligibility.service.ProgramManagementService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for program management operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Program Management", description = "APIs for social protection program management")
@SecurityRequirement(name = "bearerAuth")
public class ProgramController {

    private final ProgramManagementService programManagementService;

    @Operation(summary = "Get all programs", 
               description = "Retrieve all available social protection programs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Programs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ProgramInfo>> getAllPrograms(
            @Parameter(description = "Return only active programs")
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        log.info("Getting all programs, activeOnly: {}", activeOnly);
        
        try {
            List<ProgramInfo> programs = programManagementService.getAllPrograms(activeOnly);
            
            log.info("Retrieved {} programs", programs.size());
            
            return ResponseEntity.ok(programs);
            
        } catch (Exception e) {
            log.error("Error getting all programs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get program by code", 
               description = "Retrieve specific program information by program code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Program retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{programCode}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ProgramInfo> getProgramByCode(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode) {
        
        log.info("Getting program by code: {}", programCode);
        
        try {
            ProgramInfo program = programManagementService.getProgramByCode(programCode);
            
            if (program == null) {
                return ResponseEntity.notFound().build();
            }
            
            log.info("Retrieved program: {}", program.getProgramName());
            
            return ResponseEntity.ok(program);
            
        } catch (Exception e) {
            log.error("Error getting program by code: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get programs by agency", 
               description = "Retrieve programs by implementing agency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Programs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/agency/{agency}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ProgramInfo>> getProgramsByAgency(
            @Parameter(description = "Implementing agency", required = true)
            @PathVariable String agency,
            @Parameter(description = "Return only active programs")
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        log.info("Getting programs by agency: {}, activeOnly: {}", agency, activeOnly);
        
        try {
            List<ProgramInfo> programs = programManagementService.getProgramsByAgency(agency, activeOnly);
            
            log.info("Retrieved {} programs for agency: {}", programs.size(), agency);
            
            return ResponseEntity.ok(programs);
            
        } catch (Exception e) {
            log.error("Error getting programs by agency: {}", agency, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get programs by type", 
               description = "Retrieve programs by program type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Programs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/type/{programType}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ProgramInfo>> getProgramsByType(
            @Parameter(description = "Program type", required = true)
            @PathVariable ProgramInfo.ProgramType programType,
            @Parameter(description = "Return only active programs")
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        log.info("Getting programs by type: {}, activeOnly: {}", programType, activeOnly);
        
        try {
            List<ProgramInfo> programs = programManagementService.getProgramsByType(programType, activeOnly);
            
            log.info("Retrieved {} programs for type: {}", programs.size(), programType);
            
            return ResponseEntity.ok(programs);
            
        } catch (Exception e) {
            log.error("Error getting programs by type: {}", programType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Search programs", 
               description = "Search programs by various criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Programs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ProgramInfo>> searchPrograms(
            @RequestBody Map<String, Object> searchCriteria) {
        
        log.info("Searching programs with criteria: {}", searchCriteria);
        
        try {
            List<ProgramInfo> programs = programManagementService.searchPrograms(searchCriteria);
            
            log.info("Found {} programs matching criteria", programs.size());
            
            return ResponseEntity.ok(programs);
            
        } catch (Exception e) {
            log.error("Error searching programs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get programs by location", 
               description = "Retrieve programs available in a specific geographic area")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Programs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/location")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ProgramInfo>> getProgramsByLocation(
            @Parameter(description = "Region code", required = true)
            @RequestParam String region,
            @Parameter(description = "Province name")
            @RequestParam(required = false) String province,
            @Parameter(description = "City/Municipality name")
            @RequestParam(required = false) String cityMunicipality) {
        
        log.info("Getting programs by location: {}, {}, {}", region, province, cityMunicipality);
        
        try {
            List<ProgramInfo> programs = programManagementService
                    .getProgramsByLocation(region, province, cityMunicipality);
            
            log.info("Retrieved {} programs for location", programs.size());
            
            return ResponseEntity.ok(programs);
            
        } catch (Exception e) {
            log.error("Error getting programs by location", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Check if program is accepting applications", 
               description = "Check if a program is currently accepting new applications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status checked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{programCode}/accepting-applications")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> isProgramAcceptingApplications(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode) {
        
        log.info("Checking if program is accepting applications: {}", programCode);
        
        try {
            boolean isAccepting = programManagementService.isProgramAcceptingApplications(programCode);
            
            Map<String, Boolean> result = Map.of("acceptingApplications", isAccepting);
            
            log.info("Program {} accepting applications: {}", programCode, isAccepting);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error checking program application status: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get program capacity", 
               description = "Retrieve program capacity information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capacity information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{programCode}/capacity")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getProgramCapacity(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode) {
        
        log.info("Getting program capacity for: {}", programCode);
        
        try {
            Map<String, Object> capacity = programManagementService.getProgramCapacity(programCode);
            
            if (capacity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            log.info("Retrieved capacity information for program: {}", programCode);
            
            return ResponseEntity.ok(capacity);
            
        } catch (Exception e) {
            log.error("Error getting program capacity: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get program statistics", 
               description = "Retrieve program statistics and metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{programCode}/statistics")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getProgramStatistics(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode) {
        
        log.info("Getting program statistics for: {}", programCode);
        
        try {
            Map<String, Object> statistics = programManagementService.getProgramStatistics(programCode);
            
            log.info("Retrieved statistics for program: {}", programCode);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Error getting program statistics: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get eligible programs",
               description = "Get programs that a household might be eligible for based on criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Eligible programs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/eligible")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ProgramInfo>> getEligiblePrograms(
            @Parameter(description = "Monthly household income")
            @RequestParam(required = false) Double householdIncome,
            @Parameter(description = "Number of household members")
            @RequestParam(required = false) Integer householdSize,
            @Parameter(description = "Region")
            @RequestParam(required = false) String region,
            @Parameter(description = "Province")
            @RequestParam(required = false) String province,
            @Parameter(description = "City/Municipality")
            @RequestParam(required = false) String cityMunicipality,
            @Parameter(description = "Vulnerability factors (comma-separated)")
            @RequestParam(required = false) List<String> vulnerabilityFactors) {

        log.info("Getting eligible programs for income: {}, size: {}", householdIncome, householdSize);

        try {
            Map<String, String> location = Map.of(
                "region", region != null ? region : "",
                "province", province != null ? province : "",
                "cityMunicipality", cityMunicipality != null ? cityMunicipality : ""
            );

            List<ProgramInfo> programs = programManagementService
                    .getEligiblePrograms(householdIncome, householdSize, location, vulnerabilityFactors);

            log.info("Found {} potentially eligible programs", programs.size());

            return ResponseEntity.ok(programs);

        } catch (Exception e) {
            log.error("Error getting eligible programs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get program enrollment trends",
               description = "Retrieve enrollment trends for a program")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enrollment trends retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{programCode}/enrollment-trends")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getProgramEnrollmentTrends(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode,
            @Parameter(description = "Number of months to look back")
            @RequestParam(defaultValue = "12") int months) {

        log.info("Getting enrollment trends for program: {}, months: {}", programCode, months);

        try {
            Map<String, Object> trends = programManagementService
                    .getProgramEnrollmentTrends(programCode, months);

            log.info("Retrieved enrollment trends for program: {}", programCode);

            return ResponseEntity.ok(trends);

        } catch (Exception e) {
            log.error("Error getting enrollment trends for program: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update program status",
               description = "Update the status of a program")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Program status updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{programCode}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ProgramInfo> updateProgramStatus(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode,
            @Parameter(description = "New program status", required = true)
            @RequestParam ProgramInfo.ProgramStatus status,
            @Parameter(description = "Reason for status change", required = true)
            @RequestParam String reason,
            @Parameter(description = "User making the change", required = true)
            @RequestParam String updatedBy) {

        log.info("Updating program status for: {}, new status: {}", programCode, status);

        try {
            ProgramInfo updatedProgram = programManagementService
                    .updateProgramStatus(programCode, status, reason, updatedBy);

            if (updatedProgram == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Program status updated for: {}", programCode);

            return ResponseEntity.ok(updatedProgram);

        } catch (Exception e) {
            log.error("Error updating program status: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update program capacity",
               description = "Update the target beneficiary capacity of a program")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Program capacity updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{programCode}/capacity")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ProgramInfo> updateProgramCapacity(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode,
            @Parameter(description = "New target beneficiary capacity", required = true)
            @RequestParam Integer newCapacity,
            @Parameter(description = "Reason for capacity change", required = true)
            @RequestParam String reason,
            @Parameter(description = "User making the change", required = true)
            @RequestParam String updatedBy) {

        log.info("Updating program capacity for: {}, new capacity: {}", programCode, newCapacity);

        try {
            ProgramInfo updatedProgram = programManagementService
                    .updateProgramCapacity(programCode, newCapacity, reason, updatedBy);

            if (updatedProgram == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Program capacity updated for: {}", programCode);

            return ResponseEntity.ok(updatedProgram);

        } catch (Exception e) {
            log.error("Error updating program capacity: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Validate program configuration",
               description = "Validate the configuration of a program")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Program configuration validated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Program not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{programCode}/validate")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> validateProgramConfiguration(
            @Parameter(description = "Program code", required = true)
            @PathVariable String programCode) {

        log.info("Validating program configuration for: {}", programCode);

        try {
            Map<String, Object> validation = programManagementService
                    .validateProgramConfiguration(programCode);

            log.info("Program configuration validated for: {}", programCode);

            return ResponseEntity.ok(validation);

        } catch (Exception e) {
            log.error("Error validating program configuration: {}", programCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
