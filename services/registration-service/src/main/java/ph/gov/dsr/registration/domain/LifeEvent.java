package ph.gov.dsr.registration.domain;

import jakarta.persistence.*;
import lombok.*;
import ph.gov.dsr.common.model.BaseEntity;

import java.time.LocalDate;
import java.util.Map;

/**
 * Life event entity representing significant changes in household circumstances.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Entity
@Table(name = "life_events", schema = "dsr_core")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifeEvent extends BaseEntity {

    /**
     * Associated registration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    /**
     * Type of life event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private LifeEventType eventType;

    /**
     * Date when the life event occurred.
     */
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    /**
     * PSN of the affected household member.
     */
    @Column(name = "affected_member_psn", length = 16)
    private String affectedMemberPsn;

    /**
     * Description of the life event.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Additional event details stored as JSON.
     */
    @ElementCollection
    @CollectionTable(name = "life_event_details", joinColumns = @JoinColumn(name = "life_event_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value")
    private Map<String, String> eventDetails;

    /**
     * Current processing status of the life event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    /**
     * Date when the life event was reported.
     */
    @Column(name = "reported_date", nullable = false)
    @Builder.Default
    private LocalDate reportedDate = LocalDate.now();

    /**
     * Date when the life event was processed.
     */
    @Column(name = "processed_date")
    private LocalDate processedDate;

    /**
     * User who reported the life event.
     */
    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    /**
     * User who processed the life event.
     */
    @Column(name = "processed_by", length = 100)
    private String processedBy;

    /**
     * Processing notes and comments.
     */
    @Column(name = "processing_notes", columnDefinition = "TEXT")
    private String processingNotes;

    /**
     * Whether this life event triggers eligibility reassessment.
     */
    @Column(name = "triggers_reassessment", nullable = false)
    @Builder.Default
    private Boolean triggersReassessment = true;

    /**
     * Life event type enumeration.
     */
    public enum LifeEventType {
        BIRTH,
        DEATH,
        MARRIAGE,
        SEPARATION,
        DIVORCE,
        EMPLOYMENT_CHANGE,
        INCOME_CHANGE,
        ADDRESS_CHANGE,
        EDUCATION_CHANGE,
        HEALTH_CONDITION_CHANGE,
        DISABILITY_STATUS_CHANGE,
        HOUSEHOLD_COMPOSITION_CHANGE,
        MIGRATION,
        OTHER
    }

    /**
     * Processing status enumeration.
     */
    public enum ProcessingStatus {
        PENDING,
        UNDER_REVIEW,
        PROCESSED,
        REJECTED,
        CANCELLED
    }

    /**
     * Marks the life event as processed.
     * 
     * @param processedBy User who processed the event
     * @param notes Processing notes
     */
    public void markAsProcessed(String processedBy, String notes) {
        this.processingStatus = ProcessingStatus.PROCESSED;
        this.processedDate = LocalDate.now();
        this.processedBy = processedBy;
        this.processingNotes = notes;
    }

    /**
     * Marks the life event as rejected.
     * 
     * @param processedBy User who rejected the event
     * @param reason Rejection reason
     */
    public void markAsRejected(String processedBy, String reason) {
        this.processingStatus = ProcessingStatus.REJECTED;
        this.processedDate = LocalDate.now();
        this.processedBy = processedBy;
        this.processingNotes = reason;
    }

    /**
     * Checks if the life event is in a final state.
     * 
     * @return true if processed, rejected, or cancelled
     */
    public boolean isFinalState() {
        return processingStatus == ProcessingStatus.PROCESSED ||
               processingStatus == ProcessingStatus.REJECTED ||
               processingStatus == ProcessingStatus.CANCELLED;
    }

    /**
     * Checks if the life event can be modified.
     * 
     * @return true if the life event can be modified
     */
    public boolean canBeModified() {
        return !isFinalState() && processingStatus != ProcessingStatus.UNDER_REVIEW;
    }
}
