package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for active scan results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveScanResult {

    private String scanId;
    private String targetUrl;
    private String status;
    private Integer progress;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long duration;
    private Integer totalRequests;
    private Integer alertsRaised;
    private List<String> scannedUrls;
    private List<String> attackTypes;
    private String errorMessage;
    private String configuration;
    private String scanPolicy;
    private Integer maxRuleDuration;
    private Integer maxScanDuration;
    private Boolean delayInMs;
    private String userAgent;
    private String authenticationMethod;
    private String sessionManagement;
    private Integer threadsPerHost;
    private String hostPerScan;
    
    /**
     * Check if active scan completed successfully
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    /**
     * Check if active scan failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    /**
     * Get completion percentage
     */
    public double getCompletionPercentage() {
        return progress != null ? progress.doubleValue() : 0.0;
    }
    
    /**
     * Get alerts per request ratio
     */
    public double getAlertsPerRequestRatio() {
        if (totalRequests != null && totalRequests > 0 && alertsRaised != null) {
            return (double) alertsRaised / totalRequests;
        }
        return 0.0;
    }
}
