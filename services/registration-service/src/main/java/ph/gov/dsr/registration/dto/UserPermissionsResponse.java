package ph.gov.dsr.registration.dto;

import ph.gov.dsr.registration.entity.UserRole;

import java.util.List;
import java.util.UUID;

/**
 * DTO for user permissions response
 */
public class UserPermissionsResponse {

    private UUID userId;
    private String email;
    private UserRole role;
    private List<String> permissions;
    private List<String> modules;
    private List<String> actions;

    // Nested class for permission details
    public static class PermissionDetail {
        private String module;
        private String action;
        private String resource;
        private Boolean granted;

        public PermissionDetail() {}

        public PermissionDetail(String module, String action, String resource, Boolean granted) {
            this.module = module;
            this.action = action;
            this.resource = resource;
            this.granted = granted;
        }

        // Getters and Setters
        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }
        public Boolean getGranted() { return granted; }
        public void setGranted(Boolean granted) { this.granted = granted; }
    }

    private List<PermissionDetail> detailedPermissions;

    // Constructors
    public UserPermissionsResponse() {}

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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public List<PermissionDetail> getDetailedPermissions() {
        return detailedPermissions;
    }

    public void setDetailedPermissions(List<PermissionDetail> detailedPermissions) {
        this.detailedPermissions = detailedPermissions;
    }

    @Override
    public String toString() {
        return "UserPermissionsResponse{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", permissions=" + (permissions != null ? permissions.size() : 0) + " items" +
                '}';
    }
}
