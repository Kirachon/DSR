import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

import { AuthService, User, AuthTokens } from '../services/AuthService';

interface AuthState {
  user: User | null;
  tokens: AuthTokens | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

interface AuthActions {
  login: (user: User, tokens: AuthTokens) => Promise<void>;
  logout: () => Promise<void>;
  refreshTokens: (tokens: AuthTokens) => Promise<void>;
  updateUser: (user: Partial<User>) => Promise<void>;
  initialize: () => Promise<void>;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

type AuthStore = AuthState & AuthActions;

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      tokens: null,
      isAuthenticated: false,
      isLoading: true,
      error: null,

      // Actions
      login: async (user: User, tokens: AuthTokens) => {
        try {
          set({
            user,
            tokens,
            isAuthenticated: true,
            error: null,
            isLoading: false,
          });
          
          console.log('User logged in successfully:', user.username);
        } catch (error) {
          console.error('Login store update failed:', error);
          set({
            error: 'Failed to update login state',
            isLoading: false,
          });
        }
      },

      logout: async () => {
        try {
          set({
            user: null,
            tokens: null,
            isAuthenticated: false,
            error: null,
            isLoading: false,
          });
          
          // Clear persisted data
          await AsyncStorage.removeItem('auth-storage');
          
          console.log('User logged out successfully');
        } catch (error) {
          console.error('Logout failed:', error);
          set({
            error: 'Failed to logout',
            isLoading: false,
          });
        }
      },

      refreshTokens: async (tokens: AuthTokens) => {
        try {
          set({
            tokens,
            error: null,
          });
          
          console.log('Tokens refreshed successfully');
        } catch (error) {
          console.error('Token refresh failed:', error);
          set({
            error: 'Failed to refresh tokens',
          });
        }
      },

      updateUser: async (userUpdate: Partial<User>) => {
        try {
          const currentUser = get().user;
          if (!currentUser) {
            throw new Error('No user to update');
          }

          const updatedUser = { ...currentUser, ...userUpdate };
          
          set({
            user: updatedUser,
            error: null,
          });
          
          console.log('User updated successfully:', updatedUser);
        } catch (error) {
          console.error('User update failed:', error);
          set({
            error: 'Failed to update user',
          });
        }
      },

      initialize: async () => {
        try {
          set({ isLoading: true, error: null });
          
          // Check for stored tokens
          const storedTokens = await AuthService.getStoredTokens();
          const storedUser = await AuthService.getStoredUser();
          
          if (storedTokens && storedUser) {
            // Validate token expiration
            const isTokenValid = await AuthService.isTokenValid();
            
            if (isTokenValid) {
              set({
                user: storedUser,
                tokens: storedTokens,
                isAuthenticated: true,
                isLoading: false,
                error: null,
              });
              
              console.log('Auth state restored from storage');
            } else {
              // Try to refresh tokens
              try {
                const newAccessToken = await AuthService.refreshAccessToken();
                const newTokens = await AuthService.getStoredTokens();
                
                if (newTokens) {
                  set({
                    user: storedUser,
                    tokens: newTokens,
                    isAuthenticated: true,
                    isLoading: false,
                    error: null,
                  });
                  
                  console.log('Auth state restored with refreshed tokens');
                } else {
                  throw new Error('Failed to get refreshed tokens');
                }
              } catch (refreshError) {
                console.error('Token refresh failed during initialization:', refreshError);
                await get().logout();
              }
            }
          } else {
            set({
              user: null,
              tokens: null,
              isAuthenticated: false,
              isLoading: false,
              error: null,
            });
            
            console.log('No stored auth data found');
          }
        } catch (error) {
          console.error('Auth initialization failed:', error);
          set({
            user: null,
            tokens: null,
            isAuthenticated: false,
            isLoading: false,
            error: 'Failed to initialize authentication',
          });
        }
      },

      clearError: () => {
        set({ error: null });
      },

      setLoading: (loading: boolean) => {
        set({ isLoading: loading });
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({
        user: state.user,
        tokens: state.tokens,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        console.log('Auth store rehydrated:', state?.isAuthenticated ? 'authenticated' : 'not authenticated');
      },
    }
  )
);

// Selectors for easier access to specific state
export const useUser = () => useAuthStore((state) => state.user);
export const useIsAuthenticated = () => useAuthStore((state) => state.isAuthenticated);
export const useAuthLoading = () => useAuthStore((state) => state.isLoading);
export const useAuthError = () => useAuthStore((state) => state.error);
export const useTokens = () => useAuthStore((state) => state.tokens);

// Action selectors
export const useAuthActions = () => useAuthStore((state) => ({
  login: state.login,
  logout: state.logout,
  refreshTokens: state.refreshTokens,
  updateUser: state.updateUser,
  initialize: state.initialize,
  clearError: state.clearError,
  setLoading: state.setLoading,
}));
