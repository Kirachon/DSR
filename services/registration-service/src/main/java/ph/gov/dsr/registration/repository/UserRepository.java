package ph.gov.dsr.registration.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.registration.entity.User;
import ph.gov.dsr.registration.entity.UserRole;
import ph.gov.dsr.registration.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by email address (case insensitive)
     */
    @Query(value = "SELECT * FROM dsr_auth.users WHERE LOWER(email) = LOWER(:email)", nativeQuery = true)
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists (case insensitive)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by role and status
     */
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    /**
     * Find active users by role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);

    /**
     * Find users by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Find users created within date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find users who haven't logged in for specified days
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find locked users
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedUsers();

    /**
     * Find users with failed login attempts
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :threshold")
    List<User> findUsersWithFailedAttempts(@Param("threshold") Integer threshold);

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find staff users who can approve registrations
     */
    @Query("SELECT u FROM User u WHERE u.role IN ('LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN') AND u.status = 'ACTIVE'")
    List<User> findApprovalStaff();

    /**
     * Count users by role
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * Count users by status
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();

    /**
     * Find users created today
     */
    @Query("SELECT u FROM User u WHERE CAST(u.createdAt AS date) = CURRENT_DATE")
    List<User> findUsersCreatedToday();

    /**
     * Find users who logged in today
     */
    @Query("SELECT u FROM User u WHERE CAST(u.lastLoginAt AS date) = CURRENT_DATE")
    List<User> findUsersLoggedInToday();

    /**
     * Update last login time
     */
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Reset failed login attempts
     */
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = NULL WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Increment failed login attempts
     */
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Lock user account
     */
    @Query("UPDATE User u SET u.lockedUntil = :lockUntil WHERE u.id = :userId")
    void lockUser(@Param("userId") UUID userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Unlock user account
     */
    @Query("UPDATE User u SET u.lockedUntil = NULL, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockUser(@Param("userId") UUID userId);

    /**
     * Find users by full name (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(CONCAT(u.firstName, ' ', COALESCE(u.middleName, ''), ' ', u.lastName)) " +
           "LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<User> findByFullNameContainingIgnoreCase(@Param("fullName") String fullName);

    /**
     * Find users by first name and last name
     */
    List<User> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    /**
     * Find users created by specific user
     */
    List<User> findByCreatedBy(UUID createdBy);

    /**
     * Find users updated by specific user
     */
    List<User> findByUpdatedBy(UUID updatedBy);
}
