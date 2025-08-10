'use client';

// Case Filters Component
// Advanced filtering interface for case management

import React from 'react';

import { FormInput, FormSelect } from '@/components/forms';
import { Button } from '@/components/ui';
import type { CaseFilters as CaseFiltersType } from '@/types';

// Component props interface
interface CaseFiltersProps {
  filters: CaseFiltersType;
  onFiltersChange: (filters: CaseFiltersType) => void;
  loading?: boolean;
}

// Filter options
const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'NEW', label: 'New' },
  { value: 'ASSIGNED', label: 'Assigned' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'PENDING_REVIEW', label: 'Pending Review' },
  { value: 'PENDING_CITIZEN_RESPONSE', label: 'Pending Citizen Response' },
  { value: 'ESCALATED', label: 'Escalated' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'CLOSED', label: 'Closed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

const TYPE_OPTIONS = [
  { value: '', label: 'All Types' },
  { value: 'GRIEVANCE', label: 'Grievance' },
  { value: 'APPEAL', label: 'Appeal' },
  { value: 'INQUIRY', label: 'Inquiry' },
  { value: 'COMPLAINT', label: 'Complaint' },
  { value: 'FEEDBACK', label: 'Feedback' },
  { value: 'REQUEST', label: 'Request' },
];

const PRIORITY_OPTIONS = [
  { value: '', label: 'All Priorities' },
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'URGENT', label: 'Urgent' },
  { value: 'CRITICAL', label: 'Critical' },
];

const CATEGORY_OPTIONS = [
  { value: '', label: 'All Categories' },
  { value: 'PAYMENT_ISSUES', label: 'Payment Issues' },
  { value: 'ELIGIBILITY_ISSUES', label: 'Eligibility Issues' },
  { value: 'REGISTRATION_ISSUES', label: 'Registration Issues' },
  { value: 'DOCUMENT_ISSUES', label: 'Document Issues' },
  { value: 'SERVICE_QUALITY', label: 'Service Quality' },
  { value: 'SYSTEM_ISSUES', label: 'System Issues' },
  { value: 'POLICY_CLARIFICATION', label: 'Policy Clarification' },
  { value: 'DISCRIMINATION', label: 'Discrimination' },
  { value: 'CORRUPTION', label: 'Corruption' },
  { value: 'OTHER', label: 'Other' },
];

const ASSIGNMENT_OPTIONS = [
  { value: '', label: 'All Assignments' },
  { value: 'me', label: 'Assigned to Me' },
  { value: 'unassigned', label: 'Unassigned' },
  { value: 'team', label: 'My Team' },
];

// Case Filters component
export const CaseFilters: React.FC<CaseFiltersProps> = ({
  filters,
  onFiltersChange,
  loading = false,
}) => {
  // Handle filter changes
  const handleFilterChange = (field: keyof CaseFiltersType, value: any) => {
    onFiltersChange({
      ...filters,
      [field]: value,
    });
  };

  // Handle date range changes
  const handleDateRangeChange = (field: 'start' | 'end', value: string) => {
    onFiltersChange({
      ...filters,
      dateRange: {
        ...filters.dateRange,
        [field]: value,
      },
    });
  };

  // Clear all filters
  const handleClearFilters = () => {
    onFiltersChange({
      status: '',
      type: '',
      priority: '',
      category: '',
      assignedTo: '',
      submittedBy: '',
      dateRange: {
        start: '',
        end: '',
      },
      searchQuery: '',
      tags: [],
      isUrgent: undefined,
    });
  };

  // Check if any filters are active
  const hasActiveFilters = Object.values(filters).some(value => {
    if (typeof value === 'string') return value !== '';
    if (typeof value === 'object' && value !== null) {
      if ('start' in value && 'end' in value) {
        return value.start !== '' || value.end !== '';
      }
      if (Array.isArray(value)) return value.length > 0;
    }
    return false;
  });

  return (
    <div className='space-y-4'>
      <div className='flex justify-between items-center'>
        <h3 className='text-lg font-medium text-gray-900'>Filter Cases</h3>
        {hasActiveFilters && (
          <Button
            variant='outline'
            size='sm'
            onClick={handleClearFilters}
            disabled={loading}
          >
            Clear Filters
          </Button>
        )}
      </div>

      {/* Search */}
      <div className='grid grid-cols-1 gap-4'>
        <FormInput
          label='Search Cases'
          value={filters.searchQuery || ''}
          onChange={value => handleFilterChange('searchQuery', value)}
          placeholder='Search by case number, title, or description...'
          disabled={loading}
        />
      </div>

      {/* Primary Filters */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
        <FormSelect
          label='Status'
          value={filters.status || ''}
          onChange={value => handleFilterChange('status', value)}
          options={STATUS_OPTIONS}
          disabled={loading}
        />

        <FormSelect
          label='Type'
          value={filters.type || ''}
          onChange={value => handleFilterChange('type', value)}
          options={TYPE_OPTIONS}
          disabled={loading}
        />

        <FormSelect
          label='Priority'
          value={filters.priority || ''}
          onChange={value => handleFilterChange('priority', value)}
          options={PRIORITY_OPTIONS}
          disabled={loading}
        />

        <FormSelect
          label='Assignment'
          value={filters.assignedTo || ''}
          onChange={value => handleFilterChange('assignedTo', value)}
          options={ASSIGNMENT_OPTIONS}
          disabled={loading}
        />
      </div>

      {/* Secondary Filters */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
        <FormSelect
          label='Category'
          value={filters.category || ''}
          onChange={value => handleFilterChange('category', value)}
          options={CATEGORY_OPTIONS}
          disabled={loading}
        />

        <FormInput
          label='Date From'
          type='date'
          value={filters.dateRange.start}
          onChange={value =>
            handleDateRangeChange(
              'start',
              typeof value === 'string' ? value : value.target.value
            )
          }
          disabled={loading}
        />

        <FormInput
          label='Date To'
          type='date'
          value={filters.dateRange.end}
          onChange={value =>
            handleDateRangeChange(
              'end',
              typeof value === 'string' ? value : value.target.value
            )
          }
          disabled={loading}
        />
      </div>

      {/* Special Filters */}
      <div className='flex items-center space-x-6'>
        <div className='flex items-center space-x-2'>
          <input
            type='checkbox'
            id='urgentOnly'
            checked={filters.isUrgent || false}
            onChange={e =>
              handleFilterChange(
                'isUrgent',
                e.target.checked ? true : undefined
              )
            }
            disabled={loading}
            className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
          />
          <label
            htmlFor='urgentOnly'
            className='text-sm font-medium text-gray-700'
          >
            Urgent cases only
          </label>
        </div>
      </div>

      {/* Active Filters Summary */}
      {hasActiveFilters && (
        <div className='bg-gray-50 rounded-lg p-4'>
          <h4 className='text-sm font-medium text-gray-900 mb-2'>
            Active Filters:
          </h4>
          <div className='flex flex-wrap gap-2'>
            {filters.status && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800'>
                Status:{' '}
                {
                  STATUS_OPTIONS.find(opt => opt.value === filters.status)
                    ?.label
                }
              </span>
            )}
            {filters.type && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800'>
                Type:{' '}
                {TYPE_OPTIONS.find(opt => opt.value === filters.type)?.label}
              </span>
            )}
            {filters.priority && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800'>
                Priority:{' '}
                {
                  PRIORITY_OPTIONS.find(opt => opt.value === filters.priority)
                    ?.label
                }
              </span>
            )}
            {filters.category && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800'>
                Category:{' '}
                {
                  CATEGORY_OPTIONS.find(opt => opt.value === filters.category)
                    ?.label
                }
              </span>
            )}
            {filters.assignedTo && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800'>
                Assignment:{' '}
                {
                  ASSIGNMENT_OPTIONS.find(
                    opt => opt.value === filters.assignedTo
                  )?.label
                }
              </span>
            )}
            {filters.dateRange.start && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800'>
                From: {filters.dateRange.start}
              </span>
            )}
            {filters.dateRange.end && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800'>
                To: {filters.dateRange.end}
              </span>
            )}
            {filters.isUrgent && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800'>
                Urgent Only
              </span>
            )}
            {filters.searchQuery && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800'>
                Search: "{filters.searchQuery}"
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
