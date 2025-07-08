'use client';

// Citizen Dashboard Component
// Dashboard interface for citizens to view their benefits, applications, and profile

import Link from 'next/link';
import React, { useState, useEffect } from 'react';

import { Card, Button, Alert } from '@/components/ui';
import {
  registrationApi,
  eligibilityApi,
  paymentApi,
  grievanceApi,
} from '@/lib/api';
import type { User } from '@/types';

// Dashboard props interface
interface CitizenDashboardProps {
  user: User;
}

// Quick action interface
interface QuickAction {
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'accent';
}

// Status card interface
interface StatusCard {
  title: string;
  value: string | number;
  description: string;
  status: 'success' | 'warning' | 'error' | 'info';
  icon: React.ReactNode;
}

// Icons
const ProfileIcon = () => (
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
      d='M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z'
    />
  </svg>
);

const DocumentIcon = () => (
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
      d='M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
    />
  </svg>
);

const BenefitsIcon = () => (
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
      d='M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z'
    />
  </svg>
);

const SupportIcon = () => (
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
      d='M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192L5.636 18.364M12 2.25a9.75 9.75 0 100 19.5 9.75 9.75 0 000-19.5z'
    />
  </svg>
);

// Quick actions for citizens
const quickActions: QuickAction[] = [
  {
    title: 'Update Profile',
    description: 'Keep your personal information current',
    href: '/profile',
    icon: <ProfileIcon />,
    variant: 'primary',
  },
  {
    title: 'Register Household',
    description: 'Complete household registration',
    href: '/dashboard/registration',
    icon: <DocumentIcon />,
    variant: 'secondary',
  },
  {
    title: 'View Benefits',
    description: 'Check your current benefits status',
    href: '/benefits',
    icon: <BenefitsIcon />,
    variant: 'accent',
  },
  {
    title: 'Get Support',
    description: 'Contact support for assistance',
    href: '/support',
    icon: <SupportIcon />,
  },
];

// Status cards for citizens
const getStatusCards = (user: User, data: DashboardData): StatusCard[] => {
  const activeBenefits = data.eligibilityAssessments.filter(
    e => e.status === 'ELIGIBLE'
  ).length;
  const pendingApplications = data.registrations.filter(
    r => r.status === 'SUBMITTED' || r.status === 'UNDER_REVIEW'
  ).length;

  return [
    {
      title: 'Profile Status',
      value: user.emailVerified ? 'Verified' : 'Pending',
      description: user.emailVerified
        ? 'Your profile is verified'
        : 'Please verify your email',
      status: user.emailVerified ? 'success' : 'warning',
      icon: <ProfileIcon />,
    },
    {
      title: 'Active Benefits',
      value: data.loading ? '...' : activeBenefits.toString(),
      description: 'Currently receiving benefits',
      status: activeBenefits > 0 ? 'success' : 'info',
      icon: <BenefitsIcon />,
    },
    {
      title: 'Pending Applications',
      value: data.loading ? '...' : pendingApplications.toString(),
      description: 'Applications under review',
      status: pendingApplications > 0 ? 'warning' : 'info',
      icon: <DocumentIcon />,
    },
    {
      title: 'Account Status',
      value: user.status,
      description: 'Your account is active',
      status: user.status === 'ACTIVE' ? 'success' : 'warning',
      icon: <ProfileIcon />,
    },
  ];
};

// Dashboard data interface
interface DashboardData {
  registrations: any[];
  eligibilityAssessments: any[];
  recentPayments: any[];
  grievanceCases: any[];
  loading: boolean;
  error: string | null;
}

// Citizen Dashboard component
export const CitizenDashboard: React.FC<CitizenDashboardProps> = ({ user }) => {
  const [dashboardData, setDashboardData] = useState<DashboardData>({
    registrations: [],
    eligibilityAssessments: [],
    recentPayments: [],
    grievanceCases: [],
    loading: true,
    error: null,
  });

  // Load dashboard data
  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setDashboardData(prev => ({ ...prev, loading: true, error: null }));

        // Load user's registrations
        const registrations = await registrationApi.getMyRegistrations();

        // Load eligibility assessments (if user has PSN)
        let eligibilityAssessments: any[] = [];
        if (user.psn) {
          eligibilityAssessments = await eligibilityApi.getHouseholdEligibility(
            user.psn
          );
        }

        // Load recent payments
        const recentPayments = await paymentApi.getPayments({
          beneficiaryId: user.id,
          limit: 5,
          dateRange: {
            start: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000)
              .toISOString()
              .split('T')[0], // 90 days ago
            end: new Date().toISOString().split('T')[0], // today
          },
          amountRange: {
            min: 0,
            max: Number.MAX_SAFE_INTEGER,
          },
        });

        // Load grievance cases
        const grievanceCases = await grievanceApi.getCases({
          submittedBy: user.id,
          dateRange: {
            start: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000)
              .toISOString()
              .split('T')[0], // 90 days ago
            end: new Date().toISOString().split('T')[0], // today
          },
          limit: 10,
        });

        setDashboardData({
          registrations,
          eligibilityAssessments,
          recentPayments: recentPayments.content || [],
          grievanceCases: grievanceCases.content || [],
          loading: false,
          error: null,
        });
      } catch (error) {
        console.warn('Failed to load dashboard data:', error);
        setDashboardData(prev => ({
          ...prev,
          loading: false,
          error:
            'Failed to load some dashboard data. Using cached information.',
        }));
      }
    };

    loadDashboardData();
  }, [user.id, user.psn]);

  const statusCards = getStatusCards(user, dashboardData);

  return (
    <div className='space-y-6'>
      {/* Email Verification Alert */}
      {!user.emailVerified && (
        <Alert variant='warning' title='Email Verification Required'>
          Please verify your email address to access all features.
          <Link
            href='/auth/verify-email'
            className='font-medium underline ml-1'
          >
            Verify now
          </Link>
        </Alert>
      )}

      {/* Dashboard Data Error Alert */}
      {dashboardData.error && (
        <Alert variant='warning' title='Data Loading Issue'>
          {dashboardData.error}
        </Alert>
      )}

      {/* Status Overview */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
        {statusCards.map((card, index) => (
          <Card key={index} className='p-6'>
            <div className='flex items-center'>
              <div
                className={`flex-shrink-0 p-3 rounded-lg ${
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
              <div className='ml-4'>
                <p className='text-sm font-medium text-gray-500'>
                  {card.title}
                </p>
                <p className='text-2xl font-bold text-gray-900'>{card.value}</p>
                <p className='text-sm text-gray-600'>{card.description}</p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card className='p-6'>
        <h2 className='text-lg font-semibold text-gray-900 mb-4'>
          Quick Actions
        </h2>
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
          {quickActions.map((action, index) => (
            <Link key={index} href={action.href}>
              <div className='p-4 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div
                  className={`inline-flex p-2 rounded-lg mb-3 ${
                    action.variant === 'primary'
                      ? 'bg-primary-100 text-primary-600'
                      : action.variant === 'secondary'
                        ? 'bg-secondary-100 text-secondary-600'
                        : action.variant === 'accent'
                          ? 'bg-accent-100 text-accent-600'
                          : 'bg-gray-100 text-gray-600'
                  }`}
                >
                  {action.icon}
                </div>
                <h3 className='font-medium text-gray-900 mb-1'>
                  {action.title}
                </h3>
                <p className='text-sm text-gray-600'>{action.description}</p>
              </div>
            </Link>
          ))}
        </div>
      </Card>

      {/* Recent Activity */}
      <Card className='p-6'>
        <h2 className='text-lg font-semibold text-gray-900 mb-4'>
          Recent Activity
        </h2>
        {dashboardData.loading ? (
          <div className='space-y-4'>
            {[1, 2, 3].map(i => (
              <div
                key={i}
                className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg animate-pulse'
              >
                <div className='flex-shrink-0 w-2 h-2 bg-gray-300 rounded-full'></div>
                <div className='flex-1'>
                  <div className='h-4 bg-gray-300 rounded w-3/4 mb-1'></div>
                  <div className='h-3 bg-gray-300 rounded w-1/2'></div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className='space-y-4'>
            {/* Recent registrations */}
            {dashboardData.registrations
              .slice(0, 2)
              .map((registration, index) => (
                <div
                  key={`reg-${index}`}
                  className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'
                >
                  <div
                    className={`flex-shrink-0 w-2 h-2 rounded-full ${
                      registration.status === 'APPROVED'
                        ? 'bg-success-500'
                        : registration.status === 'SUBMITTED'
                          ? 'bg-primary-500'
                          : 'bg-warning-500'
                    }`}
                  ></div>
                  <div className='flex-1'>
                    <p className='text-sm font-medium text-gray-900'>
                      Registration {registration.status.toLowerCase()}
                    </p>
                    <p className='text-xs text-gray-500'>
                      {new Date(registration.updatedAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>
              ))}

            {/* Recent payments */}
            {dashboardData.recentPayments.slice(0, 2).map((payment, index) => (
              <div
                key={`pay-${index}`}
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
                    Payment {payment.status.toLowerCase()} - ₱
                    {payment.amount.toLocaleString()}
                  </p>
                  <p className='text-xs text-gray-500'>
                    {new Date(payment.updatedAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
            ))}

            {/* Show message if no activity */}
            {dashboardData.registrations.length === 0 &&
              dashboardData.recentPayments.length === 0 && (
                <div className='text-center py-8 text-gray-500'>
                  <p>No recent activity to display</p>
                  <p className='text-sm mt-1'>
                    Start by registering your household
                  </p>
                </div>
              )}
          </div>
        )}
        <div className='mt-4 text-center'>
          <Link
            href='/activity'
            className='text-sm text-primary-600 hover:text-primary-500 font-medium'
          >
            View all activity
          </Link>
        </div>
      </Card>

      {/* Important Information */}
      <Card className='p-6 bg-blue-50 border-blue-200'>
        <div className='flex'>
          <div className='flex-shrink-0'>
            <svg
              className='h-5 w-5 text-blue-400'
              fill='currentColor'
              viewBox='0 0 20 20'
            >
              <path
                fillRule='evenodd'
                d='M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z'
                clipRule='evenodd'
              />
            </svg>
          </div>
          <div className='ml-3'>
            <h3 className='text-sm font-medium text-blue-800'>
              Keep Your Information Updated
            </h3>
            <div className='mt-2 text-sm text-blue-700'>
              <p>
                Make sure your personal information is always current to ensure
                you receive all eligible benefits and important notifications.
              </p>
            </div>
            <div className='mt-4'>
              <Button variant='outline' size='sm' asChild>
                <Link href='/profile'>Update Profile</Link>
              </Button>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default CitizenDashboard;
