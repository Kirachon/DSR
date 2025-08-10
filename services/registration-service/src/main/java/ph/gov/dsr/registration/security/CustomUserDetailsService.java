package ph.gov.dsr.registration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.registration.entity.User;
import ph.gov.dsr.registration.entity.UserStatus;
import ph.gov.dsr.registration.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security
 * Loads user details from the database for authentication
 */
@Service
@Profile("!no-db")
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        logger.debug("User found: {} with role: {} and status: {}", 
                user.getEmail(), user.getRole(), user.getStatus());

        return new CustomUserPrincipal(user);
    }

    /**
     * Custom UserDetails implementation
     */
    public static class CustomUserPrincipal implements UserDetails {
        
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Convert user role to Spring Security authority
            String authority = "ROLE_" + user.getRole().name();
            return Collections.singletonList(new SimpleGrantedAuthority(authority));
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            // Account never expires in our system
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            // Check if account is locked
            return !user.isAccountLocked();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            // Credentials never expire in our system
            return true;
        }

        @Override
        public boolean isEnabled() {
            // Account is enabled if status is ACTIVE
            return user.getStatus() == UserStatus.ACTIVE;
        }

        /**
         * Get the underlying User entity
         */
        public User getUser() {
            return user;
        }

        /**
         * Get user ID
         */
        public String getUserId() {
            return user.getId().toString();
        }

        /**
         * Get user role
         */
        public String getRole() {
            return user.getRole().name();
        }

        /**
         * Get user full name
         */
        public String getFullName() {
            StringBuilder fullName = new StringBuilder();
            fullName.append(user.getFirstName());
            if (user.getMiddleName() != null && !user.getMiddleName().trim().isEmpty()) {
                fullName.append(" ").append(user.getMiddleName());
            }
            fullName.append(" ").append(user.getLastName());
            return fullName.toString();
        }

        @Override
        public String toString() {
            return "CustomUserPrincipal{" +
                    "email='" + user.getEmail() + '\'' +
                    ", role=" + user.getRole() +
                    ", status=" + user.getStatus() +
                    ", enabled=" + isEnabled() +
                    ", accountNonLocked=" + isAccountNonLocked() +
                    '}';
        }
    }
}
