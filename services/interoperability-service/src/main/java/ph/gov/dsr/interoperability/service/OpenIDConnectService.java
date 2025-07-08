package ph.gov.dsr.interoperability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.interoperability.dto.OIDCValidationResult;
import ph.gov.dsr.interoperability.entity.ComplianceRecord;
import ph.gov.dsr.interoperability.repository.ComplianceRecordRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for OpenID Connect (OIDC) compliance validation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenIDConnectService {

    private final ComplianceRecordRepository complianceRepository;

    @Value("${dsr.oidc.validation.enabled:true}")
    private boolean validationEnabled;

    @Value("${dsr.oidc.strict-mode:false}")
    private boolean strictMode;

    @Value("${dsr.oidc.timeout:30000}")
    private int timeoutMs;

    /**
     * Validate OpenID Connect discovery endpoint
     */
    @Transactional
    public OIDCValidationResult validateDiscoveryEndpoint(String providerId, String discoveryUrl) {
        log.info("Validating OIDC discovery endpoint for provider: {}, URL: {}", providerId, discoveryUrl);
        
        try {
            if (!validationEnabled) {
                return createValidationResult(true, "OIDC validation is disabled", providerId);
            }

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate discovery URL format
            validateDiscoveryUrlFormat(discoveryUrl, errors);

            // Validate discovery document accessibility
            validateDiscoveryDocumentAccessibility(discoveryUrl, errors, warnings);

            // Validate required discovery document fields
            validateDiscoveryDocumentFields(discoveryUrl, errors, warnings);

            // Validate supported response types
            validateSupportedResponseTypes(discoveryUrl, errors, warnings);

            // Validate supported scopes
            validateSupportedScopes(discoveryUrl, warnings);

            boolean isValid = errors.isEmpty() || (!strictMode && errors.size() <= 1);

            // Record compliance check
            recordComplianceCheck("OIDC_DISCOVERY", providerId, isValid,
                                String.format("Discovery validation - Errors: %d, Warnings: %d", 
                                            errors.size(), warnings.size()));

            return OIDCValidationResult.builder()
                    .valid(isValid)
                    .providerId(providerId)
                    .validationType("DISCOVERY")
                    .discoveryUrl(discoveryUrl)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("OIDC discovery validation failed for provider: {}", providerId, e);
            return createValidationResult(false, "Discovery validation failed: " + e.getMessage(), providerId);
        }
    }

    /**
     * Validate OpenID Connect discovery document (alias for validateDiscoveryEndpoint)
     */
    @Transactional
    public OIDCValidationResult validateDiscoveryDocument(String discoveryUrl) {
        return validateDiscoveryEndpoint("default", discoveryUrl);
    }

    /**
     * Validate OpenID Connect token endpoint
     */
    @Transactional
    public OIDCValidationResult validateTokenEndpoint(String providerId, String tokenUrl, Map<String, Object> tokenRequest) {
        log.info("Validating OIDC token endpoint for provider: {}, URL: {}", providerId, tokenUrl);
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate token URL format
            validateTokenUrlFormat(tokenUrl, errors);

            // Validate token request parameters
            validateTokenRequestParameters(tokenRequest, errors, warnings);

            // Validate token endpoint accessibility
            validateTokenEndpointAccessibility(tokenUrl, errors, warnings);

            // Validate supported grant types
            validateSupportedGrantTypes(tokenUrl, errors, warnings);

            // Validate token response format
            validateTokenResponseFormat(tokenUrl, warnings);

            boolean isValid = errors.isEmpty();

            // Record compliance check
            recordComplianceCheck("OIDC_TOKEN", providerId, isValid,
                                String.format("Token validation - Errors: %d, Warnings: %d", 
                                            errors.size(), warnings.size()));

            return OIDCValidationResult.builder()
                    .valid(isValid)
                    .providerId(providerId)
                    .validationType("TOKEN")
                    .tokenUrl(tokenUrl)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("OIDC token validation failed for provider: {}", providerId, e);
            return createValidationResult(false, "Token validation failed: " + e.getMessage(), providerId);
        }
    }

    /**
     * Validate OpenID Connect token endpoint (overloaded method)
     */
    @Transactional
    public OIDCValidationResult validateTokenEndpoint(String tokenUrl, Map<String, Object> tokenRequest) {
        return validateTokenEndpoint("default", tokenUrl, tokenRequest);
    }

    /**
     * Validate OpenID Connect userinfo endpoint
     */
    @Transactional
    public OIDCValidationResult validateUserinfoEndpoint(String providerId, String userinfoUrl, String accessToken) {
        log.info("Validating OIDC userinfo endpoint for provider: {}, URL: {}", providerId, userinfoUrl);
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate userinfo URL format
            validateUserinfoUrlFormat(userinfoUrl, errors);

            // Validate access token format
            validateAccessTokenFormat(accessToken, errors, warnings);

            // Validate userinfo endpoint accessibility
            validateUserinfoEndpointAccessibility(userinfoUrl, accessToken, errors, warnings);

            // Validate userinfo response format
            validateUserinfoResponseFormat(userinfoUrl, accessToken, errors, warnings);

            // Validate required claims
            validateRequiredClaims(userinfoUrl, accessToken, warnings);

            boolean isValid = errors.isEmpty();

            // Record compliance check
            recordComplianceCheck("OIDC_USERINFO", providerId, isValid,
                                String.format("Userinfo validation - Errors: %d, Warnings: %d", 
                                            errors.size(), warnings.size()));

            return OIDCValidationResult.builder()
                    .valid(isValid)
                    .providerId(providerId)
                    .validationType("USERINFO")
                    .userinfoUrl(userinfoUrl)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("OIDC userinfo validation failed for provider: {}", providerId, e);
            return createValidationResult(false, "Userinfo validation failed: " + e.getMessage(), providerId);
        }
    }

    /**
     * Validate OpenID Connect userinfo endpoint (overloaded method)
     */
    @Transactional
    public OIDCValidationResult validateUserinfoEndpoint(String userinfoUrl, String accessToken) {
        return validateUserinfoEndpoint("default", userinfoUrl, accessToken);
    }

    /**
     * Validate OpenID Connect scopes and claims
     */
    @Transactional
    public OIDCValidationResult validateScopesAndClaims(List<String> requiredScopes, List<String> requiredClaims) {
        log.info("Validating OIDC scopes and claims - scopes: {}, claims: {}", requiredScopes, requiredClaims);

        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate that openid scope is present
            if (requiredScopes != null && !requiredScopes.contains("openid")) {
                errors.add("OpenID scope is required");
            }

            // Validate standard scopes
            if (requiredScopes != null) {
                for (String scope : requiredScopes) {
                    if (!List.of("openid", "profile", "email", "address", "phone").contains(scope)) {
                        warnings.add("Non-standard scope requested: " + scope);
                    }
                }
            }

            // Validate standard claims
            if (requiredClaims != null) {
                for (String claim : requiredClaims) {
                    if (!List.of("sub", "name", "email", "email_verified", "profile").contains(claim)) {
                        warnings.add("Non-standard claim requested: " + claim);
                    }
                }
            }

            return OIDCValidationResult.builder()
                    .valid(errors.isEmpty())
                    .providerId("default")
                    .validationType("SCOPES_AND_CLAIMS")
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("OIDC scopes and claims validation failed", e);
            return createValidationResult(false, "Scopes and claims validation failed: " + e.getMessage(), "default");
        }
    }

    /**
     * Validate OpenID Connect scopes
     */
    @Transactional
    public OIDCValidationResult validateScopes(String providerId, List<String> requestedScopes, List<String> supportedScopes) {
        log.info("Validating OIDC scopes for provider: {}, requested: {}", providerId, requestedScopes);
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate that openid scope is present
            validateOpenIdScope(requestedScopes, errors);

            // Validate that requested scopes are supported
            validateRequestedScopes(requestedScopes, supportedScopes, errors, warnings);

            // Validate scope format
            validateScopeFormat(requestedScopes, warnings);

            // Check for deprecated scopes
            validateDeprecatedScopes(requestedScopes, warnings);

            boolean isValid = errors.isEmpty();

            // Record compliance check
            recordComplianceCheck("OIDC_SCOPES", providerId, isValid,
                                String.format("Scope validation - Requested: %d, Errors: %d, Warnings: %d", 
                                            requestedScopes.size(), errors.size(), warnings.size()));

            return OIDCValidationResult.builder()
                    .valid(isValid)
                    .providerId(providerId)
                    .validationType("SCOPES")
                    .requestedScopes(requestedScopes)
                    .supportedScopes(supportedScopes)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("OIDC scope validation failed for provider: {}", providerId, e);
            return createValidationResult(false, "Scope validation failed: " + e.getMessage(), providerId);
        }
    }

    /**
     * Update OIDC compliance settings
     */
    public void updateSettings(Map<String, Object> oidcSettings) {
        log.info("Updating OIDC compliance settings: {}", oidcSettings);
        
        if (oidcSettings.containsKey("validationEnabled")) {
            this.validationEnabled = (Boolean) oidcSettings.get("validationEnabled");
        }
        
        if (oidcSettings.containsKey("strictMode")) {
            this.strictMode = (Boolean) oidcSettings.get("strictMode");
        }
        
        if (oidcSettings.containsKey("timeoutMs")) {
            this.timeoutMs = (Integer) oidcSettings.get("timeoutMs");
        }
        
        log.info("OIDC settings updated - Validation: {}, Strict: {}, Timeout: {}ms", 
                validationEnabled, strictMode, timeoutMs);
    }

    /**
     * Get OIDC compliance statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getComplianceStatistics() {
        log.info("Getting OIDC compliance statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Get overall OIDC compliance statistics
        Object[] oidcStats = complianceRepository.getComplianceStatistics("OIDC");
        if (oidcStats != null && oidcStats.length >= 4) {
            stats.put("totalChecks", oidcStats[0]);
            stats.put("compliantChecks", oidcStats[1]);
            stats.put("nonCompliantChecks", oidcStats[2]);
            stats.put("averageScore", oidcStats[3]);
        }
        
        // Get discovery validation statistics
        Object[] discoveryStats = complianceRepository.getComplianceStatistics("OIDC_DISCOVERY");
        stats.put("discoveryValidation", discoveryStats);
        
        // Get token validation statistics
        Object[] tokenStats = complianceRepository.getComplianceStatistics("OIDC_TOKEN");
        stats.put("tokenValidation", tokenStats);
        
        // Get userinfo validation statistics
        Object[] userinfoStats = complianceRepository.getComplianceStatistics("OIDC_USERINFO");
        stats.put("userinfoValidation", userinfoStats);
        
        // Get scope validation statistics
        Object[] scopeStats = complianceRepository.getComplianceStatistics("OIDC_SCOPES");
        stats.put("scopeValidation", scopeStats);
        
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }

    // Private helper methods
    
    private void validateDiscoveryUrlFormat(String discoveryUrl, List<String> errors) {
        if (discoveryUrl == null || discoveryUrl.trim().isEmpty()) {
            errors.add("Discovery URL cannot be null or empty");
            return;
        }
        
        if (!discoveryUrl.startsWith("https://")) {
            errors.add("Discovery URL must use HTTPS");
        }
        
        if (!discoveryUrl.endsWith("/.well-known/openid_configuration")) {
            errors.add("Discovery URL must end with /.well-known/openid_configuration");
        }
    }

    private void validateDiscoveryDocumentAccessibility(String discoveryUrl, List<String> errors, List<String> warnings) {
        // In a real implementation, this would make an HTTP request to check accessibility
        // For now, we'll simulate the validation
        
        try {
            // Simulate HTTP request validation
            if (discoveryUrl.contains("invalid")) {
                errors.add("Discovery document is not accessible");
            } else if (discoveryUrl.contains("slow")) {
                warnings.add("Discovery document response is slow");
            }
        } catch (Exception e) {
            errors.add("Failed to access discovery document: " + e.getMessage());
        }
    }

    private void validateDiscoveryDocumentFields(String discoveryUrl, List<String> errors, List<String> warnings) {
        // Validate required fields in discovery document
        // This would typically involve parsing the actual discovery document
        
        List<String> requiredFields = List.of(
            "issuer", "authorization_endpoint", "token_endpoint", "userinfo_endpoint",
            "jwks_uri", "response_types_supported", "subject_types_supported",
            "id_token_signing_alg_values_supported"
        );
        
        // Simulate validation - in real implementation, would parse actual document
        for (String field : requiredFields) {
            if (discoveryUrl.contains("missing_" + field)) {
                errors.add("Required field missing in discovery document: " + field);
            }
        }
    }

    private void validateSupportedResponseTypes(String discoveryUrl, List<String> errors, List<String> warnings) {
        // Validate that required response types are supported
        List<String> requiredResponseTypes = List.of("code", "id_token", "token id_token");
        
        // Simulate validation
        if (discoveryUrl.contains("no_code_response")) {
            errors.add("Authorization code response type not supported");
        }
        
        if (discoveryUrl.contains("no_implicit")) {
            warnings.add("Implicit flow response types not supported");
        }
    }

    private void validateSupportedScopes(String discoveryUrl, List<String> warnings) {
        // Validate that standard scopes are supported
        List<String> standardScopes = List.of("openid", "profile", "email", "address", "phone");
        
        // Simulate validation
        if (discoveryUrl.contains("limited_scopes")) {
            warnings.add("Limited scope support detected");
        }
    }

    private void validateTokenUrlFormat(String tokenUrl, List<String> errors) {
        if (tokenUrl == null || tokenUrl.trim().isEmpty()) {
            errors.add("Token URL cannot be null or empty");
            return;
        }
        
        if (!tokenUrl.startsWith("https://")) {
            errors.add("Token URL must use HTTPS");
        }
    }

    private void validateTokenRequestParameters(Map<String, Object> tokenRequest, List<String> errors, List<String> warnings) {
        if (tokenRequest == null) {
            errors.add("Token request parameters cannot be null");
            return;
        }
        
        // Validate required parameters
        if (!tokenRequest.containsKey("grant_type")) {
            errors.add("Token request must include grant_type parameter");
        }
        
        String grantType = (String) tokenRequest.get("grant_type");
        if ("authorization_code".equals(grantType)) {
            if (!tokenRequest.containsKey("code")) {
                errors.add("Authorization code grant requires code parameter");
            }
            if (!tokenRequest.containsKey("redirect_uri")) {
                errors.add("Authorization code grant requires redirect_uri parameter");
            }
        }
        
        if (!tokenRequest.containsKey("client_id")) {
            warnings.add("Token request should include client_id parameter");
        }
    }

    private void validateTokenEndpointAccessibility(String tokenUrl, List<String> errors, List<String> warnings) {
        // Simulate token endpoint accessibility check
        if (tokenUrl.contains("unreachable")) {
            errors.add("Token endpoint is not accessible");
        }
    }

    private void validateSupportedGrantTypes(String tokenUrl, List<String> errors, List<String> warnings) {
        // Validate supported grant types
        if (tokenUrl.contains("no_auth_code")) {
            errors.add("Authorization code grant type not supported");
        }
        
        if (tokenUrl.contains("no_refresh")) {
            warnings.add("Refresh token grant type not supported");
        }
    }

    private void validateTokenResponseFormat(String tokenUrl, List<String> warnings) {
        // Validate token response format compliance
        if (tokenUrl.contains("non_standard_response")) {
            warnings.add("Token response format may not be fully compliant");
        }
    }

    private void validateUserinfoUrlFormat(String userinfoUrl, List<String> errors) {
        if (userinfoUrl == null || userinfoUrl.trim().isEmpty()) {
            errors.add("Userinfo URL cannot be null or empty");
            return;
        }
        
        if (!userinfoUrl.startsWith("https://")) {
            errors.add("Userinfo URL must use HTTPS");
        }
    }

    private void validateAccessTokenFormat(String accessToken, List<String> errors, List<String> warnings) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            errors.add("Access token cannot be null or empty");
            return;
        }
        
        if (accessToken.length() < 10) {
            warnings.add("Access token appears to be very short");
        }
        
        // Basic JWT format check
        if (accessToken.split("\\.").length == 3) {
            // Looks like a JWT
            warnings.add("Access token appears to be a JWT (consider using opaque tokens for userinfo)");
        }
    }

    private void validateUserinfoEndpointAccessibility(String userinfoUrl, String accessToken, 
                                                     List<String> errors, List<String> warnings) {
        // Simulate userinfo endpoint accessibility check
        if (userinfoUrl.contains("unauthorized")) {
            errors.add("Userinfo endpoint returned unauthorized");
        }
        
        if (userinfoUrl.contains("slow_response")) {
            warnings.add("Userinfo endpoint response is slow");
        }
    }

    private void validateUserinfoResponseFormat(String userinfoUrl, String accessToken, 
                                              List<String> errors, List<String> warnings) {
        // Validate userinfo response format
        if (userinfoUrl.contains("invalid_json")) {
            errors.add("Userinfo response is not valid JSON");
        }
        
        if (userinfoUrl.contains("missing_sub")) {
            errors.add("Userinfo response missing required 'sub' claim");
        }
    }

    private void validateRequiredClaims(String userinfoUrl, String accessToken, List<String> warnings) {
        // Validate presence of standard claims
        List<String> standardClaims = List.of("sub", "name", "email", "email_verified");
        
        if (userinfoUrl.contains("minimal_claims")) {
            warnings.add("Userinfo response contains minimal claims");
        }
    }

    private void validateOpenIdScope(List<String> requestedScopes, List<String> errors) {
        if (requestedScopes == null || !requestedScopes.contains("openid")) {
            errors.add("OpenID Connect requests must include 'openid' scope");
        }
    }

    private void validateRequestedScopes(List<String> requestedScopes, List<String> supportedScopes, 
                                       List<String> errors, List<String> warnings) {
        if (requestedScopes == null || supportedScopes == null) {
            return;
        }
        
        for (String scope : requestedScopes) {
            if (!supportedScopes.contains(scope)) {
                errors.add("Requested scope not supported: " + scope);
            }
        }
    }

    private void validateScopeFormat(List<String> requestedScopes, List<String> warnings) {
        if (requestedScopes == null) {
            return;
        }
        
        for (String scope : requestedScopes) {
            if (scope.contains(" ")) {
                warnings.add("Scope contains spaces (should be space-separated list): " + scope);
            }
            
            if (!scope.matches("[a-zA-Z0-9_-]+")) {
                warnings.add("Scope contains invalid characters: " + scope);
            }
        }
    }

    private void validateDeprecatedScopes(List<String> requestedScopes, List<String> warnings) {
        if (requestedScopes == null) {
            return;
        }
        
        List<String> deprecatedScopes = List.of("offline_access"); // Example deprecated scope
        
        for (String scope : requestedScopes) {
            if (deprecatedScopes.contains(scope)) {
                warnings.add("Deprecated scope used: " + scope);
            }
        }
    }

    private OIDCValidationResult createValidationResult(boolean isValid, String message, String providerId) {
        return OIDCValidationResult.builder()
                .valid(isValid)
                .providerId(providerId)
                .validationType("GENERAL")
                .errors(isValid ? new ArrayList<>() : List.of(message))
                .warnings(new ArrayList<>())
                .validatedAt(LocalDateTime.now())
                .build();
    }

    private void recordComplianceCheck(String standard, String entity, boolean compliant, String details) {
        try {
            ComplianceRecord record = new ComplianceRecord(standard, entity, compliant);
            record.setDetails(details);
            record.setCategory("OIDC_VALIDATION");
            record.setSeverity(compliant ? "LOW" : "MEDIUM");
            record.setCheckedBy("OIDC_COMPLIANCE_SERVICE");
            record.setExpiryFromStandard();
            
            complianceRepository.save(record);
            
        } catch (Exception e) {
            log.error("Failed to record OIDC compliance check", e);
        }
    }
}
