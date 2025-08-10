package ph.gov.dsr.registration.dto;

/**
 * DTO for email availability response
 */
public class EmailAvailabilityResponse {

    private String email;
    private Boolean available;
    private String message;
    private String suggestion;

    // Constructors
    public EmailAvailabilityResponse() {}

    public EmailAvailabilityResponse(String email, Boolean available, String message) {
        this.email = email;
        this.available = available;
        this.message = message;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public String toString() {
        return "EmailAvailabilityResponse{" +
                "email='" + email + '\'' +
                ", available=" + available +
                ", message='" + message + '\'' +
                '}';
    }
}
