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
 * Access Request DTO
 * Contains all information needed for zero-trust access evaluation including user, device, and network context
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequest {

    /**
     * User ID requesting access
     */
    private String userId;

    /**
     * Device ID making the request
     */
    private String deviceId;

    /**
     * Resource ID being requested
     */
    private String resourceId;

    /**
     * Session ID for the request
     */
    private String sessionId;

    /**
     * Source IP address of the request
     */
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "Source IP must be a valid IPv4 or IPv6 address")
    private String sourceIp;

    /**
     * User agent string from the request
     */
    private String userAgent;

    /**
     * HTTP method of the request
     */
    private String httpMethod;

    /**
     * Request URI/path
     */
    private String requestUri;

    /**
     * Timestamp when request was made
     */
    @Builder.Default
    private LocalDateTime requestTime = LocalDateTime.now();

    /**
     * User role/authority
     */
    private String userRole;

    /**
     * User permissions
     */
    private String[] permissions;

    /**
     * Geographic location information
     */
    private String geolocation;

    /**
     * Network context information (VPN, proxy, etc.)
     */
    private Map<String, Object> networkContext;

    /**
     * Device fingerprint information
     */
    private Map<String, Object> deviceFingerprint;

    /**
     * Request context (headers, parameters, etc.)
     */
    private Map<String, Object> requestContext;

    /**
     * Authentication method used
     */
    private String authenticationMethod;

    /**
     * Multi-factor authentication status
     */
    private Boolean mfaCompleted;

    /**
     * Previous session information
     */
    private String previousSessionId;

    /**
     * Time since last authentication
     */
    private Long timeSinceLastAuth;

    /**
     * Risk indicators detected
     */
    private String[] riskIndicators;

    /**
     * Business context for the request
     */
    private String businessContext;

    /**
     * Priority level of the request
     */
    private String priority;

    /**
     * Whether this is a sensitive operation
     */
    @Builder.Default
    private Boolean sensitiveOperation = false;

    /**
     * Expected response time requirement
     */
    private Integer expectedResponseTimeMs;

    /**
     * Correlation ID for request tracking
     */
    private String correlationId;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if request is from a mobile device
     */
    public boolean isMobileDevice() {
        if (userAgent == null) {
            return false;
        }
        String ua = userAgent.toLowerCase();
        return ua.contains("mobile") || ua.contains("android") || ua.contains("iphone") || ua.contains("ipad");
    }

    /**
     * Check if request is using VPN
     */
    public boolean isUsingVpn() {
        return networkContext != null && 
               Boolean.TRUE.equals(networkContext.get("usingVpn"));
    }

    /**
     * Check if request is using proxy
     */
    public boolean isUsingProxy() {
        return networkContext != null && 
               Boolean.TRUE.equals(networkContext.get("usingProxy"));
    }

    /**
     * Check if MFA was completed
     */
    public boolean isMfaCompleted() {
        return Boolean.TRUE.equals(mfaCompleted);
    }

    /**
     * Check if this is a sensitive operation
     */
    public boolean isSensitiveOperation() {
        return Boolean.TRUE.equals(sensitiveOperation);
    }

    /**
     * Check if request has risk indicators
     */
    public boolean hasRiskIndicators() {
        return riskIndicators != null && riskIndicators.length > 0;
    }

    /**
     * Get risk indicator count
     */
    public int getRiskIndicatorCount() {
        return riskIndicators != null ? riskIndicators.length : 0;
    }

    /**
     * Check if request is from new device
     */
    public boolean isNewDevice() {
        return deviceFingerprint != null && 
               Boolean.TRUE.equals(deviceFingerprint.get("isNewDevice"));
    }

    /**
     * Check if request is from new location
     */
    public boolean isNewLocation() {
        return networkContext != null && 
               Boolean.TRUE.equals(networkContext.get("isNewLocation"));
    }

    /**
     * Get time since last authentication in minutes
     */
    public long getTimeSinceLastAuthMinutes() {
        return timeSinceLastAuth != null ? timeSinceLastAuth / 60000 : 0; // Convert ms to minutes
    }

    /**
     * Check if authentication is stale (more than specified minutes)
     */
    public boolean isAuthenticationStale(int maxMinutes) {
        return getTimeSinceLastAuthMinutes() > maxMinutes;
    }

    /**
     * Get request summary for logging
     */
    public String getRequestSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("User ").append(userId);
        summary.append(" from device ").append(deviceId);
        summary.append(" requesting ").append(resourceId);
        summary.append(" from IP ").append(sourceIp);
        
        if (isSensitiveOperation()) {
            summary.append(" (SENSITIVE)");
        }
        
        if (hasRiskIndicators()) {
            summary.append(" (").append(getRiskIndicatorCount()).append(" risk indicators)");
        }
        
        return summary.toString();
    }

    /**
     * Get security context summary
     */
    public String getSecurityContextSummary() {
        StringBuilder context = new StringBuilder();
        context.append("Auth: ").append(authenticationMethod);
        context.append(", MFA: ").append(isMfaCompleted() ? "Yes" : "No");
        context.append(", Device: ").append(isNewDevice() ? "New" : "Known");
        context.append(", Location: ").append(isNewLocation() ? "New" : "Known");
        
        if (isUsingVpn()) {
            context.append(", VPN: Yes");
        }
        
        if (isUsingProxy()) {
            context.append(", Proxy: Yes");
        }
        
        return context.toString();
    }

    /**
     * Validate request completeness
     */
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty() &&
               deviceId != null && !deviceId.trim().isEmpty() &&
               resourceId != null && !resourceId.trim().isEmpty() &&
               sessionId != null && !sessionId.trim().isEmpty() &&
               sourceIp != null && !sourceIp.trim().isEmpty() &&
               requestTime != null;
    }

    /**
     * Create a basic access request
     */
    public static AccessRequest basic(String userId, String deviceId, String resourceId, String sessionId, String sourceIp) {
        return AccessRequest.builder()
            .userId(userId)
            .deviceId(deviceId)
            .resourceId(resourceId)
            .sessionId(sessionId)
            .sourceIp(sourceIp)
            .requestTime(LocalDateTime.now())
            .build();
    }
}
