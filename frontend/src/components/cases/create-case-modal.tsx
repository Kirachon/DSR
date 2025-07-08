'use client';

// Create Case Modal Component
// Modal for creating new grievance cases

import React, { useState } from 'react';

import { FormInput, FormSelect, FormTextarea } from '@/components/forms';
import { Modal, Button, Alert } from '@/components/ui';
import type {
  CreateCaseRequest,
  User,
  CaseType,
  CasePriority,
  CaseCategory,
} from '@/types';

// Component props interface
interface CreateCaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (caseData: CreateCaseRequest) => Promise<void>;
  currentUser: User | null;
}

// Form options
const TYPE_OPTIONS = [
  { value: '', label: 'Select Case Type' },
  { value: 'GRIEVANCE', label: 'Grievance' },
  { value: 'APPEAL', label: 'Appeal' },
  { value: 'INQUIRY', label: 'Inquiry' },
  { value: 'COMPLAINT', label: 'Complaint' },
  { value: 'FEEDBACK', label: 'Feedback' },
  { value: 'REQUEST', label: 'Request' },
];

const PRIORITY_OPTIONS = [
  { value: '', label: 'Select Priority' },
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'URGENT', label: 'Urgent' },
  { value: 'CRITICAL', label: 'Critical' },
];

const CATEGORY_OPTIONS = [
  { value: '', label: 'Select Category' },
  { value: 'PAYMENT_ISSUES', label: 'Payment Issues' },
  { value: 'ELIGIBILITY_ISSUES', label: 'Eligibility Issues' },
  { value: 'REGISTRATION_ISSUES', label: 'Registration Issues' },
  { value: 'DOCUMENT_ISSUES', label: 'Document Issues' },
  { value: 'SERVICE_QUALITY', label: 'Service Quality' },
  { value: 'SYSTEM_ISSUES', label: 'System Issues' },
  { value: 'POLICY_CLARIFICATION', label: 'Policy Clarification' },
  { value: 'DISCRIMINATION', label: 'Discrimination' },
  { value: 'CORRUPTION', label: 'Corruption' },
  { value: 'OTHER', label: 'Other' },
];

const CONTACT_METHOD_OPTIONS = [
  { value: '', label: 'Select Preferred Contact Method' },
  { value: 'EMAIL', label: 'Email' },
  { value: 'PHONE', label: 'Phone' },
  { value: 'SMS', label: 'SMS' },
  { value: 'IN_PERSON', label: 'In Person' },
];

// Initial form data
const initialFormData: CreateCaseRequest = {
  title: '',
  description: '',
  type: '' as CaseType,
  category: '' as CaseCategory,
  priority: '' as CasePriority,
  submittedBy: '',
  submittedById: '',
  contactEmail: '',
  contactPhone: '',
  preferredContactMethod: undefined,
  attachments: [],
  tags: [],
  citizenId: '',
  householdId: '',
  beneficiaryId: '',
};

// Create Case Modal component
export const CreateCaseModal: React.FC<CreateCaseModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  currentUser,
}) => {
  const [formData, setFormData] = useState<CreateCaseRequest>(initialFormData);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});

  // Handle form field changes
  const handleFieldChange = (field: keyof CreateCaseRequest, value: any) => {
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

  // Validate form
  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.title.trim()) {
      errors.title = 'Title is required';
    }

    if (!formData.description.trim()) {
      errors.description = 'Description is required';
    }

    if (!formData.type) {
      errors.type = 'Case type is required';
    }

    if (!formData.category) {
      errors.category = 'Category is required';
    }

    if (!formData.priority) {
      errors.priority = 'Priority is required';
    }

    if (!formData.submittedBy.trim()) {
      errors.submittedBy = 'Submitted by is required';
    }

    if (
      formData.contactEmail &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.contactEmail)
    ) {
      errors.contactEmail = 'Invalid email format';
    }

    if (
      formData.contactPhone &&
      !/^[\+]?[0-9\s\-\(\)]+$/.test(formData.contactPhone)
    ) {
      errors.contactPhone = 'Invalid phone number format';
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
      await onSubmit({
        ...formData,
        submittedById: currentUser?.id || 'unknown',
      });

      // Reset form
      setFormData(initialFormData);
      setValidationErrors({});
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create case');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle modal close
  const handleClose = () => {
    if (!isSubmitting) {
      setFormData(initialFormData);
      setValidationErrors({});
      setError(null);
      onClose();
    }
  };

  // Auto-fill user information
  React.useEffect(() => {
    if (currentUser && !formData.submittedBy) {
      setFormData(prev => ({
        ...prev,
        submittedBy: `${currentUser.firstName} ${currentUser.lastName}`,
        submittedById: currentUser.id,
        contactEmail: currentUser.email || '',
      }));
    }
  }, [currentUser, formData.submittedBy]);

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title='Create New Case'
      size='lg'
    >
      <div className='space-y-6'>
        {/* Error Alert */}
        {error && (
          <Alert variant='error' title='Error Creating Case'>
            {error}
          </Alert>
        )}

        {/* Basic Information */}
        <div>
          <h4 className='text-md font-medium text-gray-900 mb-4'>
            Case Information
          </h4>
          <div className='space-y-4'>
            <FormInput
              label='Case Title *'
              value={formData.title}
              onChange={value => handleFieldChange('title', value)}
              placeholder='Brief description of the issue'
              error={validationErrors.title}
              required
            />

            <FormTextarea
              label='Description *'
              value={formData.description}
              onChange={value => handleFieldChange('description', value)}
              placeholder='Detailed description of the case, including relevant background information...'
              rows={4}
              error={validationErrors.description}
              required
            />

            <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
              <FormSelect
                label='Type *'
                value={formData.type}
                onChange={value => handleFieldChange('type', value)}
                options={TYPE_OPTIONS}
                error={validationErrors.type}
                required
              />

              <FormSelect
                label='Category *'
                value={formData.category}
                onChange={value => handleFieldChange('category', value)}
                options={CATEGORY_OPTIONS}
                error={validationErrors.category}
                required
              />

              <FormSelect
                label='Priority *'
                value={formData.priority}
                onChange={value => handleFieldChange('priority', value)}
                options={PRIORITY_OPTIONS}
                error={validationErrors.priority}
                required
              />
            </div>
          </div>
        </div>

        {/* Submitter Information */}
        <div>
          <h4 className='text-md font-medium text-gray-900 mb-4'>
            Submitter Information
          </h4>
          <div className='space-y-4'>
            <FormInput
              label='Submitted By *'
              value={formData.submittedBy}
              onChange={value => handleFieldChange('submittedBy', value)}
              placeholder='Name of person submitting the case'
              error={validationErrors.submittedBy}
              required
            />

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <FormInput
                label='Contact Email'
                type='email'
                value={formData.contactEmail}
                onChange={value => handleFieldChange('contactEmail', value)}
                placeholder='email@example.com'
                error={validationErrors.contactEmail}
              />

              <FormInput
                label='Contact Phone'
                value={formData.contactPhone}
                onChange={value => handleFieldChange('contactPhone', value)}
                placeholder='+63 9XX XXX XXXX'
                error={validationErrors.contactPhone}
              />
            </div>

            <FormSelect
              label='Preferred Contact Method'
              value={formData.preferredContactMethod || ''}
              onChange={value =>
                handleFieldChange('preferredContactMethod', value || undefined)
              }
              options={CONTACT_METHOD_OPTIONS}
            />
          </div>
        </div>

        {/* Additional Information */}
        <div>
          <h4 className='text-md font-medium text-gray-900 mb-4'>
            Additional Information (Optional)
          </h4>
          <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
            <FormInput
              label='Citizen ID'
              value={formData.citizenId}
              onChange={value => handleFieldChange('citizenId', value)}
              placeholder='Citizen identifier'
            />

            <FormInput
              label='Household ID'
              value={formData.householdId}
              onChange={value => handleFieldChange('householdId', value)}
              placeholder='Household identifier'
            />

            <FormInput
              label='Beneficiary ID'
              value={formData.beneficiaryId}
              onChange={value => handleFieldChange('beneficiaryId', value)}
              placeholder='Beneficiary identifier'
            />
          </div>
        </div>

        {/* Case Type Information */}
        {formData.type && (
          <div className='bg-blue-50 border border-blue-200 rounded-lg p-4'>
            <h4 className='text-sm font-medium text-blue-800 mb-2'>
              {TYPE_OPTIONS.find(opt => opt.value === formData.type)?.label}{' '}
              Information
            </h4>
            <div className='text-sm text-blue-700'>
              {formData.type === 'GRIEVANCE' && (
                <p>
                  A grievance is a formal complaint about a service, decision,
                  or treatment received. Please provide detailed information
                  about the issue and any attempts to resolve it.
                </p>
              )}
              {formData.type === 'APPEAL' && (
                <p>
                  An appeal is a request to review a decision that you believe
                  is incorrect. Please include the original decision details and
                  reasons for the appeal.
                </p>
              )}
              {formData.type === 'INQUIRY' && (
                <p>
                  An inquiry is a request for information about programs,
                  services, or policies. Please specify what information you
                  need.
                </p>
              )}
              {formData.type === 'COMPLAINT' && (
                <p>
                  A complaint is a formal expression of dissatisfaction about
                  service quality or staff conduct. Please provide specific
                  details and any evidence.
                </p>
              )}
              {formData.type === 'FEEDBACK' && (
                <p>
                  Feedback includes suggestions, comments, or recommendations
                  for improving services. Your input helps us serve you better.
                </p>
              )}
              {formData.type === 'REQUEST' && (
                <p>
                  A request is for specific assistance or services. Please
                  clearly state what you need and any relevant circumstances.
                </p>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Modal Actions */}
      <div className='flex justify-end space-x-3 mt-6'>
        <Button variant='outline' onClick={handleClose} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button onClick={handleSubmit} disabled={isSubmitting}>
          {isSubmitting ? 'Creating...' : 'Create Case'}
        </Button>
      </div>
    </Modal>
  );
};
