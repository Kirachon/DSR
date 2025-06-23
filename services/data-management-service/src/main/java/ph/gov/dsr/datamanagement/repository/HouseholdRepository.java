package ph.gov.dsr.datamanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.datamanagement.entity.Household;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Household entity in Data Management Service
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface HouseholdRepository extends JpaRepository<Household, UUID> {

    /**
     * Find household by household number
     */
    Optional<Household> findByHouseholdNumber(String householdNumber);

    /**
     * Check if household number exists
     */
    boolean existsByHouseholdNumber(String householdNumber);

    /**
     * Find household by head of household PSN
     */
    Optional<Household> findByHeadOfHouseholdPsn(String headOfHouseholdPsn);

    /**
     * Find households by status
     */
    List<Household> findByStatus(String status);

    /**
     * Find households by source system
     */
    List<Household> findBySourceSystem(String sourceSystem);

    /**
     * Find households by region
     */
    List<Household> findByRegion(String region);

    /**
     * Find households by province
     */
    List<Household> findByProvince(String province);

    /**
     * Find households by municipality
     */
    List<Household> findByMunicipality(String municipality);

    /**
     * Find households by barangay
     */
    List<Household> findByBarangay(String barangay);

    /**
     * Find households with income below threshold
     */
    @Query("SELECT h FROM Household h WHERE h.monthlyIncome <= :threshold")
    List<Household> findHouseholdsWithIncomeBelowThreshold(@Param("threshold") BigDecimal threshold);

    /**
     * Find households with income between range
     */
    @Query("SELECT h FROM Household h WHERE h.monthlyIncome BETWEEN :minIncome AND :maxIncome")
    List<Household> findHouseholdsWithIncomeInRange(@Param("minIncome") BigDecimal minIncome, 
                                                   @Param("maxIncome") BigDecimal maxIncome);

    /**
     * Find vulnerable households
     */
    @Query("SELECT h FROM Household h WHERE h.isIndigenous = true OR h.isPwdHousehold = true " +
           "OR h.isSeniorCitizenHousehold = true OR h.isSoloParentHousehold = true")
    List<Household> findVulnerableHouseholds();

    /**
     * Find households by multiple criteria
     */
    @Query("SELECT h FROM Household h WHERE " +
           "(:region IS NULL OR h.region = :region) AND " +
           "(:province IS NULL OR h.province = :province) AND " +
           "(:municipality IS NULL OR h.municipality = :municipality) AND " +
           "(:status IS NULL OR h.status = :status) AND " +
           "(:sourceSystem IS NULL OR h.sourceSystem = :sourceSystem)")
    Page<Household> findHouseholdsByCriteria(@Param("region") String region,
                                           @Param("province") String province,
                                           @Param("municipality") String municipality,
                                           @Param("status") String status,
                                           @Param("sourceSystem") String sourceSystem,
                                           Pageable pageable);

    /**
     * Count households by source system
     */
    @Query("SELECT h.sourceSystem, COUNT(h) FROM Household h GROUP BY h.sourceSystem")
    List<Object[]> countHouseholdsBySourceSystem();

    /**
     * Count households by region
     */
    @Query("SELECT h.region, COUNT(h) FROM Household h GROUP BY h.region")
    List<Object[]> countHouseholdsByRegion();

    /**
     * Find households created within date range
     */
    @Query("SELECT h FROM Household h WHERE h.createdAt BETWEEN :startDate AND :endDate")
    List<Household> findHouseholdsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Find households with specific member count
     */
    @Query("SELECT h FROM Household h WHERE h.totalMembers = :memberCount")
    List<Household> findHouseholdsWithMemberCount(@Param("memberCount") Integer memberCount);

    /**
     * Find households with member count in range
     */
    @Query("SELECT h FROM Household h WHERE h.totalMembers BETWEEN :minMembers AND :maxMembers")
    List<Household> findHouseholdsWithMemberCountInRange(@Param("minMembers") Integer minMembers,
                                                        @Param("maxMembers") Integer maxMembers);

    /**
     * Find households without PSN for head
     */
    @Query("SELECT h FROM Household h WHERE h.headOfHouseholdPsn IS NULL OR h.headOfHouseholdPsn = ''")
    List<Household> findHouseholdsWithoutHeadPsn();

    /**
     * Find households by housing characteristics
     */
    @Query("SELECT h FROM Household h WHERE " +
           "(:housingType IS NULL OR h.housingType = :housingType) AND " +
           "(:housingTenure IS NULL OR h.housingTenure = :housingTenure) AND " +
           "(:waterSource IS NULL OR h.waterSource = :waterSource)")
    List<Household> findHouseholdsByHousingCharacteristics(@Param("housingType") String housingType,
                                                          @Param("housingTenure") String housingTenure,
                                                          @Param("waterSource") String waterSource);

    /**
     * Get household statistics by region
     */
    @Query("SELECT h.region, COUNT(h), AVG(h.totalMembers), AVG(h.monthlyIncome) " +
           "FROM Household h WHERE h.region IS NOT NULL " +
           "GROUP BY h.region ORDER BY h.region")
    List<Object[]> getHouseholdStatisticsByRegion();

    /**
     * Find households for data quality checks
     */
    @Query("SELECT h FROM Household h WHERE " +
           "h.headOfHouseholdPsn IS NULL OR " +
           "h.totalMembers = 0 OR " +
           "h.region IS NULL OR " +
           "h.province IS NULL")
    List<Household> findHouseholdsWithDataQualityIssues();

    /**
     * Find duplicate households by head PSN
     */
    @Query("SELECT h.headOfHouseholdPsn, COUNT(h) FROM Household h " +
           "WHERE h.headOfHouseholdPsn IS NOT NULL " +
           "GROUP BY h.headOfHouseholdPsn HAVING COUNT(h) > 1")
    List<Object[]> findDuplicateHouseholdsByHeadPsn();

    /**
     * Find households updated recently
     */
    @Query("SELECT h FROM Household h WHERE h.updatedAt >= :since ORDER BY h.updatedAt DESC")
    List<Household> findRecentlyUpdatedHouseholds(@Param("since") LocalDateTime since);

    /**
     * Search households by text (household number, head PSN, or location)
     */
    @Query("SELECT h FROM Household h WHERE " +
           "LOWER(h.householdNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(h.headOfHouseholdPsn) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(h.region) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(h.province) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(h.municipality) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(h.barangay) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<Household> searchHouseholds(@Param("searchText") String searchText, Pageable pageable);
}
