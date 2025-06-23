'use client';

// Header Component
// Main application header with navigation, user menu, and responsive design

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

import { cn } from '@/utils';
import { Button } from '@/components/ui';
import { useAuth, useAuthActions } from '@/contexts';

// Header props interface
export interface HeaderProps {
  className?: string;
  showNavigation?: boolean;
  showUserMenu?: boolean;
  fixed?: boolean;
}

// Navigation item interface
interface NavigationItem {
  label: string;
  href: string;
  icon?: React.ReactNode;
  requiresAuth?: boolean;
  roles?: string[];
}

// Default navigation items
const navigationItems: NavigationItem[] = [
  {
    label: 'Dashboard',
    href: '/dashboard',
    requiresAuth: true,
  },
  {
    label: 'Profile',
    href: '/profile',
    requiresAuth: true,
  },
  {
    label: 'Settings',
    href: '/settings',
    requiresAuth: true,
  },
];

// Menu icon component
const MenuIcon: React.FC<{ isOpen: boolean }> = ({ isOpen }) => (
  <svg
    className="h-6 w-6"
    fill="none"
    stroke="currentColor"
    viewBox="0 0 24 24"
  >
    {isOpen ? (
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M6 18L18 6M6 6l12 12"
      />
    ) : (
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M4 6h16M4 12h16M4 18h16"
      />
    )}
  </svg>
);

// User avatar component
const UserAvatar: React.FC<{ user: any; size?: 'sm' | 'md' | 'lg' }> = ({ 
  user, 
  size = 'md' 
}) => {
  const sizeClasses = {
    sm: 'h-6 w-6',
    md: 'h-8 w-8',
    lg: 'h-10 w-10',
  };

  const initials = user?.firstName && user?.lastName 
    ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
    : user?.email?.[0]?.toUpperCase() || 'U';

  if (user?.profilePictureUrl) {
    return (
      <img
        src={user.profilePictureUrl}
        alt={`${user.firstName} ${user.lastName}`}
        className={cn(
          'rounded-full object-cover',
          sizeClasses[size]
        )}
      />
    );
  }

  return (
    <div
      className={cn(
        'flex items-center justify-center rounded-full bg-primary-600 text-white font-medium',
        sizeClasses[size],
        size === 'sm' && 'text-xs',
        size === 'md' && 'text-sm',
        size === 'lg' && 'text-base'
      )}
    >
      {initials}
    </div>
  );
};

// Header component
export const Header: React.FC<HeaderProps> = ({
  className,
  showNavigation = true,
  showUserMenu = true,
  fixed = true,
}) => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  
  const { isAuthenticated, user } = useAuth();
  const { logout } = useAuthActions();
  const router = useRouter();

  const handleLogout = async () => {
    try {
      await logout();
      router.push('/auth/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  const filteredNavItems = navigationItems.filter(item => {
    if (item.requiresAuth && !isAuthenticated) return false;
    if (item.roles && user && !item.roles.includes(user.role)) return false;
    return true;
  });

  return (
    <header
      className={cn(
        'bg-white border-b border-gray-200 shadow-sm',
        fixed && 'fixed top-0 left-0 right-0 z-40',
        className
      )}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo and brand */}
          <div className="flex items-center">
            <Link href="/" className="flex items-center space-x-2">
              <div className="h-8 w-8 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">DSR</span>
              </div>
              <span className="text-xl font-semibold text-gray-900 hidden sm:block">
                Dynamic Social Registry
              </span>
            </Link>
          </div>

          {/* Desktop navigation */}
          {showNavigation && (
            <nav className="hidden md:flex space-x-8">
              {filteredNavItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="text-gray-500 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  {item.label}
                </Link>
              ))}
            </nav>
          )}

          {/* Right side */}
          <div className="flex items-center space-x-4">
            {/* Authentication buttons */}
            {!isAuthenticated ? (
              <div className="hidden md:flex items-center space-x-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => router.push('/auth/login')}
                >
                  Sign In
                </Button>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => router.push('/auth/register')}
                >
                  Sign Up
                </Button>
              </div>
            ) : (
              showUserMenu && (
                <div className="relative">
                  <button
                    type="button"
                    className="flex items-center space-x-2 text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                    onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                    aria-expanded={isUserMenuOpen}
                    aria-haspopup="true"
                  >
                    <UserAvatar user={user} />
                    <span className="hidden md:block text-gray-700">
                      {user?.firstName} {user?.lastName}
                    </span>
                    <svg
                      className="h-4 w-4 text-gray-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 9l-7 7-7-7"
                      />
                    </svg>
                  </button>

                  {/* User dropdown menu */}
                  {isUserMenuOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200">
                      <Link
                        href="/profile"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setIsUserMenuOpen(false)}
                      >
                        Your Profile
                      </Link>
                      <Link
                        href="/settings"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setIsUserMenuOpen(false)}
                      >
                        Settings
                      </Link>
                      <hr className="my-1" />
                      <button
                        onClick={() => {
                          setIsUserMenuOpen(false);
                          handleLogout();
                        }}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        Sign out
                      </button>
                    </div>
                  )}
                </div>
              )
            )}

            {/* Mobile menu button */}
            <button
              type="button"
              className="md:hidden inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              aria-expanded={isMobileMenuOpen}
            >
              <span className="sr-only">Open main menu</span>
              <MenuIcon isOpen={isMobileMenuOpen} />
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        {isMobileMenuOpen && (
          <div className="md:hidden">
            <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t border-gray-200">
              {/* Navigation items */}
              {showNavigation && filteredNavItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="text-gray-500 hover:text-gray-900 block px-3 py-2 rounded-md text-base font-medium"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  {item.label}
                </Link>
              ))}

              {/* Authentication buttons for mobile */}
              {!isAuthenticated && (
                <div className="pt-4 pb-3 border-t border-gray-200">
                  <div className="space-y-2">
                    <Button
                      variant="ghost"
                      fullWidth
                      onClick={() => {
                        setIsMobileMenuOpen(false);
                        router.push('/auth/login');
                      }}
                    >
                      Sign In
                    </Button>
                    <Button
                      variant="primary"
                      fullWidth
                      onClick={() => {
                        setIsMobileMenuOpen(false);
                        router.push('/auth/register');
                      }}
                    >
                      Sign Up
                    </Button>
                  </div>
                </div>
              )}

              {/* User menu for mobile */}
              {isAuthenticated && showUserMenu && (
                <div className="pt-4 pb-3 border-t border-gray-200">
                  <div className="flex items-center px-3 mb-3">
                    <UserAvatar user={user} />
                    <div className="ml-3">
                      <div className="text-base font-medium text-gray-800">
                        {user?.firstName} {user?.lastName}
                      </div>
                      <div className="text-sm font-medium text-gray-500">
                        {user?.email}
                      </div>
                    </div>
                  </div>
                  <div className="space-y-1">
                    <Link
                      href="/profile"
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900 hover:bg-gray-100"
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      Your Profile
                    </Link>
                    <Link
                      href="/settings"
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900 hover:bg-gray-100"
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      Settings
                    </Link>
                    <button
                      onClick={() => {
                        setIsMobileMenuOpen(false);
                        handleLogout();
                      }}
                      className="block w-full text-left px-3 py-2 rounded-md text-base font-medium text-gray-500 hover:text-gray-900 hover:bg-gray-100"
                    >
                      Sign out
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Click outside to close menus */}
      {(isMobileMenuOpen || isUserMenuOpen) && (
        <div
          className="fixed inset-0 z-30"
          onClick={() => {
            setIsMobileMenuOpen(false);
            setIsUserMenuOpen(false);
          }}
        />
      )}
    </header>
  );
};

export default Header;
