package ph.gov.dsr.registration.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.entity.User;
import ph.gov.dsr.registration.entity.UserRole;
import ph.gov.dsr.registration.entity.UserStatus;
import ph.gov.dsr.registration.repository.UserRepository;
import ph.gov.dsr.registration.security.JwtUtil;
import ph.gov.dsr.registration.service.AuthService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * JWT-based implementation of AuthService
 */
@Service
@Transactional
@Profile("!no-db")
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            logger.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setStatus(UserStatus.ACTIVE); // For demo purposes, activate immediately
        user.setEmailVerified(true); // For demo purposes

        user = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", user.getId());

        // Generate JWT tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Create user info
        AuthResponse.UserInfo userInfo = createUserInfo(user);

        return new AuthResponse(accessToken, refreshToken, jwtUtil.getTokenExpirationSeconds(), userInfo);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found with email: {}", request.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Login failed: Invalid password for email: {}", request.getEmail());
            // TODO: Implement failed login attempt tracking
            throw new RuntimeException("Invalid email or password");
        }

        // Check if account is active
        if (!user.isActive()) {
            logger.warn("Login failed: Account not active for email: {}", request.getEmail());
            throw new RuntimeException("Account is not active");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("User logged in successfully: {}", user.getEmail());

        // Generate JWT tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Create user info
        AuthResponse.UserInfo userInfo = createUserInfo(user);
        userInfo.setLastLoginAt(user.getLastLoginAt());

        return new AuthResponse(accessToken, refreshToken, jwtUtil.getTokenExpirationSeconds(), userInfo);
    }

    @Override
    public MessageResponse logout(String token) {
        // For demo purposes, just return success
        return MessageResponse.success("Logged out successfully");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Refreshing token");

        try {
            // Validate refresh token
            if (!jwtUtil.isTokenType(request.getRefreshToken(), "refresh")) {
                logger.warn("Invalid refresh token type");
                throw new RuntimeException("Invalid refresh token");
            }

            // Extract user ID from refresh token
            UUID userId = jwtUtil.extractUserId(request.getRefreshToken());

            // Find user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User not found for refresh token: {}", userId);
                        return new RuntimeException("Invalid refresh token");
                    });

            // Check if user is still active
            if (!user.isActive()) {
                logger.warn("User account not active for refresh: {}", user.getEmail());
                throw new RuntimeException("Account is not active");
            }

            // Generate new tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);

            // Create user info
            AuthResponse.UserInfo userInfo = createUserInfo(user);

            logger.info("Token refreshed successfully for user: {}", user.getEmail());
            return new AuthResponse(accessToken, newRefreshToken, jwtUtil.getTokenExpirationSeconds(), userInfo);

        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token");
        }
    }

    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        return MessageResponse.success("Password reset email sent (demo mode)");
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        return MessageResponse.success("Password reset successfully (demo mode)");
    }

    @Override
    public MessageResponse changePassword(ChangePasswordRequest request) {
        return MessageResponse.success("Password changed successfully (demo mode)");
    }

    @Override
    public MessageResponse verifyEmail(String token) {
        return MessageResponse.success("Email verified successfully (demo mode)");
    }

    @Override
    public MessageResponse resendVerificationEmail(String email) {
        return MessageResponse.success("Verification email sent (demo mode)");
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        // Return mock profile for demo
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(UUID.randomUUID());
        profile.setEmail("demo@dsr.gov.ph");
        profile.setFirstName("Demo");
        profile.setLastName("User");
        profile.setRole(UserRole.CITIZEN);
        profile.setStatus(UserStatus.ACTIVE);
        return profile;
    }

    @Override
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        // Return updated mock profile for demo
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(UUID.randomUUID());
        profile.setEmail("demo@dsr.gov.ph");
        profile.setFirstName("Updated Demo");
        profile.setLastName("User");
        profile.setRole(UserRole.CITIZEN);
        profile.setStatus(UserStatus.ACTIVE);
        return profile;
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        TokenValidationResponse response = new TokenValidationResponse();
        response.setValid(true);
        response.setMessage("Token is valid (demo mode)");
        return response;
    }

    @Override
    public UserPermissionsResponse getUserPermissions() {
        UserPermissionsResponse response = new UserPermissionsResponse();
        response.setPermissions(Arrays.asList("read:profile", "write:profile", "read:registrations"));
        return response;
    }

    @Override
    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        boolean available = !userRepository.existsByEmailIgnoreCase(email);
        EmailAvailabilityResponse response = new EmailAvailabilityResponse();
        response.setAvailable(available);
        response.setEmail(email);
        response.setMessage(available ? "Email is available" : "Email is already taken");
        return response;
    }

    // Placeholder implementations for remaining methods
    @Override
    public UserSessionsResponse getUserSessions() {
        return new UserSessionsResponse();
    }

    @Override
    public MessageResponse revokeSession(UUID sessionId) {
        return MessageResponse.success("Session revoked (demo mode)");
    }

    @Override
    public MessageResponse revokeAllSessions() {
        return MessageResponse.success("All sessions revoked (demo mode)");
    }

    @Override
    public TwoFactorResponse enableTwoFactor() {
        return new TwoFactorResponse();
    }

    @Override
    public MessageResponse verifyTwoFactor(String code) {
        return MessageResponse.success("2FA verified (demo mode)");
    }

    @Override
    public MessageResponse disableTwoFactor(String code) {
        return MessageResponse.success("2FA disabled (demo mode)");
    }

    @Override
    public SecurityStatusResponse getSecurityStatus() {
        return new SecurityStatusResponse();
    }

    @Override
    public AuthResponse philsysAuth(PhilSysAuthRequest request) {
        // Mock PhilSys authentication
        String accessToken = "mock_philsys_token_" + System.currentTimeMillis();
        String refreshToken = "mock_philsys_refresh_" + System.currentTimeMillis();
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(UUID.randomUUID());
        userInfo.setEmail("philsys@dsr.gov.ph");
        userInfo.setFirstName("PhilSys");
        userInfo.setLastName("User");
        userInfo.setRole(UserRole.CITIZEN);
        userInfo.setStatus(UserStatus.ACTIVE);
        
        return new AuthResponse(accessToken, refreshToken, 86400L, userInfo);
    }

    @Override
    public MessageResponse linkPhilSysAccount(LinkPhilSysRequest request) {
        return MessageResponse.success("PhilSys account linked (demo mode)");
    }

    /**
     * Helper method to create UserInfo from User entity
     */
    private AuthResponse.UserInfo createUserInfo(User user) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        userInfo.setMiddleName(user.getMiddleName());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setRole(user.getRole());
        userInfo.setStatus(user.getStatus());
        userInfo.setEmailVerified(user.getEmailVerified());
        userInfo.setPhoneVerified(user.getPhoneVerified());

        // Set permissions based on role
        userInfo.setPermissions(getPermissionsForRole(user.getRole()));

        return userInfo;
    }

    /**
     * Helper method to get permissions based on user role
     */
    private java.util.List<String> getPermissionsForRole(UserRole role) {
        switch (role) {
            case SYSTEM_ADMIN:
                return Arrays.asList("read:all", "write:all", "delete:all", "admin:system");
            case DSWD_STAFF:
                return Arrays.asList("read:registrations", "write:registrations", "approve:registrations", "read:reports");
            case LGU_STAFF:
                return Arrays.asList("read:registrations", "write:registrations", "read:local_reports");
            case CITIZEN:
                return Arrays.asList("read:profile", "write:profile", "read:own_registrations", "create:registrations");
            default:
                return Arrays.asList("read:profile");
        }
    }
}
