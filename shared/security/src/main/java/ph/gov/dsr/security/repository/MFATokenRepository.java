package ph.gov.dsr.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.security.entity.MFAMethod;
import ph.gov.dsr.security.entity.MFAToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MFAToken entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Repository
public interface MFATokenRepository extends JpaRepository<MFAToken, UUID> {

    /**
     * Find MFA token by user ID and token value
     */
    Optional<MFAToken> findByUserIdAndToken(String userId, String token);

    /**
     * Find MFA token by user ID and token value that is still valid
     */
    @Query("SELECT t FROM MFAToken t WHERE t.userId = :userId AND t.token = :token " +
           "AND t.isUsed = false AND t.expiresAt > :now AND t.attempts < t.maxAttempts")
    Optional<MFAToken> findValidTokenByUserIdAndToken(@Param("userId") String userId, 
                                                      @Param("token") String token,
                                                      @Param("now") LocalDateTime now);

    /**
     * Find all valid tokens for a user
     */
    @Query("SELECT t FROM MFAToken t WHERE t.userId = :userId " +
           "AND t.isUsed = false AND t.expiresAt > :now AND t.attempts < t.maxAttempts")
    List<MFAToken> findValidTokensByUserId(@Param("userId") String userId, 
                                          @Param("now") LocalDateTime now);

    /**
     * Find all tokens for a user by method
     */
    List<MFAToken> findByUserIdAndMethod(String userId, MFAMethod method);

    /**
     * Find all tokens for a user
     */
    List<MFAToken> findByUserId(String userId);

    /**
     * Find expired tokens
     */
    @Query("SELECT t FROM MFAToken t WHERE t.expiresAt < :now")
    List<MFAToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Find tokens that have exceeded max attempts
     */
    @Query("SELECT t FROM MFAToken t WHERE t.attempts >= t.maxAttempts AND t.isUsed = false")
    List<MFAToken> findExceededAttemptTokens();

    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(t) FROM MFAToken t WHERE t.userId = :userId " +
           "AND t.isUsed = false AND t.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") String userId, 
                                  @Param("now") LocalDateTime now);

    /**
     * Count failed attempts for a user in the last period
     */
    @Query("SELECT COUNT(t) FROM MFAToken t WHERE t.userId = :userId " +
           "AND t.createdAt > :since AND t.attempts > 0")
    long countFailedAttemptsSince(@Param("userId") String userId, 
                                 @Param("since") LocalDateTime since);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM MFAToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete used tokens older than specified date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM MFAToken t WHERE t.isUsed = true AND t.usedAt < :before")
    int deleteUsedTokensBefore(@Param("before") LocalDateTime before);

    /**
     * Delete all tokens for a user
     */
    @Modifying
    @Transactional
    void deleteByUserId(String userId);

    /**
     * Mark token as used
     */
    @Modifying
    @Transactional
    @Query("UPDATE MFAToken t SET t.isUsed = true, t.usedAt = :usedAt WHERE t.id = :tokenId")
    int markTokenAsUsed(@Param("tokenId") UUID tokenId, @Param("usedAt") LocalDateTime usedAt);

    /**
     * Increment token attempts
     */
    @Modifying
    @Transactional
    @Query("UPDATE MFAToken t SET t.attempts = t.attempts + 1 WHERE t.id = :tokenId")
    int incrementTokenAttempts(@Param("tokenId") UUID tokenId);
}
