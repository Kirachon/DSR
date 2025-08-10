package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Threat Context DTO
 * Contains contextual information about threats and security events
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatContext {

    /**
     * Context ID
     */
    private String contextId;

    /**
     * User ID associated with the threat
     */
    private String userId;

    /**
     * Session ID
     */
    private String sessionId;

    /**
     * Source IP address
     */
    private String sourceIp;

    /**
     * User agent
     */
    private String userAgent;

    /**
     * Geographic location
     */
    private Map<String, String> geolocation;

    /**
     * Device information
     */
    private Map<String, Object> deviceInfo;

    /**
     * Network information
     */
    private Map<String, Object> networkInfo;

    /**
     * Application context
     */
    private Map<String, Object> applicationContext;

    /**
     * Security events
     */
    private List<String> securityEvents;

    /**
     * Threat indicators
     */
    private List<String> threatIndicators;

    /**
     * Risk factors
     */
    private List<String> riskFactors;

    /**
     * Environmental factors
     */
    private Map<String, Object> environmentalFactors;

    /**
     * Temporal context
     */
    private Map<String, Object> temporalContext;

    /**
     * Behavioral context
     */
    private Map<String, Object> behavioralContext;

    /**
     * Context timestamp
     */
    private LocalDateTime contextTimestamp;

    /**
     * Context expiration
     */
    private LocalDateTime expiresAt;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if context is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if context has high-risk indicators
     */
    public boolean hasHighRiskIndicators() {
        return threatIndicators != null && !threatIndicators.isEmpty();
    }

    /**
     * Get context age in minutes
     */
    public long getContextAgeMinutes() {
        if (contextTimestamp == null) return 0;
        return java.time.Duration.between(contextTimestamp, LocalDateTime.now()).toMinutes();
    }

    /**
     * Check if context is fresh (less than 1 hour old)
     */
    public boolean isFresh() {
        return getContextAgeMinutes() < 60;
    }

    /**
     * Create threat context
     */
    public static ThreatContext create(String userId, String sourceIp) {
        return ThreatContext.builder()
                .contextId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .sourceIp(sourceIp)
                .contextTimestamp(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    /**
     * Validate context consistency
     */
    public boolean isValid() {
        return contextId != null && !contextId.trim().isEmpty() &&
               contextTimestamp != null;
    }
}
