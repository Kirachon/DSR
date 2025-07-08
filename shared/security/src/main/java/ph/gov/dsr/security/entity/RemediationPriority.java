package ph.gov.dsr.security.entity;

/**
 * Enumeration for remediation priority levels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum RemediationPriority {
    
    /**
     * Low priority - can be addressed in next maintenance cycle
     */
    LOW("Low", 1, "Can be addressed in next maintenance cycle", 90),
    
    /**
     * Medium priority - should be addressed within a month
     */
    MEDIUM("Medium", 2, "Should be addressed within a month", 30),
    
    /**
     * High priority - should be addressed within a week
     */
    HIGH("High", 3, "Should be addressed within a week", 7),
    
    /**
     * Critical priority - must be addressed immediately
     */
    CRITICAL("Critical", 4, "Must be addressed immediately", 1),
    
    /**
     * Emergency priority - requires immediate action
     */
    EMERGENCY("Emergency", 5, "Requires immediate action", 0);
    
    private final String displayName;
    private final int numericValue;
    private final String description;
    private final int slaInDays;
    
    RemediationPriority(String displayName, int numericValue, String description, int slaInDays) {
        this.displayName = displayName;
        this.numericValue = numericValue;
        this.description = description;
        this.slaInDays = slaInDays;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getNumericValue() {
        return numericValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getSlaInDays() {
        return slaInDays;
    }
    
    /**
     * Get remediation priority from numeric value
     */
    public static RemediationPriority fromNumericValue(int value) {
        for (RemediationPriority priority : values()) {
            if (priority.numericValue == value) {
                return priority;
            }
        }
        return LOW; // Default to low if invalid value
    }
    
    /**
     * Get remediation priority from string value (case insensitive)
     */
    public static RemediationPriority fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LOW;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LOW; // Default to low if invalid value
        }
    }
    
    /**
     * Get remediation priority from vulnerability severity
     */
    public static RemediationPriority fromVulnerabilitySeverity(VulnerabilitySeverity severity) {
        switch (severity) {
            case CRITICAL: return EMERGENCY;
            case HIGH: return CRITICAL;
            case MEDIUM: return HIGH;
            case LOW: return MEDIUM;
            case NONE: return LOW;
            default: return LOW;
        }
    }
    
    /**
     * Get remediation priority from security risk level
     */
    public static RemediationPriority fromSecurityRiskLevel(SecurityRiskLevel riskLevel) {
        switch (riskLevel) {
            case CRITICAL: return EMERGENCY;
            case HIGH: return CRITICAL;
            case MEDIUM: return HIGH;
            case LOW: return MEDIUM;
            default: return LOW;
        }
    }
    
    /**
     * Check if this priority is higher than another
     */
    public boolean isHigherThan(RemediationPriority other) {
        return this.numericValue > other.numericValue;
    }
    
    /**
     * Check if this priority is lower than another
     */
    public boolean isLowerThan(RemediationPriority other) {
        return this.numericValue < other.numericValue;
    }
    
    /**
     * Check if this priority requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this.numericValue >= 4; // CRITICAL or EMERGENCY
    }
    
    /**
     * Check if this priority requires emergency response
     */
    public boolean requiresEmergencyResponse() {
        return this == EMERGENCY;
    }
    
    /**
     * Get escalation threshold in hours
     */
    public int getEscalationThresholdHours() {
        switch (this) {
            case EMERGENCY: return 1;   // 1 hour
            case CRITICAL: return 4;    // 4 hours
            case HIGH: return 24;       // 1 day
            case MEDIUM: return 72;     // 3 days
            case LOW: return 168;       // 1 week
            default: return 168;
        }
    }
    
    /**
     * Get notification frequency in hours
     */
    public int getNotificationFrequencyHours() {
        switch (this) {
            case EMERGENCY: return 1;   // Every hour
            case CRITICAL: return 4;    // Every 4 hours
            case HIGH: return 24;       // Daily
            case MEDIUM: return 72;     // Every 3 days
            case LOW: return 168;       // Weekly
            default: return 168;
        }
    }
    
    /**
     * Check if automatic assignment should be enabled
     */
    public boolean shouldAutoAssign() {
        return this.numericValue >= 3; // HIGH, CRITICAL, or EMERGENCY
    }
    
    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        switch (this) {
            case EMERGENCY: return "#FF0000"; // Red
            case CRITICAL: return "#FF6600"; // Orange-Red
            case HIGH: return "#FF9900";      // Orange
            case MEDIUM: return "#FFCC00";    // Yellow
            case LOW: return "#00CC00";       // Green
            default: return "#808080";        // Gray
        }
    }
}
