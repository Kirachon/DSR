package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Network Segmentation Metrics DTO
 * Contains comprehensive metrics for network segmentation monitoring and analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkSegmentationMetrics {

    /**
     * Total number of network segments configured
     */
    private int totalSegments;

    /**
     * Number of currently active network segments
     */
    private int activeSegments;

    /**
     * Number of temporary/dynamic segments
     */
    private int temporarySegments;

    /**
     * Total number of network policies configured
     */
    private int totalPolicies;

    /**
     * Number of blocked network connections
     */
    private long blockedConnections;

    /**
     * Number of allowed network connections
     */
    private long allowedConnections;

    /**
     * Number of policy violations detected
     */
    private long policyViolations;

    /**
     * Number of unauthorized access attempts
     */
    private long unauthorizedAccessAttempts;

    /**
     * Number of cross-segment communication attempts
     */
    private long crossSegmentAttempts;

    /**
     * Number of successful cross-segment communications
     */
    private long successfulCrossSegment;

    /**
     * Average response time for policy evaluation in milliseconds
     */
    private double averagePolicyEvaluationTime;

    /**
     * Number of dynamic segment creations
     */
    private long dynamicSegmentCreations;

    /**
     * Number of segment isolation events
     */
    private long isolationEvents;

    /**
     * Timestamp when metrics were collected
     */
    private LocalDateTime timestamp;

    /**
     * Bandwidth utilization per segment (segment ID -> utilization percentage)
     */
    private Map<String, Double> segmentBandwidthUtilization;

    /**
     * Connection count per segment (segment ID -> connection count)
     */
    private Map<String, Integer> segmentConnectionCounts;

    /**
     * Threat level per segment (segment ID -> threat level)
     */
    private Map<String, String> segmentThreatLevels;

    /**
     * Calculate total network connections
     */
    public long getTotalConnections() {
        return blockedConnections + allowedConnections;
    }

    /**
     * Calculate connection success rate as percentage
     */
    public double getConnectionSuccessRate() {
        long totalConnections = getTotalConnections();
        if (totalConnections == 0) {
            return 100.0; // No connections means 100% success
        }
        return (double) allowedConnections / totalConnections * 100.0;
    }

    /**
     * Calculate cross-segment success rate as percentage
     */
    public double getCrossSegmentSuccessRate() {
        if (crossSegmentAttempts == 0) {
            return 0.0; // No attempts means 0% success
        }
        return (double) successfulCrossSegment / crossSegmentAttempts * 100.0;
    }

    /**
     * Calculate policy violation rate as percentage
     */
    public double getPolicyViolationRate() {
        long totalConnections = getTotalConnections();
        if (totalConnections == 0) {
            return 0.0; // No connections means 0% violations
        }
        return (double) policyViolations / totalConnections * 100.0;
    }

    /**
     * Calculate segment utilization rate as percentage
     */
    public double getSegmentUtilizationRate() {
        if (totalSegments == 0) {
            return 0.0; // No segments means 0% utilization
        }
        return (double) activeSegments / totalSegments * 100.0;
    }

    /**
     * Check if network segmentation is healthy
     */
    public boolean isHealthy() {
        return getConnectionSuccessRate() > 95.0 &&
               getPolicyViolationRate() < 5.0 &&
               averagePolicyEvaluationTime < 100.0 && // Less than 100ms
               getSegmentUtilizationRate() > 80.0;
    }

    /**
     * Check if immediate attention is required
     */
    public boolean requiresImmediateAttention() {
        return getConnectionSuccessRate() < 90.0 ||
               getPolicyViolationRate() > 10.0 ||
               averagePolicyEvaluationTime > 500.0 || // More than 500ms
               unauthorizedAccessAttempts > (getTotalConnections() * 0.1); // More than 10% unauthorized
    }

    /**
     * Get network segmentation health status
     */
    public String getHealthStatus() {
        if (requiresImmediateAttention()) {
            return "CRITICAL";
        } else if (!isHealthy()) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Get most utilized segment
     */
    public String getMostUtilizedSegment() {
        if (segmentBandwidthUtilization == null || segmentBandwidthUtilization.isEmpty()) {
            return null;
        }
        
        return segmentBandwidthUtilization.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Get highest threat segment
     */
    public String getHighestThreatSegment() {
        if (segmentThreatLevels == null || segmentThreatLevels.isEmpty()) {
            return null;
        }
        
        // Find segment with highest threat level (CRITICAL > HIGH > MEDIUM > LOW > NONE)
        return segmentThreatLevels.entrySet().stream()
            .filter(entry -> "CRITICAL".equals(entry.getValue()) || "HIGH".equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    /**
     * Validate metrics consistency
     */
    public boolean isValid() {
        return totalSegments >= 0 &&
               activeSegments >= 0 &&
               activeSegments <= totalSegments &&
               temporarySegments >= 0 &&
               temporarySegments <= totalSegments &&
               totalPolicies >= 0 &&
               blockedConnections >= 0 &&
               allowedConnections >= 0 &&
               successfulCrossSegment <= crossSegmentAttempts &&
               averagePolicyEvaluationTime >= 0 &&
               timestamp != null;
    }
}
