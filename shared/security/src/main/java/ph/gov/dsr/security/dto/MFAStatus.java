package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.MFAMethod;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for MFA status information
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAStatus {

    private String userId;
    
    private boolean mfaEnabled;
    
    private MFAMethod primaryMethod;
    
    private MFAMethod backupMethod;
    
    private LocalDateTime setupAt;
    
    private LocalDateTime lastVerifiedAt;

    // Method-specific status
    private boolean smsVerified;
    
    private String maskedSmsNumber;
    
    private boolean emailVerified;
    
    private String maskedEmailAddress;
    
    private boolean totpVerified;
    
    private boolean hardwareTokenVerified;
    
    private String hardwareTokenId;

    // Security status
    private boolean accountLocked;
    
    private LocalDateTime lockedUntil;
    
    private int failedAttempts;
    
    private int maxFailedAttempts;

    // Available methods
    private List<MFAMethod> availableMethods;
    
    private List<MFAMethod> configuredMethods;
    
    private List<MFAMethod> verifiedMethods;

    // Backup codes
    private boolean hasBackupCodes;
    
    private int remainingBackupCodes;

    // Helper methods
    public boolean isLocked() {
        return accountLocked && (lockedUntil == null || LocalDateTime.now().isBefore(lockedUntil));
    }

    public boolean canSetupMFA() {
        return !mfaEnabled;
    }

    public boolean canUseMFA() {
        return mfaEnabled && !isLocked() && !configuredMethods.isEmpty();
    }

    public boolean hasVerifiedMethod() {
        return verifiedMethods != null && !verifiedMethods.isEmpty();
    }

    public boolean needsBackupCodes() {
        return mfaEnabled && totpVerified && (!hasBackupCodes || remainingBackupCodes <= 2);
    }

    public long getSecondsUntilUnlock() {
        if (lockedUntil == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), lockedUntil).getSeconds();
    }

    // Static factory methods
    public static MFAStatus disabled(String userId) {
        return MFAStatus.builder()
                .userId(userId)
                .mfaEnabled(false)
                .failedAttempts(0)
                .maxFailedAttempts(3)
                .build();
    }

    public static MFAStatus enabled(String userId, MFAMethod primaryMethod) {
        return MFAStatus.builder()
                .userId(userId)
                .mfaEnabled(true)
                .primaryMethod(primaryMethod)
                .setupAt(LocalDateTime.now())
                .failedAttempts(0)
                .maxFailedAttempts(3)
                .build();
    }
}
