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
import ph.gov.dsr.interoperability.dto.ApiGatewayRequest;
import ph.gov.dsr.interoperability.dto.ApiGatewayResponse;
import ph.gov.dsr.interoperability.entity.ExternalSystemIntegration;
import ph.gov.dsr.interoperability.repository.ExternalSystemIntegrationRepository;
import ph.gov.dsr.interoperability.service.ApiGatewayService;
import ph.gov.dsr.interoperability.service.ExternalSystemConnectorService;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for API Gateway operations
 * Handles request routing to external systems
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@RestController
@RequestMapping("/api/v1/interoperability/gateway")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Gateway", description = "API gateway operations for external system integration")
public class ApiGatewayController {

    private final ApiGatewayService apiGatewayService;
    private final ExternalSystemConnectorService connectorService;
    private final ExternalSystemIntegrationRepository systemRepository;

    @Operation(summary = "Route request to external system", 
               description = "Route a request to an external system through the API gateway")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request routed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "External system not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "External system error")
    })
    @PostMapping("/route")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiGatewayResponse> routeRequest(
            @Valid @RequestBody ApiGatewayRequest request) {
        
        log.info("Routing request to system: {} endpoint: {}", 
                request.getSystemCode(), request.getEndpoint());
        
        try {
            ApiGatewayResponse response = apiGatewayService.routeRequest(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error routing request to system: {}", request.getSystemCode(), e);
            
            ApiGatewayResponse errorResponse = ApiGatewayResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .errorCode("GATEWAY_ERROR")
                    .errorMessage("Gateway error: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Route request with resilience", 
               description = "Route a request with circuit breaker and retry mechanisms")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request routed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "External system not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "External system error")
    })
    @PostMapping("/route-resilient")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiGatewayResponse> routeRequestWithResilience(
            @Valid @RequestBody ApiGatewayRequest request) {
        
        log.info("Routing resilient request to system: {} endpoint: {}", 
                request.getSystemCode(), request.getEndpoint());
        
        try {
            ApiGatewayResponse response = apiGatewayService.routeRequestWithResilience(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error routing resilient request to system: {}", request.getSystemCode(), e);
            
            ApiGatewayResponse errorResponse = ApiGatewayResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .errorCode("GATEWAY_ERROR")
                    .errorMessage("Gateway error: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Connect to government system", 
               description = "Connect to a specific government system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connected successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Government system not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Connection error")
    })
    @PostMapping("/connect/{systemCode}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiGatewayResponse> connectToGovernmentSystem(
            @Parameter(description = "System code") @PathVariable String systemCode,
            @Valid @RequestBody ApiGatewayRequest request) {
        
        log.info("Connecting to government system: {}", systemCode);
        
        try {
            request.setSystemCode(systemCode);
            ApiGatewayResponse response = connectorService.connectToGovernmentSystem(systemCode, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error connecting to government system: {}", systemCode, e);
            
            ApiGatewayResponse errorResponse = ApiGatewayResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .errorCode("CONNECTION_ERROR")
                    .errorMessage("Connection error: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Check system health", 
               description = "Check the health status of an external system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Health check completed"),
        @ApiResponse(responseCode = "404", description = "External system not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/systems/{systemCode}/health-check")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkSystemHealth(
            @Parameter(description = "System code") @PathVariable String systemCode) {
        
        log.info("Checking health for system: {}", systemCode);
        
        Optional<ExternalSystemIntegration> systemOpt = systemRepository.findBySystemCode(systemCode);
        if (systemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ExternalSystemIntegration system = systemOpt.get();
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Perform basic health check
            ApiGatewayRequest healthRequest = new ApiGatewayRequest();
            healthRequest.setSystemCode(systemCode);
            healthRequest.setEndpoint("/health");
            healthRequest.setMethod("GET");
            
            long startTime = System.currentTimeMillis();
            ApiGatewayResponse response = apiGatewayService.routeRequest(healthRequest);
            long responseTime = System.currentTimeMillis() - startTime;
            
            healthStatus.put("systemCode", systemCode);
            healthStatus.put("systemName", system.getSystemName());
            healthStatus.put("status", response.isSuccess() ? "HEALTHY" : "UNHEALTHY");
            healthStatus.put("responseTime", responseTime);
            healthStatus.put("lastChecked", LocalDateTime.now());
            healthStatus.put("isActive", system.getIsActive());
            healthStatus.put("uptime", system.getUptimePercentage());
            healthStatus.put("errorRate", calculateErrorRate(system));
            
            if (!response.isSuccess()) {
                healthStatus.put("errorMessage", response.getErrorMessage());
                healthStatus.put("errorCode", response.getErrorCode());
            }
            
        } catch (Exception e) {
            log.error("Health check failed for system: {}", systemCode, e);
            
            healthStatus.put("systemCode", systemCode);
            healthStatus.put("systemName", system.getSystemName());
            healthStatus.put("status", "DOWN");
            healthStatus.put("lastChecked", LocalDateTime.now());
            healthStatus.put("errorMessage", e.getMessage());
        }
        
        return ResponseEntity.ok(healthStatus);
    }

    @Operation(summary = "Get all systems health", 
               description = "Get health status of all external systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Health status retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/systems/health")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllSystemsHealth() {
        log.info("Checking health for all systems");
        
        List<ExternalSystemIntegration> systems = systemRepository.findByIsActiveTrue();
        List<Map<String, Object>> healthStatuses = systems.stream()
                .map(system -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("systemCode", system.getSystemCode());
                    status.put("systemName", system.getSystemName());
                    status.put("status", system.isHealthy() ? "HEALTHY" : "UNHEALTHY");
                    status.put("lastHealthCheck", system.getLastHealthCheck());
                    status.put("uptime", system.getUptimePercentage());
                    status.put("errorRate", calculateErrorRate(system));
                    status.put("averageResponseTime", system.getAverageResponseTimeMs());
                    return status;
                })
                .toList();
        
        return ResponseEntity.ok(healthStatuses);
    }

    @Operation(summary = "Route with load balancing", 
               description = "Route request with automatic load balancing across multiple systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request routed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "All systems unavailable")
    })
    @PostMapping("/route-balanced")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiGatewayResponse> routeWithLoadBalancing(
            @Valid @RequestBody ApiGatewayRequest request) {
        
        log.info("Routing request with load balancing for system type: {}", request.getSystemCode());
        
        try {
            ApiGatewayResponse response = apiGatewayService.routeWithLoadBalancing(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error routing with load balancing", e);
            
            ApiGatewayResponse errorResponse = ApiGatewayResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .errorCode("LOAD_BALANCING_ERROR")
                    .errorMessage("Load balancing error: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Route with advanced routing",
               description = "Route request using advanced routing strategies including failover, load balancing, and broadcast")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request routed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Routing error"),
        @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @PostMapping("/route-advanced")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiGatewayResponse> routeWithAdvancedRouting(
            @Valid @RequestBody ApiGatewayRequest request) {

        log.info("Routing request with advanced routing for system: {}", request.getSystemCode());

        try {
            ApiGatewayResponse response = apiGatewayService.routeWithAdvancedRouting(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

        } catch (Exception e) {
            log.error("Error in advanced routing", e);

            ApiGatewayResponse errorResponse = ApiGatewayResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .errorCode("ADVANCED_ROUTING_ERROR")
                    .errorMessage("Advanced routing error: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Batch health check",
               description = "Perform health check on multiple external systems")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Batch health check completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/health/batch")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Map<String, Object>>> batchHealthCheck(
            @RequestBody List<String> systemCodes) {

        log.info("Performing batch health check for {} systems", systemCodes.size());

        try {
            Map<String, Map<String, Object>> results = apiGatewayService.batchHealthCheck(systemCodes);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error in batch health check", e);

            Map<String, Map<String, Object>> errorResult = new HashMap<>();
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("status", "ERROR");
            errorStatus.put("error", e.getMessage());
            errorStatus.put("timestamp", LocalDateTime.now());
            errorResult.put("batch_error", errorStatus);

            return ResponseEntity.status(500).body(errorResult);
        }
    }

    @Operation(summary = "Connect with advanced resilience",
               description = "Connect to external system using advanced retry mechanisms and monitoring")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connection successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Connection failed"),
        @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @PostMapping("/connect-resilient/{systemCode}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiGatewayResponse> connectWithAdvancedResilience(
            @Parameter(description = "System code") @PathVariable String systemCode,
            @Valid @RequestBody ApiGatewayRequest request) {

        log.info("Connecting with advanced resilience to system: {}", systemCode);

        try {
            ApiGatewayResponse response = connectorService.connectWithAdvancedResilience(systemCode, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }

        } catch (Exception e) {
            log.error("Error in resilient connection to system: {}", systemCode, e);

            ApiGatewayResponse errorResponse = ApiGatewayResponse.builder()
                    .success(false)
                    .statusCode(500)
                    .errorCode("RESILIENT_CONNECTION_ERROR")
                    .errorMessage("Resilient connection error: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get system health metrics",
               description = "Get comprehensive health metrics for an external system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "System not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/metrics/{systemCode}")
    @PreAuthorize("hasAnyRole('DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemHealthMetrics(
            @Parameter(description = "System code") @PathVariable String systemCode,
            @Parameter(description = "Include detailed metrics") @RequestParam(defaultValue = "false") boolean detailed) {

        log.info("Getting health metrics for system: {} (detailed: {})", systemCode, detailed);

        try {
            Map<String, Object> metrics = connectorService.getSystemHealthMetrics(systemCode, detailed);
            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            log.error("Error getting health metrics for system: {}", systemCode, e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "ERROR");
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * Calculate error rate for a system
     */
    private double calculateErrorRate(ExternalSystemIntegration system) {
        long totalCalls = system.getTotalSuccessfulCalls() + system.getTotalFailedCalls();
        if (totalCalls == 0) {
            return 0.0;
        }
        return (double) system.getTotalFailedCalls() / totalCalls * 100.0;
    }
}
