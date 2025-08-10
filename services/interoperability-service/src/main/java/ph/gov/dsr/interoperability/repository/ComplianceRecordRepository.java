package ph.gov.dsr.interoperability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.interoperability.entity.ComplianceRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ComplianceRecord entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, UUID> {

    /**
     * Find records by compliance standard
     */
    List<ComplianceRecord> findByStandardOrderByCheckedAtDesc(String standard);

    /**
     * Find records by entity
     */
    List<ComplianceRecord> findByEntityOrderByCheckedAtDesc(String entity);

    /**
     * Find records by compliance status
     */
    List<ComplianceRecord> findByCompliantOrderByCheckedAtDesc(Boolean compliant);

    /**
     * Find non-compliant records
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.compliant = false ORDER BY c.checkedAt DESC")
    List<ComplianceRecord> findNonCompliantRecords();

    /**
     * Find records by standard and entity
     */
    List<ComplianceRecord> findByStandardAndEntityOrderByCheckedAtDesc(String standard, String entity);

    /**
     * Find latest record for standard and entity
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.standard = :standard AND c.entity = :entity " +
           "ORDER BY c.checkedAt DESC")
    List<ComplianceRecord> findLatestByStandardAndEntity(@Param("standard") String standard, 
                                                        @Param("entity") String entity);

    /**
     * Find records by severity
     */
    List<ComplianceRecord> findBySeverityOrderByCheckedAtDesc(String severity);

    /**
     * Find records by category
     */
    List<ComplianceRecord> findByCategoryOrderByCheckedAtDesc(String category);

    /**
     * Find records checked within date range
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.checkedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.checkedAt DESC")
    List<ComplianceRecord> findByCheckedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Find records by date range (alias for findByCheckedAtBetween)
     */
    default List<ComplianceRecord> findByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return findByCheckedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    /**
     * Find expired records
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < CURRENT_TIMESTAMP " +
           "ORDER BY c.expiresAt ASC")
    List<ComplianceRecord> findExpiredRecords();

    /**
     * Find records expiring soon (within specified days)
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.expiresAt IS NOT NULL " +
           "AND c.expiresAt BETWEEN CURRENT_TIMESTAMP AND :expiryDate " +
           "ORDER BY c.expiresAt ASC")
    List<ComplianceRecord> findRecordsExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find records requiring remediation
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.compliant = false " +
           "AND (c.remediationStatus IS NULL OR c.remediationStatus IN ('PENDING', 'FAILED')) " +
           "ORDER BY c.checkedAt DESC")
    List<ComplianceRecord> findRecordsRequiringRemediation();

    /**
     * Find records by remediation status
     */
    List<ComplianceRecord> findByRemediationStatusOrderByCheckedAtDesc(String remediationStatus);

    /**
     * Find records checked by specific user
     */
    List<ComplianceRecord> findByCheckedByOrderByCheckedAtDesc(String checkedBy);

    /**
     * Find records with reference ID
     */
    Optional<ComplianceRecord> findByReferenceId(String referenceId);

    /**
     * Count records by standard
     */
    @Query("SELECT c.standard, COUNT(c) FROM ComplianceRecord c GROUP BY c.standard")
    List<Object[]> countRecordsByStandard();

    /**
     * Count compliant vs non-compliant records
     */
    @Query("SELECT c.compliant, COUNT(c) FROM ComplianceRecord c GROUP BY c.compliant")
    List<Object[]> countRecordsByCompliance();

    /**
     * Get compliance statistics for a standard
     */
    @Query("SELECT COUNT(c), " +
           "COUNT(CASE WHEN c.compliant = true THEN 1 END), " +
           "COUNT(CASE WHEN c.compliant = false THEN 1 END), " +
           "AVG(CASE WHEN c.complianceScore IS NOT NULL THEN c.complianceScore END) " +
           "FROM ComplianceRecord c WHERE c.standard = :standard")
    Object[] getComplianceStatistics(@Param("standard") String standard);

    /**
     * Get recent compliance trends
     */
    @Query("SELECT DATE(c.checkedAt), COUNT(c), " +
           "COUNT(CASE WHEN c.compliant = true THEN 1 END) " +
           "FROM ComplianceRecord c " +
           "WHERE c.checkedAt >= :startDate " +
           "GROUP BY DATE(c.checkedAt) " +
           "ORDER BY DATE(c.checkedAt)")
    List<Object[]> getComplianceTrends(@Param("startDate") LocalDateTime startDate);

    /**
     * Find records by multiple criteria
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE " +
           "(:standard IS NULL OR c.standard = :standard) AND " +
           "(:entity IS NULL OR c.entity = :entity) AND " +
           "(:compliant IS NULL OR c.compliant = :compliant) AND " +
           "(:severity IS NULL OR c.severity = :severity) AND " +
           "(:category IS NULL OR c.category = :category) " +
           "ORDER BY c.checkedAt DESC")
    List<ComplianceRecord> findByCriteria(@Param("standard") String standard,
                                         @Param("entity") String entity,
                                         @Param("compliant") Boolean compliant,
                                         @Param("severity") String severity,
                                         @Param("category") String category);

    /**
     * Delete old records (cleanup)
     */
    @Query("DELETE FROM ComplianceRecord c WHERE c.checkedAt < :cutoffDate")
    void deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find duplicate records
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE EXISTS " +
           "(SELECT c2 FROM ComplianceRecord c2 WHERE c2.standard = c.standard " +
           "AND c2.entity = c.entity AND c2.checkedAt > c.checkedAt)")
    List<ComplianceRecord> findDuplicateRecords();

    /**
     * Get compliance summary by entity
     */
    @Query("SELECT c.entity, COUNT(c), " +
           "COUNT(CASE WHEN c.compliant = true THEN 1 END), " +
           "COUNT(CASE WHEN c.compliant = false THEN 1 END) " +
           "FROM ComplianceRecord c " +
           "GROUP BY c.entity " +
           "ORDER BY c.entity")
    List<Object[]> getComplianceSummaryByEntity();

    /**
     * Find records needing attention (non-compliant or expiring soon)
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE " +
           "(c.compliant = false) OR " +
           "(c.expiresAt IS NOT NULL AND c.expiresAt <= :alertDate) " +
           "ORDER BY c.checkedAt DESC")
    List<ComplianceRecord> findRecordsNeedingAttention(@Param("alertDate") LocalDateTime alertDate);
}
