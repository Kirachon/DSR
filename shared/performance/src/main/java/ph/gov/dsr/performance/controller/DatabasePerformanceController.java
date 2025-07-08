package ph.gov.dsr.performance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.performance.database.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database Performance Monitoring and Optimization Controller
 */
@RestController
@RequestMapping("/api/v1/admin/database-performance")
@RequiredArgsConstructor
@Tag(name = "Database Performance", description = "Database performance monitoring and optimization")
public class DatabasePerformanceController {

    private final DatabaseOptimizationService optimizationService;
    private final QueryAnalyzer queryAnalyzer;
    private final ConnectionPoolMonitor connectionPoolMonitor;

    @GetMapping("/health")
    @Operation(summary = "Get database health status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            DatabaseMetrics metrics = optimizationService.getDatabaseMetrics();
            ConnectionPoolMetrics poolMetrics = connectionPoolMonitor.getCurrentMetrics();
            
            health.put("database", Map.of(
                "healthy", metrics.isHealthy(),
                "status", metrics.getHealthStatus(),
                "connections", metrics.getActiveConnections(),
                "size", metrics.getFormattedDatabaseSize()
            ));
            
            health.put("connectionPool", Map.of(
                "healthy", poolMetrics.isHealthy(),
                "status", poolMetrics.getHealthStatus(),
                "utilization", poolMetrics.getFormattedUtilization(),
                "summary", poolMetrics.getConnectionSummary()
            ));
            
            boolean overallHealthy = metrics.isHealthy() && poolMetrics.isHealthy();
            health.put("overall", Map.of(
                "healthy", overallHealthy,
                "status", overallHealthy ? "HEALTHY" : "ISSUES_DETECTED"
            ));
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get comprehensive database metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatabaseMetrics> getDatabaseMetrics() {
        try {
            DatabaseMetrics metrics = optimizationService.getDatabaseMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/slow-queries")
    @Operation(summary = "Get slow queries analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SlowQuery>> getSlowQueries(
            @RequestParam(defaultValue = "1000") long thresholdMs) {
        try {
            List<SlowQuery> slowQueries = queryAnalyzer.getSlowQueries(thresholdMs);
            return ResponseEntity.ok(slowQueries);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/index-usage")
    @Operation(summary = "Get index usage statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IndexUsageStats>> getIndexUsage() {
        try {
            List<IndexUsageStats> stats = queryAnalyzer.getIndexUsageStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/unused-indexes")
    @Operation(summary = "Get unused indexes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UnusedIndex>> getUnusedIndexes() {
        try {
            List<UnusedIndex> unusedIndexes = queryAnalyzer.getUnusedIndexes();
            return ResponseEntity.ok(unusedIndexes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/index-suggestions")
    @Operation(summary = "Get index optimization suggestions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IndexSuggestion>> getIndexSuggestions() {
        try {
            List<IndexSuggestion> suggestions = queryAnalyzer.suggestMissingIndexes();
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/table-bloat")
    @Operation(summary = "Get table bloat information")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TableBloat>> getTableBloat() {
        try {
            List<TableBloat> bloatInfo = queryAnalyzer.getTableBloatInfo();
            return ResponseEntity.ok(bloatInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/connection-pool")
    @Operation(summary = "Get connection pool metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConnectionPoolMetrics> getConnectionPoolMetrics() {
        try {
            ConnectionPoolMetrics metrics = connectionPoolMonitor.getCurrentMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/connection-pool/history")
    @Operation(summary = "Get connection pool performance history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConnectionPoolSnapshot>> getConnectionPoolHistory() {
        try {
            List<ConnectionPoolSnapshot> history = connectionPoolMonitor.getPerformanceHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get optimization recommendations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, List<String>>> getOptimizationRecommendations() {
        try {
            Map<String, List<String>> recommendations = new HashMap<>();
            
            recommendations.put("database", optimizationService.getOptimizationRecommendations());
            recommendations.put("connectionPool", connectionPoolMonitor.getOptimizationRecommendations());
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/optimize")
    @Operation(summary = "Run database optimization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> runOptimization() {
        try {
            optimizationService.performOptimization();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Database optimization completed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/vacuum")
    @Operation(summary = "Run database vacuum")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> runVacuum() {
        try {
            optimizationService.performVacuum();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Database vacuum completed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/analyze-stats")
    @Operation(summary = "Update database statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateStatistics() {
        try {
            optimizationService.updateStatistics();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Database statistics updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/query-metrics")
    @Operation(summary = "Clear query performance metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearQueryMetrics() {
        try {
            queryAnalyzer.clearMetrics();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Query metrics cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/connection-pool/reset")
    @Operation(summary = "Reset connection pool statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetConnectionPoolStats() {
        try {
            connectionPoolMonitor.resetStatistics();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Connection pool statistics reset successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
