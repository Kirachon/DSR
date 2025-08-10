package ph.gov.dsr.performance.loadbalancer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Advanced Load Balancer Service with intelligent routing and health monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerService {

    private final RestTemplate restTemplate;
    
    @Value("${dsr.loadbalancer.health-check-interval:30000}")
    private long healthCheckInterval;
    
    @Value("${dsr.loadbalancer.circuit-breaker.failure-threshold:5}")
    private int circuitBreakerFailureThreshold;
    
    @Value("${dsr.loadbalancer.circuit-breaker.timeout-minutes:5}")
    private int circuitBreakerTimeoutMinutes;

    // Service registry
    private final Map<String, List<ServiceInstance>> serviceRegistry = new ConcurrentHashMap<>();
    
    // Load balancing state
    private final Map<String, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    private final Map<String, ServiceMetrics> serviceMetrics = new ConcurrentHashMap<>();

    /**
     * Register a service instance
     */
    public void registerService(String serviceName, ServiceInstance instance) {
        serviceRegistry.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(instance);
        serviceMetrics.put(instance.getId(), new ServiceMetrics(instance.getId()));
        log.info("Registered service instance: {} for service: {}", instance.getId(), serviceName);
    }

    /**
     * Unregister a service instance
     */
    public void unregisterService(String serviceName, String instanceId) {
        List<ServiceInstance> instances = serviceRegistry.get(serviceName);
        if (instances != null) {
            instances.removeIf(instance -> instance.getId().equals(instanceId));
            serviceMetrics.remove(instanceId);
            log.info("Unregistered service instance: {} from service: {}", instanceId, serviceName);
        }
    }

    /**
     * Route request to best available service instance
     */
    public ServiceInstance routeRequest(String serviceName, LoadBalancingStrategy strategy) {
        List<ServiceInstance> instances = getHealthyInstances(serviceName);
        
        if (instances.isEmpty()) {
            log.warn("No healthy instances available for service: {}", serviceName);
            return null;
        }

        return switch (strategy) {
            case ROUND_ROBIN -> selectRoundRobin(serviceName, instances);
            case LEAST_CONNECTIONS -> selectLeastConnections(instances);
            case WEIGHTED_RESPONSE_TIME -> selectWeightedResponseTime(instances);
            case LEAST_RESPONSE_TIME -> selectLeastResponseTime(instances);
            case RANDOM -> selectRandom(instances);
            case CONSISTENT_HASH -> selectConsistentHash(instances, serviceName);
        };
    }

    /**
     * Get healthy service instances
     */
    public List<ServiceInstance> getHealthyInstances(String serviceName) {
        List<ServiceInstance> instances = serviceRegistry.get(serviceName);
        if (instances == null) {
            return Collections.emptyList();
        }

        return instances.stream()
            .filter(this::isInstanceHealthy)
            .filter(instance -> !isCircuitBreakerOpen(instance.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Record request metrics
     */
    public void recordRequest(String instanceId, long responseTimeMs, boolean success) {
        ServiceMetrics metrics = serviceMetrics.get(instanceId);
        if (metrics != null) {
            metrics.recordRequest(responseTimeMs, success);
            
            // Update circuit breaker state
            CircuitBreakerState circuitBreaker = getCircuitBreaker(instanceId);
            if (success) {
                circuitBreaker.recordSuccess();
            } else {
                circuitBreaker.recordFailure();
            }
        }
    }

    /**
     * Scheduled health check for all service instances
     */
    @Scheduled(fixedRateString = "${dsr.loadbalancer.health-check-interval:30000}")
    public void performHealthChecks() {
        log.debug("Performing health checks on all service instances");
        
        for (Map.Entry<String, List<ServiceInstance>> entry : serviceRegistry.entrySet()) {
            String serviceName = entry.getKey();
            List<ServiceInstance> instances = entry.getValue();
            
            for (ServiceInstance instance : instances) {
                performHealthCheck(serviceName, instance);
            }
        }
    }

    /**
     * Get load balancer statistics
     */
    public LoadBalancerStats getStatistics() {
        Map<String, ServiceStats> serviceStats = new HashMap<>();
        
        for (Map.Entry<String, List<ServiceInstance>> entry : serviceRegistry.entrySet()) {
            String serviceName = entry.getKey();
            List<ServiceInstance> instances = entry.getValue();
            
            long totalRequests = instances.stream()
                .mapToLong(instance -> {
                    ServiceMetrics metrics = serviceMetrics.get(instance.getId());
                    return metrics != null ? metrics.getTotalRequests() : 0;
                })
                .sum();
            
            long healthyInstances = instances.stream()
                .mapToLong(instance -> isInstanceHealthy(instance) ? 1 : 0)
                .sum();
            
            double averageResponseTime = instances.stream()
                .mapToDouble(instance -> {
                    ServiceMetrics metrics = serviceMetrics.get(instance.getId());
                    return metrics != null ? metrics.getAverageResponseTime() : 0.0;
                })
                .average()
                .orElse(0.0);
            
            serviceStats.put(serviceName, ServiceStats.builder()
                .serviceName(serviceName)
                .totalInstances(instances.size())
                .healthyInstances((int) healthyInstances)
                .totalRequests(totalRequests)
                .averageResponseTime(averageResponseTime)
                .build());
        }
        
        return LoadBalancerStats.builder()
            .serviceStats(serviceStats)
            .totalServices(serviceRegistry.size())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Get circuit breaker status for all instances
     */
    public Map<String, CircuitBreakerStatus> getCircuitBreakerStatus() {
        Map<String, CircuitBreakerStatus> status = new HashMap<>();
        
        for (Map.Entry<String, CircuitBreakerState> entry : circuitBreakers.entrySet()) {
            String instanceId = entry.getKey();
            CircuitBreakerState state = entry.getValue();
            
            status.put(instanceId, CircuitBreakerStatus.builder()
                .instanceId(instanceId)
                .state(state.isOpen() ? "OPEN" : state.isHalfOpen() ? "HALF_OPEN" : "CLOSED")
                .failureCount(state.getFailureCount())
                .lastFailureTime(state.getLastFailureTime())
                .build());
        }
        
        return status;
    }

    // Private helper methods

    private ServiceInstance selectRoundRobin(String serviceName, List<ServiceInstance> instances) {
        AtomicInteger counter = roundRobinCounters.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
        int index = counter.getAndIncrement() % instances.size();
        return instances.get(index);
    }

    private ServiceInstance selectLeastConnections(List<ServiceInstance> instances) {
        return instances.stream()
            .min(Comparator.comparingInt(instance -> {
                ServiceMetrics metrics = serviceMetrics.get(instance.getId());
                return metrics != null ? metrics.getActiveConnections() : 0;
            }))
            .orElse(instances.get(0));
    }

    private ServiceInstance selectWeightedResponseTime(List<ServiceInstance> instances) {
        // Select instance with best weight (lower response time = higher weight)
        return instances.stream()
            .min(Comparator.comparingDouble(instance -> {
                ServiceMetrics metrics = serviceMetrics.get(instance.getId());
                double responseTime = metrics != null ? metrics.getAverageResponseTime() : Double.MAX_VALUE;
                return responseTime * (1.0 + metrics.getErrorRate()); // Factor in error rate
            }))
            .orElse(instances.get(0));
    }

    private ServiceInstance selectLeastResponseTime(List<ServiceInstance> instances) {
        return instances.stream()
            .min(Comparator.comparingDouble(instance -> {
                ServiceMetrics metrics = serviceMetrics.get(instance.getId());
                return metrics != null ? metrics.getAverageResponseTime() : Double.MAX_VALUE;
            }))
            .orElse(instances.get(0));
    }

    private ServiceInstance selectRandom(List<ServiceInstance> instances) {
        Random random = new Random();
        return instances.get(random.nextInt(instances.size()));
    }

    private ServiceInstance selectConsistentHash(List<ServiceInstance> instances, String key) {
        // Simple hash-based selection
        int hash = Math.abs(key.hashCode());
        int index = hash % instances.size();
        return instances.get(index);
    }

    private boolean isInstanceHealthy(ServiceInstance instance) {
        return instance.isHealthy();
    }

    private boolean isCircuitBreakerOpen(String instanceId) {
        CircuitBreakerState circuitBreaker = circuitBreakers.get(instanceId);
        return circuitBreaker != null && circuitBreaker.isOpen();
    }

    private CircuitBreakerState getCircuitBreaker(String instanceId) {
        return circuitBreakers.computeIfAbsent(instanceId, 
            k -> new CircuitBreakerState(circuitBreakerFailureThreshold, circuitBreakerTimeoutMinutes));
    }

    private void performHealthCheck(String serviceName, ServiceInstance instance) {
        try {
            String healthUrl = instance.getBaseUrl() + "/actuator/health";
            long startTime = System.currentTimeMillis();
            
            restTemplate.getForEntity(healthUrl, Object.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            instance.setHealthy(true);
            instance.setLastHealthCheck(LocalDateTime.now());
            
            // Record successful health check
            recordRequest(instance.getId(), responseTime, true);
            
            log.debug("Health check successful for instance: {} ({}ms)", instance.getId(), responseTime);
            
        } catch (Exception e) {
            instance.setHealthy(false);
            instance.setLastHealthCheck(LocalDateTime.now());
            
            // Record failed health check
            recordRequest(instance.getId(), 0, false);
            
            log.warn("Health check failed for instance: {} - {}", instance.getId(), e.getMessage());
        }
    }
}
