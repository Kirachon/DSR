package ph.gov.dsr.security.entity;

/**
 * Enumeration for anomaly severity levels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum AnomalySeverity {
    
    /**
     * Low - Minor anomaly with minimal impact
     */
    LOW("Low", 1, "Minor anomaly with minimal impact"),
    
    /**
     * Medium - Moderate anomaly requiring attention
     */
    MEDIUM("Medium", 2, "Moderate anomaly requiring attention"),
    
    /**
     * High - Significant anomaly requiring immediate attention
     */
    HIGH("High", 3, "Significant anomaly requiring immediate attention"),
    
    /**
     * Critical - Critical anomaly requiring emergency response
     */
    CRITICAL("Critical", 4, "Critical anomaly requiring emergency response");

    private final String displayName;
    private final int level;
    private final String description;

    AnomalySeverity(String displayName, int level, String description) {
        this.displayName = displayName;
        this.level = level;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Get severity from string value (case insensitive)
     */
    public static AnomalySeverity fromString(String value) {
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
     * Check if this severity is higher than another
     */
    public boolean isHigherThan(AnomalySeverity other) {
        return this.level > other.level;
    }
    
    /**
     * Check if this severity is lower than another
     */
    public boolean isLowerThan(AnomalySeverity other) {
        return this.level < other.level;
    }
    
    /**
     * Check if this severity requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this == HIGH || this == CRITICAL;
    }
    
    /**
     * Check if this severity requires emergency response
     */
    public boolean requiresEmergencyResponse() {
        return this == CRITICAL;
    }
    
    /**
     * Get recommended response time in minutes
     */
    public int getResponseTimeInMinutes() {
        switch (this) {
            case CRITICAL: return 5;     // 5 minutes
            case HIGH: return 30;        // 30 minutes
            case MEDIUM: return 240;     // 4 hours
            case LOW: return 1440;       // 24 hours
            default: return 240;
        }
    }
    
    /**
     * Get numeric score for this severity (0-100)
     */
    public int getScore() {
        switch (this) {
            case CRITICAL: return 100;
            case HIGH: return 80;
            case MEDIUM: return 60;
            case LOW: return 40;
            default: return 40;
        }
    }
}
