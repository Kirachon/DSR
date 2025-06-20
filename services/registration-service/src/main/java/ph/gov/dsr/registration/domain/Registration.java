package ph.gov.dsr.registration.domain;

import jakarta.persistence.*;
import lombok.*;
import ph.gov.dsr.common.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Registration entity representing a household registration in the DSR system.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Entity
@Table(name = "registrations", schema = "dsr_core")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration extends BaseEntity {

    /**
     * Unique registration identifier.
     */
    @Column(name = "registration_id", unique = true, nullable = false, length = 20)
    private String registrationId;

    /**
     * Associated household identifier.
     */
    @Column(name = "household_id", length = 20)
    private String householdId;

    /**
     * PhilSys Number of the household head.
     */
    @Column(name = "head_of_household_psn", nullable = false, length = 16)
    private String headOfHouseholdPsn;

    /**
     * Current registration status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING_VERIFICATION;

    /**
     * Registration channel used.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_channel", nullable = false)
    private RegistrationChannel registrationChannel;

    /**
     * Date when registration was submitted.
     */
    @Column(name = "submission_date", nullable = false)
    @Builder.Default
    private LocalDate submissionDate = LocalDate.now();

    /**
     * Confirmation number for the registration.
     */
    @Column(name = "confirmation_number", unique = true, length = 20)
    private String confirmationNumber;

    /**
     * Preferred language for communication.
     */
    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private String preferredLanguage = "en";

    /**
     * Contact information for the household.
     */
    @Embedded
    private ContactInformation contactInformation;

    /**
     * Data consent information.
     */
    @Embedded
    private DataConsent dataConsent;

    /**
     * Verification status and details.
     */
    @Embedded
    private VerificationInfo verificationInfo;

    /**
     * Processing notes and comments.
     */
    @Column(name = "processing_notes", columnDefinition = "TEXT")
    private String processingNotes;

    /**
     * Estimated processing completion date.
     */
    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;

    /**
     * Actual completion date.
     */
    @Column(name = "completion_date")
    private LocalDate completionDate;

    /**
     * List of life events associated with this registration.
     */
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LifeEvent> lifeEvents = new ArrayList<>();

    /**
     * List of documents associated with this registration.
     */
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RegistrationDocument> documents = new ArrayList<>();

    /**
     * Registration status enumeration.
     */
    public enum RegistrationStatus {
        PENDING_VERIFICATION,
        UNDER_REVIEW,
        VERIFIED,
        APPROVED,
        REJECTED,
        CANCELLED,
        COMPLETED
    }

    /**
     * Registration channel enumeration.
     */
    public enum RegistrationChannel {
        WEB_PORTAL,
        MOBILE_APP,
        USSD,
        SMS,
        ASSISTED_REGISTRATION,
        WALK_IN,
        COMMUNITY_DRIVE
    }

    /**
     * Contact information embedded class.
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactInformation {
        @Column(name = "mobile_number", length = 15)
        private String mobileNumber;

        @Column(name = "email_address", length = 100)
        private String emailAddress;

        @Enumerated(EnumType.STRING)
        @Column(name = "preferred_contact_method")
        @Builder.Default
        private ContactMethod preferredContactMethod = ContactMethod.SMS;

        public enum ContactMethod {
            SMS, EMAIL, CALL, MAIL
        }
    }

    /**
     * Data consent embedded class.
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataConsent {
        @Column(name = "consent_given", nullable = false)
        @Builder.Default
        private Boolean consentGiven = false;

        @Column(name = "consent_date")
        private Instant consentDate;

        @Column(name = "consent_version", length = 10)
        private String consentVersion;

        @Column(name = "consent_ip_address", length = 45)
        private String consentIpAddress;
    }

    /**
     * Verification information embedded class.
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificationInfo {
        @Column(name = "philsys_verified")
        @Builder.Default
        private Boolean philsysVerified = false;

        @Column(name = "philsys_verification_date")
        private Instant philsysVerificationDate;

        @Column(name = "address_verified")
        @Builder.Default
        private Boolean addressVerified = false;

        @Column(name = "address_verification_date")
        private Instant addressVerificationDate;

        @Column(name = "documents_verified")
        @Builder.Default
        private Boolean documentsVerified = false;

        @Column(name = "documents_verification_date")
        private Instant documentsVerificationDate;

        @Column(name = "verification_score", precision = 5, scale = 2)
        private Double verificationScore;
    }

    /**
     * Adds a life event to this registration.
     * 
     * @param lifeEvent The life event to add
     */
    public void addLifeEvent(LifeEvent lifeEvent) {
        lifeEvents.add(lifeEvent);
        lifeEvent.setRegistration(this);
    }

    /**
     * Adds a document to this registration.
     * 
     * @param document The document to add
     */
    public void addDocument(RegistrationDocument document) {
        documents.add(document);
        document.setRegistration(this);
    }

    /**
     * Checks if the registration is in a final state.
     * 
     * @return true if the registration is completed, rejected, or cancelled
     */
    public boolean isFinalState() {
        return status == RegistrationStatus.COMPLETED ||
               status == RegistrationStatus.REJECTED ||
               status == RegistrationStatus.CANCELLED;
    }

    /**
     * Checks if the registration can be modified.
     * 
     * @return true if the registration can be modified
     */
    public boolean canBeModified() {
        return !isFinalState() && status != RegistrationStatus.UNDER_REVIEW;
    }

    /**
     * Marks the registration as verified.
     */
    public void markAsVerified() {
        this.status = RegistrationStatus.VERIFIED;
        if (this.verificationInfo == null) {
            this.verificationInfo = new VerificationInfo();
        }
        this.verificationInfo.setPhilsysVerified(true);
        this.verificationInfo.setPhilsysVerificationDate(Instant.now());
    }

    /**
     * Marks the registration as completed.
     */
    public void markAsCompleted() {
        this.status = RegistrationStatus.COMPLETED;
        this.completionDate = LocalDate.now();
    }
}
