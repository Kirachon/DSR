package ph.gov.dsr.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.registration.dto.RegistrationCreateRequest;
import ph.gov.dsr.registration.dto.RegistrationResponse;
import ph.gov.dsr.registration.dto.RegistrationUpdateRequest;
import ph.gov.dsr.registration.dto.RegistrationSearchCriteria;
import ph.gov.dsr.registration.entity.RegistrationStatus;
import ph.gov.dsr.registration.service.RegistrationService;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Registration operations
 * Handles household registration CRUD operations and workflows
 */
@RestController
@RequestMapping("/api/v1/registrations")
@Tag(name = "Registration", description = "Household Registration Management API")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Operation(summary = "Create new household registration", 
               description = "Creates a new household registration with basic information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Registration already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('CITIZEN') or hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<RegistrationResponse> createRegistration(
            @Valid @RequestBody RegistrationCreateRequest request) {

        RegistrationResponse response = registrationService.createRegistration(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get registration by ID", 
               description = "Retrieves a specific registration by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration found"),
        @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<RegistrationResponse> getRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID id) {

        RegistrationResponse response = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get registration by registration number", 
               description = "Retrieves a registration by its registration number")
    @GetMapping("/number/{registrationNumber}")
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<RegistrationResponse> getRegistrationByNumber(
            @Parameter(description = "Registration Number") @PathVariable String registrationNumber) {

        RegistrationResponse response = registrationService.getRegistrationByNumber(registrationNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update registration", 
               description = "Updates an existing registration with new information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration updated successfully"),
        @ApiResponse(responseCode = "404", description = "Registration not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<RegistrationResponse> updateRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID id,
            @Valid @RequestBody RegistrationUpdateRequest request) {

        RegistrationResponse response = registrationService.updateRegistration(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete registration", 
               description = "Soft deletes a registration (changes status to CANCELLED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Registration deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LGU_STAFF')")
    public ResponseEntity<Void> deleteRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID id) {
        
        registrationService.deleteRegistration(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Submit registration for verification", 
               description = "Submits a draft registration for verification process")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('CITIZEN') or hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<RegistrationResponse> submitRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID id) {

        RegistrationResponse response = registrationService.submitRegistration(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Approve registration", 
               description = "Approves a registration after verification")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<RegistrationResponse> approveRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID id,
            @RequestParam(required = false) String notes) {
        
        RegistrationResponse response = registrationService.approveRegistration(id, notes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject registration", 
               description = "Rejects a registration with reason")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<RegistrationResponse> rejectRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID id,
            @RequestParam String reason,
            @RequestParam(required = false) String notes) {
        
        RegistrationResponse response = registrationService.rejectRegistration(id, reason, notes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assign registration to staff", 
               description = "Assigns a registration to a staff member for processing")
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<RegistrationResponse> assignRegistration(
            @Parameter(description = "Registration ID") @PathVariable UUID id,
            @RequestParam UUID staffId) {
        
        RegistrationResponse response = registrationService.assignRegistration(id, staffId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all registrations", 
               description = "Retrieves all registrations with pagination")
    @GetMapping
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<RegistrationResponse>> getAllRegistrations(
            @Parameter(description = "Pagination parameters") Pageable pageable) {

        Page<RegistrationResponse> registrations = registrationService.getAllRegistrations(pageable);
        return ResponseEntity.ok(registrations);
    }

    @Operation(summary = "Get registrations by status", 
               description = "Retrieves registrations filtered by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByStatus(
            @Parameter(description = "Registration Status") @PathVariable RegistrationStatus status) {
        
        List<RegistrationResponse> registrations = registrationService.getRegistrationsByStatus(status);
        return ResponseEntity.ok(registrations);
    }

    @Operation(summary = "Get registrations assigned to user", 
               description = "Retrieves registrations assigned to a specific user")
    @GetMapping("/assigned/{userId}")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsAssignedTo(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        List<RegistrationResponse> registrations = registrationService.getRegistrationsAssignedTo(userId);
        return ResponseEntity.ok(registrations);
    }

    @Operation(summary = "Search registrations", 
               description = "Search registrations with various criteria")
    @PostMapping("/search")
    public ResponseEntity<Page<RegistrationResponse>> searchRegistrations(
            @RequestBody RegistrationSearchCriteria criteria,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        Page<RegistrationResponse> registrations = registrationService.searchRegistrations(criteria, pageable);
        return ResponseEntity.ok(registrations);
    }

    @Operation(summary = "Get registration statistics", 
               description = "Retrieves registration statistics and counts")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Object> getRegistrationStatistics() {

        Object statistics = registrationService.getRegistrationStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get overdue registrations", 
               description = "Retrieves registrations that are overdue for processing")
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<List<RegistrationResponse>> getOverdueRegistrations() {
        
        List<RegistrationResponse> registrations = registrationService.getOverdueRegistrations();
        return ResponseEntity.ok(registrations);
    }

    @Operation(summary = "Get registrations by household", 
               description = "Retrieves all registrations for a specific household")
    @GetMapping("/household/{householdId}")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByHousehold(
            @Parameter(description = "Household ID") @PathVariable UUID householdId) {
        
        List<RegistrationResponse> registrations = registrationService.getRegistrationsByHousehold(householdId);
        return ResponseEntity.ok(registrations);
    }

    @Operation(summary = "Update registration priority", 
               description = "Updates the priority level of a registration")
    @PutMapping("/{id}/priority")
    @PreAuthorize("hasRole('LGU_STAFF') or hasRole('DSWD_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<RegistrationResponse> updateRegistrationPriority(
            @Parameter(description = "Registration ID") @PathVariable UUID id,
            @RequestParam Integer priorityLevel) {
        
        RegistrationResponse response = registrationService.updateRegistrationPriority(id, priorityLevel);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add notes to registration", 
               description = "Adds notes to a registration")
    @PostMapping("/{id}/notes")
    public ResponseEntity<RegistrationResponse> addNotes(
            @Parameter(description = "Registration ID") @PathVariable UUID id,
            @RequestParam String notes) {
        
        RegistrationResponse response = registrationService.addNotes(id, notes);
        return ResponseEntity.ok(response);
    }
}
