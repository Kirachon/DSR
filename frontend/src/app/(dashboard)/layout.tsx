// Dashboard Layout
// Protected layout for authenticated users with sidebar navigation

import { Metadata } from 'next';
import React from 'react';

import { ProtectedRoute } from '@/components/auth/protected-route';
import { DashboardLayout } from '@/components/layout';

export const metadata: Metadata = {
  title: 'Dashboard - Digital Social Registry',
  description:
    'Access your Digital Social Registry dashboard and manage your account',
};

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export default function Layout({ children }: DashboardLayoutProps) {
  return (
    <ProtectedRoute>
      <DashboardLayout>{children}</DashboardLayout>
    </ProtectedRoute>
  );
}
