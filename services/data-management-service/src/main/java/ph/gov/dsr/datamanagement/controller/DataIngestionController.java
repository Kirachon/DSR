package ph.gov.dsr.datamanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.datamanagement.dto.DataIngestionRequest;
import ph.gov.dsr.datamanagement.dto.DataIngestionResponse;
import ph.gov.dsr.datamanagement.service.DataIngestionService;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for data ingestion operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/api/v1/data-ingestion")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Ingestion", description = "Data ingestion and processing operations")
public class DataIngestionController {

    private final DataIngestionService dataIngestionService;

    @Operation(summary = "Ingest single data record", 
               description = "Ingest a single data record from various sources")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Data ingested successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/ingest")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DataIngestionResponse> ingestData(
            @Valid @RequestBody DataIngestionRequest request) {
        
        log.info("Ingesting data from source: {}", request.getSourceSystem());
        DataIngestionResponse response = dataIngestionService.ingestData(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Ingest batch of data records", 
               description = "Ingest multiple data records in a single batch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Batch ingested successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/ingest-batch")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DataIngestionResponse> ingestBatch(
            @Valid @RequestBody List<DataIngestionRequest> requests,
            @Parameter(description = "Batch ID") @RequestParam String batchId) {
        
        log.info("Ingesting batch with {} records, batchId: {}", requests.size(), batchId);
        DataIngestionResponse response = dataIngestionService.ingestBatch(requests, batchId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get ingestion status", 
               description = "Get the status of a data ingestion operation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Ingestion not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/status/{ingestionId}")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DataIngestionResponse> getIngestionStatus(
            @Parameter(description = "Ingestion ID") @PathVariable UUID ingestionId) {
        
        log.info("Getting ingestion status for ID: {}", ingestionId);
        DataIngestionResponse response = dataIngestionService.getIngestionStatus(ingestionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process legacy data file", 
               description = "Process data file from legacy systems (Listahanan, i-Registro)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "File processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/process-legacy-file")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DataIngestionResponse> processLegacyDataFile(
            @Parameter(description = "Source system") @RequestParam String sourceSystem,
            @Parameter(description = "File path") @RequestParam String filePath,
            @Parameter(description = "Data type") @RequestParam String dataType) {
        
        log.info("Processing legacy data file from {}: {}", sourceSystem, filePath);
        DataIngestionResponse response = dataIngestionService.processLegacyDataFile(sourceSystem, filePath, dataType);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Validate data without persisting", 
               description = "Validate data structure and content without saving to database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data validated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/validate")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DataIngestionResponse> validateData(
            @Valid @RequestBody DataIngestionRequest request) {
        
        log.info("Validating data from source: {}", request.getSourceSystem());
        DataIngestionResponse response = dataIngestionService.validateData(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get ingestion statistics", 
               description = "Get statistics for a specific batch or overall ingestion metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Batch not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('DSWD_STAFF') or hasRole('LGU_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DataIngestionResponse> getIngestionStatistics(
            @Parameter(description = "Batch ID (optional)") @RequestParam(required = false) String batchId) {
        
        log.info("Getting ingestion statistics for batch: {}", batchId);
        DataIngestionResponse response = dataIngestionService.getIngestionStatistics(batchId);
        return ResponseEntity.ok(response);
    }
}
