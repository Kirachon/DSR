package ph.gov.dsr.datamanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.datamanagement.entity.HouseholdMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for HouseholdMember entity in Data Management Service
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, UUID> {

    /**
     * Find all members of a specific household
     */
    List<HouseholdMember> findByHouseholdId(UUID householdId);

    /**
     * Find household member by PSN (PhilSys Number)
     */
    Optional<HouseholdMember> findByPsn(String psn);

    /**
     * Check if PSN exists
     */
    boolean existsByPsn(String psn);

    /**
     * Find head of household by household ID
     */
    @Query("SELECT m FROM HouseholdMember m WHERE m.household.id = :householdId AND m.isHeadOfHousehold = true")
    Optional<HouseholdMember> findHeadOfHouseholdByHouseholdId(@Param("householdId") UUID householdId);

    /**
     * Find members by relationship to head
     */
    List<HouseholdMember> findByRelationshipToHead(String relationshipToHead);

    /**
     * Find members by gender
     */
    List<HouseholdMember> findByGender(String gender);

    /**
     * Find members by age range
     */
    @Query("SELECT m FROM HouseholdMember m WHERE " +
           "m.birthDate BETWEEN :startDate AND :endDate")
    List<HouseholdMember> findByAgeRange(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Find minors (under 18)
     */
    @Query("SELECT m FROM HouseholdMember m WHERE " +
           "m.birthDate > :eighteenYearsAgo")
    List<HouseholdMember> findMinors(@Param("eighteenYearsAgo") LocalDate eighteenYearsAgo);

    /**
     * Find senior citizens (60 and above)
     */
    @Query("SELECT m FROM HouseholdMember m WHERE " +
           "m.birthDate <= :sixtyYearsAgo")
    List<HouseholdMember> findSeniorCitizens(@Param("sixtyYearsAgo") LocalDate sixtyYearsAgo);

    /**
     * Find members with disabilities
     */
    List<HouseholdMember> findByIsPwdTrue();

    /**
     * Find indigenous members
     */
    List<HouseholdMember> findByIsIndigenousTrue();

    /**
     * Find solo parents
     */
    List<HouseholdMember> findByIsSoloParentTrue();

    /**
     * Find OFWs (Overseas Filipino Workers)
     */
    List<HouseholdMember> findByIsOfwTrue();

    /**
     * Find pregnant members
     */
    List<HouseholdMember> findByIsPregnantTrue();

    /**
     * Find lactating members
     */
    List<HouseholdMember> findByIsLactatingTrue();

    /**
     * Find members by employment status
     */
    List<HouseholdMember> findByEmploymentStatus(String employmentStatus);

    /**
     * Find members by education level
     */
    List<HouseholdMember> findByEducationLevel(String educationLevel);

    /**
     * Find members by civil status
     */
    List<HouseholdMember> findByCivilStatus(String civilStatus);

    /**
     * Find members by source system
     */
    List<HouseholdMember> findBySourceSystem(String sourceSystem);

    /**
     * Find members with income
     */
    @Query("SELECT m FROM HouseholdMember m WHERE m.monthlyIncome > 0")
    List<HouseholdMember> findMembersWithIncome();

    /**
     * Find members without PSN
     */
    @Query("SELECT m FROM HouseholdMember m WHERE m.psn IS NULL OR m.psn = ''")
    List<HouseholdMember> findMembersWithoutPsn();

    /**
     * Find vulnerable members
     */
    @Query("SELECT m FROM HouseholdMember m WHERE " +
           "m.isPwd = true OR m.isIndigenous = true OR m.isSoloParent = true OR " +
           "m.isSeniorCitizen = true OR m.isPregnant = true OR m.isLactating = true OR " +
           "m.birthDate > :eighteenYearsAgo")
    List<HouseholdMember> findVulnerableMembers(@Param("eighteenYearsAgo") LocalDate eighteenYearsAgo);

    /**
     * Count members by household
     */
    @Query("SELECT m.household.id, COUNT(m) FROM HouseholdMember m GROUP BY m.household.id")
    List<Object[]> countMembersByHousehold();

    /**
     * Count members by gender
     */
    @Query("SELECT m.gender, COUNT(m) FROM HouseholdMember m GROUP BY m.gender")
    List<Object[]> countMembersByGender();

    /**
     * Count members by age group
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN m.birthDate > :eighteenYearsAgo THEN 'MINOR' " +
           "  WHEN m.birthDate BETWEEN :sixtyYearsAgo AND :eighteenYearsAgo THEN 'ADULT' " +
           "  ELSE 'SENIOR' " +
           "END as ageGroup, COUNT(m) " +
           "FROM HouseholdMember m " +
           "GROUP BY " +
           "CASE " +
           "  WHEN m.birthDate > :eighteenYearsAgo THEN 'MINOR' " +
           "  WHEN m.birthDate BETWEEN :sixtyYearsAgo AND :eighteenYearsAgo THEN 'ADULT' " +
           "  ELSE 'SENIOR' " +
           "END")
    List<Object[]> countMembersByAgeGroup(@Param("eighteenYearsAgo") LocalDate eighteenYearsAgo,
                                         @Param("sixtyYearsAgo") LocalDate sixtyYearsAgo);

    /**
     * Find members by name (fuzzy search)
     */
    @Query("SELECT m FROM HouseholdMember m WHERE " +
           "LOWER(m.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(m.middleName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<HouseholdMember> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find members with health conditions
     */
    @Query("SELECT m FROM HouseholdMember m WHERE SIZE(m.healthConditions) > 0")
    List<HouseholdMember> findMembersWithHealthConditions();

    /**
     * Find members by specific health condition
     */
    @Query("SELECT m FROM HouseholdMember m JOIN m.healthConditions hc WHERE hc = :condition")
    List<HouseholdMember> findMembersByHealthCondition(@Param("condition") String condition);

    /**
     * Find duplicate members by PSN
     */
    @Query("SELECT m.psn, COUNT(m) FROM HouseholdMember m " +
           "WHERE m.psn IS NOT NULL " +
           "GROUP BY m.psn HAVING COUNT(m) > 1")
    List<Object[]> findDuplicateMembersByPsn();

    /**
     * Find members with data quality issues
     */
    @Query("SELECT m FROM HouseholdMember m WHERE " +
           "m.firstName IS NULL OR m.firstName = '' OR " +
           "m.lastName IS NULL OR m.lastName = '' OR " +
           "m.birthDate IS NULL OR " +
           "m.gender IS NULL OR m.gender = ''")
    List<HouseholdMember> findMembersWithDataQualityIssues();

    /**
     * Find members by multiple criteria
     */
    @Query("SELECT m FROM HouseholdMember m WHERE " +
           "(:gender IS NULL OR m.gender = :gender) AND " +
           "(:civilStatus IS NULL OR m.civilStatus = :civilStatus) AND " +
           "(:employmentStatus IS NULL OR m.employmentStatus = :employmentStatus) AND " +
           "(:educationLevel IS NULL OR m.educationLevel = :educationLevel) AND " +
           "(:relationshipToHead IS NULL OR m.relationshipToHead = :relationshipToHead)")
    Page<HouseholdMember> findMembersByCriteria(@Param("gender") String gender,
                                               @Param("civilStatus") String civilStatus,
                                               @Param("employmentStatus") String employmentStatus,
                                               @Param("educationLevel") String educationLevel,
                                               @Param("relationshipToHead") String relationshipToHead,
                                               Pageable pageable);

    /**
     * Get member statistics
     */
    @Query("SELECT COUNT(m), " +
           "COUNT(CASE WHEN m.gender = 'MALE' THEN 1 END), " +
           "COUNT(CASE WHEN m.gender = 'FEMALE' THEN 1 END), " +
           "COUNT(CASE WHEN m.isPwd = true THEN 1 END), " +
           "COUNT(CASE WHEN m.isIndigenous = true THEN 1 END), " +
           "COUNT(CASE WHEN m.isSoloParent = true THEN 1 END) " +
           "FROM HouseholdMember m")
    Object[] getMemberStatistics();
}
