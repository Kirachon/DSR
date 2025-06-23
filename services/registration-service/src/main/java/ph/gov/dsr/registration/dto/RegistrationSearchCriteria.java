package ph.gov.dsr.registration.dto;

import ph.gov.dsr.registration.entity.RegistrationChannel;
import ph.gov.dsr.registration.entity.RegistrationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for registration search criteria
 */
public class RegistrationSearchCriteria {

    private String searchTerm;
    private List<RegistrationStatus> statuses;
    private List<RegistrationChannel> channels;
    private UUID assignedToId;
    private UUID createdById;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime submittedAfter;
    private LocalDateTime submittedBefore;
    private LocalDate dueDateAfter;
    private LocalDate dueDateBefore;
    private Integer priorityLevel;
    private Boolean isOverdue;
    private String householdNumber;
    private String registrationNumber;
    private String headOfHouseholdPsn;

    // Household criteria
    private Boolean isIndigenous;
    private Boolean isPwdHousehold;
    private Boolean isSeniorCitizenHousehold;
    private Integer minMembers;
    private Integer maxMembers;

    // Location criteria
    private String region;
    private String province;
    private String municipality;
    private String barangay;

    // Constructors
    public RegistrationSearchCriteria() {}

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public List<RegistrationStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<RegistrationStatus> statuses) {
        this.statuses = statuses;
    }

    public List<RegistrationChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<RegistrationChannel> channels) {
        this.channels = channels;
    }

    public UUID getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public LocalDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(LocalDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public LocalDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(LocalDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }

    public LocalDateTime getSubmittedAfter() {
        return submittedAfter;
    }

    public void setSubmittedAfter(LocalDateTime submittedAfter) {
        this.submittedAfter = submittedAfter;
    }

    public LocalDateTime getSubmittedBefore() {
        return submittedBefore;
    }

    public void setSubmittedBefore(LocalDateTime submittedBefore) {
        this.submittedBefore = submittedBefore;
    }

    public LocalDate getDueDateAfter() {
        return dueDateAfter;
    }

    public void setDueDateAfter(LocalDate dueDateAfter) {
        this.dueDateAfter = dueDateAfter;
    }

    public LocalDate getDueDateBefore() {
        return dueDateBefore;
    }

    public void setDueDateBefore(LocalDate dueDateBefore) {
        this.dueDateBefore = dueDateBefore;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

    public String getHouseholdNumber() {
        return householdNumber;
    }

    public void setHouseholdNumber(String householdNumber) {
        this.householdNumber = householdNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getHeadOfHouseholdPsn() {
        return headOfHouseholdPsn;
    }

    public void setHeadOfHouseholdPsn(String headOfHouseholdPsn) {
        this.headOfHouseholdPsn = headOfHouseholdPsn;
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

    public Integer getMinMembers() {
        return minMembers;
    }

    public void setMinMembers(Integer minMembers) {
        this.minMembers = minMembers;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    @Override
    public String toString() {
        return "RegistrationSearchCriteria{" +
                "searchTerm='" + searchTerm + '\'' +
                ", statuses=" + statuses +
                ", channels=" + channels +
                ", assignedToId=" + assignedToId +
                ", priorityLevel=" + priorityLevel +
                ", isOverdue=" + isOverdue +
                '}';
    }
}
