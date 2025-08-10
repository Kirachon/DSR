'use client';

// 404 Not Found Page
// Enhanced error page with navigation suggestions and user-friendly design

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useEffect, useState } from 'react';

import { Button, Card } from '@/components/ui';
import { useAuth } from '@/contexts';

// Suggested routes based on user role
const getSuggestedRoutes = (userRole?: string) => {
  const commonRoutes = [
    { href: '/dashboard', label: 'Dashboard', description: 'Return to your main dashboard' },
    { href: '/profile', label: 'Profile', description: 'Manage your profile information' },
    { href: '/settings', label: 'Settings', description: 'Adjust your preferences' },
  ];

  const citizenRoutes = [
    { href: '/journey', label: 'My Journey', description: 'Track your application progress' },
    { href: '/applications', label: 'Applications', description: 'View your applications' },
  ];

  const staffRoutes = [
    { href: '/citizens', label: 'Citizens', description: 'Manage citizen records' },
    { href: '/households', label: 'Households', description: 'Household management' },
    { href: '/reports', label: 'Reports', description: 'Generate reports' },
  ];

  const adminRoutes = [
    { href: '/admin', label: 'Administration', description: 'System administration' },
    { href: '/admin/users', label: 'User Management', description: 'Manage system users' },
  ];

  let routes = [...commonRoutes];

  if (userRole === 'CITIZEN') {
    routes = [...routes, ...citizenRoutes];
  } else if (userRole === 'LGU_STAFF' || userRole === 'DSWD_STAFF') {
    routes = [...routes, ...staffRoutes];
  } else if (userRole === 'SYSTEM_ADMIN') {
    routes = [...routes, ...staffRoutes, ...adminRoutes];
  }

  return routes;
};

// 404 Error Component
export default function NotFound() {
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const [countdown, setCountdown] = useState(10);

  // Auto-redirect countdown
  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          router.push(isAuthenticated ? '/dashboard' : '/');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [router, isAuthenticated]);

  const suggestedRoutes = getSuggestedRoutes(user?.role);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-2xl">
        {/* Error Icon */}
        <div className="flex justify-center mb-8">
          <div className="w-24 h-24 bg-red-100 rounded-full flex items-center justify-center">
            <svg
              className="w-12 h-12 text-red-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
              />
            </svg>
          </div>
        </div>

        {/* Error Message */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">Page Not Found</h1>
          <p className="text-lg text-gray-600 mb-2">
            Sorry, we couldn't find the page you're looking for.
          </p>
          <p className="text-sm text-gray-500">
            You'll be redirected to your dashboard in {countdown} seconds.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center mb-8">
          <Button
            variant="primary"
            size="lg"
            onClick={() => router.push(isAuthenticated ? '/dashboard' : '/')}
          >
            {isAuthenticated ? 'Go to Dashboard' : 'Go to Home'}
          </Button>
          <Button
            variant="secondary"
            size="lg"
            onClick={() => router.back()}
          >
            Go Back
          </Button>
        </div>

        {/* Suggested Routes */}
        {isAuthenticated && suggestedRoutes.length > 0 && (
          <Card className="p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Suggested Pages
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {suggestedRoutes.map(route => (
                <Link
                  key={route.href}
                  href={route.href}
                  className="block p-4 rounded-lg border border-gray-200 hover:border-primary-300 hover:bg-primary-50 transition-colors"
                >
                  <h3 className="font-medium text-gray-900 mb-1">
                    {route.label}
                  </h3>
                  <p className="text-sm text-gray-600">
                    {route.description}
                  </p>
                </Link>
              ))}
            </div>
          </Card>
        )}

        {/* Help Section */}
        <div className="text-center mt-8">
          <p className="text-sm text-gray-500 mb-4">
            Need help? Contact our support team.
          </p>
          <div className="flex justify-center space-x-4">
            <Link
              href="/support"
              className="text-primary-600 hover:text-primary-500 text-sm font-medium"
            >
              Support Center
            </Link>
            <span className="text-gray-300">|</span>
            <Link
              href="/contact"
              className="text-primary-600 hover:text-primary-500 text-sm font-medium"
            >
              Contact Us
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
