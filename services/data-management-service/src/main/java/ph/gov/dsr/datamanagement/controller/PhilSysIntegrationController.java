package ph.gov.dsr.datamanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationRequest;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationResponse;
import ph.gov.dsr.datamanagement.service.PhilSysIntegrationService;

/**
 * REST Controller for PhilSys integration operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/api/v1/philsys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PhilSys Integration", description = "PhilSys identity verification operations")
public class PhilSysIntegrationController {

    private final PhilSysIntegrationService philSysIntegrationService;

    @Operation(summary = "Verify PSN with PhilSys", 
               description = "Verify a PhilSys Number (PSN) and get person information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PSN verification completed"),
        @ApiResponse(responseCode = "400", description = "Invalid PSN or request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "503", description = "PhilSys service unavailable")
    })
    @PostMapping("/verify-psn")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PhilSysVerificationResponse> verifyPSN(
            @Valid @RequestBody PhilSysVerificationRequest request) {
        
        log.info("Verifying PSN: {}", request.getPsn());
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPSN(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check PSN validity", 
               description = "Check if a PSN format is valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PSN validity check completed"),
        @ApiResponse(responseCode = "400", description = "Invalid PSN format"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/validate-psn/{psn}")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Boolean> isPSNValid(
            @Parameter(description = "PhilSys Number") @PathVariable String psn) {
        
        log.info("Checking PSN validity: {}", psn);
        boolean isValid = philSysIntegrationService.isPSNValid(psn);
        return ResponseEntity.ok(isValid);
    }

    @Operation(summary = "Get person information", 
               description = "Get person information from PhilSys using PSN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Person information retrieved"),
        @ApiResponse(responseCode = "404", description = "PSN not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "503", description = "PhilSys service unavailable")
    })
    @GetMapping("/person-info/{psn}")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PhilSysVerificationResponse> getPersonInfo(
            @Parameter(description = "PhilSys Number") @PathVariable String psn) {
        
        log.info("Getting person info for PSN: {}", psn);
        PhilSysVerificationResponse response = philSysIntegrationService.getPersonInfo(psn);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify person details", 
               description = "Verify person details against PhilSys records")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Person details verification completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "503", description = "PhilSys service unavailable")
    })
    @PostMapping("/verify-details")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PhilSysVerificationResponse> verifyPersonDetails(
            @Valid @RequestBody PhilSysVerificationRequest request) {
        
        log.info("Verifying person details for PSN: {}", request.getPsn());
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPersonDetails(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check PhilSys service availability", 
               description = "Check if PhilSys service is available")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service availability check completed"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/service-availability")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Boolean> isPhilSysServiceAvailable() {
        
        log.info("Checking PhilSys service availability");
        boolean isAvailable = philSysIntegrationService.isPhilSysServiceAvailable();
        return ResponseEntity.ok(isAvailable);
    }

    @Operation(summary = "Get PhilSys service status", 
               description = "Get the current status of PhilSys service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service status retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/service-status")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<String> getPhilSysServiceStatus() {
        
        log.info("Getting PhilSys service status");
        String status = philSysIntegrationService.getPhilSysServiceStatus();
        return ResponseEntity.ok(status);
    }
}
