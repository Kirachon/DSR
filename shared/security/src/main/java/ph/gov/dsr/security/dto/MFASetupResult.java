package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.MFAMethod;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for MFA setup result
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFASetupResult {

    private boolean success;
    
    private String message;
    
    private MFAMethod method;
    
    private boolean requiresVerification;
    
    private String verificationToken;
    
    private LocalDateTime expiresAt;

    // TOTP-specific fields
    private String totpSecret;
    
    private String totpQrCodeUrl;
    
    private String totpManualEntryKey;

    // Backup codes for TOTP
    private List<String> backupCodes;

    // Hardware token specific fields
    private String hardwareTokenId;
    
    private String hardwareTokenRegistrationData;

    // SMS/Email specific fields
    private String maskedPhoneNumber;
    
    private String maskedEmailAddress;

    // Error information
    private String errorCode;
    
    private String errorMessage;
    
    private List<String> validationErrors;

    // Additional context
    private String challengeId;
    
    private String sessionId;
    
    private LocalDateTime setupAt;

    // Next steps information
    private String nextStepDescription;
    
    private String nextStepUrl;
    
    private boolean canProceedWithoutVerification;

    // Helper methods
    public boolean isSuccessful() {
        return success;
    }

    public boolean hasErrors() {
        return !success || errorCode != null || (validationErrors != null && !validationErrors.isEmpty());
    }

    public boolean needsUserAction() {
        return requiresVerification || nextStepDescription != null;
    }

    public boolean isTOTPSetup() {
        return method == MFAMethod.TOTP && totpSecret != null;
    }

    public boolean hasBackupCodes() {
        return backupCodes != null && !backupCodes.isEmpty();
    }

    // Static factory methods for common results
    public static MFASetupResult success(MFAMethod method, String message) {
        return MFASetupResult.builder()
                .success(true)
                .method(method)
                .message(message)
                .setupAt(LocalDateTime.now())
                .build();
    }

    public static MFASetupResult successWithVerification(MFAMethod method, String message, String verificationToken, LocalDateTime expiresAt) {
        return MFASetupResult.builder()
                .success(true)
                .method(method)
                .message(message)
                .requiresVerification(true)
                .verificationToken(verificationToken)
                .expiresAt(expiresAt)
                .setupAt(LocalDateTime.now())
                .build();
    }

    public static MFASetupResult failure(String errorCode, String errorMessage) {
        return MFASetupResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    public static MFASetupResult validationFailure(List<String> validationErrors) {
        return MFASetupResult.builder()
                .success(false)
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Validation failed")
                .validationErrors(validationErrors)
                .build();
    }
}
