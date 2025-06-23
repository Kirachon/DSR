package ph.gov.dsr.registration.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.entity.User;
import ph.gov.dsr.registration.entity.UserRole;
import ph.gov.dsr.registration.entity.UserStatus;
import ph.gov.dsr.registration.security.JwtUtil;
import ph.gov.dsr.registration.security.MockUserDetailsService;
import ph.gov.dsr.registration.service.AuthService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Mock implementation of AuthService for no-database mode
 */
@Service
@Profile("no-db")
public class MockAuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(MockAuthServiceImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MockUserDetailsService mockUserDetailsService;

    @Autowired
    public MockAuthServiceImpl(@Lazy PasswordEncoder passwordEncoder, @Lazy JwtUtil jwtUtil,
                              @Lazy MockUserDetailsService mockUserDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mockUserDetailsService = mockUserDetailsService;
    }



    @Override
    public AuthResponse register(RegisterRequest request) {
        logger.info("Mock registration for email: {}", request.getEmail());

        // Check if email already exists
        if (mockUserDetailsService.emailExists(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        mockUserDetailsService.addUser(user);
        logger.info("Mock user registered successfully with ID: {}", user.getId());

        // Generate JWT tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Create user info
        AuthResponse.UserInfo userInfo = createUserInfo(user);

        return new AuthResponse(accessToken, refreshToken, jwtUtil.getTokenExpirationSeconds(), userInfo);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        logger.info("Mock login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = mockUserDetailsService.getUserByEmail(request.getEmail());
        if (user == null) {
            logger.warn("Mock login failed: User not found with email: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Mock login failed: Invalid password for email: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        // Check if account is active
        if (!user.isActive()) {
            logger.warn("Mock login failed: Account not active for email: {}", request.getEmail());
            throw new RuntimeException("Account is not active");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        logger.info("Mock user logged in successfully: {}", user.getEmail());

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
        logger.info("Mock logout");
        return MessageResponse.success("Logged out successfully (mock mode)");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Mock token refresh");
        
        try {
            // Validate refresh token
            if (!jwtUtil.isTokenType(request.getRefreshToken(), "refresh")) {
                throw new RuntimeException("Invalid refresh token");
            }
            
            // Extract user ID from refresh token
            UUID userId = jwtUtil.extractUserId(request.getRefreshToken());
            String email = jwtUtil.extractEmail(request.getRefreshToken());
            
            // Find user
            User user = mockUserDetailsService.getUserByEmail(email);
            if (user == null || !user.getId().equals(userId)) {
                throw new RuntimeException("Invalid refresh token");
            }
            
            // Generate new tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);
            
            // Create user info
            AuthResponse.UserInfo userInfo = createUserInfo(user);
            
            logger.info("Mock token refreshed successfully for user: {}", user.getEmail());
            return new AuthResponse(accessToken, newRefreshToken, jwtUtil.getTokenExpirationSeconds(), userInfo);
            
        } catch (Exception e) {
            logger.error("Mock token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token");
        }
    }

    // Placeholder implementations for other methods
    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        return MessageResponse.success("Password reset email sent (mock mode)");
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        return MessageResponse.success("Password reset successfully (mock mode)");
    }

    @Override
    public MessageResponse changePassword(ChangePasswordRequest request) {
        return MessageResponse.success("Password changed successfully (mock mode)");
    }

    @Override
    public MessageResponse verifyEmail(String token) {
        return MessageResponse.success("Email verified successfully (mock mode)");
    }

    @Override
    public MessageResponse resendVerificationEmail(String email) {
        return MessageResponse.success("Verification email sent (mock mode)");
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() {
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
        try {
            jwtUtil.validateToken(jwtUtil.extractTokenFromHeader(token));
            TokenValidationResponse response = new TokenValidationResponse();
            response.setValid(true);
            response.setMessage("Token is valid");
            return response;
        } catch (Exception e) {
            TokenValidationResponse response = new TokenValidationResponse();
            response.setValid(false);
            response.setMessage("Token is invalid: " + e.getMessage());
            return response;
        }
    }

    @Override
    public UserPermissionsResponse getUserPermissions() {
        UserPermissionsResponse response = new UserPermissionsResponse();
        response.setPermissions(Arrays.asList("read:profile", "write:profile", "read:registrations"));
        return response;
    }

    @Override
    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        boolean available = !mockUserDetailsService.emailExists(email);
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
        return MessageResponse.success("Session revoked (mock mode)");
    }

    @Override
    public MessageResponse revokeAllSessions() {
        return MessageResponse.success("All sessions revoked (mock mode)");
    }

    @Override
    public TwoFactorResponse enableTwoFactor() {
        return new TwoFactorResponse();
    }

    @Override
    public MessageResponse verifyTwoFactor(String code) {
        return MessageResponse.success("2FA verified (mock mode)");
    }

    @Override
    public MessageResponse disableTwoFactor(String code) {
        return MessageResponse.success("2FA disabled (mock mode)");
    }

    @Override
    public SecurityStatusResponse getSecurityStatus() {
        return new SecurityStatusResponse();
    }

    @Override
    public AuthResponse philsysAuth(PhilSysAuthRequest request) {
        User citizenUser = mockUserDetailsService.getUserByEmail("citizen@dsr.gov.ph");
        if (citizenUser == null) {
            throw new RuntimeException("Demo citizen user not found");
        }

        String accessToken = jwtUtil.generateAccessToken(citizenUser);
        String refreshToken = jwtUtil.generateRefreshToken(citizenUser);

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(UUID.randomUUID());
        userInfo.setEmail("philsys@dsr.gov.ph");
        userInfo.setFirstName("PhilSys");
        userInfo.setLastName("User");
        userInfo.setRole(UserRole.CITIZEN);
        userInfo.setStatus(UserStatus.ACTIVE);

        return new AuthResponse(accessToken, refreshToken, jwtUtil.getTokenExpirationSeconds(), userInfo);
    }

    @Override
    public MessageResponse linkPhilSysAccount(LinkPhilSysRequest request) {
        return MessageResponse.success("PhilSys account linked (mock mode)");
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
