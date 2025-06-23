package ph.gov.dsr.registration.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contact information entity for households
 */
@Entity
@Table(name = "contact_information", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
public class ContactInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "contact_type", length = 20)
    private String contactType = "PRIMARY";

    @Column(name = "contact_person_name", length = 200)
    private String contactPersonName;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "landline_number", length = 20)
    private String landlineNumber;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "preferred_contact_method", length = 20)
    private String preferredContactMethod = "mobile";

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public ContactInformation() {}

    public ContactInformation(String contactPersonName, String mobileNumber) {
        this.contactPersonName = contactPersonName;
        this.mobileNumber = mobileNumber;
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

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getLandlineNumber() {
        return landlineNumber;
    }

    public void setLandlineNumber(String landlineNumber) {
        this.landlineNumber = landlineNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public void setPreferredContactMethod(String preferredContactMethod) {
        this.preferredContactMethod = preferredContactMethod;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
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
    public String getPrimaryContactNumber() {
        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            return mobileNumber;
        }
        return landlineNumber;
    }

    public boolean hasValidContact() {
        return (mobileNumber != null && !mobileNumber.trim().isEmpty()) ||
               (landlineNumber != null && !landlineNumber.trim().isEmpty()) ||
               (emailAddress != null && !emailAddress.trim().isEmpty());
    }

    @Override
    public String toString() {
        return "ContactInformation{" +
                "id=" + id +
                ", contactType='" + contactType + '\'' +
                ", contactPersonName='" + contactPersonName + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", isPrimary=" + isPrimary +
                '}';
    }
}
