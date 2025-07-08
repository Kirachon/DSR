package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for compliance settings
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compliance settings")
public class ComplianceSettings {

    @Schema(description = "Compliance standard", example = "FHIR")
    private String standard;

    @Schema(description = "Enable strict validation mode", example = "true")
    @Builder.Default
    private Boolean strictMode = true;

    @Schema(description = "Validation timeout in milliseconds", example = "30000")
    @Builder.Default
    private Long validationTimeoutMs = 30000L;

    @Schema(description = "Maximum retry attempts", example = "3")
    @Builder.Default
    private Integer maxRetryAttempts = 3;

    @Schema(description = "Enable detailed logging", example = "false")
    @Builder.Default
    private Boolean enableDetailedLogging = false;

    @Schema(description = "Enable caching of validation results", example = "true")
    @Builder.Default
    private Boolean enableCaching = true;

    @Schema(description = "Cache TTL in seconds", example = "3600")
    @Builder.Default
    private Long cacheTtlSeconds = 3600L;

    @Schema(description = "Minimum compliance score threshold", example = "80.0")
    @Builder.Default
    private Double minComplianceScore = 80.0;

    @Schema(description = "Enable automatic remediation", example = "false")
    @Builder.Default
    private Boolean enableAutoRemediation = false;

    @Schema(description = "Enable notifications for compliance issues", example = "true")
    @Builder.Default
    private Boolean enableNotifications = true;

    @Schema(description = "Notification threshold level", example = "HIGH")
    @Builder.Default
    private String notificationThreshold = "HIGH";

    @Schema(description = "Custom validation rules")
    private Map<String, Object> customRules;

    @Schema(description = "Standard-specific configuration")
    private Map<String, Object> standardConfig;

    @Schema(description = "Integration endpoints")
    private Map<String, String> endpoints;

    @Schema(description = "Authentication settings")
    private Map<String, Object> authSettings;

    @Schema(description = "Additional settings metadata")
    private Map<String, Object> metadata;

    @Schema(description = "FHIR-specific settings")
    private Map<String, Object> fhirSettings;

    @Schema(description = "OIDC-specific settings")
    private Map<String, Object> oidcSettings;

    @Schema(description = "GDPR-specific settings")
    private Map<String, Object> gdprSettings;

    /**
     * Check if strict mode is enabled
     */
    public boolean isStrictModeEnabled() {
        return strictMode != null && strictMode;
    }

    /**
     * Check if detailed logging is enabled
     */
    public boolean isDetailedLoggingEnabled() {
        return enableDetailedLogging != null && enableDetailedLogging;
    }

    /**
     * Check if caching is enabled
     */
    public boolean isCachingEnabled() {
        return enableCaching != null && enableCaching;
    }

    /**
     * Check if auto remediation is enabled
     */
    public boolean isAutoRemediationEnabled() {
        return enableAutoRemediation != null && enableAutoRemediation;
    }

    /**
     * Check if notifications are enabled
     */
    public boolean isNotificationsEnabled() {
        return enableNotifications != null && enableNotifications;
    }

    /**
     * Check if custom rules are configured
     */
    public boolean hasCustomRules() {
        return customRules != null && !customRules.isEmpty();
    }

    /**
     * Check if standard-specific config is available
     */
    public boolean hasStandardConfig() {
        return standardConfig != null && !standardConfig.isEmpty();
    }

    /**
     * Get settings summary
     */
    public String getSettingsSummary() {
        return String.format("%s compliance settings: strict=%s, timeout=%dms, score threshold=%.1f",
                           standard, isStrictModeEnabled(), validationTimeoutMs, minComplianceScore);
    }

    /**
     * Validate settings
     */
    public boolean isValid() {
        return standard != null && !standard.trim().isEmpty() &&
               validationTimeoutMs != null && validationTimeoutMs > 0 &&
               maxRetryAttempts != null && maxRetryAttempts >= 0 &&
               minComplianceScore != null && minComplianceScore >= 0.0 && minComplianceScore <= 100.0;
    }

    /**
     * Get effective timeout with retries
     */
    public long getEffectiveTimeoutMs() {
        if (validationTimeoutMs == null || maxRetryAttempts == null) {
            return 30000L;
        }
        return validationTimeoutMs * (maxRetryAttempts + 1);
    }

    /**
     * Check if score meets threshold
     */
    public boolean meetsScoreThreshold(Double score) {
        if (score == null || minComplianceScore == null) {
            return false;
        }
        return score >= minComplianceScore;
    }

    /**
     * Check if notification should be sent for severity level
     */
    public boolean shouldNotify(String severity) {
        if (!isNotificationsEnabled() || severity == null || notificationThreshold == null) {
            return false;
        }

        // Define severity hierarchy
        int severityLevel = getSeverityLevel(severity);
        int thresholdLevel = getSeverityLevel(notificationThreshold);

        return severityLevel >= thresholdLevel;
    }

    private int getSeverityLevel(String severity) {
        switch (severity.toUpperCase()) {
            case "CRITICAL": return 4;
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }
}