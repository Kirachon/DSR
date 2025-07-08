package ph.gov.dsr.security.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.security.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for AuditLog entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find audit logs by user ID
     */
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find audit logs by user ID with pagination
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find audit logs by event type
     */
    List<AuditLog> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * Find audit logs by event category
     */
    List<AuditLog> findByEventCategoryOrderByCreatedAtDesc(String eventCategory);

    /**
     * Find audit logs by risk level
     */
    List<AuditLog> findByRiskLevelOrderByCreatedAtDesc(String riskLevel);

    /**
     * Find audit logs by date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by date range with pagination
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate, 
                                   Pageable pageable);

    /**
     * Find audit logs by user and date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserAndDateRange(@Param("userId") UUID userId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by user ID and date range (alias for findByUserAndDateRange)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by IP address
     */
    List<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    /**
     * Find audit logs by session ID
     */
    List<AuditLog> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    /**
     * Find audit logs by correlation ID
     */
    List<AuditLog> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);

    /**
     * Find audit logs by resource type and resource ID
     */
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId ORDER BY a.createdAt DESC")
    List<AuditLog> findByResourceTypeAndResourceId(@Param("resourceType") String resourceType,
                                                   @Param("resourceId") String resourceId);

    /**
     * Find high-risk audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.riskLevel IN ('HIGH', 'CRITICAL') ORDER BY a.createdAt DESC")
    List<AuditLog> findHighRiskLogs();

    /**
     * Find failed operations
     */
    @Query("SELECT a FROM AuditLog a WHERE a.result = 'FAILURE' ORDER BY a.createdAt DESC")
    List<AuditLog> findFailedOperations();

    /**
     * Find audit logs with compliance flags
     */
    @Query("SELECT a FROM AuditLog a WHERE a.complianceFlags IS NOT NULL AND a.complianceFlags != '' ORDER BY a.createdAt DESC")
    List<AuditLog> findLogsWithComplianceFlags();

    /**
     * Find audit logs by source system
     */
    List<AuditLog> findBySourceSystemOrderByCreatedAtDesc(String sourceSystem);

    /**
     * Count audit logs by event type in date range
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.eventType = :eventType AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countByEventTypeAndDateRange(@Param("eventType") String eventType,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Count audit logs by user in date range
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countByUserAndDateRange(@Param("userId") UUID userId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent audit logs (last 24 hours)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentLogs(@Param("since") LocalDateTime since);

    /**
     * Find audit logs by multiple criteria
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:eventType IS NULL OR a.eventType = :eventType) AND " +
           "(:riskLevel IS NULL OR a.riskLevel = :riskLevel) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByCriteria(@Param("userId") UUID userId,
                                  @Param("eventType") String eventType,
                                  @Param("riskLevel") String riskLevel,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    /**
     * Find archived audit logs
     */
    List<AuditLog> findByArchivedTrueOrderByArchivedAtDesc();

    /**
     * Find non-archived audit logs
     */
    List<AuditLog> findByArchivedFalseOrderByCreatedAtDesc();

    /**
     * Find audit logs eligible for archiving
     */
    @Query("SELECT a FROM AuditLog a WHERE a.archived = false AND a.createdAt < :cutoffDate")
    List<AuditLog> findEligibleForArchiving(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete old archived audit logs
     */
    @Query("DELETE FROM AuditLog a WHERE a.archived = true AND a.archivedAt < :cutoffDate")
    void deleteOldArchivedLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get audit log statistics
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.eventType")
    List<Object[]> getEventTypeStatistics(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Get risk level statistics
     */
    @Query("SELECT a.riskLevel, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.riskLevel")
    List<Object[]> getRiskLevelStatistics(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
