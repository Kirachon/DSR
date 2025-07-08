package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.MFAMethod;

/**
 * DTO for MFA setup request
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFASetupRequest {

    private String userId;

    private MFAMethod mfaMethod;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String emailAddress;

    private String deviceId;
    
    private String deviceName;
    
    private String ipAddress;
    
    private String userAgent;

    // TOTP-specific fields
    private String totpSecret;
    
    private String totpQrCode;

    // Hardware token specific fields
    private String hardwareTokenId;
    
    private String hardwareTokenType;

    // Backup method
    private MFAMethod backupMethod;

    // Additional security context
    private String sessionId;
    
    private String challengeId;

    // Validation flags
    private boolean skipVerification = false;
    
    private boolean replaceExisting = false;

    // Helper methods
    public boolean requiresPhoneNumber() {
        return mfaMethod == MFAMethod.SMS;
    }

    public boolean requiresEmailAddress() {
        return mfaMethod == MFAMethod.EMAIL;
    }

    public boolean requiresHardwareToken() {
        return mfaMethod == MFAMethod.HARDWARE_KEY;
    }

    public boolean isTOTPMethod() {
        return mfaMethod == MFAMethod.TOTP;
    }

    public void validateRequiredFields() {
        switch (mfaMethod) {
            case SMS -> {
                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    throw new IllegalArgumentException("Phone number is required for SMS MFA");
                }
            }
            case EMAIL -> {
                if (emailAddress == null || emailAddress.trim().isEmpty()) {
                    throw new IllegalArgumentException("Email address is required for Email MFA");
                }
            }
            case HARDWARE_KEY -> {
                if (hardwareTokenId == null || hardwareTokenId.trim().isEmpty()) {
                    throw new IllegalArgumentException("Hardware token ID is required for Hardware Key MFA");
                }
            }
            case TOTP -> {
                // TOTP secret will be generated if not provided
            }
        }
    }
}
