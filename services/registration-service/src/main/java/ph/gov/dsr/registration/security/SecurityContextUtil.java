package ph.gov.dsr.registration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ph.gov.dsr.registration.entity.User;
import ph.gov.dsr.registration.entity.UserRole;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for accessing current user context and security information
 */
@Component
public class SecurityContextUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContextUtil.class);

    /**
     * Get the current authenticated user
     */
    public Optional<User> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetailsService.CustomUserPrincipal) {
                CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                    (CustomUserDetailsService.CustomUserPrincipal) principal;
                return Optional.of(userPrincipal.getUser());
            }
            
            return Optional.empty();
        } catch (Exception e) {
            logger.debug("Error getting current user: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get the current user's ID
     */
    public Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * Get the current user's email
     */
    public Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(User::getEmail);
    }

    /**
     * Get the current user's role
     */
    public Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser().map(User::getRole);
    }

    /**
     * Get the current user's full name
     */
    public Optional<String> getCurrentUserFullName() {
        return getCurrentUser().map(user -> {
            StringBuilder fullName = new StringBuilder();
            fullName.append(user.getFirstName());
            if (user.getMiddleName() != null && !user.getMiddleName().trim().isEmpty()) {
                fullName.append(" ").append(user.getMiddleName());
            }
            fullName.append(" ").append(user.getLastName());
            return fullName.toString();
        });
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(UserRole role) {
        return getCurrentUserRole()
                .map(userRole -> userRole == role)
                .orElse(false);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public boolean hasAnyRole(UserRole... roles) {
        Optional<UserRole> currentRole = getCurrentUserRole();
        if (currentRole.isEmpty()) {
            return false;
        }
        
        for (UserRole role : roles) {
            if (currentRole.get() == role) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user is a system admin
     */
    public boolean isSystemAdmin() {
        return hasRole(UserRole.SYSTEM_ADMIN);
    }

    /**
     * Check if current user is DSWD staff
     */
    public boolean isDswdStaff() {
        return hasRole(UserRole.DSWD_STAFF);
    }

    /**
     * Check if current user is LGU staff
     */
    public boolean isLguStaff() {
        return hasRole(UserRole.LGU_STAFF);
    }

    /**
     * Check if current user is a citizen
     */
    public boolean isCitizen() {
        return hasRole(UserRole.CITIZEN);
    }

    /**
     * Check if current user is staff (LGU or DSWD)
     */
    public boolean isStaff() {
        return hasAnyRole(UserRole.LGU_STAFF, UserRole.DSWD_STAFF);
    }

    /**
     * Check if current user has administrative privileges
     */
    public boolean hasAdminPrivileges() {
        return hasAnyRole(UserRole.SYSTEM_ADMIN, UserRole.DSWD_STAFF, UserRole.LGU_STAFF);
    }

    /**
     * Check if the current user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Get the current authentication object
     */
    public Optional<Authentication> getCurrentAuthentication() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return Optional.ofNullable(authentication);
        } catch (Exception e) {
            logger.debug("Error getting current authentication: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get the current user's authorities as strings
     */
    public Optional<String[]> getCurrentUserAuthorities() {
        return getCurrentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toArray(String[]::new));
    }

    /**
     * Check if current user can access resource owned by specific user
     */
    public boolean canAccessUserResource(UUID resourceOwnerId) {
        // System admin can access everything
        if (isSystemAdmin()) {
            return true;
        }
        
        // Staff can access resources in their jurisdiction
        if (isStaff()) {
            return true; // TODO: Implement jurisdiction-based access control
        }
        
        // Citizens can only access their own resources
        if (isCitizen()) {
            return getCurrentUserId()
                    .map(currentUserId -> currentUserId.equals(resourceOwnerId))
                    .orElse(false);
        }
        
        return false;
    }

    /**
     * Get current user for logging purposes (safe string representation)
     */
    public String getCurrentUserForLogging() {
        return getCurrentUser()
                .map(user -> String.format("User[id=%s, email=%s, role=%s]", 
                        user.getId(), user.getEmail(), user.getRole()))
                .orElse("Anonymous");
    }

    /**
     * Clear the security context (useful for testing)
     */
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
