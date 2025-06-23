package ph.gov.dsr.registration.entity;

/**
 * Enumeration for registration channels
 */
public enum RegistrationChannel {
    WEB_PORTAL("Web Portal"),
    MOBILE_APP("Mobile App"),
    FIELD_REGISTRATION("Field Registration"),
    CALL_CENTER("Call Center"),
    WALK_IN("Walk-in");

    private final String displayName;

    RegistrationChannel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDigital() {
        return this == WEB_PORTAL || this == MOBILE_APP;
    }

    public boolean requiresStaffAssistance() {
        return this == FIELD_REGISTRATION || this == CALL_CENTER || this == WALK_IN;
    }
}
