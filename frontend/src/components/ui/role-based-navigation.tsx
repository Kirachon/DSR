'use client';

// DSR Role-Based Navigation Component
// Dynamic navigation based on user permissions and roles

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import React, { forwardRef, useMemo } from 'react';

import { Badge } from './badge';
import { Button } from './button';
import { cn } from '@/utils';
import type { UserRole } from '@/types';

// Navigation item interface
export interface NavigationItem {
  id: string;
  label: string;
  href: string;
  icon?: React.ReactNode;
  badge?: {
    text: string;
    variant?: 'default' | 'success' | 'warning' | 'error';
  };
  description?: string;
  roles: UserRole[];
  permissions?: string[];
  children?: NavigationItem[];
  external?: boolean;
  disabled?: boolean;
}

// Navigation section interface
export interface NavigationSection {
  id: string;
  label: string;
  items: NavigationItem[];
  roles: UserRole[];
  collapsible?: boolean;
  defaultExpanded?: boolean;
}

// Role-based navigation props
export interface RoleBasedNavigationProps extends React.HTMLAttributes<HTMLElement> {
  sections: NavigationSection[];
  userRole: UserRole;
  userPermissions?: string[];
  variant?: 'sidebar' | 'horizontal' | 'mobile';
  showLabels?: boolean;
  showDescriptions?: boolean;
  showBadges?: boolean;
  collapsible?: boolean;
  onItemClick?: (item: NavigationItem) => void;
}

// Default navigation configuration for DSR roles
export const DSR_NAVIGATION_CONFIG: NavigationSection[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    roles: ['CITIZEN', 'LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN', 'CASE_WORKER'],
    items: [
      {
        id: 'overview',
        label: 'Overview',
        href: '/dashboard',
        icon: '🏠',
        roles: ['CITIZEN', 'LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN', 'CASE_WORKER'],
      },
    ],
  },
  {
    id: 'citizen-services',
    label: 'Citizen Services',
    roles: ['CITIZEN'],
    items: [
      {
        id: 'registration',
        label: 'Registration',
        href: '/registration',
        icon: '📝',
        description: 'Register for social protection programs',
        roles: ['CITIZEN'],
      },
      {
        id: 'profile',
        label: 'My Profile',
        href: '/profile',
        icon: '👤',
        description: 'Manage personal and household information',
        roles: ['CITIZEN'],
      },
      {
        id: 'applications',
        label: 'My Applications',
        href: '/applications',
        icon: '📋',
        description: 'Track application status and history',
        roles: ['CITIZEN'],
      },
      {
        id: 'benefits',
        label: 'Benefits',
        href: '/benefits',
        icon: '💰',
        description: 'View enrolled benefits and payments',
        roles: ['CITIZEN'],
      },
    ],
  },
  {
    id: 'staff-operations',
    label: 'Operations',
    roles: ['LGU_STAFF', 'DSWD_STAFF', 'CASE_WORKER'],
    items: [
      {
        id: 'citizens',
        label: 'Citizens',
        href: '/citizens',
        icon: '👥',
        description: 'Manage citizen registrations and profiles',
        roles: ['LGU_STAFF', 'DSWD_STAFF', 'CASE_WORKER'],
        children: [
          {
            id: 'citizen-registrations',
            label: 'Registrations',
            href: '/citizens/registrations',
            roles: ['LGU_STAFF', 'DSWD_STAFF'],
          },
          {
            id: 'citizen-verification',
            label: 'Verification',
            href: '/citizens/verification',
            roles: ['LGU_STAFF'],
          },
        ],
      },
      {
        id: 'cases',
        label: 'Cases',
        href: '/cases',
        icon: '📁',
        description: 'Manage individual and household cases',
        roles: ['LGU_STAFF', 'DSWD_STAFF', 'CASE_WORKER'],
        badge: {
          text: 'New',
          variant: 'warning',
        },
      },
      {
        id: 'payments',
        label: 'Payments',
        href: '/payments',
        icon: '💳',
        description: 'Process and track benefit payments',
        roles: ['LGU_STAFF', 'DSWD_STAFF'],
      },
    ],
  },
  {
    id: 'policy-management',
    label: 'Policy & Programs',
    roles: ['DSWD_STAFF'],
    items: [
      {
        id: 'programs',
        label: 'Programs',
        href: '/programs',
        icon: '🎯',
        description: 'Manage social protection programs',
        roles: ['DSWD_STAFF'],
      },
      {
        id: 'eligibility-rules',
        label: 'Eligibility Rules',
        href: '/eligibility-rules',
        icon: '⚖️',
        description: 'Configure program eligibility criteria',
        roles: ['DSWD_STAFF'],
      },
    ],
  },
  {
    id: 'analytics',
    label: 'Analytics & Reports',
    roles: ['LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN'],
    items: [
      {
        id: 'analytics',
        label: 'Analytics',
        href: '/analytics',
        icon: '📊',
        description: 'View system analytics and insights',
        roles: ['LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN'],
      },
      {
        id: 'reports',
        label: 'Reports',
        href: '/reports',
        icon: '📈',
        description: 'Generate and export reports',
        roles: ['LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN'],
      },
    ],
  },
  {
    id: 'administration',
    label: 'Administration',
    roles: ['SYSTEM_ADMIN'],
    items: [
      {
        id: 'users',
        label: 'User Management',
        href: '/admin/users',
        icon: '👥',
        description: 'Manage system users and roles',
        roles: ['SYSTEM_ADMIN'],
      },
      {
        id: 'system-settings',
        label: 'System Settings',
        href: '/admin/settings',
        icon: '⚙️',
        description: 'Configure system settings',
        roles: ['SYSTEM_ADMIN'],
      },
      {
        id: 'audit-logs',
        label: 'Audit Logs',
        href: '/admin/audit',
        icon: '📋',
        description: 'View system audit trails',
        roles: ['SYSTEM_ADMIN'],
      },
    ],
  },
];

// Navigation item component
const NavigationItemComponent: React.FC<{
  item: NavigationItem;
  userRole: UserRole;
  userPermissions?: string[];
  variant: 'sidebar' | 'horizontal' | 'mobile';
  showLabels: boolean;
  showDescriptions: boolean;
  showBadges: boolean;
  isActive: boolean;
  level: number;
  onItemClick?: (item: NavigationItem) => void;
}> = ({
  item,
  userRole,
  userPermissions = [],
  variant,
  showLabels,
  showDescriptions,
  showBadges,
  isActive,
  level,
  onItemClick,
}) => {
  const pathname = usePathname();
  
  // Check if user has access to this item
  const hasAccess = useMemo(() => {
    // Check role access
    if (!item.roles.includes(userRole)) return false;
    
    // Check permission access if specified
    if (item.permissions && item.permissions.length > 0) {
      return item.permissions.some(permission => userPermissions.includes(permission));
    }
    
    return true;
  }, [item.roles, item.permissions, userRole, userPermissions]);

  // Check if item is currently active
  const isItemActive = pathname === item.href || pathname.startsWith(item.href + '/');

  if (!hasAccess) return null;

  const handleClick = () => {
    onItemClick?.(item);
  };

  const content = (
    <div
      className={cn(
        'flex items-center gap-3 px-3 py-2 rounded-md transition-all duration-200',
        level > 0 && 'ml-6',
        variant === 'horizontal' && 'px-4 py-2',
        variant === 'mobile' && 'px-4 py-3',
        isItemActive
          ? 'bg-primary-100 text-primary-700 font-medium'
          : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900',
        item.disabled && 'opacity-50 cursor-not-allowed'
      )}
      onClick={handleClick}
    >
      {/* Icon */}
      {item.icon && (
        <span className="flex-shrink-0 text-lg">
          {item.icon}
        </span>
      )}

      {/* Content */}
      {showLabels && (
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <span className="truncate">{item.label}</span>
            {showBadges && item.badge && (
              <Badge variant={item.badge.variant} size="sm">
                {item.badge.text}
              </Badge>
            )}
          </div>
          {showDescriptions && item.description && variant !== 'horizontal' && (
            <p className="text-xs text-gray-500 mt-0.5 truncate">
              {item.description}
            </p>
          )}
        </div>
      )}

      {/* External link indicator */}
      {item.external && (
        <span className="flex-shrink-0 text-gray-400 text-sm">↗</span>
      )}
    </div>
  );

  if (item.disabled) {
    return <div className="cursor-not-allowed">{content}</div>;
  }

  if (item.external) {
    return (
      <a
        href={item.href}
        target="_blank"
        rel="noopener noreferrer"
        className="block"
      >
        {content}
      </a>
    );
  }

  return (
    <Link href={item.href} className="block">
      {content}
    </Link>
  );
};

// Navigation section component
const NavigationSectionComponent: React.FC<{
  section: NavigationSection;
  userRole: UserRole;
  userPermissions?: string[];
  variant: 'sidebar' | 'horizontal' | 'mobile';
  showLabels: boolean;
  showDescriptions: boolean;
  showBadges: boolean;
  collapsible: boolean;
  onItemClick?: (item: NavigationItem) => void;
}> = ({
  section,
  userRole,
  userPermissions,
  variant,
  showLabels,
  showDescriptions,
  showBadges,
  collapsible,
  onItemClick,
}) => {
  const [isExpanded, setIsExpanded] = React.useState(section.defaultExpanded ?? true);

  // Check if user has access to this section
  const hasAccess = section.roles.includes(userRole);
  
  // Filter accessible items
  const accessibleItems = section.items.filter(item => 
    item.roles.includes(userRole) &&
    (!item.permissions || item.permissions.some(permission => userPermissions?.includes(permission)))
  );

  if (!hasAccess || accessibleItems.length === 0) return null;

  return (
    <div className="space-y-1">
      {/* Section Header */}
      {showLabels && variant !== 'horizontal' && (
        <div
          className={cn(
            'flex items-center justify-between px-3 py-2',
            collapsible && section.collapsible && 'cursor-pointer hover:bg-gray-50 rounded-md'
          )}
          onClick={() => {
            if (collapsible && section.collapsible) {
              setIsExpanded(!isExpanded);
            }
          }}
        >
          <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
            {section.label}
          </h3>
          {collapsible && section.collapsible && (
            <span className={cn('text-gray-400 transition-transform', isExpanded && 'rotate-90')}>
              ▶
            </span>
          )}
        </div>
      )}

      {/* Section Items */}
      {(!section.collapsible || isExpanded) && (
        <div className={cn('space-y-1', variant === 'horizontal' && 'flex space-y-0 space-x-1')}>
          {accessibleItems.map((item) => (
            <div key={item.id}>
              <NavigationItemComponent
                item={item}
                userRole={userRole}
                userPermissions={userPermissions}
                variant={variant}
                showLabels={showLabels}
                showDescriptions={showDescriptions}
                showBadges={showBadges}
                isActive={false}
                level={0}
                onItemClick={onItemClick}
              />
              
              {/* Child items */}
              {item.children && (
                <div className="mt-1 space-y-1">
                  {item.children
                    .filter(child => child.roles.includes(userRole))
                    .map((child) => (
                      <NavigationItemComponent
                        key={child.id}
                        item={child}
                        userRole={userRole}
                        userPermissions={userPermissions}
                        variant={variant}
                        showLabels={showLabels}
                        showDescriptions={showDescriptions}
                        showBadges={showBadges}
                        isActive={false}
                        level={1}
                        onItemClick={onItemClick}
                      />
                    ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

// Main role-based navigation component
const RoleBasedNavigation = forwardRef<HTMLElement, RoleBasedNavigationProps>(
  (
    {
      className,
      sections,
      userRole,
      userPermissions = [],
      variant = 'sidebar',
      showLabels = true,
      showDescriptions = false,
      showBadges = true,
      collapsible = true,
      onItemClick,
      ...props
    },
    ref
  ) => {
    // Filter sections based on user role
    const accessibleSections = sections.filter(section => 
      section.roles.includes(userRole)
    );

    return (
      <nav
        ref={ref}
        className={cn(
          'space-y-6',
          variant === 'horizontal' && 'flex space-y-0 space-x-6',
          variant === 'mobile' && 'space-y-2',
          className
        )}
        role="navigation"
        aria-label="Main navigation"
        {...props}
      >
        {accessibleSections.map((section) => (
          <NavigationSectionComponent
            key={section.id}
            section={section}
            userRole={userRole}
            userPermissions={userPermissions}
            variant={variant}
            showLabels={showLabels}
            showDescriptions={showDescriptions}
            showBadges={showBadges}
            collapsible={collapsible}
            onItemClick={onItemClick}
          />
        ))}
      </nav>
    );
  }
);

RoleBasedNavigation.displayName = 'RoleBasedNavigation';

export { RoleBasedNavigation };
export type { RoleBasedNavigationProps, NavigationItem, NavigationSection };
