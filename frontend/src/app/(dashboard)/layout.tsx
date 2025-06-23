// Dashboard Layout
// Protected layout for authenticated users with sidebar navigation

import React from 'react';
import { Metadata } from 'next';

import { AuthProvider } from '@/contexts';
import { ProtectedRoute } from '@/components/auth/protected-route';
import { DashboardLayout } from '@/components/layout';

export const metadata: Metadata = {
  title: 'Dashboard - Dynamic Social Registry',
  description: 'Access your Dynamic Social Registry dashboard and manage your account',
};

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export default function Layout({ children }: DashboardLayoutProps) {
  return (
    <AuthProvider>
      <ProtectedRoute>
        <DashboardLayout>
          {children}
        </DashboardLayout>
      </ProtectedRoute>
    </AuthProvider>
  );
}
