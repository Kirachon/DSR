package ph.gov.dsr.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.service.AuthService;

import java.util.UUID;

/**
 * REST Controller for Authentication operations
 * Handles user registration, login, logout, and password management
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User Authentication and Authorization API")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User registration", 
               description = "Register a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "User login", 
               description = "Authenticate user and return access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User logout", 
               description = "Logout user and invalidate tokens")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String token) {
        MessageResponse response = authService.logout(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token", 
               description = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Forgot password", 
               description = "Send password reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset password", 
               description = "Reset password using reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change password", 
               description = "Change user password (requires authentication)")
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = authService.changePassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify email", 
               description = "Verify user email address using verification token")
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        MessageResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resend verification email", 
               description = "Resend email verification link")
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationEmail(@RequestParam String email) {
        MessageResponse response = authService.resendVerificationEmail(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user profile", 
               description = "Get authenticated user's profile information")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse response = authService.getCurrentUserProfile();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user profile", 
               description = "Update authenticated user's profile information")
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = authService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate token", 
               description = "Validate if access token is still valid")
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String token) {
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user permissions", 
               description = "Get current user's permissions and roles")
    @GetMapping("/permissions")
    public ResponseEntity<UserPermissionsResponse> getUserPermissions() {
        UserPermissionsResponse response = authService.getUserPermissions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check email availability", 
               description = "Check if email address is available for registration")
    @GetMapping("/check-email")
    public ResponseEntity<EmailAvailabilityResponse> checkEmailAvailability(@RequestParam String email) {
        EmailAvailabilityResponse response = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user sessions", 
               description = "Get all active sessions for current user")
    @GetMapping("/sessions")
    public ResponseEntity<UserSessionsResponse> getUserSessions() {
        UserSessionsResponse response = authService.getUserSessions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revoke session", 
               description = "Revoke a specific user session")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<MessageResponse> revokeSession(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId) {
        MessageResponse response = authService.revokeSession(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revoke all sessions", 
               description = "Revoke all user sessions except current")
    @DeleteMapping("/sessions")
    public ResponseEntity<MessageResponse> revokeAllSessions() {
        MessageResponse response = authService.revokeAllSessions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable two-factor authentication", 
               description = "Enable 2FA for user account")
    @PostMapping("/2fa/enable")
    public ResponseEntity<TwoFactorResponse> enableTwoFactor() {
        TwoFactorResponse response = authService.enableTwoFactor();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify two-factor authentication", 
               description = "Verify 2FA setup with TOTP code")
    @PostMapping("/2fa/verify")
    public ResponseEntity<MessageResponse> verifyTwoFactor(@RequestParam String code) {
        MessageResponse response = authService.verifyTwoFactor(code);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Disable two-factor authentication", 
               description = "Disable 2FA for user account")
    @PostMapping("/2fa/disable")
    public ResponseEntity<MessageResponse> disableTwoFactor(@RequestParam String code) {
        MessageResponse response = authService.disableTwoFactor(code);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get account security status", 
               description = "Get user account security information")
    @GetMapping("/security-status")
    public ResponseEntity<SecurityStatusResponse> getSecurityStatus() {
        SecurityStatusResponse response = authService.getSecurityStatus();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "PhilSys authentication", 
               description = "Authenticate using PhilSys QR code")
    @PostMapping("/philsys")
    public ResponseEntity<AuthResponse> philsysAuth(@Valid @RequestBody PhilSysAuthRequest request) {
        AuthResponse response = authService.philsysAuth(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Link PhilSys account", 
               description = "Link existing account with PhilSys")
    @PostMapping("/philsys/link")
    public ResponseEntity<MessageResponse> linkPhilSysAccount(@Valid @RequestBody LinkPhilSysRequest request) {
        MessageResponse response = authService.linkPhilSysAccount(request);
        return ResponseEntity.ok(response);
    }
}
