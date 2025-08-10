'use client';

// Household Management Page
// Interface for staff to manage household data, search, and view household information

import { useRouter, useSearchParams } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { FormInput, FormSelect } from '@/components/forms';
import { HouseholdList } from '@/components/households/household-list';
import { HouseholdFilters } from '@/components/households/household-filters';
import { CreateHouseholdModal } from '@/components/households/create-household-modal';
import { Card, Button, Alert, Modal } from '@/components/ui';
import { useAuth } from '@/contexts';
import { dataManagementApi } from '@/lib/api';
import type { HouseholdData, HouseholdFilters as HouseholdFiltersType } from '@/types';

// Page component
export default function HouseholdsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, isAuthenticated } = useAuth();

  // State management
  const [households, setHouseholds] = useState<HouseholdData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [filters, setFilters] = useState<HouseholdFiltersType>({
    region: searchParams.get('region') || '',
    province: searchParams.get('province') || '',
    municipality: searchParams.get('municipality') || '',
    status: searchParams.get('status') || '',
    validationStatus: searchParams.get('validationStatus') || '',
  });
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [pagination, setPagination] = useState({
    page: parseInt(searchParams.get('page') || '1'),
    limit: parseInt(searchParams.get('limit') || '20'),
    total: 0,
    totalPages: 0,
  });

  // Check authentication and permissions
  useEffect(() => {
    if (!isAuthenticated || !user) {
      router.push('/auth/login');
      return;
    }

    // Check if user has permission to view households
    const allowedRoles = ['DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN', 'CASE_WORKER'];
    if (!allowedRoles.includes(user.role)) {
      router.push('/dashboard');
      return;
    }
  }, [isAuthenticated, user, router]);

  // Load households
  const loadHouseholds = async () => {
    try {
      setLoading(true);
      setError(null);

      let response;
      if (searchQuery.trim()) {
        response = await dataManagementApi.searchHouseholds(searchQuery, filters);
      } else {
        response = await dataManagementApi.getHouseholds({
          page: pagination.page,
          limit: pagination.limit,
          ...filters,
        });
      }

      setHouseholds(response.data);
      setPagination(prev => ({
        ...prev,
        total: response.total,
        totalPages: response.totalPages,
      }));
    } catch (err) {
      console.error('Error loading households:', err);
      setError('Failed to load households. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Load households on component mount and when dependencies change
  useEffect(() => {
    loadHouseholds();
  }, [searchQuery, filters, pagination.page, pagination.limit]);

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setPagination(prev => ({ ...prev, page: 1 }));
    
    // Update URL
    const params = new URLSearchParams(searchParams);
    if (query) {
      params.set('q', query);
    } else {
      params.delete('q');
    }
    params.set('page', '1');
    router.push(`/households?${params.toString()}`);
  };

  // Handle filter changes
  const handleFilterChange = (newFilters: Partial<HouseholdFiltersType>) => {
    const updatedFilters = { ...filters, ...newFilters };
    setFilters(updatedFilters);
    setPagination(prev => ({ ...prev, page: 1 }));

    // Update URL
    const params = new URLSearchParams(searchParams);
    Object.entries(updatedFilters).forEach(([key, value]) => {
      if (value) {
        params.set(key, value);
      } else {
        params.delete(key);
      }
    });
    params.set('page', '1');
    router.push(`/households?${params.toString()}`);
  };

  // Handle pagination
  const handlePageChange = (page: number) => {
    setPagination(prev => ({ ...prev, page }));
    
    const params = new URLSearchParams(searchParams);
    params.set('page', page.toString());
    router.push(`/households?${params.toString()}`);
  };

  // Handle household creation
  const handleCreateHousehold = async (householdData: any) => {
    try {
      await dataManagementApi.createHousehold(householdData);
      setShowCreateModal(false);
      loadHouseholds(); // Refresh the list
    } catch (err) {
      console.error('Error creating household:', err);
      throw err; // Let the modal handle the error
    }
  };

  // Clear all filters
  const clearFilters = () => {
    setFilters({
      region: '',
      province: '',
      municipality: '',
      status: '',
      validationStatus: '',
    });
    setSearchQuery('');
    setPagination(prev => ({ ...prev, page: 1 }));
    router.push('/households');
  };

  if (!isAuthenticated || !user) {
    return null; // Will redirect in useEffect
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Household Management</h1>
          <p className="text-gray-600">
            Search, view, and manage household registrations
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Button
            variant="outline"
            onClick={clearFilters}
            disabled={loading}
          >
            Clear Filters
          </Button>
          <Button
            onClick={() => setShowCreateModal(true)}
            disabled={loading}
          >
            Add Household
          </Button>
        </div>
      </div>

      {/* Search and Filters */}
      <Card className="p-6">
        <div className="space-y-4">
          {/* Search Bar */}
          <div className="flex items-center space-x-4">
            <div className="flex-1">
              <FormInput
                label="Search Households"
                placeholder="Search by household number, PSN, name, or location..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearch(searchQuery);
                  }
                }}
              />
            </div>
            <Button
              onClick={() => handleSearch(searchQuery)}
              disabled={loading}
            >
              Search
            </Button>
          </div>

          {/* Filters */}
          <HouseholdFilters
            filters={filters}
            onFilterChange={handleFilterChange}
            loading={loading}
          />
        </div>
      </Card>

      {/* Error Display */}
      {error && (
        <Alert variant="error" title="Error">
          {error}
        </Alert>
      )}

      {/* Results Summary */}
      {!loading && (
        <div className="flex items-center justify-between text-sm text-gray-600">
          <span>
            Showing {households.length} of {pagination.total} households
            {searchQuery && ` for "${searchQuery}"`}
          </span>
          <span>
            Page {pagination.page} of {pagination.totalPages}
          </span>
        </div>
      )}

      {/* Household List */}
      <HouseholdList
        households={households}
        loading={loading}
        pagination={pagination}
        onPageChange={handlePageChange}
        onRefresh={loadHouseholds}
      />

      {/* Create Household Modal */}
      <CreateHouseholdModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreateHousehold}
        currentUser={user}
      />
    </div>
  );
}
