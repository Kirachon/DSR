package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Device Registration Request DTO
 * Contains all information needed for device registration in the zero-trust security system
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationRequest {

    /**
     * Unique device identifier
     */
    @Size(max = 255, message = "Device ID cannot exceed 255 characters")
    private String deviceId;

    /**
     * User ID who owns the device
     */
    @Size(max = 255, message = "User ID cannot exceed 255 characters")
    private String userId;

    /**
     * Device name/label provided by user
     */
    @Size(max = 100, message = "Device name cannot exceed 100 characters")
    private String deviceName;

    /**
     * Device type (mobile, desktop, tablet, etc.)
     */
    private String deviceType;

    /**
     * Operating system information
     */
    private String operatingSystem;

    /**
     * OS version
     */
    private String osVersion;

    /**
     * Browser information (if applicable)
     */
    private String browserInfo;

    /**
     * Browser version (if applicable)
     */
    private String browserVersion;

    /**
     * User agent string
     */
    private String userAgent;

    /**
     * IP address used during registration
     */
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "IP address must be a valid IPv4 or IPv6 address")
    private String ipAddress;

    /**
     * Geographic location during registration
     */
    private String geolocation;

    /**
     * Device fingerprint data
     */
    private Map<String, Object> fingerprintData;

    /**
     * Hardware specifications
     */
    private Map<String, Object> hardwareSpecs;

    /**
     * Security features available on device
     */
    private String[] securityFeatures;

    /**
     * Biometric capabilities
     */
    private String[] biometricCapabilities;

    /**
     * Encryption capabilities
     */
    private String[] encryptionCapabilities;

    /**
     * Device manufacturer
     */
    private String manufacturer;

    /**
     * Device model
     */
    private String model;

    /**
     * Device serial number (if available)
     */
    private String serialNumber;

    /**
     * MAC address (if available)
     */
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", 
             message = "MAC address must be in valid format")
    private String macAddress;

    /**
     * Registration timestamp
     */
    @Builder.Default
    private LocalDateTime registrationTime = LocalDateTime.now();

    /**
     * Registration source (web, mobile app, API, etc.)
     */
    private String registrationSource;

    /**
     * Whether device is corporate-managed
     */
    @Builder.Default
    private Boolean corporateManaged = false;

    /**
     * Whether device is jailbroken/rooted
     */
    @Builder.Default
    private Boolean jailbroken = false;

    /**
     * Whether device has screen lock enabled
     */
    @Builder.Default
    private Boolean screenLockEnabled = false;

    /**
     * Whether device has encryption enabled
     */
    @Builder.Default
    private Boolean encryptionEnabled = false;

    /**
     * Whether device has remote wipe capability
     */
    @Builder.Default
    private Boolean remoteWipeCapable = false;

    /**
     * Whether device has antivirus software
     */
    @Builder.Default
    private Boolean antivirusInstalled = false;

    /**
     * Whether device has VPN software
     */
    @Builder.Default
    private Boolean vpnInstalled = false;

    /**
     * Device compliance status
     */
    @Builder.Default
    private Boolean compliant = false;

    /**
     * Compliance policy version
     */
    private String compliancePolicyVersion;

    /**
     * Additional device metadata
     */
    private Map<String, Object> metadata;

    /**
     * User consent for device registration
     */
    @Builder.Default
    private Boolean userConsent = false;

    /**
     * Privacy policy acceptance
     */
    @Builder.Default
    private Boolean privacyPolicyAccepted = false;

    /**
     * Terms of service acceptance
     */
    @Builder.Default
    private Boolean termsAccepted = false;

    /**
     * Check if device is mobile
     */
    public boolean isMobileDevice() {
        return "mobile".equalsIgnoreCase(deviceType) || 
               "smartphone".equalsIgnoreCase(deviceType) ||
               "tablet".equalsIgnoreCase(deviceType);
    }

    /**
     * Check if device is desktop
     */
    public boolean isDesktopDevice() {
        return "desktop".equalsIgnoreCase(deviceType) || 
               "laptop".equalsIgnoreCase(deviceType) ||
               "workstation".equalsIgnoreCase(deviceType);
    }

    /**
     * Check if device is corporate managed
     */
    public boolean isCorporateManaged() {
        return Boolean.TRUE.equals(corporateManaged);
    }

    /**
     * Check if device is jailbroken/rooted
     */
    public boolean isJailbroken() {
        return Boolean.TRUE.equals(jailbroken);
    }

    /**
     * Check if device has basic security features
     */
    public boolean hasBasicSecurity() {
        return Boolean.TRUE.equals(screenLockEnabled) && 
               Boolean.TRUE.equals(encryptionEnabled);
    }

    /**
     * Check if device is compliant
     */
    public boolean isCompliant() {
        return Boolean.TRUE.equals(compliant);
    }

    /**
     * Check if user has given all required consents
     */
    public boolean hasAllConsents() {
        return Boolean.TRUE.equals(userConsent) &&
               Boolean.TRUE.equals(privacyPolicyAccepted) &&
               Boolean.TRUE.equals(termsAccepted);
    }

    /**
     * Check if device has biometric capabilities
     */
    public boolean hasBiometricCapabilities() {
        return biometricCapabilities != null && biometricCapabilities.length > 0;
    }

    /**
     * Check if device has security features
     */
    public boolean hasSecurityFeatures() {
        return securityFeatures != null && securityFeatures.length > 0;
    }

    /**
     * Get security feature count
     */
    public int getSecurityFeatureCount() {
        return securityFeatures != null ? securityFeatures.length : 0;
    }

    /**
     * Get biometric capability count
     */
    public int getBiometricCapabilityCount() {
        return biometricCapabilities != null ? biometricCapabilities.length : 0;
    }

    /**
     * Calculate device security score (0-100)
     */
    public int calculateSecurityScore() {
        int score = 0;
        
        // Basic security features
        if (Boolean.TRUE.equals(screenLockEnabled)) score += 20;
        if (Boolean.TRUE.equals(encryptionEnabled)) score += 25;
        if (Boolean.TRUE.equals(antivirusInstalled)) score += 15;
        if (Boolean.TRUE.equals(remoteWipeCapable)) score += 10;
        
        // Corporate management adds security
        if (isCorporateManaged()) score += 15;
        
        // Jailbreaking reduces security
        if (isJailbroken()) score -= 30;
        
        // Additional security features
        score += getSecurityFeatureCount() * 2;
        score += getBiometricCapabilityCount() * 3;
        
        // Ensure score is within valid range
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Get registration summary for logging
     */
    public String getRegistrationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Device registration request for ");
        summary.append(deviceName).append(" (").append(deviceType).append(")");
        summary.append(" by user ").append(userId);
        summary.append(" from IP ").append(ipAddress);
        summary.append(" - Security Score: ").append(calculateSecurityScore());
        
        if (isJailbroken()) {
            summary.append(" [JAILBROKEN]");
        }
        
        if (isCorporateManaged()) {
            summary.append(" [CORPORATE]");
        }
        
        return summary.toString();
    }

    /**
     * Validate request completeness and consistency
     */
    public boolean isValid() {
        return deviceId != null && !deviceId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               deviceName != null && !deviceName.trim().isEmpty() &&
               deviceType != null && !deviceType.trim().isEmpty() &&
               operatingSystem != null && !operatingSystem.trim().isEmpty() &&
               ipAddress != null && !ipAddress.trim().isEmpty() &&
               registrationTime != null &&
               hasAllConsents();
    }

    /**
     * Create a basic device registration request
     */
    public static DeviceRegistrationRequest basic(String deviceId, String userId, String deviceName, 
                                                String deviceType, String operatingSystem, String ipAddress) {
        return DeviceRegistrationRequest.builder()
            .deviceId(deviceId)
            .userId(userId)
            .deviceName(deviceName)
            .deviceType(deviceType)
            .operatingSystem(operatingSystem)
            .ipAddress(ipAddress)
            .registrationTime(LocalDateTime.now())
            .userConsent(true)
            .privacyPolicyAccepted(true)
            .termsAccepted(true)
            .build();
    }
}
