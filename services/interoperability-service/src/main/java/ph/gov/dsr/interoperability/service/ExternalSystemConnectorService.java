package ph.gov.dsr.interoperability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ph.gov.dsr.interoperability.dto.ApiGatewayRequest;
import ph.gov.dsr.interoperability.dto.ApiGatewayResponse;
import ph.gov.dsr.interoperability.entity.ExternalSystemIntegration;
import ph.gov.dsr.interoperability.repository.ExternalSystemIntegrationRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * External System Connector Service for integrating with government systems
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalSystemConnectorService {

    private final ExternalSystemIntegrationRepository systemRepository;
    private final RestTemplate restTemplate;
    
    // Connection pools and caching
    private final Map<String, Object> responseCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();
    private final long CACHE_TTL_MINUTES = 5;

    // Advanced retry and monitoring
    private final Map<String, RetryConfiguration> retryConfigurations = new ConcurrentHashMap<>();
    private final Map<String, SystemHealthMetrics> healthMetrics = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastRetryAttempts = new ConcurrentHashMap<>();

    /**
     * Connect to external government systems with comprehensive error handling
     */
    public ApiGatewayResponse connectToGovernmentSystem(String systemCode, ApiGatewayRequest request) {
        // Input validation
        if (systemCode == null || systemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("System code cannot be null or empty");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        log.info("Connecting to government system: {} for endpoint: {}", systemCode, request.getEndpoint());

        try {
            // Get system configuration
            Optional<ExternalSystemIntegration> systemOpt = systemRepository.findBySystemCode(systemCode);
            if (systemOpt.isEmpty()) {
                return createErrorResponse(systemCode, "SYSTEM_NOT_FOUND", "System not configured: " + systemCode);
            }

            ExternalSystemIntegration system = systemOpt.get();

            // Validate system is active and healthy
            if (!system.getIsActive()) {
                return createErrorResponse(systemCode, "SYSTEM_INACTIVE", "System is not active: " + systemCode);
            }
            
            // Check cache first for GET requests
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                ApiGatewayResponse cachedResponse = getCachedResponse(systemCode, request.getEndpoint());
                if (cachedResponse != null) {
                    log.debug("Returning cached response for {}: {}", systemCode, request.getEndpoint());
                    return cachedResponse;
                }
            }
            
            // Execute request based on system type
            ApiGatewayResponse response = executeSystemRequest(system, request);
            
            // Cache successful GET responses
            if (response.isSuccess() && "GET".equalsIgnoreCase(request.getMethod())) {
                cacheResponse(systemCode, request.getEndpoint(), response);
            }
            
            // Update system health status
            updateSystemHealth(system, true);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error connecting to government system {}: {}", systemCode, e.getMessage(), e);
            
            // Update system health status
            systemRepository.findBySystemCode(systemCode)
                .ifPresent(system -> updateSystemHealth(system, false));
            
            return createErrorResponse(systemCode, "CONNECTION_ERROR", "Failed to connect to system: " + e.getMessage());
        }
    }
    
    /**
     * Execute request based on system type with specific protocols
     */
    private ApiGatewayResponse executeSystemRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        // Route based on system code for specific government systems
        String systemCode = system.getSystemCode().toUpperCase();

        if (systemCode.contains("PHILSYS")) {
            return executePhilSysRequest(system, request);
        } else if (systemCode.contains("SSS")) {
            return executeSSSRequest(system, request);
        } else if (systemCode.contains("GSIS")) {
            return executeGSISRequest(system, request);
        } else if (systemCode.contains("PAGIBIG") || systemCode.contains("PAG-IBIG")) {
            return executePagIbigRequest(system, request);
        } else if (systemCode.contains("PHILHEALTH")) {
            return executePhilHealthRequest(system, request);
        } else if (systemCode.contains("BIR")) {
            return executeBIRRequest(system, request);
        } else if (systemCode.contains("BSP")) {
            return executeBSPRequest(system, request);
        } else if (systemCode.contains("LGU")) {
            return executeLGUSystemRequest(system, request);
        } else if (systemCode.contains("DEPED")) {
            return executeDepEdRequest(system, request);
        } else if (systemCode.contains("DOH")) {
            return executeDOHRequest(system, request);
        } else if (systemCode.contains("DOLE")) {
            return executeDOLERequest(system, request);
        } else if (systemCode.contains("LRA")) {
            return executeLRARequest(system, request);
        } else if (systemCode.contains("CDA")) {
            return executeCDARequest(system, request);
        } else {
            return executeGenericRequest(system, request);
        }
    }
    
    /**
     * PhilSys (Philippine Identification System) integration
     */
    private ApiGatewayResponse executePhilSysRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing PhilSys request for endpoint: {}", request.getEndpoint());
        
        try {
            // Build PhilSys-specific headers
            HttpHeaders headers = buildPhilSysHeaders(system, request);
            
            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();
            
            // Execute request with PhilSys-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createResponseFromHttpResponse(system.getSystemCode(), response, "PhilSys request successful");

        } catch (Exception e) {
            log.error("PhilSys request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "PHILSYS_ERROR", "PhilSys integration failed: " + e.getMessage());
        }
    }
    
    /**
     * SSS (Social Security System) integration
     */
    private ApiGatewayResponse executeSSSRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing SSS request for endpoint: {}", request.getEndpoint());
        
        try {
            // Build SSS-specific headers with authentication
            HttpHeaders headers = buildSSSHeaders(system, request);
            
            // Build request URL with SSS API versioning
            String url = system.getBaseUrl() + "/api/v1" + request.getEndpoint();
            
            // Execute request with SSS-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createSuccessResponse(system.getSystemCode(), response.getBody(), "SSS request successful");

        } catch (Exception e) {
            log.error("SSS request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "SSS_ERROR", "SSS integration failed: " + e.getMessage());
        }
    }
    
    /**
     * GSIS (Government Service Insurance System) integration
     */
    private ApiGatewayResponse executeGSISRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing GSIS request for endpoint: {}", request.getEndpoint());
        
        try {
            // Build GSIS-specific headers
            HttpHeaders headers = buildGSISHeaders(system, request);
            
            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();
            
            // Execute request with GSIS-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );

            return createSuccessResponse(system.getSystemCode(), response.getBody(), "GSIS request successful");

        } catch (Exception e) {
            log.error("GSIS request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "GSIS_ERROR", "GSIS integration failed: " + e.getMessage());
        }
    }

    /**
     * Pag-IBIG Fund integration
     */
    private ApiGatewayResponse executePagIbigRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing Pag-IBIG request for endpoint: {}", request.getEndpoint());

        try {
            // Build Pag-IBIG-specific headers
            HttpHeaders headers = buildPagIbigHeaders(system, request);

            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();

            // Execute request with Pag-IBIG-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createSuccessResponse(system.getSystemCode(), response.getBody(), "Pag-IBIG request successful");
            
        } catch (Exception e) {
            log.error("Pag-IBIG request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "PAGIBIG_ERROR", "Pag-IBIG integration failed: " + e.getMessage());
        }
    }
    
    /**
     * PhilHealth integration
     */
    private ApiGatewayResponse executePhilHealthRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing PhilHealth request for endpoint: {}", request.getEndpoint());
        
        try {
            // Build PhilHealth-specific headers
            HttpHeaders headers = buildPhilHealthHeaders(system, request);
            
            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();
            
            // Execute request with PhilHealth-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createSuccessResponse(system.getSystemCode(), response.getBody(), "PhilHealth request successful");
            
        } catch (Exception e) {
            log.error("PhilHealth request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "PHILHEALTH_ERROR", "PhilHealth integration failed: " + e.getMessage());
        }
    }
    
    /**
     * BIR (Bureau of Internal Revenue) integration
     */
    private ApiGatewayResponse executeBIRRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing BIR request for endpoint: {}", request.getEndpoint());
        
        try {
            // Build BIR-specific headers
            HttpHeaders headers = buildBIRHeaders(system, request);
            
            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();
            
            // Execute request with BIR-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createSuccessResponse(system.getSystemCode(), response.getBody(), "BIR request successful");
            
        } catch (Exception e) {
            log.error("BIR request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "BIR_ERROR", "BIR integration failed: " + e.getMessage());
        }
    }
    
    /**
     * BSP (Bangko Sentral ng Pilipinas) integration
     */
    private ApiGatewayResponse executeBSPRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing BSP request for endpoint: {}", request.getEndpoint());
        
        try {
            // Build BSP-specific headers
            HttpHeaders headers = buildBSPHeaders(system, request);
            
            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();
            
            // Execute request with BSP-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createSuccessResponse(system.getSystemCode(), response.getBody(), "BSP request successful");
            
        } catch (Exception e) {
            log.error("BSP request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "BSP_ERROR", "BSP integration failed: " + e.getMessage());
        }
    }
    
    /**
     * LGU System integration
     */
    private ApiGatewayResponse executeLGUSystemRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing LGU System request for endpoint: {}", request.getEndpoint());
        
        try {
            // Build LGU-specific headers
            HttpHeaders headers = buildLGUHeaders(system, request);
            
            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();
            
            // Execute request with LGU-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createSuccessResponse(system.getSystemCode(), response.getBody(), "LGU System request successful");
            
        } catch (Exception e) {
            log.error("LGU System request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "LGU_ERROR", "LGU System integration failed: " + e.getMessage());
        }
    }
    
    /**
     * Generic system integration for other systems
     */
    private ApiGatewayResponse executeGenericRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.info("Executing generic request for system: {} endpoint: {}", system.getSystemCode(), request.getEndpoint());
        
        try {
            // Build generic headers
            HttpHeaders headers = buildGenericHeaders(system, request);
            
            // Build request URL
            String url = system.getBaseUrl() + request.getEndpoint();
            
            // Execute request
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );
            
            return createSuccessResponse(system.getSystemCode(), response.getBody(), "Generic request successful");
            
        } catch (Exception e) {
            log.error("Generic request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "GENERIC_ERROR", "Generic integration failed: " + e.getMessage());
        }
    }

    /**
     * Execute DepEd (Department of Education) specific request
     */
    private ApiGatewayResponse executeDepEdRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.debug("Executing DepEd request to: {}", system.getBaseUrl());

        try {
            // Build DepEd-specific headers
            HttpHeaders headers = buildDepEdHeaders(system, request);

            // Build request URL with DepEd API versioning
            String url = system.getBaseUrl() + "/api/v2" + request.getEndpoint();

            // Execute request with DepEd-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );

            return createSuccessResponse(system.getSystemCode(), response.getBody(), "DepEd request successful");

        } catch (Exception e) {
            log.error("DepEd request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "DEPED_ERROR", "DepEd integration failed: " + e.getMessage());
        }
    }

    /**
     * Execute DOH (Department of Health) specific request
     */
    private ApiGatewayResponse executeDOHRequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.debug("Executing DOH request to: {}", system.getBaseUrl());

        try {
            // Build DOH-specific headers with FHIR compliance
            HttpHeaders headers = buildDOHHeaders(system, request);

            // Build request URL with DOH FHIR API
            String url = system.getBaseUrl() + "/fhir/R4" + request.getEndpoint();

            // Execute request with DOH-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );

            return createSuccessResponse(system.getSystemCode(), response.getBody(), "DOH request successful");

        } catch (Exception e) {
            log.error("DOH request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "DOH_ERROR", "DOH integration failed: " + e.getMessage());
        }
    }

    /**
     * Execute DOLE (Department of Labor and Employment) specific request
     */
    private ApiGatewayResponse executeDOLERequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.debug("Executing DOLE request to: {}", system.getBaseUrl());

        try {
            // Build DOLE-specific headers
            HttpHeaders headers = buildDOLEHeaders(system, request);

            // Build request URL with DOLE API versioning
            String url = system.getBaseUrl() + "/api/v1" + request.getEndpoint();

            // Execute request with DOLE-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );

            return createSuccessResponse(system.getSystemCode(), response.getBody(), "DOLE request successful");

        } catch (Exception e) {
            log.error("DOLE request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "DOLE_ERROR", "DOLE integration failed: " + e.getMessage());
        }
    }

    /**
     * Execute LRA (Land Registration Authority) specific request
     */
    private ApiGatewayResponse executeLRARequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.debug("Executing LRA request to: {}", system.getBaseUrl());

        try {
            // Build LRA-specific headers
            HttpHeaders headers = buildLRAHeaders(system, request);

            // Build request URL with LRA API versioning
            String url = system.getBaseUrl() + "/api/v1" + request.getEndpoint();

            // Execute request with LRA-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );

            return createSuccessResponse(system.getSystemCode(), response.getBody(), "LRA request successful");

        } catch (Exception e) {
            log.error("LRA request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "LRA_ERROR", "LRA integration failed: " + e.getMessage());
        }
    }

    /**
     * Execute CDA (Cooperative Development Authority) specific request
     */
    private ApiGatewayResponse executeCDARequest(ExternalSystemIntegration system, ApiGatewayRequest request) {
        log.debug("Executing CDA request to: {}", system.getBaseUrl());

        try {
            // Build CDA-specific headers
            HttpHeaders headers = buildCDAHeaders(system, request);

            // Build request URL with CDA API versioning
            String url = system.getBaseUrl() + "/api/v1" + request.getEndpoint();

            // Execute request with CDA-specific handling
            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                Object.class
            );

            return createSuccessResponse(system.getSystemCode(), response.getBody(), "CDA request successful");

        } catch (Exception e) {
            log.error("CDA request failed: {}", e.getMessage(), e);
            return createErrorResponse(system.getSystemCode(), "CDA_ERROR", "CDA integration failed: " + e.getMessage());
        }
    }

    // Header building methods for different systems

    private HttpHeaders buildPhilSysHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-PhilSys-Client-ID", system.getClientId());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildSSSHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-SSS-API-Key", system.getClientSecret());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildGSISHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-GSIS-Client-ID", system.getClientId());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildPagIbigHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-PagIbig-API-Key", system.getClientSecret());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildPhilHealthHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-PhilHealth-Client-ID", system.getClientId());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildBIRHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-BIR-API-Key", system.getClientSecret());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildBSPHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-BSP-Client-ID", system.getClientId());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildLGUHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-LGU-Code", system.getClientId());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildDepEdHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-DepEd-Client-ID", system.getClientId());
        headers.set("X-DepEd-API-Version", "2.0");
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildDOHHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-DOH-Client-ID", system.getClientId());
        headers.set("X-FHIR-Version", "4.0.1");
        headers.set("Accept", "application/fhir+json");
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildDOLEHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-DOLE-Client-ID", system.getClientId());
        headers.set("X-DOLE-API-Key", system.getClientSecret());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildLRAHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-LRA-Client-ID", system.getClientId());
        headers.set("X-LRA-Registry-Code", system.getClientSecret());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildCDAHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-CDA-Client-ID", system.getClientId());
        headers.set("X-CDA-API-Key", system.getClientSecret());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private HttpHeaders buildGenericHeaders(ExternalSystemIntegration system, ApiGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + system.getApiKey());
        headers.set("X-Client-ID", system.getClientId());
        headers.set("X-Request-ID", request.getRequestId());
        addCommonHeaders(headers, request);
        return headers;
    }

    private void addCommonHeaders(HttpHeaders headers, ApiGatewayRequest request) {
        headers.set("X-Correlation-ID", request.getCorrelationId());
        headers.set("X-User-ID", request.getUserId());
        headers.set("X-Timestamp", LocalDateTime.now().toString());

        // Add custom headers from request
        if (request.hasHeaders()) {
            request.getHeaders().forEach(headers::set);
        }
    }

    // Caching methods

    private ApiGatewayResponse getCachedResponse(String systemCode, String endpoint) {
        String cacheKey = systemCode + ":" + endpoint;
        LocalDateTime cacheTime = cacheTimestamps.get(cacheKey);

        if (cacheTime != null && cacheTime.plusMinutes(CACHE_TTL_MINUTES).isAfter(LocalDateTime.now())) {
            return (ApiGatewayResponse) responseCache.get(cacheKey);
        }

        // Remove expired cache entry
        responseCache.remove(cacheKey);
        cacheTimestamps.remove(cacheKey);
        return null;
    }

    private void cacheResponse(String systemCode, String endpoint, ApiGatewayResponse response) {
        String cacheKey = systemCode + ":" + endpoint;
        responseCache.put(cacheKey, response);
        cacheTimestamps.put(cacheKey, LocalDateTime.now());
    }

    // Health tracking methods

    private void updateSystemHealth(ExternalSystemIntegration system, boolean isHealthy) {
        system.setLastHealthCheck(LocalDateTime.now());

        if (isHealthy) {
            system.setStatus(ExternalSystemIntegration.SystemStatus.ACTIVE);
            system.setLastSuccessfulCall(LocalDateTime.now());
        } else {
            system.setStatus(ExternalSystemIntegration.SystemStatus.ERROR);
            system.setLastFailedCall(LocalDateTime.now());
        }

        systemRepository.save(system);
    }

    // Response helper methods

    private ApiGatewayResponse createSuccessResponse(String systemCode, Object data, String message) {
        return ApiGatewayResponse.builder()
            .success(true)
            .statusCode(200)
            .systemCode(systemCode)
            .body(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    private ApiGatewayResponse createResponseFromHttpResponse(String systemCode, ResponseEntity<Object> httpResponse, String successMessage) {
        boolean isSuccess = httpResponse.getStatusCode().is2xxSuccessful();

        return ApiGatewayResponse.builder()
            .success(isSuccess)
            .statusCode(httpResponse.getStatusCode().value())
            .systemCode(systemCode)
            .body(httpResponse.getBody())
            .errorCode(isSuccess ? null : "HTTP_ERROR")
            .errorMessage(isSuccess ? null : "HTTP " + httpResponse.getStatusCode().value() + " response")
            .timestamp(LocalDateTime.now())
            .build();
    }

    private ApiGatewayResponse createErrorResponse(String systemCode, String errorCode, String message) {
        return ApiGatewayResponse.builder()
            .success(false)
            .statusCode(500)
            .systemCode(systemCode)
            .errorCode(errorCode)
            .errorMessage(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Batch processing for multiple external system requests
     */
    public Map<String, ApiGatewayResponse> batchProcessRequests(Map<String, ApiGatewayRequest> requests) {
        log.info("Processing batch of {} external system requests", requests.size());

        Map<String, ApiGatewayResponse> responses = new ConcurrentHashMap<>();

        // Process requests in parallel
        List<CompletableFuture<Void>> futures = requests.entrySet().stream()
            .map(entry -> CompletableFuture.runAsync(() -> {
                try {
                    ApiGatewayResponse response = connectToGovernmentSystem(entry.getKey(), entry.getValue());
                    responses.put(entry.getKey(), response);
                } catch (Exception e) {
                    log.error("Batch request failed for system {}: {}", entry.getKey(), e.getMessage());
                    responses.put(entry.getKey(), createErrorResponse(entry.getKey(), "BATCH_ERROR", e.getMessage()));
                }
            }))
            .toList();

        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return responses;
    }

    /**
     * Clear cache for specific system or all systems
     */
    public void clearCache(String systemCode) {
        if (systemCode != null) {
            responseCache.entrySet().removeIf(entry -> entry.getKey().startsWith(systemCode + ":"));
            cacheTimestamps.entrySet().removeIf(entry -> entry.getKey().startsWith(systemCode + ":"));
        } else {
            responseCache.clear();
            cacheTimestamps.clear();
        }
        log.info("Cache cleared for system: {}", systemCode != null ? systemCode : "ALL");
    }

    /**
     * Enhanced connection with comprehensive retry and monitoring
     */
    public ApiGatewayResponse connectWithAdvancedResilience(String systemCode, ApiGatewayRequest request) {
        log.info("Connecting with advanced resilience to system: {}", systemCode);

        RetryConfiguration retryConfig = getRetryConfiguration(systemCode);
        SystemHealthMetrics metrics = getSystemHealthMetrics(systemCode);

        return executeWithAdvancedRetry(systemCode, request, retryConfig, metrics);
    }

    /**
     * Execute request with advanced retry logic and monitoring
     */
    private ApiGatewayResponse executeWithAdvancedRetry(String systemCode, ApiGatewayRequest request,
                                                       RetryConfiguration retryConfig, SystemHealthMetrics metrics) {
        Exception lastException = null;
        long totalStartTime = System.currentTimeMillis();

        for (int attempt = 1; attempt <= retryConfig.getMaxRetries(); attempt++) {
            try {
                log.debug("Attempt {} of {} for system: {}", attempt, retryConfig.getMaxRetries(), systemCode);

                // Record attempt
                metrics.recordAttempt();

                // Execute request with timeout
                ApiGatewayResponse response = executeWithTimeout(systemCode, request, retryConfig.getTimeoutMs());

                if (response.isSuccess()) {
                    // Record success
                    long totalTime = System.currentTimeMillis() - totalStartTime;
                    metrics.recordSuccess(totalTime);

                    log.info("Request successful for system {} after {} attempts in {}ms",
                            systemCode, attempt, totalTime);
                    return response;
                }

                // Check if error is retryable
                if (!isRetryableError(response)) {
                    log.warn("Non-retryable error for system {}: {}", systemCode, response.getErrorMessage());
                    metrics.recordNonRetryableFailure();
                    return response;
                }

                lastException = new RuntimeException("Request failed: " + response.getErrorMessage());

            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for system {}: {}", attempt, systemCode, e.getMessage());

                // Check if exception is retryable
                if (!isRetryableException(e)) {
                    log.warn("Non-retryable exception for system {}: {}", systemCode, e.getMessage());
                    metrics.recordNonRetryableFailure();
                    return createErrorResponse(systemCode, "NON_RETRYABLE_ERROR", e.getMessage());
                }
            }

            // Apply backoff strategy before next attempt
            if (attempt < retryConfig.getMaxRetries()) {
                long delay = calculateBackoffDelay(attempt, retryConfig);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // All retries exhausted
        long totalTime = System.currentTimeMillis() - totalStartTime;
        metrics.recordFailure(totalTime);

        log.error("All {} attempts failed for system: {} in {}ms",
                 retryConfig.getMaxRetries(), systemCode, totalTime);

        return createErrorResponse(systemCode, "RETRY_EXHAUSTED",
            "Failed after " + retryConfig.getMaxRetries() + " attempts: " +
            (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    /**
     * Execute request with timeout
     */
    private ApiGatewayResponse executeWithTimeout(String systemCode, ApiGatewayRequest request, long timeoutMs) {
        CompletableFuture<ApiGatewayResponse> future = CompletableFuture.supplyAsync(() -> {
            return connectToGovernmentSystem(systemCode, request);
        });

        try {
            return future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Request timeout after " + timeoutMs + "ms");
        } catch (Exception e) {
            throw new RuntimeException("Request execution failed", e);
        }
    }

    /**
     * Check if error response indicates a retryable condition
     */
    private boolean isRetryableError(ApiGatewayResponse response) {
        if (response.isSuccess()) {
            return false;
        }

        int statusCode = response.getStatusCode();
        String errorCode = response.getErrorCode();

        // Retryable HTTP status codes
        if (statusCode >= 500 && statusCode < 600) { // Server errors
            return true;
        }
        if (statusCode == 429) { // Rate limit
            return true;
        }
        if (statusCode == 408) { // Request timeout
            return true;
        }

        // Retryable error codes
        if (errorCode != null) {
            return errorCode.contains("TIMEOUT") ||
                   errorCode.contains("CONNECTION") ||
                   errorCode.contains("UNAVAILABLE") ||
                   errorCode.contains("RATE_LIMIT");
        }

        return false;
    }

    /**
     * Check if exception indicates a retryable condition
     */
    private boolean isRetryableException(Exception e) {
        String message = e.getMessage().toLowerCase();

        // Network-related exceptions are usually retryable
        if (e instanceof java.net.SocketTimeoutException ||
            e instanceof java.net.ConnectException ||
            e instanceof java.net.SocketException ||
            e instanceof org.springframework.web.client.ResourceAccessException) {
            return true;
        }

        // Message-based detection
        return message.contains("timeout") ||
               message.contains("connection") ||
               message.contains("unavailable") ||
               message.contains("refused");
    }

    /**
     * Calculate backoff delay using exponential backoff with jitter
     */
    private long calculateBackoffDelay(int attempt, RetryConfiguration config) {
        long baseDelay = config.getBaseDelayMs();
        double multiplier = config.getBackoffMultiplier();
        long maxDelay = config.getMaxDelayMs();

        // Exponential backoff
        long delay = (long) (baseDelay * Math.pow(multiplier, attempt - 1));

        // Apply max delay limit
        delay = Math.min(delay, maxDelay);

        // Add jitter to prevent thundering herd
        double jitter = 0.1; // 10% jitter
        long jitterAmount = (long) (delay * jitter * Math.random());
        delay += jitterAmount;

        log.debug("Calculated backoff delay for attempt {}: {}ms", attempt, delay);
        return delay;
    }

    /**
     * Get retry configuration for system
     */
    private RetryConfiguration getRetryConfiguration(String systemCode) {
        return retryConfigurations.computeIfAbsent(systemCode, code -> {
            // Default configuration
            RetryConfiguration config = new RetryConfiguration();

            // System-specific configurations
            switch (code.toUpperCase()) {
                case "PHILSYS":
                    config.setMaxRetries(5);
                    config.setBaseDelayMs(2000);
                    config.setMaxDelayMs(30000);
                    config.setTimeoutMs(60000);
                    break;
                case "SSS":
                case "GSIS":
                case "PHILHEALTH":
                    config.setMaxRetries(3);
                    config.setBaseDelayMs(1000);
                    config.setMaxDelayMs(15000);
                    config.setTimeoutMs(30000);
                    break;
                default:
                    config.setMaxRetries(3);
                    config.setBaseDelayMs(1000);
                    config.setMaxDelayMs(10000);
                    config.setTimeoutMs(20000);
            }

            return config;
        });
    }

    /**
     * Get system health metrics
     */
    private SystemHealthMetrics getSystemHealthMetrics(String systemCode) {
        return healthMetrics.computeIfAbsent(systemCode, code -> new SystemHealthMetrics(code));
    }

    /**
     * Get comprehensive system health metrics
     */
    public Map<String, Object> getSystemHealthMetrics(String systemCode, boolean detailed) {
        SystemHealthMetrics metrics = healthMetrics.get(systemCode);
        Map<String, Object> result = new HashMap<>();

        if (metrics == null) {
            result.put("status", "NO_METRICS");
            result.put("message", "No metrics available for system: " + systemCode);
            return result;
        }

        result.put("systemCode", metrics.getSystemCode());
        result.put("totalAttempts", metrics.getTotalAttempts());
        result.put("totalSuccesses", metrics.getTotalSuccesses());
        result.put("totalFailures", metrics.getTotalFailures());
        result.put("successRate", metrics.getSuccessRate());
        result.put("averageResponseTime", metrics.getAverageResponseTime());
        result.put("lastAttempt", metrics.getLastAttempt());
        result.put("lastSuccess", metrics.getLastSuccess());
        result.put("lastFailure", metrics.getLastFailure());

        if (detailed) {
            result.put("nonRetryableFailures", metrics.getTotalNonRetryableFailures());

            RetryConfiguration config = getRetryConfiguration(systemCode);
            Map<String, Object> retryConfig = new HashMap<>();
            retryConfig.put("maxRetries", config.getMaxRetries());
            retryConfig.put("baseDelayMs", config.getBaseDelayMs());
            retryConfig.put("maxDelayMs", config.getMaxDelayMs());
            retryConfig.put("timeoutMs", config.getTimeoutMs());
            result.put("retryConfiguration", retryConfig);
        }

        return result;
    }

    /**
     * Retry configuration class
     */
    public static class RetryConfiguration {
        private int maxRetries = 3;
        private long baseDelayMs = 1000;
        private long maxDelayMs = 10000;
        private double backoffMultiplier = 2.0;
        private long timeoutMs = 20000;

        // Getters and setters
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

        public long getBaseDelayMs() { return baseDelayMs; }
        public void setBaseDelayMs(long baseDelayMs) { this.baseDelayMs = baseDelayMs; }

        public long getMaxDelayMs() { return maxDelayMs; }
        public void setMaxDelayMs(long maxDelayMs) { this.maxDelayMs = maxDelayMs; }

        public double getBackoffMultiplier() { return backoffMultiplier; }
        public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }

        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    }

    /**
     * System health metrics class
     */
    public static class SystemHealthMetrics {
        private final String systemCode;
        private long totalAttempts = 0;
        private long totalSuccesses = 0;
        private long totalFailures = 0;
        private long totalNonRetryableFailures = 0;
        private long totalResponseTime = 0;
        private LocalDateTime lastAttempt;
        private LocalDateTime lastSuccess;
        private LocalDateTime lastFailure;

        public SystemHealthMetrics(String systemCode) {
            this.systemCode = systemCode;
        }

        public synchronized void recordAttempt() {
            totalAttempts++;
            lastAttempt = LocalDateTime.now();
        }

        public synchronized void recordSuccess(long responseTime) {
            totalSuccesses++;
            totalResponseTime += responseTime;
            lastSuccess = LocalDateTime.now();
        }

        public synchronized void recordFailure(long responseTime) {
            totalFailures++;
            totalResponseTime += responseTime;
            lastFailure = LocalDateTime.now();
        }

        public synchronized void recordNonRetryableFailure() {
            totalNonRetryableFailures++;
            lastFailure = LocalDateTime.now();
        }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (double) totalSuccesses / totalAttempts : 0.0;
        }

        public double getAverageResponseTime() {
            long totalCalls = totalSuccesses + totalFailures;
            return totalCalls > 0 ? (double) totalResponseTime / totalCalls : 0.0;
        }

        // Getters
        public String getSystemCode() { return systemCode; }
        public long getTotalAttempts() { return totalAttempts; }
        public long getTotalSuccesses() { return totalSuccesses; }
        public long getTotalFailures() { return totalFailures; }
        public long getTotalNonRetryableFailures() { return totalNonRetryableFailures; }
        public LocalDateTime getLastAttempt() { return lastAttempt; }
        public LocalDateTime getLastSuccess() { return lastSuccess; }
        public LocalDateTime getLastFailure() { return lastFailure; }
    }
}
