package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration DTO for OWASP ZAP scans
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OWASPZAPScanConfig {

    private String targetUrl;
    private String scanPolicy;
    private String authenticationConfig;
    private String excludeUrls;
    private String includeUrls;
    private String contextName;
    private String sessionManagement;
    private String userAgent;
    private Integer maxDepth;
    private Integer maxChildren;
    private Integer maxDuration;
    private Integer requestDelay;
    private Boolean followRedirects;
    private Boolean handleOAuthParameters;
    private Boolean parseComments;
    private Boolean parseRobotsTxt;
    private Boolean parseSitemapXml;
    private Boolean postForm;
    private Boolean processForm;
    private String spiderThreads;
    private String activeScanThreads;
    private String activeScanPolicy;
    private List<String> excludeRegexes;
    private List<String> includeRegexes;
    private String proxyHost;
    private Integer proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private Boolean useProxy;
    private String apiKey;
    private String zapHost;
    private Integer zapPort;
    private Integer connectionTimeout;
    private Integer readTimeout;
    
    /**
     * Check if authentication is configured
     */
    public boolean hasAuthentication() {
        return authenticationConfig != null && !authenticationConfig.trim().isEmpty();
    }
    
    /**
     * Check if proxy is configured
     */
    public boolean hasProxy() {
        return Boolean.TRUE.equals(useProxy) && 
               proxyHost != null && !proxyHost.trim().isEmpty() &&
               proxyPort != null;
    }
    
    /**
     * Check if custom scan policy is configured
     */
    public boolean hasCustomScanPolicy() {
        return scanPolicy != null && !scanPolicy.trim().isEmpty();
    }
    
    /**
     * Get effective max depth (default to 5 if not specified)
     */
    public int getEffectiveMaxDepth() {
        return maxDepth != null ? maxDepth : 5;
    }
    
    /**
     * Get effective max children (default to 10 if not specified)
     */
    public int getEffectiveMaxChildren() {
        return maxChildren != null ? maxChildren : 10;
    }
    
    /**
     * Get effective request delay (default to 0 if not specified)
     */
    public int getEffectiveRequestDelay() {
        return requestDelay != null ? requestDelay : 0;
    }
    
    /**
     * Get ZAP API endpoint URL
     */
    public String getZapApiUrl() {
        String host = zapHost != null ? zapHost : "localhost";
        int port = zapPort != null ? zapPort : 8080;
        return String.format("http://%s:%d", host, port);
    }
    
    /**
     * Validate configuration
     */
    public boolean isValid() {
        return targetUrl != null && !targetUrl.trim().isEmpty();
    }
}
