package ph.gov.dsr.grievance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Case Activity entity for tracking activities and communications in grievance cases
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "case_activities", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class CaseActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private GrievanceCase grievanceCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "performed_by_role", length = 50)
    private String performedByRole;

    @NotNull
    @Column(name = "activity_date", nullable = false)
    private LocalDateTime activityDate = LocalDateTime.now();

    @Column(name = "communication_channel", length = 50)
    private String communicationChannel; // EMAIL, PHONE, SMS, IN_PERSON, SYSTEM

    @Column(name = "communication_direction", length = 20)
    private String communicationDirection; // INBOUND, OUTBOUND

    @Column(name = "recipient", length = 200)
    private String recipient;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of attachment references

    @Column(name = "status_before", length = 30)
    private String statusBefore;

    @Column(name = "status_after", length = 30)
    private String statusAfter;

    @Column(name = "priority_before", length = 20)
    private String priorityBefore;

    @Column(name = "priority_after", length = 20)
    private String priorityAfter;

    @Column(name = "assigned_before", length = 100)
    private String assignedBefore;

    @Column(name = "assigned_after", length = 100)
    private String assignedAfter;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    @Column(name = "is_visible_to_complainant")
    private Boolean isVisibleToComplainant = true;

    @Column(name = "requires_response")
    private Boolean requiresResponse = false;

    @Column(name = "response_due_date")
    private LocalDateTime responseDueDate;

    @Column(name = "is_automated")
    private Boolean isAutomated = false;

    @Column(name = "system_reference", length = 200)
    private String systemReference;

    @Column(name = "external_reference", length = 200)
    private String externalReference;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "outcome", columnDefinition = "TEXT")
    private String outcome;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // Enums
    public enum ActivityType {
        CASE_CREATED,
        CASE_ASSIGNED,
        CASE_REASSIGNED,
        STATUS_CHANGED,
        PRIORITY_CHANGED,
        COMMUNICATION_SENT,
        COMMUNICATION_RECEIVED,
        DOCUMENT_UPLOADED,
        INVESTIGATION_STARTED,
        INVESTIGATION_COMPLETED,
        ESCALATION,
        RESOLUTION_PROPOSED,
        RESOLUTION_APPROVED,
        CASE_RESOLVED,
        CASE_CLOSED,
        FEEDBACK_RECEIVED,
        FOLLOW_UP_SCHEDULED,
        REMINDER_SENT,
        DEADLINE_EXTENDED,
        CASE_REOPENED,
        NOTE_ADDED,
        SYSTEM_UPDATE,
        OTHER
    }

    // Constructors
    public CaseActivity() {}

    public CaseActivity(GrievanceCase grievanceCase, ActivityType activityType, String description, String performedBy) {
        this.grievanceCase = grievanceCase;
        this.activityType = activityType;
        this.description = description;
        this.performedBy = performedBy;
    }

    // Helper methods
    
    /**
     * Check if activity is a communication
     */
    public boolean isCommunication() {
        return activityType == ActivityType.COMMUNICATION_SENT || 
               activityType == ActivityType.COMMUNICATION_RECEIVED;
    }

    /**
     * Check if activity is a status change
     */
    public boolean isStatusChange() {
        return activityType == ActivityType.STATUS_CHANGED ||
               activityType == ActivityType.CASE_ASSIGNED ||
               activityType == ActivityType.CASE_REASSIGNED ||
               activityType == ActivityType.ESCALATION ||
               activityType == ActivityType.CASE_RESOLVED ||
               activityType == ActivityType.CASE_CLOSED;
    }

    /**
     * Check if activity is overdue for response
     */
    public boolean isOverdueForResponse() {
        return requiresResponse && responseDueDate != null && 
               responseDueDate.isBefore(LocalDateTime.now());
    }

    /**
     * Check if follow-up is due
     */
    public boolean isFollowUpDue() {
        return followUpRequired && followUpDate != null && 
               followUpDate.isBefore(LocalDateTime.now());
    }

    /**
     * Get activity age in hours
     */
    public long getActivityAgeInHours() {
        return java.time.temporal.ChronoUnit.HOURS.between(activityDate, LocalDateTime.now());
    }

    /**
     * Mark as requiring follow-up
     */
    public void scheduleFollowUp(LocalDateTime followUpDate) {
        this.followUpRequired = true;
        this.followUpDate = followUpDate;
    }

    /**
     * Mark follow-up as completed
     */
    public void completeFollowUp() {
        this.followUpRequired = false;
        this.followUpDate = null;
    }

    /**
     * Set response requirement
     */
    public void requireResponse(LocalDateTime dueDate) {
        this.requiresResponse = true;
        this.responseDueDate = dueDate;
    }

    /**
     * Mark response as completed
     */
    public void completeResponse() {
        this.requiresResponse = false;
        this.responseDueDate = null;
    }

    /**
     * Get activity summary
     */
    public String getActivitySummary() {
        return String.format("%s: %s by %s on %s", 
                activityType, description, performedBy, 
                activityDate.toString());
    }

    /**
     * Create status change activity
     */
    public static CaseActivity createStatusChange(GrievanceCase grievanceCase, String oldStatus, 
                                                String newStatus, String performedBy, String reason) {
        CaseActivity activity = new CaseActivity();
        activity.setGrievanceCase(grievanceCase);
        activity.setActivityType(ActivityType.STATUS_CHANGED);
        activity.setDescription("Status changed from " + oldStatus + " to " + newStatus + 
                              (reason != null ? ": " + reason : ""));
        activity.setPerformedBy(performedBy);
        activity.setStatusBefore(oldStatus);
        activity.setStatusAfter(newStatus);
        return activity;
    }

    /**
     * Create communication activity
     */
    public static CaseActivity createCommunication(GrievanceCase grievanceCase, String channel, 
                                                 String direction, String subject, String content, 
                                                 String performedBy) {
        CaseActivity activity = new CaseActivity();
        activity.setGrievanceCase(grievanceCase);
        activity.setActivityType(direction.equals("OUTBOUND") ? 
                                ActivityType.COMMUNICATION_SENT : ActivityType.COMMUNICATION_RECEIVED);
        activity.setDescription("Communication via " + channel + ": " + subject);
        activity.setPerformedBy(performedBy);
        activity.setCommunicationChannel(channel);
        activity.setCommunicationDirection(direction);
        activity.setSubject(subject);
        activity.setContent(content);
        return activity;
    }
}
