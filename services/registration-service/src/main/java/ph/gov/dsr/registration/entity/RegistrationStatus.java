package ph.gov.dsr.registration.entity;

/**
 * Enumeration for registration status
 */
public enum RegistrationStatus {
    DRAFT("Draft"),
    PENDING_VERIFICATION("Pending Verification"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired");

    private final String displayName;

    RegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == APPROVED;
    }

    public boolean isPending() {
        return this == PENDING_VERIFICATION || this == PENDING_APPROVAL;
    }

    public boolean isFinal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }

    public boolean canBeModified() {
        return this == DRAFT || this == PENDING_VERIFICATION;
    }
}
