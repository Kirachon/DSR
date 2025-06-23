// Auth Layout
// Layout for authentication pages with guest-only access

import React from 'react';
import { Metadata } from 'next';

import { AuthProvider } from '@/contexts';
import { GuestRoute } from '@/components/auth/protected-route';

export const metadata: Metadata = {
  title: 'Authentication - Dynamic Social Registry',
  description: 'Sign in or create an account to access the Dynamic Social Registry platform',
};

interface AuthLayoutProps {
  children: React.ReactNode;
}

export default function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <AuthProvider>
      <GuestRoute>
        <div className="min-h-screen bg-gray-50">
          {children}
        </div>
      </GuestRoute>
    </AuthProvider>
  );
}
