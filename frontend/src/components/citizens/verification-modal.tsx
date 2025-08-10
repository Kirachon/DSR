'use client';

import React, { useState } from 'react';
import { Modal } from '@/components/ui/modal';
import { Button } from '@/components/ui/button';
import { StatusBadge } from '@/components/ui/status-badge';
import { FormTextarea } from '@/components/forms/form-textarea';

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

interface VerificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  verification: VerificationRecord | null;
  onVerify: (id: string, notes?: string) => void;
  onReject: (id: string, notes: string) => void;
}

export function VerificationModal({ 
  isOpen, 
  onClose, 
  verification, 
  onVerify, 
  onReject 
}: VerificationModalProps) {
  const [notes, setNotes] = useState('');
  const [action, setAction] = useState<'verify' | 'reject' | null>(null);

  if (!verification) return null;

  const handleSubmit = () => {
    if (action === 'verify') {
      onVerify(verification.id, notes);
    } else if (action === 'reject') {
      onReject(verification.id, notes);
    }
    setNotes('');
    setAction(null);
    onClose();
  };

  const handleActionClick = (selectedAction: 'verify' | 'reject') => {
    setAction(selectedAction);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Verification Details" size="lg">
      <div className="space-y-6">
        {/* Citizen Information */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Citizen Name</label>
            <p className="mt-1 text-sm text-gray-900">{verification.citizenName}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Citizen ID</label>
            <p className="mt-1 text-sm text-gray-900">{verification.citizenId}</p>
          </div>
        </div>

        {/* Verification Details */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Verification Type</label>
            <p className="mt-1 text-sm text-gray-900">{verification.verificationType}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Status</label>
            <div className="mt-1">
              <StatusBadge status={verification.status} />
            </div>
          </div>
        </div>

        {/* Dates */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Submitted Date</label>
            <p className="mt-1 text-sm text-gray-900">
              {new Date(verification.submittedDate).toLocaleDateString()}
            </p>
          </div>
          {verification.verifiedDate && (
            <div>
              <label className="block text-sm font-medium text-gray-700">Verified Date</label>
              <p className="mt-1 text-sm text-gray-900">
                {new Date(verification.verifiedDate).toLocaleDateString()}
              </p>
            </div>
          )}
        </div>

        {/* Documents */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Documents</label>
          <div className="space-y-2">
            {verification.documents.map((doc, index) => (
              <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                <span className="text-sm text-gray-900">{doc}</span>
                <Button size="sm" variant="outline">
                  View
                </Button>
              </div>
            ))}
          </div>
        </div>

        {/* Existing Notes */}
        {verification.notes && (
          <div>
            <label className="block text-sm font-medium text-gray-700">Existing Notes</label>
            <p className="mt-1 text-sm text-gray-900 p-3 bg-gray-50 rounded">
              {verification.notes}
            </p>
          </div>
        )}

        {/* Action Section */}
        {verification.status === 'PENDING' && (
          <div className="border-t pt-4">
            {!action ? (
              <div className="flex space-x-2">
                <Button onClick={() => handleActionClick('verify')}>
                  Verify
                </Button>
                <Button variant="destructive" onClick={() => handleActionClick('reject')}>
                  Reject
                </Button>
              </div>
            ) : (
              <div className="space-y-4">
                <FormTextarea
                  label={action === 'verify' ? 'Verification Notes (Optional)' : 'Rejection Reason (Required)'}
                  value={notes}
                  onChange={setNotes}
                  placeholder={
                    action === 'verify' 
                      ? 'Add any notes about the verification...'
                      : 'Please provide a reason for rejection...'
                  }
                  required={action === 'reject'}
                />
                <div className="flex space-x-2">
                  <Button 
                    onClick={handleSubmit}
                    disabled={action === 'reject' && !notes.trim()}
                  >
                    {action === 'verify' ? 'Confirm Verification' : 'Confirm Rejection'}
                  </Button>
                  <Button variant="outline" onClick={() => setAction(null)}>
                    Cancel
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Close Button */}
        <div className="flex justify-end pt-4 border-t">
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </Modal>
  );
}

export default VerificationModal;
