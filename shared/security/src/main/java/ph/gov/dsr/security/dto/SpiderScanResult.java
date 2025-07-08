package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for spider scan results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpiderScanResult {

    private String scanId;
    private String targetUrl;
    private String status;
    private Integer progress;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long duration;
    private Integer urlsFound;
    private Integer urlsProcessed;
    private List<String> discoveredUrls;
    private List<String> excludedUrls;
    private String errorMessage;
    private String configuration;
    private Integer maxDepth;
    private Integer maxChildren;
    private Boolean followRedirects;
    private String userAgent;
    private Integer requestDelay;
    private String authenticationMethod;
    private String sessionManagement;
    
    /**
     * Check if spider scan completed successfully
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    /**
     * Check if spider scan failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    /**
     * Get completion percentage
     */
    public double getCompletionPercentage() {
        if (progress != null) {
            return progress.doubleValue();
        }
        if (urlsFound != null && urlsFound > 0 && urlsProcessed != null) {
            return (double) urlsProcessed / urlsFound * 100.0;
        }
        return 0.0;
    }
}
