'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { dataManagementApi } from '@/lib/api';
import type { HouseholdData } from '@/types';

interface HouseholdDetailPageProps {
  params: {
    id: string;
  };
}

export default function HouseholdDetailPage({ params }: HouseholdDetailPageProps) {
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
    const loadHousehold = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await dataManagementApi.getHousehold(params.id);
        setHousehold(response);
      } catch (err) {
        console.error('Error loading household:', err);
        setError('Failed to load household details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (params.id) {
      loadHousehold();
    }
  }, [params.id]);

  if (!isAuthenticated || !user) {
    return null;
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/3 mb-4"></div>
          <div className="h-4 bg-gray-200 rounded w-1/2 mb-8"></div>
          <div className="space-y-4">
            <div className="h-32 bg-gray-200 rounded"></div>
            <div className="h-32 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !household) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Household Details</h1>
          </div>
          <Link href="/households">
            <Button variant="outline">
              Back to Households
            </Button>
          </Link>
        </div>
        
        <Alert variant="error" title="Error">
          {error || 'Household not found'}
        </Alert>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Household Details
          </h1>
          <p className="text-gray-600">
            Household ID: {household.id}
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Link href="/households">
            <Button variant="outline">
              Back to Households
            </Button>
          </Link>
        </div>
      </div>

      <Card className="p-6">
        <div className="flex items-start justify-between mb-6">
          <div className="flex items-center space-x-4">
            <div className="w-16 h-16 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-xl font-bold">
              HH
            </div>
            <div>
              <h2 className="text-xl font-semibold text-gray-900">
                Household Information
              </h2>
              <p className="text-gray-600">Details and members</p>
              <div className="flex items-center space-x-2 mt-1">
                <Badge variant="success">
                  Active
                </Badge>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div>
            <h3 className="text-sm font-medium text-gray-500 mb-2">PSN</h3>
            <p className="text-lg font-semibold text-gray-900">{household.psn}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500 mb-2">Total Members</h3>
            <p className="text-lg font-semibold text-gray-900">{household.members?.length || 0}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500 mb-2">Monthly Income</h3>
            <p className="text-lg font-semibold text-gray-900">
              ₱{household.monthlyIncome?.toLocaleString() || 'N/A'}
            </p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500 mb-2">Status</h3>
            <p className="text-lg font-semibold text-gray-900">
              {household.status || 'Active'}
            </p>
          </div>
        </div>
      </Card>

      {household.address && (
        <Card className="p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Address Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h3 className="text-sm font-medium text-gray-500 mb-1">Complete Address</h3>
              <p className="text-gray-900">
                {household.address.streetAddress}<br />
                {household.address.barangay}, {household.address.municipality}<br />
                {household.address.province}, {household.address.region}
              </p>
            </div>
            <div className="space-y-4">
              <div>
                <h3 className="text-sm font-medium text-gray-500 mb-1">Region</h3>
                <p className="text-gray-900">{household.address.region}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500 mb-1">Province</h3>
                <p className="text-gray-900">{household.address.province}</p>
              </div>
            </div>
          </div>
        </Card>
      )}

      {household.members && household.members.length > 0 && (
        <Card className="p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Household Members</h2>
          <div className="space-y-4">
            {household.members.map((member, index) => (
              <div
                key={member.id || index}
                className="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
              >
                <div className="flex items-center space-x-4">
                  <div className="w-10 h-10 bg-gray-100 text-gray-600 rounded-full flex items-center justify-center font-medium">
                    {member.firstName?.charAt(0) || 'M'}{member.lastName?.charAt(0) || 'M'}
                  </div>
                  <div>
                    <div className="flex items-center space-x-2">
                      <h4 className="font-medium text-gray-900">
                        {member.firstName} {member.lastName}
                      </h4>
                      {member.isHeadOfHousehold && (
                        <Badge variant="primary">Head</Badge>
                      )}
                    </div>
                    <div className="flex items-center space-x-4 text-sm text-gray-600">
                      <span>{member.relationshipToHead || 'Member'}</span>
                      <span>•</span>
                      <span>{member.gender}</span>
                    </div>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-sm font-medium text-gray-900">
                    ₱{member.monthlyIncome?.toLocaleString() || 'N/A'}
                  </p>
                  <p className="text-xs text-gray-500">Monthly Income</p>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}
