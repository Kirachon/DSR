package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for OpenID Connect validation requests
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OpenID Connect validation request")
public class OIDCValidationRequest {

    @NotBlank(message = "Provider ID is required")
    @Schema(description = "Identity provider ID", example = "google-oidc", required = true)
    private String providerId;

    @NotBlank(message = "Discovery URL is required")
    @Schema(description = "OpenID Connect discovery endpoint URL", required = true)
    private String discoveryUrl;

    @Schema(description = "Token endpoint URL")
    private String tokenEndpoint;

    @Schema(description = "Userinfo endpoint URL")
    private String userinfoEndpoint;

    @Schema(description = "Authorization endpoint URL")
    private String authorizationEndpoint;

    @Schema(description = "Client credentials for validation")
    private Map<String, Object> clientCredentials;

    @Schema(description = "Access token for userinfo validation")
    private String accessToken;

    @Schema(description = "Required scopes to validate")
    private List<String> requiredScopes;

    @Schema(description = "Required claims to validate")
    private List<String> requiredClaims;

    @Schema(description = "OIDC specification version", example = "1.0")
    @Builder.Default
    private String oidcVersion = "1.0";

    @Schema(description = "Validation context or reference", example = "SSO_INTEGRATION")
    private String validationContext;

    @Schema(description = "Strict validation mode", example = "true")
    @Builder.Default
    private Boolean strictMode = true;

    @Schema(description = "Include discovery document validation", example = "true")
    @Builder.Default
    private Boolean includeDiscoveryValidation = true;

    @Schema(description = "Include token endpoint validation", example = "true")
    @Builder.Default
    private Boolean includeTokenValidation = true;

    @Schema(description = "Include userinfo endpoint validation", example = "true")
    @Builder.Default
    private Boolean includeUserinfoValidation = true;

    @Schema(description = "Include scopes and claims validation", example = "true")
    @Builder.Default
    private Boolean includeScopesValidation = true;

    @Schema(description = "Custom validation rules")
    private Map<String, Object> customValidationRules;

    @Schema(description = "Validation timeout in milliseconds", example = "30000")
    @Builder.Default
    private Long timeoutMs = 30000L;

    @Schema(description = "Additional metadata for validation")
    private Map<String, Object> metadata;

    /**
     * Check if discovery validation is enabled
     */
    public boolean isDiscoveryValidationEnabled() {
        return includeDiscoveryValidation != null && includeDiscoveryValidation;
    }

    /**
     * Check if token validation is enabled
     */
    public boolean isTokenValidationEnabled() {
        return includeTokenValidation != null && includeTokenValidation;
    }

    /**
     * Check if userinfo validation is enabled
     */
    public boolean isUserinfoValidationEnabled() {
        return includeUserinfoValidation != null && includeUserinfoValidation;
    }

    /**
     * Check if scopes validation is enabled
     */
    public boolean isScopesValidationEnabled() {
        return includeScopesValidation != null && includeScopesValidation;
    }

    /**
     * Check if strict mode is enabled
     */
    public boolean isStrictModeEnabled() {
        return strictMode != null && strictMode;
    }

    /**
     * Check if custom validation rules are provided
     */
    public boolean hasCustomValidationRules() {
        return customValidationRules != null && !customValidationRules.isEmpty();
    }

    /**
     * Check if required scopes are specified
     */
    public boolean hasRequiredScopes() {
        return requiredScopes != null && !requiredScopes.isEmpty();
    }

    /**
     * Check if required claims are specified
     */
    public boolean hasRequiredClaims() {
        return requiredClaims != null && !requiredClaims.isEmpty();
    }

    /**
     * Check if client credentials are provided
     */
    public boolean hasClientCredentials() {
        return clientCredentials != null && !clientCredentials.isEmpty();
    }

    /**
     * Get validation summary
     */
    public String getValidationSummary() {
        return String.format("OIDC %s validation for provider %s (strict: %s)",
                           oidcVersion, providerId, isStrictModeEnabled());
    }

    /**
     * Get enabled validation types
     */
    public List<String> getEnabledValidationTypes() {
        List<String> types = new java.util.ArrayList<>();

        if (isDiscoveryValidationEnabled()) {
            types.add("DISCOVERY");
        }

        if (isTokenValidationEnabled()) {
            types.add("TOKEN");
        }

        if (isUserinfoValidationEnabled()) {
            types.add("USERINFO");
        }

        if (isScopesValidationEnabled()) {
            types.add("SCOPES");
        }

        if (hasCustomValidationRules()) {
            types.add("CUSTOM");
        }

        return types;
    }

    /**
     * Validate request parameters
     */
    public boolean isValid() {
        return providerId != null && !providerId.trim().isEmpty() &&
               discoveryUrl != null && !discoveryUrl.trim().isEmpty() &&
               oidcVersion != null && !oidcVersion.trim().isEmpty();
    }

    /**
     * Get validation configuration summary
     */
    public String getConfigurationSummary() {
        return String.format("OIDC %s validation: %s (scopes: %d, claims: %d, strict: %s)",
                           oidcVersion,
                           String.join(", ", getEnabledValidationTypes()),
                           hasRequiredScopes() ? requiredScopes.size() : 0,
                           hasRequiredClaims() ? requiredClaims.size() : 0,
                           isStrictModeEnabled());
    }
}