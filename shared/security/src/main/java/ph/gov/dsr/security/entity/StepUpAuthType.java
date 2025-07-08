package ph.gov.dsr.security.entity;

/**
 * Enumeration for step-up authentication types
 * Defines the various methods available for additional authentication verification
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum StepUpAuthType {
    
    /**
     * SMS-based one-time password
     */
    SMS("SMS OTP", "One-time password sent via SMS", 60, true, false),
    
    /**
     * Email-based one-time password
     */
    EMAIL("Email OTP", "One-time password sent via email", 70, true, false),
    
    /**
     * Time-based one-time password (TOTP)
     */
    TOTP("TOTP", "Time-based one-time password from authenticator app", 85, false, true),
    
    /**
     * Hardware security key (FIDO2/WebAuthn)
     */
    HARDWARE_KEY("Hardware Key", "FIDO2/WebAuthn hardware security key", 95, false, true),
    
    /**
     * Biometric authentication (fingerprint, face, etc.)
     */
    BIOMETRIC("Biometric", "Fingerprint, face recognition, or other biometric", 90, false, true),
    
    /**
     * Push notification to mobile app
     */
    PUSH_NOTIFICATION("Push Notification", "Push notification to registered mobile app", 80, true, true),
    
    /**
     * Voice call verification
     */
    VOICE_CALL("Voice Call", "Automated voice call with verification code", 65, true, false),
    
    /**
     * Backup codes
     */
    BACKUP_CODE("Backup Code", "Pre-generated backup recovery code", 75, false, false),
    
    /**
     * Smart card authentication
     */
    SMART_CARD("Smart Card", "Smart card with PKI certificate", 90, false, true),
    
    /**
     * QR code scanning
     */
    QR_CODE("QR Code", "QR code scanning verification", 70, false, true),
    
    /**
     * Security questions
     */
    SECURITY_QUESTIONS("Security Questions", "Pre-configured security questions", 50, false, false),
    
    /**
     * Trusted device verification
     */
    TRUSTED_DEVICE("Trusted Device", "Verification from a trusted device", 85, true, true);
    
    private final String displayName;
    private final String description;
    private final int securityStrength; // 0-100 scale
    private final boolean requiresNetwork; // Requires network connectivity
    private final boolean supportsOffline; // Can work offline
    
    StepUpAuthType(String displayName, String description, int securityStrength, 
                   boolean requiresNetwork, boolean supportsOffline) {
        this.displayName = displayName;
        this.description = description;
        this.securityStrength = securityStrength;
        this.requiresNetwork = requiresNetwork;
        this.supportsOffline = supportsOffline;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getSecurityStrength() {
        return securityStrength;
    }
    
    public boolean requiresNetwork() {
        return requiresNetwork;
    }
    
    public boolean supportsOffline() {
        return supportsOffline;
    }
    
    /**
     * Check if this authentication type is considered strong
     */
    public boolean isStrongAuthentication() {
        return securityStrength >= 80;
    }
    
    /**
     * Check if this authentication type is considered weak
     */
    public boolean isWeakAuthentication() {
        return securityStrength < 60;
    }
    
    /**
     * Check if this authentication type requires user interaction
     */
    public boolean requiresUserInteraction() {
        return this != TRUSTED_DEVICE; // Most methods require user interaction except trusted device
    }
    
    /**
     * Check if this authentication type is phishing resistant
     */
    public boolean isPhishingResistant() {
        return this == HARDWARE_KEY || 
               this == BIOMETRIC || 
               this == SMART_CARD ||
               this == PUSH_NOTIFICATION;
    }
    
    /**
     * Check if this authentication type is replay resistant
     */
    public boolean isReplayResistant() {
        return this == HARDWARE_KEY || 
               this == BIOMETRIC || 
               this == SMART_CARD ||
               this == TOTP ||
               this == SMS ||
               this == EMAIL;
    }
    
    /**
     * Get authentication type from string value (case insensitive)
     */
    public static StepUpAuthType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (StepUpAuthType type : values()) {
                if (type.displayName.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    /**
     * Get authentication types by minimum security strength
     */
    public static StepUpAuthType[] getByMinimumStrength(int minimumStrength) {
        return java.util.Arrays.stream(values())
            .filter(type -> type.securityStrength >= minimumStrength)
            .toArray(StepUpAuthType[]::new);
    }
    
    /**
     * Get authentication types that work offline
     */
    public static StepUpAuthType[] getOfflineCapable() {
        return java.util.Arrays.stream(values())
            .filter(StepUpAuthType::supportsOffline)
            .toArray(StepUpAuthType[]::new);
    }
    
    /**
     * Get authentication types that are phishing resistant
     */
    public static StepUpAuthType[] getPhishingResistant() {
        return java.util.Arrays.stream(values())
            .filter(StepUpAuthType::isPhishingResistant)
            .toArray(StepUpAuthType[]::new);
    }
    
    /**
     * Get authentication types suitable for high-security scenarios
     */
    public static StepUpAuthType[] getHighSecurity() {
        return java.util.Arrays.stream(values())
            .filter(type -> type.isStrongAuthentication() && type.isPhishingResistant())
            .toArray(StepUpAuthType[]::new);
    }
    
    /**
     * Get recommended authentication type based on risk level
     */
    public static StepUpAuthType getRecommendedForRiskLevel(String riskLevel) {
        if (riskLevel == null) {
            return TOTP; // Default fallback
        }
        
        return switch (riskLevel.toUpperCase()) {
            case "LOW" -> SMS;
            case "MEDIUM" -> TOTP;
            case "HIGH" -> HARDWARE_KEY;
            case "CRITICAL" -> BIOMETRIC;
            default -> TOTP;
        };
    }
    
    /**
     * Get fallback authentication types for this type
     */
    public StepUpAuthType[] getFallbackTypes() {
        return switch (this) {
            case HARDWARE_KEY -> new StepUpAuthType[]{BIOMETRIC, TOTP, SMS};
            case BIOMETRIC -> new StepUpAuthType[]{HARDWARE_KEY, TOTP, PUSH_NOTIFICATION};
            case TOTP -> new StepUpAuthType[]{SMS, EMAIL, BACKUP_CODE};
            case PUSH_NOTIFICATION -> new StepUpAuthType[]{TOTP, SMS, EMAIL};
            case SMS -> new StepUpAuthType[]{EMAIL, VOICE_CALL, BACKUP_CODE};
            case EMAIL -> new StepUpAuthType[]{SMS, VOICE_CALL, SECURITY_QUESTIONS};
            case SMART_CARD -> new StepUpAuthType[]{HARDWARE_KEY, BIOMETRIC, TOTP};
            case QR_CODE -> new StepUpAuthType[]{TOTP, PUSH_NOTIFICATION, SMS};
            case VOICE_CALL -> new StepUpAuthType[]{SMS, EMAIL, SECURITY_QUESTIONS};
            case BACKUP_CODE -> new StepUpAuthType[]{SECURITY_QUESTIONS};
            case SECURITY_QUESTIONS -> new StepUpAuthType[]{BACKUP_CODE};
            case TRUSTED_DEVICE -> new StepUpAuthType[]{PUSH_NOTIFICATION, TOTP, SMS};
        };
    }
    
    /**
     * Get expected response time in seconds
     */
    public int getExpectedResponseTimeSeconds() {
        return switch (this) {
            case HARDWARE_KEY, BIOMETRIC, SMART_CARD, QR_CODE -> 30;
            case TOTP, BACKUP_CODE, SECURITY_QUESTIONS -> 60;
            case PUSH_NOTIFICATION, TRUSTED_DEVICE -> 120;
            case SMS, VOICE_CALL -> 180;
            case EMAIL -> 300;
        };
    }
    
    /**
     * Get maximum allowed attempts
     */
    public int getMaxAttempts() {
        return switch (this) {
            case HARDWARE_KEY, BIOMETRIC, SMART_CARD -> 3;
            case TOTP, SMS, EMAIL, VOICE_CALL -> 3;
            case PUSH_NOTIFICATION, QR_CODE -> 2;
            case BACKUP_CODE -> 1; // Backup codes are single use
            case SECURITY_QUESTIONS -> 3;
            case TRUSTED_DEVICE -> 1;
        };
    }
    
    /**
     * Check if this type supports multiple simultaneous challenges
     */
    public boolean supportsMultipleChallenges() {
        return this == SMS || 
               this == EMAIL || 
               this == VOICE_CALL ||
               this == PUSH_NOTIFICATION;
    }
    
    /**
     * Get user-friendly setup instructions
     */
    public String getSetupInstructions() {
        return switch (this) {
            case SMS -> "Enter your mobile phone number to receive SMS codes";
            case EMAIL -> "Verify your email address to receive verification codes";
            case TOTP -> "Install an authenticator app and scan the QR code";
            case HARDWARE_KEY -> "Insert your security key and follow the prompts";
            case BIOMETRIC -> "Register your biometric data (fingerprint, face, etc.)";
            case PUSH_NOTIFICATION -> "Install the mobile app and enable push notifications";
            case VOICE_CALL -> "Provide a phone number for voice verification calls";
            case BACKUP_CODE -> "Generate and securely store backup recovery codes";
            case SMART_CARD -> "Insert your smart card and enter your PIN";
            case QR_CODE -> "Use your mobile app to scan verification QR codes";
            case SECURITY_QUESTIONS -> "Set up security questions and answers";
            case TRUSTED_DEVICE -> "Register this device as a trusted device";
        };
    }
}
