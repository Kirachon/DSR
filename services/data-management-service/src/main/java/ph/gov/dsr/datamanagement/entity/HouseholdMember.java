package ph.gov.dsr.datamanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Household member entity for Data Management Service
 * Represents individual members of households ingested from various sources
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "household_members", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class HouseholdMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "psn", unique = true, length = 16)
    private String psn; // PhilSys Number

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 20)
    @Column(name = "suffix", length = 20)
    private String suffix;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    private String gender; // MALE, FEMALE, OTHER

    @Column(name = "civil_status", length = 20)
    private String civilStatus; // SINGLE, MARRIED, WIDOWED, SEPARATED, DIVORCED

    @Column(name = "relationship_to_head", length = 30)
    private String relationshipToHead; // HEAD, SPOUSE, CHILD, PARENT, SIBLING, OTHER

    @Column(name = "is_head_of_household")
    private Boolean isHeadOfHousehold = false;

    @Column(name = "education_level", length = 50)
    private String educationLevel;

    @Column(name = "employment_status", length = 30)
    private String employmentStatus;

    @Size(max = 100)
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "monthly_income", precision = 10, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "is_pwd")
    private Boolean isPwd = false;

    @Size(max = 100)
    @Column(name = "pwd_type", length = 100)
    private String pwdType;

    @Column(name = "is_indigenous")
    private Boolean isIndigenous = false;

    @Size(max = 100)
    @Column(name = "indigenous_group", length = 100)
    private String indigenousGroup;

    @Column(name = "is_solo_parent")
    private Boolean isSoloParent = false;

    @Column(name = "is_ofw")
    private Boolean isOfw = false;

    @Column(name = "is_senior_citizen")
    private Boolean isSeniorCitizen = false;

    @Column(name = "is_pregnant")
    private Boolean isPregnant = false;

    @Column(name = "is_lactating")
    private Boolean isLactating = false;

    @ElementCollection
    @CollectionTable(
        name = "member_health_conditions",
        schema = "dsr_core",
        joinColumns = @JoinColumn(name = "household_member_id")
    )
    @Column(name = "condition")
    private List<String> healthConditions;

    @Column(name = "source_system", length = 50)
    private String sourceSystem; // LISTAHANAN, I_REGISTRO, MANUAL_ENTRY

    @Column(name = "source_record_id", length = 100)
    private String sourceRecordId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public HouseholdMember() {}

    public HouseholdMember(String firstName, String lastName, LocalDate birthDate, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    // Helper methods
    
    /**
     * Get full name of the member
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(firstName);
        
        if (middleName != null && !middleName.trim().isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        
        fullName.append(" ").append(lastName);
        
        if (suffix != null && !suffix.trim().isEmpty()) {
            fullName.append(" ").append(suffix);
        }
        
        return fullName.toString();
    }

    /**
     * Calculate age based on birth date
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    /**
     * Check if member is a minor (under 18)
     */
    public boolean isMinor() {
        Integer age = getAge();
        return age != null && age < 18;
    }

    /**
     * Check if member is vulnerable
     */
    public boolean isVulnerable() {
        return isPwd || isIndigenous || isSoloParent || isSeniorCitizen || 
               isPregnant || isLactating || isMinor();
    }

    /**
     * Check if member has income
     */
    public boolean hasIncome() {
        return monthlyIncome != null && monthlyIncome.compareTo(BigDecimal.ZERO) > 0;
    }
}
