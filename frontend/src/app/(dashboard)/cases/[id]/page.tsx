'use client';

// Case Detail Page
// Detailed view and management interface for individual cases

import { useRouter, useParams } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { FormInput, FormSelect, FormTextarea } from '@/components/forms';
import { Card, Button, Alert, Modal } from '@/components/ui';
import { useAuth } from '@/contexts';
import type { Case, CaseNote, CaseStatus } from '@/types';

// Case Detail Page component
export default function CaseDetailPage() {
  const router = useRouter();
  const params = useParams();
  const { user, isAuthenticated } = useAuth();

  const [caseData, setCaseData] = useState<Case | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAddNoteModalOpen, setIsAddNoteModalOpen] = useState(false);
  const [newNote, setNewNote] = useState('');
  const [isInternalNote, setIsInternalNote] = useState(true);

  // Load case data
  useEffect(() => {
    loadCaseData();
  }, [params.id]);

  // Load case data from API
  const loadCaseData = async () => {
    setLoading(true);
    setError(null);

    try {
      // TODO: Replace with actual API call to Grievance Service
      // const response = await grievanceApi.getCase(params.id);
      // setCaseData(response.data);

      // Mock data for now
      const mockCase: Case = {
        id: params.id as string,
        caseNumber: 'GRV-2024-001',
        title: 'Delayed benefit payment',
        type: 'GRIEVANCE',
        priority: 'HIGH',
        status: 'IN_PROGRESS',
        assignedTo: 'Maria Santos',
        assignedToId: 'case-worker-1',
        submittedBy: 'Juan Dela Cruz',
        submittedById: 'citizen-1',
        submittedDate: '2024-01-15T10:30:00Z',
        dueDate: '2024-01-20T17:00:00Z',
        description:
          "Beneficiary reports delayed 4Ps payment for December 2023. Payment was expected on December 15 but has not been received. This is causing significant hardship for the family as they depend on this payment for basic needs including food and children's school expenses.",
        category: 'PAYMENT_ISSUES',
        contactEmail: 'juan.delacruz@email.com',
        contactPhone: '+63 912 345 6789',
        preferredContactMethod: 'PHONE',
        resolution: null,
        notes: [
          {
            id: '1',
            content:
              'Initial case review completed. Checking with payment processing team to verify payment status.',
            createdBy: 'Maria Santos',
            createdById: 'case-worker-1',
            createdAt: '2024-01-15T14:30:00Z',
            isInternal: true,
          },
          {
            id: '2',
            content:
              'Contacted beneficiary to confirm bank account details. Account information is correct.',
            createdBy: 'Maria Santos',
            createdById: 'case-worker-1',
            createdAt: '2024-01-16T09:15:00Z',
            isInternal: false,
          },
          {
            id: '3',
            content:
              'Payment processing team confirmed technical issue with batch processing on December 15. Working on resolution.',
            createdBy: 'Maria Santos',
            createdById: 'case-worker-1',
            createdAt: '2024-01-16T15:45:00Z',
            isInternal: true,
          },
        ],
        attachments: [
          {
            id: '1',
            name: 'payment_inquiry_form.pdf',
            url: '/api/files/payment_inquiry_form.pdf',
            type: 'application/pdf',
            uploadedAt: '2024-01-15T10:30:00Z',
          },
          {
            id: '2',
            name: 'bank_account_verification.jpg',
            url: '/api/files/bank_account_verification.jpg',
            type: 'image/jpeg',
            uploadedAt: '2024-01-16T09:15:00Z',
          },
        ],
        createdAt: '2024-01-15T10:30:00Z',
        updatedAt: '2024-01-16T15:45:00Z',
        tags: ['payment-delay', 'technical-issue'],
        citizenId: 'citizen-1',
        householdId: 'household-1',
        beneficiaryId: 'beneficiary-1',
      };

      setCaseData(mockCase);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load case');
    } finally {
      setLoading(false);
    }
  };

  // Handle status update
  const handleStatusUpdate = async (newStatus: CaseStatus) => {
    if (!caseData) return;

    try {
      // TODO: Replace with actual API call
      // await grievanceApi.updateCaseStatus(caseData.id, newStatus);

      setCaseData(prev =>
        prev
          ? {
              ...prev,
              status: newStatus,
              updatedAt: new Date().toISOString(),
            }
          : null
      );

      console.log(`Case status updated to: ${newStatus}`);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Failed to update case status'
      );
    }
  };

  // Handle add note
  const handleAddNote = async () => {
    if (!caseData || !newNote.trim()) return;

    try {
      // TODO: Replace with actual API call
      // await grievanceApi.addCaseNote(caseData.id, { content: newNote, isInternal: isInternalNote });

      const note: CaseNote = {
        id: Date.now().toString(),
        content: newNote,
        createdBy: user?.firstName + ' ' + user?.lastName || 'Unknown',
        createdById: user?.id || 'unknown',
        createdAt: new Date().toISOString(),
        isInternal: isInternalNote,
      };

      setCaseData(prev =>
        prev
          ? {
              ...prev,
              notes: [...prev.notes, note],
              updatedAt: new Date().toISOString(),
            }
          : null
      );

      setNewNote('');
      setIsAddNoteModalOpen(false);

      console.log('Note added successfully');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add note');
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

  if (loading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse'>
          <div className='h-8 bg-gray-200 rounded w-1/3 mb-4'></div>
          <div className='h-4 bg-gray-200 rounded w-1/2 mb-6'></div>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            <div className='lg:col-span-2 space-y-6'>
              <div className='h-64 bg-gray-200 rounded'></div>
              <div className='h-48 bg-gray-200 rounded'></div>
            </div>
            <div className='space-y-6'>
              <div className='h-32 bg-gray-200 rounded'></div>
              <div className='h-48 bg-gray-200 rounded'></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !caseData) {
    return (
      <div className='max-w-2xl mx-auto p-6'>
        <Alert variant='error' title='Error Loading Case'>
          {error || 'Case not found'}
        </Alert>
        <div className='mt-4'>
          <Button onClick={() => router.push('/cases')}>Back to Cases</Button>
        </div>
      </div>
    );
  }

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
      case 'RESOLVED':
        return 'bg-green-100 text-green-800';
      case 'CLOSED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  // Format date
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex justify-between items-start'>
        <div>
          <div className='flex items-center space-x-2 mb-2'>
            <Button
              variant='outline'
              size='sm'
              onClick={() => router.push('/cases')}
            >
              ← Back to Cases
            </Button>
          </div>
          <h1 className='text-3xl font-bold text-gray-900'>
            {caseData.caseNumber}
          </h1>
          <p className='text-gray-600 mt-1'>{caseData.title}</p>
        </div>

        <div className='flex items-center space-x-3'>
          <span
            className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getPriorityColor(caseData.priority)}`}
          >
            {caseData.priority}
          </span>
          <span
            className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(caseData.status)}`}
          >
            {caseData.status.replace('_', ' ')}
          </span>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant='error' title='Error'>
          {error}
        </Alert>
      )}

      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
        {/* Main Content */}
        <div className='lg:col-span-2 space-y-6'>
          {/* Case Details */}
          <Card className='p-6'>
            <h2 className='text-lg font-semibold text-gray-900 mb-4'>
              Case Details
            </h2>
            <div className='space-y-4'>
              <div>
                <h3 className='font-medium text-gray-900 mb-2'>Description</h3>
                <p className='text-gray-700 whitespace-pre-wrap'>
                  {caseData.description}
                </p>
              </div>

              <div className='grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-gray-200'>
                <div>
                  <span className='font-medium text-gray-700'>Type:</span>
                  <p className='text-gray-900'>{caseData.type}</p>
                </div>
                <div>
                  <span className='font-medium text-gray-700'>Category:</span>
                  <p className='text-gray-900'>
                    {caseData.category.replace('_', ' ')}
                  </p>
                </div>
                <div>
                  <span className='font-medium text-gray-700'>
                    Submitted By:
                  </span>
                  <p className='text-gray-900'>{caseData.submittedBy}</p>
                </div>
                <div>
                  <span className='font-medium text-gray-700'>
                    Assigned To:
                  </span>
                  <p className='text-gray-900'>
                    {caseData.assignedTo || 'Unassigned'}
                  </p>
                </div>
                <div>
                  <span className='font-medium text-gray-700'>
                    Submitted Date:
                  </span>
                  <p className='text-gray-900'>
                    {formatDate(caseData.submittedDate)}
                  </p>
                </div>
                <div>
                  <span className='font-medium text-gray-700'>Due Date:</span>
                  <p className='text-gray-900'>
                    {formatDate(caseData.dueDate)}
                  </p>
                </div>
              </div>

              {/* Contact Information */}
              {(caseData.contactEmail || caseData.contactPhone) && (
                <div className='pt-4 border-t border-gray-200'>
                  <h3 className='font-medium text-gray-900 mb-2'>
                    Contact Information
                  </h3>
                  <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                    {caseData.contactEmail && (
                      <div>
                        <span className='font-medium text-gray-700'>
                          Email:
                        </span>
                        <p className='text-gray-900'>{caseData.contactEmail}</p>
                      </div>
                    )}
                    {caseData.contactPhone && (
                      <div>
                        <span className='font-medium text-gray-700'>
                          Phone:
                        </span>
                        <p className='text-gray-900'>{caseData.contactPhone}</p>
                      </div>
                    )}
                    {caseData.preferredContactMethod && (
                      <div>
                        <span className='font-medium text-gray-700'>
                          Preferred Contact:
                        </span>
                        <p className='text-gray-900'>
                          {caseData.preferredContactMethod}
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Tags */}
              {caseData.tags && caseData.tags.length > 0 && (
                <div className='pt-4 border-t border-gray-200'>
                  <h3 className='font-medium text-gray-900 mb-2'>Tags</h3>
                  <div className='flex flex-wrap gap-2'>
                    {caseData.tags.map((tag, index) => (
                      <span
                        key={index}
                        className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800'
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </Card>

          {/* Case Notes */}
          <Card className='p-6'>
            <div className='flex justify-between items-center mb-4'>
              <h2 className='text-lg font-semibold text-gray-900'>
                Case Notes ({caseData.notes.length})
              </h2>
              <Button onClick={() => setIsAddNoteModalOpen(true)}>
                Add Note
              </Button>
            </div>

            {caseData.notes.length === 0 ? (
              <p className='text-gray-600 text-center py-8'>
                No notes added yet.
              </p>
            ) : (
              <div className='space-y-4'>
                {caseData.notes.map(note => (
                  <div
                    key={note.id}
                    className={`p-4 rounded-lg border ${note.isInternal ? 'bg-yellow-50 border-yellow-200' : 'bg-blue-50 border-blue-200'}`}
                  >
                    <div className='flex justify-between items-start mb-2'>
                      <div className='flex items-center space-x-2'>
                        <span className='font-medium text-gray-900'>
                          {note.createdBy}
                        </span>
                        <span
                          className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                            note.isInternal
                              ? 'bg-yellow-100 text-yellow-800'
                              : 'bg-blue-100 text-blue-800'
                          }`}
                        >
                          {note.isInternal ? 'Internal' : 'External'}
                        </span>
                      </div>
                      <span className='text-sm text-gray-500'>
                        {formatDate(note.createdAt)}
                      </span>
                    </div>
                    <p className='text-gray-700 whitespace-pre-wrap'>
                      {note.content}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>

        {/* Sidebar */}
        <div className='space-y-6'>
          {/* Quick Actions */}
          <Card className='p-6'>
            <h2 className='text-lg font-semibold text-gray-900 mb-4'>
              Quick Actions
            </h2>
            <div className='space-y-3'>
              <div>
                <label className='block text-sm font-medium text-gray-700 mb-2'>
                  Update Status
                </label>
                <FormSelect
                  label=''
                  value={caseData.status}
                  onChange={handleStatusUpdate}
                  options={[
                    { value: 'NEW', label: 'New' },
                    { value: 'ASSIGNED', label: 'Assigned' },
                    { value: 'IN_PROGRESS', label: 'In Progress' },
                    { value: 'PENDING_REVIEW', label: 'Pending Review' },
                    {
                      value: 'PENDING_CITIZEN_RESPONSE',
                      label: 'Pending Citizen Response',
                    },
                    { value: 'ESCALATED', label: 'Escalated' },
                    { value: 'RESOLVED', label: 'Resolved' },
                    { value: 'CLOSED', label: 'Closed' },
                    { value: 'CANCELLED', label: 'Cancelled' },
                  ]}
                />
              </div>

              <Button variant='outline' className='w-full'>
                Reassign Case
              </Button>

              <Button variant='outline' className='w-full'>
                Escalate Case
              </Button>

              <Button variant='outline' className='w-full'>
                Generate Report
              </Button>
            </div>
          </Card>

          {/* Attachments */}
          <Card className='p-6'>
            <h2 className='text-lg font-semibold text-gray-900 mb-4'>
              Attachments ({caseData.attachments.length})
            </h2>

            {caseData.attachments.length === 0 ? (
              <p className='text-gray-600 text-center py-4'>No attachments</p>
            ) : (
              <div className='space-y-3'>
                {caseData.attachments.map(attachment => (
                  <div
                    key={attachment.id}
                    className='flex items-center justify-between p-3 border border-gray-200 rounded-lg'
                  >
                    <div className='flex items-center space-x-3'>
                      <svg
                        className='h-6 w-6 text-gray-400'
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
                      <div>
                        <p className='font-medium text-gray-900 text-sm'>
                          {attachment.name}
                        </p>
                        <p className='text-xs text-gray-500'>
                          {formatDate(attachment.uploadedAt)}
                        </p>
                      </div>
                    </div>
                    <Button size='sm' variant='outline'>
                      Download
                    </Button>
                  </div>
                ))}
              </div>
            )}

            <Button variant='outline' className='w-full mt-4'>
              Add Attachment
            </Button>
          </Card>
        </div>
      </div>

      {/* Add Note Modal */}
      <Modal
        isOpen={isAddNoteModalOpen}
        onClose={() => setIsAddNoteModalOpen(false)}
        title='Add Case Note'
      >
        <div className='space-y-4'>
          <FormTextarea
            label='Note Content'
            value={newNote}
            onChange={e => setNewNote(e.target.value)}
            placeholder='Enter your note here...'
            rows={4}
            required
          />

          <div className='flex items-center space-x-3'>
            <input
              type='checkbox'
              id='isInternal'
              checked={isInternalNote}
              onChange={e => setIsInternalNote(e.target.checked)}
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
            />
            <label
              htmlFor='isInternal'
              className='text-sm font-medium text-gray-700'
            >
              Internal note (not visible to citizen)
            </label>
          </div>

          <div className='text-sm text-gray-600'>
            {isInternalNote ? (
              <p>This note will only be visible to staff members.</p>
            ) : (
              <p>
                This note will be visible to the citizen who submitted the case.
              </p>
            )}
          </div>
        </div>

        <div className='flex justify-end space-x-3 mt-6'>
          <Button
            variant='outline'
            onClick={() => setIsAddNoteModalOpen(false)}
          >
            Cancel
          </Button>
          <Button onClick={handleAddNote} disabled={!newNote.trim()}>
            Add Note
          </Button>
        </div>
      </Modal>
    </div>
  );
}
