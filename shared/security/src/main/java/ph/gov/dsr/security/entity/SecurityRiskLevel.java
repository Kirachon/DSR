package ph.gov.dsr.security.entity;

/**
 * Enumeration for security risk levels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum SecurityRiskLevel {
    
    /**
     * Low risk - minimal impact, routine monitoring
     */
    LOW("Low", 1, "Minimal impact, routine monitoring required"),
    
    /**
     * Medium risk - moderate impact, increased monitoring
     */
    MEDIUM("Medium", 2, "Moderate impact, increased monitoring required"),
    
    /**
     * High risk - significant impact, immediate attention
     */
    HIGH("High", 3, "Significant impact, immediate attention required"),
    
    /**
     * Critical risk - severe impact, emergency response
     */
    CRITICAL("Critical", 4, "Severe impact, emergency response required");
    
    private final String displayName;
    private final int numericValue;
    private final String description;
    
    SecurityRiskLevel(String displayName, int numericValue, String description) {
        this.displayName = displayName;
        this.numericValue = numericValue;
        this.description = description;
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
    
    /**
     * Get risk level from numeric value
     */
    public static SecurityRiskLevel fromNumericValue(int value) {
        for (SecurityRiskLevel level : values()) {
            if (level.numericValue == value) {
                return level;
            }
        }
        return LOW; // Default to low if invalid value
    }
    
    /**
     * Get risk level from string value (case insensitive)
     */
    public static SecurityRiskLevel fromString(String value) {
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
     * Check if this risk level is higher than another
     */
    public boolean isHigherThan(SecurityRiskLevel other) {
        return this.numericValue > other.numericValue;
    }
    
    /**
     * Check if this risk level is lower than another
     */
    public boolean isLowerThan(SecurityRiskLevel other) {
        return this.numericValue < other.numericValue;
    }
    
    /**
     * Check if this risk level requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this == HIGH || this == CRITICAL;
    }
    
    /**
     * Check if this risk level requires emergency response
     */
    public boolean requiresEmergencyResponse() {
        return this == CRITICAL;
    }
}
