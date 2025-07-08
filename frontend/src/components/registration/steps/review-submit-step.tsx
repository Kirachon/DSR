'use client';

// Review and Submit Step Component
// Final step of household registration wizard

import React from 'react';

import { Card, Alert } from '@/components/ui';
import type { HouseholdRegistrationData } from '@/types';

// Component props interface
interface ReviewSubmitStepProps {
  data: HouseholdRegistrationData;
  onUpdate: (data: Partial<HouseholdRegistrationData>) => void;
  errors: string[];
}

// Review and Submit Step component
export const ReviewSubmitStep: React.FC<ReviewSubmitStepProps> = ({
  data,
  onUpdate,
  errors,
}) => {
  // Handle consent changes
  const handleConsentChange = (
    field: keyof typeof data.consent,
    value: boolean
  ) => {
    onUpdate({
      consent: {
        ...data.consent,
        [field]: value,
      },
    });
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

  // Format currency
  const formatCurrency = (amount: number): string => {
    return new Intl.NumberFormat('en-PH', {
      style: 'currency',
      currency: 'PHP',
    }).format(amount);
  };

  // Get document type display name
  const getDocumentTypeDisplayName = (type: string): string => {
    return type
      .replace('_', ' ')
      .toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase());
  };

  return (
    <div className='space-y-6'>
      {/* Review Instructions */}
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
              Review Your Information
            </h3>
            <div className='mt-2 text-sm text-blue-700'>
              <p>
                Please review all the information you've provided. Make sure
                everything is accurate before submitting your registration. You
                can go back to previous steps to make changes if needed.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Personal Information Summary */}
      <Card className='p-6'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Personal Information
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4 text-sm'>
          <div>
            <span className='font-medium text-gray-700'>Full Name:</span>
            <p className='text-gray-900'>
              {data.personalInfo.firstName} {data.personalInfo.middleName}{' '}
              {data.personalInfo.lastName} {data.personalInfo.suffix}
            </p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>Birth Date:</span>
            <p className='text-gray-900'>
              {data.personalInfo.birthDate} (Age:{' '}
              {calculateAge(data.personalInfo.birthDate)})
            </p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>Gender:</span>
            <p className='text-gray-900'>{data.personalInfo.gender}</p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>Civil Status:</span>
            <p className='text-gray-900'>{data.personalInfo.civilStatus}</p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>Contact Number:</span>
            <p className='text-gray-900'>{data.personalInfo.contactNumber}</p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>Email:</span>
            <p className='text-gray-900'>
              {data.personalInfo.emailAddress || 'Not provided'}
            </p>
          </div>
          {data.personalInfo.philSysNumber && (
            <div>
              <span className='font-medium text-gray-700'>PhilSys Number:</span>
              <p className='text-gray-900'>{data.personalInfo.philSysNumber}</p>
            </div>
          )}
        </div>
      </Card>

      {/* Address Summary */}
      <Card className='p-6'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>Address</h4>
        <div className='text-sm'>
          <p className='text-gray-900'>
            {data.address.houseNumber} {data.address.street},{' '}
            {data.address.barangay}, {data.address.municipality},{' '}
            {data.address.province}, {data.address.region}
            {data.address.zipCode && ` ${data.address.zipCode}`}
          </p>
        </div>
      </Card>

      {/* Household Members Summary */}
      <Card className='p-6'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Household Members ({data.householdMembers.length})
        </h4>
        <div className='space-y-3'>
          {data.householdMembers.map((member, index) => (
            <div
              key={member.id}
              className='flex items-center justify-between p-3 bg-gray-50 rounded-lg'
            >
              <div className='flex items-center space-x-3'>
                <div
                  className={`w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-medium ${
                    member.isHeadOfHousehold ? 'bg-primary-600' : 'bg-gray-400'
                  }`}
                >
                  {member.firstName.charAt(0)}
                  {member.lastName.charAt(0)}
                </div>
                <div>
                  <p className='font-medium text-gray-900'>
                    {member.firstName} {member.lastName}
                  </p>
                  <p className='text-sm text-gray-600'>
                    {member.relationship} • {member.gender} • Age:{' '}
                    {calculateAge(member.birthDate)}
                  </p>
                </div>
              </div>
              {member.isHeadOfHousehold && (
                <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-800'>
                  Head
                </span>
              )}
            </div>
          ))}
        </div>
      </Card>

      {/* Socio-Economic Summary */}
      <Card className='p-6'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Socio-Economic Information
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-4 text-sm'>
          <div>
            <span className='font-medium text-gray-700'>Household Size:</span>
            <p className='text-gray-900'>
              {data.socioEconomicInfo.householdSize}
            </p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>
              Total Monthly Income:
            </span>
            <p className='text-gray-900'>
              {formatCurrency(data.socioEconomicInfo.totalMonthlyIncome)}
            </p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>
              Primary Income Source:
            </span>
            <p className='text-gray-900'>
              {data.socioEconomicInfo.primaryIncomeSource.replace('_', ' ')}
            </p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>Housing Type:</span>
            <p className='text-gray-900'>
              {data.socioEconomicInfo.housingType.replace('_', ' ')}
            </p>
          </div>
          <div>
            <span className='font-medium text-gray-700'>
              Housing Ownership:
            </span>
            <p className='text-gray-900'>
              {data.socioEconomicInfo.housingOwnership.replace('_', ' ')}
            </p>
          </div>
        </div>

        {/* Utilities */}
        <div className='mt-4'>
          <span className='font-medium text-gray-700'>
            Access to Utilities:
          </span>
          <div className='flex flex-wrap gap-2 mt-1'>
            {Object.entries(data.socioEconomicInfo.accessToUtilities).map(
              ([utility, hasAccess]) =>
                hasAccess && (
                  <span
                    key={utility}
                    className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800'
                  >
                    {utility.charAt(0).toUpperCase() + utility.slice(1)}
                  </span>
                )
            )}
          </div>
        </div>

        {/* Assets */}
        {data.socioEconomicInfo.assets.length > 0 && (
          <div className='mt-4'>
            <span className='font-medium text-gray-700'>
              Assets ({data.socioEconomicInfo.assets.length}):
            </span>
            <div className='mt-1 space-y-1'>
              {data.socioEconomicInfo.assets.map(asset => (
                <p key={asset.id} className='text-sm text-gray-900'>
                  • {asset.description} ({formatCurrency(asset.estimatedValue)})
                </p>
              ))}
            </div>
          </div>
        )}

        {/* Vulnerabilities */}
        {data.socioEconomicInfo.vulnerabilities.length > 0 && (
          <div className='mt-4'>
            <span className='font-medium text-gray-700'>
              Vulnerabilities ({data.socioEconomicInfo.vulnerabilities.length}):
            </span>
            <div className='flex flex-wrap gap-2 mt-1'>
              {data.socioEconomicInfo.vulnerabilities.map(vulnerability => (
                <span
                  key={vulnerability.id}
                  className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    vulnerability.severity === 'CRITICAL'
                      ? 'bg-red-100 text-red-800'
                      : vulnerability.severity === 'HIGH'
                        ? 'bg-orange-100 text-orange-800'
                        : vulnerability.severity === 'MEDIUM'
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-green-100 text-green-800'
                  }`}
                >
                  {vulnerability.description}
                </span>
              ))}
            </div>
          </div>
        )}
      </Card>

      {/* Documents Summary */}
      <Card className='p-6'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Uploaded Documents ({data.documents.length})
        </h4>
        {data.documents.length === 0 ? (
          <p className='text-gray-600'>No documents uploaded</p>
        ) : (
          <div className='space-y-2'>
            {data.documents.map(document => (
              <div
                key={document.id}
                className='flex items-center justify-between p-2 bg-gray-50 rounded'
              >
                <div>
                  <p className='font-medium text-gray-900'>{document.name}</p>
                  <p className='text-sm text-gray-600'>
                    {getDocumentTypeDisplayName(document.type)}
                  </p>
                </div>
                <span
                  className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    document.status === 'PENDING'
                      ? 'bg-yellow-100 text-yellow-800'
                      : document.status === 'UPLOADED'
                        ? 'bg-blue-100 text-blue-800'
                        : document.status === 'VERIFIED'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-red-100 text-red-800'
                  }`}
                >
                  {document.status}
                </span>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Consent Section */}
      <Card className='p-6'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Consent and Agreements
        </h4>
        <div className='space-y-4'>
          <div className='flex items-start space-x-3'>
            <input
              type='checkbox'
              id='dataProcessing'
              checked={data.consent.dataProcessing}
              onChange={e =>
                handleConsentChange('dataProcessing', e.target.checked)
              }
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded mt-1'
            />
            <div className='flex-1'>
              <label
                htmlFor='dataProcessing'
                className='text-sm font-medium text-gray-700 cursor-pointer'
              >
                Data Processing Consent *
              </label>
              <p className='text-sm text-gray-600 mt-1'>
                I consent to the collection, processing, and storage of my
                personal data for the purpose of social protection program
                eligibility assessment and service delivery in accordance with
                the Philippine Data Privacy Act (R.A. 10173).
              </p>
            </div>
          </div>

          <div className='flex items-start space-x-3'>
            <input
              type='checkbox'
              id='informationSharing'
              checked={data.consent.informationSharing}
              onChange={e =>
                handleConsentChange('informationSharing', e.target.checked)
              }
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded mt-1'
            />
            <div className='flex-1'>
              <label
                htmlFor='informationSharing'
                className='text-sm font-medium text-gray-700 cursor-pointer'
              >
                Information Sharing Consent *
              </label>
              <p className='text-sm text-gray-600 mt-1'>
                I consent to the sharing of my information with relevant
                government agencies and authorized service providers for the
                purpose of program implementation and service delivery.
              </p>
            </div>
          </div>

          <div className='flex items-start space-x-3'>
            <input
              type='checkbox'
              id='programEligibility'
              checked={data.consent.programEligibility}
              onChange={e =>
                handleConsentChange('programEligibility', e.target.checked)
              }
              className='h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded mt-1'
            />
            <div className='flex-1'>
              <label
                htmlFor='programEligibility'
                className='text-sm font-medium text-gray-700 cursor-pointer'
              >
                Program Eligibility Assessment *
              </label>
              <p className='text-sm text-gray-600 mt-1'>
                I understand that the information provided will be used to
                assess my household's eligibility for various social protection
                programs and that false information may result in
                disqualification.
              </p>
            </div>
          </div>
        </div>
      </Card>

      {/* Final Notice */}
      <Alert variant='info' title='Important Notice'>
        <p>
          By submitting this registration, you certify that all information
          provided is true and accurate to the best of your knowledge. Any false
          or misleading information may result in the rejection of your
          application or disqualification from programs.
        </p>
      </Alert>
    </div>
  );
};
