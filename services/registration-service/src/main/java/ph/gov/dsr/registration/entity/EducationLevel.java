package ph.gov.dsr.registration.entity;

/**
 * Enumeration for education level
 */
public enum EducationLevel {
    NO_FORMAL_EDUCATION("No Formal Education"),
    ELEMENTARY_UNDERGRADUATE("Elementary Undergraduate"),
    ELEMENTARY_GRADUATE("Elementary Graduate"),
    HIGH_SCHOOL_UNDERGRADUATE("High School Undergraduate"),
    HIGH_SCHOOL_GRADUATE("High School Graduate"),
    VOCATIONAL("Vocational/Technical"),
    COLLEGE_UNDERGRADUATE("College Undergraduate"),
    COLLEGE_GRADUATE("College Graduate"),
    POST_GRADUATE("Post Graduate");

    private final String displayName;

    EducationLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHighSchoolOrAbove() {
        return this.ordinal() >= HIGH_SCHOOL_GRADUATE.ordinal();
    }

    public boolean isCollegeLevel() {
        return this == COLLEGE_UNDERGRADUATE || this == COLLEGE_GRADUATE || this == POST_GRADUATE;
    }
}
