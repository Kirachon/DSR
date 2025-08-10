package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for security scan operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityScanRequest {

    private String scanType; // VULNERABILITY, PENETRATION, CODE_QUALITY, NETWORK

    private String scanTool; // OWASP_ZAP, NESSUS, SONARQUBE, CUSTOM

    private String targetType; // WEB_APPLICATION, NETWORK, CODE_REPOSITORY

    private String targetIdentifier; // URL, IP range, repository name

    private String scanProfile; // QUICK, FULL, CUSTOM

    private UUID initiatedBy;

    private String initiatedByUsername;

    private Boolean scheduled;

    private Boolean recurring;

    private String cronExpression;

    private LocalDateTime scheduledStartTime;

    private String scanConfiguration; // JSON configuration

    private String scanParameters; // JSON parameters

    private String includePaths; // JSON array of paths to include

    private String excludePaths; // JSON array of paths to exclude

    private String scanRules; // JSON array of scan rules to apply

    private String customRules; // JSON array of custom rules

    private Integer maxDurationMinutes;

    private Integer maxConcurrentScans;

    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    private String businessJustification;

    private String complianceFrameworks; // JSON array of compliance requirements

    private Boolean baselineScan;

    private UUID baselineScanId;

    private Boolean compareWithBaseline;

    private String notificationRecipients; // JSON array of email addresses

    private Boolean notifyOnCompletion;

    private Boolean notifyOnFailure;

    private Boolean notifyOnHighFindings;

    private String reportFormat; // HTML, PDF, JSON, XML

    private String reportTemplate;

    private Boolean generateExecutiveSummary;

    private Boolean includeRemediation;

    private Boolean includeFalsePositives;

    private String tags; // JSON array of tags

    private String metadata; // JSON object with additional metadata

    private String environmentType; // DEVELOPMENT, STAGING, PRODUCTION

    private String applicationVersion;

    private String networkSegment;

    private String accessCredentials; // Encrypted credentials for authenticated scans

    private Boolean authenticatedScan;

    private String authenticationMethod; // BASIC, OAUTH, API_KEY, CERTIFICATE

    private String proxyConfiguration; // JSON proxy settings

    private Boolean useProxy;

    private String userAgent;

    private Integer requestDelay; // Delay between requests in milliseconds

    private Integer maxRetries;

    private String correlationId;

    private String sourceSystem;

    private String additionalContext; // JSON object with additional context

    /**
     * Get target URL for web application scans
     */
    public String getTargetUrl() {
        return targetIdentifier;
    }

    /**
     * Get user ID who initiated the scan
     */
    public UUID getUserId() {
        return initiatedBy;
    }

    /**
     * Get scan policy configuration
     */
    public String getScanPolicy() {
        return scanConfiguration;
    }

    /**
     * Get authentication configuration
     */
    public String getAuthenticationConfig() {
        return scanParameters;
    }

    /**
     * Get exclude URLs list
     */
    public String getExcludeUrls() {
        return excludePaths;
    }

    /**
     * Get network targets for network scans
     */
    public String getNetworkTargets() {
        return targetIdentifier;
    }

    /**
     * Get network scan template
     */
    public String getNetworkScanTemplate() {
        return scanProfile;
    }

    /**
     * Get network credentials
     */
    public String getNetworkCredentials() {
        return scanParameters;
    }

    /**
     * Get project key for code quality scans
     */
    public String getProjectKey() {
        return targetIdentifier;
    }

    /**
     * Get source directory for code scans
     */
    public String getSourceDirectory() {
        return includePaths;
    }

    /**
     * Get quality gate configuration
     */
    public String getQualityGate() {
        return scanRules;
    }

    /**
     * Validation method to check if the request is valid
     */
    public boolean isValid() {
        return scanType != null && !scanType.trim().isEmpty() &&
               scanTool != null && !scanTool.trim().isEmpty() &&
               targetType != null && !targetType.trim().isEmpty() &&
               targetIdentifier != null && !targetIdentifier.trim().isEmpty() &&
               initiatedBy != null;
    }

    /**
     * Check if this is a high-priority scan
     */
    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "CRITICAL".equals(priority);
    }

    /**
     * Check if this scan requires authentication
     */
    public boolean requiresAuthentication() {
        return Boolean.TRUE.equals(authenticatedScan) &&
               accessCredentials != null && !accessCredentials.trim().isEmpty();
    }

    /**
     * Check if this is a production environment scan
     */
    public boolean isProductionScan() {
        return "PRODUCTION".equals(environmentType);
    }

    /**
     * Check if notifications should be sent
     */
    public boolean shouldSendNotifications() {
        return Boolean.TRUE.equals(notifyOnCompletion) ||
               Boolean.TRUE.equals(notifyOnFailure) ||
               Boolean.TRUE.equals(notifyOnHighFindings);
    }

    /**
     * Get estimated scan duration based on scan type and profile
     */
    public int getEstimatedDurationMinutes() {
        if (maxDurationMinutes != null) {
            return maxDurationMinutes;
        }

        // Default estimates based on scan type and profile
        int baseTime = 30; // Default 30 minutes

        switch (scanType.toUpperCase()) {
            case "VULNERABILITY":
                baseTime = "QUICK".equals(scanProfile) ? 15 : "FULL".equals(scanProfile) ? 120 : 60;
                break;
            case "PENETRATION":
                baseTime = "QUICK".equals(scanProfile) ? 60 : "FULL".equals(scanProfile) ? 480 : 240;
                break;
            case "CODE_QUALITY":
                baseTime = "QUICK".equals(scanProfile) ? 10 : "FULL".equals(scanProfile) ? 60 : 30;
                break;
            case "NETWORK":
                baseTime = "QUICK".equals(scanProfile) ? 30 : "FULL".equals(scanProfile) ? 180 : 90;
                break;
        }

        return baseTime;
    }
}
