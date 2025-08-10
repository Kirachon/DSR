package ph.gov.dsr.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Health check controller for the Registration Service.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check operations")
@Slf4j
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "dsr-registration-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "3.0.0");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Check if the service is ready to serve requests")
    public ResponseEntity<Map<String, Object>> ready() {
        log.info("Readiness check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "dsr-registration-service");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
