package ph.gov.dsr.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.security.dto.*;
import ph.gov.dsr.security.entity.MFAToken;
import ph.gov.dsr.security.entity.UserMFASettings;
import ph.gov.dsr.security.entity.MFAMethod;
import ph.gov.dsr.security.repository.MFATokenRepository;
import ph.gov.dsr.security.repository.UserMFASettingsRepository;
import ph.gov.dsr.security.integration.SMSProvider;
import ph.gov.dsr.security.integration.EmailProvider;
import ph.gov.dsr.security.integration.TOTPProvider;
import ph.gov.dsr.security.exception.MFAException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Multi-Factor Authentication service supporting SMS, Email, TOTP, and Hardware tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiFactorAuthenticationService {

    private final MFATokenRepository mfaTokenRepository;
    private final UserMFASettingsRepository userMFASettingsRepository;
    private final SMSProvider smsProvider;
    private final EmailProvider emailProvider;
    private final TOTPProvider totpProvider;
    // private final AdvancedAuditService auditService;

    @Value("${dsr.security.mfa.token-length:6}")
    private int tokenLength;

    @Value("${dsr.security.mfa.token-expiry-minutes:5}")
    private int tokenExpiryMinutes;

    @Value("${dsr.security.mfa.max-attempts:3}")
    private int maxAttempts;

    @Value("${dsr.security.mfa.lockout-minutes:15}")
    private int lockoutMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Setup MFA for a user
     */
    @Transactional
    public CompletableFuture<MFASetupResult> setupMFA(MFASetupRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Setting up MFA for user: {} with method: {}", 
                        request.getUserId(), request.getMfaMethod());
                
                // Get or create user MFA settings
                UserMFASettings settings = userMFASettingsRepository.findByUserId(request.getUserId())
                    .orElse(new UserMFASettings());
                
                settings.setUserId(request.getUserId());
                settings.setMfaEnabled(true);
                settings.setPrimaryMethod(request.getMfaMethod());
                settings.setSetupAt(LocalDateTime.now());
                
                MFASetupResult result;
                
                switch (request.getMfaMethod()) {
                    case SMS:
                        result = setupSMSMFA(settings, request);
                        break;
                    case EMAIL:
                        result = setupEmailMFA(settings, request);
                        break;
                    case TOTP:
                        result = setupTOTPMFA(settings, request);
                        break;
                    // case HARDWARE_KEY:
                    //     result = setupHardwareTokenMFA(settings, request);
                    //     break;
                    default:
                        throw new MFAException("Unsupported MFA method: " + request.getMfaMethod());
                }
                
                // Save settings
                userMFASettingsRepository.save(settings);
                
                // Audit MFA setup
                // auditService.logMFASetup(UUID.fromString(request.getUserId()), request.getMfaMethod(), true);
                
                log.info("MFA setup completed for user: {}", request.getUserId());
                return result;
                
            } catch (Exception e) {
                log.error("MFA setup failed for user: {}", request.getUserId(), e);
                // auditService.logMFASetup(UUID.fromString(request.getUserId()), request.getMfaMethod(), false);
                throw new MFAException("MFA setup failed", e);
            }
        });
    }

    /**
     * Initiate MFA challenge
     */
    @Transactional
    public CompletableFuture<MFAChallengeResult> initiateMFAChallenge(MFAChallengeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Initiating MFA challenge for user: {}", request.getUserId());
                
                // Get user MFA settings
                UserMFASettings settings = userMFASettingsRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new MFAException("MFA not configured for user"));
                
                if (!settings.getMfaEnabled()) {
                    throw new MFAException("MFA is disabled for user");
                }
                
                // Check if user is locked out
                if (isUserLockedOut(request.getUserId())) {
                    throw new MFAException("User is locked out due to too many failed attempts");
                }
                
                // Generate and send MFA token
                MFAChallengeResult result = generateAndSendMFAToken(settings, request);
                
                // Audit MFA challenge initiation
                // auditService.logMFAChallenge(UUID.fromString(request.getUserId()), settings.getPrimaryMethod(), true);
                
                log.info("MFA challenge initiated for user: {}", request.getUserId());
                return result;
                
            } catch (Exception e) {
                log.error("MFA challenge initiation failed for user: {}", request.getUserId(), e);
                // auditService.logMFAChallenge(UUID.fromString(request.getUserId()), null, false);
                throw new MFAException("MFA challenge initiation failed", e);
            }
        });
    }

    /**
     * Verify MFA token
     */
    @Transactional
    public CompletableFuture<MFAVerificationResult> verifyMFAToken(MFAVerificationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Verifying MFA token for user: {}", request.getUserId());
                
                // Check if user is locked out
                if (isUserLockedOut(request.getUserId())) {
                    throw new MFAException("User is locked out due to too many failed attempts");
                }
                
                // Get user MFA settings
                UserMFASettings settings = userMFASettingsRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new MFAException("MFA not configured for user"));
                
                boolean isValid = false;
                
                switch (settings.getPrimaryMethod()) {
                    case SMS:
                    case EMAIL:
                        isValid = verifyTokenBasedMFA(request);
                        break;
                    case TOTP:
                        isValid = verifyTOTPMFA(settings, request);
                        break;
                    // case HARDWARE_TOKEN:
                    //     isValid = verifyHardwareTokenMFA(settings, request);
                    //     break;
                    default:
                        log.warn("Unsupported MFA method: {}", settings.getPrimaryMethod());
                        isValid = false;
                        break;
                }
                
                if (isValid) {
                    // Clear failed attempts
                    clearFailedAttempts(request.getUserId());
                    
                    // Update last successful verification
                    // settings.setLastVerifiedAt(LocalDateTime.now());
                    userMFASettingsRepository.save(settings);

                    // Audit successful verification
                    // auditService.logMFAVerification(UUID.fromString(request.getUserId()), settings.getPrimaryMethod(), true);
                    
                    log.info("MFA verification successful for user: {}", request.getUserId());
                    return MFAVerificationResult.builder()
                        .verified(true)
                        .success(true)
                        .method(settings.getPrimaryMethod())
                        .message("MFA verification successful")
                        .verifiedAt(LocalDateTime.now())
                        .build();
                } else {
                    // Increment failed attempts
                    incrementFailedAttempts(request.getUserId());
                    
                    // Audit failed verification
                    // auditService.logMFAVerification(UUID.fromString(request.getUserId()), settings.getPrimaryMethod(), false);
                    
                    log.warn("MFA verification failed for user: {}", request.getUserId());
                    return MFAVerificationResult.builder()
                        .verified(false)
                        .success(false)
                        .method(settings.getPrimaryMethod())
                        .errorMessage("Invalid MFA token")
                        .failureReason("INVALID_TOKEN")
                        .build();
                }
                
            } catch (Exception e) {
                log.error("MFA verification failed for user: {}", request.getUserId(), e);
                // auditService.logMFAVerification(UUID.fromString(request.getUserId()), null, false);
                throw new MFAException("MFA verification failed", e);
            }
        });
    }

    /**
     * Disable MFA for a user
     */
    @Transactional
    public CompletableFuture<Void> disableMFA(String userId, String adminUserId) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Disabling MFA for user: {} by admin: {}", userId, adminUserId);
                
                UserMFASettings settings = userMFASettingsRepository.findByUserId(userId)
                    .orElseThrow(() -> new MFAException("MFA settings not found for user"));
                
                settings.setMfaEnabled(false);
                settings.setDisabledAt(LocalDateTime.now());
                
                userMFASettingsRepository.save(settings);
                
                // Clear any existing tokens
                mfaTokenRepository.deleteByUserId(userId);
                
                // Audit MFA disable
                // auditService.logMFADisable(UUID.fromString(userId), UUID.fromString(adminUserId));
                
                log.info("MFA disabled for user: {}", userId);
                
            } catch (Exception e) {
                log.error("Failed to disable MFA for user: {}", userId, e);
                throw new MFAException("MFA disable failed", e);
            }
        });
    }

    /**
     * Get MFA status for a user
     */
    public MFAStatus getMFAStatus(String userId) {
        try {
            Optional<UserMFASettings> settings = userMFASettingsRepository.findByUserId(userId);
            
            if (settings.isEmpty()) {
                return MFAStatus.builder()
                    .userId(userId)
                    .mfaEnabled(false)
                    .build();
            }
            
            UserMFASettings userSettings = settings.get();
            
            return MFAStatus.builder()
                .userId(userId)
                .mfaEnabled(userSettings.getMfaEnabled())
                .primaryMethod(userSettings.getPrimaryMethod())
                .backupMethod(userSettings.getBackupMethod())
                .setupAt(userSettings.getSetupAt())
                .lastVerifiedAt(userSettings.getLastSuccessfulAuth())
                .accountLocked(isUserLockedOut(userId))
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get MFA status for user: {}", userId, e);
            throw new MFAException("Failed to get MFA status", e);
        }
    }

    private MFASetupResult setupSMSMFA(UserMFASettings settings, MFASetupRequest request) {
        settings.setSmsNumber(request.getPhoneNumber());
        
        // Send verification SMS
        String verificationCode = generateNumericToken();
        smsProvider.sendVerificationSMS(request.getPhoneNumber(), verificationCode);
        
        // Store verification token
        storeMFAToken(request.getUserId(), verificationCode, MFAMethod.SMS);
        
        return MFASetupResult.builder()
            .success(true)
            .method(MFAMethod.SMS)
            .message("Verification code sent to " + maskPhoneNumber(request.getPhoneNumber()))
            .requiresVerification(true)
            .build();
    }

    private MFASetupResult setupEmailMFA(UserMFASettings settings, MFASetupRequest request) {
        settings.setEmailAddress(request.getEmailAddress());
        
        // Send verification email
        String verificationCode = generateNumericToken();
        emailProvider.sendVerificationEmail(request.getEmailAddress(), verificationCode);
        
        // Store verification token
        storeMFAToken(request.getUserId(), verificationCode, MFAMethod.EMAIL);
        
        return MFASetupResult.builder()
            .success(true)
            .method(MFAMethod.EMAIL)
            .message("Verification code sent to " + maskEmail(request.getEmailAddress()))
            .requiresVerification(true)
            .build();
    }

    private MFASetupResult setupTOTPMFA(UserMFASettings settings, MFASetupRequest request) {
        // Generate TOTP secret
        String totpSecret = totpProvider.generateSecret();
        settings.setTotpSecret(totpSecret);
        
        // Generate QR code for authenticator app
        // String qrCodeUrl = totpProvider.generateQRCodeUrl(totpSecret, request.getUserId(), "DSR System");
        
        return MFASetupResult.builder()
            .success(true)
            .method(MFAMethod.TOTP)
            // .qrCodeUrl(qrCodeUrl)
            .backupCodes(totpProvider.generateBackupCodes(10))
            .requiresVerification(true)
            .build();
    }

    // private MFASetupResult setupHardwareTokenMFA(UserMFASettings settings, MFASetupRequest request) {
    //     settings.setHardwareTokenSerial(request.getHardwareTokenSerial());
    //
    //     return MFASetupResult.builder()
    //         .success(true)
    //         .method(MFAMethod.HARDWARE_TOKEN)
    //         .message("Hardware token registered successfully")
    //         .requiresVerification(false)
    //         .build();
    // }

    private MFAChallengeResult generateAndSendMFAToken(UserMFASettings settings, MFAChallengeRequest request) {
        String token = generateNumericToken();
        
        switch (settings.getPrimaryMethod()) {
            case SMS:
                smsProvider.sendMFAToken(settings.getSmsNumber(), token);
                storeMFAToken(request.getUserId(), token, MFAMethod.SMS);
                return MFAChallengeResult.builder()
                    .challengeId(generateChallengeId())
                    .method(MFAMethod.SMS)
                    .message("Code sent to " + maskPhoneNumber(settings.getSmsNumber()))
                    .expiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                    .build();
                    
            case EMAIL:
                emailProvider.sendMFAToken(settings.getEmailAddress(), token);
                storeMFAToken(request.getUserId(), token, MFAMethod.EMAIL);
                return MFAChallengeResult.builder()
                    .challengeId(generateChallengeId())
                    .method(MFAMethod.EMAIL)
                    .message("Code sent to " + maskEmail(settings.getEmailAddress()))
                    .expiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                    .build();
                    
            case TOTP:
                return MFAChallengeResult.builder()
                    .challengeId(generateChallengeId())
                    .method(MFAMethod.TOTP)
                    .message("Enter code from your authenticator app")
                    .build();
                    
            // case HARDWARE_TOKEN:
            //     return MFAChallengeResult.builder()
            //         .challengeId(generateChallengeId())
            //         .method(MFAMethod.HARDWARE_TOKEN)
            //         .message("Enter code from your hardware token")
            //         .build();
                    
            default:
                throw new MFAException("Unsupported MFA method");
        }
    }

    private boolean verifyTokenBasedMFA(MFAVerificationRequest request) {
        Optional<MFAToken> tokenOpt = mfaTokenRepository.findByUserIdAndToken(
            request.getUserId(), request.getToken());
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        MFAToken token = tokenOpt.get();
        
        // Check if token is expired
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            mfaTokenRepository.delete(token);
            return false;
        }
        
        // Token is valid, delete it
        mfaTokenRepository.delete(token);
        return true;
    }

    private boolean verifyTOTPMFA(UserMFASettings settings, MFAVerificationRequest request) {
        return totpProvider.verifyToken(settings.getTotpSecret(), request.getToken());
    }

    // private boolean verifyHardwareTokenMFA(UserMFASettings settings, MFAVerificationRequest request) {
    //     // Implementation would integrate with hardware token provider
    //     return true; // Simplified for now
    // }

    private String generateNumericToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < tokenLength; i++) {
            token.append(secureRandom.nextInt(10));
        }
        return token.toString();
    }

    private String generateChallengeId() {
        return "MFA_" + System.currentTimeMillis() + "_" + secureRandom.nextInt(1000);
    }

    private void storeMFAToken(String userId, String token, MFAMethod method) {
        MFAToken mfaToken = new MFAToken();
        mfaToken.setUserId(userId);
        mfaToken.setToken(token);
        mfaToken.setMethod(method);
        mfaToken.setCreatedAt(LocalDateTime.now());
        mfaToken.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes));
        
        mfaTokenRepository.save(mfaToken);
    }

    private boolean isUserLockedOut(String userId) {
        Optional<UserMFASettings> settings = userMFASettingsRepository.findByUserId(userId);
        if (settings.isEmpty()) {
            return false;
        }
        return settings.get().isLocked();
    }

    private void incrementFailedAttempts(String userId) {
        Optional<UserMFASettings> settings = userMFASettingsRepository.findByUserId(userId);
        if (settings.isPresent()) {
            UserMFASettings userSettings = settings.get();
            userSettings.incrementFailedAttempts();

            // Lock account if max attempts exceeded
            if (userSettings.getFailedAttempts() >= maxAttempts) {
                userSettings.lockAccount(lockoutMinutes);
            }

            userMFASettingsRepository.save(userSettings);
        }
    }

    private void clearFailedAttempts(String userId) {
        Optional<UserMFASettings> settings = userMFASettingsRepository.findByUserId(userId);
        if (settings.isPresent()) {
            UserMFASettings userSettings = settings.get();
            userSettings.clearFailedAttempts();
            userMFASettingsRepository.save(userSettings);
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() > 4) {
            return phoneNumber.substring(0, 4) + "****" + 
                   phoneNumber.substring(phoneNumber.length() - 2);
        }
        return "****";
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex > 2) {
            return email.substring(0, 2) + "****" + email.substring(atIndex);
        }
        return "****@" + email.substring(atIndex + 1);
    }

}
