'use client';

// Layout Component
// Main application layout with header, sidebar, footer, and content area

import React, { useState, useEffect } from 'react';

import { cn } from '@/utils';
import { useAuth } from '@/contexts';
import { Header } from './header';
import { Sidebar } from './sidebar';
import { Footer } from './footer';

// Layout props interface
export interface LayoutProps {
  children: React.ReactNode;
  className?: string;
  showHeader?: boolean;
  showSidebar?: boolean;
  showFooter?: boolean;
  sidebarCollapsed?: boolean;
  headerFixed?: boolean;
  sidebarFixed?: boolean;
  footerVariant?: 'default' | 'minimal' | 'detailed';
  contentPadding?: boolean;
  fullHeight?: boolean;
}

// Layout component
export const Layout: React.FC<LayoutProps> = ({
  children,
  className,
  showHeader = true,
  showSidebar = true,
  showFooter = true,
  sidebarCollapsed: controlledSidebarCollapsed,
  headerFixed = true,
  sidebarFixed = true,
  footerVariant = 'default',
  contentPadding = true,
  fullHeight = true,
}) => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const { isAuthenticated } = useAuth();

  // Handle responsive behavior
  useEffect(() => {
    const checkMobile = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      if (mobile && showSidebar) {
        setSidebarCollapsed(true);
      }
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, [showSidebar]);

  // Use controlled or internal sidebar state
  const actualSidebarCollapsed = controlledSidebarCollapsed ?? sidebarCollapsed;

  // Calculate content margins based on layout configuration
  const getContentMargins = () => {
    let marginTop = 0;
    let marginLeft = 0;
    let marginBottom = 0;

    if (showHeader && headerFixed) {
      marginTop = 64; // Header height (h-16 = 64px)
    }

    if (showSidebar && sidebarFixed && isAuthenticated && !isMobile) {
      marginLeft = actualSidebarCollapsed ? 64 : 256; // Sidebar width
    }

    return {
      marginTop: `${marginTop}px`,
      marginLeft: `${marginLeft}px`,
      marginBottom: showFooter ? '0' : '0',
    };
  };

  const contentMargins = getContentMargins();

  return (
    <div className={cn('min-h-screen bg-gray-50', fullHeight && 'h-screen', className)}>
      {/* Header */}
      {showHeader && (
        <Header
          fixed={headerFixed}
          showNavigation={!showSidebar || !isAuthenticated}
          showUserMenu={isAuthenticated}
        />
      )}

      {/* Sidebar */}
      {showSidebar && isAuthenticated && (
        <Sidebar
          collapsed={actualSidebarCollapsed}
          onToggle={setSidebarCollapsed}
          fixed={sidebarFixed}
        />
      )}

      {/* Main content area */}
      <main
        className={cn(
          'transition-all duration-300 ease-in-out',
          fullHeight && 'min-h-screen',
          contentPadding && 'p-4 sm:p-6 lg:p-8'
        )}
        style={sidebarFixed || headerFixed ? contentMargins : undefined}
      >
        <div className={cn(
          'mx-auto',
          contentPadding && 'max-w-7xl'
        )}>
          {children}
        </div>
      </main>

      {/* Footer */}
      {showFooter && (
        <Footer
          variant={footerVariant}
          className={cn(
            sidebarFixed && showSidebar && isAuthenticated && !isMobile && 'transition-all duration-300 ease-in-out'
          )}
          style={
            sidebarFixed && showSidebar && isAuthenticated && !isMobile
              ? { marginLeft: contentMargins.marginLeft }
              : undefined
          }
        />
      )}

      {/* Mobile sidebar overlay */}
      {showSidebar && isAuthenticated && isMobile && !actualSidebarCollapsed && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 md:hidden"
          onClick={() => setSidebarCollapsed(true)}
        />
      )}
    </div>
  );
};

// Specialized layout components for different page types

// Dashboard Layout
export const DashboardLayout: React.FC<{
  children: React.ReactNode;
  className?: string;
}> = ({ children, className }) => (
  <Layout
    className={className}
    showHeader={true}
    showSidebar={true}
    showFooter={false}
    headerFixed={true}
    sidebarFixed={true}
    contentPadding={true}
    fullHeight={true}
  >
    {children}
  </Layout>
);

// Auth Layout (for login, register pages)
export const AuthLayout: React.FC<{
  children: React.ReactNode;
  className?: string;
}> = ({ children, className }) => (
  <Layout
    className={className}
    showHeader={false}
    showSidebar={false}
    showFooter={true}
    footerVariant="minimal"
    contentPadding={false}
    fullHeight={true}
  >
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {children}
      </div>
    </div>
  </Layout>
);

// Public Layout (for marketing pages)
export const PublicLayout: React.FC<{
  children: React.ReactNode;
  className?: string;
}> = ({ children, className }) => (
  <Layout
    className={className}
    showHeader={true}
    showSidebar={false}
    showFooter={true}
    footerVariant="detailed"
    headerFixed={true}
    contentPadding={true}
    fullHeight={false}
  >
    {children}
  </Layout>
);

// Error Layout (for error pages)
export const ErrorLayout: React.FC<{
  children: React.ReactNode;
  className?: string;
}> = ({ children, className }) => (
  <Layout
    className={className}
    showHeader={true}
    showSidebar={false}
    showFooter={true}
    footerVariant="minimal"
    headerFixed={false}
    contentPadding={false}
    fullHeight={true}
  >
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-lg w-full text-center">
        {children}
      </div>
    </div>
  </Layout>
);

export default Layout;
