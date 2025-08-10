'use client';

// Registration Review Modal Component
// Modal for reviewing and approving/rejecting registration applications

import React, { useState } from 'react';

import { Modal, Button, Badge } from '@/components/ui';
import type { CitizenRegistration } from '@/types';

// Registration review modal props interface
interface RegistrationReviewModalProps {
  isOpen: boolean;
  registration: CitizenRegistration | null;
  onClose: () => void;
  onDecision: (registrationId: string, decision: 'APPROVED' | 'REJECTED', comments?: string) => Promise<void>;
}

// Registration Review Modal component
export const RegistrationReviewModal: React.FC<RegistrationReviewModalProps> = ({
  isOpen,
  registration,
  onClose,
  onDecision,
}) => {
  const [comments, setComments] = useState('');
  const [loading, setLoading] = useState(false);

  const handleDecision = async (decision: 'APPROVED' | 'REJECTED') => {
    if (!registration) return;

    try {
      setLoading(true);
      await onDecision(registration.id, decision, comments);
      setComments('');
      onClose();
    } catch (error) {
      console.error('Failed to process decision:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!registration) {
    return null;
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Review Registration Application" size="large">
      <div className="space-y-6">
        {/* Application Header */}
        <div className="bg-gray-50 p-4 rounded-lg">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-lg font-medium text-gray-900">{registration.applicantName}</h3>
              <p className="text-sm text-gray-600">Application ID: {registration.id}</p>
            </div>
            <Badge variant={registration.status === 'PENDING' ? 'warning' : 'info'}>
              {registration.status}
            </Badge>
          </div>
        </div>

        {/* Applicant Information */}
        <div>
          <h4 className="text-md font-medium text-gray-900 mb-3">Applicant Information</h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <span className="text-sm font-medium text-gray-700">Name:</span>
              <span className="ml-2 text-sm text-gray-900">{registration.applicantName}</span>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-700">Email:</span>
              <span className="ml-2 text-sm text-gray-900">{registration.applicantEmail}</span>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-700">Phone:</span>
              <span className="ml-2 text-sm text-gray-900">{registration.phoneNumber}</span>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-700">Submission Date:</span>
              <span className="ml-2 text-sm text-gray-900">
                {new Date(registration.submissionDate).toLocaleDateString()}
              </span>
            </div>
          </div>
        </div>

        {/* Household Information */}
        <div>
          <h4 className="text-md font-medium text-gray-900 mb-3">Household Information</h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <span className="text-sm font-medium text-gray-700">Household Size:</span>
              <span className="ml-2 text-sm text-gray-900">{registration.householdSize}</span>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-700">Monthly Income:</span>
              <span className="ml-2 text-sm text-gray-900">
                ₱{registration.monthlyIncome?.toLocaleString() || 'N/A'}
              </span>
            </div>
          </div>
        </div>

        {/* Address Information */}
        {registration.address && (
          <div>
            <h4 className="text-md font-medium text-gray-900 mb-3">Address</h4>
            <div className="bg-gray-50 p-3 rounded">
              <p className="text-sm text-gray-900">
                {registration.address.street}, {registration.address.barangay}
                <br />
                {registration.address.municipality}, {registration.address.province}
                {registration.address.zipCode && ` ${registration.address.zipCode}`}
              </p>
            </div>
          </div>
        )}

        {/* Documents */}
        {registration.documents && registration.documents.length > 0 && (
          <div>
            <h4 className="text-md font-medium text-gray-900 mb-3">Submitted Documents</h4>
            <div className="space-y-2">
              {registration.documents.map((doc, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded">
                  <div>
                    <span className="text-sm font-medium text-gray-900">{doc.type}</span>
                    <Badge variant={doc.status === 'SUBMITTED' ? 'info' : 'success'} size="sm" className="ml-2">
                      {doc.status}
                    </Badge>
                  </div>
                  {doc.url && (
                    <Button size="sm" variant="outline" onClick={() => window.open(doc.url!, '_blank')}>
                      View
                    </Button>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Review Comments */}
        <div>
          <h4 className="text-md font-medium text-gray-900 mb-3">Review Comments</h4>
          <textarea
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            placeholder="Add comments about this application..."
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
          />
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button
            variant="outline"
            onClick={() => handleDecision('REJECTED')}
            disabled={loading}
            className="text-red-600 hover:text-red-700 border-red-300 hover:border-red-400"
          >
            {loading ? 'Processing...' : 'Reject'}
          </Button>
          <Button
            onClick={() => handleDecision('APPROVED')}
            disabled={loading}
            className="bg-green-600 hover:bg-green-700 text-white"
          >
            {loading ? 'Processing...' : 'Approve'}
          </Button>
        </div>
      </div>
    </Modal>
  );
};
