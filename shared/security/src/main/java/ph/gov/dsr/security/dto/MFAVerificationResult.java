package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.MFAMethod;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for MFA verification result
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAVerificationResult {

    private boolean verified;
    
    private boolean success;
    
    private String message;
    
    private MFAMethod method;
    
    private LocalDateTime verifiedAt;

    // Token information
    private String tokenId;
    
    private boolean tokenConsumed;
    
    private boolean tokenExpired;
    
    private int attemptsUsed;
    
    private int remainingAttempts;

    // User account status
    private boolean accountLocked;
    
    private LocalDateTime lockedUntil;
    
    private int failedAttempts;
    
    private int maxFailedAttempts;

    // Device trust
    private boolean deviceTrusted;
    
    private String deviceTrustToken;
    
    private LocalDateTime deviceTrustExpiresAt;

    // Security context
    private String sessionId;
    
    private String verificationId;
    
    private String riskScore;
    
    private List<String> securityFlags;

    // Error information
    private String errorCode;
    
    private String errorMessage;
    
    private String failureReason;
    
    private List<String> validationErrors;

    // Next steps
    private boolean requiresAdditionalVerification;
    
    private List<MFAMethod> additionalMethodsRequired;
    
    private String nextStepDescription;
    
    private String nextStepUrl;

    // Backup codes
    private boolean backupCodeUsed;
    
    private int remainingBackupCodes;
    
    private boolean shouldGenerateNewBackupCodes;

    // Rate limiting
    private boolean rateLimited;
    
    private LocalDateTime rateLimitResetAt;

    // Audit information
    private String auditTrailId;
    
    private LocalDateTime auditTimestamp;

    // Helper methods
    public boolean isSuccessful() {
        return success && verified;
    }

    public boolean hasErrors() {
        return !success || errorCode != null || (validationErrors != null && !validationErrors.isEmpty());
    }

    public boolean canRetry() {
        return !accountLocked && !rateLimited && remainingAttempts > 0;
    }

    public boolean needsAccountUnlock() {
        return accountLocked && lockedUntil != null;
    }

    public boolean isAccountPermanentlyLocked() {
        return accountLocked && lockedUntil == null;
    }

    public boolean hasSecurityConcerns() {
        return securityFlags != null && !securityFlags.isEmpty();
    }

    public boolean needsBackupCodeRefresh() {
        return shouldGenerateNewBackupCodes || (backupCodeUsed && remainingBackupCodes <= 2);
    }

    public long getSecondsUntilUnlock() {
        if (lockedUntil == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), lockedUntil).getSeconds();
    }

    public long getSecondsUntilRateLimitReset() {
        if (rateLimitResetAt == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), rateLimitResetAt).getSeconds();
    }

    // Static factory methods
    public static MFAVerificationResult success(MFAMethod method, String message) {
        return MFAVerificationResult.builder()
                .verified(true)
                .success(true)
                .method(method)
                .message(message)
                .verifiedAt(LocalDateTime.now())
                .auditTimestamp(LocalDateTime.now())
                .build();
    }

    public static MFAVerificationResult failure(String errorCode, String errorMessage, String failureReason) {
        return MFAVerificationResult.builder()
                .verified(false)
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .failureReason(failureReason)
                .auditTimestamp(LocalDateTime.now())
                .build();
    }

    public static MFAVerificationResult accountLocked(LocalDateTime lockedUntil, int failedAttempts) {
        return MFAVerificationResult.builder()
                .verified(false)
                .success(false)
                .accountLocked(true)
                .lockedUntil(lockedUntil)
                .failedAttempts(failedAttempts)
                .errorCode("ACCOUNT_LOCKED")
                .errorMessage("Account is locked due to too many failed attempts")
                .auditTimestamp(LocalDateTime.now())
                .build();
    }

    public static MFAVerificationResult rateLimited(LocalDateTime resetAt) {
        return MFAVerificationResult.builder()
                .verified(false)
                .success(false)
                .rateLimited(true)
                .rateLimitResetAt(resetAt)
                .errorCode("RATE_LIMITED")
                .errorMessage("Too many verification attempts. Please try again later.")
                .auditTimestamp(LocalDateTime.now())
                .build();
    }
}
