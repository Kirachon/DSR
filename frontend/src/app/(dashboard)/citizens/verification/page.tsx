'use client';

// Citizen Verification Page
// Interface for verifying citizen identity and documents

import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { VerificationFilters } from '@/components/citizens/verification-filters';
import { VerificationList } from '@/components/citizens/verification-list';
import { VerificationModal } from '@/components/citizens/verification-modal';
import { FormInput, FormSelect } from '@/components/forms';
import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { dataManagementApi, registrationApi } from '@/lib/api';
import type { CitizenVerification, VerificationFilters as VerificationFiltersType } from '@/types';

// Verification status options
const VERIFICATION_STATUSES = [
  { value: '', label: 'All Statuses' },
  { value: 'PENDING', label: 'Pending Verification' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'VERIFIED', label: 'Verified' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'REQUIRES_ADDITIONAL_INFO', label: 'Requires Additional Info' },
];

// Citizen Verification page component
export default function CitizenVerificationPage() {
  const { user } = useAuth();
  const router = useRouter();

  // State management
  const [verifications, setVerifications] = useState<CitizenVerification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<VerificationFiltersType>({});
  const [selectedVerification, setSelectedVerification] = useState<CitizenVerification | null>(null);
  const [isVerificationModalOpen, setIsVerificationModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Load verifications data
  useEffect(() => {
    loadVerifications();
  }, [filters]);

  // Load verifications from API
  const loadVerifications = async () => {
    try {
      setLoading(true);
      setError(null);

      // Get verifications from data management service
      const response = await dataManagementApi.getVerifications({
        ...filters,
        search: searchQuery,
        page: 0,
        size: 50,
      });

      setVerifications(response.content || []);
    } catch (err) {
      console.error('Failed to load verifications:', err);
      setError('Failed to load verifications. Please try again.');
      
      // Fallback to mock data for development
      setVerifications([
        {
          id: 'VER001',
          citizenId: 'CIT001',
          citizenName: 'Juan Dela Cruz',
          citizenEmail: 'juan.delacruz@example.com',
          phoneNumber: '+639123456789',
          status: 'PENDING',
          submissionDate: '2024-01-15T10:30:00Z',
          verificationDate: null,
          verifiedBy: null,
          verificationMethod: 'DOCUMENT_REVIEW',
          documents: [
            {
              type: 'BIRTH_CERTIFICATE',
              status: 'SUBMITTED',
              url: '/docs/birth-cert-001.pdf',
              verificationStatus: 'PENDING',
            },
            {
              type: 'VALID_ID',
              status: 'SUBMITTED',
              url: '/docs/valid-id-001.pdf',
              verificationStatus: 'PENDING',
            },
          ],
          philsysData: {
            philsysId: 'PSN-1234-5678-9012',
            status: 'PENDING_VERIFICATION',
            matchScore: null,
          },
          biometricData: {
            fingerprintStatus: 'NOT_SUBMITTED',
            faceRecognitionStatus: 'NOT_SUBMITTED',
          },
        },
        {
          id: 'VER002',
          citizenId: 'CIT002',
          citizenName: 'Maria Santos',
          citizenEmail: 'maria.santos@example.com',
          phoneNumber: '+639987654321',
          status: 'VERIFIED',
          submissionDate: '2024-01-20T14:15:00Z',
          verificationDate: '2024-01-22T16:30:00Z',
          verifiedBy: 'staff@dsr.gov.ph',
          verificationMethod: 'PHILSYS_INTEGRATION',
          documents: [
            {
              type: 'BIRTH_CERTIFICATE',
              status: 'APPROVED',
              url: '/docs/birth-cert-002.pdf',
              verificationStatus: 'VERIFIED',
            },
            {
              type: 'VALID_ID',
              status: 'APPROVED',
              url: '/docs/valid-id-002.pdf',
              verificationStatus: 'VERIFIED',
            },
          ],
          philsysData: {
            philsysId: 'PSN-9876-5432-1098',
            status: 'VERIFIED',
            matchScore: 98.5,
          },
          biometricData: {
            fingerprintStatus: 'VERIFIED',
            faceRecognitionStatus: 'VERIFIED',
          },
        },
      ] as CitizenVerification[]);
    } finally {
      setLoading(false);
    }
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setTimeout(() => loadVerifications(), 300);
  };

  // Handle filter changes
  const handleFilterChange = (newFilters: VerificationFiltersType) => {
    setFilters(newFilters);
  };

  // Handle verification review
  const handleVerificationReview = (verification: CitizenVerification) => {
    setSelectedVerification(verification);
    setIsVerificationModalOpen(true);
  };

  // Handle verification decision
  const handleVerificationDecision = async (
    verificationId: string, 
    decision: 'VERIFIED' | 'REJECTED' | 'REQUIRES_ADDITIONAL_INFO', 
    comments?: string
  ) => {
    try {
      await dataManagementApi.updateVerification(verificationId, {
        status: decision,
        comments,
        verifiedBy: user?.email || 'system',
        verificationDate: new Date().toISOString(),
      });

      // Update local state
      setVerifications(prev => 
        prev.map(ver => 
          ver.id === verificationId 
            ? { 
                ...ver, 
                status: decision, 
                verificationDate: new Date().toISOString(), 
                verifiedBy: user?.email || 'system' 
              }
            : ver
        )
      );

      setIsVerificationModalOpen(false);
      setSelectedVerification(null);
    } catch (err) {
      console.error('Failed to update verification:', err);
      setError('Failed to update verification. Please try again.');
    }
  };

  // Handle PhilSys verification
  const handlePhilSysVerification = async (verificationId: string) => {
    try {
      const result = await dataManagementApi.verifyWithPhilSys(verificationId);
      
      // Update local state with PhilSys verification result
      setVerifications(prev => 
        prev.map(ver => 
          ver.id === verificationId 
            ? { 
                ...ver, 
                philsysData: result.philsysData,
                status: result.verified ? 'VERIFIED' : 'REQUIRES_ADDITIONAL_INFO'
              }
            : ver
        )
      );
    } catch (err) {
      console.error('Failed to verify with PhilSys:', err);
      setError('Failed to verify with PhilSys. Please try again.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Citizen Verification</h1>
          <p className="text-gray-600 mt-1">
            Verify citizen identity through documents and PhilSys integration
          </p>
        </div>

        <div className="flex space-x-3">
          <Button variant="outline" onClick={() => loadVerifications()}>
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
              label="Search Verifications"
              placeholder="Search by citizen name, email, or verification ID..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </div>
          <div className="lg:w-64">
            <FormSelect
              label="Status Filter"
              value={filters.status || ''}
              onChange={(e) => handleFilterChange({ ...filters, status: e.target.value })}
              options={VERIFICATION_STATUSES}
            />
          </div>
        </div>
      </Card>

      {/* Verifications List */}
      <Card className="p-6">
        <VerificationList
          verifications={verifications}
          loading={loading}
          onVerificationReview={handleVerificationReview}
          onPhilSysVerification={handlePhilSysVerification}
          onRefresh={loadVerifications}
        />
      </Card>

      {/* Verification Modal */}
      <VerificationModal
        isOpen={isVerificationModalOpen}
        verification={selectedVerification}
        onClose={() => {
          setIsVerificationModalOpen(false);
          setSelectedVerification(null);
        }}
        onDecision={handleVerificationDecision}
      />
    </div>
  );
}
