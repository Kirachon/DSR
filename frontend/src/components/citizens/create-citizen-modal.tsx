'use client';

// Create Citizen Modal Component
// Modal for creating new citizen records

import React, { useState } from 'react';

import { Modal, Button, Alert } from '@/components/ui';
import { FormInput, FormSelect, FormTextarea } from '@/components/forms';
import type { CreateCitizenRequest } from '@/types';

// Create citizen modal props interface
interface CreateCitizenModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateCitizenRequest) => Promise<void>;
}

// Form validation
interface FormErrors {
  [key: string]: string;
}

// Create Citizen Modal component
export const CreateCitizenModal: React.FC<CreateCitizenModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  // State management
  const [formData, setFormData] = useState<CreateCitizenRequest>({
    firstName: '',
    lastName: '',
    middleName: '',
    email: '',
    phoneNumber: '',
    dateOfBirth: '',
    gender: '',
    address: {
      street: '',
      barangay: '',
      municipality: '',
      province: '',
      zipCode: '',
    },
    emergencyContact: {
      name: '',
      relationship: '',
      phoneNumber: '',
    },
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);

  // Handle form field change
  const handleFieldChange = (field: string, value: string) => {
    if (field.includes('.')) {
      const [parent, child] = field.split('.');
      setFormData(prev => ({
        ...prev,
        [parent]: {
          ...prev[parent as keyof CreateCitizenRequest] as any,
          [child]: value,
        },
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [field]: value,
      }));
    }

    // Clear error for this field
    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: '',
      }));
    }
  };

  // Validate form
  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    // Required fields
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    }
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }
    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = 'Phone number is required';
    }
    if (!formData.dateOfBirth) {
      newErrors.dateOfBirth = 'Date of birth is required';
    }
    if (!formData.gender) {
      newErrors.gender = 'Gender is required';
    }

    // Address validation
    if (!formData.address.street.trim()) {
      newErrors['address.street'] = 'Street address is required';
    }
    if (!formData.address.barangay.trim()) {
      newErrors['address.barangay'] = 'Barangay is required';
    }
    if (!formData.address.municipality.trim()) {
      newErrors['address.municipality'] = 'Municipality is required';
    }
    if (!formData.address.province.trim()) {
      newErrors['address.province'] = 'Province is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);
      await onSubmit(formData);
      handleClose();
    } catch (error) {
      console.error('Failed to create citizen:', error);
    } finally {
      setLoading(false);
    }
  };

  // Handle modal close
  const handleClose = () => {
    setFormData({
      firstName: '',
      lastName: '',
      middleName: '',
      email: '',
      phoneNumber: '',
      dateOfBirth: '',
      gender: '',
      address: {
        street: '',
        barangay: '',
        municipality: '',
        province: '',
        zipCode: '',
      },
      emergencyContact: {
        name: '',
        relationship: '',
        phoneNumber: '',
      },
    });
    setErrors({});
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Create New Citizen">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Personal Information */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">Personal Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <FormInput
              label="First Name *"
              value={formData.firstName}
              onChange={(e) => handleFieldChange('firstName', e.target.value)}
              error={errors.firstName}
              required
            />
            <FormInput
              label="Last Name *"
              value={formData.lastName}
              onChange={(e) => handleFieldChange('lastName', e.target.value)}
              error={errors.lastName}
              required
            />
            <FormInput
              label="Middle Name"
              value={formData.middleName}
              onChange={(e) => handleFieldChange('middleName', e.target.value)}
            />
            <FormSelect
              label="Gender *"
              value={formData.gender}
              onChange={(e) => handleFieldChange('gender', e.target.value)}
              error={errors.gender}
              options={[
                { value: '', label: 'Select Gender' },
                { value: 'MALE', label: 'Male' },
                { value: 'FEMALE', label: 'Female' },
                { value: 'OTHER', label: 'Other' },
              ]}
              required
            />
            <FormInput
              label="Date of Birth *"
              type="date"
              value={formData.dateOfBirth}
              onChange={(e) => handleFieldChange('dateOfBirth', e.target.value)}
              error={errors.dateOfBirth}
              required
            />
            <FormInput
              label="Email *"
              type="email"
              value={formData.email}
              onChange={(e) => handleFieldChange('email', e.target.value)}
              error={errors.email}
              required
            />
            <FormInput
              label="Phone Number *"
              value={formData.phoneNumber}
              onChange={(e) => handleFieldChange('phoneNumber', e.target.value)}
              error={errors.phoneNumber}
              placeholder="+639XXXXXXXXX"
              required
            />
          </div>
        </div>

        {/* Address Information */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">Address Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <FormInput
              label="Street Address *"
              value={formData.address.street}
              onChange={(e) => handleFieldChange('address.street', e.target.value)}
              error={errors['address.street']}
              required
            />
            <FormInput
              label="Barangay *"
              value={formData.address.barangay}
              onChange={(e) => handleFieldChange('address.barangay', e.target.value)}
              error={errors['address.barangay']}
              required
            />
            <FormInput
              label="Municipality/City *"
              value={formData.address.municipality}
              onChange={(e) => handleFieldChange('address.municipality', e.target.value)}
              error={errors['address.municipality']}
              required
            />
            <FormInput
              label="Province *"
              value={formData.address.province}
              onChange={(e) => handleFieldChange('address.province', e.target.value)}
              error={errors['address.province']}
              required
            />
            <FormInput
              label="ZIP Code"
              value={formData.address.zipCode}
              onChange={(e) => handleFieldChange('address.zipCode', e.target.value)}
            />
          </div>
        </div>

        {/* Emergency Contact */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">Emergency Contact</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <FormInput
              label="Contact Name"
              value={formData.emergencyContact.name}
              onChange={(e) => handleFieldChange('emergencyContact.name', e.target.value)}
            />
            <FormInput
              label="Relationship"
              value={formData.emergencyContact.relationship}
              onChange={(e) => handleFieldChange('emergencyContact.relationship', e.target.value)}
              placeholder="e.g., Spouse, Parent, Sibling"
            />
            <FormInput
              label="Phone Number"
              value={formData.emergencyContact.phoneNumber}
              onChange={(e) => handleFieldChange('emergencyContact.phoneNumber', e.target.value)}
              placeholder="+639XXXXXXXXX"
            />
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
          <Button type="button" variant="outline" onClick={handleClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Create Citizen'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
