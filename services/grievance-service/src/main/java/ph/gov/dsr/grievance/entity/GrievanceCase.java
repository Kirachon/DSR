package ph.gov.dsr.grievance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Grievance Case entity for managing grievance cases and complaints
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "grievance_cases", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class GrievanceCase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "case_number", unique = true, nullable = false, length = 50)
    private String caseNumber;

    @NotBlank
    @Column(name = "complainant_psn", nullable = false, length = 16)
    private String complainantPsn;

    @Column(name = "complainant_name", length = 200)
    private String complainantName;

    @Column(name = "complainant_email", length = 200)
    private String complainantEmail;

    @Column(name = "complainant_phone", length = 50)
    private String complainantPhone;

    @NotBlank
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private GrievanceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CaseStatus status = CaseStatus.SUBMITTED;

    @Column(name = "program_code", length = 50)
    private String programCode;

    @Column(name = "service_provider", length = 200)
    private String serviceProvider;

    @Column(name = "incident_date")
    private LocalDateTime incidentDate;

    @Column(name = "incident_location", length = 500)
    private String incidentLocation;

    @NotNull
    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate = LocalDateTime.now();

    @Column(name = "submission_channel", length = 50)
    private String submissionChannel; // ONLINE, PHONE, EMAIL, WALK_IN, MOBILE_APP

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Column(name = "resolution_target_date")
    private LocalDateTime resolutionTargetDate;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    @Column(name = "resolution_summary", columnDefinition = "TEXT")
    private String resolutionSummary;

    @Column(name = "resolution_actions", columnDefinition = "TEXT")
    private String resolutionActions;

    @Column(name = "complainant_satisfaction", length = 20)
    private String complainantSatisfaction; // VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED

    @Column(name = "feedback_comments", columnDefinition = "TEXT")
    private String feedbackComments;

    @Column(name = "escalation_level")
    private Integer escalationLevel = 0;

    @Column(name = "escalated_to", length = 200)
    private String escalatedTo;

    @Column(name = "escalation_date")
    private LocalDateTime escalationDate;

    @Column(name = "escalation_reason", columnDefinition = "TEXT")
    private String escalationReason;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    @Column(name = "requires_investigation")
    private Boolean requiresInvestigation = false;

    @Column(name = "investigation_findings", columnDefinition = "TEXT")
    private String investigationFindings;

    @Column(name = "corrective_actions", columnDefinition = "TEXT")
    private String correctiveActions;

    @Column(name = "preventive_measures", columnDefinition = "TEXT")
    private String preventiveMeasures;

    @Column(name = "related_cases", columnDefinition = "TEXT")
    private String relatedCases; // JSON array of related case IDs

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of attachment references

    @Column(name = "communication_log", columnDefinition = "JSONB")
    private String communicationLog;

    @Column(name = "workflow_state", columnDefinition = "JSONB")
    private String workflowState;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array of tags

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Relationships
    @OneToMany(mappedBy = "grievanceCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CaseActivity> activities = new ArrayList<>();

    // Enums
    public enum GrievanceCategory {
        SERVICE_DELIVERY,
        PAYMENT_ISSUE,
        ELIGIBILITY_DISPUTE,
        STAFF_CONDUCT,
        SYSTEM_ERROR,
        DATA_PRIVACY,
        DISCRIMINATION,
        CORRUPTION,
        ACCESS_ISSUE,
        QUALITY_CONCERN,
        OTHER
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum CaseStatus {
        SUBMITTED,
        ACKNOWLEDGED,
        UNDER_REVIEW,
        INVESTIGATING,
        PENDING_RESPONSE,
        ESCALATED,
        RESOLVED,
        CLOSED,
        REJECTED,
        CANCELLED
    }

    // Constructors
    public GrievanceCase() {}

    public GrievanceCase(String complainantPsn, String subject, String description, GrievanceCategory category) {
        this.complainantPsn = complainantPsn;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.caseNumber = generateCaseNumber();
    }

    // Helper methods
    
    /**
     * Check if case is open
     */
    public boolean isOpen() {
        return status != CaseStatus.RESOLVED && status != CaseStatus.CLOSED && 
               status != CaseStatus.REJECTED && status != CaseStatus.CANCELLED;
    }

    /**
     * Check if case is overdue
     */
    public boolean isOverdue() {
        return isOpen() && resolutionTargetDate != null && 
               resolutionTargetDate.isBefore(LocalDateTime.now());
    }

    /**
     * Calculate case age in days
     */
    public long getCaseAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(submissionDate, LocalDateTime.now());
    }

    /**
     * Calculate days until target resolution
     */
    public long getDaysUntilTarget() {
        if (resolutionTargetDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), resolutionTargetDate);
    }

    /**
     * Assign case to staff member
     */
    public void assignTo(String staffMember) {
        this.assignedTo = staffMember;
        this.assignedDate = LocalDateTime.now();
        this.status = CaseStatus.UNDER_REVIEW;
        
        // Set target resolution date based on priority
        switch (priority) {
            case CRITICAL:
                this.resolutionTargetDate = LocalDateTime.now().plusDays(1);
                break;
            case HIGH:
                this.resolutionTargetDate = LocalDateTime.now().plusDays(3);
                break;
            case MEDIUM:
                this.resolutionTargetDate = LocalDateTime.now().plusDays(7);
                break;
            case LOW:
                this.resolutionTargetDate = LocalDateTime.now().plusDays(14);
                break;
        }
    }

    /**
     * Escalate case
     */
    public void escalate(String escalatedTo, String reason) {
        this.escalationLevel++;
        this.escalatedTo = escalatedTo;
        this.escalationDate = LocalDateTime.now();
        this.escalationReason = reason;
        this.status = CaseStatus.ESCALATED;
        
        // Adjust priority if escalated
        if (priority == Priority.LOW) {
            priority = Priority.MEDIUM;
        } else if (priority == Priority.MEDIUM) {
            priority = Priority.HIGH;
        }
    }

    /**
     * Resolve case
     */
    public void resolve(String resolutionSummary, String resolutionActions) {
        this.status = CaseStatus.RESOLVED;
        this.resolutionDate = LocalDateTime.now();
        this.resolutionSummary = resolutionSummary;
        this.resolutionActions = resolutionActions;
    }

    /**
     * Close case
     */
    public void close() {
        this.status = CaseStatus.CLOSED;
    }

    /**
     * Add activity to case
     */
    public void addActivity(CaseActivity activity) {
        activities.add(activity);
        activity.setGrievanceCase(this);
    }

    /**
     * Generate case number
     */
    private String generateCaseNumber() {
        return "GRV-" + System.currentTimeMillis();
    }

    /**
     * Get case summary
     */
    public String getCaseSummary() {
        return String.format("%s: %s (Status: %s, Priority: %s, Age: %d days)", 
                caseNumber, subject, status, priority, getCaseAgeInDays());
    }
}
