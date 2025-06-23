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
import ph.gov.dsr.datamanagement.dto.DeduplicationRequest;
import ph.gov.dsr.datamanagement.dto.DeduplicationResponse;
import ph.gov.dsr.datamanagement.service.DeduplicationService;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for deduplication operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/api/v1/deduplication")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deduplication", description = "Data deduplication and duplicate management operations")
public class DeduplicationController {

    private final DeduplicationService deduplicationService;

    @Operation(summary = "Find potential duplicates", 
               description = "Find potential duplicate entities using fuzzy matching algorithms")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Duplicate search completed"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/find-duplicates")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DeduplicationResponse> findDuplicates(
            @Valid @RequestBody DeduplicationRequest request) {
        
        log.info("Finding duplicates for entity type: {}", request.getEntityType());
        DeduplicationResponse response = deduplicationService.findDuplicates(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check if entity is duplicate", 
               description = "Check if an entity is a duplicate of existing records")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Duplicate check completed"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/is-duplicate")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Boolean> isDuplicate(
            @Parameter(description = "Entity type") @RequestParam String entityType,
            @RequestBody Map<String, Object> entityData,
            @Parameter(description = "Match threshold") @RequestParam(defaultValue = "0.8") double threshold) {
        
        log.info("Checking if entity is duplicate for type: {}", entityType);
        boolean isDuplicate = deduplicationService.isDuplicate(entityType, entityData, threshold);
        return ResponseEntity.ok(isDuplicate);
    }

    @Operation(summary = "Merge duplicate entities", 
               description = "Merge two duplicate entities, keeping the primary and removing the duplicate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Entities merged successfully"),
        @ApiResponse(responseCode = "404", description = "Entity not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/merge")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> mergeDuplicates(
            @Parameter(description = "Primary entity ID") @RequestParam UUID primaryEntityId,
            @Parameter(description = "Duplicate entity ID") @RequestParam UUID duplicateEntityId,
            @Parameter(description = "Entity type") @RequestParam String entityType) {
        
        log.info("Merging duplicates: {} <- {} for type: {}", 
                primaryEntityId, duplicateEntityId, entityType);
        deduplicationService.mergeDuplicates(primaryEntityId, duplicateEntityId, entityType);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark entities as not duplicates", 
               description = "Mark two entities as not duplicates to prevent future matching")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Entities marked as not duplicates"),
        @ApiResponse(responseCode = "404", description = "Entity not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/mark-not-duplicates")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> markAsNotDuplicates(
            @Parameter(description = "First entity ID") @RequestParam UUID entityId1,
            @Parameter(description = "Second entity ID") @RequestParam UUID entityId2,
            @Parameter(description = "Entity type") @RequestParam String entityType) {
        
        log.info("Marking entities as not duplicates: {} and {} for type: {}", 
                entityId1, entityId2, entityType);
        deduplicationService.markAsNotDuplicates(entityId1, entityId2, entityType);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get deduplication statistics", 
               description = "Get statistics about deduplication operations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDeduplicationStatistics() {
        
        log.info("Getting deduplication statistics");
        Map<String, Object> statistics = deduplicationService.getDeduplicationStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Configure matching algorithm", 
               description = "Configure parameters for the matching algorithm")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Algorithm configured successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/configure-algorithm")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> configureMatchingAlgorithm(
            @Parameter(description = "Algorithm name") @RequestParam String algorithm,
            @RequestBody Map<String, Object> parameters) {
        
        log.info("Configuring matching algorithm: {} with parameters: {}", algorithm, parameters);
        deduplicationService.configureMatchingAlgorithm(algorithm, parameters);
        return ResponseEntity.noContent().build();
    }
}
