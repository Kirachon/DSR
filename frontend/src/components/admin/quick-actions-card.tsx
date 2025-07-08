'use client';

// Quick Actions Card Component
// Provides quick access to common administrative actions

import React from 'react';
import Link from 'next/link';

import { Card } from '@/components/ui';

// Quick action interface
interface QuickAction {
  title: string;
  description: string;
  href: string;
  icon: string;
  color: string;
}

// Quick actions card props interface
interface QuickActionsCardProps {
  actions?: QuickAction[];
}

// Default quick actions
const DEFAULT_ACTIONS: QuickAction[] = [
  {
    title: 'User Management',
    description: 'Manage users, roles, and permissions',
    href: '/admin/users',
    icon: '👥',
    color: 'bg-blue-500',
  },
  {
    title: 'System Settings',
    description: 'Configure system parameters and settings',
    href: '/admin/settings',
    icon: '⚙️',
    color: 'bg-green-500',
  },
  {
    title: 'Security Center',
    description: 'Monitor security events and configure policies',
    href: '/admin/security',
    icon: '🔒',
    color: 'bg-red-500',
  },
  {
    title: 'System Logs',
    description: 'View and analyze system logs',
    href: '/admin/logs',
    icon: '📋',
    color: 'bg-yellow-500',
  },
  {
    title: 'Backup & Recovery',
    description: 'Manage system backups and recovery',
    href: '/admin/backup',
    icon: '💾',
    color: 'bg-purple-500',
  },
  {
    title: 'Performance Monitor',
    description: 'Monitor system performance and resources',
    href: '/admin/performance',
    icon: '📊',
    color: 'bg-indigo-500',
  },
];

// Quick Actions Card component
export const QuickActionsCard: React.FC<QuickActionsCardProps> = ({
  actions = DEFAULT_ACTIONS,
}) => {
  return (
    <Card className="p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {actions.map((action, index) => (
          <Link key={index} href={action.href}>
            <div className="flex items-center p-4 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer">
              <div className={`flex-shrink-0 w-12 h-12 ${action.color} rounded-lg flex items-center justify-center text-white text-xl`}>
                {action.icon}
              </div>
              <div className="ml-4">
                <h3 className="font-medium text-gray-900">{action.title}</h3>
                <p className="text-sm text-gray-600">{action.description}</p>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </Card>
  );
};
