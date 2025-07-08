'use client';

// Payment Filters Component
// Advanced filtering interface for payment management

import React from 'react';

import { FormInput, FormSelect } from '@/components/forms';
import { Button } from '@/components/ui';
import type { PaymentFilters as PaymentFiltersType } from '@/types';

// Component props interface
interface PaymentFiltersProps {
  filters: PaymentFiltersType;
  onFiltersChange: (filters: PaymentFiltersType) => void;
  loading?: boolean;
}

// Filter options
const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'PROCESSING', label: 'Processing' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'FAILED', label: 'Failed' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'REFUNDED', label: 'Refunded' },
  { value: 'ON_HOLD', label: 'On Hold' },
];

const PAYMENT_METHOD_OPTIONS = [
  { value: '', label: 'All Payment Methods' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
  { value: 'DIGITAL_WALLET', label: 'Digital Wallet' },
  { value: 'CASH_PICKUP', label: 'Cash Pickup' },
  { value: 'CHECK', label: 'Check' },
  { value: 'PREPAID_CARD', label: 'Prepaid Card' },
];

const PROGRAM_OPTIONS = [
  { value: '', label: 'All Programs' },
  { value: '4Ps', label: '4Ps (Pantawid Pamilyang Pilipino Program)' },
  { value: 'SENIOR_CITIZEN', label: 'Senior Citizen Pension' },
  { value: 'PWD_ALLOWANCE', label: 'PWD Allowance' },
  { value: 'EMERGENCY_SUBSIDY', label: 'Emergency Subsidy' },
  { value: 'LIVELIHOOD_ASSISTANCE', label: 'Livelihood Assistance' },
  { value: 'EDUCATIONAL_ASSISTANCE', label: 'Educational Assistance' },
  { value: 'MEDICAL_ASSISTANCE', label: 'Medical Assistance' },
  { value: 'OTHER', label: 'Other' },
];

const FSP_PROVIDER_OPTIONS = [
  { value: '', label: 'All FSP Providers' },
  { value: 'BDO', label: 'BDO' },
  { value: 'BPI', label: 'BPI' },
  { value: 'METROBANK', label: 'Metrobank' },
  { value: 'GCASH', label: 'GCash' },
  { value: 'PAYMAYA', label: 'PayMaya' },
  { value: 'MLHUILLIER', label: 'M Lhuillier' },
  { value: 'CEBUANA', label: 'Cebuana Lhuillier' },
  { value: 'PALAWAN', label: 'Palawan Pawnshop' },
];

// Payment Filters component
export const PaymentFilters: React.FC<PaymentFiltersProps> = ({
  filters,
  onFiltersChange,
  loading = false,
}) => {
  // Handle filter changes
  const handleFilterChange = (field: keyof PaymentFiltersType, value: any) => {
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

  // Handle amount range changes
  const handleAmountRangeChange = (field: 'min' | 'max', value: string) => {
    onFiltersChange({
      ...filters,
      amountRange: {
        ...filters.amountRange,
        [field]: parseFloat(value) || 0,
      },
    });
  };

  // Clear all filters
  const handleClearFilters = () => {
    onFiltersChange({
      status: '',
      paymentMethod: '',
      program: '',
      batchId: '',
      beneficiaryId: '',
      fspProvider: '',
      dateRange: {
        start: '',
        end: '',
      },
      amountRange: {
        min: 0,
        max: 0,
      },
      searchQuery: '',
      isVerified: undefined,
      isReconciled: undefined,
      hasFailures: undefined,
    });
  };

  // Check if any filters are active
  const hasActiveFilters = Object.values(filters).some(value => {
    if (typeof value === 'string') return value !== '';
    if (typeof value === 'object' && value !== null) {
      if ('start' in value && 'end' in value) {
        return value.start !== '' || value.end !== '';
      }
      if ('min' in value && 'max' in value) {
        return value.min > 0 || value.max > 0;
      }
    }
    return false;
  });

  return (
    <div className='space-y-4'>
      <div className='flex justify-between items-center'>
        <h3 className='text-lg font-medium text-gray-900'>Filter Payments</h3>
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
          label='Search Payments'
          value={filters.searchQuery || ''}
          onChange={value => handleFilterChange('searchQuery', value)}
          placeholder='Search by payment ID, beneficiary name, or reference...'
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
          label='Payment Method'
          value={filters.paymentMethod || ''}
          onChange={value => handleFilterChange('paymentMethod', value)}
          options={PAYMENT_METHOD_OPTIONS}
          disabled={loading}
        />

        <FormSelect
          label='Program'
          value={filters.program || ''}
          onChange={value => handleFilterChange('program', value)}
          options={PROGRAM_OPTIONS}
          disabled={loading}
        />

        <FormSelect
          label='FSP Provider'
          value={filters.fspProvider || ''}
          onChange={value => handleFilterChange('fspProvider', value)}
          options={FSP_PROVIDER_OPTIONS}
          disabled={loading}
        />
      </div>

      {/* Secondary Filters */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
        <FormInput
          label='Batch ID'
          value={filters.batchId || ''}
          onChange={value => handleFilterChange('batchId', value)}
          placeholder='Enter batch ID'
          disabled={loading}
        />

        <FormInput
          label='Beneficiary ID'
          value={filters.beneficiaryId || ''}
          onChange={value => handleFilterChange('beneficiaryId', value)}
          placeholder='Enter beneficiary ID'
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

      {/* Amount Range */}
      <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
        <FormInput
          label='Minimum Amount (PHP)'
          type='number'
          value={
            filters.amountRange.min > 0
              ? filters.amountRange.min.toString()
              : ''
          }
          onChange={value =>
            handleAmountRangeChange(
              'min',
              typeof value === 'string' ? value : value.target.value
            )
          }
          placeholder='0.00'
          min='0'
          step='0.01'
          disabled={loading}
        />

        <FormInput
          label='Maximum Amount (PHP)'
          type='number'
          value={
            filters.amountRange.max > 0
              ? filters.amountRange.max.toString()
              : ''
          }
          onChange={value =>
            handleAmountRangeChange(
              'max',
              typeof value === 'string' ? value : value.target.value
            )
          }
          placeholder='0.00'
          min='0'
          step='0.01'
          disabled={loading}
        />
      </div>

      {/* Special Filters */}
      <div className='flex items-center space-x-6'>
        <div className='flex items-center space-x-2'>
          <input
            type='checkbox'
            id='isVerified'
            checked={filters.isVerified || false}
            onChange={e =>
              handleFilterChange(
                'isVerified',
                e.target.checked ? true : undefined
              )
            }
            disabled={loading}
            className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
          />
          <label
            htmlFor='isVerified'
            className='text-sm font-medium text-gray-700'
          >
            Verified payments only
          </label>
        </div>

        <div className='flex items-center space-x-2'>
          <input
            type='checkbox'
            id='isReconciled'
            checked={filters.isReconciled || false}
            onChange={e =>
              handleFilterChange(
                'isReconciled',
                e.target.checked ? true : undefined
              )
            }
            disabled={loading}
            className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
          />
          <label
            htmlFor='isReconciled'
            className='text-sm font-medium text-gray-700'
          >
            Reconciled payments only
          </label>
        </div>

        <div className='flex items-center space-x-2'>
          <input
            type='checkbox'
            id='hasFailures'
            checked={filters.hasFailures || false}
            onChange={e =>
              handleFilterChange(
                'hasFailures',
                e.target.checked ? true : undefined
              )
            }
            disabled={loading}
            className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
          />
          <label
            htmlFor='hasFailures'
            className='text-sm font-medium text-gray-700'
          >
            Failed payments only
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
            {filters.paymentMethod && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800'>
                Method:{' '}
                {
                  PAYMENT_METHOD_OPTIONS.find(
                    opt => opt.value === filters.paymentMethod
                  )?.label
                }
              </span>
            )}
            {filters.program && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800'>
                Program:{' '}
                {
                  PROGRAM_OPTIONS.find(opt => opt.value === filters.program)
                    ?.label
                }
              </span>
            )}
            {filters.fspProvider && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800'>
                FSP:{' '}
                {
                  FSP_PROVIDER_OPTIONS.find(
                    opt => opt.value === filters.fspProvider
                  )?.label
                }
              </span>
            )}
            {filters.batchId && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800'>
                Batch: {filters.batchId}
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
            {filters.amountRange.min > 0 && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800'>
                Min: ₱{filters.amountRange.min.toLocaleString()}
              </span>
            )}
            {filters.amountRange.max > 0 && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800'>
                Max: ₱{filters.amountRange.max.toLocaleString()}
              </span>
            )}
            {filters.searchQuery && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-pink-100 text-pink-800'>
                Search: "{filters.searchQuery}"
              </span>
            )}
            {filters.isVerified && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800'>
                Verified Only
              </span>
            )}
            {filters.isReconciled && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800'>
                Reconciled Only
              </span>
            )}
            {filters.hasFailures && (
              <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800'>
                Failed Only
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
