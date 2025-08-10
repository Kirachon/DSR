package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Traffic Pattern Analysis DTO
 * Contains network traffic pattern analysis results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficPatternAnalysis {

    /**
     * Analysis ID
     */
    private String analysisId;

    /**
     * Source IP address
     */
    private String sourceIp;

    /**
     * Target IP address
     */
    private String targetIp;

    /**
     * Analysis period start
     */
    private LocalDateTime analysisStartTime;

    /**
     * Analysis period end
     */
    private LocalDateTime analysisEndTime;

    /**
     * Traffic volume (bytes)
     */
    private Long trafficVolume;

    /**
     * Packet count
     */
    private Long packetCount;

    /**
     * Connection count
     */
    private Integer connectionCount;

    /**
     * Unique ports accessed
     */
    private Integer uniquePortsAccessed;

    /**
     * Average packet size
     */
    private Double averagePacketSize;

    /**
     * Traffic rate (bytes per second)
     */
    private Double trafficRate;

    /**
     * Connection rate (connections per minute)
     */
    private Double connectionRate;

    /**
     * Anomaly score (0-100)
     */
    private Integer anomalyScore;

    /**
     * Detected patterns
     */
    private List<String> detectedPatterns;

    /**
     * Suspicious activities
     */
    private List<String> suspiciousActivities;

    /**
     * Protocol distribution
     */
    private Map<String, Long> protocolDistribution;

    /**
     * Port distribution
     */
    private Map<String, Long> portDistribution;

    /**
     * Time-based patterns
     */
    private Map<String, Object> timeBasedPatterns;

    /**
     * Behavioral indicators
     */
    private List<String> behavioralIndicators;

    /**
     * Analysis confidence (0-100)
     */
    private Integer confidence;

    /**
     * Analysis metadata
     */
    private Map<String, Object> analysisMetadata;

    /**
     * Check if traffic shows anomalous patterns
     */
    public boolean isAnomalous() {
        return anomalyScore != null && anomalyScore >= 70;
    }

    /**
     * Check if traffic indicates potential attack
     */
    public boolean indicatesPotentialAttack() {
        return isAnomalous() && 
               (suspiciousActivities != null && !suspiciousActivities.isEmpty());
    }

    /**
     * Check if traffic shows DDoS patterns
     */
    public boolean showsDdosPatterns() {
        return detectedPatterns != null && 
               detectedPatterns.stream().anyMatch(pattern -> 
                   pattern.contains("DDOS") || pattern.contains("FLOOD"));
    }

    /**
     * Check if traffic shows port scanning
     */
    public boolean showsPortScanning() {
        return uniquePortsAccessed != null && uniquePortsAccessed > 100;
    }

    /**
     * Check if traffic shows brute force patterns
     */
    public boolean showsBruteForcePatterns() {
        return detectedPatterns != null && 
               detectedPatterns.stream().anyMatch(pattern -> 
                   pattern.contains("BRUTE_FORCE") || pattern.contains("REPEATED_ATTEMPTS"));
    }

    /**
     * Get analysis duration in minutes
     */
    public long getAnalysisDurationMinutes() {
        if (analysisStartTime == null || analysisEndTime == null) return 0;
        return java.time.Duration.between(analysisStartTime, analysisEndTime).toMinutes();
    }

    /**
     * Get traffic intensity level
     */
    public String getTrafficIntensity() {
        if (trafficRate == null) return "UNKNOWN";
        if (trafficRate > 1000000) return "VERY_HIGH"; // > 1MB/s
        if (trafficRate > 100000) return "HIGH";       // > 100KB/s
        if (trafficRate > 10000) return "MEDIUM";      // > 10KB/s
        if (trafficRate > 1000) return "LOW";          // > 1KB/s
        return "MINIMAL";
    }

    /**
     * Get connection intensity level
     */
    public String getConnectionIntensity() {
        if (connectionRate == null) return "UNKNOWN";
        if (connectionRate > 100) return "VERY_HIGH";  // > 100 conn/min
        if (connectionRate > 50) return "HIGH";        // > 50 conn/min
        if (connectionRate > 20) return "MEDIUM";      // > 20 conn/min
        if (connectionRate > 5) return "LOW";          // > 5 conn/min
        return "MINIMAL";
    }

    /**
     * Create high-anomaly traffic analysis
     */
    public static TrafficPatternAnalysis highAnomaly(String sourceIp, List<String> patterns, int anomalyScore) {
        return TrafficPatternAnalysis.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .sourceIp(sourceIp)
                .anomalyScore(anomalyScore)
                .detectedPatterns(patterns)
                .confidence(80)
                .analysisStartTime(LocalDateTime.now().minusHours(1))
                .analysisEndTime(LocalDateTime.now())
                .build();
    }

    /**
     * Create normal traffic analysis
     */
    public static TrafficPatternAnalysis normal(String sourceIp) {
        return TrafficPatternAnalysis.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .sourceIp(sourceIp)
                .anomalyScore(15)
                .confidence(85)
                .analysisStartTime(LocalDateTime.now().minusHours(1))
                .analysisEndTime(LocalDateTime.now())
                .build();
    }

    /**
     * Validate traffic pattern analysis consistency
     */
    public boolean isValid() {
        return analysisId != null && !analysisId.trim().isEmpty() &&
               sourceIp != null && !sourceIp.trim().isEmpty() &&
               analysisStartTime != null &&
               (trafficVolume == null || trafficVolume >= 0) &&
               (packetCount == null || packetCount >= 0) &&
               (connectionCount == null || connectionCount >= 0) &&
               (uniquePortsAccessed == null || uniquePortsAccessed >= 0) &&
               (anomalyScore == null || (anomalyScore >= 0 && anomalyScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100));
    }
}
