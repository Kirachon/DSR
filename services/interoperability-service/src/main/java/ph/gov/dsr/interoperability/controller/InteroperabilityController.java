package ph.gov.dsr.interoperability.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.interoperability.entity.ExternalSystemIntegration;
import ph.gov.dsr.interoperability.repository.ExternalSystemIntegrationRepository;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for Interoperability Service operations
 * Handles external system management and integration
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@RestController
@RequestMapping("/api/v1/interoperability")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Interoperability", description = "External system integration and management operations")
public class InteroperabilityController {

    private final ExternalSystemIntegrationRepository systemRepository;

    @Operation(summary = "Get all external systems", 
               description = "Retrieve all configured external systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "External systems retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/systems")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ExternalSystemIntegration>> getAllExternalSystems() {
        log.info("Retrieving all external systems");
        List<ExternalSystemIntegration> systems = systemRepository.findAll();
        return ResponseEntity.ok(systems);
    }

    @Operation(summary = "Get external system by code", 
               description = "Retrieve external system configuration by system code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "External system retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "External system not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/systems/{systemCode}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ExternalSystemIntegration> getExternalSystem(
            @Parameter(description = "System code") @PathVariable String systemCode) {
        
        log.info("Retrieving external system: {}", systemCode);
        Optional<ExternalSystemIntegration> system = systemRepository.findBySystemCode(systemCode);
        
        if (system.isPresent()) {
            return ResponseEntity.ok(system.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create external system", 
               description = "Create a new external system integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "External system created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/systems")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ExternalSystemIntegration> createExternalSystem(
            @Valid @RequestBody ExternalSystemIntegration system) {
        
        log.info("Creating external system: {}", system.getSystemCode());
        ExternalSystemIntegration savedSystem = systemRepository.save(system);
        return ResponseEntity.status(201).body(savedSystem);
    }

    @Operation(summary = "Update external system", 
               description = "Update an existing external system integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "External system updated successfully"),
        @ApiResponse(responseCode = "404", description = "External system not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/systems/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ExternalSystemIntegration> updateExternalSystem(
            @Parameter(description = "System ID") @PathVariable UUID id,
            @Valid @RequestBody ExternalSystemIntegration system) {
        
        log.info("Updating external system: {}", id);
        
        if (!systemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        system.setId(id);
        ExternalSystemIntegration updatedSystem = systemRepository.save(system);
        return ResponseEntity.ok(updatedSystem);
    }

    @Operation(summary = "Delete external system", 
               description = "Delete an external system integration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "External system deleted successfully"),
        @ApiResponse(responseCode = "404", description = "External system not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/systems/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteExternalSystem(
            @Parameter(description = "System ID") @PathVariable UUID id) {
        
        log.info("Deleting external system: {}", id);
        
        if (!systemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        systemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get active external systems", 
               description = "Retrieve all active external systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active external systems retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/systems/active")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ExternalSystemIntegration>> getActiveExternalSystems() {
        log.info("Retrieving active external systems");
        List<ExternalSystemIntegration> systems = systemRepository.findByIsActiveTrue();
        return ResponseEntity.ok(systems);
    }

    @Operation(summary = "Get systems by type", 
               description = "Retrieve external systems by system type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "External systems retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/systems/type/{systemType}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<ExternalSystemIntegration>> getSystemsByType(
            @Parameter(description = "System type") @PathVariable String systemType) {
        
        log.info("Retrieving systems by type: {}", systemType);
        List<ExternalSystemIntegration> systems = systemRepository.findBySystemType(
            ExternalSystemIntegration.SystemType.valueOf(systemType.toUpperCase()));
        return ResponseEntity.ok(systems);
    }

    @Operation(summary = "Toggle system status", 
               description = "Activate or deactivate an external system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System status updated successfully"),
        @ApiResponse(responseCode = "404", description = "External system not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/systems/{id}/toggle-status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ExternalSystemIntegration> toggleSystemStatus(
            @Parameter(description = "System ID") @PathVariable UUID id) {
        
        log.info("Toggling status for system: {}", id);
        
        Optional<ExternalSystemIntegration> systemOpt = systemRepository.findById(id);
        if (systemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ExternalSystemIntegration system = systemOpt.get();
        system.setIsActive(!system.getIsActive());
        ExternalSystemIntegration updatedSystem = systemRepository.save(system);
        
        log.info("System {} status changed to: {}", system.getSystemCode(), system.getIsActive());
        return ResponseEntity.ok(updatedSystem);
    }
}
