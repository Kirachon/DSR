'use client';

// Case Worker Dashboard Component
// Dashboard interface for case workers to manage grievances and cases

import Link from 'next/link';
import React from 'react';

import { Card, Button, Alert } from '@/components/ui';
import type { User } from '@/types';

// Dashboard props interface
interface CaseWorkerDashboardProps {
  user: User;
}

// Case interface
interface Case {
  id: string;
  caseNumber: string;
  title: string;
  type: 'GRIEVANCE' | 'APPEAL' | 'INQUIRY' | 'COMPLAINT';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status:
    | 'NEW'
    | 'ASSIGNED'
    | 'IN_PROGRESS'
    | 'PENDING_REVIEW'
    | 'RESOLVED'
    | 'CLOSED';
  assignedTo: string;
  submittedBy: string;
  submittedDate: string;
  dueDate: string;
  description: string;
}

// Metric card interface
interface MetricCard {
  title: string;
  value: string | number;
  change: string;
  trend: 'up' | 'down' | 'neutral';
  icon: React.ReactNode;
  color: string;
}

// Icons
const CaseIcon = () => (
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

const ClockIcon = () => (
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
      d='M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z'
    />
  </svg>
);

const CheckIcon = () => (
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
      d='M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
    />
  </svg>
);

const AlertIcon = () => (
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
      d='M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z'
    />
  </svg>
);

// Case worker metrics
const metrics: MetricCard[] = [
  {
    title: 'Active Cases',
    value: '24',
    change: '+3 this week',
    trend: 'up',
    icon: <CaseIcon />,
    color: 'primary',
  },
  {
    title: 'Pending Review',
    value: '7',
    change: '-2 from yesterday',
    trend: 'down',
    icon: <ClockIcon />,
    color: 'warning',
  },
  {
    title: 'Resolved Today',
    value: '5',
    change: '+2 from yesterday',
    trend: 'up',
    icon: <CheckIcon />,
    color: 'success',
  },
  {
    title: 'Urgent Cases',
    value: '3',
    change: 'Requires attention',
    trend: 'neutral',
    icon: <AlertIcon />,
    color: 'error',
  },
];

// Sample cases data
const recentCases: Case[] = [
  {
    id: '1',
    caseNumber: 'GRV-2024-001',
    title: 'Delayed benefit payment',
    type: 'GRIEVANCE',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    assignedTo: 'You',
    submittedBy: 'Maria Santos',
    submittedDate: '2024-01-15',
    dueDate: '2024-01-20',
    description: 'Beneficiary reports delayed 4Ps payment for December 2023',
  },
  {
    id: '2',
    caseNumber: 'APP-2024-002',
    title: 'Eligibility appeal',
    type: 'APPEAL',
    priority: 'MEDIUM',
    status: 'PENDING_REVIEW',
    assignedTo: 'You',
    submittedBy: 'Juan Dela Cruz',
    submittedDate: '2024-01-14',
    dueDate: '2024-01-25',
    description: 'Appeal for 4Ps eligibility rejection',
  },
  {
    id: '3',
    caseNumber: 'INQ-2024-003',
    title: 'Program information request',
    type: 'INQUIRY',
    priority: 'LOW',
    status: 'NEW',
    assignedTo: 'Unassigned',
    submittedBy: 'Ana Garcia',
    submittedDate: '2024-01-16',
    dueDate: '2024-01-23',
    description: 'Request for information about senior citizen benefits',
  },
];

// Get priority color
const getPriorityColor = (priority: string) => {
  switch (priority) {
    case 'URGENT':
      return 'bg-red-100 text-red-800';
    case 'HIGH':
      return 'bg-orange-100 text-orange-800';
    case 'MEDIUM':
      return 'bg-yellow-100 text-yellow-800';
    case 'LOW':
      return 'bg-green-100 text-green-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// Get status color
const getStatusColor = (status: string) => {
  switch (status) {
    case 'NEW':
      return 'bg-blue-100 text-blue-800';
    case 'ASSIGNED':
      return 'bg-purple-100 text-purple-800';
    case 'IN_PROGRESS':
      return 'bg-yellow-100 text-yellow-800';
    case 'PENDING_REVIEW':
      return 'bg-orange-100 text-orange-800';
    case 'RESOLVED':
      return 'bg-green-100 text-green-800';
    case 'CLOSED':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// Case Worker Dashboard component
export const CaseWorkerDashboard: React.FC<CaseWorkerDashboardProps> = ({
  user,
}) => {
  return (
    <div className='space-y-6'>
      {/* Urgent Cases Alert */}
      <Alert variant='warning' title='Urgent Cases Require Attention'>
        You have 3 urgent cases that need immediate attention.
        <Link
          href='/cases?priority=urgent'
          className='font-medium underline ml-1'
        >
          View urgent cases
        </Link>
      </Alert>

      {/* Metrics Overview */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
        {metrics.map((metric, index) => (
          <Card key={index} className='p-6'>
            <div className='flex items-center'>
              <div
                className={`flex-shrink-0 p-3 rounded-lg ${
                  metric.color === 'primary'
                    ? 'bg-primary-100 text-primary-600'
                    : metric.color === 'success'
                      ? 'bg-success-100 text-success-600'
                      : metric.color === 'warning'
                        ? 'bg-warning-100 text-warning-600'
                        : metric.color === 'error'
                          ? 'bg-error-100 text-error-600'
                          : 'bg-gray-100 text-gray-600'
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
                  className={`text-sm ${
                    metric.trend === 'up'
                      ? 'text-success-600'
                      : metric.trend === 'down'
                        ? 'text-error-600'
                        : 'text-gray-600'
                  }`}
                >
                  {metric.change}
                </p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        {/* Quick Actions */}
        <Card className='p-6'>
          <h2 className='text-lg font-semibold text-gray-900 mb-4'>
            Quick Actions
          </h2>
          <div className='space-y-3'>
            <Link href='/cases/new'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-primary-100 text-primary-600 rounded-lg'>
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
                      d='M12 6v6m0 0v6m0-6h6m-6 0H6'
                    />
                  </svg>
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>Create New Case</p>
                  <p className='text-sm text-gray-600'>
                    Start a new grievance or inquiry case
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/cases?status=assigned'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-warning-100 text-warning-600 rounded-lg'>
                  <CaseIcon />
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>My Assigned Cases</p>
                  <p className='text-sm text-gray-600'>
                    View cases assigned to you
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/cases/search'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-accent-100 text-accent-600 rounded-lg'>
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
                      d='M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z'
                    />
                  </svg>
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>Search Cases</p>
                  <p className='text-sm text-gray-600'>
                    Find cases by number or citizen
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/reports/cases'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-success-100 text-success-600 rounded-lg'>
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
                      d='M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'
                    />
                  </svg>
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>Case Reports</p>
                  <p className='text-sm text-gray-600'>
                    Generate case statistics and reports
                  </p>
                </div>
              </div>
            </Link>
          </div>
        </Card>

        {/* Recent Cases */}
        <Card className='p-6'>
          <h2 className='text-lg font-semibold text-gray-900 mb-4'>
            Recent Cases
          </h2>
          <div className='space-y-4'>
            {recentCases.map(caseItem => (
              <div
                key={caseItem.id}
                className='border border-gray-200 rounded-lg p-4'
              >
                <div className='flex items-start justify-between mb-2'>
                  <div className='flex-1'>
                    <div className='flex items-center space-x-2 mb-1'>
                      <h3 className='font-medium text-gray-900 text-sm'>
                        {caseItem.caseNumber}
                      </h3>
                      <span
                        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getPriorityColor(caseItem.priority)}`}
                      >
                        {caseItem.priority}
                      </span>
                      <span
                        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(caseItem.status)}`}
                      >
                        {caseItem.status.replace('_', ' ')}
                      </span>
                    </div>
                    <p className='text-sm font-medium text-gray-900 mb-1'>
                      {caseItem.title}
                    </p>
                    <p className='text-xs text-gray-600 mb-2'>
                      {caseItem.description}
                    </p>
                    <div className='flex items-center space-x-4 text-xs text-gray-500'>
                      <span>By: {caseItem.submittedBy}</span>
                      <span>Due: {caseItem.dueDate}</span>
                      <span>Type: {caseItem.type}</span>
                    </div>
                  </div>
                </div>
                <div className='flex justify-end'>
                  <Button size='sm' variant='outline' asChild>
                    <Link href={`/cases/${caseItem.id}`}>View Details</Link>
                  </Button>
                </div>
              </div>
            ))}
          </div>
          <div className='mt-4 text-center'>
            <Link
              href='/cases'
              className='text-sm text-primary-600 hover:text-primary-500 font-medium'
            >
              View all cases
            </Link>
          </div>
        </Card>
      </div>

      {/* Case Activity Timeline */}
      <Card className='p-6'>
        <h2 className='text-lg font-semibold text-gray-900 mb-4'>
          Recent Activity
        </h2>
        <div className='space-y-4'>
          <div className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'>
            <div className='flex-shrink-0 w-2 h-2 bg-success-500 rounded-full'></div>
            <div className='flex-1'>
              <p className='text-sm font-medium text-gray-900'>
                Resolved case GRV-2024-005
              </p>
              <p className='text-xs text-gray-500'>
                Benefit payment issue resolved - 1 hour ago
              </p>
            </div>
          </div>
          <div className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'>
            <div className='flex-shrink-0 w-2 h-2 bg-primary-500 rounded-full'></div>
            <div className='flex-1'>
              <p className='text-sm font-medium text-gray-900'>
                Updated case APP-2024-002
              </p>
              <p className='text-xs text-gray-500'>
                Added review notes and requested additional documents - 3 hours
                ago
              </p>
            </div>
          </div>
          <div className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'>
            <div className='flex-shrink-0 w-2 h-2 bg-warning-500 rounded-full'></div>
            <div className='flex-1'>
              <p className='text-sm font-medium text-gray-900'>
                Assigned new case INQ-2024-003
              </p>
              <p className='text-xs text-gray-500'>
                Senior citizen benefits inquiry assigned - 5 hours ago
              </p>
            </div>
          </div>
          <div className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'>
            <div className='flex-shrink-0 w-2 h-2 bg-accent-500 rounded-full'></div>
            <div className='flex-1'>
              <p className='text-sm font-medium text-gray-900'>
                Generated weekly case report
              </p>
              <p className='text-xs text-gray-500'>
                Case resolution statistics for week ending Jan 14 - 1 day ago
              </p>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default CaseWorkerDashboard;
