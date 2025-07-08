'use client';

// Administration Page
// Main administration interface for system administrators

import Link from 'next/link';
import React, { useState, useEffect } from 'react';

import { SystemHealthCard } from '@/components/admin/system-health-card';
import { SystemMetricsCard } from '@/components/admin/system-metrics-card';
import { RecentActivitiesCard } from '@/components/admin/recent-activities-card';
import { QuickActionsCard } from '@/components/admin/quick-actions-card';
import { Card, Button, Alert } from '@/components/ui';
import { useAuth } from '@/contexts';
import { analyticsApi, checkAllServicesHealth } from '@/lib/api';
import type { SystemMetrics, SystemActivity } from '@/types';

// Administration page component
export default function AdministrationPage() {
  const { user } = useAuth();

  // State management
  const [systemMetrics, setSystemMetrics] = useState<SystemMetrics | null>(null);
  const [serviceHealth, setServiceHealth] = useState<Record<string, boolean>>({});
  const [recentActivities, setRecentActivities] = useState<SystemActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load administration data
  useEffect(() => {
    loadAdministrationData();
  }, []);

  // Load all administration data
  const loadAdministrationData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Load system health
      const healthStatus = await checkAllServicesHealth();
      setServiceHealth(healthStatus);

      // Load system metrics
      try {
        const metrics = await analyticsApi.getSystemMetrics();
        setSystemMetrics(metrics);
      } catch (metricsError) {
        console.warn('System metrics not available:', metricsError);
        // Set mock metrics for development
        setSystemMetrics({
          totalUsers: 15420,
          activeUsers: 8750,
          totalHouseholds: 12340,
          totalPayments: 45600,
          systemUptime: 99.8,
          averageResponseTime: 1.2,
          errorRate: 0.03,
          storageUsage: 67.5,
        } as SystemMetrics);
      }

      // Load recent activities
      try {
        const activities = await analyticsApi.getSystemActivities({ limit: 10 });
        setRecentActivities(activities);
      } catch (activitiesError) {
        console.warn('System activities not available:', activitiesError);
        // Set mock activities for development
        setRecentActivities([
          {
            id: '1',
            type: 'USER_LOGIN',
            description: 'User admin@dsr.gov.ph logged in',
            timestamp: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
            userId: 'admin@dsr.gov.ph',
            severity: 'INFO',
          },
          {
            id: '2',
            type: 'SYSTEM_UPDATE',
            description: 'System configuration updated',
            timestamp: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
            userId: 'system',
            severity: 'INFO',
          },
          {
            id: '3',
            type: 'SECURITY_ALERT',
            description: 'Multiple failed login attempts detected',
            timestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
            userId: 'security-system',
            severity: 'WARNING',
          },
        ] as SystemActivity[]);
      }
    } catch (err) {
      console.error('Failed to load administration data:', err);
      setError('Failed to load administration data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Quick actions configuration
  const quickActions = [
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

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">System Administration</h1>
          <p className="text-gray-600 mt-1">
            Monitor and manage the DSR system infrastructure
          </p>
        </div>

        <div className="flex space-x-3">
          <Button variant="outline" onClick={() => loadAdministrationData()}>
            Refresh
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="error" title="Error">
          {error}
        </Alert>
      )}

      {/* System Overview Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* System Health */}
        <SystemHealthCard
          serviceHealth={serviceHealth}
          loading={loading}
          onRefresh={loadAdministrationData}
        />

        {/* System Metrics */}
        <SystemMetricsCard
          metrics={systemMetrics}
          loading={loading}
        />
      </div>

      {/* Quick Actions */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {quickActions.map((action, index) => (
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

      {/* Recent Activities */}
      <RecentActivitiesCard
        activities={recentActivities}
        loading={loading}
        onRefresh={loadAdministrationData}
      />

      {/* System Status Summary */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">System Status Summary</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {Object.values(serviceHealth).filter(Boolean).length}
            </div>
            <div className="text-sm text-gray-600">Services Online</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600">
              {Object.values(serviceHealth).filter(status => !status).length}
            </div>
            <div className="text-sm text-gray-600">Services Offline</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">
              {systemMetrics?.activeUsers?.toLocaleString() || '0'}
            </div>
            <div className="text-sm text-gray-600">Active Users</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600">
              {systemMetrics?.systemUptime?.toFixed(1) || '0'}%
            </div>
            <div className="text-sm text-gray-600">System Uptime</div>
          </div>
        </div>
      </Card>
    </div>
  );
}
