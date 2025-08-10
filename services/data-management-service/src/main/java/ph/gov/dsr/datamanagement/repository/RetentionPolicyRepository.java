package ph.gov.dsr.datamanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.datamanagement.entity.RetentionPolicy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RetentionPolicy entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@Repository
public interface RetentionPolicyRepository extends JpaRepository<RetentionPolicy, UUID> {

    /**
     * Find retention policy by entity type
     */
    Optional<RetentionPolicy> findByEntityTypeAndIsActiveTrue(String entityType);

    /**
     * Find all active retention policies
     */
    List<RetentionPolicy> findByIsActiveTrueOrderByEntityType();

    /**
     * Find retention policies with auto-archive enabled
     */
    List<RetentionPolicy> findByAutoArchiveEnabledTrueAndIsActiveTrueOrderByEntityType();

    /**
     * Find retention policies with auto-delete enabled
     */
    List<RetentionPolicy> findByAutoDeleteEnabledTrueAndIsActiveTrueOrderByEntityType();

    /**
     * Find retention policies by created by
     */
    List<RetentionPolicy> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Check if retention policy exists for entity type
     */
    boolean existsByEntityTypeAndIsActiveTrue(String entityType);

    /**
     * Find currently effective retention policies
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.isActive = true " +
           "AND (p.effectiveFrom IS NULL OR p.effectiveFrom <= :currentDate) " +
           "AND (p.effectiveUntil IS NULL OR p.effectiveUntil >= :currentDate) " +
           "ORDER BY p.entityType")
    List<RetentionPolicy> findCurrentlyEffectivePolicies(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find retention policies that will become effective soon
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.isActive = true " +
           "AND p.effectiveFrom BETWEEN :currentDate AND :futureDate " +
           "ORDER BY p.effectiveFrom")
    List<RetentionPolicy> findUpcomingPolicies(@Param("currentDate") LocalDateTime currentDate,
                                              @Param("futureDate") LocalDateTime futureDate);

    /**
     * Find retention policies that will expire soon
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.isActive = true " +
           "AND p.effectiveUntil BETWEEN :currentDate AND :futureDate " +
           "ORDER BY p.effectiveUntil")
    List<RetentionPolicy> findExpiringPolicies(@Param("currentDate") LocalDateTime currentDate,
                                              @Param("futureDate") LocalDateTime futureDate);

    /**
     * Find retention policies by retention days range
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.isActive = true " +
           "AND p.retentionDays BETWEEN :minDays AND :maxDays " +
           "ORDER BY p.retentionDays")
    List<RetentionPolicy> findByRetentionDaysRange(@Param("minDays") Integer minDays,
                                                   @Param("maxDays") Integer maxDays);

    /**
     * Get retention policy statistics
     */
    @Query("SELECT " +
           "COUNT(p) as totalPolicies, " +
           "COUNT(CASE WHEN p.isActive = true THEN 1 END) as activePolicies, " +
           "COUNT(CASE WHEN p.autoArchiveEnabled = true THEN 1 END) as autoArchivePolicies, " +
           "COUNT(CASE WHEN p.autoDeleteEnabled = true THEN 1 END) as autoDeletePolicies, " +
           "AVG(p.retentionDays) as averageRetentionDays, " +
           "MAX(p.retentionDays) as maxRetentionDays, " +
           "MIN(p.retentionDays) as minRetentionDays " +
           "FROM RetentionPolicy p")
    Object[] getRetentionPolicyStatistics();

    /**
     * Find retention policies updated recently
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.updatedAt >= :since " +
           "ORDER BY p.updatedAt DESC")
    List<RetentionPolicy> findRecentlyUpdatedPolicies(@Param("since") LocalDateTime since);

    /**
     * Find retention policies by entity types
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.entityType IN :entityTypes " +
           "AND p.isActive = true ORDER BY p.entityType")
    List<RetentionPolicy> findByEntityTypesAndIsActiveTrue(@Param("entityTypes") List<String> entityTypes);

    /**
     * Count retention policies by auto-archive setting
     */
    @Query("SELECT p.autoArchiveEnabled, COUNT(p) FROM RetentionPolicy p " +
           "WHERE p.isActive = true GROUP BY p.autoArchiveEnabled")
    List<Object[]> countPoliciesByAutoArchiveSetting();

    /**
     * Find retention policies with longest retention periods
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.isActive = true " +
           "ORDER BY p.retentionDays DESC")
    List<RetentionPolicy> findPoliciesOrderByRetentionDaysDesc();

    /**
     * Find retention policies with shortest retention periods
     */
    @Query("SELECT p FROM RetentionPolicy p WHERE p.isActive = true " +
           "ORDER BY p.retentionDays ASC")
    List<RetentionPolicy> findPoliciesOrderByRetentionDaysAsc();
}
