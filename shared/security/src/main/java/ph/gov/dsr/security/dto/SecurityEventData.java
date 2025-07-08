package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Security Event Data DTO
 * Contains comprehensive security event data used in threat analysis and detection
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventData {

    /**
     * Unique event identifier
     */
    private String eventId;

    /**
     * Event type (login, logout, access_attempt, data_access, etc.)
     */
    private String eventType;

    /**
     * Event category (authentication, authorization, data_access, network, etc.)
     */
    private String eventCategory;

    /**
     * Event severity level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String severityLevel;

    /**
     * Event timestamp
     */
    private LocalDateTime eventTimestamp;

    /**
     * User ID associated with the event (if applicable)
     */
    private String userId;

    /**
     * Session ID associated with the event (if applicable)
     */
    private String sessionId;

    /**
     * Device ID associated with the event (if applicable)
     */
    private String deviceId;

    /**
     * Source IP address
     */
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "Source IP must be a valid IPv4 or IPv6 address")
    private String sourceIpAddress;

    /**
     * Target IP address (if applicable)
     */
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "Target IP must be a valid IPv4 or IPv6 address")
    private String targetIpAddress;

    /**
     * Source port (if applicable)
     */
    private Integer sourcePort;

    /**
     * Target port (if applicable)
     */
    private Integer targetPort;

    /**
     * Protocol used (HTTP, HTTPS, TCP, UDP, etc.)
     */
    private String protocol;

    /**
     * HTTP method (if applicable)
     */
    private String httpMethod;

    /**
     * Request URL (if applicable)
     */
    private String requestUrl;

    /**
     * User agent string
     */
    private String userAgent;

    /**
     * Referrer URL (if applicable)
     */
    private String referrer;

    /**
     * Geographic location of source
     */
    private String geographicLocation;

    /**
     * Country code of source
     */
    private String countryCode;

    /**
     * Resource accessed (if applicable)
     */
    private String resourceAccessed;

    /**
     * Action performed
     */
    private String actionPerformed;

    /**
     * Result of the action (SUCCESS, FAILURE, BLOCKED, etc.)
     */
    private String actionResult;

    /**
     * Error code (if applicable)
     */
    private String errorCode;

    /**
     * Error message (if applicable)
     */
    private String errorMessage;

    /**
     * Data size transferred (in bytes)
     */
    private Long dataSizeBytes;

    /**
     * Duration of the event (in milliseconds)
     */
    private Long durationMs;

    /**
     * Authentication method used (if applicable)
     */
    private String authenticationMethod;

    /**
     * Whether MFA was used
     */
    private Boolean mfaUsed;

    /**
     * Risk score assigned to the event (0-100)
     */
    private Integer riskScore;

    /**
     * Threat indicators detected
     */
    private String[] threatIndicators;

    /**
     * Anomaly flags detected
     */
    private String[] anomalyFlags;

    /**
     * Security policies violated (if any)
     */
    private String[] policyViolations;

    /**
     * Whether event was blocked by security controls
     */
    @Builder.Default
    private Boolean blockedBySecurityControls = false;

    /**
     * Security control that blocked the event (if applicable)
     */
    private String blockingSecurityControl;

    /**
     * Whether event triggered an alert
     */
    @Builder.Default
    private Boolean alertTriggered = false;

    /**
     * Alert ID (if alert was triggered)
     */
    private String alertId;

    /**
     * Event source system
     */
    private String sourceSystem;

    /**
     * Event correlation ID
     */
    private String correlationId;

    /**
     * Additional event metadata
     */
    private Map<String, Object> eventMetadata;

    /**
     * HTTP headers (if applicable)
     */
    private Map<String, String> httpHeaders;

    /**
     * Request parameters (if applicable)
     */
    private Map<String, String> requestParameters;

    /**
     * Response data (if applicable)
     */
    private Map<String, Object> responseData;

    /**
     * Check if event is high severity
     */
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(severityLevel) || "CRITICAL".equalsIgnoreCase(severityLevel);
    }

    /**
     * Check if event is critical
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(severityLevel);
    }

    /**
     * Check if event was successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(actionResult);
    }

    /**
     * Check if event was blocked
     */
    public boolean wasBlocked() {
        return Boolean.TRUE.equals(blockedBySecurityControls) || "BLOCKED".equalsIgnoreCase(actionResult);
    }

    /**
     * Check if event triggered an alert
     */
    public boolean triggeredAlert() {
        return Boolean.TRUE.equals(alertTriggered);
    }

    /**
     * Check if MFA was used
     */
    public boolean usedMfa() {
        return Boolean.TRUE.equals(mfaUsed);
    }

    /**
     * Check if event has threat indicators
     */
    public boolean hasThreatIndicators() {
        return threatIndicators != null && threatIndicators.length > 0;
    }

    /**
     * Check if event has anomaly flags
     */
    public boolean hasAnomalyFlags() {
        return anomalyFlags != null && anomalyFlags.length > 0;
    }

    /**
     * Check if event has policy violations
     */
    public boolean hasPolicyViolations() {
        return policyViolations != null && policyViolations.length > 0;
    }

    /**
     * Check if event is from external source
     */
    public boolean isFromExternalSource() {
        if (sourceIpAddress == null) {
            return false;
        }
        
        // Check if IP is private (RFC 1918)
        return !isPrivateIpAddress(sourceIpAddress);
    }

    /**
     * Check if IP address is private
     */
    private boolean isPrivateIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        
        // Simple check for common private IP ranges
        return ipAddress.startsWith("10.") ||
               ipAddress.startsWith("192.168.") ||
               (ipAddress.startsWith("172.") && 
                ipAddress.split("\\.").length > 1 &&
                Integer.parseInt(ipAddress.split("\\.")[1]) >= 16 &&
                Integer.parseInt(ipAddress.split("\\.")[1]) <= 31) ||
               ipAddress.equals("127.0.0.1") ||
               ipAddress.equals("::1");
    }

    /**
     * Get threat indicator count
     */
    public int getThreatIndicatorCount() {
        return threatIndicators != null ? threatIndicators.length : 0;
    }

    /**
     * Get anomaly flag count
     */
    public int getAnomalyFlagCount() {
        return anomalyFlags != null ? anomalyFlags.length : 0;
    }

    /**
     * Get policy violation count
     */
    public int getPolicyViolationCount() {
        return policyViolations != null ? policyViolations.length : 0;
    }

    /**
     * Get event duration in a human-readable format
     */
    public String getFormattedDuration() {
        if (durationMs == null) {
            return "Unknown";
        }
        
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.1fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Get data size in a human-readable format
     */
    public String getFormattedDataSize() {
        if (dataSizeBytes == null) {
            return "Unknown";
        }
        
        if (dataSizeBytes < 1024) {
            return dataSizeBytes + " bytes";
        } else if (dataSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", dataSizeBytes / 1024.0);
        } else if (dataSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", dataSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", dataSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get event summary for logging
     */
    public String getEventSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Event: ").append(eventType);
        summary.append(" (").append(eventCategory).append(")");
        summary.append(" - Severity: ").append(severityLevel);
        summary.append(", Result: ").append(actionResult);
        summary.append(", Source: ").append(sourceIpAddress);
        
        if (userId != null) {
            summary.append(", User: ").append(userId);
        }
        
        if (hasThreatIndicators()) {
            summary.append(" [").append(getThreatIndicatorCount()).append(" threats]");
        }
        
        if (wasBlocked()) {
            summary.append(" [BLOCKED]");
        }
        
        if (triggeredAlert()) {
            summary.append(" [ALERT]");
        }
        
        return summary.toString();
    }

    /**
     * Validate event data consistency
     */
    public boolean isValid() {
        return eventId != null && !eventId.trim().isEmpty() &&
               eventType != null && !eventType.trim().isEmpty() &&
               eventCategory != null && !eventCategory.trim().isEmpty() &&
               severityLevel != null && !severityLevel.trim().isEmpty() &&
               eventTimestamp != null &&
               (riskScore == null || (riskScore >= 0 && riskScore <= 100)) &&
               (sourcePort == null || (sourcePort >= 0 && sourcePort <= 65535)) &&
               (targetPort == null || (targetPort >= 0 && targetPort <= 65535)) &&
               (durationMs == null || durationMs >= 0) &&
               (dataSizeBytes == null || dataSizeBytes >= 0);
    }

    /**
     * Get source IP address (convenience method for anomaly detection)
     */
    public String getSourceIp() {
        return sourceIpAddress;
    }

    /**
     * Get event timestamp (convenience method for anomaly detection)
     */
    public LocalDateTime getTimestamp() {
        return eventTimestamp;
    }
}
