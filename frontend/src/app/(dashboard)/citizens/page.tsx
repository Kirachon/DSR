'use client';

// Citizens Management Page
// Main interface for managing citizen records, registrations, and verification

import { useRouter, useSearchParams } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { CitizenFilters } from '@/components/citizens/citizen-filters';
import { CitizenList } from '@/components/citizens/citizen-list';
import { CreateCitizenModal } from '@/components/citizens/create-citizen-modal';
import { Card, Button, Alert, Modal } from '@/components/ui';
import { useAuth } from '@/contexts';
import { registrationApi, dataManagementApi } from '@/lib/api';
import type { Citizen, CitizenFilters as CitizenFiltersType } from '@/types';

// Citizens page component
export default function CitizensPage() {
  const { user } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();

  // State management
  const [citizens, setCitizens] = useState<Citizen[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<CitizenFiltersType>({});
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedCitizen, setSelectedCitizen] = useState<Citizen | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  // Load citizens data
  useEffect(() => {
    loadCitizens();
  }, [filters]);

  // Load citizens from API
  const loadCitizens = async () => {
    try {
      setLoading(true);
      setError(null);

      // Get citizens from registration service
      const response = await registrationApi.getCitizens({
        ...filters,
        search: searchQuery,
        page: 0,
        size: 50,
      });

      setCitizens(response.content || []);
    } catch (err) {
      console.error('Failed to load citizens:', err);
      setError('Failed to load citizens. Please try again.');

      // Set empty array when API fails - no mock data fallback
      setCitizens([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    // Trigger search after a short delay
    setTimeout(() => loadCitizens(), 300);
  };

  // Handle filter changes
  const handleFilterChange = (newFilters: CitizenFiltersType) => {
    setFilters(newFilters);
  };

  // Handle citizen selection
  const handleCitizenSelect = (citizen: Citizen) => {
    setSelectedCitizen(citizen);
    router.push(`/citizens/${citizen.id}`);
  };

  // Handle citizen creation
  const handleCitizenCreate = async (citizenData: any) => {
    try {
      const newCitizen = await registrationApi.createCitizen(citizenData);
      setCitizens(prev => [newCitizen, ...prev]);
      setIsCreateModalOpen(false);
    } catch (err) {
      console.error('Failed to create citizen:', err);
      setError('Failed to create citizen. Please try again.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Citizens Management</h1>
          <p className="text-gray-600 mt-1">
            Manage citizen records, registrations, and verification status
          </p>
        </div>

        <div className="flex space-x-3">
          <Button variant="outline" onClick={() => loadCitizens()}>
            Refresh
          </Button>
          <Button onClick={() => setIsCreateModalOpen(true)}>
            Add New Citizen
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="error" title="Error">
          {error}
        </Alert>
      )}

      {/* Search and Filters */}
      <Card className="p-6">
        <div className="flex flex-col lg:flex-row gap-4">
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Search Citizens
            </label>
            <input
              type="text"
              placeholder="Search by name, email, or phone number..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
          <div className="lg:w-64">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Status Filter
            </label>
            <select
              value={filters.status || ''}
              onChange={(e) => handleFilterChange({ ...filters, status: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="PENDING">Pending</option>
              <option value="INACTIVE">Inactive</option>
              <option value="SUSPENDED">Suspended</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Citizens List */}
      <Card className="p-6">
        <CitizenList
          citizens={citizens}
          loading={loading}
          onCitizenSelect={handleCitizenSelect}
          onRefresh={loadCitizens}
        />
      </Card>

      {/* Create Citizen Modal */}
      <CreateCitizenModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCitizenCreate}
      />
    </div>
  );
}
