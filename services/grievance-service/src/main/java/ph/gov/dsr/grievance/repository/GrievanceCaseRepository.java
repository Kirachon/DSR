package ph.gov.dsr.grievance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.grievance.entity.GrievanceCase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for GrievanceCase entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface GrievanceCaseRepository extends JpaRepository<GrievanceCase, UUID> {

    /**
     * Find case by case number
     */
    Optional<GrievanceCase> findByCaseNumber(String caseNumber);

    /**
     * Find cases by complainant PSN
     */
    List<GrievanceCase> findByComplainantPsnOrderBySubmissionDateDesc(String complainantPsn);

    /**
     * Find cases by status
     */
    List<GrievanceCase> findByStatus(GrievanceCase.CaseStatus status);

    /**
     * Find cases by category
     */
    List<GrievanceCase> findByCategory(GrievanceCase.GrievanceCategory category);

    /**
     * Find cases by priority
     */
    List<GrievanceCase> findByPriority(GrievanceCase.Priority priority);

    /**
     * Find cases assigned to specific person
     */
    List<GrievanceCase> findByAssignedToOrderByPriorityDescSubmissionDateAsc(String assignedTo);

    /**
     * Find overdue cases
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.resolutionTargetDate < :now " +
           "AND c.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') " +
           "ORDER BY c.resolutionTargetDate ASC")
    List<GrievanceCase> findOverdueCases(@Param("now") LocalDateTime now);

    /**
     * Find cases by submission date range
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.submissionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY c.submissionDate DESC")
    List<GrievanceCase> findBySubmissionDateBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find urgent cases
     */
    List<GrievanceCase> findByIsUrgentTrueOrderBySubmissionDateAsc();

    /**
     * Find cases requiring investigation
     */
    List<GrievanceCase> findByRequiresInvestigationTrueOrderBySubmissionDateAsc();

    /**
     * Find escalated cases
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.escalationLevel > 0 " +
           "ORDER BY c.escalationLevel DESC, c.escalationDate ASC")
    List<GrievanceCase> findEscalatedCases();

    /**
     * Find cases by program code
     */
    List<GrievanceCase> findByProgramCodeOrderBySubmissionDateDesc(String programCode);

    /**
     * Find cases by service provider
     */
    List<GrievanceCase> findByServiceProviderOrderBySubmissionDateDesc(String serviceProvider);

    /**
     * Find cases by submission channel
     */
    List<GrievanceCase> findBySubmissionChannelOrderBySubmissionDateDesc(String submissionChannel);

    /**
     * Count cases by status
     */
    @Query("SELECT c.status, COUNT(c) FROM GrievanceCase c GROUP BY c.status")
    List<Object[]> countCasesByStatus();

    /**
     * Count cases by category
     */
    @Query("SELECT c.category, COUNT(c) FROM GrievanceCase c GROUP BY c.category")
    List<Object[]> countCasesByCategory();

    /**
     * Count cases by priority
     */
    @Query("SELECT c.priority, COUNT(c) FROM GrievanceCase c GROUP BY c.priority")
    List<Object[]> countCasesByPriority();

    /**
     * Get case statistics
     */
    @Query("SELECT COUNT(c), " +
           "COUNT(CASE WHEN c.status = 'RESOLVED' THEN 1 END), " +
           "COUNT(CASE WHEN c.status = 'CLOSED' THEN 1 END), " +
           "COUNT(CASE WHEN c.resolutionTargetDate < CURRENT_TIMESTAMP AND c.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') THEN 1 END), " +
           "AVG(CASE WHEN c.resolutionDate IS NOT NULL THEN EXTRACT(DAY FROM (c.resolutionDate - c.submissionDate)) END) " +
           "FROM GrievanceCase c")
    Object[] getCaseStatistics();

    /**
     * Find cases by multiple criteria
     */
    @Query("SELECT c FROM GrievanceCase c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:assignedTo IS NULL OR c.assignedTo = :assignedTo) AND " +
           "(:startDate IS NULL OR c.submissionDate >= :startDate) AND " +
           "(:endDate IS NULL OR c.submissionDate <= :endDate)")
    Page<GrievanceCase> findByCriteria(@Param("status") GrievanceCase.CaseStatus status,
                                      @Param("priority") GrievanceCase.Priority priority,
                                      @Param("category") GrievanceCase.GrievanceCategory category,
                                      @Param("assignedTo") String assignedTo,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    /**
     * Search cases by text
     */
    @Query("SELECT c FROM GrievanceCase c WHERE " +
           "LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.subject) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.complainantName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(c.complainantPsn) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<GrievanceCase> searchCases(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Find cases with satisfaction feedback
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.complainantSatisfaction IS NOT NULL " +
           "ORDER BY c.resolutionDate DESC")
    List<GrievanceCase> findCasesWithSatisfactionFeedback();

    /**
     * Find cases by satisfaction level
     */
    List<GrievanceCase> findByComplainantSatisfactionOrderByResolutionDateDesc(String satisfaction);

    /**
     * Find anonymous cases
     */
    List<GrievanceCase> findByIsAnonymousTrueOrderBySubmissionDateDesc();

    /**
     * Find cases by escalation level
     */
    List<GrievanceCase> findByEscalationLevelOrderByEscalationDateDesc(Integer escalationLevel);

    /**
     * Find cases escalated to specific person/department
     */
    List<GrievanceCase> findByEscalatedToOrderByEscalationDateDesc(String escalatedTo);

    /**
     * Find cases resolved within target time
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.resolutionDate IS NOT NULL " +
           "AND c.resolutionTargetDate IS NOT NULL " +
           "AND c.resolutionDate <= c.resolutionTargetDate " +
           "ORDER BY c.resolutionDate DESC")
    List<GrievanceCase> findCasesResolvedWithinTarget();

    /**
     * Find cases resolved after target time
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.resolutionDate IS NOT NULL " +
           "AND c.resolutionTargetDate IS NOT NULL " +
           "AND c.resolutionDate > c.resolutionTargetDate " +
           "ORDER BY c.resolutionDate DESC")
    List<GrievanceCase> findCasesResolvedAfterTarget();

    /**
     * Get monthly case statistics
     */
    @Query("SELECT YEAR(c.submissionDate), MONTH(c.submissionDate), COUNT(c), " +
           "COUNT(CASE WHEN c.status = 'RESOLVED' THEN 1 END) " +
           "FROM GrievanceCase c " +
           "WHERE c.submissionDate >= :startDate " +
           "GROUP BY YEAR(c.submissionDate), MONTH(c.submissionDate) " +
           "ORDER BY YEAR(c.submissionDate), MONTH(c.submissionDate)")
    List<Object[]> getMonthlyCaseStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * Find cases by incident date range
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.incidentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY c.incidentDate DESC")
    List<GrievanceCase> findByIncidentDateBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Check if case number exists
     */
    boolean existsByCaseNumber(String caseNumber);

    /**
     * Count open cases for complainant
     */
    @Query("SELECT COUNT(c) FROM GrievanceCase c WHERE c.complainantPsn = :psn " +
           "AND c.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    long countOpenCasesForComplainant(@Param("psn") String psn);

    /**
     * Find recent cases for complainant
     */
    @Query("SELECT c FROM GrievanceCase c WHERE c.complainantPsn = :psn " +
           "AND c.submissionDate >= :since ORDER BY c.submissionDate DESC")
    List<GrievanceCase> findRecentCasesForComplainant(@Param("psn") String psn,
                                                     @Param("since") LocalDateTime since);
}
