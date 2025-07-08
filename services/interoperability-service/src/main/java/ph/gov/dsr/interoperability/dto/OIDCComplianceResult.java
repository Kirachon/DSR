package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for OpenID Connect compliance results
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OpenID Connect compliance result")
public class OIDCComplianceResult {

    @Schema(description = "Whether the provider is OIDC compliant", example = "true")
    private boolean compliant;

    @Schema(description = "Identity provider ID", example = "google-oidc")
    private String providerId;

    @Schema(description = "OIDC specification version", example = "1.0")
    private String oidcVersion;

    @Schema(description = "List of validation results from different checks")
    private List<OIDCValidationResult> validationResults;

    @Schema(description = "Overall compliance score (0-100)", example = "92.5")
    private Double complianceScore;

    @Schema(description = "Compliance level", example = "FULLY_COMPLIANT")
    private String complianceLevel;

    @Schema(description = "List of compliance errors found")
    private List<String> errors;

    @Schema(description = "List of compliance warnings found")
    private List<String> warnings;

    @Schema(description = "List of compliance recommendations")
    private List<String> recommendations;

    @Schema(description = "Error message if validation failed")
    private String errorMessage;

    @Schema(description = "Additional compliance details")
    private String details;

    @Schema(description = "When the compliance check was performed")
    private LocalDateTime validatedAt;

    @Schema(description = "Validation duration in milliseconds", example = "350")
    private Long validationDurationMs;

    @Schema(description = "Validator used", example = "OIDC_COMPLIANCE_SERVICE")
    private String validator;

    @Schema(description = "Validation context or reference", example = "SSO_INTEGRATION")
    private String validationContext;

    @Schema(description = "Discovery endpoint URL validated")
    private String discoveryUrl;

    @Schema(description = "Token endpoint URL validated")
    private String tokenUrl;

    @Schema(description = "Userinfo endpoint URL validated")
    private String userinfoUrl;

    @Schema(description = "Authorization endpoint URL validated")
    private String authorizationUrl;

    @Schema(description = "Supported scopes by the provider")
    private List<String> supportedScopes;

    @Schema(description = "Supported claims by the provider")
    private List<String> supportedClaims;

    @Schema(description = "Supported response types")
    private List<String> supportedResponseTypes;

    @Schema(description = "Supported grant types")
    private List<String> supportedGrantTypes;

    @Schema(description = "Issuer identifier")
    private String issuer;

    @Schema(description = "Client ID used for validation")
    private String clientId;

    @Schema(description = "Compliance metadata")
    private Map<String, Object> metadata;

    /**
     * Check if compliance check has errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if compliance check has warnings
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
     * Get compliance summary
     */
    public String getComplianceSummary() {
        if (compliant) {
            return String.format("OIDC %s compliance passed for provider %s", oidcVersion, providerId);
        } else {
            return String.format("OIDC %s compliance failed for provider %s - %d errors, %d warnings",
                               oidcVersion, providerId,
                               errors != null ? errors.size() : 0,
                               warnings != null ? warnings.size() : 0);
        }
    }

    /**
     * Check if compliance is critical (has errors)
     */
    public boolean isCritical() {
        return hasErrors();
    }

    /**
     * Get compliance quality level
     */
    public String getQualityLevel() {
        if (!compliant) {
            return "POOR";
        } else if (hasWarnings()) {
            return "GOOD";
        } else {
            return "EXCELLENT";
        }
    }

    /**
     * Get compliance level based on results
     */
    public String getComplianceLevel() {
        if (complianceLevel != null) {
            return complianceLevel;
        }

        if (!compliant) {
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
     * Calculate compliance score based on validation results
     */
    public Double calculateComplianceScore() {
        if (complianceScore != null) {
            return complianceScore;
        }

        if (validationResults == null || validationResults.isEmpty()) {
            return compliant ? 100.0 : 0.0;
        }

        double totalScore = 0.0;
        int validResults = 0;

        for (OIDCValidationResult result : validationResults) {
            if (result.getValidationScore() != null) {
                totalScore += result.getValidationScore();
                validResults++;
            }
        }

        return validResults > 0 ? totalScore / validResults : (compliant ? 100.0 : 0.0);
    }

    /**
     * Check if supported scopes are available
     */
    public boolean hasSupportedScopes() {
        return supportedScopes != null && !supportedScopes.isEmpty();
    }

    /**
     * Check if supported claims are available
     */
    public boolean hasSupportedClaims() {
        return supportedClaims != null && !supportedClaims.isEmpty();
    }

    /**
     * Get validation result by type
     */
    public OIDCValidationResult getValidationResultByType(String validationType) {
        if (validationResults == null) {
            return null;
        }

        return validationResults.stream()
                .filter(result -> validationType.equals(result.getValidationType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if validation passed for specific type
     */
    public boolean isValidForType(String validationType) {
        OIDCValidationResult result = getValidationResultByType(validationType);
        return result != null && result.isValid();
    }

    /**
     * Check if discovery validation passed
     */
    public boolean isDiscoveryValid() {
        return isValidForType("DISCOVERY");
    }

    /**
     * Check if token validation passed
     */
    public boolean isTokenValid() {
        return isValidForType("TOKEN");
    }

    /**
     * Check if userinfo validation passed
     */
    public boolean isUserinfoValid() {
        return isValidForType("USERINFO");
    }

    /**
     * Check if scopes validation passed
     */
    public boolean isScopesValid() {
        return isValidForType("SCOPES");
    }

    /**
     * Get recommended next actions
     */
    public List<String> getRecommendedActions() {
        List<String> actions = new java.util.ArrayList<>();

        if (hasErrors()) {
            actions.add("Address OIDC compliance errors immediately");
        }

        if (hasWarnings()) {
            actions.add("Review and address OIDC compliance warnings");
        }

        if (!compliant) {
            actions.add("Ensure OIDC provider meets specification requirements");
        }

        if (!isDiscoveryValid()) {
            actions.add("Fix OIDC discovery document issues");
        }

        if (!isTokenValid()) {
            actions.add("Fix OIDC token endpoint configuration");
        }

        if (!isUserinfoValid()) {
            actions.add("Fix OIDC userinfo endpoint configuration");
        }

        if (!isScopesValid()) {
            actions.add("Review and fix OIDC scopes and claims configuration");
        }

        if (recommendations != null) {
            actions.addAll(recommendations);
        }

        return actions;
    }

    /**
     * Get OIDC endpoints summary
     */
    public Map<String, String> getEndpointsSummary() {
        Map<String, String> endpoints = new java.util.HashMap<>();

        if (discoveryUrl != null) {
            endpoints.put("discovery", discoveryUrl);
        }

        if (tokenUrl != null) {
            endpoints.put("token", tokenUrl);
        }

        if (userinfoUrl != null) {
            endpoints.put("userinfo", userinfoUrl);
        }

        if (authorizationUrl != null) {
            endpoints.put("authorization", authorizationUrl);
        }

        return endpoints;
    }

    /**
     * Get capabilities summary
     */
    public Map<String, Object> getCapabilitiesSummary() {
        Map<String, Object> capabilities = new java.util.HashMap<>();

        capabilities.put("scopesCount", hasSupportedScopes() ? supportedScopes.size() : 0);
        capabilities.put("claimsCount", hasSupportedClaims() ? supportedClaims.size() : 0);
        capabilities.put("responseTypesCount", supportedResponseTypes != null ? supportedResponseTypes.size() : 0);
        capabilities.put("grantTypesCount", supportedGrantTypes != null ? supportedGrantTypes.size() : 0);
        capabilities.put("issuer", issuer);
        capabilities.put("clientId", clientId);

        return capabilities;
    }
}