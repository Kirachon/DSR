// Custom Authentication Hooks
// Provides convenient hooks for authentication functionality

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

import { useAuthContext, useAuthActions as useContextAuthActions } from '@/contexts';
import { authService } from '@/services';
import type {
  LoginRequest,
  RegisterRequest,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  User,
  UserRole,
} from '@/types';

// Main authentication hook
export const useAuth = () => {
  const context = useAuthContext();
  const router = useRouter();

  const loginAndRedirect = useCallback(async (
    credentials: LoginRequest,
    redirectTo: string = '/dashboard'
  ) => {
    await context.login(credentials);
    router.push(redirectTo);
  }, [context.login, router]);

  const logoutAndRedirect = useCallback(async (
    redirectTo: string = '/auth/login'
  ) => {
    await context.logout();
    router.push(redirectTo);
  }, [context.logout, router]);

  return {
    ...context,
    loginAndRedirect,
    logoutAndRedirect,
  };
};

// Authentication actions hook
export const useAuthActions = () => {
  const actions = useContextAuthActions();
  const router = useRouter();

  const registerAndRedirect = useCallback(async (
    userData: RegisterRequest,
    redirectTo: string = '/dashboard'
  ) => {
    await actions.register(userData);
    router.push(redirectTo);
  }, [actions.register, router]);

  return {
    ...actions,
    registerAndRedirect,
  };
};

// User profile hook
export const useUserProfile = () => {
  const { user, updateProfile, isLoading } = useAuth();
  const [isUpdating, setIsUpdating] = useState(false);

  const updateUserProfile = useCallback(async (updates: Partial<User>) => {
    setIsUpdating(true);
    try {
      await updateProfile(updates);
    } finally {
      setIsUpdating(false);
    }
  }, [updateProfile]);

  return {
    user,
    updateUserProfile,
    isLoading: isLoading || isUpdating,
  };
};

// Password management hook
export const usePasswordManagement = () => {
  const { changePassword, forgotPassword, resetPassword } = useAuthActions();
  const [isChanging, setIsChanging] = useState(false);
  const [isSendingReset, setIsSendingReset] = useState(false);
  const [isResetting, setIsResetting] = useState(false);

  const changeUserPassword = useCallback(async (data: ChangePasswordRequest) => {
    setIsChanging(true);
    try {
      await changePassword(data);
    } finally {
      setIsChanging(false);
    }
  }, [changePassword]);

  const sendPasswordReset = useCallback(async (data: ForgotPasswordRequest) => {
    setIsSendingReset(true);
    try {
      await forgotPassword(data);
    } finally {
      setIsSendingReset(false);
    }
  }, [forgotPassword]);

  const resetUserPassword = useCallback(async (data: ResetPasswordRequest) => {
    setIsResetting(true);
    try {
      await resetPassword(data);
    } finally {
      setIsResetting(false);
    }
  }, [resetPassword]);

  return {
    changeUserPassword,
    sendPasswordReset,
    resetUserPassword,
    isChanging,
    isSendingReset,
    isResetting,
  };
};

// Role-based access control hooks
export const useRoleAccess = () => {
  const { user } = useAuth();

  const hasRole = useCallback((role: UserRole): boolean => {
    return user?.role === role;
  }, [user?.role]);

  const hasAnyRole = useCallback((roles: UserRole[]): boolean => {
    return user ? roles.includes(user.role as UserRole) : false;
  }, [user?.role]);

  const isAdmin = useCallback((): boolean => {
    return hasRole('SYSTEM_ADMIN');
  }, [hasRole]);

  const isStaff = useCallback((): boolean => {
    return hasAnyRole(['LGU_STAFF', 'DSWD_STAFF']);
  }, [hasAnyRole]);

  const isCitizen = useCallback((): boolean => {
    return hasRole('CITIZEN');
  }, [hasRole]);

  return {
    hasRole,
    hasAnyRole,
    isAdmin,
    isStaff,
    isCitizen,
    userRole: user?.role,
  };
};

// Permission-based access control hook
export const usePermissions = () => {
  const { permissions } = useAuth();

  const hasPermission = useCallback((permission: string): boolean => {
    return permissions.includes(permission);
  }, [permissions]);

  const hasAnyPermission = useCallback((requiredPermissions: string[]): boolean => {
    return requiredPermissions.some(permission => permissions.includes(permission));
  }, [permissions]);

  const hasAllPermissions = useCallback((requiredPermissions: string[]): boolean => {
    return requiredPermissions.every(permission => permissions.includes(permission));
  }, [permissions]);

  return {
    permissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
  };
};

// Authentication status hook
export const useAuthStatus = () => {
  const { isAuthenticated, isLoading, error, user } = useAuth();
  const [isOnline, setIsOnline] = useState(true);

  // Monitor online status
  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  const isReady = !isLoading && isOnline;
  const canMakeRequests = isAuthenticated && isReady;

  return {
    isAuthenticated,
    isLoading,
    isOnline,
    isReady,
    canMakeRequests,
    error,
    user,
  };
};

// Session management hook
export const useSession = () => {
  const { user, logout, refreshToken } = useAuth();
  const [sessionWarning, setSessionWarning] = useState(false);
  const [timeUntilExpiry, setTimeUntilExpiry] = useState<number | null>(null);

  // Monitor session expiry
  useEffect(() => {
    if (!user) return;

    const token = authService.getAccessToken();
    if (!token) return;

    const checkExpiry = () => {
      try {
        const timeLeft = authService.getTimeUntilExpiration ?
          authService.getTimeUntilExpiration(token) : 0;
        setTimeUntilExpiry(timeLeft);

        // Show warning 5 minutes before expiry
        if (timeLeft <= 300 && timeLeft > 0) {
          setSessionWarning(true);
        } else {
          setSessionWarning(false);
        }

        // Auto-logout if expired
        if (timeLeft <= 0) {
          logout();
        }
      } catch (error) {
        console.error('Error checking token expiry:', error);
      }
    };

    const interval = setInterval(checkExpiry, 60000); // Check every minute
    checkExpiry(); // Initial check

    return () => clearInterval(interval);
  }, [user, logout]);

  const extendSession = useCallback(async () => {
    try {
      await refreshToken();
      setSessionWarning(false);
    } catch (error) {
      console.error('Failed to extend session:', error);
      await logout();
    }
  }, [refreshToken, logout]);

  const formatTimeLeft = useCallback((seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  }, []);

  return {
    sessionWarning,
    timeUntilExpiry,
    extendSession,
    formatTimeLeft,
  };
};

// Local storage sync hook
export const useAuthSync = () => {
  const { user, logout } = useAuth();

  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      // If tokens are cleared in another tab, logout current session
      if (e.key === 'dsr_access_token' && e.newValue === null) {
        logout();
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [logout]);

  return { user };
};

// Authentication redirect hook
export const useAuthRedirect = () => {
  const { isAuthenticated } = useAuth();
  const router = useRouter();

  const redirectIfAuthenticated = useCallback((to: string = '/dashboard') => {
    if (isAuthenticated) {
      router.push(to);
    }
  }, [isAuthenticated, router]);

  const redirectIfNotAuthenticated = useCallback((to: string = '/auth/login') => {
    if (!isAuthenticated) {
      router.push(to);
    }
  }, [isAuthenticated, router]);

  return {
    redirectIfAuthenticated,
    redirectIfNotAuthenticated,
  };
};

// Export all hooks
export {
  useUserProfile,
  usePasswordManagement,
  useRoleAccess,
  usePermissions,
  useAuthStatus,
  useSession,
  useAuthSync,
  useAuthRedirect,
};
