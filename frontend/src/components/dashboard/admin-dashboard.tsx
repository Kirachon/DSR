'use client';

// System Admin Dashboard Component
// Dashboard interface for system administrators with comprehensive system management tools
// Provides system monitoring, user management, security oversight, and administrative controls

import Link from 'next/link';
import React, { useState, useEffect } from 'react';

import { Card, Button, Alert } from '@/components/ui';
import { ServiceStatus } from '@/components/system/service-status';
import { WorkflowStatus } from '@/components/workflows/workflow-status';
import { analyticsApi, checkAllServicesHealth } from '@/lib/api';
import type { User } from '@/types';

// Dashboard props interface
interface AdminDashboardProps {
  user: User;
}

// System metric interface
interface SystemMetric {
  title: string;
  value: string | number;
  change: string;
  trend: 'up' | 'down' | 'neutral';
  status: 'healthy' | 'warning' | 'critical';
  icon: React.ReactNode;
}

// Service status interface
interface ServiceStatus {
  name: string;
  status: 'online' | 'offline' | 'degraded';
  uptime: string;
  responseTime: string;
  lastCheck: string;
}

// User activity interface
interface UserActivity {
  id: string;
  action: string;
  user: string;
  role: string;
  timestamp: string;
  status: 'success' | 'warning' | 'error';
}

// Security alert interface
interface SecurityAlert {
  id: string;
  type:
    | 'login_failure'
    | 'suspicious_activity'
    | 'policy_violation'
    | 'system_breach';
  severity: 'low' | 'medium' | 'high' | 'critical';
  description: string;
  timestamp: string;
  resolved: boolean;
}

// Icons
const ServerIcon = () => (
  <svg
    className='h-6 w-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01'
    />
  </svg>
);

const UsersIcon = () => (
  <svg
    className='h-6 w-6'
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

const SecurityIcon = () => (
  <svg
    className='h-6 w-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z'
    />
  </svg>
);

const DatabaseIcon = () => (
  <svg
    className='h-6 w-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4'
    />
  </svg>
);

const PerformanceIcon = () => (
  <svg
    className='h-6 w-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M13 10V3L4 14h7v7l9-11h-7z'
    />
  </svg>
);

const ConfigIcon = () => (
  <svg
    className='h-6 w-6'
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

const AuditIcon = () => (
  <svg
    className='h-6 w-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01'
    />
  </svg>
);

const TrendUpIcon = () => (
  <svg
    className='h-4 w-4'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M13 7h8m0 0v8m0-8l-8 8-4-4-6 6'
    />
  </svg>
);

const TrendDownIcon = () => (
  <svg
    className='h-4 w-4'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M13 17h8m0 0V9m0 8l-8-8-4 4-6-6'
    />
  </svg>
);

// System metrics for admin dashboard
const systemMetrics: SystemMetric[] = [
  {
    title: 'System Uptime',
    value: '99.97%',
    change: '+0.02% this month',
    trend: 'up',
    status: 'healthy',
    icon: <ServerIcon />,
  },
  {
    title: 'Active Users',
    value: '12,847',
    change: '+342 this week',
    trend: 'up',
    status: 'healthy',
    icon: <UsersIcon />,
  },
  {
    title: 'Database Performance',
    value: '1.2ms',
    change: 'Avg query time',
    trend: 'neutral',
    status: 'healthy',
    icon: <DatabaseIcon />,
  },
  {
    title: 'Security Score',
    value: '98.5%',
    change: '+1.2% this week',
    trend: 'up',
    status: 'healthy',
    icon: <SecurityIcon />,
  },
];

// System Admin Dashboard component
export const AdminDashboard: React.FC<AdminDashboardProps> = ({ user }) => {
  // State for real-time data
  const [serviceStatuses, setServiceStatuses] = useState<ServiceStatus[]>([]);
  const [userActivities, setUserActivities] = useState<UserActivity[]>([]);
  const [securityAlerts, setSecurityAlerts] = useState<SecurityAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load dashboard data on component mount
  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Load service health status
      const healthStatus = await checkAllServicesHealth();
      const services = Object.entries(healthStatus).map(([name, isHealthy]) => ({
        name: name.charAt(0).toUpperCase() + name.slice(1) + ' Service',
        status: isHealthy ? 'online' : 'offline',
        uptime: isHealthy ? '99.9%' : '0%',
        responseTime: isHealthy ? '<200ms' : 'N/A',
        lastCheck: 'Just now',
      }));
      setServiceStatuses(services);

      // Load analytics data for user activities and security alerts
      try {
        const dashboardData = await analyticsApi.getDashboardSummary();
        if (dashboardData.userActivities) {
          setUserActivities(dashboardData.userActivities);
        }
        if (dashboardData.securityAlerts) {
          setSecurityAlerts(dashboardData.securityAlerts);
        }
      } catch (analyticsError) {
        console.warn('Analytics data not available:', analyticsError);
        // Set empty arrays if analytics service is not available
        setUserActivities([]);
        setSecurityAlerts([]);
      }
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const unresolvedAlerts = securityAlerts.filter(alert => !alert.resolved);
  const criticalAlerts = unresolvedAlerts.filter(
    alert => alert.severity === 'critical' || alert.severity === 'high'
  );

  if (loading) {
    return (
      <div className='space-y-6'>
        <div className='text-center py-8'>
          <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto'></div>
          <p className='mt-2 text-gray-600'>Loading dashboard data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className='space-y-6'>
        <Alert variant='error' title='Error Loading Dashboard'>
          {error}
          <Button onClick={loadDashboardData} className='ml-2'>
            Retry
          </Button>
        </Alert>
      </div>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Security Alerts */}
      {criticalAlerts.length > 0 && (
        <Alert variant='error' title='Critical Security Alerts'>
          {criticalAlerts.length} high-priority security alert
          {criticalAlerts.length > 1 ? 's' : ''} require
          {criticalAlerts.length === 1 ? 's' : ''} immediate attention.
          <Link
            href='/admin/security/alerts'
            className='font-medium underline ml-1'
          >
            View alerts
          </Link>
        </Alert>
      )}

      {/* System Health Metrics */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
        {systemMetrics.map((metric, index) => (
          <Card key={index} className='p-6'>
            <div className='flex items-center'>
              <div
                className={`flex-shrink-0 p-3 rounded-lg ${
                  metric.status === 'healthy'
                    ? 'bg-success-100 text-success-600'
                    : metric.status === 'warning'
                      ? 'bg-warning-100 text-warning-600'
                      : 'bg-error-100 text-error-600'
                }`}
              >
                {metric.icon}
              </div>
              <div className='ml-4'>
                <p className='text-sm font-medium text-gray-500'>
                  {metric.title}
                </p>
                <p className='text-2xl font-bold text-gray-900'>
                  {metric.value}
                </p>
                <p
                  className={`text-sm flex items-center ${
                    metric.trend === 'up'
                      ? 'text-success-600'
                      : metric.trend === 'down'
                        ? 'text-error-600'
                        : 'text-gray-600'
                  }`}
                >
                  {metric.trend === 'up' && <TrendUpIcon />}
                  {metric.trend === 'down' && <TrendDownIcon />}
                  <span className='ml-1'>{metric.change}</span>
                </p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Service Status and Workflow Status */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        <ServiceStatus />
        <WorkflowStatus />
      </div>

      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        {/* Administrative Quick Actions */}
        <Card className='p-6'>
          <h2 className='text-lg font-semibold text-gray-900 mb-4'>
            Administrative Tools
          </h2>
          <div className='space-y-3'>
            <Link href='/admin/users'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-primary-100 text-primary-600 rounded-lg'>
                  <UsersIcon />
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>User Management</p>
                  <p className='text-sm text-gray-600'>
                    Manage users, roles, and permissions
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/admin/system/config'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-success-100 text-success-600 rounded-lg'>
                  <ConfigIcon />
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>
                    System Configuration
                  </p>
                  <p className='text-sm text-gray-600'>
                    Configure system settings and parameters
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/admin/security'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-error-100 text-error-600 rounded-lg'>
                  <SecurityIcon />
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>Security Center</p>
                  <p className='text-sm text-gray-600'>
                    Monitor security alerts and policies
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/admin/audit'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-accent-100 text-accent-600 rounded-lg'>
                  <AuditIcon />
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>Audit Logs</p>
                  <p className='text-sm text-gray-600'>
                    View system audit trails and logs
                  </p>
                </div>
              </div>
            </Link>
          </div>
        </Card>

        {/* Service Status Monitor */}
        <Card className='p-6'>
          <h2 className='text-lg font-semibold text-gray-900 mb-4'>
            Service Status
          </h2>
          <div className='space-y-4'>
            {serviceStatuses.map((service, index) => (
              <div
                key={index}
                className='flex items-center justify-between p-3 bg-gray-50 rounded-lg'
              >
                <div className='flex items-center space-x-3'>
                  <div
                    className={`w-3 h-3 rounded-full ${
                      service.status === 'online'
                        ? 'bg-success-500'
                        : service.status === 'degraded'
                          ? 'bg-warning-500'
                          : 'bg-error-500'
                    }`}
                  ></div>
                  <div>
                    <p className='font-medium text-gray-900 text-sm'>
                      {service.name}
                    </p>
                    <div className='flex items-center space-x-4 text-xs text-gray-500'>
                      <span>Uptime: {service.uptime}</span>
                      <span>Response: {service.responseTime}</span>
                    </div>
                  </div>
                </div>
                <div className='text-right'>
                  <span
                    className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                      service.status === 'online'
                        ? 'bg-success-100 text-success-800'
                        : service.status === 'degraded'
                          ? 'bg-warning-100 text-warning-800'
                          : 'bg-error-100 text-error-800'
                    }`}
                  >
                    {service.status}
                  </span>
                  <p className='text-xs text-gray-500 mt-1'>
                    {service.lastCheck}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Recent User Activity */}
      <Card className='p-6'>
        <h2 className='text-lg font-semibold text-gray-900 mb-4'>
          Recent User Activity
        </h2>
        <div className='overflow-x-auto'>
          <table className='min-w-full divide-y divide-gray-200'>
            <thead className='bg-gray-50'>
              <tr>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>
                  Action
                </th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>
                  User
                </th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>
                  Role
                </th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>
                  Time
                </th>
                <th className='px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider'>
                  Status
                </th>
              </tr>
            </thead>
            <tbody className='bg-white divide-y divide-gray-200'>
              {userActivities.map(activity => (
                <tr key={activity.id} className='hover:bg-gray-50'>
                  <td className='px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900'>
                    {activity.action}
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm text-gray-500'>
                    {activity.user}
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm text-gray-500'>
                    <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800'>
                      {activity.role.replace('_', ' ')}
                    </span>
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm text-gray-500'>
                    {activity.timestamp}
                  </td>
                  <td className='px-6 py-4 whitespace-nowrap text-sm text-gray-500'>
                    <span
                      className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                        activity.status === 'success'
                          ? 'bg-success-100 text-success-800'
                          : activity.status === 'warning'
                            ? 'bg-warning-100 text-warning-800'
                            : 'bg-error-100 text-error-800'
                      }`}
                    >
                      {activity.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Security Alerts Panel */}
      <Card className='p-6'>
        <div className='flex items-center justify-between mb-4'>
          <h2 className='text-lg font-semibold text-gray-900'>
            Security Alerts
          </h2>
          <Link href='/admin/security/alerts'>
            <Button variant='outline' size='sm'>
              View All
            </Button>
          </Link>
        </div>
        <div className='space-y-4'>
          {securityAlerts.slice(0, 5).map(alert => (
            <div
              key={alert.id}
              className={`flex items-start space-x-3 p-3 rounded-lg border ${
                alert.resolved
                  ? 'bg-gray-50 border-gray-200'
                  : alert.severity === 'critical'
                    ? 'bg-error-50 border-error-200'
                    : alert.severity === 'high'
                      ? 'bg-warning-50 border-warning-200'
                      : 'bg-blue-50 border-blue-200'
              }`}
            >
              <div
                className={`flex-shrink-0 w-2 h-2 rounded-full mt-2 ${
                  alert.resolved
                    ? 'bg-gray-400'
                    : alert.severity === 'critical'
                      ? 'bg-error-500'
                      : alert.severity === 'high'
                        ? 'bg-warning-500'
                        : alert.severity === 'medium'
                          ? 'bg-blue-500'
                          : 'bg-gray-500'
                }`}
              ></div>
              <div className='flex-1'>
                <div className='flex items-center space-x-2'>
                  <p className='text-sm font-medium text-gray-900'>
                    {alert.description}
                  </p>
                  <span
                    className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                      alert.severity === 'critical'
                        ? 'bg-error-100 text-error-800'
                        : alert.severity === 'high'
                          ? 'bg-warning-100 text-warning-800'
                          : alert.severity === 'medium'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {alert.severity}
                  </span>
                  {alert.resolved && (
                    <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-success-100 text-success-800'>
                      Resolved
                    </span>
                  )}
                </div>
                <div className='flex items-center space-x-4 mt-1'>
                  <p className='text-xs text-gray-500'>
                    Type: {alert.type.replace('_', ' ')}
                  </p>
                  <p className='text-xs text-gray-500'>{alert.timestamp}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};

export default AdminDashboard;
