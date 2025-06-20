package ph.gov.dsr.registration.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.common.dto.ApiResponse;
import ph.gov.dsr.common.dto.PaginationInfo;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.service.RegistrationService;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for household registration operations.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@RestController
@RequestMapping("/api/v3/registrations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Registration", description = "Household registration and profile management")
@SecurityRequirement(name = "OAuth2")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Operation(
        summary = "Create new household registration",
        description = "Creates a new household registration in the DSR system with PhilSys verification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration created successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Duplicate registration detected"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('registration:write')")
    public ResponseEntity<ApiResponse<RegistrationResponseDto>> createRegistration(
            @Valid @RequestBody RegistrationRequestDto request) {
        
        log.info("Creating new registration for PSN: {}", 
                 maskPsn(request.getHeadOfHouseholdPsn()));
        
        RegistrationResponseDto response = registrationService.createRegistration(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration created successfully"));
    }

    @Operation(
        summary = "Search household registrations",
        description = "Search for household registrations based on various criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('registration:read')")
    public ResponseEntity<ApiResponse<Page<RegistrationSummaryDto>>> searchRegistrations(
            @Parameter(description = "PhilSys Number to search for")
            @RequestParam(required = false) String psn,
            
            @Parameter(description = "Household ID to search for")
            @RequestParam(required = false) String householdId,
            
            @Parameter(description = "Filter by region")
            @RequestParam(required = false) String region,
            
            @Parameter(description = "Filter by registration status")
            @RequestParam(required = false) String status,
            
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        log.info("Searching registrations with filters - PSN: {}, HouseholdId: {}, Region: {}, Status: {}",
                 maskPsn(psn), householdId, region, status);
        
        RegistrationSearchCriteria criteria = RegistrationSearchCriteria.builder()
                .psn(psn)
                .householdId(householdId)
                .region(region)
                .status(status)
                .build();
        
        Page<RegistrationSummaryDto> results = registrationService.searchRegistrations(criteria, pageable);
        
        Map<String, Object> metadata = Map.of(
                "pagination", PaginationInfo.fromPage(results),
                "totalResults", results.getTotalElements()
        );
        
        return ResponseEntity.ok(
                ApiResponse.success(results, "Search completed successfully", metadata));
    }

    @Operation(
        summary = "Get registration details",
        description = "Retrieves detailed information about a specific household registration"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @GetMapping("/{registrationId}")
    @PreAuthorize("hasAuthority('registration:read')")
    public ResponseEntity<ApiResponse<RegistrationDetailDto>> getRegistration(
            @Parameter(description = "Registration ID", required = true)
            @PathVariable UUID registrationId,
            
            @Parameter(description = "Additional data to include")
            @RequestParam(required = false, defaultValue = "") String include) {
        
        log.info("Retrieving registration details for ID: {}", registrationId);
        
        RegistrationDetailDto registration = registrationService.getRegistrationDetail(registrationId, include);
        
        return ResponseEntity.ok(
                ApiResponse.success(registration, "Registration details retrieved successfully"));
    }

    @Operation(
        summary = "Update registration",
        description = "Updates an existing household registration"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration updated successfully"),
        @ApiResponse(responseCode = "404", description = "Registration not found"),
        @ApiResponse(responseCode = "409", description = "Conflict with current state"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PutMapping("/{registrationId}")
    @PreAuthorize("hasAuthority('registration:write')")
    public ResponseEntity<ApiResponse<RegistrationResponseDto>> updateRegistration(
            @Parameter(description = "Registration ID", required = true)
            @PathVariable UUID registrationId,
            
            @Valid @RequestBody RegistrationUpdateRequestDto request) {
        
        log.info("Updating registration: {}", registrationId);
        
        RegistrationResponseDto response = registrationService.updateRegistration(registrationId, request);
        
        return ResponseEntity.ok(
                ApiResponse.success(response, "Registration updated successfully"));
    }

    @Operation(
        summary = "Report life event",
        description = "Reports a significant life event that may affect household eligibility"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Life event accepted for processing"),
        @ApiResponse(responseCode = "404", description = "Registration not found"),
        @ApiResponse(responseCode = "400", description = "Invalid life event data")
    })
    @PostMapping("/{registrationId}/life-events")
    @PreAuthorize("hasAuthority('registration:write')")
    public ResponseEntity<ApiResponse<LifeEventResponseDto>> reportLifeEvent(
            @Parameter(description = "Registration ID", required = true)
            @PathVariable UUID registrationId,
            
            @Valid @RequestBody LifeEventRequestDto request) {
        
        log.info("Reporting life event for registration: {}, event type: {}", 
                 registrationId, request.getEventType());
        
        LifeEventResponseDto response = registrationService.reportLifeEvent(registrationId, request);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Life event accepted for processing"));
    }

    @Operation(
        summary = "Get registration life events",
        description = "Retrieves all life events for a specific registration"
    )
    @GetMapping("/{registrationId}/life-events")
    @PreAuthorize("hasAuthority('registration:read')")
    public ResponseEntity<ApiResponse<Page<LifeEventDto>>> getLifeEvents(
            @Parameter(description = "Registration ID", required = true)
            @PathVariable UUID registrationId,
            
            @PageableDefault(size = 20, sort = "eventDate") Pageable pageable) {
        
        log.info("Retrieving life events for registration: {}", registrationId);
        
        Page<LifeEventDto> lifeEvents = registrationService.getLifeEvents(registrationId, pageable);
        
        Map<String, Object> metadata = Map.of(
                "pagination", PaginationInfo.fromPage(lifeEvents)
        );
        
        return ResponseEntity.ok(
                ApiResponse.success(lifeEvents, "Life events retrieved successfully", metadata));
    }

    @Operation(
        summary = "Get registration status",
        description = "Retrieves the current status and processing information for a registration"
    )
    @GetMapping("/{registrationId}/status")
    @PreAuthorize("hasAuthority('registration:read')")
    public ResponseEntity<ApiResponse<RegistrationStatusDto>> getRegistrationStatus(
            @Parameter(description = "Registration ID", required = true)
            @PathVariable UUID registrationId) {
        
        log.info("Retrieving status for registration: {}", registrationId);
        
        RegistrationStatusDto status = registrationService.getRegistrationStatus(registrationId);
        
        return ResponseEntity.ok(
                ApiResponse.success(status, "Registration status retrieved successfully"));
    }

    /**
     * Masks PSN for logging purposes.
     */
    private String maskPsn(String psn) {
        if (psn == null || psn.length() < 8) {
            return "****";
        }
        return psn.substring(0, 4) + "****" + psn.substring(psn.length() - 4);
    }
}
