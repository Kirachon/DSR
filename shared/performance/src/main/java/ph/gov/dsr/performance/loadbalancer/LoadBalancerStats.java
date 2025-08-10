package ph.gov.dsr.performance.loadbalancer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Load balancer statistics and metrics
 */
@Data
@Builder
public class LoadBalancerStats {
    private Map<String, ServiceStats> serviceStats;
    private int totalServices;
    private LocalDateTime timestamp;
    
    public int getTotalInstances() {
        return serviceStats.values().stream()
            .mapToInt(ServiceStats::getTotalInstances)
            .sum();
    }
    
    public int getTotalHealthyInstances() {
        return serviceStats.values().stream()
            .mapToInt(ServiceStats::getHealthyInstances)
            .sum();
    }
    
    public double getOverallHealthPercentage() {
        int total = getTotalInstances();
        int healthy = getTotalHealthyInstances();
        return total > 0 ? (double) healthy / total * 100.0 : 0.0;
    }
    
    public long getTotalRequests() {
        return serviceStats.values().stream()
            .mapToLong(ServiceStats::getTotalRequests)
            .sum();
    }
    
    public double getAverageResponseTime() {
        return serviceStats.values().stream()
            .mapToDouble(ServiceStats::getAverageResponseTime)
            .average()
            .orElse(0.0);
    }
    
    public String getHealthStatus() {
        double healthPercentage = getOverallHealthPercentage();
        
        if (healthPercentage >= 90) return "EXCELLENT";
        if (healthPercentage >= 75) return "GOOD";
        if (healthPercentage >= 50) return "FAIR";
        if (healthPercentage >= 25) return "POOR";
        return "CRITICAL";
    }
    
    public boolean hasUnhealthyServices() {
        return serviceStats.values().stream()
            .anyMatch(stats -> stats.getHealthyInstances() == 0);
    }
    
    public boolean hasPerformanceIssues() {
        return serviceStats.values().stream()
            .anyMatch(stats -> stats.getAverageResponseTime() > 2000); // 2 seconds threshold
    }
}
