'use client';

// Household Filters Component
// Advanced filtering options for household search

import React from 'react';

import { FormSelect } from '@/components/forms';
import type { HouseholdFilters as HouseholdFiltersType } from '@/types';

// Component props interface
interface HouseholdFiltersProps {
  filters: HouseholdFiltersType;
  onFilterChange: (filters: Partial<HouseholdFiltersType>) => void;
  loading?: boolean;
}

// Philippine regions data
const REGIONS = [
  { value: '', label: 'All Regions' },
  { value: 'I', label: 'Region I - Ilocos Region' },
  { value: 'II', label: 'Region II - Cagayan Valley' },
  { value: 'III', label: 'Region III - Central Luzon' },
  { value: 'IV-A', label: 'Region IV-A - CALABARZON' },
  { value: 'IV-B', label: 'Region IV-B - MIMAROPA' },
  { value: 'V', label: 'Region V - Bicol Region' },
  { value: 'VI', label: 'Region VI - Western Visayas' },
  { value: 'VII', label: 'Region VII - Central Visayas' },
  { value: 'VIII', label: 'Region VIII - Eastern Visayas' },
  { value: 'IX', label: 'Region IX - Zamboanga Peninsula' },
  { value: 'X', label: 'Region X - Northern Mindanao' },
  { value: 'XI', label: 'Region XI - Davao Region' },
  { value: 'XII', label: 'Region XII - SOCCSKSARGEN' },
  { value: 'XIII', label: 'Region XIII - Caraga' },
  { value: 'NCR', label: 'National Capital Region' },
  { value: 'CAR', label: 'Cordillera Administrative Region' },
  { value: 'BARMM', label: 'Bangsamoro Autonomous Region' },
];

// Status options
const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'active', label: 'Active' },
  { value: 'inactive', label: 'Inactive' },
  { value: 'suspended', label: 'Suspended' },
  { value: 'pending', label: 'Pending' },
];

// Validation status options
const VALIDATION_STATUS_OPTIONS = [
  { value: '', label: 'All Validation Statuses' },
  { value: 'validated', label: 'Validated' },
  { value: 'pending', label: 'Pending Validation' },
  { value: 'rejected', label: 'Rejected' },
  { value: 'requires_review', label: 'Requires Review' },
];

// Sample provinces for selected regions (in a real app, this would be dynamic)
const PROVINCES_BY_REGION: Record<string, Array<{ value: string; label: string }>> = {
  'II': [
    { value: '', label: 'All Provinces' },
    { value: 'Batanes', label: 'Batanes' },
    { value: 'Cagayan', label: 'Cagayan' },
    { value: 'Isabela', label: 'Isabela' },
    { value: 'Nueva Vizcaya', label: 'Nueva Vizcaya' },
    { value: 'Quirino', label: 'Quirino' },
  ],
  'NCR': [
    { value: '', label: 'All Cities/Municipalities' },
    { value: 'Manila', label: 'Manila' },
    { value: 'Quezon City', label: 'Quezon City' },
    { value: 'Makati', label: 'Makati' },
    { value: 'Pasig', label: 'Pasig' },
    { value: 'Taguig', label: 'Taguig' },
  ],
  // Add more regions as needed
};

// Sample municipalities for selected provinces
const MUNICIPALITIES_BY_PROVINCE: Record<string, Array<{ value: string; label: string }>> = {
  'Cagayan': [
    { value: '', label: 'All Municipalities' },
    { value: 'Tuguegarao City', label: 'Tuguegarao City' },
    { value: 'Aparri', label: 'Aparri' },
    { value: 'Baggao', label: 'Baggao' },
    { value: 'Ballesteros', label: 'Ballesteros' },
    { value: 'Buguey', label: 'Buguey' },
  ],
  // Add more provinces as needed
};

// Household Filters component
export const HouseholdFilters: React.FC<HouseholdFiltersProps> = ({
  filters,
  onFilterChange,
  loading = false,
}) => {
  // Get provinces for selected region
  const getProvinceOptions = () => {
    if (!filters.region) {
      return [{ value: '', label: 'Select Region First' }];
    }
    return PROVINCES_BY_REGION[filters.region] || [{ value: '', label: 'All Provinces' }];
  };

  // Get municipalities for selected province
  const getMunicipalityOptions = () => {
    if (!filters.province) {
      return [{ value: '', label: 'Select Province First' }];
    }
    return MUNICIPALITIES_BY_PROVINCE[filters.province] || [{ value: '', label: 'All Municipalities' }];
  };

  // Handle region change
  const handleRegionChange = (region: string) => {
    onFilterChange({
      region,
      province: '', // Reset province when region changes
      municipality: '', // Reset municipality when region changes
    });
  };

  // Handle province change
  const handleProvinceChange = (province: string) => {
    onFilterChange({
      province,
      municipality: '', // Reset municipality when province changes
    });
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
      {/* Region Filter */}
      <div>
        <FormSelect
          label="Region"
          value={filters.region}
          onChange={handleRegionChange}
          options={REGIONS}
          disabled={loading}
        />
      </div>

      {/* Province Filter */}
      <div>
        <FormSelect
          label="Province"
          value={filters.province}
          onChange={handleProvinceChange}
          options={getProvinceOptions()}
          disabled={loading || !filters.region}
        />
      </div>

      {/* Municipality Filter */}
      <div>
        <FormSelect
          label="Municipality"
          value={filters.municipality}
          onChange={(municipality) => onFilterChange({ municipality })}
          options={getMunicipalityOptions()}
          disabled={loading || !filters.province}
        />
      </div>

      {/* Status Filter */}
      <div>
        <FormSelect
          label="Status"
          value={filters.status}
          onChange={(status) => onFilterChange({ status })}
          options={STATUS_OPTIONS}
          disabled={loading}
        />
      </div>

      {/* Validation Status Filter */}
      <div>
        <FormSelect
          label="Validation Status"
          value={filters.validationStatus}
          onChange={(validationStatus) => onFilterChange({ validationStatus })}
          options={VALIDATION_STATUS_OPTIONS}
          disabled={loading}
        />
      </div>
    </div>
  );
};
