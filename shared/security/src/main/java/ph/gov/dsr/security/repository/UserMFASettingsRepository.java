package ph.gov.dsr.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.security.entity.MFAMethod;
import ph.gov.dsr.security.entity.UserMFASettings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserMFASettings entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Repository
public interface UserMFASettingsRepository extends JpaRepository<UserMFASettings, UUID> {

    /**
     * Find MFA settings by user ID
     */
    Optional<UserMFASettings> findByUserId(String userId);

    /**
     * Find all users with MFA enabled
     */
    List<UserMFASettings> findByMfaEnabledTrue();

    /**
     * Find all users with MFA disabled
     */
    List<UserMFASettings> findByMfaEnabledFalse();

    /**
     * Find users by primary MFA method
     */
    List<UserMFASettings> findByPrimaryMethod(MFAMethod method);

    /**
     * Find users with specific backup method
     */
    List<UserMFASettings> findByBackupMethod(MFAMethod method);

    /**
     * Find users with SMS number
     */
    List<UserMFASettings> findBySmsNumberIsNotNull();

    /**
     * Find users with verified SMS
     */
    List<UserMFASettings> findBySmsVerifiedTrue();

    /**
     * Find users with email address configured
     */
    List<UserMFASettings> findByEmailAddressIsNotNull();

    /**
     * Find users with verified email
     */
    List<UserMFASettings> findByEmailVerifiedTrue();

    /**
     * Find users with TOTP configured
     */
    List<UserMFASettings> findByTotpSecretIsNotNull();

    /**
     * Find users with verified TOTP
     */
    List<UserMFASettings> findByTotpVerifiedTrue();

    /**
     * Find users with hardware token configured
     */
    List<UserMFASettings> findByHardwareTokenIdIsNotNull();

    /**
     * Find users with verified hardware token
     */
    List<UserMFASettings> findByHardwareTokenVerifiedTrue();

    /**
     * Find locked users
     */
    @Query("SELECT s FROM UserMFASettings s WHERE s.lockedUntil IS NOT NULL AND s.lockedUntil > :now")
    List<UserMFASettings> findLockedUsers(@Param("now") LocalDateTime now);

    /**
     * Find users with failed attempts above threshold
     */
    @Query("SELECT s FROM UserMFASettings s WHERE s.failedAttempts >= :threshold")
    List<UserMFASettings> findUsersWithFailedAttempts(@Param("threshold") int threshold);

    /**
     * Find users who haven't authenticated recently
     */
    @Query("SELECT s FROM UserMFASettings s WHERE s.mfaEnabled = true " +
           "AND (s.lastSuccessfulAuth IS NULL OR s.lastSuccessfulAuth < :since)")
    List<UserMFASettings> findUsersNotAuthenticatedSince(@Param("since") LocalDateTime since);

    /**
     * Count users with MFA enabled
     */
    long countByMfaEnabledTrue();

    /**
     * Count users by primary method
     */
    long countByPrimaryMethod(MFAMethod method);

    /**
     * Check if user exists with MFA enabled
     */
    boolean existsByUserIdAndMfaEnabledTrue(String userId);

    /**
     * Check if SMS number is already used by another user
     */
    @Query("SELECT COUNT(s) > 0 FROM UserMFASettings s WHERE s.smsNumber = :smsNumber AND s.userId != :userId")
    boolean existsBySmsNumberAndUserIdNot(@Param("smsNumber") String smsNumber, @Param("userId") String userId);

    /**
     * Check if email address is already used by another user
     */
    @Query("SELECT COUNT(s) > 0 FROM UserMFASettings s WHERE s.emailAddress = :emailAddress AND s.userId != :userId")
    boolean existsByEmailAddressAndUserIdNot(@Param("emailAddress") String emailAddress, @Param("userId") String userId);

    /**
     * Update failed attempts
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserMFASettings s SET s.failedAttempts = :attempts, s.lastFailedAuth = :now WHERE s.userId = :userId")
    int updateFailedAttempts(@Param("userId") String userId, @Param("attempts") int attempts, @Param("now") LocalDateTime now);

    /**
     * Clear failed attempts
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserMFASettings s SET s.failedAttempts = 0, s.lastSuccessfulAuth = :now, s.lockedUntil = NULL WHERE s.userId = :userId")
    int clearFailedAttempts(@Param("userId") String userId, @Param("now") LocalDateTime now);

    /**
     * Lock user account
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserMFASettings s SET s.lockedUntil = :lockedUntil WHERE s.userId = :userId")
    int lockUser(@Param("userId") String userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    /**
     * Unlock user account
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserMFASettings s SET s.lockedUntil = NULL WHERE s.userId = :userId")
    int unlockUser(@Param("userId") String userId);

    /**
     * Enable MFA for user
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserMFASettings s SET s.mfaEnabled = true, s.setupAt = :setupAt WHERE s.userId = :userId")
    int enableMFA(@Param("userId") String userId, @Param("setupAt") LocalDateTime setupAt);

    /**
     * Disable MFA for user
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserMFASettings s SET s.mfaEnabled = false, s.disabledAt = :disabledAt WHERE s.userId = :userId")
    int disableMFA(@Param("userId") String userId, @Param("disabledAt") LocalDateTime disabledAt);

    /**
     * Delete MFA settings by user ID
     */
    @Modifying
    @Transactional
    void deleteByUserId(String userId);
}
