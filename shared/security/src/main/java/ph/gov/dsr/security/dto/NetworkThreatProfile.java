package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Network Threat Profile DTO
 * Contains comprehensive network-based threat profiling and risk assessment data
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkThreatProfile {

    /**
     * Unique identifier for the network threat profile
     */
    private String profileId;

    /**
     * IP address being profiled
     */
    private String ipAddress;

    /**
     * Network range or subnet
     */
    private String networkRange;

    /**
     * Geographic location information
     */
    private String geographicLocation;

    /**
     * Country code
     */
    private String countryCode;

    /**
     * ISP or organization
     */
    private String isp;

    /**
     * Overall threat level
     */
    private String threatLevel; // LOW, MEDIUM, HIGH, CRITICAL

    /**
     * Risk score (0-100, where 100 is highest risk)
     */
    private Integer riskScore;

    /**
     * Reputation score (0-100, where 100 is best reputation)
     */
    private Integer reputationScore;

    /**
     * Whether the IP is on a blacklist
     */
    private Boolean blacklisted;

    /**
     * Whether the IP is on a whitelist
     */
    private Boolean whitelisted;

    /**
     * List of threat categories associated with this network
     */
    private List<String> threatCategories;

    /**
     * List of malicious activities detected
     */
    private List<String> maliciousActivities;

    /**
     * List of attack patterns observed
     */
    private List<String> attackPatterns;

    /**
     * Network behavior indicators
     */
    private List<String> behaviorIndicators;

    /**
     * Traffic analysis results
     */
    private Map<String, Object> trafficAnalysis;

    /**
     * Port scan activities
     */
    private Map<String, Object> portScanActivity;

    /**
     * DDoS attack indicators
     */
    private Map<String, Object> ddosIndicators;

    /**
     * Botnet membership indicators
     */
    private Map<String, Object> botnetIndicators;

    /**
     * First time this IP was seen
     */
    private LocalDateTime firstSeen;

    /**
     * Last time this IP was seen
     */
    private LocalDateTime lastSeen;

    /**
     * Last time threat intelligence was updated
     */
    private LocalDateTime lastThreatUpdate;

    /**
     * Number of security incidents associated with this IP
     */
    private Integer incidentCount;

    /**
     * Number of blocked connections from this IP
     */
    private Long blockedConnections;

    /**
     * Number of successful connections from this IP
     */
    private Long successfulConnections;

    /**
     * Whether enhanced monitoring is required
     */
    private Boolean enhancedMonitoringRequired;

    /**
     * Whether immediate blocking is recommended
     */
    private Boolean immediateBlockingRecommended;

    /**
     * Confidence level of the threat assessment (0-100)
     */
    private Integer confidence;

    /**
     * Data sources used for profiling
     */
    private List<String> dataSources;

    /**
     * Profile creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last profile update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Profile expiration timestamp
     */
    private LocalDateTime expiresAt;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if profile is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if IP should be blocked
     */
    public boolean shouldBlock() {
        return immediateBlockingRecommended != null && immediateBlockingRecommended;
    }

    /**
     * Check if enhanced monitoring is needed
     */
    public boolean needsEnhancedMonitoring() {
        return enhancedMonitoringRequired != null && enhancedMonitoringRequired;
    }

    /**
     * Get threat category based on risk score
     */
    public String getThreatCategory() {
        if (riskScore == null) return "UNKNOWN";
        if (riskScore >= 80) return "HIGH_RISK";
        if (riskScore >= 60) return "MEDIUM_RISK";
        if (riskScore >= 40) return "LOW_RISK";
        return "MINIMAL_RISK";
    }

    /**
     * Calculate profile age in hours
     */
    public long getProfileAgeHours() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if profile is stale (older than 7 days)
     */
    public boolean isStale() {
        return getProfileAgeHours() > 168; // 7 days
    }

    /**
     * Create high-risk network profile
     */
    public static NetworkThreatProfile highRisk(String ipAddress, List<String> threatCategories) {
        return NetworkThreatProfile.builder()
                .profileId(java.util.UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .threatLevel("HIGH")
                .riskScore(85)
                .reputationScore(20)
                .blacklisted(true)
                .threatCategories(threatCategories)
                .enhancedMonitoringRequired(true)
                .immediateBlockingRecommended(true)
                .confidence(80)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    /**
     * Create low-risk network profile
     */
    public static NetworkThreatProfile lowRisk(String ipAddress) {
        return NetworkThreatProfile.builder()
                .profileId(java.util.UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .threatLevel("LOW")
                .riskScore(20)
                .reputationScore(80)
                .blacklisted(false)
                .whitelisted(false)
                .enhancedMonitoringRequired(false)
                .immediateBlockingRecommended(false)
                .confidence(70)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(90))
                .build();
    }

    /**
     * Validate network threat profile consistency
     */
    public boolean isValid() {
        return profileId != null && !profileId.trim().isEmpty() &&
               ipAddress != null && !ipAddress.trim().isEmpty() &&
               threatLevel != null && !threatLevel.trim().isEmpty() &&
               createdAt != null &&
               (riskScore == null || (riskScore >= 0 && riskScore <= 100)) &&
               (reputationScore == null || (reputationScore >= 0 && reputationScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (incidentCount == null || incidentCount >= 0) &&
               (blockedConnections == null || blockedConnections >= 0) &&
               (successfulConnections == null || successfulConnections >= 0);
    }
}
