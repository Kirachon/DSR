package ph.gov.dsr.registration.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO for updating an existing registration
 */
public class RegistrationUpdateRequest {

    @Min(value = 1, message = "Priority level must be between 1 and 5")
    @Max(value = 5, message = "Priority level must be between 1 and 5")
    private Integer priorityLevel;

    private String notes;

    // Constructors
    public RegistrationUpdateRequest() {}

    public RegistrationUpdateRequest(Integer priorityLevel, String notes) {
        this.priorityLevel = priorityLevel;
        this.notes = notes;
    }

    // Getters and Setters
    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "RegistrationUpdateRequest{" +
                "priorityLevel=" + priorityLevel +
                ", notes='" + notes + '\'' +
                '}';
    }
}
