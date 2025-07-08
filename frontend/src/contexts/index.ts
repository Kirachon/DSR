// Central export for all contexts
// DSR Frontend Context Providers

// Authentication Context
export {
  AuthProvider,
  useAuthContext,
  useAuth,
  useAuthActions,
  useUser,
  usePermissions,
  usePreferences,
  useHasRole,
  useHasPermission,
  useHasAnyRole,
  useHasAnyPermission,
  useIsAuthenticated,
  useIsLoading,
  useAuthError,
} from './auth-context';
