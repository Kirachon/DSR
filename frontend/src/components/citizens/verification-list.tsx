'use client';

import React, { useState } from 'react';
import { DataTable } from '@/components/ui/data-table';
import { StatusBadge } from '@/components/ui/status-badge';
import { Button } from '@/components/ui/button';

interface VerificationRecord {
  id: string;
  citizenName: string;
  citizenId: string;
  verificationType: string;
  status: 'PENDING' | 'VERIFIED' | 'REJECTED' | 'EXPIRED';
  submittedDate: string;
  verifiedDate?: string;
  verifiedBy?: string;
  documents: string[];
  notes?: string;
}

interface VerificationListProps {
  verifications: VerificationRecord[];
  onVerify: (id: string) => void;
  onReject: (id: string) => void;
  onView: (verification: VerificationRecord) => void;
}

export function VerificationList({ verifications, onVerify, onReject, onView }: VerificationListProps) {
  const [sortField, setSortField] = useState<keyof VerificationRecord>('submittedDate');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  const columns = [
    {
      key: 'citizenName',
      label: 'Citizen Name',
      sortable: true,
    },
    {
      key: 'citizenId',
      label: 'Citizen ID',
      sortable: true,
    },
    {
      key: 'verificationType',
      label: 'Verification Type',
      sortable: true,
    },
    {
      key: 'status',
      label: 'Status',
      sortable: true,
      render: (value: string) => <StatusBadge status={value} />,
    },
    {
      key: 'submittedDate',
      label: 'Submitted',
      sortable: true,
      render: (value: string) => new Date(value).toLocaleDateString(),
    },
    {
      key: 'verifiedDate',
      label: 'Verified',
      sortable: true,
      render: (value: string) => value ? new Date(value).toLocaleDateString() : '-',
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (_: any, record: VerificationRecord) => (
        <div className="flex space-x-2">
          <Button
            size="sm"
            variant="outline"
            onClick={() => onView(record)}
          >
            View
          </Button>
          {record.status === 'PENDING' && (
            <>
              <Button
                size="sm"
                variant="default"
                onClick={() => onVerify(record.id)}
              >
                Verify
              </Button>
              <Button
                size="sm"
                variant="destructive"
                onClick={() => onReject(record.id)}
              >
                Reject
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  const sortedVerifications = [...verifications].sort((a, b) => {
    const aValue = a[sortField];
    const bValue = b[sortField];
    
    if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1;
    if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1;
    return 0;
  });

  const handleSort = (field: keyof VerificationRecord) => {
    if (field === sortField) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Verification Queue</h2>
        <div className="text-sm text-gray-600">
          {verifications.filter(v => v.status === 'PENDING').length} pending verifications
        </div>
      </div>
      
      <DataTable
        data={sortedVerifications}
        columns={columns}
        onSort={handleSort}
        sortField={sortField}
        sortDirection={sortDirection}
      />
    </div>
  );
}

export default VerificationList;
