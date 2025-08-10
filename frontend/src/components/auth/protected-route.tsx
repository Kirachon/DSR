'use client';

// Protected Route Component
// Handles authentication and authorization for protected pages

import { redirect } from 'next/navigation';
import React from 'react';

import { useAuth, useHasRole, useHasPermission } from '@/contexts';
import type { ProtectedRouteProps } from '@/types';
import { UserRole } from '@/types';

// Loading component
const LoadingSpinner: React.FC = () => (
  <div className='flex items-center justify-center min-h-screen'>
    <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600'></div>
  </div>
);

// Unauthorized component
const UnauthorizedAccess: React.FC<{ message?: string }> = ({
  message = "You don't have permission to access this page.",
}) => (
  <div className='flex items-center justify-center min-h-screen'>
    <div className='text-center'>
      <div className='mb-4'>
        <svg
          className='mx-auto h-16 w-16 text-error-500'
          fill='none'
          viewBox='0 0 24 24'
          stroke='currentColor'
        >
          <path
            strokeLinecap='round'
            strokeLinejoin='round'
            strokeWidth={2}
            d='M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 19.5c-.77.833.192 2.5 1.732 2.5z'
          />
        </svg>
      </div>
      <h1 className='text-2xl font-bold text-gray-900 mb-2'>Access Denied</h1>
      <p className='text-gray-600 mb-6'>{message}</p>
      <button
        onClick={() => window.history.back()}
        className='inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500'
      >
        Go Back
      </button>
    </div>
  </div>
);

// Protected Route Component
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  requiredPermissions = [],
  fallback,
}) => {
  const { isAuthenticated, isLoading, user, isInitialized } = useAuth();
  const hasRequiredRole = useHasRole(requiredRole || '');
  const hasRequiredPermissions = requiredPermissions.every(permission =>
    useHasPermission(permission)
  );

  // Show loading while initializing or loading
  if (!isInitialized || isLoading) {
    return <LoadingSpinner />;
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    redirect('/auth/login');
    return null;
  }

  // Check role-based access
  if (requiredRole && !hasRequiredRole) {
    return (
      fallback || (
        <UnauthorizedAccess
          message={`This page requires ${requiredRole} role access.`}
        />
      )
    );
  }

  // Check permission-based access
  if (requiredPermissions.length > 0 && !hasRequiredPermissions) {
    return (
      fallback || (
        <UnauthorizedAccess message="You don't have the required permissions to access this page." />
      )
    );
  }

  // User is authenticated and authorized
  return <>{children}</>;
};

// Higher-order component for route protection
export const withAuth = <P extends object>(
  Component: React.ComponentType<P>,
  options?: {
    requiredRole?: UserRole;
    requiredPermissions?: string[];
    fallback?: React.ReactNode;
  }
) => {
  const AuthenticatedComponent: React.FC<P> = props => (
    <ProtectedRoute
      requiredRole={options?.requiredRole}
      requiredPermissions={options?.requiredPermissions}
      fallback={options?.fallback}
    >
      <Component {...props} />
    </ProtectedRoute>
  );

  AuthenticatedComponent.displayName = `withAuth(${Component.displayName || Component.name})`;

  return AuthenticatedComponent;
};

// Role-specific route guards
export const AdminRoute: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <ProtectedRoute requiredRole={UserRole.SYSTEM_ADMIN}>
    {children}
  </ProtectedRoute>
);

export const StaffRoute: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <ProtectedRoute requiredRole={UserRole.LGU_STAFF}>{children}</ProtectedRoute>
);

export const DSWDStaffRoute: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <ProtectedRoute requiredRole={UserRole.DSWD_STAFF}>{children}</ProtectedRoute>
);

export const CitizenRoute: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <ProtectedRoute requiredRole={UserRole.CITIZEN}>{children}</ProtectedRoute>
);

// Multi-role route guard
export const MultiRoleRoute: React.FC<{
  children: React.ReactNode;
  allowedRoles: UserRole[];
}> = ({ children, allowedRoles }) => {
  const { user } = useAuth();
  const hasAccess = user && allowedRoles.includes(user.role as UserRole);

  if (!hasAccess) {
    return (
      <UnauthorizedAccess
        message={`This page is restricted to: ${allowedRoles.join(', ')}`}
      />
    );
  }

  return <>{children}</>;
};

// Guest route (only for non-authenticated users)
export const GuestRoute: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const { isAuthenticated, isLoading, isInitialized } = useAuth();

  // Show loading while initializing
  if (!isInitialized || isLoading) {
    return <LoadingSpinner />;
  }

  // Redirect to dashboard if already authenticated
  if (isAuthenticated) {
    redirect('/dashboard');
    return null;
  }

  return <>{children}</>;
};

// Route guard hook
export const useRouteGuard = (options: {
  requiredRole?: UserRole;
  requiredPermissions?: string[];
  redirectTo?: string;
}) => {
  const { isAuthenticated, user } = useAuth();
  const hasRequiredRole = useHasRole(options.requiredRole || '');
  const hasRequiredPermissions =
    options.requiredPermissions?.every(permission =>
      useHasPermission(permission)
    ) ?? true;

  const canAccess =
    isAuthenticated &&
    (!options.requiredRole || hasRequiredRole) &&
    hasRequiredPermissions;

  const checkAccess = () => {
    if (!isAuthenticated) {
      redirect(options.redirectTo || '/auth/login');
      return false;
    }

    if (options.requiredRole && !hasRequiredRole) {
      redirect('/unauthorized');
      return false;
    }

    if (options.requiredPermissions && !hasRequiredPermissions) {
      redirect('/unauthorized');
      return false;
    }

    return true;
  };

  return {
    canAccess,
    checkAccess,
    isAuthenticated,
    user,
    hasRequiredRole,
    hasRequiredPermissions,
  };
};

export default ProtectedRoute;
