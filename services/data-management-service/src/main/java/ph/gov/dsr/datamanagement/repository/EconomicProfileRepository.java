package ph.gov.dsr.datamanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.datamanagement.entity.EconomicProfile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for EconomicProfile entity in Data Management Service
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface EconomicProfileRepository extends JpaRepository<EconomicProfile, UUID> {

    /**
     * Find economic profile by household ID
     */
    Optional<EconomicProfile> findByHouseholdId(UUID householdId);

    /**
     * Find all economic profiles by household IDs
     */
    List<EconomicProfile> findByHouseholdIdIn(List<UUID> householdIds);

    /**
     * Find profiles by verification status
     */
    List<EconomicProfile> findByVerificationStatus(String verificationStatus);

    /**
     * Find profiles by source system
     */
    List<EconomicProfile> findBySourceSystem(String sourceSystem);

    /**
     * Find profiles by assessment method
     */
    List<EconomicProfile> findByAssessmentMethod(String assessmentMethod);

    /**
     * Find poor households
     */
    List<EconomicProfile> findByIsPoorTrue();

    /**
     * Find non-poor households
     */
    List<EconomicProfile> findByIsPoorFalse();

    /**
     * Find profiles with income below threshold
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.totalHouseholdIncome <= :threshold")
    List<EconomicProfile> findProfilesWithIncomeBelow(@Param("threshold") BigDecimal threshold);

    /**
     * Find profiles with income in range
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.totalHouseholdIncome BETWEEN :minIncome AND :maxIncome")
    List<EconomicProfile> findProfilesWithIncomeInRange(@Param("minIncome") BigDecimal minIncome,
                                                        @Param("maxIncome") BigDecimal maxIncome);

    /**
     * Find profiles with per capita income below threshold
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.perCapitaIncome <= :threshold")
    List<EconomicProfile> findProfilesWithPerCapitaIncomeBelow(@Param("threshold") BigDecimal threshold);

    /**
     * Find profiles with PMT score below threshold
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.pmtScore <= :threshold")
    List<EconomicProfile> findProfilesWithPmtScoreBelow(@Param("threshold") BigDecimal threshold);

    /**
     * Find profiles with high vulnerability scores
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.economicVulnerabilityScore >= :threshold")
    List<EconomicProfile> findHighVulnerabilityProfiles(@Param("threshold") BigDecimal threshold);

    /**
     * Find profiles with debt issues
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.debtToIncomeRatio >= :threshold")
    List<EconomicProfile> findProfilesWithHighDebtRatio(@Param("threshold") BigDecimal threshold);

    /**
     * Find profiles with food security issues
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.foodSecurityScore <= :threshold")
    List<EconomicProfile> findProfilesWithFoodInsecurity(@Param("threshold") BigDecimal threshold);

    /**
     * Find profiles with housing adequacy issues
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.housingAdequacyScore <= :threshold")
    List<EconomicProfile> findProfilesWithHousingInadequacy(@Param("threshold") BigDecimal threshold);

    /**
     * Find profiles with multiple income sources
     */
    @Query("SELECT e FROM EconomicProfile e WHERE " +
           "(CASE WHEN e.hasSalaryIncome = true THEN 1 ELSE 0 END) + " +
           "(CASE WHEN e.hasBusinessIncome = true THEN 1 ELSE 0 END) + " +
           "(CASE WHEN e.hasAgriculturalIncome = true THEN 1 ELSE 0 END) + " +
           "(CASE WHEN e.hasRemittanceIncome = true THEN 1 ELSE 0 END) + " +
           "(CASE WHEN e.hasPensionIncome = true THEN 1 ELSE 0 END) + " +
           "(CASE WHEN e.hasOtherIncome = true THEN 1 ELSE 0 END) >= :minSources")
    List<EconomicProfile> findProfilesWithMultipleIncomeSources(@Param("minSources") int minSources);

    /**
     * Find profiles with asset ownership
     */
    @Query("SELECT e FROM EconomicProfile e WHERE " +
           "e.ownsHouse = true OR e.ownsLand = true OR e.ownsVehicle = true OR " +
           "e.ownsLivestock = true OR e.hasSavings = true OR e.hasAppliances = true")
    List<EconomicProfile> findProfilesWithAssets();

    /**
     * Find profiles without assets
     */
    @Query("SELECT e FROM EconomicProfile e WHERE " +
           "e.ownsHouse = false AND e.ownsLand = false AND e.ownsVehicle = false AND " +
           "e.ownsLivestock = false AND e.hasSavings = false AND e.hasAppliances = false")
    List<EconomicProfile> findProfilesWithoutAssets();

    /**
     * Find profiles assessed within date range
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.assessmentDate BETWEEN :startDate AND :endDate")
    List<EconomicProfile> findProfilesAssessedBetween(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find profiles needing verification
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.verificationStatus = 'PENDING' " +
           "AND e.assessmentDate <= :cutoffDate")
    List<EconomicProfile> findProfilesNeedingVerification(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find profiles by assessor
     */
    List<EconomicProfile> findByAssessorId(String assessorId);

    /**
     * Count profiles by verification status
     */
    @Query("SELECT e.verificationStatus, COUNT(e) FROM EconomicProfile e GROUP BY e.verificationStatus")
    List<Object[]> countProfilesByVerificationStatus();

    /**
     * Count profiles by poverty status
     */
    @Query("SELECT e.isPoor, COUNT(e) FROM EconomicProfile e GROUP BY e.isPoor")
    List<Object[]> countProfilesByPovertyStatus();

    /**
     * Count profiles by source system
     */
    @Query("SELECT e.sourceSystem, COUNT(e) FROM EconomicProfile e GROUP BY e.sourceSystem")
    List<Object[]> countProfilesBySourceSystem();

    /**
     * Get income statistics
     */
    @Query("SELECT MIN(e.totalHouseholdIncome), MAX(e.totalHouseholdIncome), " +
           "AVG(e.totalHouseholdIncome), MIN(e.perCapitaIncome), " +
           "MAX(e.perCapitaIncome), AVG(e.perCapitaIncome) " +
           "FROM EconomicProfile e WHERE e.totalHouseholdIncome IS NOT NULL")
    Object[] getIncomeStatistics();

    /**
     * Get PMT score statistics
     */
    @Query("SELECT MIN(e.pmtScore), MAX(e.pmtScore), AVG(e.pmtScore) " +
           "FROM EconomicProfile e WHERE e.pmtScore IS NOT NULL")
    Object[] getPmtScoreStatistics();

    /**
     * Get vulnerability score statistics
     */
    @Query("SELECT MIN(e.economicVulnerabilityScore), MAX(e.economicVulnerabilityScore), " +
           "AVG(e.economicVulnerabilityScore) " +
           "FROM EconomicProfile e WHERE e.economicVulnerabilityScore IS NOT NULL")
    Object[] getVulnerabilityScoreStatistics();

    /**
     * Find profiles with data quality issues
     */
    @Query("SELECT e FROM EconomicProfile e WHERE " +
           "e.totalHouseholdIncome IS NULL OR " +
           "e.perCapitaIncome IS NULL OR " +
           "e.pmtScore IS NULL OR " +
           "e.verificationStatus IS NULL")
    List<EconomicProfile> findProfilesWithDataQualityIssues();

    /**
     * Find profiles by multiple criteria
     */
    @Query("SELECT e FROM EconomicProfile e WHERE " +
           "(:verificationStatus IS NULL OR e.verificationStatus = :verificationStatus) AND " +
           "(:sourceSystem IS NULL OR e.sourceSystem = :sourceSystem) AND " +
           "(:assessmentMethod IS NULL OR e.assessmentMethod = :assessmentMethod) AND " +
           "(:isPoor IS NULL OR e.isPoor = :isPoor) AND " +
           "(:minIncome IS NULL OR e.totalHouseholdIncome >= :minIncome) AND " +
           "(:maxIncome IS NULL OR e.totalHouseholdIncome <= :maxIncome)")
    Page<EconomicProfile> findProfilesByCriteria(@Param("verificationStatus") String verificationStatus,
                                                 @Param("sourceSystem") String sourceSystem,
                                                 @Param("assessmentMethod") String assessmentMethod,
                                                 @Param("isPoor") Boolean isPoor,
                                                 @Param("minIncome") BigDecimal minIncome,
                                                 @Param("maxIncome") BigDecimal maxIncome,
                                                 Pageable pageable);

    /**
     * Find recently updated profiles
     */
    @Query("SELECT e FROM EconomicProfile e WHERE e.updatedAt >= :since ORDER BY e.updatedAt DESC")
    List<EconomicProfile> findRecentlyUpdatedProfiles(@Param("since") LocalDateTime since);

    /**
     * Find profiles for poverty analysis
     */
    @Query("SELECT e FROM EconomicProfile e WHERE " +
           "e.pmtScore IS NOT NULL AND e.povertyThreshold IS NOT NULL AND " +
           "e.perCapitaIncome IS NOT NULL AND e.verificationStatus = 'VERIFIED'")
    List<EconomicProfile> findProfilesForPovertyAnalysis();

    /**
     * Calculate average poverty gap for poor households
     */
    @Query("SELECT AVG(e.povertyGap) FROM EconomicProfile e WHERE e.isPoor = true AND e.povertyGap IS NOT NULL")
    BigDecimal calculateAveragePovertyGap();

    /**
     * Find profiles eligible for specific programs based on criteria
     */
    @Query("SELECT e FROM EconomicProfile e WHERE " +
           "e.isPoor = true AND e.verificationStatus = 'VERIFIED' AND " +
           "(:maxPmtScore IS NULL OR e.pmtScore <= :maxPmtScore) AND " +
           "(:maxPerCapitaIncome IS NULL OR e.perCapitaIncome <= :maxPerCapitaIncome)")
    List<EconomicProfile> findProfilesEligibleForPrograms(@Param("maxPmtScore") BigDecimal maxPmtScore,
                                                          @Param("maxPerCapitaIncome") BigDecimal maxPerCapitaIncome);
}
