package ph.gov.dsr.eligibility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.eligibility.entity.EligibilityAssessment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for EligibilityAssessment entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface EligibilityAssessmentRepository extends JpaRepository<EligibilityAssessment, UUID> {

    /**
     * Find latest assessment for PSN and program
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.psn = :psn AND e.programCode = :programCode " +
           "ORDER BY e.assessmentDate DESC")
    Optional<EligibilityAssessment> findLatestByPsnAndProgramCode(@Param("psn") String psn, 
                                                                 @Param("programCode") String programCode);

    /**
     * Find all assessments for a PSN
     */
    List<EligibilityAssessment> findByPsnOrderByAssessmentDateDesc(String psn);

    /**
     * Find assessments by PSN and program code
     */
    List<EligibilityAssessment> findByPsnAndProgramCodeOrderByAssessmentDateDesc(String psn, String programCode);

    /**
     * Find assessments by household ID
     */
    List<EligibilityAssessment> findByHouseholdIdOrderByAssessmentDateDesc(UUID householdId);

    /**
     * Find assessments by status
     */
    List<EligibilityAssessment> findByStatus(EligibilityAssessment.EligibilityStatus status);

    /**
     * Find assessments by program code
     */
    List<EligibilityAssessment> findByProgramCodeOrderByAssessmentDateDesc(String programCode);

    /**
     * Find valid assessments (not expired)
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.validUntil > :now")
    List<EligibilityAssessment> findValidAssessments(@Param("now") LocalDateTime now);

    /**
     * Find expired assessments
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.validUntil <= :now")
    List<EligibilityAssessment> findExpiredAssessments(@Param("now") LocalDateTime now);

    /**
     * Find assessments needing review
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.status IN ('PENDING_REVIEW', 'UNDER_REVIEW')")
    List<EligibilityAssessment> findAssessmentsNeedingReview();

    /**
     * Find eligible assessments
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.status IN ('ELIGIBLE', 'CONDITIONAL')")
    List<EligibilityAssessment> findEligibleAssessments();

    /**
     * Find assessments by PMT score range
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.pmtScore BETWEEN :minScore AND :maxScore")
    List<EligibilityAssessment> findByPmtScoreRange(@Param("minScore") BigDecimal minScore,
                                                   @Param("maxScore") BigDecimal maxScore);

    /**
     * Find poor households (PMT score below threshold)
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.isPoor = true")
    List<EligibilityAssessment> findPoorHouseholds();

    /**
     * Find assessments by overall score range
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.overallScore BETWEEN :minScore AND :maxScore")
    List<EligibilityAssessment> findByOverallScoreRange(@Param("minScore") BigDecimal minScore,
                                                       @Param("maxScore") BigDecimal maxScore);

    /**
     * Find assessments created within date range
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.assessmentDate BETWEEN :startDate AND :endDate")
    List<EligibilityAssessment> findByAssessmentDateBetween(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find active program enrollments for a PSN
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.psn = :psn AND e.status IN ('ELIGIBLE', 'CONDITIONAL', 'APPROVED') " +
           "AND (e.validUntil IS NULL OR e.validUntil > CURRENT_TIMESTAMP)")
    List<EligibilityAssessment> findActiveProgramsByPsn(@Param("psn") String psn);

    /**
     * Find assessments by assessor
     */
    List<EligibilityAssessment> findByAssessorId(String assessorId);

    /**
     * Find assessments by source system
     */
    List<EligibilityAssessment> findBySourceSystem(String sourceSystem);

    /**
     * Check if valid assessment exists for PSN and program
     */
    @Query("SELECT COUNT(e) > 0 FROM EligibilityAssessment e WHERE e.psn = :psn AND e.programCode = :programCode " +
           "AND e.validUntil > :now AND e.status IN ('ELIGIBLE', 'CONDITIONAL')")
    boolean hasValidEligibleAssessment(@Param("psn") String psn, 
                                      @Param("programCode") String programCode,
                                      @Param("now") LocalDateTime now);

    /**
     * Count assessments by status
     */
    @Query("SELECT e.status, COUNT(e) FROM EligibilityAssessment e GROUP BY e.status")
    List<Object[]> countAssessmentsByStatus();

    /**
     * Count assessments by program
     */
    @Query("SELECT e.programCode, COUNT(e) FROM EligibilityAssessment e GROUP BY e.programCode")
    List<Object[]> countAssessmentsByProgram();

    /**
     * Get assessment statistics
     */
    @Query("SELECT COUNT(e), AVG(e.pmtScore), AVG(e.overallScore), " +
           "COUNT(CASE WHEN e.isPoor = true THEN 1 END) " +
           "FROM EligibilityAssessment e")
    Object[] getAssessmentStatistics();

    /**
     * Find assessments with multiple criteria
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE " +
           "(:psn IS NULL OR e.psn = :psn) AND " +
           "(:programCode IS NULL OR e.programCode = :programCode) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:assessorId IS NULL OR e.assessorId = :assessorId) AND " +
           "(:startDate IS NULL OR e.assessmentDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.assessmentDate <= :endDate)")
    Page<EligibilityAssessment> findByCriteria(@Param("psn") String psn,
                                              @Param("programCode") String programCode,
                                              @Param("status") EligibilityAssessment.EligibilityStatus status,
                                              @Param("assessorId") String assessorId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);

    /**
     * Find assessments expiring soon
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.validUntil BETWEEN :now AND :expirationDate " +
           "AND e.status IN ('ELIGIBLE', 'CONDITIONAL')")
    List<EligibilityAssessment> findAssessmentsExpiringSoon(@Param("now") LocalDateTime now,
                                                           @Param("expirationDate") LocalDateTime expirationDate);

    /**
     * Find duplicate assessments (same PSN, program, and date)
     */
    @Query("SELECT e.psn, e.programCode, DATE(e.assessmentDate), COUNT(e) " +
           "FROM EligibilityAssessment e " +
           "GROUP BY e.psn, e.programCode, DATE(e.assessmentDate) " +
           "HAVING COUNT(e) > 1")
    List<Object[]> findDuplicateAssessments();

    /**
     * Find assessments by vulnerability score range
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.vulnerabilityScore BETWEEN :minScore AND :maxScore")
    List<EligibilityAssessment> findByVulnerabilityScoreRange(@Param("minScore") BigDecimal minScore,
                                                             @Param("maxScore") BigDecimal maxScore);

    /**
     * Find recent assessments for a household
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.householdId = :householdId " +
           "AND e.assessmentDate >= :since ORDER BY e.assessmentDate DESC")
    List<EligibilityAssessment> findRecentAssessmentsForHousehold(@Param("householdId") UUID householdId,
                                                                 @Param("since") LocalDateTime since);

    /**
     * Search assessments by text (PSN or program code)
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE " +
           "LOWER(e.psn) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(e.programCode) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<EligibilityAssessment> searchAssessments(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Find assessments with high scores (top performers)
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE e.overallScore >= :threshold " +
           "ORDER BY e.overallScore DESC")
    List<EligibilityAssessment> findHighScoringAssessments(@Param("threshold") BigDecimal threshold);

    /**
     * Find assessments requiring urgent review
     */
    @Query("SELECT e FROM EligibilityAssessment e WHERE " +
           "(e.status = 'PENDING_REVIEW' AND e.assessmentDate <= :urgentDate) OR " +
           "(e.validUntil <= :expirationDate AND e.status IN ('ELIGIBLE', 'CONDITIONAL'))")
    List<EligibilityAssessment> findAssessmentsRequiringUrgentReview(@Param("urgentDate") LocalDateTime urgentDate,
                                                                    @Param("expirationDate") LocalDateTime expirationDate);
}
