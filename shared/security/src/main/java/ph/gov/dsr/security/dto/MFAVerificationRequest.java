package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.MFAMethod;

/**
 * DTO for MFA verification request
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAVerificationRequest {

    private String userId;

    @Pattern(regexp = "^[0-9A-Za-z]{4,10}$", message = "Invalid token format")
    private String token;

    private String challengeId;
    
    private MFAMethod method;
    
    private String sessionId;
    
    private String deviceId;
    
    private String ipAddress;
    
    private String userAgent;

    // Backup code verification
    private boolean isBackupCode = false;
    
    private String backupCodeId;

    // Hardware token specific
    private String hardwareTokenSignature;
    
    private String hardwareTokenChallenge;

    // TOTP specific
    private long totpTimestamp;
    
    private int totpWindow = 1; // Allow 1 time window tolerance

    // Security context
    private String transactionId;
    
    private String operationType;
    
    private boolean trustDevice = false;
    
    private int trustDeviceDays = 30;

    // Verification options
    private boolean consumeToken = true;
    
    private boolean allowExpiredToken = false;
    
    private boolean skipRateLimiting = false;

    // Additional context
    private String verificationContext;
    
    private String clientFingerprint;
    
    private String geolocation;

    // Helper methods
    public boolean isNumericToken() {
        return token != null && token.matches("^[0-9]+$");
    }

    public boolean isAlphanumericToken() {
        return token != null && token.matches("^[0-9A-Za-z]+$");
    }

    public boolean hasHardwareTokenData() {
        return hardwareTokenSignature != null && hardwareTokenChallenge != null;
    }

    public boolean hasTOTPTimestamp() {
        return totpTimestamp > 0;
    }

    public boolean hasTransactionContext() {
        return transactionId != null && !transactionId.trim().isEmpty();
    }

    public boolean shouldTrustDevice() {
        return trustDevice && trustDeviceDays > 0;
    }

    public boolean hasSecurityContext() {
        return clientFingerprint != null || geolocation != null || verificationContext != null;
    }

    public int getTokenAsInt() {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public long getTokenAsLong() {
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}
