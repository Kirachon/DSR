package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration DTO for Nessus scans
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NessusScanConfig {

    private String targets;
    private String scanTemplate;
    private String credentials;
    private String policyId;
    private String scanName;
    private String description;
    private String folderId;
    private String scannerId;
    private Boolean enabled;
    private String launch;
    private String timezone;
    private String startTime;
    private String rrules;
    private List<String> targetList;
    private List<String> excludeTargets;
    private String portRange;
    private Boolean pingTargets;
    private Boolean unscannedCgiPath;
    private Boolean safeChecks;
    private Boolean stopScanOnDisconnect;
    private Integer maxChecksPerHost;
    private Integer maxHostsPerScan;
    private Integer networkReceiveTimeout;
    private Integer networkConnectTimeout;
    private String pluginSet;
    private List<String> enabledPlugins;
    private List<String> disabledPlugins;
    private String reportFormat;
    private Boolean emailNotification;
    private String emailRecipients;
    private String tags;
    private String acls;
    
    /**
     * Check if credentials are configured
     */
    public boolean hasCredentials() {
        return credentials != null && !credentials.trim().isEmpty();
    }
    
    /**
     * Check if custom policy is configured
     */
    public boolean hasCustomPolicy() {
        return policyId != null && !policyId.trim().isEmpty();
    }
    
    /**
     * Check if scan is scheduled
     */
    public boolean isScheduled() {
        return startTime != null && !startTime.trim().isEmpty();
    }
    
    /**
     * Check if email notifications are enabled
     */
    public boolean hasEmailNotification() {
        return Boolean.TRUE.equals(emailNotification) && 
               emailRecipients != null && !emailRecipients.trim().isEmpty();
    }
    
    /**
     * Get effective max checks per host (default to 5 if not specified)
     */
    public int getEffectiveMaxChecksPerHost() {
        return maxChecksPerHost != null ? maxChecksPerHost : 5;
    }
    
    /**
     * Get effective max hosts per scan (default to 30 if not specified)
     */
    public int getEffectiveMaxHostsPerScan() {
        return maxHostsPerScan != null ? maxHostsPerScan : 30;
    }
    
    /**
     * Get effective network receive timeout (default to 5 seconds if not specified)
     */
    public int getEffectiveNetworkReceiveTimeout() {
        return networkReceiveTimeout != null ? networkReceiveTimeout : 5;
    }
    
    /**
     * Get effective network connect timeout (default to 5 seconds if not specified)
     */
    public int getEffectiveNetworkConnectTimeout() {
        return networkConnectTimeout != null ? networkConnectTimeout : 5;
    }
    
    /**
     * Get effective port range (default to common ports if not specified)
     */
    public String getEffectivePortRange() {
        return portRange != null ? portRange : "1-65535";
    }
    
    /**
     * Validate configuration
     */
    public boolean isValid() {
        return targets != null && !targets.trim().isEmpty();
    }
}
