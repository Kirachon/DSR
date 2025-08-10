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
import ph.gov.dsr.datamanagement.dto.ValidationRequest;
import ph.gov.dsr.datamanagement.dto.ValidationResponse;
import ph.gov.dsr.datamanagement.service.DataValidationService;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for data validation operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/api/v1/data-validation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Validation", description = "Data validation and cleaning operations")
public class DataValidationController {

    private final DataValidationService dataValidationService;

    @Operation(summary = "Validate single data record", 
               description = "Validate a single data record against defined rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data validated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/validate")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ValidationResponse> validateData(
            @Valid @RequestBody ValidationRequest request) {
        
        log.info("Validating data of type: {}", request.getDataType());
        ValidationResponse response = dataValidationService.validateData(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate batch of data records", 
               description = "Validate multiple data records in a single batch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Batch validated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/validate-batch")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<ValidationResponse>> validateBatch(
            @Valid @RequestBody List<ValidationRequest> requests) {
        
        log.info("Validating batch of {} records", requests.size());
        List<ValidationResponse> responses = dataValidationService.validateBatch(requests);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Clean and normalize data", 
               description = "Clean and normalize data according to defined rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data cleaned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/clean")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanData(
            @RequestBody Map<String, Object> data,
            @Parameter(description = "Data type") @RequestParam String dataType) {
        
        log.info("Cleaning data of type: {}", dataType);
        Map<String, Object> cleanedData = dataValidationService.cleanData(data, dataType);
        return ResponseEntity.ok(cleanedData);
    }

    @Operation(summary = "Get validation rules", 
               description = "Get validation rules for a specific data type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rules retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Data type not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/rules/{dataType}")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<String>> getValidationRules(
            @Parameter(description = "Data type") @PathVariable String dataType) {
        
        log.info("Getting validation rules for data type: {}", dataType);
        List<String> rules = dataValidationService.getValidationRules(dataType);
        return ResponseEntity.ok(rules);
    }

    @Operation(summary = "Add validation rule", 
               description = "Add a custom validation rule for a data type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rule added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid rule definition"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/rules/{dataType}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> addValidationRule(
            @Parameter(description = "Data type") @PathVariable String dataType,
            @Parameter(description = "Rule name") @RequestParam String ruleName,
            @Parameter(description = "Rule expression") @RequestParam String ruleExpression) {
        
        log.info("Adding validation rule '{}' for data type: {}", ruleName, dataType);
        dataValidationService.addValidationRule(dataType, ruleName, ruleExpression);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Remove validation rule", 
               description = "Remove a validation rule for a data type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Rule removed successfully"),
        @ApiResponse(responseCode = "404", description = "Rule not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/rules/{dataType}/{ruleName}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> removeValidationRule(
            @Parameter(description = "Data type") @PathVariable String dataType,
            @Parameter(description = "Rule name") @PathVariable String ruleName) {
        
        log.info("Removing validation rule '{}' for data type: {}", ruleName, dataType);
        dataValidationService.removeValidationRule(dataType, ruleName);
        return ResponseEntity.noContent().build();
    }
}
