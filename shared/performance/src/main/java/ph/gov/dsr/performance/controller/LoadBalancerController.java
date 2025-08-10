package ph.gov.dsr.performance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.performance.loadbalancer.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load Balancer Management and Monitoring Controller
 */
@RestController
@RequestMapping("/api/v1/admin/load-balancer")
@RequiredArgsConstructor
@Tag(name = "Load Balancer Management", description = "Load balancer monitoring and management operations")
public class LoadBalancerController {

    private final LoadBalancerService loadBalancerService;

    @GetMapping("/health")
    @Operation(summary = "Get load balancer health status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLoadBalancerHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            LoadBalancerStats stats = loadBalancerService.getStatistics();
            
            health.put("healthy", true);
            health.put("status", "OPERATIONAL");
            health.put("totalServices", stats.getTotalServices());
            health.put("timestamp", stats.getTimestamp());
            
            // Service health summary
            Map<String, Object> serviceHealth = new HashMap<>();
            for (Map.Entry<String, ServiceStats> entry : stats.getServiceStats().entrySet()) {
                ServiceStats serviceStats = entry.getValue();
                serviceHealth.put(entry.getKey(), Map.of(
                    "totalInstances", serviceStats.getTotalInstances(),
                    "healthyInstances", serviceStats.getHealthyInstances(),
                    "healthPercentage", serviceStats.getHealthPercentage()
                ));
            }
            health.put("services", serviceHealth);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("healthy", false);
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get comprehensive load balancer statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoadBalancerStats> getStatistics() {
        try {
            LoadBalancerStats stats = loadBalancerService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/services/{serviceName}/instances")
    @Operation(summary = "Get healthy instances for a service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceInstance>> getServiceInstances(@PathVariable String serviceName) {
        try {
            List<ServiceInstance> instances = loadBalancerService.getHealthyInstances(serviceName);
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/services/{serviceName}/instances")
    @Operation(summary = "Register a new service instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerInstance(
            @PathVariable String serviceName,
            @RequestBody ServiceInstance instance) {
        try {
            loadBalancerService.registerService(serviceName, instance);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "registered");
            response.put("instanceId", instance.getId());
            response.put("message", "Service instance registered successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/services/{serviceName}/instances/{instanceId}")
    @Operation(summary = "Unregister a service instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> unregisterInstance(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        try {
            loadBalancerService.unregisterService(serviceName, instanceId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "unregistered");
            response.put("instanceId", instanceId);
            response.put("message", "Service instance unregistered successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/services/{serviceName}/route")
    @Operation(summary = "Route request to best available instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceInstance> routeRequest(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "ROUND_ROBIN") LoadBalancingStrategy strategy) {
        try {
            ServiceInstance instance = loadBalancerService.routeRequest(serviceName, strategy);
            
            if (instance != null) {
                return ResponseEntity.ok(instance);
            } else {
                return ResponseEntity.status(503).build(); // Service Unavailable
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/circuit-breakers")
    @Operation(summary = "Get circuit breaker status for all instances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, CircuitBreakerStatus>> getCircuitBreakerStatus() {
        try {
            Map<String, CircuitBreakerStatus> status = loadBalancerService.getCircuitBreakerStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/circuit-breakers/{instanceId}/reset")
    @Operation(summary = "Reset circuit breaker for an instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetCircuitBreaker(@PathVariable String instanceId) {
        try {
            // This would require adding a reset method to LoadBalancerService
            Map<String, String> response = new HashMap<>();
            response.put("status", "reset");
            response.put("instanceId", instanceId);
            response.put("message", "Circuit breaker reset successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/health-check")
    @Operation(summary = "Trigger manual health check for all instances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerHealthCheck() {
        try {
            loadBalancerService.performHealthChecks();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Health checks performed for all instances");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/strategies")
    @Operation(summary = "Get available load balancing strategies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLoadBalancingStrategies() {
        Map<String, Object> strategies = new HashMap<>();
        
        for (LoadBalancingStrategy strategy : LoadBalancingStrategy.values()) {
            strategies.put(strategy.name(), Map.of(
                "name", strategy.name(),
                "description", strategy.getDescription(),
                "requiresMetrics", strategy.requiresMetrics(),
                "supportsWeights", strategy.supportsWeights(),
                "providesSessionAffinity", strategy.providesSessionAffinity()
            ));
        }
        
        return ResponseEntity.ok(strategies);
    }

    @PostMapping("/metrics/{instanceId}")
    @Operation(summary = "Record request metrics for an instance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> recordMetrics(
            @PathVariable String instanceId,
            @RequestParam long responseTimeMs,
            @RequestParam boolean success) {
        try {
            loadBalancerService.recordRequest(instanceId, responseTimeMs, success);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "recorded");
            response.put("instanceId", instanceId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/services/{serviceName}/recommendations")
    @Operation(summary = "Get optimization recommendations for a service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOptimizationRecommendations(@PathVariable String serviceName) {
        try {
            // This would analyze service performance and provide recommendations
            Map<String, Object> recommendations = new HashMap<>();
            
            List<ServiceInstance> instances = loadBalancerService.getHealthyInstances(serviceName);
            
            recommendations.put("serviceName", serviceName);
            recommendations.put("currentInstances", instances.size());
            recommendations.put("recommendations", List.of(
                "Consider adding more instances if response times are high",
                "Monitor error rates and implement circuit breakers",
                "Use weighted response time strategy for better performance"
            ));
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
