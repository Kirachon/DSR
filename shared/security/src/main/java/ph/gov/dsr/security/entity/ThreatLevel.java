package ph.gov.dsr.security.entity;

/**
 * Enumeration for threat levels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum ThreatLevel {
    
    /**
     * No threat detected
     */
    NONE("None", 0, "No threat detected", "green"),
    
    /**
     * Low threat level
     */
    LOW("Low", 1, "Low threat level - routine monitoring", "blue"),
    
    /**
     * Medium threat level
     */
    MEDIUM("Medium", 2, "Medium threat level - increased monitoring", "yellow"),
    
    /**
     * High threat level
     */
    HIGH("High", 3, "High threat level - immediate attention required", "orange"),
    
    /**
     * Critical threat level
     */
    CRITICAL("Critical", 4, "Critical threat level - emergency response required", "red");
    
    private final String displayName;
    private final int numericValue;
    private final String description;
    private final String colorCode;
    
    ThreatLevel(String displayName, int numericValue, String description, String colorCode) {
        this.displayName = displayName;
        this.numericValue = numericValue;
        this.description = description;
        this.colorCode = colorCode;
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
    
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * Get threat level from numeric value
     */
    public static ThreatLevel fromNumericValue(int value) {
        for (ThreatLevel level : values()) {
            if (level.numericValue == value) {
                return level;
            }
        }
        return NONE; // Default to no threat if invalid value
    }
    
    /**
     * Get threat level from string value (case insensitive)
     */
    public static ThreatLevel fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NONE;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE; // Default to no threat if invalid value
        }
    }
    
    /**
     * Check if this threat level is higher than another
     */
    public boolean isHigherThan(ThreatLevel other) {
        return this.numericValue > other.numericValue;
    }
    
    /**
     * Check if this threat level is lower than another
     */
    public boolean isLowerThan(ThreatLevel other) {
        return this.numericValue < other.numericValue;
    }
    
    /**
     * Check if this threat level requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this.numericValue >= 3; // HIGH or CRITICAL
    }
    
    /**
     * Check if this threat level requires emergency response
     */
    public boolean requiresEmergencyResponse() {
        return this == CRITICAL;
    }
    
    /**
     * Get response time requirement in minutes
     */
    public int getResponseTimeMinutes() {
        switch (this) {
            case CRITICAL: return 5;    // 5 minutes
            case HIGH: return 30;       // 30 minutes
            case MEDIUM: return 120;    // 2 hours
            case LOW: return 480;       // 8 hours
            case NONE: return 0;        // No response required
            default: return 480;
        }
    }
    
    /**
     * Get escalation threshold in minutes
     */
    public int getEscalationThresholdMinutes() {
        switch (this) {
            case CRITICAL: return 15;   // 15 minutes
            case HIGH: return 60;       // 1 hour
            case MEDIUM: return 240;    // 4 hours
            case LOW: return 1440;      // 24 hours
            case NONE: return 0;        // No escalation
            default: return 1440;
        }
    }
    
    /**
     * Check if automatic blocking should be enabled
     */
    public boolean shouldAutoBlock() {
        return this.numericValue >= 3; // HIGH or CRITICAL
    }
    
    /**
     * Check if notifications should be sent
     */
    public boolean shouldSendNotifications() {
        return this.numericValue >= 2; // MEDIUM, HIGH, or CRITICAL
    }
}
