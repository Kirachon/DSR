package ph.gov.dsr.registration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ph.gov.dsr.registration.entity.User;
import ph.gov.dsr.registration.entity.UserRole;
import ph.gov.dsr.registration.entity.UserStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock UserDetailsService implementation for no-database mode
 * Self-contained to avoid circular dependencies
 */
@Service
@Primary
@Profile("no-db")
public class MockUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(MockUserDetailsService.class);

    private final Map<String, User> users = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public MockUserDetailsService() {
        createDemoUsers();
    }

    private void createDemoUsers() {
        // Create demo admin user
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@dsr.gov.ph");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setRole(UserRole.SYSTEM_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);
        users.put(admin.getEmail(), admin);

        // Create demo citizen user
        User citizen = new User();
        citizen.setId(UUID.randomUUID());
        citizen.setEmail("citizen@dsr.gov.ph");
        citizen.setPasswordHash(passwordEncoder.encode("citizen123"));
        citizen.setFirstName("Demo");
        citizen.setLastName("Citizen");
        citizen.setRole(UserRole.CITIZEN);
        citizen.setStatus(UserStatus.ACTIVE);
        citizen.setEmailVerified(true);
        users.put(citizen.getEmail(), citizen);

        logger.info("Created demo users: admin@dsr.gov.ph (admin123), citizen@dsr.gov.ph (citizen123)");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading mock user by email: {}", email);

        User user = users.get(email.toLowerCase());
        if (user == null) {
            logger.warn("Mock user not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        logger.debug("Mock user found: {} with role: {} and status: {}",
                user.getEmail(), user.getRole(), user.getStatus());

        return new CustomUserDetailsService.CustomUserPrincipal(user);
    }

    /**
     * Get user by email (for mock authentication)
     */
    public User getUserByEmail(String email) {
        return users.get(email.toLowerCase());
    }

    /**
     * Add user (for mock registration)
     */
    public void addUser(User user) {
        users.put(user.getEmail().toLowerCase(), user);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return users.containsKey(email.toLowerCase());
    }
}
