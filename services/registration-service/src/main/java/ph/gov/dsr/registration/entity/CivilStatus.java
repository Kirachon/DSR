package ph.gov.dsr.registration.entity;

/**
 * Enumeration for civil status
 */
public enum CivilStatus {
    SINGLE("Single"),
    MARRIED("Married"),
    WIDOWED("Widowed"),
    SEPARATED("Separated"),
    DIVORCED("Divorced"),
    LIVE_IN("Live-in");

    private final String displayName;

    CivilStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isMarried() {
        return this == MARRIED || this == LIVE_IN;
    }

    public boolean isSingle() {
        return this == SINGLE || this == SEPARATED || this == DIVORCED || this == WIDOWED;
    }
}
