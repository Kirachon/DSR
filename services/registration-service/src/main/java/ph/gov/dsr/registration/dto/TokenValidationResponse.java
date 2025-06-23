package ph.gov.dsr.registration.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for token validation response
 */
public class TokenValidationResponse {

    private Boolean valid;
    private String message;
    private UUID userId;
    private String email;
    private List<String> roles;
    private List<String> permissions;
    private LocalDateTime expiresAt;
    private LocalDateTime issuedAt;
    private String tokenType;

    // Constructors
    public TokenValidationResponse() {}

    public TokenValidationResponse(Boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    // Getters and Setters
    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "TokenValidationResponse{" +
                "valid=" + valid +
                ", message='" + message + '\'' +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", tokenType='" + tokenType + '\'' +
                '}';
    }
}
