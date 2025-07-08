'use client';

// Citizen List Component
// Displays a list of citizens with filtering and action capabilities

import React from 'react';

import { Button, Badge, Loading } from '@/components/ui';
import type { Citizen } from '@/types';

// Citizen list props interface
interface CitizenListProps {
  citizens: Citizen[];
  loading: boolean;
  onCitizenSelect: (citizen: Citizen) => void;
  onRefresh: () => void;
}

// Status badge variant mapping
const getStatusVariant = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'INACTIVE':
      return 'secondary';
    case 'SUSPENDED':
      return 'error';
    default:
      return 'secondary';
  }
};

// Verification status badge variant mapping
const getVerificationVariant = (status: string) => {
  switch (status) {
    case 'VERIFIED':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'REJECTED':
      return 'error';
    default:
      return 'secondary';
  }
};

// Citizen List component
export const CitizenList: React.FC<CitizenListProps> = ({
  citizens,
  loading,
  onCitizenSelect,
  onRefresh,
}) => {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loading text="Loading citizens..." />
      </div>
    );
  }

  if (citizens.length === 0) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-500 mb-4">
          <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No citizens found</h3>
        <p className="text-gray-600 mb-4">
          No citizens match your current search criteria.
        </p>
        <Button onClick={onRefresh}>Refresh List</Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Citizens Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Citizen
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Contact
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Verification
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Household
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Registration Date
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {citizens.map((citizen) => (
              <tr key={citizen.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10">
                      <div className="h-10 w-10 rounded-full bg-primary-100 flex items-center justify-center">
                        <span className="text-sm font-medium text-primary-700">
                          {citizen.firstName.charAt(0)}{citizen.lastName.charAt(0)}
                        </span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900">
                        {citizen.firstName} {citizen.lastName}
                      </div>
                      <div className="text-sm text-gray-500">
                        ID: {citizen.id}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{citizen.email}</div>
                  <div className="text-sm text-gray-500">{citizen.phoneNumber}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <Badge variant={getStatusVariant(citizen.status)}>
                    {citizen.status}
                  </Badge>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <Badge variant={getVerificationVariant(citizen.verificationStatus)}>
                    {citizen.verificationStatus}
                  </Badge>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {citizen.householdId || 'N/A'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(citizen.registrationDate).toLocaleDateString()}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex justify-end space-x-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => onCitizenSelect(citizen)}
                    >
                      View Details
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
          Showing {citizens.length} citizens
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
