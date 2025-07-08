package ph.gov.dsr.performance.loadbalancer;

import lombok.Builder;
import lombok.Data;

/**
 * Service-level statistics for load balancing
 */
@Data
@Builder
public class ServiceStats {
    private String serviceName;
    private int totalInstances;
    private int healthyInstances;
    private long totalRequests;
    private double averageResponseTime;
    
    public int getUnhealthyInstances() {
        return totalInstances - healthyInstances;
    }
    
    public double getHealthPercentage() {
        return totalInstances > 0 ? (double) healthyInstances / totalInstances * 100.0 : 0.0;
    }
    
    public String getHealthStatus() {
        if (healthyInstances == 0) return "CRITICAL";
        if (healthyInstances < totalInstances / 2) return "DEGRADED";
        if (healthyInstances < totalInstances) return "PARTIAL";
        return "HEALTHY";
    }
    
    public boolean isFullyHealthy() {
        return healthyInstances == totalInstances && totalInstances > 0;
    }
    
    public boolean hasCapacityIssues() {
        return healthyInstances <= 1; // Only one or no healthy instances
    }
    
    public double getRequestsPerInstance() {
        return healthyInstances > 0 ? (double) totalRequests / healthyInstances : 0.0;
    }
    
    public String getPerformanceLevel() {
        if (averageResponseTime < 100) return "EXCELLENT";
        if (averageResponseTime < 500) return "GOOD";
        if (averageResponseTime < 1000) return "FAIR";
        if (averageResponseTime < 2000) return "POOR";
        return "CRITICAL";
    }
}
