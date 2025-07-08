package ph.gov.dsr.security.entity;

/**
 * Enumeration for Multi-Factor Authentication methods
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum MFAMethod {
    
    /**
     * SMS-based OTP
     */
    SMS("SMS", "SMS One-Time Password", true, false),
    
    /**
     * Email-based OTP
     */
    EMAIL("Email", "Email One-Time Password", true, false),
    
    /**
     * Time-based OTP (TOTP) using authenticator apps
     */
    TOTP("TOTP", "Time-based One-Time Password", false, true),
    
    /**
     * Hardware security keys (FIDO2/WebAuthn)
     */
    HARDWARE_KEY("Hardware Key", "FIDO2/WebAuthn Hardware Security Key", false, true),
    
    /**
     * Biometric authentication
     */
    BIOMETRIC("Biometric", "Biometric Authentication", false, true),
    
    /**
     * Push notifications to mobile app
     */
    PUSH_NOTIFICATION("Push", "Mobile Push Notification", true, true),
    
    /**
     * Voice call verification
     */
    VOICE_CALL("Voice", "Voice Call Verification", true, false),
    
    /**
     * Backup codes
     */
    BACKUP_CODES("Backup Codes", "One-time Backup Codes", false, false);
    
    private final String displayName;
    private final String description;
    private final boolean requiresConnectivity;
    private final boolean isPhishingResistant;
    
    MFAMethod(String displayName, String description, boolean requiresConnectivity, boolean isPhishingResistant) {
        this.displayName = displayName;
        this.description = description;
        this.requiresConnectivity = requiresConnectivity;
        this.isPhishingResistant = isPhishingResistant;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean requiresConnectivity() {
        return requiresConnectivity;
    }
    
    public boolean isPhishingResistant() {
        return isPhishingResistant;
    }
    
    /**
     * Get MFA method from string value (case insensitive)
     */
    public static MFAMethod fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return SMS; // Default to SMS
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (MFAMethod method : values()) {
                if (method.displayName.equalsIgnoreCase(value)) {
                    return method;
                }
            }
            return SMS; // Default fallback
        }
    }
    
    /**
     * Get security level of the MFA method
     */
    public SecurityLevel getSecurityLevel() {
        switch (this) {
            case HARDWARE_KEY:
            case BIOMETRIC:
                return SecurityLevel.HIGH;
            case TOTP:
            case PUSH_NOTIFICATION:
                return SecurityLevel.MEDIUM;
            case SMS:
            case EMAIL:
            case VOICE_CALL:
                return SecurityLevel.LOW;
            case BACKUP_CODES:
                return SecurityLevel.MINIMAL;
            default:
                return SecurityLevel.LOW;
        }
    }
    
    /**
     * Check if this method is recommended for high-security environments
     */
    public boolean isRecommendedForHighSecurity() {
        return isPhishingResistant && getSecurityLevel().ordinal() >= SecurityLevel.MEDIUM.ordinal();
    }
    
    /**
     * Security level enumeration
     */
    public enum SecurityLevel {
        MINIMAL,
        LOW,
        MEDIUM,
        HIGH
    }
}
