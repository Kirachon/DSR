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
 * Threat Intelligence Result DTO
 * Contains comprehensive threat intelligence lookup results including IOCs and threat data
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatIntelligenceResult {

    /**
     * Unique lookup result ID
     */
    private String lookupResultId;

    /**
     * Query that was performed
     */
    private String query;

    /**
     * Query type (IP, DOMAIN, URL, HASH, etc.)
     */
    private String queryType;

    /**
     * Whether threat intelligence was found
     */
    private Boolean threatFound;

    /**
     * Threat confidence score (0-100)
     */
    private Integer threatConfidence;

    /**
     * Lookup timestamp
     */
    @Builder.Default
    private LocalDateTime lookupTime = LocalDateTime.now();

    /**
     * Query response time in milliseconds
     */
    private Long responseTimeMs;

    /**
     * Primary threat category
     */
    private String primaryThreatCategory;

    /**
     * Secondary threat categories
     */
    private List<String> secondaryThreatCategories;

    /**
     * Threat severity level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String threatSeverity;

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
     * First seen timestamp
     */
    private LocalDateTime firstSeen;

    /**
     * Last seen timestamp
     */
    private LocalDateTime lastSeen;

    /**
     * Threat intelligence sources that provided the data
     */
    private List<String> intelligenceSources;

    /**
     * Source reliability scores
     */
    private Map<String, Integer> sourceReliabilityScores;

    /**
     * IOCs (Indicators of Compromise) found
     */
    private List<String> iocsFound;

    /**
     * Related IOCs
     */
    private List<String> relatedIocs;

    /**
     * TTPs (Tactics, Techniques, and Procedures)
     */
    private List<String> ttps;

    /**
     * MITRE ATT&CK techniques
     */
    private List<String> mitreAttackTechniques;

    /**
     * Geographic locations associated with threat
     */
    private List<String> geographicLocations;

    /**
     * Target industries or sectors
     */
    private List<String> targetIndustries;

    /**
     * Recommended actions based on intelligence
     */
    private List<String> recommendedActions;

    /**
     * Mitigation strategies
     */
    private List<String> mitigationStrategies;

    /**
     * Whether immediate blocking is recommended
     */
    @Builder.Default
    private Boolean immediateBlockingRecommended = false;

    /**
     * Whether enhanced monitoring is recommended
     */
    @Builder.Default
    private Boolean enhancedMonitoringRecommended = false;

    /**
     * Whether incident response is required
     */
    @Builder.Default
    private Boolean incidentResponseRequired = false;

    /**
     * Threat intelligence feed sources
     */
    private List<String> feedSources;

    /**
     * Data freshness (how recent the intelligence is)
     */
    private String dataFreshness;

    /**
     * Attribution confidence level
     */
    private Integer attributionConfidence;

    /**
     * Additional threat context
     */
    private Map<String, Object> additionalContext;

    /**
     * Related threat reports or references
     */
    private List<String> relatedReports;

    /**
     * Check if threat was found
     */
    public boolean isThreatFound() {
        return Boolean.TRUE.equals(threatFound);
    }

    /**
     * Check if immediate blocking is recommended
     */
    public boolean needsImmediateBlocking() {
        return Boolean.TRUE.equals(immediateBlockingRecommended);
    }

    /**
     * Check if enhanced monitoring is recommended
     */
    public boolean needsEnhancedMonitoring() {
        return Boolean.TRUE.equals(enhancedMonitoringRecommended);
    }

    /**
     * Check if incident response is required
     */
    public boolean needsIncidentResponse() {
        return Boolean.TRUE.equals(incidentResponseRequired);
    }

    /**
     * Check if threat is high severity
     */
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(threatSeverity) || "CRITICAL".equalsIgnoreCase(threatSeverity);
    }

    /**
     * Check if threat is critical
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(threatSeverity);
    }

    /**
     * Check if threat confidence is high
     */
    public boolean isHighConfidence() {
        return threatConfidence != null && threatConfidence >= 90;
    }

    /**
     * Check if threat confidence is low
     */
    public boolean isLowConfidence() {
        return threatConfidence != null && threatConfidence < 70;
    }

    /**
     * Check if attribution confidence is high
     */
    public boolean isHighAttributionConfidence() {
        return attributionConfidence != null && attributionConfidence >= 90;
    }

    /**
     * Check if there are IOCs
     */
    public boolean hasIocs() {
        return iocsFound != null && !iocsFound.isEmpty();
    }

    /**
     * Check if there are TTPs
     */
    public boolean hasTtps() {
        return ttps != null && !ttps.isEmpty();
    }

    /**
     * Check if there are MITRE ATT&CK techniques
     */
    public boolean hasMitreAttackTechniques() {
        return mitreAttackTechniques != null && !mitreAttackTechniques.isEmpty();
    }

    /**
     * Check if there are recommended actions
     */
    public boolean hasRecommendedActions() {
        return recommendedActions != null && !recommendedActions.isEmpty();
    }

    /**
     * Get IOC count
     */
    public int getIocCount() {
        return iocsFound != null ? iocsFound.size() : 0;
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
     * Get intelligence source count
     */
    public int getIntelligenceSourceCount() {
        return intelligenceSources != null ? intelligenceSources.size() : 0;
    }

    /**
     * Get response time in a human-readable format
     */
    public String getFormattedResponseTime() {
        if (responseTimeMs == null) {
            return "Unknown";
        }
        
        if (responseTimeMs < 1000) {
            return responseTimeMs + "ms";
        } else if (responseTimeMs < 60000) {
            return String.format("%.1fs", responseTimeMs / 1000.0);
        } else {
            long minutes = responseTimeMs / 60000;
            long seconds = (responseTimeMs % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Get average source reliability score
     */
    public Double getAverageSourceReliability() {
        if (sourceReliabilityScores == null || sourceReliabilityScores.isEmpty()) {
            return null;
        }
        
        return sourceReliabilityScores.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }

    /**
     * Get threat intelligence summary
     */
    public String getThreatIntelligenceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat Intelligence Lookup - ");
        summary.append("Query: ").append(query);
        summary.append(" (").append(queryType).append(")");
        
        if (isThreatFound()) {
            summary.append(" - THREAT FOUND");
            summary.append(", Category: ").append(primaryThreatCategory != null ? primaryThreatCategory : "Unknown");
            summary.append(", Severity: ").append(threatSeverity);
            summary.append(", Confidence: ").append(threatConfidence).append("%");
            
            if (isCritical()) {
                summary.append(" [CRITICAL]");
            }
            
            if (needsImmediateBlocking()) {
                summary.append(" [BLOCK IMMEDIATELY]");
            }
            
            if (needsIncidentResponse()) {
                summary.append(" [INCIDENT RESPONSE]");
            }
        } else {
            summary.append(" - NO THREAT FOUND");
        }
        
        summary.append(", Response Time: ").append(getFormattedResponseTime());
        
        return summary.toString();
    }

    /**
     * Get intelligence details summary
     */
    public String getIntelligenceDetailsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Intelligence Details - ");
        summary.append("Sources: ").append(getIntelligenceSourceCount());
        
        if (getAverageSourceReliability() != null) {
            summary.append(", Avg Reliability: ").append(String.format("%.1f", getAverageSourceReliability()));
        }
        
        if (hasIocs()) {
            summary.append(", IOCs: ").append(getIocCount());
        }
        
        if (hasTtps()) {
            summary.append(", TTPs: ").append(getTtpCount());
        }
        
        if (hasMitreAttackTechniques()) {
            summary.append(", MITRE Techniques: ").append(getMitreAttackTechniqueCount());
        }
        
        if (threatActor != null) {
            summary.append(", Actor: ").append(threatActor);
        }
        
        if (malwareFamily != null) {
            summary.append(", Malware: ").append(malwareFamily);
        }
        
        return summary.toString();
    }

    /**
     * Create a threat found result
     */
    public static ThreatIntelligenceResult threatFound(String lookupResultId, String query, String queryType,
                                                     String category, String severity, int confidence) {
        return ThreatIntelligenceResult.builder()
            .lookupResultId(lookupResultId)
            .query(query)
            .queryType(queryType)
            .threatFound(true)
            .primaryThreatCategory(category)
            .threatSeverity(severity)
            .threatConfidence(confidence)
            .immediateBlockingRecommended(confidence >= 80)
            .enhancedMonitoringRecommended(confidence >= 70)
            .incidentResponseRequired("CRITICAL".equalsIgnoreCase(severity))
            .lookupTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a no threat found result
     */
    public static ThreatIntelligenceResult noThreatFound(String lookupResultId, String query, String queryType) {
        return ThreatIntelligenceResult.builder()
            .lookupResultId(lookupResultId)
            .query(query)
            .queryType(queryType)
            .threatFound(false)
            .threatConfidence(0)
            .immediateBlockingRecommended(false)
            .enhancedMonitoringRecommended(false)
            .incidentResponseRequired(false)
            .lookupTime(LocalDateTime.now())
            .build();
    }

    /**
     * Create a disabled result
     */
    public static ThreatIntelligenceResult disabled() {
        return ThreatIntelligenceResult.builder()
            .lookupResultId("DISABLED")
            .query("N/A")
            .queryType("DISABLED")
            .threatFound(false)
            .threatConfidence(0)
            .immediateBlockingRecommended(false)
            .enhancedMonitoringRecommended(false)
            .incidentResponseRequired(false)
            .lookupTime(LocalDateTime.now())
            .build();
    }

    /**
     * Validate intelligence result consistency
     */
    public boolean isValid() {
        return lookupResultId != null && !lookupResultId.trim().isEmpty() &&
               query != null && !query.trim().isEmpty() &&
               queryType != null && !queryType.trim().isEmpty() &&
               threatFound != null &&
               lookupTime != null &&
               (threatConfidence == null || (threatConfidence >= 0 && threatConfidence <= 100)) &&
               (attributionConfidence == null || (attributionConfidence >= 0 && attributionConfidence <= 100)) &&
               (responseTimeMs == null || responseTimeMs >= 0) &&
               (firstSeen == null || lastSeen == null || !firstSeen.isAfter(lastSeen));
    }
}
