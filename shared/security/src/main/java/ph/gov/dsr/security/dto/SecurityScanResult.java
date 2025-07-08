package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for security scan results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityScanResult {

    private UUID scanId;

    private String scanType;

    private String scanTool;

    private String targetType;

    private String targetIdentifier;

    private String status; // SCHEDULED, RUNNING, COMPLETED, FAILED, CANCELLED

    private Integer progressPercentage;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Long durationMinutes;

    private UUID initiatedBy;

    private String initiatedByUsername;

    private Integer totalFindings;

    private Integer criticalFindings;

    private Integer highFindings;

    private Integer mediumFindings;

    private Integer lowFindings;

    private Integer infoFindings;

    private Integer falsePositives;

    private Double overallRiskScore; // 0.0 to 100.0

    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    private List<Finding> findings;

    private String executiveSummary;

    private String technicalSummary;

    private List<String> recommendations;

    private String reportPath;

    private Boolean reportGenerated;

    private String scanConfiguration;

    private String scanResults; // Raw scan results as JSON

    private String errorMessage;

    private String scanEngineVersion;

    private String scanRulesVersion;

    private List<String> complianceFrameworks;

    private ComplianceStatus complianceStatus;

    private BaselineComparison baselineComparison;

    private String tags; // JSON array of tags

    private String metadata; // Additional metadata as JSON

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Additional fields for VulnerabilityScanner compatibility
    private WebApplicationScanResult webApplicationResult;
    private NetworkScanResult networkResult;
    private CodeQualityScanResult codeQualityResult;

    /**
     * Finding details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Finding {
        private String id;
        private String title;
        private String description;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private String category;
        private String cweId;
        private String cvssScore;
        private String location;
        private String evidence;
        private String recommendation;
        private Boolean falsePositive;
        private String status; // NEW, CONFIRMED, FIXED, IGNORED
        private LocalDateTime firstDetected;
        private LocalDateTime lastSeen;
    }

    /**
     * Compliance status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceStatus {
        private String framework; // OWASP, PCI_DSS, ISO_27001, NIST
        private String status; // COMPLIANT, NON_COMPLIANT, PARTIAL
        private Integer passedChecks;
        private Integer failedChecks;
        private Integer totalChecks;
        private Double compliancePercentage;
        private List<String> failedRequirements;
    }

    /**
     * Baseline comparison
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaselineComparison {
        private UUID baselineScanId;
        private LocalDateTime baselineScanDate;
        private Integer newFindings;
        private Integer resolvedFindings;
        private Integer unchangedFindings;
        private String trendDirection; // IMPROVING, DEGRADING, STABLE
        private Double riskScoreChange;
        private String summary;
    }

    /**
     * Check if scan completed successfully
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Check if scan failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if scan is still running
     */
    public boolean isRunning() {
        return "RUNNING".equals(status);
    }

    /**
     * Get total number of vulnerabilities
     */
    public Integer getTotalVulnerabilities() {
        return totalFindings != null ? totalFindings : 0;
    }

    /**
     * Get number of critical vulnerabilities
     */
    public Integer getCriticalVulnerabilities() {
        return criticalFindings != null ? criticalFindings : 0;
    }

    /**
     * Get number of high vulnerabilities
     */
    public Integer getHighVulnerabilities() {
        return highFindings != null ? highFindings : 0;
    }

    /**
     * Get number of medium vulnerabilities
     */
    public Integer getMediumVulnerabilities() {
        return mediumFindings != null ? mediumFindings : 0;
    }

    /**
     * Get number of low vulnerabilities
     */
    public Integer getLowVulnerabilities() {
        return lowFindings != null ? lowFindings : 0;
    }

    /**
     * Get web application scan result
     */
    public WebApplicationScanResult getWebApplicationResult() {
        return webApplicationResult;
    }

    /**
     * Get network scan result
     */
    public NetworkScanResult getNetworkResult() {
        return networkResult;
    }

    /**
     * Get code quality scan result
     */
    public CodeQualityScanResult getCodeQualityResult() {
        return codeQualityResult;
    }

    /**
     * Check if scan has high-severity findings
     */
    public boolean hasHighSeverityFindings() {
        return (criticalFindings != null && criticalFindings > 0) ||
               (highFindings != null && highFindings > 0);
    }

    /**
     * Check if scan has critical findings
     */
    public boolean hasCriticalFindings() {
        return criticalFindings != null && criticalFindings > 0;
    }

    /**
     * Get total findings count
     */
    public int getTotalFindingsCount() {
        int total = 0;
        if (criticalFindings != null) total += criticalFindings;
        if (highFindings != null) total += highFindings;
        if (mediumFindings != null) total += mediumFindings;
        if (lowFindings != null) total += lowFindings;
        if (infoFindings != null) total += infoFindings;
        return total;
    }

    /**
     * Check if immediate action is required
     */
    public boolean requiresImmediateAction() {
        return "CRITICAL".equals(riskLevel) || hasCriticalFindings();
    }

    /**
     * Get scan duration in human-readable format
     */
    public String getFormattedDuration() {
        if (durationMinutes == null) {
            return "Unknown";
        }
        
        if (durationMinutes < 60) {
            return durationMinutes + " minutes";
        } else {
            long hours = durationMinutes / 60;
            long minutes = durationMinutes % 60;
            return hours + "h " + minutes + "m";
        }
    }
}
