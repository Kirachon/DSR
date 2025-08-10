'use client';

// Create Household Modal Component
// Modal for creating new household records

import React, { useState } from 'react';

import { FormInput, FormSelect, FormTextarea } from '@/components/forms';
import { Modal, Button, Alert } from '@/components/ui';
import type { User } from '@/types';

// Component props interface
interface CreateHouseholdModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (householdData: any) => Promise<void>;
  currentUser: User;
}

// Form data interface
interface HouseholdFormData {
  // Household Head Information
  headFirstName: string;
  headLastName: string;
  headMiddleName: string;
  headPsn: string;
  headBirthDate: string;
  headGender: string;
  headCivilStatus: string;
  headNationality: string;
  headOccupation: string;
  headMonthlyIncome: string;
  headEducationLevel: string;

  // Address Information
  region: string;
  province: string;
  municipality: string;
  barangay: string;
  streetAddress: string;
  zipCode: string;

  // Household Information
  householdNumber: string;
  totalMembers: string;
  monthlyIncome: string;
  housingType: string;
  housingTenure: string;
  waterSource: string;
  toiletFacility: string;
  electricitySource: string;
  cookingFuel: string;
  preferredLanguage: string;
  notes: string;
}

// Initial form data
const initialFormData: HouseholdFormData = {
  headFirstName: '',
  headLastName: '',
  headMiddleName: '',
  headPsn: '',
  headBirthDate: '',
  headGender: '',
  headCivilStatus: '',
  headNationality: 'Filipino',
  headOccupation: '',
  headMonthlyIncome: '',
  headEducationLevel: '',
  region: '',
  province: '',
  municipality: '',
  barangay: '',
  streetAddress: '',
  zipCode: '',
  householdNumber: '',
  totalMembers: '1',
  monthlyIncome: '',
  housingType: '',
  housingTenure: '',
  waterSource: '',
  toiletFacility: '',
  electricitySource: '',
  cookingFuel: '',
  preferredLanguage: 'Filipino',
  notes: '',
};

// Gender options
const GENDER_OPTIONS = [
  { value: '', label: 'Select Gender' },
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
];

// Civil status options
const CIVIL_STATUS_OPTIONS = [
  { value: '', label: 'Select Civil Status' },
  { value: 'SINGLE', label: 'Single' },
  { value: 'MARRIED', label: 'Married' },
  { value: 'WIDOWED', label: 'Widowed' },
  { value: 'SEPARATED', label: 'Separated' },
  { value: 'DIVORCED', label: 'Divorced' },
];

// Education level options
const EDUCATION_OPTIONS = [
  { value: '', label: 'Select Education Level' },
  { value: 'NO_FORMAL_EDUCATION', label: 'No Formal Education' },
  { value: 'ELEMENTARY_UNDERGRADUATE', label: 'Elementary Undergraduate' },
  { value: 'ELEMENTARY_GRADUATE', label: 'Elementary Graduate' },
  { value: 'HIGH_SCHOOL_UNDERGRADUATE', label: 'High School Undergraduate' },
  { value: 'HIGH_SCHOOL_GRADUATE', label: 'High School Graduate' },
  { value: 'COLLEGE_UNDERGRADUATE', label: 'College Undergraduate' },
  { value: 'COLLEGE_GRADUATE', label: 'College Graduate' },
  { value: 'POST_GRADUATE', label: 'Post Graduate' },
];

// Housing type options
const HOUSING_TYPE_OPTIONS = [
  { value: '', label: 'Select Housing Type' },
  { value: 'SINGLE_HOUSE', label: 'Single House' },
  { value: 'DUPLEX', label: 'Duplex' },
  { value: 'MULTI_UNIT_RESIDENTIAL', label: 'Multi-unit Residential' },
  { value: 'COMMERCIAL_INDUSTRIAL_AGRICULTURAL', label: 'Commercial/Industrial/Agricultural' },
  { value: 'INSTITUTIONAL_LIVING_QUARTERS', label: 'Institutional Living Quarters' },
  { value: 'OTHER', label: 'Other' },
];

// Create Household Modal component
export const CreateHouseholdModal: React.FC<CreateHouseholdModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  currentUser,
}) => {
  const [formData, setFormData] = useState<HouseholdFormData>(initialFormData);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Handle form field changes
  const handleChange = (field: keyof HouseholdFormData, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    try {
      // Validate required fields
      const requiredFields = [
        'headFirstName',
        'headLastName',
        'headPsn',
        'headBirthDate',
        'headGender',
        'region',
        'province',
        'municipality',
        'barangay',
        'streetAddress',
      ];

      const missingFields = requiredFields.filter(field => !formData[field]);
      if (missingFields.length > 0) {
        throw new Error(`Please fill in all required fields: ${missingFields.join(', ')}`);
      }

      // Prepare household data
      const householdData = {
        householdNumber: formData.householdNumber || `HH-${Date.now()}`,
        headOfHouseholdPsn: formData.headPsn,
        totalMembers: parseInt(formData.totalMembers) || 1,
        monthlyIncome: formData.monthlyIncome ? parseFloat(formData.monthlyIncome) : null,
        housingType: formData.housingType,
        housingTenure: formData.housingTenure,
        waterSource: formData.waterSource,
        toiletFacility: formData.toiletFacility,
        electricitySource: formData.electricitySource,
        cookingFuel: formData.cookingFuel,
        preferredLanguage: formData.preferredLanguage,
        notes: formData.notes,
        region: formData.region,
        province: formData.province,
        municipality: formData.municipality,
        barangay: formData.barangay,
        streetAddress: formData.streetAddress,
        zipCode: formData.zipCode,
        householdHead: {
          firstName: formData.headFirstName,
          lastName: formData.headLastName,
          middleName: formData.headMiddleName,
          psn: formData.headPsn,
          birthDate: formData.headBirthDate,
          gender: formData.headGender,
          civilStatus: formData.headCivilStatus,
          nationality: formData.headNationality,
          occupation: formData.headOccupation,
          monthlyIncome: formData.headMonthlyIncome ? parseFloat(formData.headMonthlyIncome) : null,
          educationLevel: formData.headEducationLevel,
        },
        sourceSystem: 'MANUAL_ENTRY',
        createdBy: currentUser.id,
      };

      await onSubmit(householdData);
      
      // Reset form and close modal
      setFormData(initialFormData);
      onClose();
    } catch (err) {
      console.error('Error creating household:', err);
      setError(err instanceof Error ? err.message : 'Failed to create household');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle modal close
  const handleClose = () => {
    if (!isSubmitting) {
      setFormData(initialFormData);
      setError(null);
      onClose();
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Create New Household"
      size="large"
    >
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Error Display */}
        {error && (
          <Alert variant="error" title="Error">
            {error}
          </Alert>
        )}

        {/* Household Head Information */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Household Head Information
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <FormInput
              label="First Name *"
              value={formData.headFirstName}
              onChange={(e) => handleChange('headFirstName', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormInput
              label="Last Name *"
              value={formData.headLastName}
              onChange={(e) => handleChange('headLastName', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormInput
              label="Middle Name"
              value={formData.headMiddleName}
              onChange={(e) => handleChange('headMiddleName', e.target.value)}
              disabled={isSubmitting}
            />
            <FormInput
              label="PhilSys Number (PSN) *"
              value={formData.headPsn}
              onChange={(e) => handleChange('headPsn', e.target.value)}
              required
              disabled={isSubmitting}
              maxLength={16}
            />
            <FormInput
              label="Birth Date *"
              type="date"
              value={formData.headBirthDate}
              onChange={(e) => handleChange('headBirthDate', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormSelect
              label="Gender *"
              value={formData.headGender}
              onChange={(value) => handleChange('headGender', value)}
              options={GENDER_OPTIONS}
              required
              disabled={isSubmitting}
            />
            <FormSelect
              label="Civil Status"
              value={formData.headCivilStatus}
              onChange={(value) => handleChange('headCivilStatus', value)}
              options={CIVIL_STATUS_OPTIONS}
              disabled={isSubmitting}
            />
            <FormInput
              label="Nationality"
              value={formData.headNationality}
              onChange={(e) => handleChange('headNationality', e.target.value)}
              disabled={isSubmitting}
            />
            <FormInput
              label="Occupation"
              value={formData.headOccupation}
              onChange={(e) => handleChange('headOccupation', e.target.value)}
              disabled={isSubmitting}
            />
            <FormInput
              label="Monthly Income"
              type="number"
              value={formData.headMonthlyIncome}
              onChange={(e) => handleChange('headMonthlyIncome', e.target.value)}
              disabled={isSubmitting}
              min="0"
              step="0.01"
            />
            <FormSelect
              label="Education Level"
              value={formData.headEducationLevel}
              onChange={(value) => handleChange('headEducationLevel', value)}
              options={EDUCATION_OPTIONS}
              disabled={isSubmitting}
            />
          </div>
        </div>

        {/* Address Information */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Address Information
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <FormInput
              label="Region *"
              value={formData.region}
              onChange={(e) => handleChange('region', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormInput
              label="Province *"
              value={formData.province}
              onChange={(e) => handleChange('province', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormInput
              label="Municipality *"
              value={formData.municipality}
              onChange={(e) => handleChange('municipality', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormInput
              label="Barangay *"
              value={formData.barangay}
              onChange={(e) => handleChange('barangay', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormInput
              label="Street Address *"
              value={formData.streetAddress}
              onChange={(e) => handleChange('streetAddress', e.target.value)}
              required
              disabled={isSubmitting}
            />
            <FormInput
              label="ZIP Code"
              value={formData.zipCode}
              onChange={(e) => handleChange('zipCode', e.target.value)}
              disabled={isSubmitting}
            />
          </div>
        </div>

        {/* Household Information */}
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            Household Information
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <FormInput
              label="Household Number"
              value={formData.householdNumber}
              onChange={(e) => handleChange('householdNumber', e.target.value)}
              disabled={isSubmitting}
              placeholder="Auto-generated if empty"
            />
            <FormInput
              label="Total Members"
              type="number"
              value={formData.totalMembers}
              onChange={(e) => handleChange('totalMembers', e.target.value)}
              disabled={isSubmitting}
              min="1"
            />
            <FormInput
              label="Monthly Household Income"
              type="number"
              value={formData.monthlyIncome}
              onChange={(e) => handleChange('monthlyIncome', e.target.value)}
              disabled={isSubmitting}
              min="0"
              step="0.01"
            />
            <FormSelect
              label="Housing Type"
              value={formData.housingType}
              onChange={(value) => handleChange('housingType', value)}
              options={HOUSING_TYPE_OPTIONS}
              disabled={isSubmitting}
            />
            <FormInput
              label="Preferred Language"
              value={formData.preferredLanguage}
              onChange={(e) => handleChange('preferredLanguage', e.target.value)}
              disabled={isSubmitting}
            />
          </div>
          <div className="mt-4">
            <FormTextarea
              label="Notes"
              value={formData.notes}
              onChange={(e) => handleChange('notes', e.target.value)}
              disabled={isSubmitting}
              rows={3}
            />
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex items-center justify-end space-x-3 pt-6 border-t">
          <Button
            type="button"
            variant="outline"
            onClick={handleClose}
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            loading={isSubmitting}
            disabled={isSubmitting}
          >
            Create Household
          </Button>
        </div>
      </form>
    </Modal>
  );
};
