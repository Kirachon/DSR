package ph.gov.dsr.registration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ph.gov.dsr.registration.entity.RegistrationChannel;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a new household registration
 */
public class RegistrationCreateRequest {

    @NotNull(message = "Household information is required")
    @Valid
    private HouseholdCreateDto household;

    @NotEmpty(message = "At least one household member is required")
    @Valid
    private List<HouseholdMemberCreateDto> members;

    @NotNull(message = "Address information is required")
    @Valid
    private HouseholdAddressCreateDto address;

    @Valid
    private ContactInformationCreateDto contactInfo;

    @NotNull(message = "Registration channel is required")
    private RegistrationChannel registrationChannel = RegistrationChannel.WEB_PORTAL;

    @NotNull(message = "Consent must be given")
    @AssertTrue(message = "Consent must be given to proceed with registration")
    private Boolean consentGiven;

    private String preferredLanguage = "en";

    private String notes;

    // Nested DTOs
    public static class HouseholdCreateDto {
        @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
        private BigDecimal monthlyIncome;

        private Boolean isIndigenous = false;
        private Boolean isPwdHousehold = false;
        private Boolean isSeniorCitizenHousehold = false;
        private String preferredLanguage = "en";
        private String notes;

        // Getters and Setters
        public BigDecimal getMonthlyIncome() { return monthlyIncome; }
        public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
        public Boolean getIsIndigenous() { return isIndigenous; }
        public void setIsIndigenous(Boolean isIndigenous) { this.isIndigenous = isIndigenous; }
        public Boolean getIsPwdHousehold() { return isPwdHousehold; }
        public void setIsPwdHousehold(Boolean isPwdHousehold) { this.isPwdHousehold = isPwdHousehold; }
        public Boolean getIsSeniorCitizenHousehold() { return isSeniorCitizenHousehold; }
        public void setIsSeniorCitizenHousehold(Boolean isSeniorCitizenHousehold) { this.isSeniorCitizenHousehold = isSeniorCitizenHousehold; }
        public String getPreferredLanguage() { return preferredLanguage; }
        public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class HouseholdMemberCreateDto {
        @Size(max = 16, message = "PSN must not exceed 16 characters")
        private String psn;

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        private String lastName;

        @Size(max = 100, message = "Middle name must not exceed 100 characters")
        private String middleName;

        @Size(max = 20, message = "Suffix must not exceed 20 characters")
        private String suffix;

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        private java.time.LocalDate birthDate;

        @NotNull(message = "Gender is required")
        private String gender;

        @NotNull(message = "Civil status is required")
        private String civilStatus;

        @NotNull(message = "Relationship to head is required")
        private String relationshipToHead;

        private Boolean isHeadOfHousehold = false;
        private String educationLevel;
        private String employmentStatus;
        private String occupation;

        @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
        private BigDecimal monthlyIncome;

        private Boolean isPwd = false;
        private String pwdType;
        private Boolean isIndigenous = false;
        private String indigenousGroup;
        private Boolean isSoloParent = false;
        private Boolean isOfw = false;
        private List<String> healthConditions;

        // Getters and Setters
        public String getPsn() { return psn; }
        public void setPsn(String psn) { this.psn = psn; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getMiddleName() { return middleName; }
        public void setMiddleName(String middleName) { this.middleName = middleName; }
        public String getSuffix() { return suffix; }
        public void setSuffix(String suffix) { this.suffix = suffix; }
        public java.time.LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(java.time.LocalDate birthDate) { this.birthDate = birthDate; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getCivilStatus() { return civilStatus; }
        public void setCivilStatus(String civilStatus) { this.civilStatus = civilStatus; }
        public String getRelationshipToHead() { return relationshipToHead; }
        public void setRelationshipToHead(String relationshipToHead) { this.relationshipToHead = relationshipToHead; }
        public Boolean getIsHeadOfHousehold() { return isHeadOfHousehold; }
        public void setIsHeadOfHousehold(Boolean isHeadOfHousehold) { this.isHeadOfHousehold = isHeadOfHousehold; }
        public String getEducationLevel() { return educationLevel; }
        public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
        public String getEmploymentStatus() { return employmentStatus; }
        public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
        public String getOccupation() { return occupation; }
        public void setOccupation(String occupation) { this.occupation = occupation; }
        public BigDecimal getMonthlyIncome() { return monthlyIncome; }
        public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
        public Boolean getIsPwd() { return isPwd; }
        public void setIsPwd(Boolean isPwd) { this.isPwd = isPwd; }
        public String getPwdType() { return pwdType; }
        public void setPwdType(String pwdType) { this.pwdType = pwdType; }
        public Boolean getIsIndigenous() { return isIndigenous; }
        public void setIsIndigenous(Boolean isIndigenous) { this.isIndigenous = isIndigenous; }
        public String getIndigenousGroup() { return indigenousGroup; }
        public void setIndigenousGroup(String indigenousGroup) { this.indigenousGroup = indigenousGroup; }
        public Boolean getIsSoloParent() { return isSoloParent; }
        public void setIsSoloParent(Boolean isSoloParent) { this.isSoloParent = isSoloParent; }
        public Boolean getIsOfw() { return isOfw; }
        public void setIsOfw(Boolean isOfw) { this.isOfw = isOfw; }
        public List<String> getHealthConditions() { return healthConditions; }
        public void setHealthConditions(List<String> healthConditions) { this.healthConditions = healthConditions; }
    }

    public static class HouseholdAddressCreateDto {
        private String streetAddress;

        @NotBlank(message = "Barangay is required")
        private String barangay;

        @NotBlank(message = "Municipality is required")
        private String municipality;

        @NotBlank(message = "Province is required")
        private String province;

        @NotBlank(message = "Region is required")
        private String region;

        private String zipCode;
        private String country = "Philippines";
        private String housingType;
        private String housingMaterial;
        private String roofMaterial;
        private Boolean hasElectricity = false;
        private Boolean hasWaterSupply = false;
        private Boolean hasToilet = false;
        private String waterSource;
        private String toiletType;

        // Getters and Setters
        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
        public String getBarangay() { return barangay; }
        public void setBarangay(String barangay) { this.barangay = barangay; }
        public String getMunicipality() { return municipality; }
        public void setMunicipality(String municipality) { this.municipality = municipality; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getHousingType() { return housingType; }
        public void setHousingType(String housingType) { this.housingType = housingType; }
        public String getHousingMaterial() { return housingMaterial; }
        public void setHousingMaterial(String housingMaterial) { this.housingMaterial = housingMaterial; }
        public String getRoofMaterial() { return roofMaterial; }
        public void setRoofMaterial(String roofMaterial) { this.roofMaterial = roofMaterial; }
        public Boolean getHasElectricity() { return hasElectricity; }
        public void setHasElectricity(Boolean hasElectricity) { this.hasElectricity = hasElectricity; }
        public Boolean getHasWaterSupply() { return hasWaterSupply; }
        public void setHasWaterSupply(Boolean hasWaterSupply) { this.hasWaterSupply = hasWaterSupply; }
        public Boolean getHasToilet() { return hasToilet; }
        public void setHasToilet(Boolean hasToilet) { this.hasToilet = hasToilet; }
        public String getWaterSource() { return waterSource; }
        public void setWaterSource(String waterSource) { this.waterSource = waterSource; }
        public String getToiletType() { return toiletType; }
        public void setToiletType(String toiletType) { this.toiletType = toiletType; }
    }

    public static class ContactInformationCreateDto {
        private String contactPersonName;
        private String mobileNumber;
        private String landlineNumber;
        private String emailAddress;
        private String preferredContactMethod = "mobile";

        // Getters and Setters
        public String getContactPersonName() { return contactPersonName; }
        public void setContactPersonName(String contactPersonName) { this.contactPersonName = contactPersonName; }
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public String getLandlineNumber() { return landlineNumber; }
        public void setLandlineNumber(String landlineNumber) { this.landlineNumber = landlineNumber; }
        public String getEmailAddress() { return emailAddress; }
        public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
        public String getPreferredContactMethod() { return preferredContactMethod; }
        public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }
    }

    // Main class getters and setters
    public HouseholdCreateDto getHousehold() { return household; }
    public void setHousehold(HouseholdCreateDto household) { this.household = household; }
    public List<HouseholdMemberCreateDto> getMembers() { return members; }
    public void setMembers(List<HouseholdMemberCreateDto> members) { this.members = members; }
    public HouseholdAddressCreateDto getAddress() { return address; }
    public void setAddress(HouseholdAddressCreateDto address) { this.address = address; }
    public ContactInformationCreateDto getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInformationCreateDto contactInfo) { this.contactInfo = contactInfo; }
    public RegistrationChannel getRegistrationChannel() { return registrationChannel; }
    public void setRegistrationChannel(RegistrationChannel registrationChannel) { this.registrationChannel = registrationChannel; }
    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
