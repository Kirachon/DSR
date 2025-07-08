'use client';

// Create Payment Batch Modal Component
// Modal for creating new payment batches

import React, { useState } from 'react';

import { FormInput, FormSelect, FormTextarea } from '@/components/forms';
import { Modal, Button, Alert } from '@/components/ui';
import type { CreatePaymentBatchRequest, User, PaymentMethod } from '@/types';

// Component props interface
interface CreatePaymentBatchModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (batchData: CreatePaymentBatchRequest) => Promise<void>;
  currentUser: User | null;
}

// Form options
const PROGRAM_OPTIONS = [
  { value: '', label: 'Select Program' },
  { value: '4Ps', label: '4Ps (Pantawid Pamilyang Pilipino Program)' },
  { value: 'SENIOR_CITIZEN', label: 'Senior Citizen Pension' },
  { value: 'PWD_ALLOWANCE', label: 'PWD Allowance' },
  { value: 'EMERGENCY_SUBSIDY', label: 'Emergency Subsidy' },
  { value: 'LIVELIHOOD_ASSISTANCE', label: 'Livelihood Assistance' },
  { value: 'EDUCATIONAL_ASSISTANCE', label: 'Educational Assistance' },
  { value: 'MEDICAL_ASSISTANCE', label: 'Medical Assistance' },
  { value: 'OTHER', label: 'Other' },
];

const PAYMENT_METHOD_OPTIONS = [
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
  { value: 'DIGITAL_WALLET', label: 'Digital Wallet' },
  { value: 'CASH_PICKUP', label: 'Cash Pickup' },
  { value: 'CHECK', label: 'Check' },
  { value: 'PREPAID_CARD', label: 'Prepaid Card' },
];

// Initial form data
const initialFormData: CreatePaymentBatchRequest = {
  name: '',
  description: '',
  program: '',
  scheduledDate: '',
  beneficiaries: [],
  notes: '',
  tags: [],
};

// Sample beneficiary data for demo
const sampleBeneficiaries = [
  {
    beneficiaryId: 'BEN-001',
    name: 'Maria Santos',
    amount: 1500.0,
    paymentMethod: 'BANK_TRANSFER' as PaymentMethod,
    bankAccount: {
      accountNumber: '1234567890',
      bankName: 'BDO',
      accountName: 'Maria Santos',
    },
  },
  {
    beneficiaryId: 'BEN-002',
    name: 'Juan Dela Cruz',
    amount: 1800.0,
    paymentMethod: 'DIGITAL_WALLET' as PaymentMethod,
    digitalWallet: {
      walletType: 'GCASH' as const,
      walletNumber: '09123456789',
      accountName: 'Juan Dela Cruz',
    },
  },
  {
    beneficiaryId: 'BEN-003',
    name: 'Ana Garcia',
    amount: 500.0,
    paymentMethod: 'CASH_PICKUP' as PaymentMethod,
    cashPickup: {
      location: 'Barangay Hall - Brgy. San Jose',
      pickupCode: 'PICKUP-789',
    },
  },
];

// Create Payment Batch Modal component
export const CreatePaymentBatchModal: React.FC<
  CreatePaymentBatchModalProps
> = ({ isOpen, onClose, onSubmit, currentUser }) => {
  const [formData, setFormData] =
    useState<CreatePaymentBatchRequest>(initialFormData);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});
  const [selectedBeneficiaries, setSelectedBeneficiaries] = useState<string[]>(
    []
  );

  // Handle form field changes
  const handleFieldChange = (
    field: keyof CreatePaymentBatchRequest,
    value: any
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));

    // Clear validation error for this field
    if (validationErrors[field]) {
      setValidationErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  // Handle beneficiary selection
  const handleBeneficiaryToggle = (beneficiaryId: string) => {
    setSelectedBeneficiaries(prev => {
      if (prev.includes(beneficiaryId)) {
        return prev.filter(id => id !== beneficiaryId);
      } else {
        return [...prev, beneficiaryId];
      }
    });
  };

  // Validate form
  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.name.trim()) {
      errors.name = 'Batch name is required';
    }

    if (!formData.program) {
      errors.program = 'Program is required';
    }

    if (!formData.scheduledDate) {
      errors.scheduledDate = 'Scheduled date is required';
    }

    if (selectedBeneficiaries.length === 0) {
      errors.beneficiaries = 'At least one beneficiary must be selected';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async () => {
    setError(null);

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      // Prepare beneficiaries data
      const beneficiaries = selectedBeneficiaries.map(beneficiaryId => {
        const beneficiary = sampleBeneficiaries.find(
          b => b.beneficiaryId === beneficiaryId
        );
        if (!beneficiary)
          throw new Error(`Beneficiary ${beneficiaryId} not found`);

        return {
          beneficiaryId: beneficiary.beneficiaryId,
          amount: beneficiary.amount,
          paymentMethod: beneficiary.paymentMethod,
          bankAccount: beneficiary.bankAccount,
          digitalWallet: beneficiary.digitalWallet,
          cashPickup: beneficiary.cashPickup,
        };
      });

      await onSubmit({
        ...formData,
        beneficiaries,
        tags: formData.tags || [],
      });

      // Reset form
      setFormData(initialFormData);
      setSelectedBeneficiaries([]);
      setValidationErrors({});
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Failed to create payment batch'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle modal close
  const handleClose = () => {
    if (!isSubmitting) {
      setFormData(initialFormData);
      setSelectedBeneficiaries([]);
      setValidationErrors({});
      setError(null);
      onClose();
    }
  };

  // Calculate totals
  const totalBeneficiaries = selectedBeneficiaries.length;
  const totalAmount = selectedBeneficiaries.reduce((sum, beneficiaryId) => {
    const beneficiary = sampleBeneficiaries.find(
      b => b.beneficiaryId === beneficiaryId
    );
    return sum + (beneficiary?.amount || 0);
  }, 0);

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title='Create Payment Batch'
      size='lg'
    >
      <div className='space-y-6'>
        {/* Error Alert */}
        {error && (
          <Alert variant='error' title='Error Creating Payment Batch'>
            {error}
          </Alert>
        )}

        {/* Batch Information */}
        <div>
          <h4 className='text-md font-medium text-gray-900 mb-4'>
            Batch Information
          </h4>
          <div className='space-y-4'>
            <FormInput
              label='Batch Name *'
              value={formData.name}
              onChange={value => handleFieldChange('name', value)}
              placeholder='e.g., 4Ps January 2024 Disbursement'
              error={validationErrors.name}
              required
            />

            <FormTextarea
              label='Description'
              value={formData.description}
              onChange={value => handleFieldChange('description', value)}
              placeholder='Optional description of the payment batch...'
              rows={3}
            />

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <FormSelect
                label='Program *'
                value={formData.program}
                onChange={value => handleFieldChange('program', value)}
                options={PROGRAM_OPTIONS}
                error={validationErrors.program}
                required
              />

              <FormInput
                label='Scheduled Date *'
                type='datetime-local'
                value={formData.scheduledDate}
                onChange={value => handleFieldChange('scheduledDate', value)}
                error={validationErrors.scheduledDate}
                required
              />
            </div>
          </div>
        </div>

        {/* Beneficiary Selection */}
        <div>
          <h4 className='text-md font-medium text-gray-900 mb-4'>
            Select Beneficiaries
            {validationErrors.beneficiaries && (
              <span className='text-sm text-red-600 ml-2'>
                ({validationErrors.beneficiaries})
              </span>
            )}
          </h4>

          <div className='border border-gray-200 rounded-lg max-h-64 overflow-y-auto'>
            {sampleBeneficiaries.map(beneficiary => (
              <div
                key={beneficiary.beneficiaryId}
                className={`p-4 border-b border-gray-200 last:border-b-0 ${
                  selectedBeneficiaries.includes(beneficiary.beneficiaryId)
                    ? 'bg-primary-50 border-primary-200'
                    : 'hover:bg-gray-50'
                }`}
              >
                <div className='flex items-center space-x-3'>
                  <input
                    type='checkbox'
                    id={beneficiary.beneficiaryId}
                    checked={selectedBeneficiaries.includes(
                      beneficiary.beneficiaryId
                    )}
                    onChange={() =>
                      handleBeneficiaryToggle(beneficiary.beneficiaryId)
                    }
                    className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
                  />
                  <div className='flex-1'>
                    <div className='flex items-center justify-between'>
                      <div>
                        <h5 className='font-medium text-gray-900'>
                          {beneficiary.name}
                        </h5>
                        <p className='text-sm text-gray-600'>
                          ID: {beneficiary.beneficiaryId}
                        </p>
                      </div>
                      <div className='text-right'>
                        <p className='font-medium text-gray-900'>
                          ₱{beneficiary.amount.toLocaleString()}
                        </p>
                        <p className='text-sm text-gray-600'>
                          {beneficiary.paymentMethod.replace('_', ' ')}
                        </p>
                      </div>
                    </div>

                    {/* Payment Method Details */}
                    <div className='mt-2 text-xs text-gray-500'>
                      {beneficiary.bankAccount && (
                        <span>
                          Bank: {beneficiary.bankAccount.bankName} -{' '}
                          {beneficiary.bankAccount.accountNumber}
                        </span>
                      )}
                      {beneficiary.digitalWallet && (
                        <span>
                          Wallet: {beneficiary.digitalWallet.walletType} -{' '}
                          {beneficiary.digitalWallet.walletNumber}
                        </span>
                      )}
                      {beneficiary.cashPickup && (
                        <span>Pickup: {beneficiary.cashPickup.location}</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Selection Summary */}
          {totalBeneficiaries > 0 && (
            <div className='mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg'>
              <div className='flex justify-between items-center'>
                <div>
                  <h5 className='font-medium text-blue-900'>
                    Selection Summary
                  </h5>
                  <p className='text-sm text-blue-700'>
                    {totalBeneficiaries} beneficiar
                    {totalBeneficiaries === 1 ? 'y' : 'ies'} selected
                  </p>
                </div>
                <div className='text-right'>
                  <p className='font-medium text-blue-900'>
                    Total: ₱{totalAmount.toLocaleString()}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Additional Information */}
        <div>
          <h4 className='text-md font-medium text-gray-900 mb-4'>
            Additional Information
          </h4>
          <FormTextarea
            label='Notes'
            value={formData.notes}
            onChange={value => handleFieldChange('notes', value)}
            placeholder='Optional notes about this payment batch...'
            rows={3}
          />
        </div>

        {/* Batch Preview */}
        {formData.program && totalBeneficiaries > 0 && (
          <div className='bg-gray-50 border border-gray-200 rounded-lg p-4'>
            <h4 className='text-sm font-medium text-gray-900 mb-2'>
              Batch Preview
            </h4>
            <div className='grid grid-cols-2 gap-4 text-sm'>
              <div>
                <span className='text-gray-600'>Program:</span>
                <p className='font-medium'>
                  {
                    PROGRAM_OPTIONS.find(opt => opt.value === formData.program)
                      ?.label
                  }
                </p>
              </div>
              <div>
                <span className='text-gray-600'>Scheduled Date:</span>
                <p className='font-medium'>
                  {formData.scheduledDate
                    ? new Date(formData.scheduledDate).toLocaleString()
                    : 'Not set'}
                </p>
              </div>
              <div>
                <span className='text-gray-600'>Total Beneficiaries:</span>
                <p className='font-medium'>{totalBeneficiaries}</p>
              </div>
              <div>
                <span className='text-gray-600'>Total Amount:</span>
                <p className='font-medium'>₱{totalAmount.toLocaleString()}</p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Modal Actions */}
      <div className='flex justify-end space-x-3 mt-6'>
        <Button variant='outline' onClick={handleClose} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          disabled={isSubmitting || totalBeneficiaries === 0}
        >
          {isSubmitting
            ? 'Creating...'
            : `Create Batch (${totalBeneficiaries} payments)`}
        </Button>
      </div>
    </Modal>
  );
};
