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
}
