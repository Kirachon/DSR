'use client';

// Citizen Filters Component
// Provides filtering options for the citizen list

import React from 'react';

// Removed FormSelect, FormInput imports to avoid useFormContext issues
import { Button } from '@/components/ui';
import type { CitizenFilters } from '@/types';

// Citizen filters props interface
interface CitizenFiltersProps {
  filters: CitizenFilters;
  onFiltersChange: (filters: CitizenFilters) => void;
  onReset: () => void;
}

// Filter options
const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'SUSPENDED', label: 'Suspended' },
];

const VERIFICATION_OPTIONS = [
  { value: '', label: 'All Verification Statuses' },
  { value: 'VERIFIED', label: 'Verified' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'REJECTED', label: 'Rejected' },
];

const REGION_OPTIONS = [
  { value: '', label: 'All Regions' },
  { value: 'NCR', label: 'National Capital Region' },
  { value: 'CAR', label: 'Cordillera Administrative Region' },
  { value: 'REGION_1', label: 'Region I - Ilocos Region' },
  { value: 'REGION_2', label: 'Region II - Cagayan Valley' },
  { value: 'REGION_3', label: 'Region III - Central Luzon' },
  { value: 'REGION_4A', label: 'Region IV-A - CALABARZON' },
  { value: 'REGION_4B', label: 'Region IV-B - MIMAROPA' },
  { value: 'REGION_5', label: 'Region V - Bicol Region' },
  { value: 'REGION_6', label: 'Region VI - Western Visayas' },
  { value: 'REGION_7', label: 'Region VII - Central Visayas' },
  { value: 'REGION_8', label: 'Region VIII - Eastern Visayas' },
  { value: 'REGION_9', label: 'Region IX - Zamboanga Peninsula' },
  { value: 'REGION_10', label: 'Region X - Northern Mindanao' },
  { value: 'REGION_11', label: 'Region XI - Davao Region' },
  { value: 'REGION_12', label: 'Region XII - SOCCSKSARGEN' },
  { value: 'REGION_13', label: 'Region XIII - Caraga' },
  { value: 'BARMM', label: 'Bangsamoro Autonomous Region' },
];

// Citizen Filters component
export const CitizenFilters: React.FC<CitizenFiltersProps> = ({
  filters,
  onFiltersChange,
  onReset,
}) => {
  // Handle filter change
  const handleFilterChange = (key: keyof CitizenFilters, value: any) => {
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
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Status Filter */}
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">Status</label>
          <select
            value={filters.status || ''}
            onChange={(e) => handleFilterChange('status', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          >
            {STATUS_OPTIONS.map(option => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>

        {/* Verification Status Filter */}
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">Verification Status</label>
          <select
            value={filters.verificationStatus || ''}
            onChange={(e) => handleFilterChange('verificationStatus', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          >
            {VERIFICATION_OPTIONS.map(option => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>

        {/* Region Filter */}
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">Region</label>
          <select
            value={filters.region || ''}
            onChange={(e) => handleFilterChange('region', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          >
            {REGION_OPTIONS.map(option => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>

        {/* Age Range */}
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">Age Range</label>
          <div className="flex space-x-2">
            <input
              type="number"
              placeholder="Min"
              value={filters.ageMin || ''}
              onChange={(e) => handleFilterChange('ageMin', e.target.value ? parseInt(e.target.value) : undefined)}
              className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
            <input
              type="number"
              placeholder="Max"
              value={filters.ageMax || ''}
              onChange={(e) => handleFilterChange('ageMax', e.target.value ? parseInt(e.target.value) : undefined)}
              className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
        </div>
      </div>

      {/* Date Range Filter */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">
            Registration Date Range
          </label>
          <div className="flex space-x-2">
            <input
              type="date"
              placeholder="Start Date"
              value={filters.registrationDateStart || ''}
              onChange={(e) => handleFilterChange('registrationDateStart', e.target.value)}
              className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
            <input
              type="date"
              placeholder="End Date"
              value={filters.registrationDateEnd || ''}
              onChange={(e) => handleFilterChange('registrationDateEnd', e.target.value)}
              className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
        </div>

        {/* Household Size Filter */}
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">
            Household Size Range
          </label>
          <div className="flex space-x-2">
            <input
              type="number"
              placeholder="Min"
              value={filters.householdSizeMin || ''}
              onChange={(e) => handleFilterChange('householdSizeMin', e.target.value ? parseInt(e.target.value) : undefined)}
              className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
            <input
              type="number"
              placeholder="Max"
              value={filters.householdSizeMax || ''}
              onChange={(e) => handleFilterChange('householdSizeMax', e.target.value ? parseInt(e.target.value) : undefined)}
              className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
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
