package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Threat Feed DTO
 * Contains threat intelligence feed information and indicators
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatFeed {

    /**
     * Unique identifier for the threat feed
     */
    private String feedId;

    /**
     * Name of the threat feed
     */
    private String feedName;

    /**
     * Source of the threat feed
     */
    private String source;

    /**
     * Type of threat feed (IOC, MALWARE, PHISHING, etc.)
     */
    private String feedType;

    /**
     * Feed reliability score (0-100)
     */
    private Integer reliabilityScore;

    /**
     * Whether the feed is currently active
     */
    private Boolean active;

    /**
     * Feed URL or endpoint
     */
    private String feedUrl;

    /**
     * Feed format (JSON, XML, CSV, etc.)
     */
    private String feedFormat;

    /**
     * Update frequency in hours
     */
    private Integer updateFrequencyHours;

    /**
     * Last update timestamp
     */
    private LocalDateTime lastUpdated;

    /**
     * Next scheduled update
     */
    private LocalDateTime nextUpdate;

    /**
     * Number of indicators in the feed
     */
    private Long indicatorCount;

    /**
     * Number of fresh indicators (less than 24 hours old)
     */
    private Long freshIndicatorCount;

    /**
     * Feed categories
     */
    private List<String> categories;

    /**
     * Supported indicator types
     */
    private List<String> supportedIndicatorTypes;

    /**
     * Feed configuration parameters
     */
    private Map<String, Object> configuration;

    /**
     * Feed statistics
     */
    private Map<String, Object> statistics;

    /**
     * Authentication credentials (if required)
     */
    private Map<String, String> credentials;

    /**
     * Feed description
     */
    private String description;

    /**
     * Feed provider information
     */
    private String provider;

    /**
     * Feed license information
     */
    private String license;

    /**
     * Feed creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    private LocalDateTime modifiedAt;

    /**
     * Feed status (ACTIVE, INACTIVE, ERROR, MAINTENANCE)
     */
    private String status;

    /**
     * Error message if feed is in error state
     */
    private String errorMessage;

    /**
     * Check if feed needs update
     */
    public boolean needsUpdate() {
        if (nextUpdate == null) return true;
        return LocalDateTime.now().isAfter(nextUpdate);
    }

    /**
     * Check if feed is healthy
     */
    public boolean isHealthy() {
        return active && "ACTIVE".equals(status) && errorMessage == null;
    }

    /**
     * Get feed age in hours
     */
    public long getFeedAgeHours() {
        if (lastUpdated == null) return Long.MAX_VALUE;
        return java.time.Duration.between(lastUpdated, LocalDateTime.now()).toHours();
    }

    /**
     * Check if feed is stale (older than 48 hours)
     */
    public boolean isStale() {
        return getFeedAgeHours() > 48;
    }

    /**
     * Get feed freshness percentage
     */
    public double getFreshnessPercentage() {
        if (indicatorCount == null || indicatorCount == 0) return 0.0;
        if (freshIndicatorCount == null) return 0.0;
        return (double) freshIndicatorCount / indicatorCount * 100.0;
    }

    /**
     * Create active threat feed
     */
    public static ThreatFeed createActive(String feedId, String feedName, String source, String feedType) {
        return ThreatFeed.builder()
                .feedId(feedId)
                .feedName(feedName)
                .source(source)
                .feedType(feedType)
                .active(true)
                .status("ACTIVE")
                .reliabilityScore(75)
                .updateFrequencyHours(24)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create inactive threat feed
     */
    public static ThreatFeed createInactive(String feedId, String feedName, String reason) {
        return ThreatFeed.builder()
                .feedId(feedId)
                .feedName(feedName)
                .active(false)
                .status("INACTIVE")
                .errorMessage(reason)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate threat feed consistency
     */
    public boolean isValid() {
        return feedId != null && !feedId.trim().isEmpty() &&
               feedName != null && !feedName.trim().isEmpty() &&
               source != null && !source.trim().isEmpty() &&
               feedType != null && !feedType.trim().isEmpty() &&
               active != null &&
               createdAt != null &&
               (reliabilityScore == null || (reliabilityScore >= 0 && reliabilityScore <= 100)) &&
               (updateFrequencyHours == null || updateFrequencyHours >= 1) &&
               (indicatorCount == null || indicatorCount >= 0) &&
               (freshIndicatorCount == null || freshIndicatorCount >= 0);
    }
}
