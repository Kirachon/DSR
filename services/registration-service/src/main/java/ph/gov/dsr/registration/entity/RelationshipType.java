package ph.gov.dsr.registration.entity;

/**
 * Enumeration for relationship to head of household
 */
public enum RelationshipType {
    HEAD("Head of Household"),
    SPOUSE("Spouse"),
    CHILD("Child"),
    PARENT("Parent"),
    SIBLING("Sibling"),
    GRANDPARENT("Grandparent"),
    GRANDCHILD("Grandchild"),
    OTHER_RELATIVE("Other Relative"),
    NON_RELATIVE("Non-Relative");

    private final String displayName;

    RelationshipType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDirectFamily() {
        return this == HEAD || this == SPOUSE || this == CHILD || this == PARENT;
    }

    public boolean isExtendedFamily() {
        return this == SIBLING || this == GRANDPARENT || this == GRANDCHILD || this == OTHER_RELATIVE;
    }
}
