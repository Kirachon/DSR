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
 * Threat Intelligence Statistics DTO
 * Contains comprehensive statistics for threat intelligence service performance and metrics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatIntelligenceStats {

    /**
     * Total number of threat intelligence queries performed
     */
    private long totalQueries;

    /**
     * Number of successful threat intelligence lookups
     */
    private long successfulLookups;

    /**
     * Number of failed threat intelligence lookups
     */
    private long failedLookups;

    /**
     * Number of threat indicators found
     */
    private long threatIndicatorsFound;

    /**
     * Number of malicious IPs identified
     */
    private long maliciousIpsIdentified;

    /**
     * Number of malicious domains identified
     */
    private long maliciousDomainsIdentified;

    /**
     * Number of malicious file hashes identified
     */
    private long maliciousFileHashesIdentified;

    /**
     * Number of malicious URLs identified
     */
    private long maliciousUrlsIdentified;

    /**
     * Number of active threat feeds
     */
    private int activeThreatFeeds;

    /**
     * Number of threat feed updates received
     */
    private long threatFeedUpdates;

    /**
     * Number of IOCs (Indicators of Compromise) in database
     */
    private long totalIocsInDatabase;

    /**
     * Number of fresh IOCs (less than 24 hours old)
     */
    private long freshIocs;

    /**
     * Number of stale IOCs (older than 30 days)
     */
    private long staleIocs;

    /**
     * Average query response time in milliseconds
     */
    private double averageQueryResponseTimeMs;

    /**
     * Cache hit rate as percentage (0-100)
     */
    private double cacheHitRate;

    /**
     * Threat intelligence accuracy rate as percentage (0-100)
     */
    private double accuracyRate;

    /**
     * Number of false positive threat indicators
     */
    private long falsePositiveIndicators;

    /**
     * Number of confirmed threat indicators
     */
    private long confirmedThreatIndicators;

    /**
     * Service uptime in hours
     */
    private double serviceUptimeHours;

    /**
     * Memory usage in MB
     */
    private double memoryUsageMB;

    /**
     * CPU usage percentage
     */
    private double cpuUsagePercentage;

    /**
     * Database size in MB
     */
    private double databaseSizeMB;

    /**
     * Whether threat intelligence feeds are enabled
     */
    private boolean feedsEnabled;

    /**
     * Number of IP indicators in database
     */
    private long ipIndicators;

    /**
     * Number of domain indicators in database
     */
    private long domainIndicators;

    /**
     * Number of hash indicators in database
     */
    private long hashIndicators;

    /**
     * Last update timestamp for threat intelligence feeds
     */
    private LocalDateTime lastUpdate;

    /**
     * Timestamp when statistics were collected
     */
    private LocalDateTime timestamp;

    /**
     * Threat intelligence by source breakdown
     */
    private Map<String, Long> threatIntelBySource;

    /**
     * Query performance by type
     */
    private Map<String, Double> queryPerformanceByType;

    /**
     * Threat feed reliability scores
     */
    private Map<String, Double> feedReliabilityScores;

    /**
     * Calculate query success rate as percentage
     */
    public double getQuerySuccessRate() {
        if (totalQueries == 0) {
            return 100.0;
        }
        return (double) successfulLookups / totalQueries * 100.0;
    }

    /**
     * Calculate threat detection rate as percentage
     */
    public double getThreatDetectionRate() {
        if (successfulLookups == 0) {
            return 0.0;
        }
        return (double) threatIndicatorsFound / successfulLookups * 100.0;
    }

    /**
     * Calculate false positive rate as percentage
     */
    public double getFalsePositiveRate() {
        if (threatIndicatorsFound == 0) {
            return 0.0;
        }
        return (double) falsePositiveIndicators / threatIndicatorsFound * 100.0;
    }

    /**
     * Calculate IOC freshness rate as percentage
     */
    public double getIocFreshnessRate() {
        if (totalIocsInDatabase == 0) {
            return 100.0;
        }
        return (double) freshIocs / totalIocsInDatabase * 100.0;
    }

    /**
     * Calculate query throughput (queries per second)
     */
    public double getQueryThroughput() {
        if (averageQueryResponseTimeMs == 0) {
            return 0.0;
        }
        return 1000.0 / averageQueryResponseTimeMs; // Convert ms to queries per second
    }

    /**
     * Check if service is performing well
     */
    public boolean isPerformingWell() {
        return getQuerySuccessRate() > 95.0 &&
               accuracyRate > 85.0 &&
               getFalsePositiveRate() < 10.0 &&
               averageQueryResponseTimeMs < 100.0 &&
               cacheHitRate > 80.0 &&
               cpuUsagePercentage < 80.0 &&
               memoryUsageMB < 1024.0; // Less than 1GB
    }

    /**
     * Check if immediate attention is required
     */
    public boolean requiresImmediateAttention() {
        return getQuerySuccessRate() < 90.0 ||
               accuracyRate < 70.0 ||
               getFalsePositiveRate() > 25.0 ||
               averageQueryResponseTimeMs > 500.0 ||
               cacheHitRate < 50.0 ||
               cpuUsagePercentage > 90.0 ||
               memoryUsageMB > 2048.0 || // More than 2GB
               activeThreatFeeds == 0; // No active feeds
    }

    /**
     * Get service health status
     */
    public String getServiceHealthStatus() {
        if (requiresImmediateAttention()) {
            return "CRITICAL";
        } else if (!isPerformingWell()) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Calculate service efficiency score (0-100)
     */
    public double getServiceEfficiencyScore() {
        double accuracyWeight = 0.3;
        double speedWeight = 0.25;
        double reliabilityWeight = 0.25;
        double resourceWeight = 0.2;
        
        // Speed score (lower response time is better)
        double speedScore = Math.max(0, 100 - (averageQueryResponseTimeMs / 10.0)); // 10ms baseline
        
        // Reliability score
        double reliabilityScore = getQuerySuccessRate();
        
        // Resource efficiency score
        double resourceScore = Math.max(0, 100 - (cpuUsagePercentage + (memoryUsageMB / 10.24))); // 1GB = 100 points
        
        return (accuracyRate * accuracyWeight) + 
               (speedScore * speedWeight) + 
               (reliabilityScore * reliabilityWeight) +
               (resourceScore * resourceWeight);
    }

    /**
     * Get most reliable threat feed
     */
    public String getMostReliableThreatFeed() {
        if (feedReliabilityScores == null || feedReliabilityScores.isEmpty()) {
            return "UNKNOWN";
        }
        
        return feedReliabilityScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");
    }

    /**
     * Get threat intelligence summary
     */
    public String getThreatIntelligenceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat Intelligence Service Status: ").append(getServiceHealthStatus());
        summary.append(", Total Queries: ").append(totalQueries);
        summary.append(", Success Rate: ").append(String.format("%.1f%%", getQuerySuccessRate()));
        summary.append(", Threats Found: ").append(threatIndicatorsFound);
        summary.append(", Detection Rate: ").append(String.format("%.1f%%", getThreatDetectionRate()));
        summary.append(", Accuracy: ").append(String.format("%.1f%%", accuracyRate));
        summary.append(", Cache Hit Rate: ").append(String.format("%.1f%%", cacheHitRate));
        summary.append(", Active Feeds: ").append(activeThreatFeeds);
        
        return summary.toString();
    }

    /**
     * Get IOC database summary
     */
    public String getIocDatabaseSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("IOC Database - Total: ").append(totalIocsInDatabase);
        summary.append(", Fresh: ").append(freshIocs);
        summary.append(", Stale: ").append(staleIocs);
        summary.append(", Freshness Rate: ").append(String.format("%.1f%%", getIocFreshnessRate()));
        summary.append(", Size: ").append(String.format("%.1fMB", databaseSizeMB));
        
        return summary.toString();
    }

    /**
     * Get threat breakdown summary
     */
    public String getThreatBreakdownSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Threat Indicators - ");
        summary.append("Malicious IPs: ").append(maliciousIpsIdentified);
        summary.append(", Domains: ").append(maliciousDomainsIdentified);
        summary.append(", File Hashes: ").append(maliciousFileHashesIdentified);
        summary.append(", URLs: ").append(maliciousUrlsIdentified);
        
        return summary.toString();
    }

    /**
     * Get resource utilization summary
     */
    public String getResourceUtilizationSummary() {
        return String.format("CPU: %.1f%%, Memory: %.1fMB, Uptime: %.1fh, Throughput: %.1f queries/sec",
            cpuUsagePercentage, memoryUsageMB, serviceUptimeHours, getQueryThroughput());
    }

    /**
     * Validate statistics consistency
     */
    public boolean isValid() {
        return totalQueries >= 0 &&
               successfulLookups >= 0 &&
               failedLookups >= 0 &&
               (successfulLookups + failedLookups) <= totalQueries &&
               threatIndicatorsFound <= successfulLookups &&
               maliciousIpsIdentified <= threatIndicatorsFound &&
               maliciousDomainsIdentified <= threatIndicatorsFound &&
               maliciousFileHashesIdentified <= threatIndicatorsFound &&
               maliciousUrlsIdentified <= threatIndicatorsFound &&
               activeThreatFeeds >= 0 &&
               totalIocsInDatabase >= 0 &&
               freshIocs <= totalIocsInDatabase &&
               staleIocs <= totalIocsInDatabase &&
               falsePositiveIndicators <= threatIndicatorsFound &&
               confirmedThreatIndicators <= threatIndicatorsFound &&
               averageQueryResponseTimeMs >= 0 &&
               cacheHitRate >= 0 && cacheHitRate <= 100 &&
               accuracyRate >= 0 && accuracyRate <= 100 &&
               cpuUsagePercentage >= 0 && cpuUsagePercentage <= 100 &&
               memoryUsageMB >= 0 &&
               databaseSizeMB >= 0 &&
               serviceUptimeHours >= 0 &&
               timestamp != null;
    }
}
