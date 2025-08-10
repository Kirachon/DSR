package ph.gov.dsr.registration.entity;

/**
 * Enumeration for user account status
 */
public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    PENDING_VERIFICATION("Pending Verification");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public boolean requiresVerification() {
        return this == PENDING_VERIFICATION;
    }
}
