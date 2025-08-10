package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Anomaly Detection Statistics DTO
 * Contains comprehensive statistics for anomaly detection engine performance and metrics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionStats {

    /**
     * Total number of events analyzed
     */
    private long totalEventsAnalyzed;

    /**
     * Total number of anomalies detected
     */
    private long totalAnomaliesDetected;

    /**
     * Number of critical anomalies detected
     */
    private long criticalAnomalies;

    /**
     * Number of high severity anomalies detected
     */
    private long highAnomalies;

    /**
     * Number of medium severity anomalies detected
     */
    private long mediumAnomalies;

    /**
     * Number of low severity anomalies detected
     */
    private long lowAnomalies;

    /**
     * Number of false positive anomalies
     */
    private long falsePositives;

    /**
     * Number of confirmed true positive anomalies
     */
    private long truePositives;

    /**
     * Detection accuracy rate as percentage (0-100)
     */
    private double detectionAccuracy;

    /**
     * Average anomaly score across all detected anomalies
     */
    private double averageAnomalyScore;

    /**
     * Average processing time per event in milliseconds
     */
    private double averageProcessingTimeMs;

    /**
     * Number of user behavior anomalies detected
     */
    private long userBehaviorAnomalies;

    /**
     * Number of network anomalies detected
     */
    private long networkAnomalies;

    /**
     * Number of temporal anomalies detected
     */
    private long temporalAnomalies;

    /**
     * Number of access pattern anomalies detected
     */
    private long accessPatternAnomalies;

    /**
     * Number of volume anomalies detected
     */
    private long volumeAnomalies;

    /**
     * Number of baseline updates performed
     */
    private long baselineUpdates;

    /**
     * Number of active baselines being maintained
     */
    private int activeBaselines;

    /**
     * Engine uptime in hours
     */
    private double engineUptimeHours;

    /**
     * Memory usage in MB
     */
    private double memoryUsageMB;

    /**
     * CPU usage percentage
     */
    private double cpuUsagePercentage;

    /**
     * Number of user baselines tracked
     */
    private long userBaselinesTracked;

    /**
     * Number of network baselines tracked
     */
    private long networkBaselinesTracked;

    /**
     * Average user activity level
     */
    private double averageUserActivityLevel;

    /**
     * Average network activity level
     */
    private double averageNetworkActivityLevel;

    /**
     * Timestamp when statistics were collected
     */
    private LocalDateTime timestamp;

    /**
     * Anomaly detection by type breakdown
     */
    private Map<String, Long> anomaliesByType;

    /**
     * Detection confidence distribution
     */
    private Map<String, Long> confidenceDistribution;

    /**
     * Performance metrics by detection method
     */
    private Map<String, Double> performanceByMethod;

    /**
     * Calculate anomaly detection rate as percentage
     */
    public double getAnomalyDetectionRate() {
        if (totalEventsAnalyzed == 0) {
            return 0.0;
        }
        return (double) totalAnomaliesDetected / totalEventsAnalyzed * 100.0;
    }

    /**
     * Calculate false positive rate as percentage
     */
    public double getFalsePositiveRate() {
        if (totalAnomaliesDetected == 0) {
            return 0.0;
        }
        return (double) falsePositives / totalAnomaliesDetected * 100.0;
    }

    /**
     * Calculate true positive rate as percentage
     */
    public double getTruePositiveRate() {
        if (totalAnomaliesDetected == 0) {
            return 0.0;
        }
        return (double) truePositives / totalAnomaliesDetected * 100.0;
    }

    /**
     * Calculate critical anomaly percentage
     */
    public double getCriticalAnomalyPercentage() {
        if (totalAnomaliesDetected == 0) {
            return 0.0;
        }
        return (double) criticalAnomalies / totalAnomaliesDetected * 100.0;
    }

    /**
     * Calculate processing throughput (events per second)
     */
    public double getProcessingThroughput() {
        if (averageProcessingTimeMs == 0) {
            return 0.0;
        }
        return 1000.0 / averageProcessingTimeMs; // Convert ms to events per second
    }

    /**
     * Check if engine is performing well
     */
    public boolean isPerformingWell() {
        return detectionAccuracy > 85.0 &&
               getFalsePositiveRate() < 15.0 &&
               averageProcessingTimeMs < 100.0 &&
               cpuUsagePercentage < 80.0 &&
               memoryUsageMB < 1024.0; // Less than 1GB
    }

    /**
     * Check if immediate attention is required
     */
    public boolean requiresImmediateAttention() {
        return detectionAccuracy < 70.0 ||
               getFalsePositiveRate() > 30.0 ||
               averageProcessingTimeMs > 500.0 ||
               cpuUsagePercentage > 90.0 ||
               memoryUsageMB > 2048.0 || // More than 2GB
               criticalAnomalies > (totalAnomaliesDetected * 0.5); // More than 50% critical
    }

    /**
     * Get engine health status
     */
    public String getEngineHealthStatus() {
        if (requiresImmediateAttention()) {
            return "CRITICAL";
        } else if (!isPerformingWell()) {
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
        double resourceWeight = 0.3;
        
        // Speed score (lower processing time is better)
        double speedScore = Math.max(0, 100 - (averageProcessingTimeMs / 10.0)); // 10ms baseline
        
        // Resource efficiency score
        double resourceScore = Math.max(0, 100 - (cpuUsagePercentage + (memoryUsageMB / 10.24))); // 1GB = 100 points
        
        return (detectionAccuracy * accuracyWeight) + 
               (speedScore * speedWeight) + 
               (resourceScore * resourceWeight);
    }

    /**
     * Get most common anomaly type
     */
    public String getMostCommonAnomalyType() {
        if (anomaliesByType == null || anomaliesByType.isEmpty()) {
            return "UNKNOWN";
        }
        
        return anomaliesByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");
    }

    /**
     * Get anomaly detection summary
     */
    public String getAnomalyDetectionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Anomaly Detection Engine Status: ").append(getEngineHealthStatus());
        summary.append(", Events Analyzed: ").append(totalEventsAnalyzed);
        summary.append(", Anomalies Detected: ").append(totalAnomaliesDetected);
        summary.append(", Detection Rate: ").append(String.format("%.2f%%", getAnomalyDetectionRate()));
        summary.append(", Accuracy: ").append(String.format("%.1f%%", detectionAccuracy));
        summary.append(", False Positive Rate: ").append(String.format("%.1f%%", getFalsePositiveRate()));
        summary.append(", Avg Processing: ").append(String.format("%.1fms", averageProcessingTimeMs));
        
        return summary.toString();
    }

    /**
     * Get resource utilization summary
     */
    public String getResourceUtilizationSummary() {
        return String.format("CPU: %.1f%%, Memory: %.1fMB, Uptime: %.1fh, Throughput: %.1f events/sec",
            cpuUsagePercentage, memoryUsageMB, engineUptimeHours, getProcessingThroughput());
    }

    /**
     * Validate statistics consistency
     */
    public boolean isValid() {
        return totalEventsAnalyzed >= 0 &&
               totalAnomaliesDetected >= 0 &&
               totalAnomaliesDetected <= totalEventsAnalyzed &&
               criticalAnomalies <= totalAnomaliesDetected &&
               highAnomalies <= totalAnomaliesDetected &&
               mediumAnomalies <= totalAnomaliesDetected &&
               lowAnomalies <= totalAnomaliesDetected &&
               (criticalAnomalies + highAnomalies + mediumAnomalies + lowAnomalies) <= totalAnomaliesDetected &&
               falsePositives <= totalAnomaliesDetected &&
               truePositives <= totalAnomaliesDetected &&
               detectionAccuracy >= 0 && detectionAccuracy <= 100 &&
               averageAnomalyScore >= 0 && averageAnomalyScore <= 100 &&
               averageProcessingTimeMs >= 0 &&
               cpuUsagePercentage >= 0 && cpuUsagePercentage <= 100 &&
               memoryUsageMB >= 0 &&
               engineUptimeHours >= 0 &&
               activeBaselines >= 0 &&
               timestamp != null;
    }
}
