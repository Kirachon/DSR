package ph.gov.dsr.datamanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Economic profile entity for Data Management Service
 * Represents economic assessment data for households
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "economic_profiles", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class EconomicProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "household_id", nullable = false, columnDefinition = "UUID")
    private UUID householdId;

    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate = LocalDateTime.now();

    @Column(name = "total_household_income", precision = 12, scale = 2)
    private BigDecimal totalHouseholdIncome;

    @Column(name = "per_capita_income", precision = 12, scale = 2)
    private BigDecimal perCapitaIncome;

    @Column(name = "total_assets_value", precision = 12, scale = 2)
    private BigDecimal totalAssetsValue;

    @Column(name = "total_monthly_expenses", precision = 12, scale = 2)
    private BigDecimal totalMonthlyExpenses;

    @Column(name = "food_expenses", precision = 10, scale = 2)
    private BigDecimal foodExpenses;

    @Column(name = "housing_expenses", precision = 10, scale = 2)
    private BigDecimal housingExpenses;

    @Column(name = "education_expenses", precision = 10, scale = 2)
    private BigDecimal educationExpenses;

    @Column(name = "health_expenses", precision = 10, scale = 2)
    private BigDecimal healthExpenses;

    @Column(name = "transportation_expenses", precision = 10, scale = 2)
    private BigDecimal transportationExpenses;

    @Column(name = "other_expenses", precision = 10, scale = 2)
    private BigDecimal otherExpenses;

    // Income sources
    @Column(name = "has_salary_income")
    private Boolean hasSalaryIncome = false;

    @Column(name = "has_business_income")
    private Boolean hasBusinessIncome = false;

    @Column(name = "has_agricultural_income")
    private Boolean hasAgriculturalIncome = false;

    @Column(name = "has_remittance_income")
    private Boolean hasRemittanceIncome = false;

    @Column(name = "has_pension_income")
    private Boolean hasPensionIncome = false;

    @Column(name = "has_other_income")
    private Boolean hasOtherIncome = false;

    // Assets
    @Column(name = "owns_house")
    private Boolean ownsHouse = false;

    @Column(name = "owns_land")
    private Boolean ownsLand = false;

    @Column(name = "owns_vehicle")
    private Boolean ownsVehicle = false;

    @Column(name = "owns_livestock")
    private Boolean ownsLivestock = false;

    @Column(name = "has_savings")
    private Boolean hasSavings = false;

    @Column(name = "has_appliances")
    private Boolean hasAppliances = false;

    // Vulnerability indicators
    @Column(name = "debt_to_income_ratio", precision = 5, scale = 2)
    private BigDecimal debtToIncomeRatio;

    @Column(name = "food_security_score", precision = 5, scale = 2)
    private BigDecimal foodSecurityScore;

    @Column(name = "housing_adequacy_score", precision = 5, scale = 2)
    private BigDecimal housingAdequacyScore;

    @Column(name = "economic_vulnerability_score", precision = 5, scale = 2)
    private BigDecimal economicVulnerabilityScore;

    // PMT-related fields
    @Column(name = "pmt_score", precision = 10, scale = 4)
    private BigDecimal pmtScore;

    @Column(name = "poverty_threshold", precision = 10, scale = 2)
    private BigDecimal povertyThreshold;

    @Column(name = "is_poor")
    private Boolean isPoor = false;

    @Column(name = "poverty_gap", precision = 10, scale = 2)
    private BigDecimal povertyGap;

    // Additional data as JSON
    @Column(name = "additional_data", columnDefinition = "JSONB")
    private String additionalData;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "assessment_method", length = 50)
    private String assessmentMethod; // SURVEY, ADMINISTRATIVE, HYBRID

    @Column(name = "assessor_id", length = 100)
    private String assessorId;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public EconomicProfile() {}

    public EconomicProfile(UUID householdId) {
        this.householdId = householdId;
    }

    // Helper methods

    /**
     * Calculate per capita income based on total income and household size
     */
    public void calculatePerCapitaIncome(int householdSize) {
        if (totalHouseholdIncome != null && householdSize > 0) {
            this.perCapitaIncome = totalHouseholdIncome.divide(
                BigDecimal.valueOf(householdSize), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Calculate poverty gap if household is poor
     */
    public void calculatePovertyGap() {
        if (isPoor && povertyThreshold != null && perCapitaIncome != null) {
            this.povertyGap = povertyThreshold.subtract(perCapitaIncome);
            if (this.povertyGap.compareTo(BigDecimal.ZERO) < 0) {
                this.povertyGap = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Calculate economic vulnerability score based on various indicators
     */
    public void calculateVulnerabilityScore() {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;

        // Income-based factors
        if (debtToIncomeRatio != null) {
            score = score.add(debtToIncomeRatio.multiply(BigDecimal.valueOf(0.3)));
            factors++;
        }

        // Food security
        if (foodSecurityScore != null) {
            score = score.add(foodSecurityScore.multiply(BigDecimal.valueOf(0.25)));
            factors++;
        }

        // Housing adequacy
        if (housingAdequacyScore != null) {
            score = score.add(housingAdequacyScore.multiply(BigDecimal.valueOf(0.25)));
            factors++;
        }

        // Asset ownership (inverse relationship)
        BigDecimal assetScore = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(ownsHouse)) assetScore = assetScore.add(BigDecimal.valueOf(0.3));
        if (Boolean.TRUE.equals(ownsLand)) assetScore = assetScore.add(BigDecimal.valueOf(0.2));
        if (Boolean.TRUE.equals(ownsVehicle)) assetScore = assetScore.add(BigDecimal.valueOf(0.15));
        if (Boolean.TRUE.equals(hasSavings)) assetScore = assetScore.add(BigDecimal.valueOf(0.15));

        // Higher asset ownership reduces vulnerability
        score = score.add(BigDecimal.ONE.subtract(assetScore).multiply(BigDecimal.valueOf(0.2)));
        factors++;

        if (factors > 0) {
            this.economicVulnerabilityScore = score.divide(BigDecimal.valueOf(factors), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Check if household meets poverty criteria
     */
    public void assessPovertyStatus() {
        if (perCapitaIncome != null && povertyThreshold != null) {
            this.isPoor = perCapitaIncome.compareTo(povertyThreshold) <= 0;
            calculatePovertyGap();
        }
    }

    /**
     * Get income diversification score
     */
    public int getIncomeDiversificationScore() {
        int sources = 0;
        if (Boolean.TRUE.equals(hasSalaryIncome)) sources++;
        if (Boolean.TRUE.equals(hasBusinessIncome)) sources++;
        if (Boolean.TRUE.equals(hasAgriculturalIncome)) sources++;
        if (Boolean.TRUE.equals(hasRemittanceIncome)) sources++;
        if (Boolean.TRUE.equals(hasPensionIncome)) sources++;
        if (Boolean.TRUE.equals(hasOtherIncome)) sources++;
        return sources;
    }
}
