'use client';

// Registration Filters Component
// Provides filtering options for registration applications

import React from 'react';

import { Button } from '@/components/ui';
import type { RegistrationFilters } from '@/types';

// Registration filters props interface
interface RegistrationFiltersProps {
  filters: RegistrationFilters;
  onFiltersChange: (filters: RegistrationFilters) => void;
  onReset: () => void;
}

// Registration Filters component
export const RegistrationFilters: React.FC<RegistrationFiltersProps> = ({
  filters,
  onFiltersChange,
  onReset,
}) => {
  // Handle filter change
  const handleFilterChange = (key: keyof RegistrationFilters, value: any) => {
    onFiltersChange({
      ...filters,
      [key]: value,
    });
  };

  // Check if any filters are active
  const hasActiveFilters = Object.values(filters).some(value => 
    value !== '' && value !== null && value !== undefined
  );

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Status Filter */}
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">Status</label>
          <select
            value={filters.status || ''}
            onChange={(e) => handleFilterChange('status', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending Review</option>
            <option value="UNDER_REVIEW">Under Review</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="REQUIRES_DOCUMENTS">Requires Documents</option>
          </select>
        </div>

        {/* Date Range */}
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">Submission Date From</label>
          <input
            type="date"
            value={filters.submissionDateStart || ''}
            onChange={(e) => handleFilterChange('submissionDateStart', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          />
        </div>

        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">Submission Date To</label>
          <input
            type="date"
            value={filters.submissionDateEnd || ''}
            onChange={(e) => handleFilterChange('submissionDateEnd', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          />
        </div>
      </div>

      {/* Filter Actions */}
      <div className="flex justify-between items-center pt-4 border-t border-gray-200">
        <div className="text-sm text-gray-600">
          {hasActiveFilters ? 'Filters applied' : 'No filters applied'}
        </div>
        <div className="flex space-x-2">
          {hasActiveFilters && (
            <Button variant="outline" size="sm" onClick={onReset}>
              Clear Filters
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};
