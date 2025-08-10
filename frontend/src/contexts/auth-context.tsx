'use client';

// Authentication Context Provider
// Provides authentication state and actions to the entire application

import React, { createContext, useContext, useEffect, useState } from 'react';

import { useAuthStore, initializeAuth } from '@/lib/auth-store';
import type { AuthContextType } from '@/types';

// Create the authentication context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Authentication Provider Props
interface AuthProviderProps {
  children: React.ReactNode;
}

// Authentication Provider Component
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isInitialized, setIsInitialized] = useState(false);

  // Get auth state and actions from Zustand store
  const authState = useAuthStore();

  // Initialize authentication on mount
  useEffect(() => {
    const initialize = async () => {
      try {
        await initializeAuth();
      } catch (error) {
        console.error('Failed to initialize authentication:', error);
      } finally {
        setIsInitialized(true);
      }
    };

    initialize();
  }, []);

  // Set up automatic token refresh
  useEffect(() => {
    if (!authState.isAuthenticated) return undefined;

    const interval = setInterval(async () => {
      try {
        // Check if token needs refresh (5 minutes before expiry)
        const token = authState.accessToken;
        if (token) {
          const payload = JSON.parse(atob(token.split('.')[1]));
          const currentTime = Date.now() / 1000;
          const bufferTime = 5 * 60; // 5 minutes

          if (payload.exp < currentTime + bufferTime) {
            await authState.refreshTokens();
          }
        }
      } catch (error) {
        console.error('Token refresh failed:', error);
        // If refresh fails, logout user
        await authState.logout();
      }
    }, 60000); // Check every minute

    return () => clearInterval(interval);
  }, [
    authState.isAuthenticated,
    authState.accessToken,
    authState.refreshTokens,
    authState.logout,
  ]);

  // Context value
  const contextValue: AuthContextType = {
    ...authState,
    isInitialized,
  };

  // Show loading spinner while initializing
  if (!isInitialized) {
    return (
      <div className='flex items-center justify-center min-h-screen'>
        <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600'></div>
      </div>
    );
  }

  return (
    <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
  );
};

// Hook to use authentication context
export const useAuthContext = (): AuthContextType => {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }

  return context;
};

// Convenience hooks that use the context
export const useAuth = () => {
  const { isAuthenticated, isLoading, user, error, isInitialized } =
    useAuthContext();
  return { isAuthenticated, isLoading, user, error, isInitialized };
};

export const useAuthActions = () => {
  const {
    login,
    register,
    logout,
    refreshToken,
    updateProfile,
    changePassword,
    forgotPassword,
    resetPassword,
    verifyEmail,
    resendVerification,
    clearError,
    setLoading,
  } = useAuthContext();

  return {
    login,
    register,
    logout,
    refreshToken,
    updateProfile,
    changePassword,
    forgotPassword,
    resetPassword,
    verifyEmail,
    resendVerification,
    clearError,
    setLoading,
  };
};

export const useUser = () => {
  const { user } = useAuthContext();
  return user;
};

export const usePermissions = () => {
  const { permissions } = useAuthContext();
  return permissions;
};

export const usePreferences = () => {
  const { preferences } = useAuthContext();
  return preferences;
};

// Role-based access control hooks
export const useHasRole = (requiredRole: string) => {
  const user = useUser();
  return user?.role === requiredRole;
};

export const useHasPermission = (requiredPermission: string) => {
  const permissions = usePermissions();
  return permissions.includes(requiredPermission);
};

export const useHasAnyRole = (requiredRoles: string[]) => {
  const user = useUser();
  return user ? requiredRoles.includes(user.role) : false;
};

export const useHasAnyPermission = (requiredPermissions: string[]) => {
  const permissions = usePermissions();
  return requiredPermissions.some(permission =>
    permissions.includes(permission)
  );
};

// Authentication status hooks
export const useIsAuthenticated = () => {
  const { isAuthenticated } = useAuthContext();
  return isAuthenticated;
};

export const useIsLoading = () => {
  const { isLoading } = useAuthContext();
  return isLoading;
};

export const useAuthError = () => {
  const { error } = useAuthContext();
  return error;
};

export default AuthProvider;
