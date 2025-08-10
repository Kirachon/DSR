package ph.gov.dsr.datamanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for the Data Management Service.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check operations")
@Slf4j
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Check if the service is running")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "dsr-data-management-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "3.0.0");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Check if the service is ready to serve requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is ready")
    })
    public ResponseEntity<Map<String, Object>> ready() {
        log.info("Readiness check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "dsr-data-management-service");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
