package ph.gov.dsr.registration.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.registration.entity.Registration;
import ph.gov.dsr.registration.entity.RegistrationChannel;
import ph.gov.dsr.registration.entity.RegistrationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Registration entity
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    /**
     * Find registration by registration number
     */
    Optional<Registration> findByRegistrationNumber(String registrationNumber);

    /**
     * Check if registration number exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Find registrations by status
     */
    List<Registration> findByStatus(RegistrationStatus status);

    /**
     * Find registrations by registration channel
     */
    List<Registration> findByRegistrationChannel(RegistrationChannel channel);

    /**
     * Find registrations by status and channel
     */
    List<Registration> findByStatusAndRegistrationChannel(RegistrationStatus status, RegistrationChannel channel);

    /**
     * Find registrations by household ID
     */
    List<Registration> findByHouseholdId(UUID householdId);

    /**
     * Find registrations assigned to specific user
     */
    List<Registration> findByAssignedToId(UUID assignedToId);

    /**
     * Find registrations created by specific user
     */
    List<Registration> findByCreatedById(UUID createdById);

    /**
     * Find registrations submitted within date range
     */
    @Query("SELECT r FROM Registration r WHERE r.submissionDate BETWEEN :startDate AND :endDate")
    List<Registration> findBySubmissionDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Find registrations created within date range
     */
    @Query("SELECT r FROM Registration r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<Registration> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Find overdue registrations
     */
    @Query("SELECT r FROM Registration r WHERE r.estimatedCompletionDate < CURRENT_DATE " +
           "AND r.status NOT IN ('APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')")
    List<Registration> findOverdueRegistrations();

    /**
     * Find registrations by priority level
     */
    List<Registration> findByPriorityLevel(Integer priorityLevel);

    /**
     * Find high priority registrations
     */
    @Query("SELECT r FROM Registration r WHERE r.priorityLevel = 1 AND r.status IN ('PENDING_VERIFICATION', 'PENDING_APPROVAL')")
    List<Registration> findHighPriorityRegistrations();

    /**
     * Count registrations by status
     */
    long countByStatus(RegistrationStatus status);

    /**
     * Count registrations by channel
     */
    long countByRegistrationChannel(RegistrationChannel channel);

    /**
     * Count registrations created today
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE CAST(r.createdAt AS date) = CURRENT_DATE")
    long countRegistrationsCreatedToday();

    /**
     * Count registrations submitted today
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE CAST(r.submissionDate AS date) = CURRENT_DATE")
    long countRegistrationsSubmittedToday();

    /**
     * Count registrations approved today
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE CAST(r.approvalDate AS date) = CURRENT_DATE")
    long countRegistrationsApprovedToday();

    /**
     * Find registrations pending verification
     */
    @Query("SELECT r FROM Registration r WHERE r.status = 'PENDING_VERIFICATION'")
    List<Registration> findPendingVerification();

    /**
     * Find registrations pending approval
     */
    @Query("SELECT r FROM Registration r WHERE r.status = 'PENDING_APPROVAL'")
    List<Registration> findPendingApproval();

    /**
     * Find approved registrations
     */
    @Query("SELECT r FROM Registration r WHERE r.status = 'APPROVED'")
    List<Registration> findApprovedRegistrations();

    /**
     * Find rejected registrations
     */
    @Query("SELECT r FROM Registration r WHERE r.status = 'REJECTED'")
    List<Registration> findRejectedRegistrations();

    /**
     * Search registrations by registration number or household number
     */
    @Query("SELECT r FROM Registration r WHERE " +
           "r.registrationNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "r.household.householdNumber LIKE CONCAT('%', :searchTerm, '%')")
    Page<Registration> searchRegistrations(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find registrations by status with pagination
     */
    Page<Registration> findByStatus(RegistrationStatus status, Pageable pageable);

    /**
     * Find registrations assigned to user with pagination
     */
    Page<Registration> findByAssignedToId(UUID assignedToId, Pageable pageable);

    /**
     * Generate next registration number
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(r.registrationNumber, 5) AS INTEGER)), 0) + 1 " +
           "FROM Registration r WHERE r.registrationNumber LIKE 'REG-%'")
    Integer getNextRegistrationNumber();

    /**
     * Find registrations by completion date range
     */
    @Query("SELECT r FROM Registration r WHERE r.completionDate BETWEEN :startDate AND :endDate")
    List<Registration> findByCompletionDateBetween(@Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find registrations with specific estimated completion date
     */
    List<Registration> findByEstimatedCompletionDate(LocalDate estimatedCompletionDate);

    /**
     * Find registrations due today
     */
    @Query("SELECT r FROM Registration r WHERE r.estimatedCompletionDate = CURRENT_DATE " +
           "AND r.status NOT IN ('APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')")
    List<Registration> findRegistrationsDueToday();

    /**
     * Find registrations due this week
     */
    @Query(value = "SELECT * FROM dsr_core.registrations r WHERE r.estimated_completion_date >= CURRENT_DATE " +
           "AND r.estimated_completion_date <= CURRENT_DATE + INTERVAL '7 days' " +
           "AND r.status NOT IN ('APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')", nativeQuery = true)
    List<Registration> findRegistrationsDueThisWeek();

    /**
     * Get average processing time for completed registrations
     */
    @Query(value = "SELECT AVG(r.completion_date - r.submission_date) " +
           "FROM dsr_core.registrations r WHERE r.completion_date IS NOT NULL AND r.submission_date IS NOT NULL",
           nativeQuery = true)
    Double getAverageProcessingTimeInDays();

    /**
     * Count registrations by status and date range
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.status = :status " +
           "AND r.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") RegistrationStatus status,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Find registrations with notes containing specific text
     */
    @Query("SELECT r FROM Registration r WHERE r.notes LIKE CONCAT('%', :searchText, '%')")
    List<Registration> findByNotesContaining(@Param("searchText") String searchText);

    /**
     * Find registrations by household head PSN
     */
    @Query("SELECT r FROM Registration r WHERE r.household.headOfHouseholdPsn = :psn")
    List<Registration> findByHouseholdHeadPsn(@Param("psn") String psn);

    /**
     * Find registrations by household characteristics
     */
    @Query("SELECT r FROM Registration r WHERE " +
           "(:isIndigenous IS NULL OR r.household.isIndigenous = :isIndigenous) AND " +
           "(:isPwd IS NULL OR r.household.isPwdHousehold = :isPwd) AND " +
           "(:isSenior IS NULL OR r.household.isSeniorCitizenHousehold = :isSenior)")
    List<Registration> findByHouseholdCharacteristics(
        @Param("isIndigenous") Boolean isIndigenous,
        @Param("isPwd") Boolean isPwd,
        @Param("isSenior") Boolean isSenior
    );

    /**
     * Update registration status
     */
    @Query("UPDATE Registration r SET r.status = :status, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") RegistrationStatus status);

    /**
     * Update registration priority
     */
    @Query("UPDATE Registration r SET r.priorityLevel = :priority, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    void updatePriority(@Param("id") UUID id, @Param("priority") Integer priority);

    /**
     * Assign registration to user
     */
    @Query("UPDATE Registration r SET r.assignedTo.id = :userId, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    void assignToUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
