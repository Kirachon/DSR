'use client';

// Personal Information Step Component
// First step of household registration wizard

import React from 'react';

import { FormInput, FormSelect } from '@/components/forms';
import { Input, Button } from '@/components/ui';
import type {
  PersonalInfo,
  Address,
  User,
  HouseholdRegistrationData,
} from '@/types';

// Component props interface
interface PersonalInformationStepProps {
  data: PersonalInfo;
  address: Address;
  onUpdate: (data: Partial<HouseholdRegistrationData>) => void;
  errors: string[];
  currentUser: User | null;
}

// Gender options
const GENDER_OPTIONS = [
  { value: '', label: 'Select Gender' },
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
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
  { value: 'VOCATIONAL', label: 'Vocational/Technical' },
  { value: 'COLLEGE_UNDERGRADUATE', label: 'College Undergraduate' },
  { value: 'COLLEGE_GRADUATE', label: 'College Graduate' },
  { value: 'POST_GRADUATE', label: 'Post Graduate' },
];

// Philippine regions
const REGIONS = [
  { value: '', label: 'Select Region' },
  { value: 'NCR', label: 'National Capital Region (NCR)' },
  { value: 'CAR', label: 'Cordillera Administrative Region (CAR)' },
  { value: 'REGION_I', label: 'Region I - Ilocos Region' },
  { value: 'REGION_II', label: 'Region II - Cagayan Valley' },
  { value: 'REGION_III', label: 'Region III - Central Luzon' },
  { value: 'REGION_IV_A', label: 'Region IV-A - CALABARZON' },
  { value: 'REGION_IV_B', label: 'Region IV-B - MIMAROPA' },
  { value: 'REGION_V', label: 'Region V - Bicol Region' },
  { value: 'REGION_VI', label: 'Region VI - Western Visayas' },
  { value: 'REGION_VII', label: 'Region VII - Central Visayas' },
  { value: 'REGION_VIII', label: 'Region VIII - Eastern Visayas' },
  { value: 'REGION_IX', label: 'Region IX - Zamboanga Peninsula' },
  { value: 'REGION_X', label: 'Region X - Northern Mindanao' },
  { value: 'REGION_XI', label: 'Region XI - Davao Region' },
  { value: 'REGION_XII', label: 'Region XII - SOCCSKSARGEN' },
  { value: 'REGION_XIII', label: 'Region XIII - Caraga' },
  {
    value: 'BARMM',
    label: 'Bangsamoro Autonomous Region in Muslim Mindanao (BARMM)',
  },
];

// Personal Information Step component
export const PersonalInformationStep: React.FC<
  PersonalInformationStepProps
> = ({ data, address, onUpdate, errors, currentUser }) => {
  // Handle personal info updates
  const handlePersonalInfoChange = (
    field: keyof PersonalInfo,
    value: string | number
  ) => {
    onUpdate({
      personalInfo: {
        ...data,
        [field]: value,
      },
    });
  };

  // Handle address updates
  const handleAddressChange = (
    field: keyof Address,
    value: string | number
  ) => {
    onUpdate({
      address: {
        ...address,
        [field]: value,
      },
    });
  };

  // Auto-fill from current user if available
  React.useEffect(() => {
    if (currentUser && !data.firstName) {
      handlePersonalInfoChange('firstName', currentUser.firstName || '');
      handlePersonalInfoChange('lastName', currentUser.lastName || '');
      handlePersonalInfoChange('emailAddress', currentUser.email || '');
    }
  }, [currentUser, data.firstName]);

  return (
    <div className='space-y-8'>
      {/* Personal Information Section */}
      <div>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Personal Information
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          <FormInput
            label='First Name *'
            value={data.firstName}
            onChange={e =>
              handlePersonalInfoChange(
                'firstName',
                typeof e === 'string' ? e : e.target.value
              )
            }
            placeholder='Enter first name'
            required
          />

          <FormInput
            label='Middle Name'
            value={data.middleName}
            onChange={e =>
              handlePersonalInfoChange(
                'middleName',
                typeof e === 'string' ? e : e.target.value
              )
            }
            placeholder='Enter middle name'
          />

          <FormInput
            label='Last Name *'
            value={data.lastName}
            onChange={e =>
              handlePersonalInfoChange(
                'lastName',
                typeof e === 'string' ? e : e.target.value
              )
            }
            placeholder='Enter last name'
            required
          />

          <FormInput
            label='Suffix'
            value={data.suffix}
            onChange={value =>
              handlePersonalInfoChange(
                'suffix',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Jr., Sr., III, etc.'
          />

          <FormInput
            label='Birth Date *'
            type='date'
            value={data.birthDate}
            onChange={value =>
              handlePersonalInfoChange(
                'birthDate',
                typeof value === 'string' ? value : value.target.value
              )
            }
            required
          />

          <FormSelect
            label='Gender *'
            value={data.gender}
            onChange={value =>
              handlePersonalInfoChange(
                'gender',
                typeof value === 'string' ? value : value.target.value
              )
            }
            options={GENDER_OPTIONS}
            required
          />

          <FormSelect
            label='Civil Status *'
            value={data.civilStatus}
            onChange={value =>
              handlePersonalInfoChange(
                'civilStatus',
                typeof value === 'string' ? value : value.target.value
              )
            }
            options={CIVIL_STATUS_OPTIONS}
            required
          />

          <FormInput
            label='Nationality'
            value={data.nationality}
            onChange={value =>
              handlePersonalInfoChange(
                'nationality',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter nationality'
          />

          <FormInput
            label='Religion'
            value={data.religion}
            onChange={value =>
              handlePersonalInfoChange(
                'religion',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter religion'
          />
        </div>
      </div>

      {/* Education and Employment Section */}
      <div>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Education & Employment
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          <FormSelect
            label='Education Level'
            value={data.educationLevel}
            onChange={value =>
              handlePersonalInfoChange(
                'educationLevel',
                typeof value === 'string' ? value : value.target.value
              )
            }
            options={EDUCATION_OPTIONS}
          />

          <FormInput
            label='Occupation'
            value={data.occupation}
            onChange={value =>
              handlePersonalInfoChange(
                'occupation',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter occupation'
          />

          <FormInput
            label='Monthly Income (PHP)'
            type='number'
            value={data.monthlyIncome.toString()}
            onChange={value =>
              handlePersonalInfoChange(
                'monthlyIncome',
                parseFloat(
                  typeof value === 'string' ? value : value.target.value
                ) || 0
              )
            }
            placeholder='0.00'
            min='0'
            step='0.01'
          />
        </div>
      </div>

      {/* Contact Information Section */}
      <div>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Contact Information
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <FormInput
            label='Contact Number *'
            value={data.contactNumber}
            onChange={value =>
              handlePersonalInfoChange(
                'contactNumber',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='+63 9XX XXX XXXX'
            required
          />

          <FormInput
            label='Email Address'
            type='email'
            value={data.emailAddress}
            onChange={value =>
              handlePersonalInfoChange(
                'emailAddress',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter email address'
          />

          <FormInput
            label='PhilSys Number'
            value={data.philSysNumber}
            onChange={value =>
              handlePersonalInfoChange(
                'philSysNumber',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='XXXX-XXXX-XXXX'
            maxLength={14}
          />
        </div>
      </div>

      {/* Address Information Section */}
      <div>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Address Information
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          <FormInput
            label='House Number'
            value={address.houseNumber}
            onChange={value =>
              handleAddressChange(
                'houseNumber',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter house number'
          />

          <FormInput
            label='Street'
            value={address.street}
            onChange={value =>
              handleAddressChange(
                'street',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter street name'
          />

          <FormInput
            label='Barangay *'
            value={address.barangay}
            onChange={value =>
              handleAddressChange(
                'barangay',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter barangay'
            required
          />

          <FormInput
            label='Municipality/City *'
            value={address.municipality}
            onChange={value =>
              handleAddressChange(
                'municipality',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter municipality/city'
            required
          />

          <FormInput
            label='Province *'
            value={address.province}
            onChange={value =>
              handleAddressChange(
                'province',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter province'
            required
          />

          <FormSelect
            label='Region *'
            value={address.region}
            onChange={value =>
              handleAddressChange(
                'region',
                typeof value === 'string' ? value : value.target.value
              )
            }
            options={REGIONS}
            required
          />

          <FormInput
            label='ZIP Code'
            value={address.zipCode}
            onChange={value =>
              handleAddressChange(
                'zipCode',
                typeof value === 'string' ? value : value.target.value
              )
            }
            placeholder='Enter ZIP code'
            maxLength={4}
          />
        </div>
      </div>

      {/* Information Notice */}
      <div className='bg-blue-50 border border-blue-200 rounded-lg p-4'>
        <div className='flex'>
          <div className='flex-shrink-0'>
            <svg
              className='h-5 w-5 text-blue-400'
              fill='currentColor'
              viewBox='0 0 20 20'
            >
              <path
                fillRule='evenodd'
                d='M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z'
                clipRule='evenodd'
              />
            </svg>
          </div>
          <div className='ml-3'>
            <h3 className='text-sm font-medium text-blue-800'>
              Information Privacy Notice
            </h3>
            <div className='mt-2 text-sm text-blue-700'>
              <p>
                All personal information collected will be used solely for the
                purpose of social protection program eligibility assessment and
                service delivery. Your data is protected under the Philippine
                Data Privacy Act.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
