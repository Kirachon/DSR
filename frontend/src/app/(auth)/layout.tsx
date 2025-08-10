// Auth Layout
// Layout for authentication pages with guest-only access

import { Metadata } from 'next';
import React from 'react';

import { GuestRoute } from '@/components/auth/protected-route';

export const metadata: Metadata = {
  title: 'Authentication - Digital Social Registry',
  description:
    'Sign in or create an account to access the Digital Social Registry platform',
};

interface AuthLayoutProps {
  children: React.ReactNode;
}

export default function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <GuestRoute>
      <div className='min-h-screen bg-gray-50'>{children}</div>
    </GuestRoute>
  );
}
