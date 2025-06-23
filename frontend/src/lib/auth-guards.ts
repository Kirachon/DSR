// Authentication Guards for Next.js Middleware
// Server-side route protection and authentication checks

import { NextRequest, NextResponse } from 'next/server';

import { TokenUtils } from '@/utils';
import type { UserRole, TokenPayload } from '@/types';

// Route configuration
export interface RouteConfig {
  path: string;
  requiredRole?: UserRole;
  requiredPermissions?: string[];
  isPublic?: boolean;
  isGuestOnly?: boolean;
}

// Default route configurations
export const routeConfigs: RouteConfig[] = [
  // Public routes (no authentication required)
  { path: '/', isPublic: true },
  { path: '/about', isPublic: true },
  { path: '/contact', isPublic: true },
  { path: '/privacy', isPublic: true },
  { path: '/terms', isPublic: true },

  // Guest-only routes (redirect if authenticated)
  { path: '/auth/login', isGuestOnly: true },
  { path: '/auth/register', isGuestOnly: true },
  { path: '/auth/forgot-password', isGuestOnly: true },
  { path: '/auth/reset-password', isGuestOnly: true },

  // Protected routes
  { path: '/dashboard' },
  { path: '/profile' },
  { path: '/settings' },

  // Role-specific routes
  { path: '/admin', requiredRole: 'SYSTEM_ADMIN' },
  { path: '/staff', requiredRole: 'LGU_STAFF' },
  { path: '/dswd', requiredRole: 'DSWD_STAFF' },
  { path: '/citizen', requiredRole: 'CITIZEN' },

  // API routes (require authentication)
  { path: '/api/protected' },
];

// Extract token from request
export const extractTokenFromRequest = (request: NextRequest): string | null => {
  // Try Authorization header first
  const authHeader = request.headers.get('authorization');
  if (authHeader && authHeader.startsWith('Bearer ')) {
    return authHeader.substring(7);
  }

  // Try cookies
  const tokenFromCookie = request.cookies.get('dsr_access_token')?.value;
  if (tokenFromCookie) {
    return tokenFromCookie;
  }

  return null;
};

// Validate token and extract payload
export const validateRequestToken = (request: NextRequest): {
  isValid: boolean;
  payload: TokenPayload | null;
  error?: string;
} => {
  const token = extractTokenFromRequest(request);
  
  if (!token) {
    return {
      isValid: false,
      payload: null,
      error: 'No token provided',
    };
  }

  return TokenUtils.validateToken(token);
};

// Check if route matches pattern
export const matchesRoute = (pathname: string, routePath: string): boolean => {
  // Exact match
  if (pathname === routePath) {
    return true;
  }

  // Wildcard match (e.g., /admin/* matches /admin/users)
  if (routePath.endsWith('/*')) {
    const basePath = routePath.slice(0, -2);
    return pathname.startsWith(basePath);
  }

  // Dynamic route match (e.g., /user/[id] matches /user/123)
  const routeSegments = routePath.split('/');
  const pathSegments = pathname.split('/');

  if (routeSegments.length !== pathSegments.length) {
    return false;
  }

  return routeSegments.every((segment, index) => {
    if (segment.startsWith('[') && segment.endsWith(']')) {
      return true; // Dynamic segment matches anything
    }
    return segment === pathSegments[index];
  });
};

// Find route configuration for pathname
export const findRouteConfig = (pathname: string): RouteConfig | null => {
  return routeConfigs.find(config => matchesRoute(pathname, config.path)) || null;
};

// Check if user has required role
export const hasRequiredRole = (userRole: string, requiredRole?: UserRole): boolean => {
  if (!requiredRole) return true;
  return userRole === requiredRole;
};

// Check if user has required permissions
export const hasRequiredPermissions = (
  userPermissions: string[],
  requiredPermissions?: string[]
): boolean => {
  if (!requiredPermissions || requiredPermissions.length === 0) return true;
  return requiredPermissions.every(permission => userPermissions.includes(permission));
};

// Main authentication guard
export const authGuard = (request: NextRequest): NextResponse | null => {
  const { pathname } = request.nextUrl;
  const routeConfig = findRouteConfig(pathname);

  // If no specific config found, treat as protected route
  const isPublic = routeConfig?.isPublic ?? false;
  const isGuestOnly = routeConfig?.isGuestOnly ?? false;
  const requiredRole = routeConfig?.requiredRole;
  const requiredPermissions = routeConfig?.requiredPermissions;

  // Allow public routes
  if (isPublic) {
    return null; // Continue to the route
  }

  // Validate token
  const { isValid, payload, error } = validateRequestToken(request);

  // Handle guest-only routes
  if (isGuestOnly) {
    if (isValid && payload) {
      // Redirect authenticated users away from guest-only routes
      return NextResponse.redirect(new URL('/dashboard', request.url));
    }
    return null; // Allow unauthenticated users to access guest-only routes
  }

  // Handle protected routes
  if (!isValid || !payload) {
    // Redirect to login for protected routes
    const loginUrl = new URL('/auth/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // Check role-based access
  if (requiredRole && !hasRequiredRole(payload.role, requiredRole)) {
    return NextResponse.redirect(new URL('/unauthorized', request.url));
  }

  // Check permission-based access
  // Note: Permissions would need to be included in the JWT payload
  // or fetched from an additional API call
  if (requiredPermissions && requiredPermissions.length > 0) {
    // For now, we'll skip permission checks in middleware
    // and handle them in the component level
    console.warn('Permission checks in middleware not implemented');
  }

  // User is authenticated and authorized
  return null; // Continue to the route
};

// Rate limiting guard
export const rateLimitGuard = (request: NextRequest): NextResponse | null => {
  // Implement rate limiting logic here
  // This is a placeholder for future implementation
  return null;
};

// CSRF protection guard
export const csrfGuard = (request: NextRequest): NextResponse | null => {
  // Skip CSRF checks for GET requests
  if (request.method === 'GET') {
    return null;
  }

  // Check for CSRF token in headers
  const csrfToken = request.headers.get('x-csrf-token');
  const csrfCookie = request.cookies.get('csrf-token')?.value;

  if (!csrfToken || !csrfCookie || csrfToken !== csrfCookie) {
    // For API routes, return 403
    if (request.nextUrl.pathname.startsWith('/api/')) {
      return new NextResponse('CSRF token mismatch', { status: 403 });
    }
  }

  return null;
};

// Security headers guard
export const securityHeadersGuard = (response: NextResponse): NextResponse => {
  // Add security headers
  response.headers.set('X-Content-Type-Options', 'nosniff');
  response.headers.set('X-Frame-Options', 'DENY');
  response.headers.set('X-XSS-Protection', '1; mode=block');
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');
  response.headers.set(
    'Content-Security-Policy',
    "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' http://localhost:8080;"
  );

  return response;
};

// Combined middleware guard
export const middlewareGuard = (request: NextRequest): NextResponse => {
  // Apply authentication guard
  const authResponse = authGuard(request);
  if (authResponse) {
    return securityHeadersGuard(authResponse);
  }

  // Apply rate limiting guard
  const rateLimitResponse = rateLimitGuard(request);
  if (rateLimitResponse) {
    return securityHeadersGuard(rateLimitResponse);
  }

  // Apply CSRF guard
  const csrfResponse = csrfGuard(request);
  if (csrfResponse) {
    return securityHeadersGuard(csrfResponse);
  }

  // Continue with security headers
  const response = NextResponse.next();
  return securityHeadersGuard(response);
};

export default middlewareGuard;
