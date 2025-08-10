package ph.gov.dsr.security.entity;

/**
 * Enumeration for network-specific trust levels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum NetworkTrustLevel {
    
    /**
     * Untrusted network - external/internet
     */
    UNTRUSTED("Untrusted", 0, "Untrusted network - external/internet"),
    
    /**
     * Guest network - limited access
     */
    GUEST("Guest", 1, "Guest network - limited access"),
    
    /**
     * DMZ network - semi-trusted
     */
    DMZ("DMZ", 2, "DMZ network - semi-trusted"),
    
    /**
     * Internal network - trusted
     */
    INTERNAL("Internal", 3, "Internal network - trusted"),
    
    /**
     * Secure network - highly trusted
     */
    SECURE("Secure", 4, "Secure network - highly trusted"),
    
    /**
     * Management network - fully trusted
     */
    MANAGEMENT("Management", 5, "Management network - fully trusted");
    
    private final String displayName;
    private final int numericValue;
    private final String description;
    
    NetworkTrustLevel(String displayName, int numericValue, String description) {
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
     * Get network trust level from numeric value
     */
    public static NetworkTrustLevel fromNumericValue(int value) {
        for (NetworkTrustLevel level : values()) {
            if (level.numericValue == value) {
                return level;
            }
        }
        return UNTRUSTED; // Default to untrusted if invalid value
    }
    
    /**
     * Get network trust level from string value (case insensitive)
     */
    public static NetworkTrustLevel fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNTRUSTED;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNTRUSTED; // Default to untrusted if invalid value
        }
    }
    
    /**
     * Check if this trust level is higher than another
     */
    public boolean isHigherThan(NetworkTrustLevel other) {
        return this.numericValue > other.numericValue;
    }
    
    /**
     * Check if this trust level is lower than another
     */
    public boolean isLowerThan(NetworkTrustLevel other) {
        return this.numericValue < other.numericValue;
    }
    
    /**
     * Check if this network allows outbound connections
     */
    public boolean allowsOutboundConnections() {
        return this.numericValue >= 1; // All except UNTRUSTED
    }
    
    /**
     * Check if this network allows inbound connections
     */
    public boolean allowsInboundConnections() {
        return this.numericValue >= 2; // DMZ and above
    }
    
    /**
     * Check if this network requires encryption
     */
    public boolean requiresEncryption() {
        return this.numericValue <= 2; // UNTRUSTED, GUEST, DMZ
    }
    
    /**
     * Check if this network requires VPN
     */
    public boolean requiresVPN() {
        return this == UNTRUSTED;
    }
    
    /**
     * Get default firewall action for this network
     */
    public String getDefaultFirewallAction() {
        switch (this) {
            case UNTRUSTED: return "DENY";
            case GUEST: return "RESTRICT";
            case DMZ: return "FILTER";
            case INTERNAL: return "ALLOW";
            case SECURE: return "ALLOW";
            case MANAGEMENT: return "ALLOW";
            default: return "DENY";
        }
    }
    
    /**
     * Get monitoring level for this network
     */
    public String getMonitoringLevel() {
        switch (this) {
            case UNTRUSTED: return "FULL";
            case GUEST: return "HIGH";
            case DMZ: return "MEDIUM";
            case INTERNAL: return "STANDARD";
            case SECURE: return "STANDARD";
            case MANAGEMENT: return "HIGH";
            default: return "FULL";
        }
    }
}
