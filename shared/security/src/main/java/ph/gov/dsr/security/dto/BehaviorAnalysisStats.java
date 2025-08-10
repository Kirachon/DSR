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
 * Behavior Analysis Statistics DTO
 * Contains comprehensive statistics for behavioral analysis engine performance and metrics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysisStats {

    /**
     * Total number of user behaviors analyzed
     */
    private long totalBehaviorsAnalyzed;

    /**
     * Number of suspicious behaviors detected
     */
    private long suspiciousBehaviorsDetected;

    /**
     * Number of normal behaviors identified
     */
    private long normalBehaviorsIdentified;

    /**
     * Number of velocity violations detected
     */
    private long velocityViolations;

    /**
     * Number of impossible travel incidents detected
     */
    private long impossibleTravelIncidents;

    /**
     * Number of unusual time access patterns detected
     */
    private long unusualTimeAccessPatterns;

    /**
     * Number of unusual resource usage patterns detected
     */
    private long unusualResourceUsagePatterns;

    /**
     * Number of data exfiltration patterns detected
     */
    private long dataExfiltrationPatterns;

    /**
     * Number of privilege escalation attempts detected
     */
    private long privilegeEscalationAttempts;

    /**
     * Number of lateral movement patterns detected
     */
    private long lateralMovementPatterns;

    /**
     * Number of active user behavior profiles
     */
    private int activeUserProfiles;

    /**
     * Number of behavior baselines maintained
     */
    private int behaviorBaselines;

    /**
     * Average behavior risk score across all users
     */
    private double averageBehaviorRiskScore;

    /**
     * Average analysis confidence level
     */
    private double averageAnalysisConfidence;

    /**
     * Average processing time per behavior analysis in milliseconds
     */
    private double averageProcessingTimeMs;

    /**
     * Number of behavior pattern updates performed
     */
    private long behaviorPatternUpdates;

    /**
     * Number of false positive behavior alerts
     */
    private long falsePositiveBehaviorAlerts;

    /**
     * Number of confirmed malicious behaviors
     */
    private long confirmedMaliciousBehaviors;

    /**
     * Detection accuracy rate as percentage (0-100)
     */
    private double detectionAccuracy;

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
     * Number of user patterns tracked
     */
    private long userPatternsTracked;

    /**
     * Number of session patterns tracked
     */
    private long sessionPatternsTracked;

    /**
     * Average risk score across all behaviors
     */
    private double averageRiskScore;

    /**
     * Number of high-risk users identified
     */
    private long highRiskUsers;

    /**
     * Timestamp when statistics were collected
     */
    private LocalDateTime timestamp;

    /**
     * Behavior analysis by type breakdown
     */
    private Map<String, Long> behaviorsByType;

    /**
     * Risk score distribution
     */
    private Map<String, Long> riskScoreDistribution;

    /**
     * User activity patterns summary
     */
    private Map<String, Double> activityPatterns;

    /**
     * Calculate suspicious behavior rate as percentage
     */
    public double getSuspiciousBehaviorRate() {
        if (totalBehaviorsAnalyzed == 0) {
            return 0.0;
        }
        return (double) suspiciousBehaviorsDetected / totalBehaviorsAnalyzed * 100.0;
    }

    /**
     * Calculate false positive rate as percentage
     */
    public double getFalsePositiveRate() {
        if (suspiciousBehaviorsDetected == 0) {
            return 0.0;
        }
        return (double) falsePositiveBehaviorAlerts / suspiciousBehaviorsDetected * 100.0;
    }

    /**
     * Calculate true positive rate as percentage
     */
    public double getTruePositiveRate() {
        if (suspiciousBehaviorsDetected == 0) {
            return 0.0;
        }
        return (double) confirmedMaliciousBehaviors / suspiciousBehaviorsDetected * 100.0;
    }

    /**
     * Calculate processing throughput (behaviors per second)
     */
    public double getProcessingThroughput() {
        if (averageProcessingTimeMs == 0) {
            return 0.0;
        }
        return 1000.0 / averageProcessingTimeMs; // Convert ms to behaviors per second
    }

    /**
     * Calculate high-risk behavior percentage
     */
    public double getHighRiskBehaviorPercentage() {
        if (totalBehaviorsAnalyzed == 0) {
            return 0.0;
        }
        long highRiskBehaviors = velocityViolations + impossibleTravelIncidents + 
                               dataExfiltrationPatterns + privilegeEscalationAttempts + 
                               lateralMovementPatterns;
        return (double) highRiskBehaviors / totalBehaviorsAnalyzed * 100.0;
    }

    /**
     * Check if engine is performing well
     */
    public boolean isPerformingWell() {
        return detectionAccuracy > 85.0 &&
               getFalsePositiveRate() < 15.0 &&
               averageAnalysisConfidence > 80.0 &&
               averageProcessingTimeMs < 200.0 &&
               cpuUsagePercentage < 80.0 &&
               memoryUsageMB < 1024.0; // Less than 1GB
    }

    /**
     * Check if immediate attention is required
     */
    public boolean requiresImmediateAttention() {
        return detectionAccuracy < 70.0 ||
               getFalsePositiveRate() > 30.0 ||
               averageAnalysisConfidence < 60.0 ||
               averageProcessingTimeMs > 1000.0 ||
               cpuUsagePercentage > 90.0 ||
               memoryUsageMB > 2048.0 || // More than 2GB
               getHighRiskBehaviorPercentage() > 50.0; // More than 50% high-risk
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
     * Calculate analysis efficiency score (0-100)
     */
    public double getAnalysisEfficiencyScore() {
        double accuracyWeight = 0.3;
        double confidenceWeight = 0.3;
        double speedWeight = 0.2;
        double resourceWeight = 0.2;
        
        // Speed score (lower processing time is better)
        double speedScore = Math.max(0, 100 - (averageProcessingTimeMs / 20.0)); // 20ms baseline
        
        // Resource efficiency score
        double resourceScore = Math.max(0, 100 - (cpuUsagePercentage + (memoryUsageMB / 10.24))); // 1GB = 100 points
        
        return (detectionAccuracy * accuracyWeight) + 
               (averageAnalysisConfidence * confidenceWeight) +
               (speedScore * speedWeight) + 
               (resourceScore * resourceWeight);
    }

    /**
     * Get most common suspicious behavior type
     */
    public String getMostCommonSuspiciousBehaviorType() {
        if (behaviorsByType == null || behaviorsByType.isEmpty()) {
            return "UNKNOWN";
        }
        
        return behaviorsByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");
    }

    /**
     * Get behavior analysis summary
     */
    public String getBehaviorAnalysisSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Behavior Analysis Engine Status: ").append(getEngineHealthStatus());
        summary.append(", Behaviors Analyzed: ").append(totalBehaviorsAnalyzed);
        summary.append(", Suspicious: ").append(suspiciousBehaviorsDetected);
        summary.append(", Suspicious Rate: ").append(String.format("%.2f%%", getSuspiciousBehaviorRate()));
        summary.append(", Accuracy: ").append(String.format("%.1f%%", detectionAccuracy));
        summary.append(", Confidence: ").append(String.format("%.1f%%", averageAnalysisConfidence));
        summary.append(", High-Risk Rate: ").append(String.format("%.1f%%", getHighRiskBehaviorPercentage()));
        
        return summary.toString();
    }

    /**
     * Get threat pattern summary
     */
    public String getThreatPatternSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat Patterns - ");
        summary.append("Velocity Violations: ").append(velocityViolations);
        summary.append(", Impossible Travel: ").append(impossibleTravelIncidents);
        summary.append(", Data Exfiltration: ").append(dataExfiltrationPatterns);
        summary.append(", Privilege Escalation: ").append(privilegeEscalationAttempts);
        summary.append(", Lateral Movement: ").append(lateralMovementPatterns);
        
        return summary.toString();
    }

    /**
     * Get resource utilization summary
     */
    public String getResourceUtilizationSummary() {
        return String.format("CPU: %.1f%%, Memory: %.1fMB, Uptime: %.1fh, Throughput: %.1f behaviors/sec",
            cpuUsagePercentage, memoryUsageMB, engineUptimeHours, getProcessingThroughput());
    }

    /**
     * Validate statistics consistency
     */
    public boolean isValid() {
        return totalBehaviorsAnalyzed >= 0 &&
               suspiciousBehaviorsDetected >= 0 &&
               normalBehaviorsIdentified >= 0 &&
               (suspiciousBehaviorsDetected + normalBehaviorsIdentified) <= totalBehaviorsAnalyzed &&
               velocityViolations <= suspiciousBehaviorsDetected &&
               impossibleTravelIncidents <= suspiciousBehaviorsDetected &&
               unusualTimeAccessPatterns <= suspiciousBehaviorsDetected &&
               unusualResourceUsagePatterns <= suspiciousBehaviorsDetected &&
               dataExfiltrationPatterns <= suspiciousBehaviorsDetected &&
               privilegeEscalationAttempts <= suspiciousBehaviorsDetected &&
               lateralMovementPatterns <= suspiciousBehaviorsDetected &&
               falsePositiveBehaviorAlerts <= suspiciousBehaviorsDetected &&
               confirmedMaliciousBehaviors <= suspiciousBehaviorsDetected &&
               activeUserProfiles >= 0 &&
               behaviorBaselines >= 0 &&
               averageBehaviorRiskScore >= 0 && averageBehaviorRiskScore <= 100 &&
               averageAnalysisConfidence >= 0 && averageAnalysisConfidence <= 100 &&
               detectionAccuracy >= 0 && detectionAccuracy <= 100 &&
               averageProcessingTimeMs >= 0 &&
               cpuUsagePercentage >= 0 && cpuUsagePercentage <= 100 &&
               memoryUsageMB >= 0 &&
               engineUptimeHours >= 0 &&
               timestamp != null;
    }
}
