package ph.gov.dsr.security.entity;

/**
 * Enumeration for trust levels in zero-trust security model
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum TrustLevel {
    
    /**
     * No trust - deny all access
     */
    NONE("None", 0, "No trust - deny all access"),
    
    /**
     * Minimal trust - very limited access
     */
    MINIMAL("Minimal", 1, "Minimal trust - very limited access"),
    
    /**
     * Low trust - basic access with restrictions
     */
    LOW("Low", 2, "Low trust - basic access with restrictions"),
    
    /**
     * Medium trust - standard access
     */
    MEDIUM("Medium", 3, "Medium trust - standard access"),
    
    /**
     * High trust - elevated access
     */
    HIGH("High", 4, "High trust - elevated access"),
    
    /**
     * Full trust - unrestricted access
     */
    FULL("Full", 5, "Full trust - unrestricted access");
    
    private final String displayName;
    private final int numericValue;
    private final String description;
    
    TrustLevel(String displayName, int numericValue, String description) {
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
     * Get trust level from numeric value
     */
    public static TrustLevel fromNumericValue(int value) {
        for (TrustLevel level : values()) {
            if (level.numericValue == value) {
                return level;
            }
        }
        return NONE; // Default to no trust if invalid value
    }
    
    /**
     * Get trust level from string value (case insensitive)
     */
    public static TrustLevel fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NONE;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE; // Default to no trust if invalid value
        }
    }
    
    /**
     * Check if this trust level is higher than another
     */
    public boolean isHigherThan(TrustLevel other) {
        return this.numericValue > other.numericValue;
    }
    
    /**
     * Check if this trust level is lower than another
     */
    public boolean isLowerThan(TrustLevel other) {
        return this.numericValue < other.numericValue;
    }
    
    /**
     * Check if this trust level allows access
     */
    public boolean allowsAccess() {
        return this.numericValue > 0;
    }
    
    /**
     * Check if this trust level allows elevated operations
     */
    public boolean allowsElevatedOperations() {
        return this.numericValue >= 4; // HIGH or FULL
    }
    
    /**
     * Check if this trust level requires additional verification
     */
    public boolean requiresAdditionalVerification() {
        return this.numericValue <= 2; // NONE, MINIMAL, or LOW
    }
    
    /**
     * Get the maximum allowed session duration in minutes
     */
    public int getMaxSessionDurationMinutes() {
        switch (this) {
            case NONE: return 0;
            case MINIMAL: return 15;
            case LOW: return 60;
            case MEDIUM: return 240;    // 4 hours
            case HIGH: return 480;      // 8 hours
            case FULL: return 1440;     // 24 hours
            default: return 0;
        }
    }
    
    /**
     * Get the required re-authentication interval in minutes
     */
    public int getReAuthenticationIntervalMinutes() {
        switch (this) {
            case NONE: return 0;        // No access
            case MINIMAL: return 5;
            case LOW: return 30;
            case MEDIUM: return 120;    // 2 hours
            case HIGH: return 240;      // 4 hours
            case FULL: return 480;      // 8 hours
            default: return 5;
        }
    }
}
