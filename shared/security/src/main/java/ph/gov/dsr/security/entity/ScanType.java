package ph.gov.dsr.security.entity;

/**
 * Enumeration for security scan types
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum ScanType {
    
    /**
     * Comprehensive scan including all scan types
     */
    COMPREHENSIVE("Comprehensive", "Full security assessment including web, network, and code scans"),
    
    /**
     * Web application vulnerability scan
     */
    WEB_APPLICATION("Web Application", "OWASP ZAP based web application security scan"),
    
    /**
     * Network infrastructure scan
     */
    NETWORK("Network", "Nessus based network infrastructure vulnerability scan"),
    
    /**
     * Code quality and security scan
     */
    CODE_QUALITY("Code Quality", "SonarQube based code quality and security analysis"),
    
    /**
     * Penetration testing scan
     */
    PENETRATION("Penetration", "Manual and automated penetration testing"),
    
    /**
     * Compliance audit scan
     */
    COMPLIANCE("Compliance", "Compliance framework validation scan"),
    
    /**
     * Scheduled automated scan
     */
    SCHEDULED("Scheduled", "Automated scheduled security scan"),
    
    /**
     * Quick vulnerability assessment
     */
    QUICK("Quick", "Fast vulnerability assessment scan"),
    
    /**
     * Deep security analysis
     */
    DEEP("Deep", "Comprehensive deep security analysis"),
    
    /**
     * Custom scan configuration
     */
    CUSTOM("Custom", "Custom configured security scan");
    
    private final String displayName;
    private final String description;
    
    ScanType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get scan type from string value (case insensitive)
     */
    public static ScanType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return QUICK;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return QUICK; // Default to quick if invalid value
        }
    }
    
    /**
     * Check if this scan type requires authentication
     */
    public boolean requiresAuthentication() {
        return this == WEB_APPLICATION || this == PENETRATION || this == COMPREHENSIVE;
    }
    
    /**
     * Check if this scan type is automated
     */
    public boolean isAutomated() {
        return this == SCHEDULED || this == QUICK || this == WEB_APPLICATION || this == NETWORK || this == CODE_QUALITY;
    }
    
    /**
     * Get estimated duration in minutes
     */
    public int getEstimatedDurationMinutes() {
        switch (this) {
            case QUICK: return 15;
            case WEB_APPLICATION: return 60;
            case NETWORK: return 90;
            case CODE_QUALITY: return 30;
            case PENETRATION: return 240;
            case COMPLIANCE: return 120;
            case COMPREHENSIVE: return 300;
            case DEEP: return 480;
            case SCHEDULED: return 60;
            case CUSTOM: return 120;
            default: return 60;
        }
    }
    
    /**
     * Check if this scan type requires manual intervention
     */
    public boolean requiresManualIntervention() {
        return this == PENETRATION || this == DEEP || this == CUSTOM;
    }
}
