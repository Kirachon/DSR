'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { dataManagementApi } from '@/lib/api';
import type { HouseholdData } from '@/types';

interface HouseholdDetailClientProps {
  id: string;
}

export function HouseholdDetailClient({ id }: HouseholdDetailClientProps) {
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const [household, setHousehold] = useState<HouseholdData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated || !user) {
      router.push('/auth/login');
      return;
    }
  }, [isAuthenticated, user, router]);

  useEffect(() => {
    const fetchHousehold = async () => {
      if (!id || !isAuthenticated) return;

      try {
        setLoading(true);
        setError(null);
        const response = await dataManagementApi.getHousehold(id);
        setHousehold(response.data);
      } catch (err) {
        console.error('Error fetching household:', err);
        setError('Failed to load household data. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchHousehold();
  }, [id, isAuthenticated]);

  if (!isAuthenticated || !user) {
    return null;
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-4"></div>
          <div className="h-64 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6">
        <Alert variant="error" title="Error Loading Household">
          {error}
        </Alert>
        <Button onClick={() => router.back()}>Go Back</Button>
      </div>
    );
  }

  if (!household) {
    return (
      <div className="space-y-6">
        <Alert variant="warning" title="Household Not Found">
          The requested household could not be found.
        </Alert>
        <Button onClick={() => router.back()}>Go Back</Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Household Details
          </h1>
          <p className="text-gray-600">
            Household ID: {household.id}
          </p>
        </div>
        <div className="flex space-x-3">
          <Button variant="outline" onClick={() => router.back()}>
            Back
          </Button>
          <Button>
            Edit Household
          </Button>
        </div>
      </div>

      {/* Household Information */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          Basic Information
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Head of Household
            </label>
            <p className="text-gray-900">
              {household.headOfHousehold?.firstName} {household.headOfHousehold?.lastName}
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Address
            </label>
            <p className="text-gray-900">
              {household.address?.street}, {household.address?.barangay}
              <br />
              {household.address?.municipality}, {household.address?.province}
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Contact Number
            </label>
            <p className="text-gray-900">
              {household.contactNumber || 'Not provided'}
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status
            </label>
            <Badge variant={household.status === 'ACTIVE' ? 'success' : 'secondary'}>
              {household.status}
            </Badge>
          </div>
        </div>
      </Card>

      {/* Household Members */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          Household Members ({household.members?.length || 0})
        </h2>
        {household.members && household.members.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Relationship
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Age
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {household.members.map((member, index) => (
                  <tr key={index}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {member.firstName} {member.lastName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {member.relationship}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {member.age}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Badge variant={member.status === 'ACTIVE' ? 'success' : 'secondary'}>
                        {member.status}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-gray-500">No household members found.</p>
        )}
      </Card>

      {/* Economic Information */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          Economic Information
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Monthly Income
            </label>
            <p className="text-gray-900">
              ₱{household.monthlyIncome?.toLocaleString() || 'Not provided'}
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Income Source
            </label>
            <p className="text-gray-900">
              {household.incomeSource || 'Not provided'}
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Poverty Status
            </label>
            <Badge variant={household.povertyStatus === 'POOR' ? 'error' : 'success'}>
              {household.povertyStatus || 'Not assessed'}
            </Badge>
          </div>
        </div>
      </Card>

      {/* Actions */}
      <div className="flex justify-end space-x-3">
        <Button variant="outline">
          Generate Report
        </Button>
        <Button>
          Update Information
        </Button>
      </div>
    </div>
  );
}
