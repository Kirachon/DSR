package ph.gov.dsr.registration.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Verification information entity
 */
@Entity
@Table(name = "verification_info", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
public class VerificationInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @NotBlank
    @Column(name = "verification_type", nullable = false, length = 50)
    private String verificationType;

    @Column(name = "verification_method", length = 50)
    private String verificationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verification_data", columnDefinition = "TEXT")
    private String verificationData;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public VerificationInfo() {}

    public VerificationInfo(String verificationType, String verificationMethod) {
        this.verificationType = verificationType;
        this.verificationMethod = verificationMethod;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getVerificationData() {
        return verificationData;
    }

    public void setVerificationData(String verificationData) {
        this.verificationData = verificationData;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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

    // Utility methods
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    public void verify(User verifier) {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verificationDate = LocalDateTime.now();
        this.verifiedBy = verifier;
    }

    public void fail(String reason) {
        this.verificationStatus = VerificationStatus.FAILED;
        this.verificationDate = LocalDateTime.now();
        this.notes = reason;
    }

    @Override
    public String toString() {
        return "VerificationInfo{" +
                "id=" + id +
                ", verificationType='" + verificationType + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", verificationDate=" + verificationDate +
                '}';
    }
}

/**
 * Enumeration for verification status
 */
enum VerificationStatus {
    PENDING("Pending"),
    VERIFIED("Verified"),
    FAILED("Failed"),
    EXPIRED("Expired");

    private final String displayName;

    VerificationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
