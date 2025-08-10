package ph.gov.dsr.registration.dto;

import ph.gov.dsr.registration.entity.RegistrationChannel;
import ph.gov.dsr.registration.entity.RegistrationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Registration response
 */
public class RegistrationResponse {

    private UUID id;
    private String registrationNumber;
    private UUID householdId;
    private String householdNumber;
    private RegistrationStatus status;
    private RegistrationChannel registrationChannel;
    private LocalDateTime submissionDate;
    private LocalDateTime verificationDate;
    private LocalDateTime approvalDate;
    private LocalDateTime rejectionDate;
    private String rejectionReason;
    private UUID assignedToId;
    private String assignedToName;
    private Integer priorityLevel;
    private LocalDate estimatedCompletionDate;
    private LocalDate completionDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private String updatedByName;

    // Household summary information
    private String headOfHouseholdName;
    private String headOfHouseholdPsn;
    private Integer totalMembers;
    private String currentAddress;
    private String contactNumber;

    // Constructors
    public RegistrationResponse() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(UUID householdId) {
        this.householdId = householdId;
    }

    public String getHouseholdNumber() {
        return householdNumber;
    }

    public void setHouseholdNumber(String householdNumber) {
        this.householdNumber = householdNumber;
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

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDateTime getRejectionDate() {
        return rejectionDate;
    }

    public void setRejectionDate(LocalDateTime rejectionDate) {
        this.rejectionDate = rejectionDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public UUID getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public LocalDate getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    public void setEstimatedCompletionDate(LocalDate estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
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

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getHeadOfHouseholdName() {
        return headOfHouseholdName;
    }

    public void setHeadOfHouseholdName(String headOfHouseholdName) {
        this.headOfHouseholdName = headOfHouseholdName;
    }

    public String getHeadOfHouseholdPsn() {
        return headOfHouseholdPsn;
    }

    public void setHeadOfHouseholdPsn(String headOfHouseholdPsn) {
        this.headOfHouseholdPsn = headOfHouseholdPsn;
    }

    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    // Utility methods
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    public String getChannelDisplayName() {
        return registrationChannel != null ? registrationChannel.getDisplayName() : null;
    }

    public boolean isPending() {
        return status != null && status.isPending();
    }

    public boolean isCompleted() {
        return status != null && status.isFinal();
    }

    public boolean canBeModified() {
        return status != null && status.canBeModified();
    }

    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "id=" + id +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", status=" + status +
                ", householdNumber='" + householdNumber + '\'' +
                '}';
    }
}
