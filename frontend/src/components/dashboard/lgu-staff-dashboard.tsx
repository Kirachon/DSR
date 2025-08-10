'use client';

// LGU Staff Dashboard Component
// Dashboard interface for Local Government Unit staff to manage citizens and applications

import Link from 'next/link';
import React, { useState, useEffect } from 'react';

import { Card, Button, Alert } from '@/components/ui';
import type { User } from '@/types';

// Task Queue Filter Component
const TaskQueueFilter: React.FC<{
  onFilterChange: (filters: any) => void;
}> = ({ onFilterChange }) => {
  const [filters, setFilters] = useState({
    priority: 'all',
    status: 'all',
    dueDate: 'all',
  });

  const handleFilterChange = (key: string, value: string) => {
    const newFilters = { ...filters, [key]: value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  return (
    <div className="bg-gray-50 p-4 rounded-lg mb-6">
      <h3 className="text-sm font-medium text-gray-700 mb-3">Filter Tasks</h3>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Priority</label>
          <select
            value={filters.priority}
            onChange={(e) => handleFilterChange('priority', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-primary-500 focus:border-primary-500"
          >
            <option value="all">All Priorities</option>
            <option value="high">High Priority</option>
            <option value="medium">Medium Priority</option>
            <option value="low">Low Priority</option>
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Status</label>
          <select
            value={filters.status}
            onChange={(e) => handleFilterChange('status', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-primary-500 focus:border-primary-500"
          >
            <option value="all">All Status</option>
            <option value="pending">Pending</option>
            <option value="in-progress">In Progress</option>
            <option value="completed">Completed</option>
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Due Date</label>
          <select
            value={filters.dueDate}
            onChange={(e) => handleFilterChange('dueDate', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-primary-500 focus:border-primary-500"
          >
            <option value="all">All Dates</option>
            <option value="overdue">Overdue</option>
            <option value="today">Due Today</option>
            <option value="week">Due This Week</option>
          </select>
        </div>
      </div>
    </div>
  );
};

// Dashboard props interface
interface LGUStaffDashboardProps {
  user: User;
}

// Metric card interface
interface MetricCard {
  title: string;
  value: string | number;
  change: string;
  trend: 'up' | 'down' | 'neutral';
  icon: React.ReactNode;
}

// Task interface
interface Task {
  id: string;
  title: string;
  description: string;
  priority: 'high' | 'medium' | 'low';
  dueDate: string;
  status: 'pending' | 'in-progress' | 'completed';
}

// Icons
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

// Metrics for LGU staff
const metrics: MetricCard[] = [
  {
    title: 'Total Citizens',
    value: '1,247',
    change: '+12 this week',
    trend: 'up',
    icon: <UsersIcon />,
  },
  {
    title: 'Pending Applications',
    value: '23',
    change: '-5 from yesterday',
    trend: 'down',
    icon: <DocumentIcon />,
  },
  {
    title: 'Approved Today',
    value: '8',
    change: '+3 from yesterday',
    trend: 'up',
    icon: <CheckIcon />,
  },
  {
    title: 'Avg. Processing Time',
    value: '2.3 days',
    change: '-0.5 days',
    trend: 'down',
    icon: <ClockIcon />,
  },
];

// Sample tasks for LGU staff
const tasks: Task[] = [
  {
    id: '1',
    title: 'Review benefit applications',
    description: '15 new applications require review',
    priority: 'high',
    dueDate: 'Today',
    status: 'pending',
  },
  {
    id: '2',
    title: 'Update citizen profiles',
    description: '8 profiles need verification',
    priority: 'medium',
    dueDate: 'Tomorrow',
    status: 'in-progress',
  },
  {
    id: '3',
    title: 'Generate monthly report',
    description: 'Prepare citizen statistics report',
    priority: 'low',
    dueDate: 'This week',
    status: 'pending',
  },
];

// LGU Staff Dashboard component
export const LGUStaffDashboard: React.FC<LGUStaffDashboardProps> = ({
  user,
}) => {
  const [filteredTasks, setFilteredTasks] = useState(tasks);

  const handleFilterChange = (filters: any) => {
    let filtered = [...tasks];

    if (filters.priority !== 'all') {
      filtered = filtered.filter(task => task.priority === filters.priority);
    }
    if (filters.status !== 'all') {
      filtered = filtered.filter(task => task.status === filters.status);
    }

    setFilteredTasks(filtered);
  };

  return (
    <div className='space-y-6'>
      {/* Task Queue Filter */}
      <TaskQueueFilter onFilterChange={handleFilterChange} />

      {/* Metrics Overview */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
        {metrics.map((metric, index) => (
          <Card key={index} className='p-6'>
            <div className='flex items-center'>
              <div className='flex-shrink-0 p-3 bg-primary-100 text-primary-600 rounded-lg'>
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
                  {metric.trend === 'up' && (
                    <svg
                      className='h-4 w-4 mr-1'
                      fill='none'
                      stroke='currentColor'
                      viewBox='0 0 24 24'
                    >
                      <path
                        strokeLinecap='round'
                        strokeLinejoin='round'
                        strokeWidth={2}
                        d='M7 17l9.2-9.2M17 17V7H7'
                      />
                    </svg>
                  )}
                  {metric.trend === 'down' && (
                    <svg
                      className='h-4 w-4 mr-1'
                      fill='none'
                      stroke='currentColor'
                      viewBox='0 0 24 24'
                    >
                      <path
                        strokeLinecap='round'
                        strokeLinejoin='round'
                        strokeWidth={2}
                        d='M17 7l-9.2 9.2M7 7v10h10'
                      />
                    </svg>
                  )}
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
            <Link href='/citizens/registrations'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-primary-100 text-primary-600 rounded-lg'>
                  <UsersIcon />
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>
                    Review Registrations
                  </p>
                  <p className='text-sm text-gray-600'>
                    Process new citizen registrations
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/applications'>
              <div className='flex items-center p-3 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 cursor-pointer'>
                <div className='flex-shrink-0 p-2 bg-secondary-100 text-secondary-600 rounded-lg'>
                  <DocumentIcon />
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>
                    Manage Applications
                  </p>
                  <p className='text-sm text-gray-600'>
                    Review benefit applications
                  </p>
                </div>
              </div>
            </Link>

            <Link href='/reports'>
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
                      d='M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'
                    />
                  </svg>
                </div>
                <div className='ml-3'>
                  <p className='font-medium text-gray-900'>Generate Reports</p>
                  <p className='text-sm text-gray-600'>
                    Create statistical reports
                  </p>
                </div>
              </div>
            </Link>
          </div>
        </Card>

        {/* Pending Tasks */}
        <Card className='p-6'>
          <h2 className='text-lg font-semibold text-gray-900 mb-4'>
            Pending Tasks
          </h2>
          <div className='space-y-3'>
            {tasks.map(task => (
              <div
                key={task.id}
                className='p-3 border border-gray-200 rounded-lg'
              >
                <div className='flex items-center justify-between mb-2'>
                  <h3 className='font-medium text-gray-900'>{task.title}</h3>
                  <span
                    className={`px-2 py-1 text-xs font-medium rounded-full ${
                      task.priority === 'high'
                        ? 'bg-error-100 text-error-800'
                        : task.priority === 'medium'
                          ? 'bg-warning-100 text-warning-800'
                          : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {task.priority}
                  </span>
                </div>
                <p className='text-sm text-gray-600 mb-2'>{task.description}</p>
                <div className='flex items-center justify-between'>
                  <span className='text-xs text-gray-500'>
                    Due: {task.dueDate}
                  </span>
                  <Button size='sm' variant='outline'>
                    View
                  </Button>
                </div>
              </div>
            ))}
          </div>
          <div className='mt-4 text-center'>
            <Link
              href='/tasks'
              className='text-sm text-primary-600 hover:text-primary-500 font-medium'
            >
              View all tasks
            </Link>
          </div>
        </Card>
      </div>

      {/* Recent Activity */}
      <Card className='p-6'>
        <h2 className='text-lg font-semibold text-gray-900 mb-4'>
          Recent Activity
        </h2>
        <div className='space-y-4'>
          <div className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'>
            <div className='flex-shrink-0 w-2 h-2 bg-success-500 rounded-full'></div>
            <div className='flex-1'>
              <p className='text-sm font-medium text-gray-900'>
                Approved 3 benefit applications
              </p>
              <p className='text-xs text-gray-500'>30 minutes ago</p>
            </div>
          </div>
          <div className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'>
            <div className='flex-shrink-0 w-2 h-2 bg-primary-500 rounded-full'></div>
            <div className='flex-1'>
              <p className='text-sm font-medium text-gray-900'>
                Verified 5 citizen profiles
              </p>
              <p className='text-xs text-gray-500'>2 hours ago</p>
            </div>
          </div>
          <div className='flex items-center space-x-3 p-3 bg-gray-50 rounded-lg'>
            <div className='flex-shrink-0 w-2 h-2 bg-warning-500 rounded-full'></div>
            <div className='flex-1'>
              <p className='text-sm font-medium text-gray-900'>
                Generated monthly statistics report
              </p>
              <p className='text-xs text-gray-500'>1 day ago</p>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default LGUStaffDashboard;
