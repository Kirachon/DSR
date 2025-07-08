'use client';

// Staff Dashboard Component
// Dashboard for DSWD staff, LGU staff, and case workers

import Link from 'next/link';
import React, { useState, useEffect } from 'react';

import { Card, Button, Alert } from '@/components/ui';
import {
  registrationApi,
  paymentApi,
  grievanceApi,
  analyticsApi,
} from '@/lib/api';
import type { User } from '@/types';

// Staff Dashboard props interface
interface StaffDashboardProps {
  user: User;
}

// Dashboard data interface
interface StaffDashboardData {
  pendingRegistrations: any[];
  recentPayments: any[];
  assignedCases: any[];
  kpis: any[];
  loading: boolean;
  error: string | null;
}

// Status card interface
interface StatusCard {
  title: string;
  value: string;
  description: string;
  status: 'success' | 'warning' | 'error' | 'info';
  icon: React.ReactNode;
  action?: {
    label: string;
    href: string;
  };
}

// Icons
const TaskIcon = () => (
  <svg
    className='w-6 h-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2'
    />
  </svg>
);

const PaymentIcon = () => (
  <svg
    className='w-6 h-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1'
    />
  </svg>
);

const CaseIcon = () => (
  <svg
    className='w-6 h-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M8 7V3a2 2 0 012-2h4a2 2 0 012 2v4m-6 0V6a2 2 0 012-2h4a2 2 0 012 2v1m-6 0h8m-9 0h10a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2v-8a2 2 0 012-2z'
    />
  </svg>
);

const StatsIcon = () => (
  <svg
    className='w-6 h-6'
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

// Status cards for staff
const getStatusCards = (user: User, data: StaffDashboardData): StatusCard[] => {
  const pendingCount = data.pendingRegistrations.length;
  const assignedCasesCount = data.assignedCases.length;
  const recentPaymentsCount = data.recentPayments.length;

  return [
    {
      title: 'Pending Registrations',
      value: data.loading ? '...' : pendingCount.toString(),
      description: 'Registrations awaiting review',
      status: pendingCount > 10 ? 'warning' : 'info',
      icon: <TaskIcon />,
      action: {
        label: 'Review',
        href: '/registration?status=pending',
      },
    },
    {
      title: 'Assigned Cases',
      value: data.loading ? '...' : assignedCasesCount.toString(),
      description: 'Cases assigned to you',
      status: assignedCasesCount > 5 ? 'warning' : 'success',
      icon: <CaseIcon />,
      action: {
        label: 'View Cases',
        href: '/cases?assignee=me',
      },
    },
    {
      title: 'Recent Payments',
      value: data.loading ? '...' : recentPaymentsCount.toString(),
      description: 'Payments processed today',
      status: 'info',
      icon: <PaymentIcon />,
      action: {
        label: 'View Payments',
        href: '/payments',
      },
    },
    {
      title: 'Performance',
      value: data.loading ? '...' : '95%',
      description: 'Monthly completion rate',
      status: 'success',
      icon: <StatsIcon />,
      action: {
        label: 'View Analytics',
        href: '/analytics',
      },
    },
  ];
};

// Staff Dashboard component
export const StaffDashboard: React.FC<StaffDashboardProps> = ({ user }) => {
  const [dashboardData, setDashboardData] = useState<StaffDashboardData>({
    pendingRegistrations: [],
    recentPayments: [],
    assignedCases: [],
    kpis: [],
    loading: true,
    error: null,
  });

  // Load dashboard data
  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setDashboardData(prev => ({ ...prev, loading: true, error: null }));

        // Load pending registrations (staff can review)
        const pendingRegistrations = await registrationApi.getRegistrations({
          status: 'SUBMITTED',
          limit: 10,
        });

        // Load assigned cases
        const assignedCases = await grievanceApi.getCases({
          assignedTo: user.id,
          status: 'OPEN',
          limit: 10,
          dateRange: {
            start: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
              .toISOString()
              .split('T')[0], // 30 days ago
            end: new Date().toISOString().split('T')[0], // today
          },
        });

        // Load recent payments
        const recentPayments = await paymentApi.getPayments({
          limit: 5,
          dateRange: {
            start: new Date(Date.now() - 24 * 60 * 60 * 1000)
              .toISOString()
              .split('T')[0], // Last 24 hours
            end: new Date().toISOString().split('T')[0],
          },
          amountRange: {
            min: 0,
            max: Number.MAX_SAFE_INTEGER,
          },
        });

        // Load KPIs for staff role
        const kpis = await analyticsApi.getKPIValues([
          'REGISTRATION_PROCESSING_TIME',
          'CASE_RESOLUTION_RATE',
          'PAYMENT_SUCCESS_RATE',
        ]);

        setDashboardData({
          pendingRegistrations: pendingRegistrations.content || [],
          assignedCases: assignedCases.content || [],
          recentPayments: recentPayments.content || [],
          kpis,
          loading: false,
          error: null,
        });
      } catch (error) {
        console.warn('Failed to load staff dashboard data:', error);
        setDashboardData(prev => ({
          ...prev,
          loading: false,
          error:
            'Failed to load some dashboard data. Please refresh to try again.',
        }));
      }
    };

    loadDashboardData();
  }, [user.id]);

  const statusCards = getStatusCards(user, dashboardData);

  return (
    <div className='space-y-6'>
      {/* Welcome Section */}
      <div className='bg-gradient-to-r from-primary-600 to-primary-700 rounded-lg p-6 text-white'>
        <h1 className='text-2xl font-bold'>Welcome back, {user.firstName}!</h1>
        <p className='text-primary-100 mt-1'>
          {user.role === 'DSWD_STAFF'
            ? 'DSWD Staff Dashboard'
            : user.role === 'LGU_STAFF'
              ? 'LGU Staff Dashboard'
              : 'Case Worker Dashboard'}
        </p>
      </div>

      {/* Error Alert */}
      {dashboardData.error && (
        <Alert variant='warning' title='Data Loading Issue'>
          {dashboardData.error}
        </Alert>
      )}

      {/* Status Cards */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
        {statusCards.map((card, index) => (
          <Card key={index} className='p-6 hover:shadow-lg transition-shadow'>
            <div className='flex items-center justify-between'>
              <div className='flex-1'>
                <div className='flex items-center space-x-3'>
                  <div
                    className={`p-2 rounded-lg ${
                      card.status === 'success'
                        ? 'bg-success-100 text-success-600'
                        : card.status === 'warning'
                          ? 'bg-warning-100 text-warning-600'
                          : card.status === 'error'
                            ? 'bg-error-100 text-error-600'
                            : 'bg-primary-100 text-primary-600'
                    }`}
                  >
                    {card.icon}
                  </div>
                  <div>
                    <p className='text-sm font-medium text-gray-500'>
                      {card.title}
                    </p>
                    <p className='text-2xl font-bold text-gray-900'>
                      {card.value}
                    </p>
                  </div>
                </div>
                <p className='text-sm text-gray-600 mt-2'>{card.description}</p>
                {card.action && (
                  <Link
                    href={card.action.href}
                    className='inline-flex items-center text-sm font-medium text-primary-600 hover:text-primary-500 mt-2'
                  >
                    {card.action.label}
                    <svg
                      className='w-4 h-4 ml-1'
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
                  </Link>
                )}
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Main Content Grid */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        {/* Pending Tasks */}
        <Card className='p-6'>
          <h2 className='text-lg font-semibold text-gray-900 mb-4'>
            Pending Tasks
          </h2>
          {dashboardData.loading ? (
            <div className='space-y-3'>
              {[1, 2, 3].map(i => (
                <div key={i} className='animate-pulse'>
                  <div className='h-4 bg-gray-300 rounded w-3/4 mb-2'></div>
                  <div className='h-3 bg-gray-300 rounded w-1/2'></div>
                </div>
              ))}
            </div>
          ) : (
            <div className='space-y-4'>
              {dashboardData.pendingRegistrations
                .slice(0, 5)
                .map((registration, index) => (
                  <div
                    key={index}
                    className='flex items-center justify-between p-3 bg-gray-50 rounded-lg'
                  >
                    <div className='flex-1'>
                      <p className='text-sm font-medium text-gray-900'>
                        Registration Review -{' '}
                        {registration.householdHead?.firstName}{' '}
                        {registration.householdHead?.lastName}
                      </p>
                      <p className='text-xs text-gray-500'>
                        Submitted:{' '}
                        {new Date(registration.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <Button size='sm' variant='outline'>
                      Review
                    </Button>
                  </div>
                ))}

              {dashboardData.pendingRegistrations.length === 0 && (
                <div className='text-center py-8 text-gray-500'>
                  <TaskIcon />
                  <p className='mt-2'>No pending tasks</p>
                  <p className='text-sm'>
                    Great job staying on top of your work!
                  </p>
                </div>
              )}
            </div>
          )}
        </Card>

        {/* Recent Activity */}
        <Card className='p-6'>
          <h2 className='text-lg font-semibold text-gray-900 mb-4'>
            Recent Activity
          </h2>
          {dashboardData.loading ? (
            <div className='space-y-3'>
              {[1, 2, 3].map(i => (
                <div key={i} className='animate-pulse'>
                  <div className='h-4 bg-gray-300 rounded w-3/4 mb-2'></div>
                  <div className='h-3 bg-gray-300 rounded w-1/2'></div>
                </div>
              ))}
            </div>
          ) : (
            <div className='space-y-4'>
              {/* Recent cases */}
              {dashboardData.assignedCases.slice(0, 3).map((case_, index) => (
                <div
                  key={`case-${index}`}
                  className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'
                >
                  <div
                    className={`flex-shrink-0 w-2 h-2 rounded-full ${
                      case_.priority === 'HIGH'
                        ? 'bg-error-500'
                        : case_.priority === 'MEDIUM'
                          ? 'bg-warning-500'
                          : 'bg-success-500'
                    }`}
                  ></div>
                  <div className='flex-1'>
                    <p className='text-sm font-medium text-gray-900'>
                      Case updated: {case_.title}
                    </p>
                    <p className='text-xs text-gray-500'>
                      {new Date(case_.updatedAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>
              ))}

              {/* Recent payments */}
              {dashboardData.recentPayments
                .slice(0, 2)
                .map((payment, index) => (
                  <div
                    key={`payment-${index}`}
                    className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'
                  >
                    <div
                      className={`flex-shrink-0 w-2 h-2 rounded-full ${
                        payment.status === 'COMPLETED'
                          ? 'bg-success-500'
                          : payment.status === 'PENDING'
                            ? 'bg-warning-500'
                            : 'bg-error-500'
                      }`}
                    ></div>
                    <div className='flex-1'>
                      <p className='text-sm font-medium text-gray-900'>
                        Payment processed - ₱{payment.amount?.toLocaleString()}
                      </p>
                      <p className='text-xs text-gray-500'>
                        {new Date(payment.updatedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                ))}

              {dashboardData.assignedCases.length === 0 &&
                dashboardData.recentPayments.length === 0 && (
                  <div className='text-center py-8 text-gray-500'>
                    <p>No recent activity</p>
                    <p className='text-sm mt-1'>
                      Activity will appear here as you work
                    </p>
                  </div>
                )}
            </div>
          )}
        </Card>
      </div>

      {/* Quick Actions */}
      <Card className='p-6'>
        <h2 className='text-lg font-semibold text-gray-900 mb-4'>
          Quick Actions
        </h2>
        <div className='grid grid-cols-2 md:grid-cols-4 gap-4'>
          <Link
            href='/registration'
            className='flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors'
          >
            <TaskIcon />
            <span className='text-sm font-medium text-gray-900 mt-2'>
              Review Registrations
            </span>
          </Link>
          <Link
            href='/cases'
            className='flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors'
          >
            <CaseIcon />
            <span className='text-sm font-medium text-gray-900 mt-2'>
              Manage Cases
            </span>
          </Link>
          <Link
            href='/payments'
            className='flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors'
          >
            <PaymentIcon />
            <span className='text-sm font-medium text-gray-900 mt-2'>
              Process Payments
            </span>
          </Link>
          <Link
            href='/analytics'
            className='flex flex-col items-center p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors'
          >
            <StatsIcon />
            <span className='text-sm font-medium text-gray-900 mt-2'>
              View Analytics
            </span>
          </Link>
        </div>
      </Card>
    </div>
  );
};

export default StaffDashboard;
