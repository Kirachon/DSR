package ph.gov.dsr.security.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User MFA Settings entity for storing user-specific MFA configuration
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Entity
@Table(name = "user_mfa_settings", schema = "dsr_auth", indexes = {
    @Index(name = "idx_user_mfa_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_user_mfa_enabled", columnList = "mfa_enabled")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class UserMFASettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_method", length = 20)
    private MFAMethod primaryMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_method", length = 20)
    private MFAMethod backupMethod;

    // SMS-specific settings
    @Column(name = "sms_number", length = 20)
    private String smsNumber;

    @Column(name = "sms_verified", nullable = false)
    private Boolean smsVerified = false;

    // Email-specific settings
    @Column(name = "email_address", length = 255)
    private String emailAddress;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    // TOTP-specific settings
    @Column(name = "totp_secret", length = 32)
    private String totpSecret;

    @Column(name = "totp_verified", nullable = false)
    private Boolean totpVerified = false;

    @Column(name = "totp_backup_codes", columnDefinition = "TEXT")
    private String totpBackupCodes; // JSON array of backup codes

    // Hardware token settings
    @Column(name = "hardware_token_id", length = 100)
    private String hardwareTokenId;

    @Column(name = "hardware_token_verified", nullable = false)
    private Boolean hardwareTokenVerified = false;

    // Security settings
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_successful_auth")
    private LocalDateTime lastSuccessfulAuth;

    @Column(name = "last_failed_auth")
    private LocalDateTime lastFailedAuth;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "setup_at")
    private LocalDateTime setupAt;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    // Constructors
    public UserMFASettings() {}

    public UserMFASettings(String userId) {
        this.userId = userId;
    }

    // Helper methods
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
        this.lastFailedAuth = LocalDateTime.now();
    }

    public void clearFailedAttempts() {
        this.failedAttempts = 0;
        this.lastSuccessfulAuth = LocalDateTime.now();
        this.lockedUntil = null;
    }

    public void lockAccount(int lockoutMinutes) {
        this.lockedUntil = LocalDateTime.now().plusMinutes(lockoutMinutes);
    }

    public boolean isMethodVerified(MFAMethod method) {
        return switch (method) {
            case SMS -> smsVerified;
            case EMAIL -> emailVerified;
            case TOTP -> totpVerified;
            case HARDWARE_KEY -> hardwareTokenVerified;
            default -> false;
        };
    }

    public void setMethodVerified(MFAMethod method, boolean verified) {
        switch (method) {
            case SMS -> this.smsVerified = verified;
            case EMAIL -> this.emailVerified = verified;
            case TOTP -> this.totpVerified = verified;
            case HARDWARE_KEY -> this.hardwareTokenVerified = verified;
        }
    }
}
