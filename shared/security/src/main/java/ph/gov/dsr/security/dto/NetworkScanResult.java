package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for network scan results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkScanResult {

    private String scanId;
    private String targets;
    private List<NetworkVulnerability> vulnerabilities;
    private Long scanDuration;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;
    private String scanTool;
    private String scanVersion;
    private Integer totalHosts;
    private Integer scannedHosts;
    private Integer totalPorts;
    private Integer openPorts;
    private Integer totalPlugins;
    private String reportPath;
    private String executiveSummary;
    private String technicalSummary;
    private List<String> recommendations;
    private String errorMessage;
    private String scanConfiguration;
    private String metadata;
    private List<String> hostList;
    private List<String> portList;
    private String credentialStatus;
    private String complianceResults;
    
    /**
     * Get total number of vulnerabilities
     */
    public int getTotalVulnerabilities() {
        return vulnerabilities != null ? vulnerabilities.size() : 0;
    }
    
    /**
     * Get number of critical vulnerabilities
     */
    public long getCriticalVulnerabilities() {
        return vulnerabilities != null ? 
            vulnerabilities.stream().filter(NetworkVulnerability::isCritical).count() : 0;
    }
    
    /**
     * Get number of high severity vulnerabilities
     */
    public long getHighVulnerabilities() {
        return vulnerabilities != null ? 
            vulnerabilities.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().name().equals("HIGH"))
                .count() : 0;
    }
    
    /**
     * Get number of medium severity vulnerabilities
     */
    public long getMediumVulnerabilities() {
        return vulnerabilities != null ? 
            vulnerabilities.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().name().equals("MEDIUM"))
                .count() : 0;
    }
    
    /**
     * Get number of low severity vulnerabilities
     */
    public long getLowVulnerabilities() {
        return vulnerabilities != null ? 
            vulnerabilities.stream()
                .filter(v -> v.getSeverity() != null && v.getSeverity().name().equals("LOW"))
                .count() : 0;
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
     * Check if there are critical vulnerabilities
     */
    public boolean hasCriticalVulnerabilities() {
        return getCriticalVulnerabilities() > 0;
    }
    
    /**
     * Get scan coverage percentage
     */
    public double getScanCoverage() {
        if (totalHosts == null || totalHosts == 0) {
            return 0.0;
        }
        return (double) (scannedHosts != null ? scannedHosts : 0) / totalHosts * 100.0;
    }
}
