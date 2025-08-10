package ph.gov.dsr.registration.entity;

/**
 * Enumeration for user roles in the DSR system
 */
public enum UserRole {
    CITIZEN("Citizen"),
    LGU_STAFF("LGU Staff"),
    DSWD_STAFF("DSWD Staff"),
    SYSTEM_ADMIN("System Administrator"),
    FIELD_WORKER("Field Worker"),
    CALL_CENTER_AGENT("Call Center Agent");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAdminRole() {
        return this == SYSTEM_ADMIN || this == DSWD_STAFF;
    }

    public boolean isStaffRole() {
        return this == LGU_STAFF || this == DSWD_STAFF || this == FIELD_WORKER || this == CALL_CENTER_AGENT;
    }

    public boolean canApproveRegistrations() {
        return this == LGU_STAFF || this == DSWD_STAFF || this == SYSTEM_ADMIN;
    }

    public boolean canAccessAnalytics() {
        return this == DSWD_STAFF || this == SYSTEM_ADMIN;
    }
}
