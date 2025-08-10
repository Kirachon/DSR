package ph.gov.dsr.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.registration.entity.HouseholdMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for HouseholdMember entity operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-22
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
     * Find head of household for a specific household
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.household.id = :householdId AND hm.relationshipToHead = 'HEAD'")
    Optional<HouseholdMember> findHeadOfHousehold(@Param("householdId") UUID householdId);

    /**
     * Find all members with specific relationship to head
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.household.id = :householdId AND hm.relationshipToHead = :relationship")
    List<HouseholdMember> findByHouseholdIdAndRelationshipToHead(@Param("householdId") UUID householdId, 
                                                                 @Param("relationship") String relationship);

    /**
     * Find members by age range
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.birthDate BETWEEN :startDate AND :endDate")
    List<HouseholdMember> findByAgeRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find all PWD members
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.isPwd = true")
    List<HouseholdMember> findAllPwdMembers();

    /**
     * Find all senior citizen members (age 60 and above)
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM hm.birthDate) >= 60")
    List<HouseholdMember> findAllSeniorCitizenMembers();

    /**
     * Find all indigenous members
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.isIndigenous = true")
    List<HouseholdMember> findAllIndigenousMembers();

    /**
     * Find members by employment status
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.employmentStatus = :status")
    List<HouseholdMember> findByEmploymentStatus(@Param("status") String status);

    /**
     * Find members by education level
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.educationLevel = :level")
    List<HouseholdMember> findByEducationLevel(@Param("level") String level);

    /**
     * Count members in a household
     */
    @Query("SELECT COUNT(hm) FROM HouseholdMember hm WHERE hm.household.id = :householdId")
    Long countByHouseholdId(@Param("householdId") UUID householdId);

    /**
     * Find members with health conditions
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.healthConditions IS NOT NULL AND SIZE(hm.healthConditions) > 0")
    List<HouseholdMember> findMembersWithHealthConditions();

    // Note: Pregnancy tracking method removed as isPregnant field
    // does not exist in the current HouseholdMember entity structure.
    // If needed in the future, add isPregnant boolean field to the HouseholdMember entity first.

    // Note: Lactation tracking method removed as isLactating field
    // does not exist in the current HouseholdMember entity structure.
    // If needed in the future, add isLactating boolean field to the HouseholdMember entity first.

    /**
     * Find solo parent members
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.isSoloParent = true")
    List<HouseholdMember> findSoloParentMembers();

    /**
     * Find members by gender
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.gender = :gender")
    List<HouseholdMember> findByGender(@Param("gender") String gender);

    /**
     * Find members by civil status
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.civilStatus = :status")
    List<HouseholdMember> findByCivilStatus(@Param("status") String status);

    /**
     * Find members with income
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.monthlyIncome IS NOT NULL AND hm.monthlyIncome > 0")
    List<HouseholdMember> findMembersWithIncome();

    /**
     * Find members by name (case-insensitive search)
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE " +
           "LOWER(hm.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(hm.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(hm.middleName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<HouseholdMember> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find members updated after specific date
     */
    @Query("SELECT hm FROM HouseholdMember hm WHERE hm.updatedAt > :date")
    List<HouseholdMember> findByUpdatedAtAfter(@Param("date") LocalDate date);

    /**
     * Get statistics for household members
     */
    @Query("SELECT " +
           "COUNT(hm) as totalMembers, " +
           "COUNT(CASE WHEN hm.gender = 'MALE' THEN 1 END) as maleMembers, " +
           "COUNT(CASE WHEN hm.gender = 'FEMALE' THEN 1 END) as femaleMembers, " +
           "COUNT(CASE WHEN hm.isPwd = true THEN 1 END) as pwdMembers, " +
           "COUNT(CASE WHEN EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM hm.birthDate) >= 60 THEN 1 END) as seniorMembers " +
           "FROM HouseholdMember hm WHERE hm.household.id = :householdId")
    Object getHouseholdMemberStatistics(@Param("householdId") UUID householdId);
}
