package ph.gov.dsr.datamanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Household entity for Data Management Service
 * Represents household data ingested from various sources
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "households", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "household_number", unique = true, nullable = false, length = 50)
    private String householdNumber;

    @Column(name = "head_of_household_psn", length = 16)
    private String headOfHouseholdPsn;

    @NotNull
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem; // LISTAHANAN, I_REGISTRO, MANUAL_ENTRY

    @PositiveOrZero
    @Column(name = "total_members")
    private Integer totalMembers = 0;

    @Column(name = "monthly_income", precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "municipality", length = 100)
    private String municipality;

    @Column(name = "barangay", length = 100)
    private String barangay;

    @Column(name = "is_indigenous")
    private Boolean isIndigenous = false;

    @Column(name = "is_pwd_household")
    private Boolean isPwdHousehold = false;

    @Column(name = "is_senior_citizen_household")
    private Boolean isSeniorCitizenHousehold = false;

    @Column(name = "is_solo_parent_household")
    private Boolean isSoloParentHousehold = false;

    @Column(name = "housing_type", length = 50)
    private String housingType;

    @Column(name = "housing_tenure", length = 50)
    private String housingTenure;

    @Column(name = "water_source", length = 50)
    private String waterSource;

    @Column(name = "toilet_facility", length = 50)
    private String toiletFacility;

    @Column(name = "electricity_source", length = 50)
    private String electricitySource;

    @Column(name = "cooking_fuel", length = 50)
    private String cookingFuel;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HouseholdMember> members = new ArrayList<>();

    // Constructors
    public Household() {}

    public Household(String householdNumber) {
        this.householdNumber = householdNumber;
    }

    // Helper methods
    public void addMember(HouseholdMember member) {
        members.add(member);
        member.setHousehold(this);
        this.totalMembers = members.size();
    }

    public void removeMember(HouseholdMember member) {
        members.remove(member);
        member.setHousehold(null);
        this.totalMembers = members.size();
    }

    /**
     * Calculate total household income from all members
     */
    public BigDecimal calculateTotalIncome() {
        BigDecimal total = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;
        
        for (HouseholdMember member : members) {
            if (member.getMonthlyIncome() != null) {
                total = total.add(member.getMonthlyIncome());
            }
        }
        
        return total;
    }

    /**
     * Check if household has vulnerable members
     */
    public boolean hasVulnerableMembers() {
        return isIndigenous || isPwdHousehold || isSeniorCitizenHousehold || isSoloParentHousehold;
    }

    /**
     * Get head of household member
     */
    public HouseholdMember getHeadOfHousehold() {
        return members.stream()
                .filter(member -> Boolean.TRUE.equals(member.getIsHeadOfHousehold()))
                .findFirst()
                .orElse(null);
    }
}
