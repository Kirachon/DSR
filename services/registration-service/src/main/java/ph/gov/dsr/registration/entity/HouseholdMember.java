package ph.gov.dsr.registration.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Household member entity representing an individual in a household
 */
@Entity
@Table(name = "household_members", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
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

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 100)
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Size(max = 20)
    @Column(name = "suffix", length = 20)
    private String suffix;

    @NotNull
    @Past
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "civil_status", nullable = false)
    private CivilStatus civilStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_to_head", nullable = false)
    private RelationshipType relationshipToHead;

    @Column(name = "is_head_of_household")
    private Boolean isHeadOfHousehold = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevel educationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status")
    private EmploymentStatus employmentStatus;

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

    @ElementCollection
    @CollectionTable(
        name = "member_health_conditions",
        schema = "dsr_core",
        joinColumns = @JoinColumn(name = "household_member_id")
    )
    @Column(name = "condition")
    private List<String> healthConditions;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public HouseholdMember() {}

    public HouseholdMember(String firstName, String lastName, LocalDate birthDate, Gender gender, CivilStatus civilStatus, RelationshipType relationshipToHead) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.civilStatus = civilStatus;
        this.relationshipToHead = relationshipToHead;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public String getPsn() {
        return psn;
    }

    public void setPsn(String psn) {
        this.psn = psn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public CivilStatus getCivilStatus() {
        return civilStatus;
    }

    public void setCivilStatus(CivilStatus civilStatus) {
        this.civilStatus = civilStatus;
    }

    public RelationshipType getRelationshipToHead() {
        return relationshipToHead;
    }

    public void setRelationshipToHead(RelationshipType relationshipToHead) {
        this.relationshipToHead = relationshipToHead;
    }

    public Boolean getIsHeadOfHousehold() {
        return isHeadOfHousehold;
    }

    public void setIsHeadOfHousehold(Boolean isHeadOfHousehold) {
        this.isHeadOfHousehold = isHeadOfHousehold;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(EducationLevel educationLevel) {
        this.educationLevel = educationLevel;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public Boolean getIsPwd() {
        return isPwd;
    }

    public void setIsPwd(Boolean isPwd) {
        this.isPwd = isPwd;
    }

    public String getPwdType() {
        return pwdType;
    }

    public void setPwdType(String pwdType) {
        this.pwdType = pwdType;
    }

    public Boolean getIsIndigenous() {
        return isIndigenous;
    }

    public void setIsIndigenous(Boolean isIndigenous) {
        this.isIndigenous = isIndigenous;
    }

    public String getIndigenousGroup() {
        return indigenousGroup;
    }

    public void setIndigenousGroup(String indigenousGroup) {
        this.indigenousGroup = indigenousGroup;
    }

    public Boolean getIsSoloParent() {
        return isSoloParent;
    }

    public void setIsSoloParent(Boolean isSoloParent) {
        this.isSoloParent = isSoloParent;
    }

    public Boolean getIsOfw() {
        return isOfw;
    }

    public void setIsOfw(Boolean isOfw) {
        this.isOfw = isOfw;
    }

    public List<String> getHealthConditions() {
        return healthConditions;
    }

    public void setHealthConditions(List<String> healthConditions) {
        this.healthConditions = healthConditions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
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

    public int getAge() {
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    public boolean isMinor() {
        return getAge() < 18;
    }

    public boolean isSeniorCitizen() {
        return getAge() >= 60;
    }

    @Override
    public String toString() {
        return "HouseholdMember{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", relationshipToHead=" + relationshipToHead +
                ", isHeadOfHousehold=" + isHeadOfHousehold +
                '}';
    }
}
