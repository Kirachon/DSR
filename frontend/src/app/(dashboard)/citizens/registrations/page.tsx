'use client';

// Citizen Registrations Page
// Interface for managing citizen registration applications and approvals

import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { RegistrationFilters } from '@/components/citizens/registration-filters';
import { RegistrationList } from '@/components/citizens/registration-list';
import { RegistrationReviewModal } from '@/components/citizens/registration-review-modal';
import { FormInput, FormSelect } from '@/components/forms';
import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { registrationApi } from '@/lib/api';
import type { CitizenRegistration, RegistrationFilters as RegistrationFiltersType } from '@/types';

// Registration status options
const REGISTRATION_STATUSES = [
  { value: '', label: 'All Statuses' },
  { value: 'PENDING', label: 'Pending Review' },
  { value: 'UNDER_REVIEW', label: 'Under Review' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'REQUIRES_DOCUMENTS', label: 'Requires Documents' },
];

// Citizen Registrations page component
export default function CitizenRegistrationsPage() {
  const { user } = useAuth();
  const router = useRouter();

  // State management
  const [registrations, setRegistrations] = useState<CitizenRegistration[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<RegistrationFiltersType>({});
  const [selectedRegistration, setSelectedRegistration] = useState<CitizenRegistration | null>(null);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Load registrations data
  useEffect(() => {
    loadRegistrations();
  }, [filters]);

  // Load registrations from API
  const loadRegistrations = async () => {
    try {
      setLoading(true);
      setError(null);

      // Get registrations from registration service
      const response = await registrationApi.getRegistrations({
        ...filters,
        search: searchQuery,
        page: 0,
        size: 50,
      });

      setRegistrations(response.content || []);
    } catch (err) {
      console.error('Failed to load registrations:', err);
      setError('Failed to load registrations. Please try again.');

      // Set empty array when API fails - no mock data fallback
      setRegistrations([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setTimeout(() => loadRegistrations(), 300);
  };

  // Handle filter changes
  const handleFilterChange = (newFilters: RegistrationFiltersType) => {
    setFilters(newFilters);
  };

  // Handle registration review
  const handleRegistrationReview = (registration: CitizenRegistration) => {
    setSelectedRegistration(registration);
    setIsReviewModalOpen(true);
  };

  // Handle registration approval/rejection
  const handleRegistrationDecision = async (registrationId: string, decision: 'APPROVED' | 'REJECTED', comments?: string) => {
    try {
      await registrationApi.reviewRegistration(registrationId, {
        decision,
        comments,
        reviewedBy: user?.email || 'system',
      });

      // Update local state
      setRegistrations(prev => 
        prev.map(reg => 
          reg.id === registrationId 
            ? { ...reg, status: decision, reviewDate: new Date().toISOString(), reviewedBy: user?.email || 'system' }
            : reg
        )
      );

      setIsReviewModalOpen(false);
      setSelectedRegistration(null);
    } catch (err) {
      console.error('Failed to update registration:', err);
      setError('Failed to update registration. Please try again.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Citizen Registrations</h1>
          <p className="text-gray-600 mt-1">
            Review and manage citizen registration applications
          </p>
        </div>

        <div className="flex space-x-3">
          <Button variant="outline" onClick={() => loadRegistrations()}>
            Refresh
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
            <FormInput
              label="Search Registrations"
              placeholder="Search by applicant name, email, or registration ID..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </div>
          <div className="lg:w-64">
            <FormSelect
              label="Status Filter"
              value={filters.status || ''}
              onChange={(e) => handleFilterChange({ ...filters, status: e.target.value })}
              options={REGISTRATION_STATUSES}
            />
          </div>
        </div>
      </Card>

      {/* Registrations List */}
      <Card className="p-6">
        <RegistrationList
          registrations={registrations}
          loading={loading}
          onRegistrationReview={handleRegistrationReview}
          onRefresh={loadRegistrations}
        />
      </Card>

      {/* Registration Review Modal */}
      <RegistrationReviewModal
        isOpen={isReviewModalOpen}
        registration={selectedRegistration}
        onClose={() => {
          setIsReviewModalOpen(false);
          setSelectedRegistration(null);
        }}
        onDecision={handleRegistrationDecision}
      />
    </div>
  );
}
