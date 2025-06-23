'use client';

// Main Dashboard Page
// Role-based dashboard that renders appropriate content based on user role

import React from 'react';

import { useAuth } from '@/contexts';
import { PageLoading } from '@/components/ui';
import { CitizenDashboard } from '@/components/dashboard/citizen-dashboard';
import { LGUStaffDashboard } from '@/components/dashboard/lgu-staff-dashboard';
import { DSWDStaffDashboard } from '@/components/dashboard/dswd-staff-dashboard';
import { AdminDashboard } from '@/components/dashboard/admin-dashboard';
import type { UserRole } from '@/types';

// Main Dashboard component
export default function DashboardPage() {
  const { user, isLoading, isAuthenticated } = useAuth();

  if (isLoading || !isAuthenticated) {
    return <PageLoading text="Loading dashboard..." />;
  }

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            Unable to load dashboard
          </h2>
          <p className="text-gray-600">
            Please try refreshing the page or contact support if the problem persists.
          </p>
        </div>
      </div>
    );
  }

  // Render role-specific dashboard
  const renderDashboard = () => {
    switch (user.role as UserRole) {
      case 'CITIZEN':
        return <CitizenDashboard user={user} />;
      case 'LGU_STAFF':
        return <LGUStaffDashboard user={user} />;
      case 'DSWD_STAFF':
        return <DSWDStaffDashboard user={user} />;
      case 'SYSTEM_ADMIN':
        return <AdminDashboard user={user} />;
      default:
        return (
          <div className="flex items-center justify-center min-h-96">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">
                Unknown Role
              </h2>
              <p className="text-gray-600">
                Your account role ({user.role}) is not recognized. Please contact support.
              </p>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Welcome back, {user.firstName}!
            </h1>
            <p className="text-gray-600">
              {new Date().toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
          </div>
          <div className="text-right">
            <p className="text-sm text-gray-500">Role</p>
            <p className="text-lg font-medium text-gray-900">
              {user.role.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
            </p>
          </div>
        </div>
      </div>

      {/* Role-specific Dashboard Content */}
      {renderDashboard()}
    </div>
  );
}
