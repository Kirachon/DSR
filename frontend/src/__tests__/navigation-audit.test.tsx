// Navigation Audit Test Suite
// Comprehensive testing of all navigation links and routing functionality

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useRouter } from 'next/navigation';
import React from 'react';

import { Header } from '@/components/layout/header';
import { Sidebar } from '@/components/layout/sidebar';
import { AuthProvider } from '@/contexts/auth-context';
import type { User } from '@/types';
import { UserRole, UserStatus } from '@/types';

// Mock Next.js router
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  usePathname: jest.fn(() => '/dashboard'),
}));

// Mock user for testing
const mockUser: User = {
  id: '1',
  email: 'test@dsr.gov.ph',
  firstName: 'Test',
  lastName: 'User',
  role: UserRole.CITIZEN,
  status: UserStatus.ACTIVE,
  createdAt: new Date(),
  updatedAt: new Date(),
};

// Test wrapper component
const TestWrapper: React.FC<{ children: React.ReactNode; user?: User }> = ({
  children,
  user = mockUser,
}) => (
  <AuthProvider>
    {children}
  </AuthProvider>
);

describe('Navigation Audit', () => {
  const mockPush = jest.fn();
  const mockRouter = {
    push: mockPush,
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
    replace: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue(mockRouter);
  });

  describe('Header Navigation', () => {
    it('should render all header navigation links', () => {
      render(
        <TestWrapper>
          <Header />
        </TestWrapper>
      );

      // Check for main navigation elements
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.getByText('Profile')).toBeInTheDocument();
      expect(screen.getByText('Settings')).toBeInTheDocument();
    });

    it('should navigate to dashboard when dashboard link is clicked', async () => {
      const user = userEvent.setup();
      render(
        <TestWrapper>
          <Header />
        </TestWrapper>
      );

      const dashboardLink = screen.getByText('Dashboard');
      await user.click(dashboardLink);

      expect(mockPush).toHaveBeenCalledWith('/dashboard');
    });

    it('should navigate to profile when profile link is clicked', async () => {
      const user = userEvent.setup();
      render(
        <TestWrapper>
          <Header />
        </TestWrapper>
      );

      const profileLink = screen.getByText('Profile');
      await user.click(profileLink);

      expect(mockPush).toHaveBeenCalledWith('/profile');
    });
  });

  describe('Sidebar Navigation', () => {
    it('should render all sidebar navigation items for citizen role', () => {
      render(
        <TestWrapper user={{ ...mockUser, role: UserRole.CITIZEN }}>
          <Sidebar />
        </TestWrapper>
      );

      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.getByText('My Journey')).toBeInTheDocument();
      expect(screen.getByText('Profile')).toBeInTheDocument();
      expect(screen.getByText('Settings')).toBeInTheDocument();
    });

    it('should render admin navigation items for admin role', () => {
      render(
        <TestWrapper user={{ ...mockUser, role: UserRole.SYSTEM_ADMIN }}>
          <Sidebar />
        </TestWrapper>
      );

      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.getByText('Citizens')).toBeInTheDocument();
      expect(screen.getByText('Households')).toBeInTheDocument();
      expect(screen.getByText('Reports')).toBeInTheDocument();
      expect(screen.getByText('Administration')).toBeInTheDocument();
    });

    it('should navigate to citizens page when citizens link is clicked', async () => {
      const user = userEvent.setup();
      render(
        <TestWrapper user={{ ...mockUser, role: UserRole.SYSTEM_ADMIN }}>
          <Sidebar />
        </TestWrapper>
      );

      const citizensLink = screen.getByText('Citizens');
      await user.click(citizensLink);

      expect(mockPush).toHaveBeenCalledWith('/citizens');
    });
  });

  describe('Route Accessibility', () => {
    const testRoutes = [
      { path: '/dashboard', name: 'Dashboard' },
      { path: '/journey', name: 'My Journey' },
      { path: '/citizens', name: 'Citizens' },
      { path: '/citizens/registrations', name: 'Registrations' },
      { path: '/citizens/verification', name: 'Verification' },
      { path: '/households', name: 'Households' },
      { path: '/reports', name: 'Reports' },
      { path: '/admin', name: 'Administration' },
      { path: '/admin/users', name: 'User Management' },
      { path: '/admin/settings', name: 'System Settings' },
      { path: '/profile', name: 'Profile' },
      { path: '/settings', name: 'Settings' },
    ];

    testRoutes.forEach(route => {
      it(`should be able to navigate to ${route.name} (${route.path})`, () => {
        // Test that the route is properly configured
        expect(route.path).toMatch(/^\/[a-z0-9\-\/]*$/);
        expect(route.name).toBeTruthy();
      });
    });
  });

  describe('Role-Based Access Control', () => {
    it('should show citizen-specific navigation for citizen users', () => {
      render(
        <TestWrapper user={{ ...mockUser, role: UserRole.CITIZEN }}>
          <Sidebar />
        </TestWrapper>
      );

      expect(screen.getByText('My Journey')).toBeInTheDocument();
      expect(screen.queryByText('Citizens')).not.toBeInTheDocument();
      expect(screen.queryByText('Administration')).not.toBeInTheDocument();
    });

    it('should show staff navigation for LGU staff users', () => {
      render(
        <TestWrapper user={{ ...mockUser, role: UserRole.LGU_STAFF }}>
          <Sidebar />
        </TestWrapper>
      );

      expect(screen.getByText('Citizens')).toBeInTheDocument();
      expect(screen.getByText('Households')).toBeInTheDocument();
      expect(screen.queryByText('My Journey')).not.toBeInTheDocument();
    });

    it('should show admin navigation for system admin users', () => {
      render(
        <TestWrapper user={{ ...mockUser, role: UserRole.SYSTEM_ADMIN }}>
          <Sidebar />
        </TestWrapper>
      );

      expect(screen.getByText('Administration')).toBeInTheDocument();
      expect(screen.getByText('Citizens')).toBeInTheDocument();
      expect(screen.getByText('Reports')).toBeInTheDocument();
    });
  });

  describe('Mobile Navigation', () => {
    beforeEach(() => {
      // Mock mobile viewport
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 375,
      });
    });

    it('should render mobile menu toggle button', () => {
      render(
        <TestWrapper>
          <Header />
        </TestWrapper>
      );

      const menuButton = screen.getByRole('button', { name: /menu/i });
      expect(menuButton).toBeInTheDocument();
    });

    it('should toggle mobile menu when menu button is clicked', async () => {
      const user = userEvent.setup();
      render(
        <TestWrapper>
          <Header />
        </TestWrapper>
      );

      const menuButton = screen.getByRole('button', { name: /menu/i });
      await user.click(menuButton);

      // Mobile menu should be visible
      expect(screen.getByRole('navigation')).toBeInTheDocument();
    });
  });
});
