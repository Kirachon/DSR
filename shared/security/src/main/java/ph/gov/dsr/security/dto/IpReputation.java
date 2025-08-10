package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IP Reputation DTO
 * Contains IP address reputation information and scoring
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpReputation {

    /**
     * Reputation assessment ID
     */
    private String reputationId;

    /**
     * IP address being assessed
     */
    private String ipAddress;

    /**
     * Overall reputation score (0-100, where 100 is best reputation)
     */
    private Integer reputationScore;

    /**
     * Reputation category
     */
    private String reputationCategory;

    /**
     * Whether IP is blacklisted
     */
    private Boolean blacklisted;

    /**
     * Whether IP is whitelisted
     */
    private Boolean whitelisted;

    /**
     * Threat categories associated with this IP
     */
    private List<String> threatCategories;

    /**
     * Malicious activities detected
     */
    private List<String> maliciousActivities;

    /**
     * Reputation sources
     */
    private List<String> reputationSources;

    /**
     * First time IP was seen
     */
    private LocalDateTime firstSeen;

    /**
     * Last time IP was seen
     */
    private LocalDateTime lastSeen;

    /**
     * Last reputation update
     */
    private LocalDateTime lastUpdated;

    /**
     * Number of reports against this IP
     */
    private Integer reportCount;

    /**
     * Confidence in reputation assessment (0-100)
     */
    private Integer confidence;

    /**
     * ISP information
     */
    private String isp;

    /**
     * Organization information
     */
    private String organization;

    /**
     * ASN (Autonomous System Number)
     */
    private String asn;

    /**
     * Assessment timestamp
     */
    private LocalDateTime assessedAt;

    /**
     * Additional reputation data
     */
    private Map<String, Object> reputationData;

    /**
     * Check if IP has bad reputation
     */
    public boolean hasBadReputation() {
        return reputationScore != null && reputationScore < 30;
    }

    /**
     * Check if IP has good reputation
     */
    public boolean hasGoodReputation() {
        return reputationScore != null && reputationScore >= 70;
    }

    /**
     * Check if IP should be blocked
     */
    public boolean shouldBlock() {
        return Boolean.TRUE.equals(blacklisted) || hasBadReputation();
    }

    /**
     * Check if IP should be trusted
     */
    public boolean shouldTrust() {
        return Boolean.TRUE.equals(whitelisted) && hasGoodReputation();
    }

    /**
     * Check if enhanced monitoring is needed
     */
    public boolean needsEnhancedMonitoring() {
        return hasBadReputation() || 
               (threatCategories != null && !threatCategories.isEmpty()) ||
               (maliciousActivities != null && !maliciousActivities.isEmpty());
    }

    /**
     * Get reputation level
     */
    public String getReputationLevel() {
        if (reputationScore == null) return "UNKNOWN";
        if (reputationScore >= 80) return "EXCELLENT";
        if (reputationScore >= 60) return "GOOD";
        if (reputationScore >= 40) return "NEUTRAL";
        if (reputationScore >= 20) return "POOR";
        return "MALICIOUS";
    }

    /**
     * Get reputation age in hours
     */
    public long getReputationAgeHours() {
        if (lastUpdated == null) return Long.MAX_VALUE;
        return java.time.Duration.between(lastUpdated, LocalDateTime.now()).toHours();
    }

    /**
     * Check if reputation is stale (older than 24 hours)
     */
    public boolean isStale() {
        return getReputationAgeHours() > 24;
    }

    /**
     * Create bad reputation assessment
     */
    public static IpReputation badReputation(String ipAddress, List<String> threatCategories, int score) {
        return IpReputation.builder()
                .reputationId(java.util.UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .reputationScore(score)
                .reputationCategory("MALICIOUS")
                .blacklisted(true)
                .whitelisted(false)
                .threatCategories(threatCategories)
                .confidence(85)
                .assessedAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Create good reputation assessment
     */
    public static IpReputation goodReputation(String ipAddress, int score) {
        return IpReputation.builder()
                .reputationId(java.util.UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .reputationScore(score)
                .reputationCategory("TRUSTED")
                .blacklisted(false)
                .whitelisted(true)
                .confidence(80)
                .assessedAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Create neutral reputation assessment
     */
    public static IpReputation neutralReputation(String ipAddress) {
        return IpReputation.builder()
                .reputationId(java.util.UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .reputationScore(50)
                .reputationCategory("NEUTRAL")
                .blacklisted(false)
                .whitelisted(false)
                .confidence(70)
                .assessedAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Validate IP reputation consistency
     */
    public boolean isValid() {
        return reputationId != null && !reputationId.trim().isEmpty() &&
               ipAddress != null && !ipAddress.trim().isEmpty() &&
               assessedAt != null &&
               (reputationScore == null || (reputationScore >= 0 && reputationScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (reportCount == null || reportCount >= 0);
    }
}
