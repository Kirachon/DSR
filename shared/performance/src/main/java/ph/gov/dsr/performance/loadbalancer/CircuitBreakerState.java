package ph.gov.dsr.performance.loadbalancer;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Circuit breaker state for service instances
 */
@Data
public class CircuitBreakerState {
    private int failureCount = 0;
    private LocalDateTime lastFailureTime;
    private LocalDateTime lastSuccessTime;
    private boolean isOpen = false;
    private boolean isHalfOpen = false;
    private final int failureThreshold;
    private final int timeoutMinutes;
    private int successCountInHalfOpen = 0;
    private final int requiredSuccessCount = 3;

    public CircuitBreakerState(int failureThreshold, int timeoutMinutes) {
        this.failureThreshold = failureThreshold;
        this.timeoutMinutes = timeoutMinutes;
    }

    /**
     * Check if circuit breaker is open
     */
    public boolean isOpen() {
        if (isOpen && lastFailureTime != null) {
            // Check if timeout period has passed to transition to half-open
            if (LocalDateTime.now().isAfter(lastFailureTime.plusMinutes(timeoutMinutes))) {
                transitionToHalfOpen();
            }
        }
        return isOpen;
    }

    /**
     * Record a successful request
     */
    public synchronized void recordSuccess() {
        lastSuccessTime = LocalDateTime.now();
        
        if (isHalfOpen) {
            successCountInHalfOpen++;
            if (successCountInHalfOpen >= requiredSuccessCount) {
                transitionToClosed();
            }
        } else if (isOpen) {
            // Should not happen, but handle gracefully
            transitionToHalfOpen();
        } else {
            // Circuit is closed, reset failure count
            failureCount = 0;
        }
    }

    /**
     * Record a failed request
     */
    public synchronized void recordFailure() {
        failureCount++;
        lastFailureTime = LocalDateTime.now();
        
        if (isHalfOpen) {
            // Failure in half-open state, go back to open
            transitionToOpen();
        } else if (!isOpen && failureCount >= failureThreshold) {
            // Threshold reached, open the circuit
            transitionToOpen();
        }
    }

    /**
     * Get circuit breaker state as string
     */
    public String getState() {
        if (isOpen) return "OPEN";
        if (isHalfOpen) return "HALF_OPEN";
        return "CLOSED";
    }

    /**
     * Get failure rate percentage
     */
    public double getFailureRate() {
        // This would require tracking total requests, simplified for now
        return failureCount > 0 ? Math.min(100.0, (double) failureCount / failureThreshold * 100) : 0.0;
    }

    /**
     * Check if circuit breaker should allow request
     */
    public boolean allowRequest() {
        if (isOpen()) {
            return false;
        }
        
        if (isHalfOpen) {
            // In half-open state, allow limited requests
            return successCountInHalfOpen < requiredSuccessCount;
        }
        
        return true; // Closed state allows all requests
    }

    /**
     * Get time until circuit breaker can transition to half-open
     */
    public long getTimeUntilHalfOpen() {
        if (!isOpen || lastFailureTime == null) {
            return 0;
        }
        
        LocalDateTime halfOpenTime = lastFailureTime.plusMinutes(timeoutMinutes);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(halfOpenTime)) {
            return 0;
        }
        
        return java.time.Duration.between(now, halfOpenTime).toSeconds();
    }

    /**
     * Reset circuit breaker to closed state
     */
    public synchronized void reset() {
        failureCount = 0;
        isOpen = false;
        isHalfOpen = false;
        successCountInHalfOpen = 0;
        lastFailureTime = null;
    }

    /**
     * Force circuit breaker to open state
     */
    public synchronized void forceOpen() {
        isOpen = true;
        isHalfOpen = false;
        lastFailureTime = LocalDateTime.now();
    }

    /**
     * Get health status based on circuit breaker state
     */
    public String getHealthStatus() {
        if (isOpen) {
            return "CIRCUIT_OPEN";
        }
        if (isHalfOpen) {
            return "CIRCUIT_HALF_OPEN";
        }
        if (failureCount > 0) {
            return "DEGRADED";
        }
        return "HEALTHY";
    }

    // Private helper methods

    private void transitionToOpen() {
        isOpen = true;
        isHalfOpen = false;
        successCountInHalfOpen = 0;
        lastFailureTime = LocalDateTime.now();
    }

    private void transitionToHalfOpen() {
        isOpen = false;
        isHalfOpen = true;
        successCountInHalfOpen = 0;
    }

    private void transitionToClosed() {
        isOpen = false;
        isHalfOpen = false;
        failureCount = 0;
        successCountInHalfOpen = 0;
        lastFailureTime = null;
    }
}
