'use client';

// Household Composition Step Component
// Second step of household registration wizard

import React, { useState } from 'react';

import { FormInput, FormSelect } from '@/components/forms';
import { Button, Card, Modal } from '@/components/ui';
import type { HouseholdMember, HouseholdRegistrationData } from '@/types';

// Component props interface
interface HouseholdCompositionStepProps {
  data: HouseholdMember[];
  onUpdate: (data: Partial<HouseholdRegistrationData>) => void;
  errors: string[];
}

// Relationship options
const RELATIONSHIP_OPTIONS = [
  { value: '', label: 'Select Relationship' },
  { value: 'HEAD', label: 'Head of Household' },
  { value: 'SPOUSE', label: 'Spouse' },
  { value: 'SON', label: 'Son' },
  { value: 'DAUGHTER', label: 'Daughter' },
  { value: 'FATHER', label: 'Father' },
  { value: 'MOTHER', label: 'Mother' },
  { value: 'BROTHER', label: 'Brother' },
  { value: 'SISTER', label: 'Sister' },
  { value: 'GRANDFATHER', label: 'Grandfather' },
  { value: 'GRANDMOTHER', label: 'Grandmother' },
  { value: 'GRANDSON', label: 'Grandson' },
  { value: 'GRANDDAUGHTER', label: 'Granddaughter' },
  { value: 'UNCLE', label: 'Uncle' },
  { value: 'AUNT', label: 'Aunt' },
  { value: 'NEPHEW', label: 'Nephew' },
  { value: 'NIECE', label: 'Niece' },
  { value: 'COUSIN', label: 'Cousin' },
  { value: 'IN_LAW', label: 'In-law' },
  { value: 'BOARDER', label: 'Boarder' },
  { value: 'OTHER', label: 'Other' },
];

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

// Initial member data
const initialMemberData: HouseholdMember = {
  id: '',
  firstName: '',
  lastName: '',
  middleName: '',
  suffix: '',
  birthDate: '',
  gender: '',
  civilStatus: '',
  relationship: '',
  occupation: '',
  monthlyIncome: 0,
  philSysNumber: '',
  isHeadOfHousehold: false,
  hasDisability: false,
  disabilityType: '',
  isPregnant: false,
  isLactating: false,
  isStudent: false,
  schoolLevel: '',
};

// Household Composition Step component
export const HouseholdCompositionStep: React.FC<
  HouseholdCompositionStepProps
> = ({ data, onUpdate, errors }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingMember, setEditingMember] = useState<HouseholdMember | null>(
    null
  );
  const [memberData, setMemberData] =
    useState<HouseholdMember>(initialMemberData);

  // Handle member data changes
  const handleMemberChange = (
    field: keyof HouseholdMember,
    value: string | number | boolean
  ) => {
    setMemberData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  // Open modal for adding new member
  const handleAddMember = () => {
    setEditingMember(null);
    setMemberData({ ...initialMemberData, id: Date.now().toString() });
    setIsModalOpen(true);
  };

  // Open modal for editing existing member
  const handleEditMember = (member: HouseholdMember) => {
    setEditingMember(member);
    setMemberData(member);
    setIsModalOpen(true);
  };

  // Save member (add or update)
  const handleSaveMember = () => {
    // Basic validation
    if (
      !memberData.firstName ||
      !memberData.lastName ||
      !memberData.birthDate ||
      !memberData.gender ||
      !memberData.relationship
    ) {
      return;
    }

    let updatedMembers: HouseholdMember[];

    if (editingMember) {
      // Update existing member
      updatedMembers = data.map(member =>
        member.id === editingMember.id ? memberData : member
      );
    } else {
      // Add new member
      updatedMembers = [...data, memberData];
    }

    // Ensure only one head of household
    if (memberData.relationship === 'HEAD') {
      updatedMembers = updatedMembers.map(member => ({
        ...member,
        isHeadOfHousehold: member.id === memberData.id,
        relationship:
          member.id === memberData.id
            ? 'HEAD'
            : member.relationship === 'HEAD'
              ? 'SPOUSE'
              : member.relationship,
      }));
    }

    onUpdate({ householdMembers: updatedMembers });
    setIsModalOpen(false);
  };

  // Remove member
  const handleRemoveMember = (memberId: string) => {
    const updatedMembers = data.filter(member => member.id !== memberId);
    onUpdate({ householdMembers: updatedMembers });
  };

  // Calculate age from birth date
  const calculateAge = (birthDate: string): number => {
    if (!birthDate) return 0;
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (
      monthDiff < 0 ||
      (monthDiff === 0 && today.getDate() < birth.getDate())
    ) {
      age--;
    }
    return age;
  };

  return (
    <div className='space-y-6'>
      {/* Instructions */}
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
              Household Composition Instructions
            </h3>
            <div className='mt-2 text-sm text-blue-700'>
              <p>
                Add all members of your household, including yourself. A
                household member is anyone who lives in the same dwelling and
                shares meals or living expenses.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Add Member Button */}
      <div className='flex justify-between items-center'>
        <h4 className='text-lg font-medium text-gray-900'>
          Household Members ({data.length})
        </h4>
        <Button onClick={handleAddMember}>Add Member</Button>
      </div>

      {/* Members List */}
      {data.length === 0 ? (
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
                d='M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z'
              />
            </svg>
            <h3 className='text-lg font-medium text-gray-900 mb-2'>
              No household members added
            </h3>
            <p className='text-gray-600 mb-4'>
              Start by adding yourself and other members of your household.
            </p>
            <Button onClick={handleAddMember}>Add First Member</Button>
          </div>
        </Card>
      ) : (
        <div className='space-y-4'>
          {data.map((member, index) => (
            <Card key={member.id} className='p-4'>
              <div className='flex items-center justify-between'>
                <div className='flex-1'>
                  <div className='flex items-center space-x-4'>
                    <div className='flex-shrink-0'>
                      <div
                        className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-medium ${
                          member.isHeadOfHousehold
                            ? 'bg-primary-600'
                            : 'bg-gray-400'
                        }`}
                      >
                        {member.firstName.charAt(0)}
                        {member.lastName.charAt(0)}
                      </div>
                    </div>
                    <div className='flex-1'>
                      <div className='flex items-center space-x-2'>
                        <h4 className='text-lg font-medium text-gray-900'>
                          {member.firstName} {member.middleName}{' '}
                          {member.lastName} {member.suffix}
                        </h4>
                        {member.isHeadOfHousehold && (
                          <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-800'>
                            Head of Household
                          </span>
                        )}
                      </div>
                      <div className='mt-1 text-sm text-gray-600'>
                        <span>{member.relationship}</span>
                        <span className='mx-2'>•</span>
                        <span>{member.gender}</span>
                        <span className='mx-2'>•</span>
                        <span>Age: {calculateAge(member.birthDate)}</span>
                        {member.occupation && (
                          <>
                            <span className='mx-2'>•</span>
                            <span>{member.occupation}</span>
                          </>
                        )}
                      </div>
                      {(member.hasDisability ||
                        member.isPregnant ||
                        member.isLactating ||
                        member.isStudent) && (
                        <div className='mt-2 flex flex-wrap gap-1'>
                          {member.hasDisability && (
                            <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-800'>
                              PWD
                            </span>
                          )}
                          {member.isPregnant && (
                            <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-pink-100 text-pink-800'>
                              Pregnant
                            </span>
                          )}
                          {member.isLactating && (
                            <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800'>
                              Lactating
                            </span>
                          )}
                          {member.isStudent && (
                            <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800'>
                              Student
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
                <div className='flex items-center space-x-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => handleEditMember(member)}
                  >
                    Edit
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => handleRemoveMember(member.id)}
                    className='text-red-600 hover:text-red-700 hover:border-red-300'
                  >
                    Remove
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Add/Edit Member Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingMember ? 'Edit Household Member' : 'Add Household Member'}
        size='lg'
      >
        <div className='space-y-6'>
          {/* Basic Information */}
          <div>
            <h4 className='text-md font-medium text-gray-900 mb-4'>
              Basic Information
            </h4>
            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <FormInput
                label='First Name *'
                value={memberData.firstName}
                onChange={value =>
                  handleMemberChange(
                    'firstName',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                placeholder='Enter first name'
                required
              />

              <FormInput
                label='Last Name *'
                value={memberData.lastName}
                onChange={value =>
                  handleMemberChange(
                    'lastName',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                placeholder='Enter last name'
                required
              />

              <FormInput
                label='Middle Name'
                value={memberData.middleName}
                onChange={value =>
                  handleMemberChange(
                    'middleName',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                placeholder='Enter middle name'
              />

              <FormInput
                label='Suffix'
                value={memberData.suffix}
                onChange={value =>
                  handleMemberChange(
                    'suffix',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                placeholder='Jr., Sr., III, etc.'
              />

              <FormInput
                label='Birth Date *'
                type='date'
                value={memberData.birthDate}
                onChange={value =>
                  handleMemberChange(
                    'birthDate',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                required
              />

              <FormSelect
                label='Gender *'
                value={memberData.gender}
                onChange={value =>
                  handleMemberChange(
                    'gender',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                options={GENDER_OPTIONS}
                required
              />

              <FormSelect
                label='Civil Status'
                value={memberData.civilStatus}
                onChange={value =>
                  handleMemberChange(
                    'civilStatus',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                options={CIVIL_STATUS_OPTIONS}
              />

              <FormSelect
                label='Relationship to Head *'
                value={memberData.relationship}
                onChange={value =>
                  handleMemberChange(
                    'relationship',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                options={RELATIONSHIP_OPTIONS}
                required
              />
            </div>
          </div>

          {/* Employment Information */}
          <div>
            <h4 className='text-md font-medium text-gray-900 mb-4'>
              Employment Information
            </h4>
            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <FormInput
                label='Occupation'
                value={memberData.occupation}
                onChange={value =>
                  handleMemberChange(
                    'occupation',
                    typeof value === 'string' ? value : value.target.value
                  )
                }
                placeholder='Enter occupation'
              />

              <FormInput
                label='Monthly Income (PHP)'
                type='number'
                value={memberData.monthlyIncome.toString()}
                onChange={value =>
                  handleMemberChange(
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

          {/* Additional Information */}
          <div>
            <h4 className='text-md font-medium text-gray-900 mb-4'>
              Additional Information
            </h4>
            <div className='space-y-4'>
              <div className='flex items-center space-x-3'>
                <input
                  type='checkbox'
                  id='hasDisability'
                  checked={memberData.hasDisability}
                  onChange={e =>
                    handleMemberChange('hasDisability', e.target.checked)
                  }
                  className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
                />
                <label
                  htmlFor='hasDisability'
                  className='text-sm font-medium text-gray-700'
                >
                  Person with Disability (PWD)
                </label>
              </div>

              {memberData.hasDisability && (
                <FormInput
                  label='Disability Type'
                  value={memberData.disabilityType}
                  onChange={value =>
                    handleMemberChange(
                      'disabilityType',
                      typeof value === 'string' ? value : value.target.value
                    )
                  }
                  placeholder='Specify disability type'
                />
              )}

              {memberData.gender === 'FEMALE' &&
                calculateAge(memberData.birthDate) >= 15 &&
                calculateAge(memberData.birthDate) <= 49 && (
                  <>
                    <div className='flex items-center space-x-3'>
                      <input
                        type='checkbox'
                        id='isPregnant'
                        checked={memberData.isPregnant}
                        onChange={e =>
                          handleMemberChange('isPregnant', e.target.checked)
                        }
                        className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
                      />
                      <label
                        htmlFor='isPregnant'
                        className='text-sm font-medium text-gray-700'
                      >
                        Currently Pregnant
                      </label>
                    </div>

                    <div className='flex items-center space-x-3'>
                      <input
                        type='checkbox'
                        id='isLactating'
                        checked={memberData.isLactating}
                        onChange={e =>
                          handleMemberChange('isLactating', e.target.checked)
                        }
                        className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
                      />
                      <label
                        htmlFor='isLactating'
                        className='text-sm font-medium text-gray-700'
                      >
                        Currently Lactating
                      </label>
                    </div>
                  </>
                )}

              <div className='flex items-center space-x-3'>
                <input
                  type='checkbox'
                  id='isStudent'
                  checked={memberData.isStudent}
                  onChange={e =>
                    handleMemberChange('isStudent', e.target.checked)
                  }
                  className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded'
                />
                <label
                  htmlFor='isStudent'
                  className='text-sm font-medium text-gray-700'
                >
                  Currently a Student
                </label>
              </div>

              {memberData.isStudent && (
                <FormInput
                  label='School Level'
                  value={memberData.schoolLevel}
                  onChange={value =>
                    handleMemberChange(
                      'schoolLevel',
                      typeof value === 'string' ? value : value.target.value
                    )
                  }
                  placeholder='Elementary, High School, College, etc.'
                />
              )}
            </div>
          </div>
        </div>

        {/* Modal Actions */}
        <div className='flex justify-end space-x-3 mt-6'>
          <Button variant='outline' onClick={() => setIsModalOpen(false)}>
            Cancel
          </Button>
          <Button onClick={handleSaveMember}>
            {editingMember ? 'Update Member' : 'Add Member'}
          </Button>
        </div>
      </Modal>
    </div>
  );
};
