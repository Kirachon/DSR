'use client';

// Registration List Component
// Displays a list of citizen registration applications

import React from 'react';

import { Button, Badge, Loading } from '@/components/ui';
import type { CitizenRegistration } from '@/types';

// Registration list props interface
interface RegistrationListProps {
  registrations: CitizenRegistration[];
  loading: boolean;
  onRegistrationReview: (registration: CitizenRegistration) => void;
  onRefresh: () => void;
}

// Status badge variant mapping
const getStatusVariant = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'UNDER_REVIEW':
      return 'info';
    case 'REJECTED':
      return 'error';
    case 'REQUIRES_DOCUMENTS':
      return 'warning';
    default:
      return 'secondary';
  }
};

// Registration List component
export const RegistrationList: React.FC<RegistrationListProps> = ({
  registrations,
  loading,
  onRegistrationReview,
  onRefresh,
}) => {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loading text="Loading registrations..." />
      </div>
    );
  }

  if (registrations.length === 0) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-500 mb-4">
          <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No registrations found</h3>
        <p className="text-gray-600 mb-4">
          No registration applications match your current criteria.
        </p>
        <Button onClick={onRefresh}>Refresh List</Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Registrations Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Applicant
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Contact
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Household Info
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Submission Date
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {registrations.map((registration) => (
              <tr key={registration.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10">
                      <div className="h-10 w-10 rounded-full bg-primary-100 flex items-center justify-center">
                        <span className="text-sm font-medium text-primary-700">
                          {registration.applicantName.split(' ').map(n => n.charAt(0)).join('')}
                        </span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900">
                        {registration.applicantName}
                      </div>
                      <div className="text-sm text-gray-500">
                        ID: {registration.id}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{registration.applicantEmail}</div>
                  <div className="text-sm text-gray-500">{registration.phoneNumber}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <Badge variant={getStatusVariant(registration.status)}>
                    {registration.status}
                  </Badge>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">
                    Size: {registration.householdSize}
                  </div>
                  <div className="text-sm text-gray-500">
                    Income: ₱{registration.monthlyIncome?.toLocaleString() || 'N/A'}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(registration.submissionDate).toLocaleDateString()}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex justify-end space-x-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => onRegistrationReview(registration)}
                    >
                      Review
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-gray-700">
          Showing {registrations.length} registrations
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" size="sm" disabled>
            Previous
          </Button>
          <Button variant="outline" size="sm" disabled>
            Next
          </Button>
        </div>
      </div>
    </div>
  );
};
