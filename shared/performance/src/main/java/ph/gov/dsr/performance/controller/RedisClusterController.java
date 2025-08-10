package ph.gov.dsr.performance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.performance.cache.CacheStatistics;
import ph.gov.dsr.performance.cache.ClusterInfo;
import ph.gov.dsr.performance.cache.RedisClusterCacheManager;
import ph.gov.dsr.performance.monitoring.ClusterMetrics;
import ph.gov.dsr.performance.monitoring.RedisClusterMonitoringService;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cluster Management and Monitoring Controller
 */
@RestController
@RequestMapping("/api/v1/admin/redis-cluster")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dsr.cache.type", havingValue = "redis")
@Tag(name = "Redis Cluster Management", description = "Redis cluster monitoring and management operations")
public class RedisClusterController {

    private final RedisClusterCacheManager cacheManager;
    private final RedisClusterMonitoringService monitoringService;

    @GetMapping("/health")
    @Operation(summary = "Check Redis cluster health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getClusterHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean isHealthy = cacheManager.isHealthy();
            ClusterInfo clusterInfo = cacheManager.getClusterInfo();
            
            health.put("healthy", isHealthy);
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("clusterInfo", clusterInfo);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("healthy", false);
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/info")
    @Operation(summary = "Get detailed cluster information")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClusterInfo> getClusterInfo() {
        try {
            ClusterInfo clusterInfo = cacheManager.getClusterInfo();
            return ResponseEntity.ok(clusterInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get cache statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CacheStatistics> getCacheStatistics() {
        try {
            CacheStatistics stats = cacheManager.getCacheStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get comprehensive cluster metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClusterMetrics> getClusterMetrics() {
        try {
            ClusterMetrics metrics = monitoringService.getClusterMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/warmup")
    @Operation(summary = "Perform cache warmup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> performCacheWarmup() {
        try {
            monitoringService.performCacheWarmup();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "started");
            response.put("message", "Cache warmup initiated");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/cache")
    @Operation(summary = "Clear all caches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        try {
            monitoringService.clearAllCaches();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All caches cleared");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/cache/{cacheName}")
    @Operation(summary = "Clear specific cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        try {
            cacheManager.evictCache(cacheName);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cache '" + cacheName + "' cleared");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/cache/{cacheName}/hit-rate")
    @Operation(summary = "Get hit rate for specific cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheHitRate(@PathVariable String cacheName) {
        try {
            double hitRate = monitoringService.getCacheHitRate(cacheName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("cacheName", cacheName);
            response.put("hitRate", hitRate);
            response.put("hitRatePercentage", hitRate * 100);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/nodes")
    @Operation(summary = "Get individual node status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, RedisClusterMonitoringService.NodeMetrics>> getNodeStatus() {
        try {
            ClusterMetrics metrics = monitoringService.getClusterMetrics();
            return ResponseEntity.ok(metrics.getNodeMetrics());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/health-check")
    @Operation(summary = "Trigger manual health check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerHealthCheck() {
        try {
            monitoringService.performHealthCheck();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Health check performed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
