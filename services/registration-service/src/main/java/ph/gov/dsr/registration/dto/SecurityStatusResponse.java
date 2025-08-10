package ph.gov.dsr.registration.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for security status response
 */
public class SecurityStatusResponse {

    private UUID userId;
    private String email;
    private Boolean twoFactorEnabled;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime lastPasswordChange;
    private LocalDateTime lastLoginAt;
    private Integer failedLoginAttempts;
    private Boolean accountLocked;
    private LocalDateTime lockoutExpiresAt;
    private List<SecurityEvent> recentSecurityEvents;

    // Nested class for security events
    public static class SecurityEvent {
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String ipAddress;
        private String userAgent;
        private String location;

        public SecurityEvent() {}

        public SecurityEvent(String eventType, String description, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.description = description;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        @Override
        public String toString() {
            return "SecurityEvent{" +
                    "eventType='" + eventType + '\'' +
                    ", description='" + description + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    // Constructors
    public SecurityStatusResponse() {}

    // Getters and Setters
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

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public LocalDateTime getLastPasswordChange() {
        return lastPasswordChange;
    }

    public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getLockoutExpiresAt() {
        return lockoutExpiresAt;
    }

    public void setLockoutExpiresAt(LocalDateTime lockoutExpiresAt) {
        this.lockoutExpiresAt = lockoutExpiresAt;
    }

    public List<SecurityEvent> getRecentSecurityEvents() {
        return recentSecurityEvents;
    }

    public void setRecentSecurityEvents(List<SecurityEvent> recentSecurityEvents) {
        this.recentSecurityEvents = recentSecurityEvents;
    }

    @Override
    public String toString() {
        return "SecurityStatusResponse{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", twoFactorEnabled=" + twoFactorEnabled +
                ", emailVerified=" + emailVerified +
                ", accountLocked=" + accountLocked +
                '}';
    }
}
