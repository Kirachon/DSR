'use client';

// Sidebar Component
// Collapsible sidebar navigation with role-based menu items

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import React, { useState } from 'react';

import { useAuth } from '@/contexts';
import { UserRole } from '@/types';
import { cn } from '@/utils';

// Sidebar props interface
export interface SidebarProps {
  className?: string;
  collapsed?: boolean;
  onToggle?: (collapsed: boolean) => void;
  fixed?: boolean;
}

// Navigation item interface
interface NavigationItem {
  label: string;
  href: string;
  icon: React.ReactNode;
  badge?: string | number;
  children?: NavigationItem[];
  roles?: UserRole[];
  permissions?: string[];
}

// Navigation icons
const DashboardIcon = () => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2-2z'
    />
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M8 5a2 2 0 012-2h4a2 2 0 012 2v6H8V5z'
    />
  </svg>
);

const UsersIcon = () => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z'
    />
  </svg>
);

const SettingsIcon = () => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z'
    />
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M15 12a3 3 0 11-6 0 3 3 0 016 0z'
    />
  </svg>
);

const ReportsIcon = () => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'
    />
  </svg>
);

const ProfileIcon = () => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z'
    />
  </svg>
);

const HouseholdIcon = () => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6'
    />
  </svg>
);

const JourneyIcon = () => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7'
    />
  </svg>
);

// Navigation items configuration
const navigationItems: NavigationItem[] = [
  {
    label: 'Dashboard',
    href: '/dashboard',
    icon: <DashboardIcon />,
  },
  {
    label: 'My Journey',
    href: '/journey',
    icon: <JourneyIcon />,
    roles: [UserRole.CITIZEN],
  },
  {
    label: 'Citizens',
    href: '/citizens',
    icon: <UsersIcon />,
    roles: [UserRole.LGU_STAFF, UserRole.DSWD_STAFF, UserRole.SYSTEM_ADMIN],
    children: [
      {
        label: 'All Citizens',
        href: '/citizens',
        icon: <UsersIcon />,
      },
      {
        label: 'Registrations',
        href: '/citizens/registrations',
        icon: <UsersIcon />,
      },
      {
        label: 'Verification',
        href: '/citizens/verification',
        icon: <UsersIcon />,
        roles: [UserRole.LGU_STAFF, UserRole.DSWD_STAFF],
      },
    ],
  },
  {
    label: 'Households',
    href: '/households',
    icon: <HouseholdIcon />,
    roles: [UserRole.LGU_STAFF, UserRole.DSWD_STAFF, UserRole.SYSTEM_ADMIN, UserRole.CASE_WORKER],
  },
  {
    label: 'Reports',
    href: '/reports',
    icon: <ReportsIcon />,
    roles: [UserRole.LGU_STAFF, UserRole.DSWD_STAFF, UserRole.SYSTEM_ADMIN],
  },
  {
    label: 'Administration',
    href: '/admin',
    icon: <SettingsIcon />,
    roles: [UserRole.SYSTEM_ADMIN],
    children: [
      {
        label: 'Users',
        href: '/admin/users',
        icon: <UsersIcon />,
      },
      {
        label: 'System Settings',
        href: '/admin/settings',
        icon: <SettingsIcon />,
      },
    ],
  },
  {
    label: 'Profile',
    href: '/profile',
    icon: <ProfileIcon />,
  },
  {
    label: 'Settings',
    href: '/settings',
    icon: <SettingsIcon />,
  },
];

// Chevron icon for expandable items
const ChevronIcon: React.FC<{ expanded: boolean }> = ({ expanded }) => (
  <svg
    className={cn(
      'h-4 w-4 transition-transform duration-200',
      expanded && 'rotate-90'
    )}
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M9 5l7 7-7 7'
    />
  </svg>
);

// Navigation item component
const NavigationItem: React.FC<{
  item: NavigationItem;
  collapsed: boolean;
  level?: number;
}> = ({ item, collapsed, level = 0 }) => {
  const [expanded, setExpanded] = useState(false);
  const pathname = usePathname();
  const { user } = useAuth();

  // Check if user has access to this item
  const hasAccess = () => {
    if (item.roles && user && !item.roles.includes(user.role as UserRole)) {
      return false;
    }
    // Add permission checks here if needed
    return true;
  };

  if (!hasAccess()) {
    return null;
  }

  const isActive =
    pathname === item.href || pathname.startsWith(item.href + '/');
  const hasChildren = item.children && item.children.length > 0;

  const handleClick = () => {
    if (hasChildren && !collapsed) {
      setExpanded(!expanded);
    }
  };

  return (
    <div>
      <div
        className={cn(
          'flex items-center justify-between px-3 py-2 text-sm font-medium rounded-md transition-colors cursor-pointer',
          level > 0 && 'ml-4',
          isActive
            ? 'bg-primary-100 text-primary-700 border-r-2 border-primary-500'
            : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
        )}
        onClick={handleClick}
      >
        <Link
          href={item.href}
          className='flex items-center flex-1 min-w-0'
          onClick={e => hasChildren && !collapsed && e.preventDefault()}
        >
          <span className='flex-shrink-0'>{item.icon}</span>
          {!collapsed && (
            <>
              <span className='ml-3 truncate'>{item.label}</span>
              {item.badge && (
                <span className='ml-auto inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-primary-100 text-primary-800'>
                  {item.badge}
                </span>
              )}
            </>
          )}
        </Link>

        {hasChildren && !collapsed && <ChevronIcon expanded={expanded} />}
      </div>

      {/* Children */}
      {hasChildren && !collapsed && expanded && (
        <div className='mt-1 space-y-1'>
          {item.children!.map(child => (
            <NavigationItem
              key={child.href}
              item={child}
              collapsed={collapsed}
              level={level + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
};

// Sidebar component
export const Sidebar: React.FC<SidebarProps> = ({
  className,
  collapsed: controlledCollapsed,
  onToggle,
  fixed = true,
}) => {
  const [internalCollapsed, setInternalCollapsed] = useState(false);
  const { user } = useAuth();

  const collapsed = controlledCollapsed ?? internalCollapsed;

  const handleToggle = () => {
    const newCollapsed = !collapsed;
    if (onToggle) {
      onToggle(newCollapsed);
    } else {
      setInternalCollapsed(newCollapsed);
    }
  };

  const filteredItems = navigationItems.filter(item => {
    if (item.roles && user && !item.roles.includes(user.role as UserRole)) {
      return false;
    }
    return true;
  });

  return (
    <div
      className={cn(
        'bg-white border-r border-gray-200 transition-all duration-300 ease-in-out',
        collapsed ? 'w-16' : 'w-64',
        fixed && 'fixed left-0 top-16 bottom-0 z-30',
        className
      )}
    >
      {/* Toggle button */}
      <div className='flex items-center justify-between p-4 border-b border-gray-200'>
        {!collapsed && (
          <h2 className='text-lg font-semibold text-gray-900'>Navigation</h2>
        )}
        <button
          onClick={handleToggle}
          className='p-1 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500'
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          <svg
            className={cn(
              'h-5 w-5 transition-transform duration-200',
              collapsed && 'rotate-180'
            )}
            fill='none'
            stroke='currentColor'
            viewBox='0 0 24 24'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth={2}
              d='M11 19l-7-7 7-7m8 14l-7-7 7-7'
            />
          </svg>
        </button>
      </div>

      {/* Navigation */}
      <nav className='flex-1 px-2 py-4 space-y-1 overflow-y-auto'>
        {filteredItems.map(item => (
          <NavigationItem key={item.href} item={item} collapsed={collapsed} />
        ))}
      </nav>

      {/* User info (when expanded) */}
      {!collapsed && user && (
        <div className='border-t border-gray-200 p-4'>
          <div className='flex items-center'>
            <div className='flex-shrink-0'>
              <div className='h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center'>
                <span className='text-white text-sm font-medium'>
                  {user.firstName?.[0]}
                  {user.lastName?.[0]}
                </span>
              </div>
            </div>
            <div className='ml-3 min-w-0 flex-1'>
              <p className='text-sm font-medium text-gray-900 truncate'>
                {user.firstName} {user.lastName}
              </p>
              <p className='text-xs text-gray-500 truncate'>
                {user.role.replace('_', ' ').toLowerCase()}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Sidebar;
