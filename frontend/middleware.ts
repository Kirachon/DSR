// Next.js Middleware for Authentication and Security
// Handles route protection, authentication checks, and security headers

import { NextRequest } from 'next/server';

import { middlewareGuard } from '@/lib/auth-guards';

// Middleware function
export function middleware(request: NextRequest) {
  return middlewareGuard(request);
}

// Configure which routes the middleware should run on
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder files
     */
    '/((?!_next/static|_next/image|favicon.ico|public/).*)',
  ],
};
