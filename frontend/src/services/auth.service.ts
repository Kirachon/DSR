// Authentication Service for DSR Frontend
// Handles all authentication-related API calls

import { api, tokenManager } from '@/lib/api-client';
import { apiEndpoints } from '@/lib/config';
import type {
  LoginRequest,
  RegisterRequest,
  RefreshTokenRequest,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  VerifyEmailRequest,
  ResendVerificationRequest,
  AuthResponse,
  MessageResponse,
  UserProfileResponse,
  SecurityStatusResponse,
  User,
} from '@/types';

export class AuthService {
  /**
   * Authenticate user with email and password
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await api.post<AuthResponse>(
        apiEndpoints.auth.login,
        credentials
      );

      // Store tokens after successful login
      if (response.accessToken && response.refreshToken) {
        tokenManager.setAccessToken(response.accessToken);
        tokenManager.setRefreshToken(response.refreshToken);
      }

      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  /**
   * Register a new user
   */
  async register(userData: RegisterRequest): Promise<AuthResponse> {
    try {
      const response = await api.post<AuthResponse>(
        apiEndpoints.auth.register,
        userData
      );

      // Store tokens after successful registration
      if (response.accessToken && response.refreshToken) {
        tokenManager.setAccessToken(response.accessToken);
        tokenManager.setRefreshToken(response.refreshToken);
      }

      return response;
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  }

  /**
   * Logout user and invalidate tokens
   */
  async logout(): Promise<MessageResponse> {
    try {
      const token = tokenManager.getAccessToken();

      if (token) {
        // Call logout endpoint to invalidate token on server
        const response = await api.post<MessageResponse>(
          apiEndpoints.auth.logout,
          {},
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        // Clear tokens from local storage
        tokenManager.clearTokens();

        return response;
      }

      // If no token, just clear local storage
      tokenManager.clearTokens();
      return {
        message: 'Logged out successfully',
        success: true,
        timestamp: new Date().toISOString(),
      };
    } catch (error) {
      // Even if logout fails on server, clear local tokens
      tokenManager.clearTokens();
      console.error('Logout error:', error);
      throw error;
    }
  }

  /**
   * Refresh access token using refresh token
   */
  async refreshToken(): Promise<AuthResponse> {
    try {
      const refreshToken = tokenManager.getRefreshToken();

      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await api.post<AuthResponse>(apiEndpoints.auth.refresh, {
        refreshToken,
      } as RefreshTokenRequest);

      // Update stored tokens
      if (response.accessToken) {
        tokenManager.setAccessToken(response.accessToken);
      }
      if (response.refreshToken) {
        tokenManager.setRefreshToken(response.refreshToken);
      }

      return response;
    } catch (error) {
      // If refresh fails, clear all tokens
      tokenManager.clearTokens();
      console.error('Token refresh failed:', error);
      throw error;
    }
  }

  /**
   * Get current user profile
   */
  async getProfile(): Promise<UserProfileResponse> {
    try {
      return await api.get<UserProfileResponse>(apiEndpoints.auth.profile);
    } catch (error) {
      console.error('Failed to get user profile:', error);
      throw error;
    }
  }

  /**
   * Update user profile
   */
  async updateProfile(updates: Partial<User>): Promise<UserProfileResponse> {
    try {
      return await api.put<UserProfileResponse>(
        apiEndpoints.auth.profile,
        updates
      );
    } catch (error) {
      console.error('Failed to update profile:', error);
      throw error;
    }
  }

  /**
   * Change user password
   */
  async changePassword(data: ChangePasswordRequest): Promise<MessageResponse> {
    try {
      return await api.post<MessageResponse>(
        apiEndpoints.auth.changePassword,
        data
      );
    } catch (error) {
      console.error('Failed to change password:', error);
      throw error;
    }
  }

  /**
   * Request password reset
   */
  async forgotPassword(data: ForgotPasswordRequest): Promise<MessageResponse> {
    try {
      return await api.post<MessageResponse>(
        apiEndpoints.auth.forgotPassword,
        data
      );
    } catch (error) {
      console.error('Failed to request password reset:', error);
      throw error;
    }
  }

  /**
   * Reset password with token
   */
  async resetPassword(data: ResetPasswordRequest): Promise<MessageResponse> {
    try {
      return await api.post<MessageResponse>(
        apiEndpoints.auth.resetPassword,
        data
      );
    } catch (error) {
      console.error('Failed to reset password:', error);
      throw error;
    }
  }

  /**
   * Verify email with token
   */
  async verifyEmail(data: VerifyEmailRequest): Promise<MessageResponse> {
    try {
      return await api.post<MessageResponse>(
        apiEndpoints.auth.verifyEmail,
        data
      );
    } catch (error) {
      console.error('Failed to verify email:', error);
      throw error;
    }
  }

  /**
   * Resend email verification
   */
  async resendVerification(
    data: ResendVerificationRequest
  ): Promise<MessageResponse> {
    try {
      return await api.post<MessageResponse>(
        apiEndpoints.auth.resendVerification,
        data
      );
    } catch (error) {
      console.error('Failed to resend verification:', error);
      throw error;
    }
  }

  /**
   * Get account security status
   */
  async getSecurityStatus(): Promise<SecurityStatusResponse> {
    try {
      return await api.get<SecurityStatusResponse>(
        apiEndpoints.auth.securityStatus
      );
    } catch (error) {
      console.error('Failed to get security status:', error);
      throw error;
    }
  }

  /**
   * Check if user is currently authenticated
   */
  isAuthenticated(): boolean {
    const token = tokenManager.getAccessToken();
    return token !== null && !tokenManager.isTokenExpired(token);
  }

  /**
   * Check if access token needs refresh
   */
  shouldRefreshToken(): boolean {
    const token = tokenManager.getAccessToken();
    return token !== null && tokenManager.shouldRefreshToken(token);
  }

  /**
   * Get current access token
   */
  getAccessToken(): string | null {
    return tokenManager.getAccessToken();
  }

  /**
   * Get current refresh token
   */
  getRefreshToken(): string | null {
    return tokenManager.getRefreshToken();
  }

  /**
   * Clear all authentication data
   */
  clearAuth(): void {
    tokenManager.clearTokens();
  }
}

// Create and export singleton instance
export const authService = new AuthService();
export default authService;
