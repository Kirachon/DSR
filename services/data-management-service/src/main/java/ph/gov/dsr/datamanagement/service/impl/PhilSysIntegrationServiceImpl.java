package ph.gov.dsr.datamanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationRequest;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationResponse;
import ph.gov.dsr.datamanagement.service.PhilSysIntegrationService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Production implementation of PhilSysIntegrationService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("!no-db")
@RequiredArgsConstructor
@Slf4j
public class PhilSysIntegrationServiceImpl implements PhilSysIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${dsr.philsys.api-url:http://localhost:9000/api/v1}")
    private String philSysApiUrl;

    @Value("${dsr.philsys.api-key:development_key}")
    private String philSysApiKey;

    @Value("${dsr.philsys.timeout:30000}")
    private int timeoutMs;

    @Value("${dsr.philsys.mock-enabled:false}")
    private boolean mockEnabled;

    // PSN validation pattern
    private static final Pattern PSN_PATTERN = Pattern.compile("\\d{4}-\\d{4}-\\d{4}");

    @Override
    public PhilSysVerificationResponse verifyPSN(PhilSysVerificationRequest request) {
        log.info("Verifying PSN: {} for person: {} {}", 
                request.getPsn(), request.getFirstName(), request.getLastName());

        PhilSysVerificationResponse response = new PhilSysVerificationResponse();
        response.setPsn(request.getPsn());
        response.setVerifiedAt(LocalDateTime.now());

        try {
            // Basic PSN format validation
            if (!isValidPSNFormat(request.getPsn())) {
                response.setValid(false);
                response.setVerificationStatus("INVALID_FORMAT");
                response.setErrorMessage("PSN format is invalid. Expected format: XXXX-XXXX-XXXX");
                return response;
            }

            if (mockEnabled) {
                return createMockVerificationResponse(request);
            }

            // Call PhilSys API
            String url = philSysApiUrl + "/verify";
            HttpHeaders headers = createHeaders();
            HttpEntity<PhilSysVerificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PhilSysVerificationResponse> apiResponse = restTemplate.exchange(
                    url, HttpMethod.POST, entity, PhilSysVerificationResponse.class);

            if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
                response = apiResponse.getBody();
                response.setVerifiedAt(LocalDateTime.now());
            } else {
                response.setValid(false);
                response.setVerificationStatus("API_ERROR");
                response.setErrorMessage("PhilSys API returned unexpected response");
            }

        } catch (HttpClientErrorException e) {
            log.error("PhilSys API client error: {}", e.getMessage());
            response.setValid(false);
            response.setVerificationStatus("API_ERROR");
            response.setErrorMessage("PhilSys API error: " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("PhilSys API connection error: {}", e.getMessage());
            response.setValid(false);
            response.setVerificationStatus("CONNECTION_ERROR");
            response.setErrorMessage("Unable to connect to PhilSys service");
        } catch (Exception e) {
            log.error("Unexpected error during PSN verification", e);
            response.setValid(false);
            response.setVerificationStatus("SYSTEM_ERROR");
            response.setErrorMessage("Internal error during verification");
        }

        log.info("PSN verification completed. Status: {}, Valid: {}", 
                response.getVerificationStatus(), response.isValid());

        return response;
    }

    @Override
    public boolean isPSNValid(String psn) {
        log.debug("Checking PSN validity: {}", psn);

        if (!isValidPSNFormat(psn)) {
            return false;
        }

        if (mockEnabled) {
            // Mock validation - consider PSN valid if it follows pattern and doesn't start with "0000"
            return !psn.startsWith("0000");
        }

        try {
            String url = philSysApiUrl + "/check/" + psn;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return Boolean.TRUE.equals(body.get("exists"));
            }

        } catch (Exception e) {
            log.error("Error checking PSN validity", e);
        }

        return false;
    }

    @Override
    public PhilSysVerificationResponse getPersonInfo(String psn) {
        log.info("Getting person info for PSN: {}", psn);

        PhilSysVerificationResponse response = new PhilSysVerificationResponse();
        response.setPsn(psn);
        response.setVerifiedAt(LocalDateTime.now());

        try {
            if (!isValidPSNFormat(psn)) {
                response.setValid(false);
                response.setVerificationStatus("INVALID_FORMAT");
                response.setErrorMessage("PSN format is invalid");
                return response;
            }

            if (mockEnabled) {
                return createMockPersonInfo(psn);
            }

            String url = philSysApiUrl + "/person/" + psn;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<PhilSysVerificationResponse> apiResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, PhilSysVerificationResponse.class);

            if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
                response = apiResponse.getBody();
                response.setVerifiedAt(LocalDateTime.now());
            } else {
                response.setValid(false);
                response.setVerificationStatus("NOT_FOUND");
                response.setErrorMessage("Person not found in PhilSys");
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                response.setValid(false);
                response.setVerificationStatus("NOT_FOUND");
                response.setErrorMessage("Person not found in PhilSys");
            } else {
                response.setValid(false);
                response.setVerificationStatus("API_ERROR");
                response.setErrorMessage("PhilSys API error: " + e.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error getting person info from PhilSys", e);
            response.setValid(false);
            response.setVerificationStatus("SYSTEM_ERROR");
            response.setErrorMessage("Internal error retrieving person info");
        }

        return response;
    }

    @Override
    public PhilSysVerificationResponse verifyPersonDetails(PhilSysVerificationRequest request) {
        log.info("Verifying person details for PSN: {}", request.getPsn());

        // First verify PSN exists
        PhilSysVerificationResponse response = verifyPSN(request);

        if (!response.isValid()) {
            return response;
        }

        // Then verify details match
        if (mockEnabled) {
            return createMockDetailVerification(request);
        }

        try {
            String url = philSysApiUrl + "/verify-details";
            HttpHeaders headers = createHeaders();
            HttpEntity<PhilSysVerificationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PhilSysVerificationResponse> apiResponse = restTemplate.exchange(
                    url, HttpMethod.POST, entity, PhilSysVerificationResponse.class);

            if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
                response = apiResponse.getBody();
                response.setVerifiedAt(LocalDateTime.now());
            }

        } catch (Exception e) {
            log.error("Error verifying person details", e);
            response.setValid(false);
            response.setVerificationStatus("VERIFICATION_ERROR");
            response.setErrorMessage("Error verifying person details");
        }

        return response;
    }

    @Override
    public boolean isPhilSysServiceAvailable() {
        log.debug("Checking PhilSys service availability");

        if (mockEnabled) {
            return true; // Mock service is always available
        }

        try {
            String url = philSysApiUrl + "/health";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.warn("PhilSys service is not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPhilSysServiceStatus() {
        log.debug("Getting PhilSys service status");

        if (mockEnabled) {
            return "MOCK_MODE";
        }

        if (isPhilSysServiceAvailable()) {
            return "AVAILABLE";
        } else {
            return "UNAVAILABLE";
        }
    }

    /**
     * Validate PSN format
     */
    private boolean isValidPSNFormat(String psn) {
        return psn != null && PSN_PATTERN.matcher(psn.trim()).matches();
    }

    /**
     * Create HTTP headers for PhilSys API calls
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + philSysApiKey);
        headers.set("X-API-Version", "1.0");
        return headers;
    }

    /**
     * Create mock verification response
     */
    private PhilSysVerificationResponse createMockVerificationResponse(PhilSysVerificationRequest request) {
        PhilSysVerificationResponse response = new PhilSysVerificationResponse();
        response.setPsn(request.getPsn());
        response.setVerifiedAt(LocalDateTime.now());

        // Mock logic - consider valid if PSN doesn't start with "0000"
        boolean isValid = !request.getPsn().startsWith("0000");

        response.setValid(isValid);
        response.setVerificationStatus(isValid ? "VERIFIED" : "NOT_FOUND");
        response.setConfidenceScore(isValid ? 0.95 : 0.0);

        if (isValid) {
            response.setFirstName(request.getFirstName());
            response.setLastName(request.getLastName());
            response.setDateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().toString() : null);
            response.setSex(request.getSex());
            response.setMatchScore(calculateMockMatchScore(request));
        } else {
            response.setErrorMessage("PSN not found in PhilSys database");
        }

        return response;
    }

    /**
     * Create mock person info response
     */
    private PhilSysVerificationResponse createMockPersonInfo(String psn) {
        PhilSysVerificationResponse response = new PhilSysVerificationResponse();
        response.setPsn(psn);
        response.setVerifiedAt(LocalDateTime.now());

        if (!psn.startsWith("0000")) {
            response.setValid(true);
            response.setVerificationStatus("FOUND");
            response.setFirstName("Juan");
            response.setLastName("Dela Cruz");
            response.setDateOfBirth("1990-01-01");
            response.setSex("M");
            response.setConfidenceScore(1.0);
        } else {
            response.setValid(false);
            response.setVerificationStatus("NOT_FOUND");
            response.setErrorMessage("Person not found");
        }

        return response;
    }

    /**
     * Create mock detail verification response
     */
    private PhilSysVerificationResponse createMockDetailVerification(PhilSysVerificationRequest request) {
        PhilSysVerificationResponse response = createMockVerificationResponse(request);

        if (response.isValid()) {
            // Calculate match score based on provided details
            double matchScore = calculateMockMatchScore(request);
            response.setMatchScore(matchScore);

            if (matchScore >= 0.8) {
                response.setVerificationStatus("VERIFIED");
            } else if (matchScore >= 0.6) {
                response.setVerificationStatus("PARTIAL_MATCH");
            } else {
                response.setVerificationStatus("MISMATCH");
                response.setValid(false);
            }
        }

        return response;
    }

    /**
     * Calculate mock match score
     */
    private double calculateMockMatchScore(PhilSysVerificationRequest request) {
        double score = 1.0;

        // Simple mock scoring logic
        if (request.getFirstName() != null && !request.getFirstName().equalsIgnoreCase("Juan")) {
            score -= 0.2;
        }
        if (request.getLastName() != null && !request.getLastName().equalsIgnoreCase("Dela Cruz")) {
            score -= 0.2;
        }
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().toString().equals("1990-01-01")) {
            score -= 0.3;
        }
        if (request.getSex() != null && !request.getSex().equalsIgnoreCase("M")) {
            score -= 0.1;
        }

        return Math.max(0.0, score);
    }
}
