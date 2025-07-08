package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.ThreatLevel;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Threat Detection Metrics DTO
 * Contains comprehensive metrics for threat detection system monitoring and analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatDetectionMetrics {

    /**
     * Total number of threats detected
     */
    private long totalThreatsDetected;

    /**
     * Number of critical threats detected
     */
    private long criticalThreatsDetected;

    /**
     * Number of high severity threats detected
     */
    private long highThreatsDetected;

    /**
     * Number of medium severity threats detected
     */
    private long mediumThreatsDetected;

    /**
     * Number of low severity threats detected
     */
    private long lowThreatsDetected;

    /**
     * Number of active threat contexts being monitored
     */
    private int activeThreatContexts;

    /**
     * Number of user behavior profiles being tracked
     */
    private int userBehaviorProfiles;

    /**
     * Number of network threat profiles being monitored
     */
    private int networkThreatProfiles;

    /**
     * Current overall threat level
     */
    private ThreatLevel currentThreatLevel;

    /**
     * Average threat score across all detected threats
     */
    private double averageThreatScore;

    /**
     * Number of threats detected in the last hour
     */
    private long threatsDetectedLastHour;

    /**
     * Number of threats detected in the last 24 hours
     */
    private long threatsDetectedLast24Hours;

    /**
     * Number of false positives identified
     */
    private long falsePositives;

    /**
     * Detection accuracy rate as percentage
     */
    private double detectionAccuracy;

    /**
     * Average time to detect threats in seconds
     */
    private double averageDetectionTimeSeconds;

    /**
     * Average time to respond to threats in seconds
     */
    private double averageResponseTimeSeconds;

    /**
     * Number of automated responses triggered
     */
    private long automatedResponsesTriggered;

    /**
     * Number of manual investigations initiated
     */
    private long manualInvestigationsInitiated;

    /**
     * Whether threat detection is enabled
     */
    private boolean threatDetectionEnabled;

    /**
     * Whether real-time detection is enabled
     */
    private boolean realTimeDetectionEnabled;

    /**
     * Whether machine learning is enabled
     */
    private boolean machineLearningEnabled;

    /**
     * Whether threat intelligence feeds are active
     */
    private boolean threatIntelligenceActive;

    /**
     * Timestamp when metrics were collected
     */
    private LocalDateTime timestamp;

    /**
     * Threat detection by category breakdown
     */
    private Map<String, Long> threatsByCategory;

    /**
     * Threat detection by source breakdown
     */
    private Map<String, Long> threatsBySource;

    /**
     * Detection engine performance metrics
     */
    private Map<String, Double> enginePerformanceMetrics;

    /**
     * Calculate threat detection rate per hour
     */
    public double getThreatDetectionRatePerHour() {
        return threatsDetectedLastHour;
    }

    /**
     * Calculate critical threat percentage
     */
    public double getCriticalThreatPercentage() {
        if (totalThreatsDetected == 0) {
            return 0.0;
        }
        return (double) criticalThreatsDetected / totalThreatsDetected * 100.0;
    }

    /**
     * Calculate false positive rate
     */
    public double getFalsePositiveRate() {
        if (totalThreatsDetected == 0) {
            return 0.0;
        }
        return (double) falsePositives / totalThreatsDetected * 100.0;
    }

    /**
     * Check if system is in high threat state
     */
    public boolean isHighThreatState() {
        return currentThreatLevel == ThreatLevel.HIGH || 
               currentThreatLevel == ThreatLevel.CRITICAL ||
               getCriticalThreatPercentage() > 20.0 ||
               threatsDetectedLastHour > 50;
    }

    /**
     * Check if immediate attention is required
     */
    public boolean requiresImmediateAttention() {
        return currentThreatLevel.requiresImmediateAttention() ||
               criticalThreatsDetected > 0 ||
               threatsDetectedLastHour > 100 ||
               detectionAccuracy < 80.0;
    }

    /**
     * Get system health status
     */
    public String getSystemHealthStatus() {
        if (requiresImmediateAttention()) {
            return "CRITICAL";
        } else if (isHighThreatState()) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Calculate detection efficiency score (0-100)
     */
    public double getDetectionEfficiencyScore() {
        double accuracyWeight = 0.4;
        double speedWeight = 0.3;
        double responseWeight = 0.3;
        
        // Normalize detection time (assume 60 seconds is baseline)
        double speedScore = Math.max(0, 100 - (averageDetectionTimeSeconds / 60.0 * 100));
        
        // Normalize response time (assume 300 seconds is baseline)
        double responseScore = Math.max(0, 100 - (averageResponseTimeSeconds / 300.0 * 100));
        
        return (detectionAccuracy * accuracyWeight) + 
               (speedScore * speedWeight) + 
               (responseScore * responseWeight);
    }

    /**
     * Get threat trend analysis
     */
    public String getThreatTrend() {
        if (threatsDetectedLast24Hours > threatsDetectedLastHour * 20) {
            return "INCREASING";
        } else if (threatsDetectedLast24Hours < threatsDetectedLastHour * 15) {
            return "DECREASING";
        } else {
            return "STABLE";
        }
    }

    /**
     * Check if machine learning is performing well
     */
    public boolean isMachineLearningEffective() {
        return machineLearningEnabled && 
               detectionAccuracy > 85.0 && 
               getFalsePositiveRate() < 10.0;
    }

    /**
     * Get overall threat detection summary
     */
    public String getThreatDetectionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat Detection Status: ").append(getSystemHealthStatus());
        summary.append(", Total Threats: ").append(totalThreatsDetected);
        summary.append(", Critical: ").append(criticalThreatsDetected);
        summary.append(", Accuracy: ").append(String.format("%.1f%%", detectionAccuracy));
        summary.append(", Trend: ").append(getThreatTrend());
        
        if (realTimeDetectionEnabled) {
            summary.append(" [REAL-TIME]");
        }
        
        if (machineLearningEnabled) {
            summary.append(" [ML-ENABLED]");
        }
        
        return summary.toString();
    }

    /**
     * Validate metrics consistency
     */
    public boolean isValid() {
        return totalThreatsDetected >= 0 &&
               criticalThreatsDetected <= totalThreatsDetected &&
               highThreatsDetected <= totalThreatsDetected &&
               mediumThreatsDetected <= totalThreatsDetected &&
               lowThreatsDetected <= totalThreatsDetected &&
               (criticalThreatsDetected + highThreatsDetected + mediumThreatsDetected + lowThreatsDetected) <= totalThreatsDetected &&
               activeThreatContexts >= 0 &&
               userBehaviorProfiles >= 0 &&
               networkThreatProfiles >= 0 &&
               averageThreatScore >= 0 && averageThreatScore <= 100 &&
               detectionAccuracy >= 0 && detectionAccuracy <= 100 &&
               averageDetectionTimeSeconds >= 0 &&
               averageResponseTimeSeconds >= 0 &&
               falsePositives <= totalThreatsDetected &&
               currentThreatLevel != null &&
               timestamp != null;
    }
}
