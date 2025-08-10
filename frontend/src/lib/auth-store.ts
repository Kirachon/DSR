// Authentication Store using Zustand
// Centralized state management for authentication

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';

import { authService } from '@/services';
import type {
  AuthStore,
  User,
  UserPreferences,
  LoginRequest,
  RegisterRequest,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  VerifyEmailRequest,
  ResendVerificationRequest,
} from '@/types';

// Default user preferences
const defaultPreferences: UserPreferences = {
  theme: 'light',
  language: 'en',
  timezone: 'UTC',
  emailNotifications: true,
  smsNotifications: false,
  pushNotifications: false,
};

// Create the authentication store
export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        isAuthenticated: false,
        isLoading: false,
        user: null,
        accessToken: null,
        refreshToken: null,
        permissions: [],
        preferences: null,
        error: null,

        // Actions
        login: async (credentials: LoginRequest) => {
          set({ isLoading: true, error: null });

          try {
            const response = await authService.login(credentials);

            set({
              isAuthenticated: true,
              isLoading: false,
              user: response.user,
              accessToken: response.accessToken,
              refreshToken: response.refreshToken,
              error: null,
            });

            // Fetch additional user data
            await get().loadUserProfile();
          } catch (error: any) {
            set({
              isAuthenticated: false,
              isLoading: false,
              user: null,
              accessToken: null,
              refreshToken: null,
              error: error.message || 'Login failed',
            });
            throw error;
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true, error: null });

          try {
            const response = await authService.register(userData);

            set({
              isAuthenticated: true,
              isLoading: false,
              user: response.user,
              accessToken: response.accessToken,
              refreshToken: response.refreshToken,
              error: null,
            });

            // Fetch additional user data
            await get().loadUserProfile();
          } catch (error: any) {
            set({
              isAuthenticated: false,
              isLoading: false,
              user: null,
              accessToken: null,
              refreshToken: null,
              error: error.message || 'Registration failed',
            });
            throw error;
          }
        },

        logout: async () => {
          set({ isLoading: true });

          try {
            await authService.logout();
          } catch (error) {
            console.error('Logout error:', error);
          } finally {
            // Always clear state regardless of API call result
            set({
              isAuthenticated: false,
              isLoading: false,
              user: null,
              accessToken: null,
              refreshToken: null,
              permissions: [],
              preferences: null,
              error: null,
            });
          }
        },

        refreshTokens: async () => {
          try {
            const response = await authService.refreshToken();

            set({
              accessToken: response.accessToken,
              refreshToken: response.refreshToken,
              user: response.user,
            });
          } catch (error: any) {
            // If refresh fails, logout user
            await get().logout();
            throw error;
          }
        },

        updateProfile: async (updates: Partial<User>) => {
          set({ isLoading: true, error: null });

          try {
            const response = await authService.updateProfile(updates);

            set({
              isLoading: false,
              user: response.user,
              preferences: response.preferences,
              permissions: response.permissions,
            });
          } catch (error: any) {
            set({
              isLoading: false,
              error: error.message || 'Failed to update profile',
            });
            throw error;
          }
        },

        changePassword: async (data: ChangePasswordRequest) => {
          set({ isLoading: true, error: null });

          try {
            await authService.changePassword(data);
            set({ isLoading: false });
          } catch (error: any) {
            set({
              isLoading: false,
              error: error.message || 'Failed to change password',
            });
            throw error;
          }
        },

        forgotPassword: async (data: ForgotPasswordRequest) => {
          set({ isLoading: true, error: null });

          try {
            await authService.forgotPassword(data);
            set({ isLoading: false });
          } catch (error: any) {
            set({
              isLoading: false,
              error: error.message || 'Failed to send password reset email',
            });
            throw error;
          }
        },

        resetPassword: async (data: ResetPasswordRequest) => {
          set({ isLoading: true, error: null });

          try {
            await authService.resetPassword(data);
            set({ isLoading: false });
          } catch (error: any) {
            set({
              isLoading: false,
              error: error.message || 'Failed to reset password',
            });
            throw error;
          }
        },

        verifyEmail: async (data: VerifyEmailRequest) => {
          set({ isLoading: true, error: null });

          try {
            await authService.verifyEmail(data);

            // Update user's email verification status
            const currentUser = get().user;
            if (currentUser) {
              set({
                isLoading: false,
                user: { ...currentUser, emailVerified: true },
              });
            } else {
              set({ isLoading: false });
            }
          } catch (error: any) {
            set({
              isLoading: false,
              error: error.message || 'Failed to verify email',
            });
            throw error;
          }
        },

        resendVerification: async (data: ResendVerificationRequest) => {
          set({ isLoading: true, error: null });

          try {
            await authService.resendVerification(data);
            set({ isLoading: false });
          } catch (error: any) {
            set({
              isLoading: false,
              error: error.message || 'Failed to resend verification email',
            });
            throw error;
          }
        },

        clearError: () => {
          set({ error: null });
        },

        setLoading: (loading: boolean) => {
          set({ isLoading: loading });
        },

        // Helper method to load user profile
        loadUserProfile: async () => {
          try {
            const response = await authService.getProfile();
            set({
              user: response.user,
              permissions: response.permissions,
              preferences: response.preferences || defaultPreferences,
            });
          } catch (error) {
            console.error('Failed to load user profile:', error);
          }
        },

        // Helper method to initialize auth state
        initialize: async () => {
          const isAuthenticated = authService.isAuthenticated();

          if (isAuthenticated) {
            set({ isAuthenticated: true });

            try {
              // Check if token needs refresh
              if (authService.shouldRefreshToken()) {
                await get().refreshTokens();
              }

              // Load user profile
              await get().loadUserProfile();
            } catch (error) {
              console.error('Auth initialization failed:', error);
              await get().logout();
            }
          } else {
            set({ isAuthenticated: false });
          }
        },
      }),
      {
        name: 'dsr-auth-store',
        partialize: state => ({
          // Only persist essential data, not sensitive tokens
          user: state.user,
          preferences: state.preferences,
          permissions: state.permissions,
        }),
      }
    ),
    {
      name: 'auth-store',
    }
  )
);

// Selector hooks for specific parts of the auth state
// Note: Main auth hooks are exported from auth-context.tsx to avoid conflicts
export const useAuthStoreState = () =>
  useAuthStore(state => ({
    isAuthenticated: state.isAuthenticated,
    isLoading: state.isLoading,
    user: state.user,
    error: state.error,
  }));

export const useAuthStoreActions = () =>
  useAuthStore(state => ({
    login: state.login,
    register: state.register,
    logout: state.logout,
    refreshTokens: state.refreshTokens,
    updateProfile: state.updateProfile,
    changePassword: state.changePassword,
    forgotPassword: state.forgotPassword,
    resetPassword: state.resetPassword,
    verifyEmail: state.verifyEmail,
    resendVerification: state.resendVerification,
    clearError: state.clearError,
    setLoading: state.setLoading,
  }));

export const useUserFromStore = () => useAuthStore(state => state.user);
export const usePermissionsFromStore = () => useAuthStore(state => state.permissions);
export const usePreferencesFromStore = () => useAuthStore(state => state.preferences);

// Initialize auth store
export const initializeAuth = () => useAuthStore.getState().initialize();
