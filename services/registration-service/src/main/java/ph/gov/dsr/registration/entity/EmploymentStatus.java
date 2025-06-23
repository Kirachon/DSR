package ph.gov.dsr.registration.entity;

/**
 * Enumeration for employment status
 */
public enum EmploymentStatus {
    EMPLOYED("Employed"),
    UNEMPLOYED("Unemployed"),
    SELF_EMPLOYED("Self-Employed"),
    RETIRED("Retired"),
    STUDENT("Student"),
    HOMEMAKER("Homemaker"),
    DISABLED("Disabled"),
    OTHER("Other");

    private final String displayName;

    EmploymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isWorking() {
        return this == EMPLOYED || this == SELF_EMPLOYED;
    }

    public boolean isEconomicallyActive() {
        return this == EMPLOYED || this == SELF_EMPLOYED || this == UNEMPLOYED;
    }

    public boolean isEconomicallyInactive() {
        return this == RETIRED || this == STUDENT || this == HOMEMAKER || this == DISABLED;
    }
}
