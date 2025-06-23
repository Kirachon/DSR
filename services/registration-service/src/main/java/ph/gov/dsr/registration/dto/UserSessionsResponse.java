package ph.gov.dsr.registration.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user sessions response
 */
public class UserSessionsResponse {

    private UUID userId;
    private String email;
    private List<SessionInfo> activeSessions;
    private Integer totalActiveSessions;

    // Nested class for session information
    public static class SessionInfo {
        private String sessionId;
        private String deviceInfo;
        private String ipAddress;
        private String location;
        private String userAgent;
        private LocalDateTime loginTime;
        private LocalDateTime lastActivity;
        private Boolean current;

        public SessionInfo() {}

        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
        public Boolean getCurrent() { return current; }
        public void setCurrent(Boolean current) { this.current = current; }

        @Override
        public String toString() {
            return "SessionInfo{" +
                    "sessionId='" + sessionId + '\'' +
                    ", deviceInfo='" + deviceInfo + '\'' +
                    ", ipAddress='" + ipAddress + '\'' +
                    ", current=" + current +
                    '}';
        }
    }

    // Constructors
    public UserSessionsResponse() {}

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

    public List<SessionInfo> getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(List<SessionInfo> activeSessions) {
        this.activeSessions = activeSessions;
        this.totalActiveSessions = activeSessions != null ? activeSessions.size() : 0;
    }

    public Integer getTotalActiveSessions() {
        return totalActiveSessions;
    }

    public void setTotalActiveSessions(Integer totalActiveSessions) {
        this.totalActiveSessions = totalActiveSessions;
    }

    @Override
    public String toString() {
        return "UserSessionsResponse{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", totalActiveSessions=" + totalActiveSessions +
                '}';
    }
}
