package ph.gov.dsr.registration.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registration entity representing a registration application
 */
@Entity
@Table(name = "registrations", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "registration_number", unique = true, nullable = false, length = 50)
    private String registrationNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_user_id")
    private User applicantUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status = RegistrationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_channel", nullable = false)
    private RegistrationChannel registrationChannel = RegistrationChannel.WEB_PORTAL;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "rejection_date")
    private LocalDateTime rejectionDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "priority_level")
    private Integer priorityLevel = 3; // 1=HIGH, 2=MEDIUM, 3=LOW

    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    // Relationships
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VerificationInfo> verifications = new ArrayList<>();

    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    // Constructors
    public Registration() {}

    public Registration(String registrationNumber, Household household) {
        this.registrationNumber = registrationNumber;
        this.household = household;
    }

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

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public User getApplicantUser() {
        return applicantUser;
    }

    public void setApplicantUser(User applicantUser) {
        this.applicantUser = applicantUser;
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

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<VerificationInfo> getVerifications() {
        return verifications;
    }

    public void setVerifications(List<VerificationInfo> verifications) {
        this.verifications = verifications;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    // Utility methods
    public void submit() {
        if (status == RegistrationStatus.DRAFT) {
            this.status = RegistrationStatus.PENDING_VERIFICATION;
            this.submissionDate = LocalDateTime.now();
        }
    }

    public void approve(User approver) {
        this.status = RegistrationStatus.APPROVED;
        this.approvalDate = LocalDateTime.now();
        this.completionDate = LocalDate.now();
        this.updatedBy = approver;
    }

    public void reject(String reason, User rejector) {
        this.status = RegistrationStatus.REJECTED;
        this.rejectionDate = LocalDateTime.now();
        this.rejectionReason = reason;
        this.completionDate = LocalDate.now();
        this.updatedBy = rejector;
    }

    public void assignTo(User staff) {
        this.assignedTo = staff;
        if (estimatedCompletionDate == null) {
            // Set estimated completion to 30 days from assignment
            this.estimatedCompletionDate = LocalDate.now().plusDays(30);
        }
    }

    public boolean isOverdue() {
        return estimatedCompletionDate != null && 
               LocalDate.now().isAfter(estimatedCompletionDate) && 
               !status.isFinal();
    }

    public long getDaysInProcess() {
        LocalDateTime startDate = submissionDate != null ? submissionDate : createdAt;
        LocalDateTime endDate = completionDate != null ? 
            completionDate.atStartOfDay() : LocalDateTime.now();
        return java.time.Duration.between(startDate, endDate).toDays();
    }

    @Override
    public String toString() {
        return "Registration{" +
                "id=" + id +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", status=" + status +
                ", priorityLevel=" + priorityLevel +
                '}';
    }
}
