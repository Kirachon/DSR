package ph.gov.dsr.interoperability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import ph.gov.dsr.interoperability.dto.ApiGatewayRequest;
import ph.gov.dsr.interoperability.dto.ApiGatewayResponse;
import ph.gov.dsr.interoperability.entity.ExternalSystemIntegration;
import ph.gov.dsr.interoperability.repository.ExternalSystemIntegrationRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * API Gateway Service for routing requests to external systems
 * Handles authentication, rate limiting, monitoring, and error handling
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiGatewayService {

    private final RestTemplate restTemplate;
    private final ExternalSystemIntegrationRepository systemRepository;
    private final Map<String, List<LocalDateTime>> rateLimitTracker = new ConcurrentHashMap<>();

    /**
     * Route request to external system
     */
    public ApiGatewayResponse routeRequest(ApiGatewayRequest request) {
        log.info("Routing request to system: {} endpoint: {}", request.getSystemCode(), request.getEndpoint());
        
        try {
            // Get system configuration
            Optional<ExternalSystemIntegration> systemOpt = systemRepository.findBySystemCode(request.getSystemCode());
            if (systemOpt.isEmpty()) {
                return createErrorResponse("SYSTEM_NOT_FOUND", "External system not found: " + request.getSystemCode());
            }
            
            ExternalSystemIntegration system = systemOpt.get();
            
            // Check system availability
            if (!system.isAvailable()) {
                return createErrorResponse("SYSTEM_UNAVAILABLE", "System is not available: " + system.getSystemName());
            }
            
            // Check rate limits
            if (isRateLimitExceeded(system)) {
                return createErrorResponse("RATE_LIMIT_EXCEEDED", "Rate limit exceeded for system: " + system.getSystemName());
            }
            
            // Build request URL
            String url = buildRequestUrl(system, request.getEndpoint());
            
            // Prepare headers with authentication
            HttpHeaders headers = prepareHeaders(system, request.getHeaders());
            
            // Create HTTP entity
            HttpEntity<Object> entity = new HttpEntity<>(request.getBody(), headers);
            
            // Execute request with timing
            long startTime = System.currentTimeMillis();
            ResponseEntity<Object> response = executeRequest(url, request.getMethod(), entity);
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Record successful call
            system.recordSuccessfulCall(responseTime);
            systemRepository.save(system);
            
            // Track rate limit
            trackRateLimit(system.getSystemCode());
            
            log.info("Successfully routed request to {} in {}ms", system.getSystemName(), responseTime);
            
            return ApiGatewayResponse.builder()
                    .success(true)
                    .statusCode(response.getStatusCode().value())
                    .headers(convertHeaders(response.getHeaders()))
                    .body(response.getBody())
                    .responseTime(responseTime)
                    .systemCode(system.getSystemCode())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (HttpClientErrorException e) {
            log.error("HTTP error routing request to {}: {} - {}", 
                     request.getSystemCode(), e.getStatusCode(), e.getResponseBodyAsString());
            
            recordFailedCall(request.getSystemCode());
            
            return ApiGatewayResponse.builder()
                    .success(false)
                    .statusCode(e.getStatusCode().value())
                    .errorCode("HTTP_ERROR")
                    .errorMessage(e.getMessage())
                    .body(e.getResponseBodyAsString())
                    .systemCode(request.getSystemCode())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (ResourceAccessException e) {
            log.error("Connection error routing request to {}: {}", request.getSystemCode(), e.getMessage());
            
            recordFailedCall(request.getSystemCode());
            
            return createErrorResponse("CONNECTION_ERROR", "Failed to connect to system: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error routing request to {}: {}", request.getSystemCode(), e.getMessage(), e);
            
            recordFailedCall(request.getSystemCode());
            
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Check system health
     */
    public Map<String, Object> checkSystemHealth(String systemCode) {
        log.info("Checking health for system: {}", systemCode);
        
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            Optional<ExternalSystemIntegration> systemOpt = systemRepository.findBySystemCode(systemCode);
            if (systemOpt.isEmpty()) {
                healthStatus.put("status", "NOT_FOUND");
                healthStatus.put("message", "System not found");
                return healthStatus;
            }
            
            ExternalSystemIntegration system = systemOpt.get();
            
            // Perform health check
            String healthUrl = system.getBaseUrl() + "/health";
            long startTime = System.currentTimeMillis();
            
            try {
                ResponseEntity<Object> response = restTemplate.getForEntity(healthUrl, Object.class);
                long responseTime = System.currentTimeMillis() - startTime;
                
                healthStatus.put("status", "HEALTHY");
                healthStatus.put("responseTime", responseTime);
                healthStatus.put("httpStatus", response.getStatusCode().value());
                healthStatus.put("lastChecked", LocalDateTime.now());
                
                // Update system health check timestamp
                system.setLastHealthCheck(LocalDateTime.now());
                system.setStatus(ExternalSystemIntegration.SystemStatus.ACTIVE);
                systemRepository.save(system);
                
            } catch (Exception e) {
                healthStatus.put("status", "UNHEALTHY");
                healthStatus.put("error", e.getMessage());
                healthStatus.put("lastChecked", LocalDateTime.now());
                
                // Update system status
                system.setLastHealthCheck(LocalDateTime.now());
                system.setStatus(ExternalSystemIntegration.SystemStatus.ERROR);
                systemRepository.save(system);
            }
            
            // Add system information
            healthStatus.put("systemName", system.getSystemName());
            healthStatus.put("organization", system.getOrganization());
            healthStatus.put("successRate", system.getSuccessRate());
            healthStatus.put("averageResponseTime", system.getAverageResponseTimeMs());
            
        } catch (Exception e) {
            log.error("Error checking system health for {}: {}", systemCode, e.getMessage(), e);
            healthStatus.put("status", "ERROR");
            healthStatus.put("error", e.getMessage());
        }
        
        return healthStatus;
    }

    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStatistics(String systemCode) {
        log.info("Getting statistics for system: {}", systemCode);
        
        Optional<ExternalSystemIntegration> systemOpt = systemRepository.findBySystemCode(systemCode);
        if (systemOpt.isEmpty()) {
            return Map.of("error", "System not found");
        }
        
        ExternalSystemIntegration system = systemOpt.get();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("systemCode", system.getSystemCode());
        stats.put("systemName", system.getSystemName());
        stats.put("organization", system.getOrganization());
        stats.put("status", system.getStatus());
        stats.put("isActive", system.getIsActive());
        stats.put("totalSuccessfulCalls", system.getTotalSuccessfulCalls());
        stats.put("totalFailedCalls", system.getTotalFailedCalls());
        stats.put("successRate", system.getSuccessRate());
        stats.put("averageResponseTime", system.getAverageResponseTimeMs());
        stats.put("lastSuccessfulCall", system.getLastSuccessfulCall());
        stats.put("lastFailedCall", system.getLastFailedCall());
        stats.put("lastHealthCheck", system.getLastHealthCheck());
        stats.put("uptimePercentage", system.getUptimePercentage());
        
        return stats;
    }

    /**
     * List all registered systems
     */
    public List<Map<String, Object>> listSystems() {
        log.info("Listing all registered systems");
        
        List<ExternalSystemIntegration> systems = systemRepository.findAll();
        List<Map<String, Object>> systemList = new ArrayList<>();
        
        for (ExternalSystemIntegration system : systems) {
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("systemCode", system.getSystemCode());
            systemInfo.put("systemName", system.getSystemName());
            systemInfo.put("organization", system.getOrganization());
            systemInfo.put("systemType", system.getSystemType());
            systemInfo.put("integrationType", system.getIntegrationType());
            systemInfo.put("status", system.getStatus());
            systemInfo.put("isActive", system.getIsActive());
            systemInfo.put("isHealthy", system.isHealthy());
            systemInfo.put("successRate", system.getSuccessRate());
            systemInfo.put("lastHealthCheck", system.getLastHealthCheck());
            
            systemList.add(systemInfo);
        }
        
        return systemList;
    }

    // Helper methods
    
    private boolean isRateLimitExceeded(ExternalSystemIntegration system) {
        String systemCode = system.getSystemCode();
        LocalDateTime now = LocalDateTime.now();
        
        List<LocalDateTime> calls = rateLimitTracker.getOrDefault(systemCode, new ArrayList<>());
        
        // Clean old calls (older than 1 day)
        calls.removeIf(callTime -> callTime.isBefore(now.minusDays(1)));
        
        // Count calls in different time windows
        int callsInLastMinute = (int) calls.stream()
                .filter(callTime -> callTime.isAfter(now.minusMinutes(1)))
                .count();
        
        int callsInLastHour = (int) calls.stream()
                .filter(callTime -> callTime.isAfter(now.minusHours(1)))
                .count();
        
        int callsInLastDay = calls.size();
        
        return system.isRateLimitExceeded(callsInLastMinute, callsInLastHour, callsInLastDay);
    }

    private void trackRateLimit(String systemCode) {
        rateLimitTracker.computeIfAbsent(systemCode, k -> new ArrayList<>()).add(LocalDateTime.now());
    }

    private String buildRequestUrl(ExternalSystemIntegration system, String endpoint) {
        String baseUrl = system.getBaseUrl();
        if (baseUrl.endsWith("/") && endpoint.startsWith("/")) {
            return baseUrl + endpoint.substring(1);
        } else if (!baseUrl.endsWith("/") && !endpoint.startsWith("/")) {
            return baseUrl + "/" + endpoint;
        } else {
            return baseUrl + endpoint;
        }
    }

    private HttpHeaders prepareHeaders(ExternalSystemIntegration system, Map<String, String> requestHeaders) {
        HttpHeaders headers = new HttpHeaders();
        
        // Add request headers
        if (requestHeaders != null) {
            requestHeaders.forEach(headers::add);
        }
        
        // Add authentication headers
        String authType = system.getAuthenticationType();
        if ("API_KEY".equals(authType) && system.getApiKey() != null) {
            headers.add("X-API-Key", system.getApiKey());
        } else if ("BEARER".equals(authType) && system.getApiKey() != null) {
            headers.add("Authorization", "Bearer " + system.getApiKey());
        }
        
        // Set content type if not specified
        if (!headers.containsKey("Content-Type")) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        
        return headers;
    }

    private ResponseEntity<Object> executeRequest(String url, String method, HttpEntity<Object> entity) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        return restTemplate.exchange(url, httpMethod, entity, Object.class);
    }

    private Map<String, String> convertHeaders(HttpHeaders headers) {
        Map<String, String> headerMap = new HashMap<>();
        headers.forEach((key, values) -> {
            if (!values.isEmpty()) {
                headerMap.put(key, values.get(0));
            }
        });
        return headerMap;
    }

    private void recordFailedCall(String systemCode) {
        systemRepository.findBySystemCode(systemCode).ifPresent(system -> {
            system.recordFailedCall();
            systemRepository.save(system);
        });
    }

    private ApiGatewayResponse createErrorResponse(String errorCode, String errorMessage) {
        return ApiGatewayResponse.builder()
                .success(false)
                .statusCode(500)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Enhanced API gateway with circuit breaker and retry mechanisms
     */
    public ApiGatewayResponse routeRequestWithResilience(ApiGatewayRequest request) {
        log.info("Routing request with resilience patterns to system: {}", request.getSystemCode());

        return executeWithCircuitBreaker(request.getSystemCode(), () -> {
            return executeWithRetry(request, 3);
        });
    }

    private ApiGatewayResponse executeWithRetry(ApiGatewayRequest request, int maxRetries) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.debug("Attempt {} of {} for system: {}", attempt, maxRetries, request.getSystemCode());
                return routeRequest(request);

            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for system {}: {}", attempt, request.getSystemCode(), e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        // Exponential backoff
                        long delay = (long) Math.pow(2, attempt - 1) * 1000; // 1s, 2s, 4s
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("All {} attempts failed for system: {}", maxRetries, request.getSystemCode());
        return createErrorResponse("RETRY_EXHAUSTED",
            "Failed after " + maxRetries + " attempts: " +
            (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    private ApiGatewayResponse executeWithCircuitBreaker(String systemCode,
                                                       java.util.function.Supplier<ApiGatewayResponse> operation) {
        // Simple circuit breaker implementation
        CircuitBreakerState state = getCircuitBreakerState(systemCode);

        if (state.isOpen()) {
            log.warn("Circuit breaker is OPEN for system: {}", systemCode);
            return createErrorResponse("CIRCUIT_BREAKER_OPEN",
                "Circuit breaker is open for system: " + systemCode);
        }

        try {
            ApiGatewayResponse response = operation.get();

            if (response.isSuccess()) {
                state.recordSuccess();
            } else {
                state.recordFailure();
            }

            return response;

        } catch (Exception e) {
            state.recordFailure();
            throw e;
        }
    }

    private CircuitBreakerState getCircuitBreakerState(String systemCode) {
        return circuitBreakerStates.computeIfAbsent(systemCode, k -> new CircuitBreakerState());
    }

    // Circuit breaker state tracking
    private final Map<String, CircuitBreakerState> circuitBreakerStates = new ConcurrentHashMap<>();

    private static class CircuitBreakerState {
        private int failureCount = 0;
        private LocalDateTime lastFailureTime;
        private boolean isOpen = false;
        private final int failureThreshold = 5;
        private final long timeoutMinutes = 5;

        public boolean isOpen() {
            if (isOpen && lastFailureTime != null) {
                // Check if timeout period has passed
                if (LocalDateTime.now().isAfter(lastFailureTime.plusMinutes(timeoutMinutes))) {
                    isOpen = false;
                    failureCount = 0;
                    log.info("Circuit breaker transitioning to HALF_OPEN state");
                }
            }
            return isOpen;
        }

        public void recordSuccess() {
            failureCount = 0;
            isOpen = false;
            lastFailureTime = null;
        }

        public void recordFailure() {
            failureCount++;
            lastFailureTime = LocalDateTime.now();

            if (failureCount >= failureThreshold) {
                isOpen = true;
                log.warn("Circuit breaker OPENED after {} failures", failureCount);
            }
        }
    }

    /**
     * Batch health check for multiple systems
     */
    public Map<String, Map<String, Object>> batchHealthCheck(List<String> systemCodes) {
        log.info("Performing batch health check for {} systems", systemCodes.size());

        Map<String, Map<String, Object>> results = new ConcurrentHashMap<>();

        // Execute health checks in parallel
        systemCodes.parallelStream().forEach(systemCode -> {
            try {
                Map<String, Object> healthStatus = checkSystemHealth(systemCode);
                results.put(systemCode, healthStatus);
            } catch (Exception e) {
                Map<String, Object> errorStatus = new HashMap<>();
                errorStatus.put("status", "ERROR");
                errorStatus.put("error", e.getMessage());
                errorStatus.put("lastChecked", LocalDateTime.now());
                results.put(systemCode, errorStatus);
            }
        });

        return results;
    }

    /**
     * Advanced request routing with load balancing
     */
    public ApiGatewayResponse routeWithLoadBalancing(ApiGatewayRequest request) {
        log.info("Routing request with load balancing for system type: {}", request.getSystemCode());

        // Find all healthy systems of the same type
        List<ExternalSystemIntegration> healthySystems = findHealthySystemsByType(request.getSystemCode());

        if (healthySystems.isEmpty()) {
            return createErrorResponse("NO_HEALTHY_SYSTEMS",
                "No healthy systems available for type: " + request.getSystemCode());
        }

        // Select system using round-robin load balancing
        ExternalSystemIntegration selectedSystem = selectSystemWithLoadBalancing(healthySystems);

        // Update request with selected system
        ApiGatewayRequest routedRequest = new ApiGatewayRequest();
        routedRequest.setSystemCode(selectedSystem.getSystemCode());
        routedRequest.setEndpoint(request.getEndpoint());
        routedRequest.setMethod(request.getMethod());
        routedRequest.setHeaders(request.getHeaders());
        routedRequest.setBody(request.getBody());
        routedRequest.setTimeoutSeconds(request.getTimeoutSeconds());

        return routeRequestWithResilience(routedRequest);
    }

    private List<ExternalSystemIntegration> findHealthySystemsByType(String systemType) {
        return systemRepository.findByIsActiveTrue().stream()
            .filter(system -> system.isHealthy() && system.getSystemType().name().equals(systemType))
            .sorted((s1, s2) -> {
                // Sort by health score (success rate + response time)
                double score1 = calculateHealthScore(s1);
                double score2 = calculateHealthScore(s2);
                return Double.compare(score2, score1); // Higher score first
            })
            .collect(java.util.stream.Collectors.toList());
    }

    private ExternalSystemIntegration selectSystemWithLoadBalancing(List<ExternalSystemIntegration> systems) {
        if (systems.isEmpty()) {
            return null;
        }

        // Weighted load balancing based on system health and capacity
        return selectSystemWithWeightedLoadBalancing(systems);
    }

    /**
     * Advanced weighted load balancing algorithm
     */
    private ExternalSystemIntegration selectSystemWithWeightedLoadBalancing(List<ExternalSystemIntegration> systems) {
        // Calculate weights based on health score and inverse response time
        Map<ExternalSystemIntegration, Double> weights = new HashMap<>();
        double totalWeight = 0.0;

        for (ExternalSystemIntegration system : systems) {
            double healthScore = calculateHealthScore(system);
            double responseTimeFactor = 1.0 / Math.max(system.getAverageResponseTimeMs(), 1.0);
            double weight = healthScore * responseTimeFactor * 100; // Scale up for better precision

            weights.put(system, weight);
            totalWeight += weight;
        }

        // Select system based on weighted random selection
        double random = Math.random() * totalWeight;
        double currentWeight = 0.0;

        for (Map.Entry<ExternalSystemIntegration, Double> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (random <= currentWeight) {
                log.debug("Selected system {} with weight {} (health score: {})",
                         entry.getKey().getSystemCode(), entry.getValue(),
                         calculateHealthScore(entry.getKey()));
                return entry.getKey();
            }
        }

        // Fallback to first system if selection fails
        return systems.get(0);
    }

    /**
     * Calculate health score for load balancing decisions
     */
    private double calculateHealthScore(ExternalSystemIntegration system) {
        double successRate = system.getSuccessRate();
        double uptimePercentage = system.getUptimePercentage();
        double responseTimeFactor = Math.max(0.1, 1.0 - (system.getAverageResponseTimeMs() / 10000.0)); // Normalize to 0.1-1.0

        // Weighted health score: 50% success rate, 30% uptime, 20% response time
        return (successRate * 0.5) + (uptimePercentage * 0.3) + (responseTimeFactor * 0.2);
    }

    /**
     * Advanced routing with path-based routing and request transformation
     */
    public ApiGatewayResponse routeWithAdvancedRouting(ApiGatewayRequest request) {
        log.info("Routing request with advanced routing for system: {} path: {}",
                request.getSystemCode(), request.getEndpoint());

        try {
            // Apply request transformation rules
            ApiGatewayRequest transformedRequest = applyRequestTransformation(request);

            // Determine routing strategy based on path and system type
            RoutingStrategy strategy = determineRoutingStrategy(transformedRequest);

            switch (strategy) {
                case LOAD_BALANCED:
                    return routeWithLoadBalancing(transformedRequest);
                case FAILOVER:
                    return routeWithFailover(transformedRequest);
                case DIRECT:
                    return routeRequestWithResilience(transformedRequest);
                case BROADCAST:
                    return routeWithBroadcast(transformedRequest);
                default:
                    return routeRequestWithResilience(transformedRequest);
            }

        } catch (Exception e) {
            log.error("Error in advanced routing for system: {}", request.getSystemCode(), e);
            return createErrorResponse("ROUTING_ERROR", "Advanced routing failed: " + e.getMessage());
        }
    }

    /**
     * Apply request transformation rules
     */
    private ApiGatewayRequest applyRequestTransformation(ApiGatewayRequest request) {
        ApiGatewayRequest transformed = new ApiGatewayRequest();
        transformed.setSystemCode(request.getSystemCode());
        transformed.setMethod(request.getMethod());
        transformed.setHeaders(request.getHeaders());
        transformed.setBody(request.getBody());
        transformed.setTimeoutSeconds(request.getTimeoutSeconds());

        // Transform endpoint based on system-specific rules
        String transformedEndpoint = transformEndpoint(request.getSystemCode(), request.getEndpoint());
        transformed.setEndpoint(transformedEndpoint);

        // Add system-specific headers
        Map<String, String> enhancedHeaders = enhanceHeaders(request.getSystemCode(), request.getHeaders());
        transformed.setHeaders(enhancedHeaders);

        return transformed;
    }

    /**
     * Transform endpoint based on system-specific rules
     */
    private String transformEndpoint(String systemCode, String endpoint) {
        // Apply system-specific endpoint transformations
        switch (systemCode.toUpperCase()) {
            case "PHILSYS":
                return endpoint.startsWith("/api/") ? endpoint : "/api/v2" + endpoint;
            case "SSS":
                return endpoint.startsWith("/sss/") ? endpoint : "/sss/api/v1" + endpoint;
            case "GSIS":
                return endpoint.startsWith("/gsis/") ? endpoint : "/gsis/api/v1" + endpoint;
            case "PHILHEALTH":
                return endpoint.startsWith("/philhealth/") ? endpoint : "/philhealth/api/v1" + endpoint;
            case "DOH":
                return endpoint.startsWith("/fhir/") ? endpoint : "/fhir/R4" + endpoint;
            default:
                return endpoint;
        }
    }

    /**
     * Enhance headers with system-specific requirements
     */
    private Map<String, String> enhanceHeaders(String systemCode, Map<String, String> originalHeaders) {
        Map<String, String> enhanced = new HashMap<>(originalHeaders != null ? originalHeaders : new HashMap<>());

        // Add common headers
        enhanced.put("X-DSR-Gateway", "true");
        enhanced.put("X-DSR-Timestamp", LocalDateTime.now().toString());
        enhanced.put("X-DSR-Request-ID", java.util.UUID.randomUUID().toString());

        // Add system-specific headers
        switch (systemCode.toUpperCase()) {
            case "DOH":
                enhanced.put("Accept", "application/fhir+json");
                enhanced.put("Content-Type", "application/fhir+json");
                break;
            case "PHILSYS":
                enhanced.put("X-PhilSys-Version", "2.0");
                break;
            case "SSS":
                enhanced.put("X-SSS-Client", "DSR-Gateway");
                break;
        }

        return enhanced;
    }

    /**
     * Determine routing strategy based on request characteristics
     */
    private RoutingStrategy determineRoutingStrategy(ApiGatewayRequest request) {
        String systemCode = request.getSystemCode().toUpperCase();
        String endpoint = request.getEndpoint();

        // Critical systems use failover strategy
        if (systemCode.contains("PHILSYS") || systemCode.contains("SSS") || systemCode.contains("GSIS")) {
            return RoutingStrategy.FAILOVER;
        }

        // Batch operations use load balancing
        if (endpoint.contains("/batch") || endpoint.contains("/bulk")) {
            return RoutingStrategy.LOAD_BALANCED;
        }

        // Notification endpoints use broadcast
        if (endpoint.contains("/notify") || endpoint.contains("/broadcast")) {
            return RoutingStrategy.BROADCAST;
        }

        // Default to direct routing
        return RoutingStrategy.DIRECT;
    }

    /**
     * Route with failover capability
     */
    private ApiGatewayResponse routeWithFailover(ApiGatewayRequest request) {
        log.info("Routing with failover for system: {}", request.getSystemCode());

        List<ExternalSystemIntegration> systems = findHealthySystemsByType(request.getSystemCode());

        if (systems.isEmpty()) {
            return createErrorResponse("NO_SYSTEMS_AVAILABLE",
                "No systems available for failover: " + request.getSystemCode());
        }

        // Try each system in order of health score
        for (ExternalSystemIntegration system : systems) {
            try {
                ApiGatewayRequest systemRequest = new ApiGatewayRequest();
                systemRequest.setSystemCode(system.getSystemCode());
                systemRequest.setEndpoint(request.getEndpoint());
                systemRequest.setMethod(request.getMethod());
                systemRequest.setHeaders(request.getHeaders());
                systemRequest.setBody(request.getBody());
                systemRequest.setTimeoutSeconds(request.getTimeoutSeconds());

                ApiGatewayResponse response = routeRequestWithResilience(systemRequest);

                if (response.isSuccess()) {
                    log.info("Failover successful using system: {}", system.getSystemCode());
                    return response;
                }

                log.warn("Failover attempt failed for system: {} - trying next", system.getSystemCode());

            } catch (Exception e) {
                log.warn("Failover attempt error for system: {} - {}", system.getSystemCode(), e.getMessage());
            }
        }

        return createErrorResponse("FAILOVER_EXHAUSTED",
            "All failover attempts failed for system type: " + request.getSystemCode());
    }

    /**
     * Route with broadcast to multiple systems
     */
    private ApiGatewayResponse routeWithBroadcast(ApiGatewayRequest request) {
        log.info("Broadcasting request to multiple systems for type: {}", request.getSystemCode());

        List<ExternalSystemIntegration> systems = findHealthySystemsByType(request.getSystemCode());

        if (systems.isEmpty()) {
            return createErrorResponse("NO_SYSTEMS_AVAILABLE",
                "No systems available for broadcast: " + request.getSystemCode());
        }

        List<ApiGatewayResponse> responses = new ArrayList<>();
        List<CompletableFuture<ApiGatewayResponse>> futures = new ArrayList<>();

        // Execute requests in parallel
        for (ExternalSystemIntegration system : systems) {
            CompletableFuture<ApiGatewayResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    ApiGatewayRequest systemRequest = new ApiGatewayRequest();
                    systemRequest.setSystemCode(system.getSystemCode());
                    systemRequest.setEndpoint(request.getEndpoint());
                    systemRequest.setMethod(request.getMethod());
                    systemRequest.setHeaders(request.getHeaders());
                    systemRequest.setBody(request.getBody());
                    systemRequest.setTimeoutSeconds(request.getTimeoutSeconds());

                    return routeRequestWithResilience(systemRequest);

                } catch (Exception e) {
                    log.error("Broadcast error for system: {}", system.getSystemCode(), e);
                    return createErrorResponse("BROADCAST_ERROR",
                        "Broadcast failed for system: " + system.getSystemCode() + " - " + e.getMessage());
                }
            });

            futures.add(future);
        }

        // Wait for all responses
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, java.util.concurrent.TimeUnit.SECONDS);

            for (CompletableFuture<ApiGatewayResponse> future : futures) {
                responses.add(future.get());
            }

        } catch (Exception e) {
            log.error("Broadcast timeout or error", e);
            return createErrorResponse("BROADCAST_TIMEOUT", "Broadcast operation timed out or failed");
        }

        // Aggregate responses
        return aggregateBroadcastResponses(responses);
    }

    /**
     * Aggregate multiple broadcast responses
     */
    private ApiGatewayResponse aggregateBroadcastResponses(List<ApiGatewayResponse> responses) {
        int successCount = 0;
        int totalCount = responses.size();
        List<String> errors = new ArrayList<>();

        for (ApiGatewayResponse response : responses) {
            if (response.isSuccess()) {
                successCount++;
            } else {
                errors.add(response.getErrorMessage());
            }
        }

        boolean overallSuccess = successCount > 0; // At least one success

        Map<String, Object> aggregatedBody = new HashMap<>();
        aggregatedBody.put("totalSystems", totalCount);
        aggregatedBody.put("successfulSystems", successCount);
        aggregatedBody.put("failedSystems", totalCount - successCount);
        aggregatedBody.put("successRate", (double) successCount / totalCount);

        if (!errors.isEmpty()) {
            aggregatedBody.put("errors", errors);
        }

        return ApiGatewayResponse.builder()
                .success(overallSuccess)
                .statusCode(overallSuccess ? 200 : 207) // 207 Multi-Status for partial success
                .body(aggregatedBody)
                .responseTime(0L) // Aggregate timing would be complex
                .systemCode("BROADCAST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Routing strategy enumeration
     */
    private enum RoutingStrategy {
        DIRECT,
        LOAD_BALANCED,
        FAILOVER,
        BROADCAST
    }
}
