'use client';

// Case List Component
// Display and manage list of cases with actions

import React, { useState, useEffect } from 'react';

import { FormSelect } from '@/components/forms';
import { Card, Button, Modal } from '@/components/ui';
import { registrationApi } from '@/lib/api';
import type { Case, CaseStatus } from '@/types';

// Component props interface
interface CaseListProps {
  cases: Case[];
  loading?: boolean;
  onCaseClick: (caseId: string) => void;
  onStatusChange: (caseId: string, newStatus: CaseStatus) => void;
  onAssignmentChange: (
    caseId: string,
    assignedToId: string | null,
    assignedTo: string
  ) => void;
}

// Status options for quick updates
const STATUS_OPTIONS = [
  { value: 'NEW', label: 'New' },
  { value: 'ASSIGNED', label: 'Assigned' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'PENDING_REVIEW', label: 'Pending Review' },
  { value: 'PENDING_CITIZEN_RESPONSE', label: 'Pending Citizen Response' },
  { value: 'ESCALATED', label: 'Escalated' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'CLOSED', label: 'Closed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

// Assignment options will be loaded from API inside the component

// Get priority color
const getPriorityColor = (priority: string) => {
  switch (priority) {
    case 'CRITICAL':
      return 'bg-red-100 text-red-800 border-red-200';
    case 'URGENT':
      return 'bg-red-100 text-red-800 border-red-200';
    case 'HIGH':
      return 'bg-orange-100 text-orange-800 border-orange-200';
    case 'MEDIUM':
      return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    case 'LOW':
      return 'bg-green-100 text-green-800 border-green-200';
    default:
      return 'bg-gray-100 text-gray-800 border-gray-200';
  }
};

// Get status color
const getStatusColor = (status: string) => {
  switch (status) {
    case 'NEW':
      return 'bg-blue-100 text-blue-800';
    case 'ASSIGNED':
      return 'bg-purple-100 text-purple-800';
    case 'IN_PROGRESS':
      return 'bg-yellow-100 text-yellow-800';
    case 'PENDING_REVIEW':
      return 'bg-orange-100 text-orange-800';
    case 'PENDING_CITIZEN_RESPONSE':
      return 'bg-indigo-100 text-indigo-800';
    case 'ESCALATED':
      return 'bg-red-100 text-red-800';
    case 'RESOLVED':
      return 'bg-green-100 text-green-800';
    case 'CLOSED':
      return 'bg-gray-100 text-gray-800';
    case 'CANCELLED':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// Get type color
const getTypeColor = (type: string) => {
  switch (type) {
    case 'GRIEVANCE':
      return 'bg-red-50 text-red-700 border-red-200';
    case 'APPEAL':
      return 'bg-orange-50 text-orange-700 border-orange-200';
    case 'INQUIRY':
      return 'bg-blue-50 text-blue-700 border-blue-200';
    case 'COMPLAINT':
      return 'bg-purple-50 text-purple-700 border-purple-200';
    case 'FEEDBACK':
      return 'bg-green-50 text-green-700 border-green-200';
    case 'REQUEST':
      return 'bg-indigo-50 text-indigo-700 border-indigo-200';
    default:
      return 'bg-gray-50 text-gray-700 border-gray-200';
  }
};

// Format date
const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

// Calculate days until due
const getDaysUntilDue = (dueDate: string): number => {
  const due = new Date(dueDate);
  const now = new Date();
  const diffTime = due.getTime() - now.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
};

// Case List component
export const CaseList: React.FC<CaseListProps> = ({
  cases,
  loading = false,
  onCaseClick,
  onStatusChange,
  onAssignmentChange,
}) => {
  const [selectedCase, setSelectedCase] = useState<Case | null>(null);
  const [isQuickActionModalOpen, setIsQuickActionModalOpen] = useState(false);
  const [assignmentOptions, setAssignmentOptions] = useState([
    { value: '', label: 'Unassigned' },
  ]);

  // Load assignment options from API
  useEffect(() => {
    const loadAssignmentOptions = async () => {
      try {
        // Load staff members who can be assigned cases
        const staffMembers = await registrationApi.getStaffMembers();
        const options = [
          { value: '', label: 'Unassigned' },
          ...staffMembers.map((staff: any) => ({
            value: staff.id,
            label: `${staff.firstName} ${staff.lastName}`,
          })),
        ];
        setAssignmentOptions(options);
      } catch (error) {
        console.warn('Failed to load assignment options:', error);
        // Keep default unassigned option
      }
    };

    loadAssignmentOptions();
  }, []);

  // Handle quick action modal
  const handleQuickAction = (caseItem: Case) => {
    setSelectedCase(caseItem);
    setIsQuickActionModalOpen(true);
  };

  // Handle status update
  const handleStatusUpdate = (newStatus: CaseStatus) => {
    if (selectedCase) {
      onStatusChange(selectedCase.id, newStatus);
      setIsQuickActionModalOpen(false);
      setSelectedCase(null);
    }
  };

  // Handle assignment update
  const handleAssignmentUpdate = (assignedToId: string) => {
    if (selectedCase) {
      const assignedTo = assignedToId
        ? ASSIGNMENT_OPTIONS.find(opt => opt.value === assignedToId)?.label ||
          'Unknown'
        : 'Unassigned';
      onAssignmentChange(selectedCase.id, assignedToId || null, assignedTo);
      setIsQuickActionModalOpen(false);
      setSelectedCase(null);
    }
  };

  if (loading) {
    return (
      <Card className='p-6'>
        <div className='animate-pulse space-y-4'>
          {[...Array(5)].map((_, index) => (
            <div key={index} className='border border-gray-200 rounded-lg p-4'>
              <div className='flex items-center justify-between mb-3'>
                <div className='h-4 bg-gray-200 rounded w-1/4'></div>
                <div className='flex space-x-2'>
                  <div className='h-6 bg-gray-200 rounded w-16'></div>
                  <div className='h-6 bg-gray-200 rounded w-20'></div>
                </div>
              </div>
              <div className='h-4 bg-gray-200 rounded w-3/4 mb-2'></div>
              <div className='h-3 bg-gray-200 rounded w-1/2'></div>
            </div>
          ))}
        </div>
      </Card>
    );
  }

  if (cases.length === 0) {
    return (
      <Card className='p-8 text-center'>
        <div className='text-gray-500'>
          <svg
            className='mx-auto h-12 w-12 text-gray-400 mb-4'
            fill='none'
            stroke='currentColor'
            viewBox='0 0 24 24'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth={2}
              d='M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
            />
          </svg>
          <h3 className='text-lg font-medium text-gray-900 mb-2'>
            No cases found
          </h3>
          <p className='text-gray-600'>
            No cases match your current filters. Try adjusting your search
            criteria.
          </p>
        </div>
      </Card>
    );
  }

  return (
    <>
      <Card className='p-6'>
        <div className='flex justify-between items-center mb-6'>
          <h2 className='text-lg font-semibold text-gray-900'>
            Cases ({cases.length})
          </h2>
          <div className='text-sm text-gray-600'>
            Showing {cases.length} case{cases.length !== 1 ? 's' : ''}
          </div>
        </div>

        <div className='space-y-4'>
          {cases.map(caseItem => {
            const daysUntilDue = getDaysUntilDue(caseItem.dueDate);
            const isOverdue = daysUntilDue < 0;
            const isDueSoon = daysUntilDue <= 2 && daysUntilDue >= 0;

            return (
              <div
                key={caseItem.id}
                className={`border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer ${
                  isOverdue
                    ? 'border-red-300 bg-red-50'
                    : isDueSoon
                      ? 'border-yellow-300 bg-yellow-50'
                      : 'border-gray-200 hover:border-gray-300'
                }`}
                onClick={() => onCaseClick(caseItem.id)}
              >
                <div className='flex items-start justify-between mb-3'>
                  <div className='flex-1'>
                    <div className='flex items-center space-x-3 mb-2'>
                      <h3 className='font-medium text-gray-900 text-sm'>
                        {caseItem.caseNumber}
                      </h3>
                      <span
                        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getTypeColor(caseItem.type)}`}
                      >
                        {caseItem.type}
                      </span>
                      <span
                        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getPriorityColor(caseItem.priority)}`}
                      >
                        {caseItem.priority}
                      </span>
                      <span
                        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(caseItem.status)}`}
                      >
                        {caseItem.status.replace('_', ' ')}
                      </span>
                    </div>
                    <h4 className='font-medium text-gray-900 mb-1'>
                      {caseItem.title}
                    </h4>
                    <p className='text-sm text-gray-600 mb-2 line-clamp-2'>
                      {caseItem.description}
                    </p>
                    <div className='flex items-center space-x-4 text-xs text-gray-500'>
                      <span>By: {caseItem.submittedBy}</span>
                      <span>
                        Assigned: {caseItem.assignedTo || 'Unassigned'}
                      </span>
                      <span>
                        Submitted: {formatDate(caseItem.submittedDate)}
                      </span>
                      <span
                        className={
                          isOverdue
                            ? 'text-red-600 font-medium'
                            : isDueSoon
                              ? 'text-yellow-600 font-medium'
                              : ''
                        }
                      >
                        Due: {formatDate(caseItem.dueDate)}
                        {isOverdue &&
                          ` (${Math.abs(daysUntilDue)} days overdue)`}
                        {isDueSoon &&
                          ` (${daysUntilDue} day${daysUntilDue !== 1 ? 's' : ''} left)`}
                      </span>
                    </div>
                  </div>
                  <div className='flex items-center space-x-2 ml-4'>
                    <Button
                      size='sm'
                      variant='outline'
                      onClick={e => {
                        e.stopPropagation();
                        handleQuickAction(caseItem);
                      }}
                    >
                      Quick Action
                    </Button>
                  </div>
                </div>

                {/* Additional indicators */}
                <div className='flex items-center justify-between'>
                  <div className='flex items-center space-x-2'>
                    {caseItem.notes.length > 0 && (
                      <span className='inline-flex items-center text-xs text-gray-500'>
                        <svg
                          className='h-3 w-3 mr-1'
                          fill='none'
                          stroke='currentColor'
                          viewBox='0 0 24 24'
                        >
                          <path
                            strokeLinecap='round'
                            strokeLinejoin='round'
                            strokeWidth={2}
                            d='M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-3.582 8-8 8a8.959 8.959 0 01-4.906-1.476L3 21l2.476-5.094A8.959 8.959 0 013 12c0-4.418 3.582-8 8-8s8 3.582 8 8z'
                          />
                        </svg>
                        {caseItem.notes.length} note
                        {caseItem.notes.length !== 1 ? 's' : ''}
                      </span>
                    )}
                    {caseItem.attachments.length > 0 && (
                      <span className='inline-flex items-center text-xs text-gray-500'>
                        <svg
                          className='h-3 w-3 mr-1'
                          fill='none'
                          stroke='currentColor'
                          viewBox='0 0 24 24'
                        >
                          <path
                            strokeLinecap='round'
                            strokeLinejoin='round'
                            strokeWidth={2}
                            d='M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13'
                          />
                        </svg>
                        {caseItem.attachments.length} file
                        {caseItem.attachments.length !== 1 ? 's' : ''}
                      </span>
                    )}
                    {caseItem.isUrgent && (
                      <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800'>
                        URGENT
                      </span>
                    )}
                  </div>
                  <div className='text-xs text-gray-500'>
                    Updated: {formatDate(caseItem.updatedAt)}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </Card>

      {/* Quick Action Modal */}
      <Modal
        isOpen={isQuickActionModalOpen}
        onClose={() => setIsQuickActionModalOpen(false)}
        title={`Quick Actions - ${selectedCase?.caseNumber}`}
      >
        {selectedCase && (
          <div className='space-y-6'>
            <div>
              <h4 className='font-medium text-gray-900 mb-2'>
                {selectedCase.title}
              </h4>
              <p className='text-sm text-gray-600'>
                {selectedCase.description}
              </p>
            </div>

            <div>
              <h4 className='font-medium text-gray-900 mb-3'>Update Status</h4>
              <div className='grid grid-cols-2 gap-2'>
                {STATUS_OPTIONS.map(status => (
                  <Button
                    key={status.value}
                    variant={
                      selectedCase.status === status.value
                        ? 'primary'
                        : 'outline'
                    }
                    size='sm'
                    onClick={() =>
                      handleStatusUpdate(status.value as CaseStatus)
                    }
                    disabled={selectedCase.status === status.value}
                  >
                    {status.label}
                  </Button>
                ))}
              </div>
            </div>

            <div>
              <h4 className='font-medium text-gray-900 mb-3'>Reassign Case</h4>
              <FormSelect
                label=''
                value={selectedCase.assignedToId || ''}
                onChange={handleAssignmentUpdate}
                options={ASSIGNMENT_OPTIONS}
              />
            </div>
          </div>
        )}
      </Modal>
    </>
  );
};
