'use client';

// Household List Component
// Displays a list of households with pagination and actions

import Link from 'next/link';
import React from 'react';

import { Card, Button, Badge, Pagination } from '@/components/ui';
import type { HouseholdData } from '@/types';

// Component props interface
interface HouseholdListProps {
  households: HouseholdData[];
  loading: boolean;
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
  onPageChange: (page: number) => void;
  onRefresh: () => void;
}

// Status badge component
const StatusBadge: React.FC<{ status: string }> = ({ status }) => {
  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return 'success';
      case 'inactive':
        return 'warning';
      case 'suspended':
        return 'error';
      case 'pending':
        return 'info';
      default:
        return 'neutral';
    }
  };

  return (
    <Badge variant={getStatusColor(status)}>
      {status}
    </Badge>
  );
};

// Validation status badge component
const ValidationBadge: React.FC<{ status: string }> = ({ status }) => {
  const getValidationColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'validated':
        return 'success';
      case 'pending':
        return 'warning';
      case 'rejected':
        return 'error';
      case 'requires_review':
        return 'info';
      default:
        return 'neutral';
    }
  };

  return (
    <Badge variant={getValidationColor(status)}>
      {status.replace('_', ' ')}
    </Badge>
  );
};

// Format currency
const formatCurrency = (amount: number | undefined) => {
  if (!amount) return 'N/A';
  return new Intl.NumberFormat('en-PH', {
    style: 'currency',
    currency: 'PHP',
  }).format(amount);
};

// Format date
const formatDate = (dateString: string | undefined) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('en-PH', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

// Household List component
export const HouseholdList: React.FC<HouseholdListProps> = ({
  households,
  loading,
  pagination,
  onPageChange,
  onRefresh,
}) => {
  if (loading) {
    return (
      <Card className="p-6">
        <div className="space-y-4">
          {[...Array(5)].map((_, index) => (
            <div key={index} className="animate-pulse">
              <div className="flex items-center space-x-4">
                <div className="w-12 h-12 bg-gray-200 rounded-full"></div>
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-1/4"></div>
                  <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                </div>
                <div className="w-20 h-6 bg-gray-200 rounded"></div>
              </div>
            </div>
          ))}
        </div>
      </Card>
    );
  }

  if (households.length === 0) {
    return (
      <Card className="p-12 text-center">
        <div className="space-y-4">
          <div className="w-16 h-16 mx-auto bg-gray-100 rounded-full flex items-center justify-center">
            <svg
              className="w-8 h-8 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
              />
            </svg>
          </div>
          <div>
            <h3 className="text-lg font-medium text-gray-900">No households found</h3>
            <p className="text-gray-600">
              Try adjusting your search criteria or filters.
            </p>
          </div>
          <Button onClick={onRefresh} variant="outline">
            Refresh
          </Button>
        </div>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {/* Household Cards */}
      <div className="space-y-4">
        {households.map((household) => (
          <Card key={household.id} className="p-6 hover:shadow-md transition-shadow">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-4 mb-3">
                  {/* Household Avatar */}
                  <div className="w-12 h-12 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center font-medium">
                    {household.householdHead.firstName.charAt(0)}
                    {household.householdHead.lastName.charAt(0)}
                  </div>
                  
                  {/* Basic Info */}
                  <div className="flex-1">
                    <div className="flex items-center space-x-3 mb-1">
                      <h3 className="text-lg font-semibold text-gray-900">
                        {household.householdHead.firstName} {household.householdHead.lastName}
                      </h3>
                      <StatusBadge status={household.status || 'active'} />
                      <ValidationBadge status={household.validationStatus || 'pending'} />
                    </div>
                    <div className="flex items-center space-x-4 text-sm text-gray-600">
                      <span>PSN: {household.psn}</span>
                      <span>•</span>
                      <span>Members: {household.members.length}</span>
                      <span>•</span>
                      <span>Income: {formatCurrency(household.monthlyIncome)}</span>
                    </div>
                  </div>
                </div>

                {/* Address */}
                <div className="mb-3">
                  <p className="text-sm text-gray-600">
                    <span className="font-medium">Address:</span>{' '}
                    {household.address.streetAddress}, {household.address.barangay},{' '}
                    {household.address.municipality}, {household.address.province},{' '}
                    {household.address.region}
                  </p>
                </div>

                {/* Additional Info */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                  <div>
                    <span className="text-gray-500">Registration:</span>
                    <p className="font-medium">{formatDate(household.registrationDate)}</p>
                  </div>
                  <div>
                    <span className="text-gray-500">Source:</span>
                    <p className="font-medium">{household.sourceSystem || 'Manual'}</p>
                  </div>
                  <div>
                    <span className="text-gray-500">Language:</span>
                    <p className="font-medium">{household.preferredLanguage || 'Filipino'}</p>
                  </div>
                  <div>
                    <span className="text-gray-500">Updated:</span>
                    <p className="font-medium">{formatDate(household.updatedAt)}</p>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex items-center space-x-2 ml-4">
                <Link href={`/households/${household.id}`}>
                  <Button variant="outline" size="sm">
                    View Details
                  </Button>
                </Link>
                <Link href={`/households/${household.id}/edit`}>
                  <Button variant="outline" size="sm">
                    Edit
                  </Button>
                </Link>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="flex justify-center">
          <Pagination
            currentPage={pagination.page}
            totalPages={pagination.totalPages}
            onPageChange={onPageChange}
          />
        </div>
      )}
    </div>
  );
};
