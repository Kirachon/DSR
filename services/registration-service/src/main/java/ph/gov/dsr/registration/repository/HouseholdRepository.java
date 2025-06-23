package ph.gov.dsr.registration.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.registration.entity.Household;
import ph.gov.dsr.registration.entity.RegistrationChannel;
import ph.gov.dsr.registration.entity.RegistrationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Household entity
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
    List<Household> findByStatus(RegistrationStatus status);

    /**
     * Find households by registration channel
     */
    List<Household> findByRegistrationChannel(RegistrationChannel channel);

    /**
     * Find households by status and channel
     */
    List<Household> findByStatusAndRegistrationChannel(RegistrationStatus status, RegistrationChannel channel);

    /**
     * Find households registered within date range
     */
    @Query("SELECT h FROM Household h WHERE h.registrationDate BETWEEN :startDate AND :endDate")
    List<Household> findByRegistrationDateBetween(@Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);

    /**
     * Find households by total members range
     */
    @Query("SELECT h FROM Household h WHERE h.totalMembers BETWEEN :minMembers AND :maxMembers")
    List<Household> findByTotalMembersBetween(@Param("minMembers") Integer minMembers, 
                                             @Param("maxMembers") Integer maxMembers);

    /**
     * Find households by monthly income range
     */
    @Query("SELECT h FROM Household h WHERE h.monthlyIncome BETWEEN :minIncome AND :maxIncome")
    List<Household> findByMonthlyIncomeBetween(@Param("minIncome") BigDecimal minIncome, 
                                              @Param("maxIncome") BigDecimal maxIncome);

    /**
     * Find households below poverty line
     */
    @Query("SELECT h FROM Household h WHERE h.monthlyIncome < :povertyLine OR h.monthlyIncome IS NULL")
    List<Household> findBelowPovertyLine(@Param("povertyLine") BigDecimal povertyLine);

    /**
     * Find indigenous households
     */
    List<Household> findByIsIndigenousTrue();

    /**
     * Find PWD households
     */
    List<Household> findByIsPwdHouseholdTrue();

    /**
     * Find senior citizen households
     */
    List<Household> findByIsSeniorCitizenHouseholdTrue();

    /**
     * Find households with consent given
     */
    List<Household> findByConsentGivenTrue();

    /**
     * Find households without consent
     */
    List<Household> findByConsentGivenFalseOrConsentGivenIsNull();

    /**
     * Search households by household number or head PSN
     */
    @Query("SELECT h FROM Household h WHERE " +
           "h.householdNumber LIKE %:searchTerm% OR " +
           "h.headOfHouseholdPsn LIKE %:searchTerm%")
    Page<Household> searchHouseholds(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find households created by specific user
     */
    List<Household> findByCreatedBy(UUID createdBy);

    /**
     * Find households updated by specific user
     */
    List<Household> findByUpdatedBy(UUID updatedBy);

    /**
     * Count households by status
     */
    @Query("SELECT h.status, COUNT(h) FROM Household h GROUP BY h.status")
    List<Object[]> countHouseholdsByStatus();

    /**
     * Count households by registration channel
     */
    @Query("SELECT h.registrationChannel, COUNT(h) FROM Household h GROUP BY h.registrationChannel")
    List<Object[]> countHouseholdsByChannel();

    /**
     * Find households registered today
     */
    @Query("SELECT h FROM Household h WHERE h.registrationDate = CURRENT_DATE")
    List<Household> findHouseholdsRegisteredToday();

    /**
     * Find households registered this month
     */
    @Query("SELECT h FROM Household h WHERE EXTRACT(YEAR FROM h.registrationDate) = EXTRACT(YEAR FROM CURRENT_DATE) " +
           "AND EXTRACT(MONTH FROM h.registrationDate) = EXTRACT(MONTH FROM CURRENT_DATE)")
    List<Household> findHouseholdsRegisteredThisMonth();

    /**
     * Find households registered this year
     */
    @Query("SELECT h FROM Household h WHERE EXTRACT(YEAR FROM h.registrationDate) = EXTRACT(YEAR FROM CURRENT_DATE)")
    List<Household> findHouseholdsRegisteredThisYear();

    /**
     * Get average household size
     */
    @Query("SELECT AVG(h.totalMembers) FROM Household h WHERE h.totalMembers > 0")
    Double getAverageHouseholdSize();

    /**
     * Get average monthly income
     */
    @Query("SELECT AVG(h.monthlyIncome) FROM Household h WHERE h.monthlyIncome IS NOT NULL")
    BigDecimal getAverageMonthlyIncome();

    /**
     * Find households by preferred language
     */
    List<Household> findByPreferredLanguage(String language);

    /**
     * Find households with specific characteristics
     */
    @Query("SELECT h FROM Household h WHERE " +
           "(:isIndigenous IS NULL OR h.isIndigenous = :isIndigenous) AND " +
           "(:isPwd IS NULL OR h.isPwdHousehold = :isPwd) AND " +
           "(:isSenior IS NULL OR h.isSeniorCitizenHousehold = :isSenior) AND " +
           "(:minIncome IS NULL OR h.monthlyIncome >= :minIncome) AND " +
           "(:maxIncome IS NULL OR h.monthlyIncome <= :maxIncome) AND " +
           "(:minMembers IS NULL OR h.totalMembers >= :minMembers) AND " +
           "(:maxMembers IS NULL OR h.totalMembers <= :maxMembers)")
    Page<Household> findHouseholdsWithCriteria(
        @Param("isIndigenous") Boolean isIndigenous,
        @Param("isPwd") Boolean isPwd,
        @Param("isSenior") Boolean isSenior,
        @Param("minIncome") BigDecimal minIncome,
        @Param("maxIncome") BigDecimal maxIncome,
        @Param("minMembers") Integer minMembers,
        @Param("maxMembers") Integer maxMembers,
        Pageable pageable
    );

    /**
     * Find households needing verification
     */
    @Query("SELECT h FROM Household h WHERE h.status IN ('DRAFT', 'PENDING_VERIFICATION')")
    List<Household> findHouseholdsNeedingVerification();

    /**
     * Find households pending approval
     */
    @Query("SELECT h FROM Household h WHERE h.status = 'PENDING_APPROVAL'")
    List<Household> findHouseholdsPendingApproval();

    /**
     * Find approved households
     */
    @Query("SELECT h FROM Household h WHERE h.status = 'APPROVED'")
    List<Household> findApprovedHouseholds();

    /**
     * Generate next household number
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(h.householdNumber, 4) AS INTEGER)), 0) + 1 " +
           "FROM Household h WHERE h.householdNumber LIKE 'HH-%'")
    Integer getNextHouseholdNumber();

    /**
     * Find households by region (through address)
     */
    @Query("SELECT DISTINCT h FROM Household h " +
           "JOIN h.addresses a WHERE a.region = :region AND a.isCurrent = true")
    List<Household> findByRegion(@Param("region") String region);

    /**
     * Find households by province (through address)
     */
    @Query("SELECT DISTINCT h FROM Household h " +
           "JOIN h.addresses a WHERE a.province = :province AND a.isCurrent = true")
    List<Household> findByProvince(@Param("province") String province);

    /**
     * Find households by municipality (through address)
     */
    @Query("SELECT DISTINCT h FROM Household h " +
           "JOIN h.addresses a WHERE a.municipality = :municipality AND a.isCurrent = true")
    List<Household> findByMunicipality(@Param("municipality") String municipality);
}
