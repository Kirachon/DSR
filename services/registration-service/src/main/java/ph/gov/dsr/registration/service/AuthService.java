package ph.gov.dsr.registration.service;

import ph.gov.dsr.registration.dto.*;

import java.util.UUID;

/**
 * Service interface for Authentication operations
 */
public interface AuthService {

    /**
     * Register a new user
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate user login
     */
    AuthResponse login(LoginRequest request);

    /**
     * Logout user
     */
    MessageResponse logout(String token);

    /**
     * Refresh access token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Send forgot password email
     */
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password using token
     */
    MessageResponse resetPassword(ResetPasswordRequest request);

    /**
     * Change user password
     */
    MessageResponse changePassword(ChangePasswordRequest request);

    /**
     * Verify email address
     */
    MessageResponse verifyEmail(String token);

    /**
     * Resend verification email
     */
    MessageResponse resendVerificationEmail(String email);

    /**
     * Get current user profile
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * Update user profile
     */
    UserProfileResponse updateProfile(UpdateProfileRequest request);

    /**
     * Validate token
     */
    TokenValidationResponse validateToken(String token);

    /**
     * Get user permissions
     */
    UserPermissionsResponse getUserPermissions();

    /**
     * Check email availability
     */
    EmailAvailabilityResponse checkEmailAvailability(String email);

    /**
     * Get user sessions
     */
    UserSessionsResponse getUserSessions();

    /**
     * Revoke specific session
     */
    MessageResponse revokeSession(UUID sessionId);

    /**
     * Revoke all sessions
     */
    MessageResponse revokeAllSessions();

    /**
     * Enable two-factor authentication
     */
    TwoFactorResponse enableTwoFactor();

    /**
     * Verify two-factor authentication
     */
    MessageResponse verifyTwoFactor(String code);

    /**
     * Disable two-factor authentication
     */
    MessageResponse disableTwoFactor(String code);

    /**
     * Get security status
     */
    SecurityStatusResponse getSecurityStatus();

    /**
     * PhilSys authentication
     */
    AuthResponse philsysAuth(PhilSysAuthRequest request);

    /**
     * Link PhilSys account
     */
    MessageResponse linkPhilSysAccount(LinkPhilSysRequest request);
}
