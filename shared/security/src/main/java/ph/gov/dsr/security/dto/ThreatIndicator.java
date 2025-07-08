package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Threat Indicator DTO
 * Contains comprehensive threat indicators and IOCs (Indicators of Compromise) data
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatIndicator {

    /**
     * Unique indicator ID
     */
    private String indicatorId;

    /**
     * Indicator value (IP, domain, hash, URL, etc.)
     */
    private String indicatorValue;

    /**
     * Indicator type (IP, DOMAIN, URL, HASH, EMAIL, etc.)
     */
    private String indicatorType;

    /**
     * Threat level associated with this indicator
     */
    private String threatLevel;

    /**
     * Confidence score (0-100)
     */
    private Integer confidenceScore;

    /**
     * Indicator creation timestamp
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Last updated timestamp
     */
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();

    /**
     * First seen timestamp
     */
    private LocalDateTime firstSeen;

    /**
     * Last seen timestamp
     */
    private LocalDateTime lastSeen;

    /**
     * Expiration timestamp (when indicator becomes stale)
     */
    private LocalDateTime expiresAt;

    /**
     * Threat category
     */
    private String threatCategory;

    /**
     * Threat description
     */
    private String threatDescription;

    /**
     * Malware family (if applicable)
     */
    private String malwareFamily;

    /**
     * Attack campaign (if applicable)
     */
    private String attackCampaign;

    /**
     * Threat actor or group
     */
    private String threatActor;

    /**
     * Source of the indicator
     */
    private String source;

    /**
     * Source reliability score (0-100)
     */
    private Integer sourceReliability;

    /**
     * Tags associated with the indicator
     */
    private List<String> tags;

    /**
     * TTPs (Tactics, Techniques, and Procedures)
     */
    private List<String> ttps;

    /**
     * MITRE ATT&CK techniques
     */
    private List<String> mitreAttackTechniques;

    /**
     * Kill chain phases
     */
    private List<String> killChainPhases;

    /**
     * Geographic locations associated with indicator
     */
    private List<String> geographicLocations;

    /**
     * Target industries or sectors
     */
    private List<String> targetIndustries;

    /**
     * Related indicators
     */
    private List<String> relatedIndicators;

    /**
     * Whether indicator is currently active
     */
    @Builder.Default
    private Boolean active = true;

    /**
     * Whether indicator has been verified
     */
    @Builder.Default
    private Boolean verified = false;

    /**
     * Whether indicator is whitelisted
     */
    @Builder.Default
    private Boolean whitelisted = false;

    /**
     * Whether indicator should trigger alerts
     */
    @Builder.Default
    private Boolean alertEnabled = true;

    /**
     * Whether indicator should be blocked automatically
     */
    @Builder.Default
    private Boolean blockingEnabled = false;

    /**
     * Number of times this indicator has been observed
     */
    @Builder.Default
    private Integer observationCount = 0;

    /**
     * Number of false positive reports
     */
    @Builder.Default
    private Integer falsePositiveCount = 0;

    /**
     * Additional context information
     */
    private Map<String, Object> additionalContext;

    /**
     * Indicator metadata
     */
    private Map<String, String> metadata;

    /**
     * References to external reports or sources
     */
    private List<String> references;

    /**
     * Check if indicator is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    /**
     * Check if indicator is verified
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(verified);
    }

    /**
     * Check if indicator is whitelisted
     */
    public boolean isWhitelisted() {
        return Boolean.TRUE.equals(whitelisted);
    }

    /**
     * Check if alerts are enabled
     */
    public boolean isAlertEnabled() {
        return Boolean.TRUE.equals(alertEnabled);
    }

    /**
     * Check if blocking is enabled
     */
    public boolean isBlockingEnabled() {
        return Boolean.TRUE.equals(blockingEnabled);
    }

    /**
     * Check if indicator is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if indicator is high confidence
     */
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 90;
    }

    /**
     * Check if indicator is low confidence
     */
    public boolean isLowConfidence() {
        return confidenceScore != null && confidenceScore < 70;
    }

    /**
     * Check if indicator is high threat level
     */
    public boolean isHighThreatLevel() {
        return "HIGH".equalsIgnoreCase(threatLevel) || "CRITICAL".equalsIgnoreCase(threatLevel);
    }

    /**
     * Check if indicator is critical
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(threatLevel);
    }

    /**
     * Check if source is reliable
     */
    public boolean isReliableSource() {
        return sourceReliability != null && sourceReliability >= 80;
    }

    /**
     * Check if indicator has false positives
     */
    public boolean hasFalsePositives() {
        return falsePositiveCount != null && falsePositiveCount > 0;
    }

    /**
     * Check if indicator has been observed recently (within last 24 hours)
     */
    public boolean isRecentlyObserved() {
        return lastSeen != null && lastSeen.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Get false positive rate as percentage
     */
    public double getFalsePositiveRate() {
        if (observationCount == null || observationCount == 0) {
            return 0.0;
        }
        return (double) (falsePositiveCount != null ? falsePositiveCount : 0) / observationCount * 100.0;
    }

    /**
     * Get indicator age in days
     */
    public long getIndicatorAgeInDays() {
        if (firstSeen == null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
        }
        return java.time.Duration.between(firstSeen, LocalDateTime.now()).toDays();
    }

    /**
     * Get days until expiration
     */
    public Long getDaysUntilExpiration() {
        if (expiresAt == null) {
            return null;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
    }

    /**
     * Get tag count
     */
    public int getTagCount() {
        return tags != null ? tags.size() : 0;
    }

    /**
     * Get TTP count
     */
    public int getTtpCount() {
        return ttps != null ? ttps.size() : 0;
    }

    /**
     * Get MITRE ATT&CK technique count
     */
    public int getMitreAttackTechniqueCount() {
        return mitreAttackTechniques != null ? mitreAttackTechniques.size() : 0;
    }

    /**
     * Get related indicator count
     */
    public int getRelatedIndicatorCount() {
        return relatedIndicators != null ? relatedIndicators.size() : 0;
    }

    /**
     * Get threat indicator summary
     */
    public String getThreatIndicatorSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat Indicator - ");
        summary.append("Type: ").append(indicatorType);
        summary.append(", Value: ").append(indicatorValue);
        summary.append(", Threat Level: ").append(threatLevel);
        summary.append(", Confidence: ").append(confidenceScore).append("%");
        summary.append(", Source Reliability: ").append(sourceReliability).append("%");
        summary.append(", Observations: ").append(observationCount);
        
        if (isCritical()) {
            summary.append(" [CRITICAL]");
        }
        
        if (isExpired()) {
            summary.append(" [EXPIRED]");
        }
        
        if (isWhitelisted()) {
            summary.append(" [WHITELISTED]");
        }
        
        if (isBlockingEnabled()) {
            summary.append(" [AUTO-BLOCK]");
        }
        
        return summary.toString();
    }

    /**
     * Get indicator status summary
     */
    public String getIndicatorStatusSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Status - ");
        summary.append("Active: ").append(isActive() ? "Yes" : "No");
        summary.append(", Verified: ").append(isVerified() ? "Yes" : "No");
        summary.append(", Age: ").append(getIndicatorAgeInDays()).append(" days");
        
        if (getDaysUntilExpiration() != null) {
            summary.append(", Expires in: ").append(getDaysUntilExpiration()).append(" days");
        }
        
        if (hasFalsePositives()) {
            summary.append(", False Positive Rate: ").append(String.format("%.1f%%", getFalsePositiveRate()));
        }
        
        return summary.toString();
    }

    /**
     * Create a high-threat indicator
     */
    public static ThreatIndicator highThreat(String indicatorId, String value, String type, 
                                           String category, int confidence) {
        return ThreatIndicator.builder()
            .indicatorId(indicatorId)
            .indicatorValue(value)
            .indicatorType(type)
            .threatLevel("HIGH")
            .threatCategory(category)
            .confidenceScore(confidence)
            .alertEnabled(true)
            .blockingEnabled(confidence >= 90)
            .active(true)
            .verified(false)
            .createdAt(LocalDateTime.now())
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    /**
     * Create a low-threat indicator
     */
    public static ThreatIndicator lowThreat(String indicatorId, String value, String type, int confidence) {
        return ThreatIndicator.builder()
            .indicatorId(indicatorId)
            .indicatorValue(value)
            .indicatorType(type)
            .threatLevel("LOW")
            .confidenceScore(confidence)
            .alertEnabled(false)
            .blockingEnabled(false)
            .active(true)
            .verified(false)
            .createdAt(LocalDateTime.now())
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    /**
     * Validate threat indicator consistency
     */
    public boolean isValid() {
        return indicatorId != null && !indicatorId.trim().isEmpty() &&
               indicatorValue != null && !indicatorValue.trim().isEmpty() &&
               indicatorType != null && !indicatorType.trim().isEmpty() &&
               threatLevel != null && !threatLevel.trim().isEmpty() &&
               confidenceScore != null && confidenceScore >= 0 && confidenceScore <= 100 &&
               (sourceReliability == null || (sourceReliability >= 0 && sourceReliability <= 100)) &&
               createdAt != null &&
               lastUpdated != null &&
               (observationCount == null || observationCount >= 0) &&
               (falsePositiveCount == null || falsePositiveCount >= 0) &&
               (firstSeen == null || lastSeen == null || !firstSeen.isAfter(lastSeen)) &&
               (expiresAt == null || expiresAt.isAfter(createdAt));
    }
}
