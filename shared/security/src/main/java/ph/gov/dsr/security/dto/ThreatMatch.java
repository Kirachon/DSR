package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Threat Match DTO
 * Contains information about threat intelligence matches and indicators
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatMatch {

    /**
     * Unique identifier for the threat match
     */
    private String matchId;

    /**
     * Query that produced this match
     */
    private String query;

    /**
     * Type of query (IP, DOMAIN, HASH, URL, etc.)
     */
    private String queryType;

    /**
     * Matched indicator value
     */
    private String matchedIndicator;

    /**
     * Type of matched indicator
     */
    private String indicatorType;

    /**
     * Threat feed source that provided the match
     */
    private String source;

    /**
     * Feed ID that provided the match
     */
    private String feedId;

    /**
     * Match confidence score (0-100)
     */
    private Integer confidence;

    /**
     * Threat severity level
     */
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    /**
     * Primary threat category
     */
    private String primaryThreatCategory;

    /**
     * List of all threat categories
     */
    private List<String> threatCategories;

    /**
     * Threat description
     */
    private String description;

    /**
     * Malware family (if applicable)
     */
    private String malwareFamily;

    /**
     * Attack type
     */
    private String attackType;

    /**
     * Whether this is an exact match
     */
    private Boolean exactMatch;

    /**
     * Match score (0-100, where 100 is perfect match)
     */
    private Integer matchScore;

    /**
     * First time this indicator was seen
     */
    private LocalDateTime firstSeen;

    /**
     * Last time this indicator was seen
     */
    private LocalDateTime lastSeen;

    /**
     * When this match was found
     */
    private LocalDateTime matchedAt;

    /**
     * Indicator expiration date
     */
    private LocalDateTime expiresAt;

    /**
     * Whether immediate action is recommended
     */
    private Boolean immediateActionRecommended;

    /**
     * Whether blocking is recommended
     */
    private Boolean blockingRecommended;

    /**
     * Whether enhanced monitoring is recommended
     */
    private Boolean enhancedMonitoringRecommended;

    /**
     * Tags associated with this threat
     */
    private List<String> tags;

    /**
     * Additional context information
     */
    private Map<String, Object> context;

    /**
     * Attribution information
     */
    private Map<String, String> attribution;

    /**
     * Related indicators
     */
    private List<String> relatedIndicators;

    /**
     * MITRE ATT&CK techniques
     */
    private List<String> mitreAttackTechniques;

    /**
     * Kill chain phases
     */
    private List<String> killChainPhases;

    /**
     * Geolocation information
     */
    private Map<String, String> geolocation;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if match is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if immediate action is needed
     */
    public boolean needsImmediateAction() {
        return immediateActionRecommended != null && immediateActionRecommended;
    }

    /**
     * Check if blocking is recommended
     */
    public boolean shouldBlock() {
        return blockingRecommended != null && blockingRecommended;
    }

    /**
     * Check if enhanced monitoring is needed
     */
    public boolean needsEnhancedMonitoring() {
        return enhancedMonitoringRecommended != null && enhancedMonitoringRecommended;
    }

    /**
     * Get threat level based on severity
     */
    public String getThreatLevel() {
        if (severity == null) return "UNKNOWN";
        switch (severity.toUpperCase()) {
            case "CRITICAL": return "CRITICAL";
            case "HIGH": return "HIGH";
            case "MEDIUM": return "MEDIUM";
            case "LOW": return "LOW";
            default: return "UNKNOWN";
        }
    }

    /**
     * Get match age in hours
     */
    public long getMatchAgeHours() {
        if (matchedAt == null) return 0;
        return java.time.Duration.between(matchedAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if match is fresh (less than 24 hours old)
     */
    public boolean isFresh() {
        return getMatchAgeHours() < 24;
    }

    /**
     * Check if match is stale (older than 7 days)
     */
    public boolean isStale() {
        return getMatchAgeHours() > 168; // 7 days
    }

    /**
     * Create high-confidence threat match
     */
    public static ThreatMatch highConfidence(String query, String queryType, String matchedIndicator, 
                                           String source, String severity) {
        return ThreatMatch.builder()
                .matchId(java.util.UUID.randomUUID().toString())
                .query(query)
                .queryType(queryType)
                .matchedIndicator(matchedIndicator)
                .indicatorType(queryType)
                .source(source)
                .confidence(90)
                .severity(severity)
                .exactMatch(true)
                .matchScore(95)
                .immediateActionRecommended("CRITICAL".equalsIgnoreCase(severity) || "HIGH".equalsIgnoreCase(severity))
                .blockingRecommended("CRITICAL".equalsIgnoreCase(severity))
                .enhancedMonitoringRecommended(true)
                .matchedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create low-confidence threat match
     */
    public static ThreatMatch lowConfidence(String query, String queryType, String matchedIndicator, String source) {
        return ThreatMatch.builder()
                .matchId(java.util.UUID.randomUUID().toString())
                .query(query)
                .queryType(queryType)
                .matchedIndicator(matchedIndicator)
                .indicatorType(queryType)
                .source(source)
                .confidence(40)
                .severity("LOW")
                .exactMatch(false)
                .matchScore(50)
                .immediateActionRecommended(false)
                .blockingRecommended(false)
                .enhancedMonitoringRecommended(false)
                .matchedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate threat match consistency
     */
    public boolean isValid() {
        return matchId != null && !matchId.trim().isEmpty() &&
               query != null && !query.trim().isEmpty() &&
               queryType != null && !queryType.trim().isEmpty() &&
               matchedIndicator != null && !matchedIndicator.trim().isEmpty() &&
               indicatorType != null && !indicatorType.trim().isEmpty() &&
               source != null && !source.trim().isEmpty() &&
               severity != null && !severity.trim().isEmpty() &&
               exactMatch != null &&
               matchedAt != null &&
               (confidence == null || (confidence >= 0 && confidence <= 100)) &&
               (matchScore == null || (matchScore >= 0 && matchScore <= 100));
    }
}
