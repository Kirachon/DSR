package ph.gov.dsr.performance.loadbalancer;

/**
 * Load balancing strategies supported by the DSR load balancer
 */
public enum LoadBalancingStrategy {
    /**
     * Round-robin: Distribute requests evenly across all healthy instances
     */
    ROUND_ROBIN,
    
    /**
     * Least connections: Route to instance with fewest active connections
     */
    LEAST_CONNECTIONS,
    
    /**
     * Weighted response time: Route based on response time and error rate
     */
    WEIGHTED_RESPONSE_TIME,
    
    /**
     * Least response time: Route to instance with lowest average response time
     */
    LEAST_RESPONSE_TIME,
    
    /**
     * Random: Randomly select from healthy instances
     */
    RANDOM,
    
    /**
     * Consistent hash: Use consistent hashing for sticky sessions
     */
    CONSISTENT_HASH;
    
    public String getDescription() {
        return switch (this) {
            case ROUND_ROBIN -> "Distributes requests evenly across all healthy instances";
            case LEAST_CONNECTIONS -> "Routes to the instance with the fewest active connections";
            case WEIGHTED_RESPONSE_TIME -> "Routes based on response time and error rate weights";
            case LEAST_RESPONSE_TIME -> "Routes to the instance with the lowest average response time";
            case RANDOM -> "Randomly selects from available healthy instances";
            case CONSISTENT_HASH -> "Uses consistent hashing for session affinity";
        };
    }
    
    public boolean requiresMetrics() {
        return this == LEAST_CONNECTIONS || 
               this == WEIGHTED_RESPONSE_TIME || 
               this == LEAST_RESPONSE_TIME;
    }
    
    public boolean supportsWeights() {
        return this == WEIGHTED_RESPONSE_TIME;
    }
    
    public boolean providesSessionAffinity() {
        return this == CONSISTENT_HASH;
    }
}
