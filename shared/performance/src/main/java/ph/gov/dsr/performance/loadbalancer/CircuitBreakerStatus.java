package ph.gov.dsr.performance.loadbalancer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Circuit breaker status information
 */
@Data
@Builder
public class CircuitBreakerStatus {
    private String instanceId;
    private String state; // OPEN, HALF_OPEN, CLOSED
    private int failureCount;
    private LocalDateTime lastFailureTime;
    private LocalDateTime lastSuccessTime;
    private long timeUntilHalfOpen;
    private double failureRate;
    private String healthStatus;
    
    public boolean isHealthy() {
        return "CLOSED".equals(state) && failureCount == 0;
    }
    
    public boolean isBlocking() {
        return "OPEN".equals(state);
    }
    
    public boolean isRecovering() {
        return "HALF_OPEN".equals(state);
    }
    
    public String getStatusDescription() {
        return switch (state) {
            case "OPEN" -> "Circuit is open - blocking all requests";
            case "HALF_OPEN" -> "Circuit is half-open - allowing limited requests";
            case "CLOSED" -> "Circuit is closed - allowing all requests";
            default -> "Unknown circuit breaker state";
        };
    }
    
    public String getFormattedFailureRate() {
        return String.format("%.1f%%", failureRate);
    }
    
    public boolean requiresAttention() {
        return isBlocking() || failureCount > 3;
    }
}
