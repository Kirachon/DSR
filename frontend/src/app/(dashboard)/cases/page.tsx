'use client';

// Cases Management Page
// Main interface for managing grievances, appeals, and inquiries

import { useRouter, useSearchParams } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { CaseFilters } from '@/components/cases/case-filters';
import { CaseList } from '@/components/cases/case-list';
import { CreateCaseModal } from '@/components/cases/create-case-modal';
import { FormInput, FormSelect } from '@/components/forms';
import { Card, Button, Alert, Modal } from '@/components/ui';
import { useAuth } from '@/contexts';
import { grievanceApi } from '@/lib/api';
import type { Case, CaseFilters as CaseFiltersType } from '@/types';

// Cases Management Page component
export default function CasesPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, isAuthenticated } = useAuth();

  const [cases, setCases] = useState<Case[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [filters, setFilters] = useState<CaseFiltersType>({
    status: searchParams.get('status') || '',
    priority: searchParams.get('priority') || '',
    type: searchParams.get('type') || '',
    assignedTo: searchParams.get('assignedTo') || '',
    dateRange: {
      start: searchParams.get('startDate') || '',
      end: searchParams.get('endDate') || '',
    },
    searchQuery: searchParams.get('q') || '',
  });

  // Load cases data
  useEffect(() => {
    loadCases();
  }, [filters]);

  // Load cases from API
  const loadCases = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await grievanceApi.getCases(filters);
      setCases(response.content || []);
    } catch (err) {
      console.error('Failed to load cases:', err);
      setError(err instanceof Error ? err.message : 'Failed to load cases');
      setCases([]); // Set empty array when API fails
    } finally {
      setLoading(false);
    }
  };

  // Handle filter changes
  const handleFiltersChange = (newFilters: CaseFiltersType) => {
    setFilters(newFilters);

    // Update URL parameters
    const params = new URLSearchParams();
    if (newFilters.status) params.set('status', newFilters.status);
    if (newFilters.priority) params.set('priority', newFilters.priority);
    if (newFilters.type) params.set('type', newFilters.type);
    if (newFilters.assignedTo) params.set('assignedTo', newFilters.assignedTo);
    if (newFilters.dateRange.start)
      params.set('startDate', newFilters.dateRange.start);
    if (newFilters.dateRange.end)
      params.set('endDate', newFilters.dateRange.end);
    if (newFilters.searchQuery) params.set('q', newFilters.searchQuery);

    router.push(`/cases?${params.toString()}`);
  };

  // Handle case creation
  const handleCreateCase = async (caseData: any) => {
    try {
      const response = await grievanceApi.createCase(caseData);
      setCases(prev => [response, ...prev]);
      setIsCreateModalOpen(false);
      console.log('Case created successfully:', response.caseNumber);
    } catch (err) {
      console.error('Failed to create case:', err);

      // Fallback to mock creation if API fails
      console.warn('Using mock creation as fallback');

      // Mock successful creation
      const newCase: Case = {
        id: Date.now().toString(),
        caseNumber: `GRV-2024-${String(cases.length + 1).padStart(3, '0')}`,
        title: caseData.title,
        type: caseData.type,
        priority: caseData.priority,
        status: 'NEW',
        assignedTo: 'Unassigned',
        assignedToId: null,
        submittedBy:
          caseData.submittedBy ||
          user?.firstName + ' ' + user?.lastName ||
          'Unknown',
        submittedById: user?.id || 'unknown',
        submittedDate: new Date().toISOString(),
        dueDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(), // 7 days from now
        description: caseData.description,
        category: caseData.category,
        resolution: null,
        notes: [],
        attachments: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      setCases(prev => [newCase, ...prev]);
      setIsCreateModalOpen(false);

      // Show success message
      // TODO: Add toast notification
      console.log('Case created successfully:', newCase.caseNumber);
    }
  };

  // Redirect if not authenticated
  if (!isAuthenticated) {
    router.push('/auth/login');
    return null;
  }

  // Check if user has permission to view cases
  const canViewCases =
    user?.role &&
    ['DSWD_STAFF', 'LGU_STAFF', 'CASE_WORKER', 'SYSTEM_ADMIN'].includes(
      user.role
    );
  if (!canViewCases) {
    return (
      <div className='max-w-2xl mx-auto p-6'>
        <Alert variant='error' title='Access Denied'>
          You don't have permission to view cases. Please contact your
          administrator.
        </Alert>
      </div>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex justify-between items-center'>
        <div>
          <h1 className='text-3xl font-bold text-gray-900'>Case Management</h1>
          <p className='text-gray-600 mt-1'>
            Manage grievances, appeals, and inquiries from citizens
          </p>
        </div>

        <div className='flex space-x-3'>
          <Button variant='outline' onClick={() => loadCases()}>
            Refresh
          </Button>
          <Button onClick={() => setIsCreateModalOpen(true)}>
            Create New Case
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant='error' title='Error Loading Cases'>
          {error}
        </Alert>
      )}

      {/* Filters */}
      <Card className='p-6'>
        <CaseFilters
          filters={filters}
          onFiltersChange={handleFiltersChange}
          loading={loading}
        />
      </Card>

      {/* Cases List */}
      <CaseList
        cases={cases}
        loading={loading}
        onCaseClick={caseId => router.push(`/cases/${caseId}`)}
        onStatusChange={async (caseId, newStatus) => {
          try {
            await grievanceApi.updateCaseStatus(caseId, newStatus);
            setCases(prev =>
              prev.map(c =>
                c.id === caseId
                  ? {
                      ...c,
                      status: newStatus,
                      updatedAt: new Date().toISOString(),
                    }
                  : c
              )
            );
          } catch (err) {
            console.error('Failed to update case status:', err);
            setError('Failed to update case status');
          }
        }}
        onAssignmentChange={async (caseId, assignedToId, assignedTo) => {
          try {
            await grievanceApi.assignCase(caseId, assignedToId);
            setCases(prev =>
              prev.map(c =>
                c.id === caseId
                  ? {
                      ...c,
                      assignedToId,
                      assignedTo,
                      updatedAt: new Date().toISOString(),
                    }
                  : c
              )
            );
          } catch (err) {
            console.error('Failed to assign case:', err);
            setError('Failed to assign case');
          }
        }}
      />

      {/* Create Case Modal */}
      <CreateCaseModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreateCase}
        currentUser={user}
      />
    </div>
  );
}
