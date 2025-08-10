package ph.gov.dsr.registration.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Household entity representing a registered household in the DSR system
 */
@Entity
@Table(name = "households", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
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
    private LocalDate registrationDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status = RegistrationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_channel", nullable = false)
    private RegistrationChannel registrationChannel = RegistrationChannel.WEB_PORTAL;

    @PositiveOrZero
    @Column(name = "total_members")
    private Integer totalMembers = 0;

    @Column(name = "monthly_income", precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "is_indigenous")
    private Boolean isIndigenous = false;

    @Column(name = "is_pwd_household")
    private Boolean isPwdHousehold = false;

    @Column(name = "is_senior_citizen_household")
    private Boolean isSeniorCitizenHousehold = false;

    @Column(name = "consent_given")
    private Boolean consentGiven = false;

    @Column(name = "consent_date")
    private LocalDateTime consentDate;

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

    @Column(name = "created_by", columnDefinition = "UUID")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "UUID")
    private UUID updatedBy;

    // Relationships
    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HouseholdMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HouseholdAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContactInformation> contactInformation = new ArrayList<>();

    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Registration> registrations = new ArrayList<>();

    // Constructors
    public Household() {}

    public Household(String householdNumber) {
        this.householdNumber = householdNumber;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHouseholdNumber() {
        return householdNumber;
    }

    public void setHouseholdNumber(String householdNumber) {
        this.householdNumber = householdNumber;
    }

    public String getHeadOfHouseholdPsn() {
        return headOfHouseholdPsn;
    }

    public void setHeadOfHouseholdPsn(String headOfHouseholdPsn) {
        this.headOfHouseholdPsn = headOfHouseholdPsn;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public RegistrationChannel getRegistrationChannel() {
        return registrationChannel;
    }

    public void setRegistrationChannel(RegistrationChannel registrationChannel) {
        this.registrationChannel = registrationChannel;
    }

    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public Boolean getIsIndigenous() {
        return isIndigenous;
    }

    public void setIsIndigenous(Boolean isIndigenous) {
        this.isIndigenous = isIndigenous;
    }

    public Boolean getIsPwdHousehold() {
        return isPwdHousehold;
    }

    public void setIsPwdHousehold(Boolean isPwdHousehold) {
        this.isPwdHousehold = isPwdHousehold;
    }

    public Boolean getIsSeniorCitizenHousehold() {
        return isSeniorCitizenHousehold;
    }

    public void setIsSeniorCitizenHousehold(Boolean isSeniorCitizenHousehold) {
        this.isSeniorCitizenHousehold = isSeniorCitizenHousehold;
    }

    public Boolean getConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(Boolean consentGiven) {
        this.consentGiven = consentGiven;
        if (consentGiven && consentDate == null) {
            this.consentDate = LocalDateTime.now();
        }
    }

    public LocalDateTime getConsentDate() {
        return consentDate;
    }

    public void setConsentDate(LocalDateTime consentDate) {
        this.consentDate = consentDate;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<HouseholdMember> getMembers() {
        return members;
    }

    public void setMembers(List<HouseholdMember> members) {
        this.members = members;
    }

    public List<HouseholdAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<HouseholdAddress> addresses) {
        this.addresses = addresses;
    }

    public List<ContactInformation> getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(List<ContactInformation> contactInformation) {
        this.contactInformation = contactInformation;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }

    // Utility methods
    public HouseholdMember getHeadOfHousehold() {
        return members.stream()
                .filter(HouseholdMember::getIsHeadOfHousehold)
                .findFirst()
                .orElse(null);
    }

    public void addMember(HouseholdMember member) {
        members.add(member);
        member.setHousehold(this);
        updateTotalMembers();
    }

    public void removeMember(HouseholdMember member) {
        members.remove(member);
        member.setHousehold(null);
        updateTotalMembers();
    }

    private void updateTotalMembers() {
        this.totalMembers = members.size();
    }

    public boolean isEligibleForRegistration() {
        return consentGiven && !members.isEmpty() && getHeadOfHousehold() != null;
    }

    @Override
    public String toString() {
        return "Household{" +
                "id=" + id +
                ", householdNumber='" + householdNumber + '\'' +
                ", status=" + status +
                ", totalMembers=" + totalMembers +
                '}';
    }
}
