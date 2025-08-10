package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for OpenID Connect validation results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Data
@Builder
@Schema(description = "OpenID Connect validation result")
public class OIDCValidationResult {

    @Schema(description = "Whether the validation passed", example = "true")
    private boolean valid;

    @Schema(description = "Identity provider ID", example = "google-oidc")
    private String providerId;

    @Schema(description = "Type of validation performed", example = "DISCOVERY")
    private String validationType;

    @Schema(description = "Discovery endpoint URL")
    private String discoveryUrl;

    @Schema(description = "Token endpoint URL")
    private String tokenUrl;

    @Schema(description = "Userinfo endpoint URL")
    private String userinfoUrl;

    @Schema(description = "Authorization endpoint URL")
    private String authorizationUrl;

    @Schema(description = "JWKS URI")
    private String jwksUri;

    @Schema(description = "Requested scopes")
    private List<String> requestedScopes;

    @Schema(description = "Supported scopes")
    private List<String> supportedScopes;

    @Schema(description = "Supported response types")
    private List<String> supportedResponseTypes;

    @Schema(description = "Supported grant types")
    private List<String> supportedGrantTypes;

    @Schema(description = "Validation errors found")
    private List<String> errors;

    @Schema(description = "Validation warnings found")
    private List<String> warnings;

    @Schema(description = "Validation score (0-100)", example = "95.5")
    private Double validationScore;

    @Schema(description = "Validation severity", example = "MEDIUM")
    private String severity;

    @Schema(description = "Additional validation details")
    private String details;

    @Schema(description = "When the validation was performed")
    private LocalDateTime validatedAt;

    @Schema(description = "Validation duration in milliseconds", example = "250")
    private Long validationDurationMs;

    @Schema(description = "Validator used", example = "OIDC_COMPLIANCE_SERVICE")
    private String validator;

    @Schema(description = "Validation context or reference", example = "SSO_INTEGRATION")
    private String validationContext;

    @Schema(description = "OIDC specification version", example = "1.0")
    private String oidcVersion;

    @Schema(description = "Client ID used for validation")
    private String clientId;

    @Schema(description = "Issuer identifier")
    private String issuer;

    /**
     * Check if validation has errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if validation has warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Get total issue count (errors + warnings)
     */
    public int getTotalIssueCount() {
        int errorCount = errors != null ? errors.size() : 0;
        int warningCount = warnings != null ? warnings.size() : 0;
        return errorCount + warningCount;
    }

    /**
     * Get validation summary
     */
    public String getValidationSummary() {
        if (valid) {
            return String.format("OIDC %s validation passed for provider %s", validationType, providerId);
        } else {
            return String.format("OIDC %s validation failed for provider %s - %d errors, %d warnings", 
                               validationType, providerId,
                               errors != null ? errors.size() : 0,
                               warnings != null ? warnings.size() : 0);
        }
    }

    /**
     * Check if validation is critical (has errors)
     */
    public boolean isCritical() {
        return hasErrors();
    }

    /**
     * Get validation quality level
     */
    public String getQualityLevel() {
        if (!valid) {
            return "POOR";
        } else if (hasWarnings()) {
            return "GOOD";
        } else {
            return "EXCELLENT";
        }
    }

    /**
     * Check if provider supports specific scope
     */
    public boolean supportsScope(String scope) {
        return supportedScopes != null && supportedScopes.contains(scope);
    }

    /**
     * Check if provider supports specific response type
     */
    public boolean supportsResponseType(String responseType) {
        return supportedResponseTypes != null && supportedResponseTypes.contains(responseType);
    }

    /**
     * Check if provider supports specific grant type
     */
    public boolean supportsGrantType(String grantType) {
        return supportedGrantTypes != null && supportedGrantTypes.contains(grantType);
    }

    /**
     * Get compliance level based on validation results
     */
    public String getComplianceLevel() {
        if (!valid) {
            return "NON_COMPLIANT";
        }
        
        if (hasErrors()) {
            return "PARTIALLY_COMPLIANT";
        }
        
        if (hasWarnings()) {
            return "MOSTLY_COMPLIANT";
        }
        
        return "FULLY_COMPLIANT";
    }

    /**
     * Check if this is a discovery validation
     */
    public boolean isDiscoveryValidation() {
        return "DISCOVERY".equals(validationType);
    }

    /**
     * Check if this is a token validation
     */
    public boolean isTokenValidation() {
        return "TOKEN".equals(validationType);
    }

    /**
     * Check if this is a userinfo validation
     */
    public boolean isUserinfoValidation() {
        return "USERINFO".equals(validationType);
    }

    /**
     * Check if this is a scope validation
     */
    public boolean isScopeValidation() {
        return "SCOPES".equals(validationType);
    }
}
