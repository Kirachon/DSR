package ph.gov.dsr.security.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.security.entity.SecurityEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for SecurityEvent entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    /**
     * Find security events by event type
     */
    List<SecurityEvent> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * Find security events by severity
     */
    List<SecurityEvent> findBySeverityOrderByCreatedAtDesc(String severity);

    /**
     * Find security events by status
     */
    List<SecurityEvent> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find open security events
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.status IN ('OPEN', 'INVESTIGATING') ORDER BY s.createdAt DESC")
    List<SecurityEvent> findOpenEvents();

    /**
     * Find open security events with pagination
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.status IN ('OPEN', 'INVESTIGATING') ORDER BY s.createdAt DESC")
    Page<SecurityEvent> findOpenEvents(Pageable pageable);

    /**
     * Find high-severity events
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.severity IN ('HIGH', 'CRITICAL') ORDER BY s.createdAt DESC")
    List<SecurityEvent> findHighSeverityEvents();

    /**
     * Find critical events
     */
    List<SecurityEvent> findBySeverityAndStatusOrderByCreatedAtDesc(String severity, String status);

    /**
     * Find events by user ID
     */
    List<SecurityEvent> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find events by source IP
     */
    List<SecurityEvent> findBySourceIpOrderByCreatedAtDesc(String sourceIp);

    /**
     * Find events by date range
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<SecurityEvent> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find events by date range with pagination
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    Page<SecurityEvent> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

    /**
     * Find events assigned to user
     */
    List<SecurityEvent> findByAssignedToOrderByCreatedAtDesc(UUID assignedTo);

    /**
     * Find events assigned to team
     */
    List<SecurityEvent> findByAssignedTeamOrderByCreatedAtDesc(String assignedTeam);

    /**
     * Find escalated events
     */
    List<SecurityEvent> findByEscalatedTrueOrderByEscalatedAtDesc();

    /**
     * Find events by correlation ID
     */
    List<SecurityEvent> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);

    /**
     * Find false positives
     */
    List<SecurityEvent> findByFalsePositiveTrueOrderByCreatedAtDesc();

    /**
     * Find events by attack vector
     */
    List<SecurityEvent> findByAttackVectorOrderByCreatedAtDesc(String attackVector);

    /**
     * Find events by confidence score range
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.confidenceScore BETWEEN :minScore AND :maxScore ORDER BY s.createdAt DESC")
    List<SecurityEvent> findByConfidenceScoreRange(@Param("minScore") Double minScore,
                                                   @Param("maxScore") Double maxScore);

    /**
     * Find events by risk score range
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.riskScore BETWEEN :minScore AND :maxScore ORDER BY s.createdAt DESC")
    List<SecurityEvent> findByRiskScoreRange(@Param("minScore") Integer minScore,
                                             @Param("maxScore") Integer maxScore);

    /**
     * Find recent events (last 24 hours)
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<SecurityEvent> findRecentEvents(@Param("since") LocalDateTime since);

    /**
     * Find unresolved events older than specified time
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.status IN ('OPEN', 'INVESTIGATING') AND s.createdAt < :cutoffDate ORDER BY s.createdAt ASC")
    List<SecurityEvent> findUnresolvedEventsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find events requiring escalation
     */
    @Query("SELECT s FROM SecurityEvent s WHERE s.escalated = false AND s.severity IN ('HIGH', 'CRITICAL') AND s.createdAt < :cutoffDate ORDER BY s.createdAt ASC")
    List<SecurityEvent> findEventsRequiringEscalation(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count events by severity in date range
     */
    @Query("SELECT COUNT(s) FROM SecurityEvent s WHERE s.severity = :severity AND s.createdAt BETWEEN :startDate AND :endDate")
    Long countBySeverityAndDateRange(@Param("severity") String severity,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Count events by status in date range
     */
    @Query("SELECT COUNT(s) FROM SecurityEvent s WHERE s.status = :status AND s.createdAt BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") String status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find events by multiple criteria
     */
    @Query("SELECT s FROM SecurityEvent s WHERE " +
           "(:eventType IS NULL OR s.eventType = :eventType) AND " +
           "(:severity IS NULL OR s.severity = :severity) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:assignedTo IS NULL OR s.assignedTo = :assignedTo) AND " +
           "(:startDate IS NULL OR s.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR s.createdAt <= :endDate) " +
           "ORDER BY s.createdAt DESC")
    Page<SecurityEvent> findByCriteria(@Param("eventType") String eventType,
                                       @Param("severity") String severity,
                                       @Param("status") String status,
                                       @Param("assignedTo") UUID assignedTo,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    /**
     * Get event statistics by type
     */
    @Query("SELECT s.eventType, COUNT(s) FROM SecurityEvent s WHERE s.createdAt BETWEEN :startDate AND :endDate GROUP BY s.eventType")
    List<Object[]> getEventTypeStatistics(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Get event statistics by severity
     */
    @Query("SELECT s.severity, COUNT(s) FROM SecurityEvent s WHERE s.createdAt BETWEEN :startDate AND :endDate GROUP BY s.severity")
    List<Object[]> getSeverityStatistics(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Get average resolution time by severity
     */
    @Query("SELECT s.severity, AVG(EXTRACT(EPOCH FROM (s.resolvedAt - s.createdAt))/3600) FROM SecurityEvent s WHERE s.resolvedAt IS NOT NULL GROUP BY s.severity")
    List<Object[]> getAverageResolutionTimeBySeverity();
}
