'use client';

// Household Registration Wizard Component
// Multi-step wizard for household registration with progress tracking

import React, { useState, useCallback } from 'react';

import { Card, Button, Alert } from '@/components/ui';
import { registrationApi, dataManagementApi } from '@/lib/api';
import type { HouseholdRegistrationData, User } from '@/types';

import { DocumentUploadStep } from './steps/document-upload-step';
import { HouseholdCompositionStep } from './steps/household-composition-step';
import { PersonalInformationStep } from './steps/personal-information-step';
import { ReviewSubmitStep } from './steps/review-submit-step';
import { SocioEconomicStep } from './steps/socio-economic-step';

// Wizard props interface
interface HouseholdRegistrationWizardProps {
  onSubmit: (data: HouseholdRegistrationData) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
  currentUser: User | null;
}

// Registration steps
const STEPS = [
  {
    id: 'personal',
    title: 'Personal Information',
    description: 'Basic personal details',
  },
  {
    id: 'household',
    title: 'Household Composition',
    description: 'Family members and relationships',
  },
  {
    id: 'socioeconomic',
    title: 'Socio-Economic Information',
    description: 'Income, assets, and living conditions',
  },
  {
    id: 'documents',
    title: 'Document Upload',
    description: 'Required supporting documents',
  },
  {
    id: 'review',
    title: 'Review & Submit',
    description: 'Verify information and submit',
  },
];

// Initial form data
const initialFormData: HouseholdRegistrationData = {
  personalInfo: {
    firstName: '',
    lastName: '',
    middleName: '',
    suffix: '',
    birthDate: '',
    gender: '',
    civilStatus: '',
    nationality: 'Filipino',
    religion: '',
    educationLevel: '',
    occupation: '',
    monthlyIncome: 0,
    philSysNumber: '',
    contactNumber: '',
    emailAddress: '',
  },
  address: {
    houseNumber: '',
    street: '',
    streetAddress: '',
    barangay: '',
    municipality: '',
    province: '',
    region: '',
    zipCode: '',
    coordinates: {
      latitude: 0,
      longitude: 0,
    },
  },
  householdMembers: [],
  socioEconomicInfo: {
    householdSize: 1,
    totalMonthlyIncome: 0,
    primaryIncomeSource: '',
    housingType: '',
    housingOwnership: '',
    accessToUtilities: {
      electricity: false,
      water: false,
      internet: false,
      sewerage: false,
    },
    assets: [],
    vulnerabilities: [],
  },
  documents: [],
  consent: {
    dataProcessing: false,
    informationSharing: false,
    programEligibility: false,
  },
};

// Household Registration Wizard component
export const HouseholdRegistrationWizard: React.FC<
  HouseholdRegistrationWizardProps
> = ({ onSubmit, onCancel, isSubmitting, currentUser }) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [formData, setFormData] =
    useState<HouseholdRegistrationData>(initialFormData);
  const [stepErrors, setStepErrors] = useState<Record<string, string[]>>({});
  const [isValidating, setIsValidating] = useState(false);
  const [validationWarnings, setValidationWarnings] = useState<string[]>([]);
  const [duplicateCheckResult, setDuplicateCheckResult] = useState<any>(null);

  // Update form data
  const updateFormData = useCallback(
    (stepData: Partial<HouseholdRegistrationData>) => {
      setFormData(prev => ({ ...prev, ...stepData }));
    },
    []
  );

  // Validate data with backend
  const validateWithBackend = useCallback(async () => {
    if (!formData.personalInfo.firstName || !formData.personalInfo.lastName) {
      return;
    }

    setIsValidating(true);
    try {
      // Validate household data
      const validationResult =
        await dataManagementApi.validateHouseholdData(formData);

      if (validationResult.warnings.length > 0) {
        setValidationWarnings(validationResult.warnings.map(w => w.message));
      } else {
        setValidationWarnings([]);
      }

      // Check for duplicates
      const duplicateResult = await dataManagementApi.checkDuplicates(formData);
      setDuplicateCheckResult(duplicateResult);
    } catch (error) {
      console.warn('Backend validation failed:', error);
      // Continue with client-side validation only
    } finally {
      setIsValidating(false);
    }
  }, [formData]);

  // Check for duplicates when personal info changes
  React.useEffect(() => {
    if (
      currentStep >= 1 &&
      formData.personalInfo.firstName &&
      formData.personalInfo.lastName
    ) {
      const timeoutId = setTimeout(validateWithBackend, 1000);
      return () => clearTimeout(timeoutId);
    }
    return undefined;
  }, [formData.personalInfo, currentStep, validateWithBackend]);

  // Validate current step
  const validateCurrentStep = useCallback((): boolean => {
    const stepId = STEPS[currentStep].id;
    const errors: string[] = [];

    switch (stepId) {
      case 'personal':
        if (!formData.personalInfo.firstName)
          errors.push('First name is required');
        if (!formData.personalInfo.lastName)
          errors.push('Last name is required');
        if (!formData.personalInfo.birthDate)
          errors.push('Birth date is required');
        if (!formData.personalInfo.gender) errors.push('Gender is required');
        if (!formData.personalInfo.contactNumber)
          errors.push('Contact number is required');
        break;
      case 'household':
        if (formData.householdMembers.length === 0) {
          errors.push('At least one household member is required');
        }
        break;
      case 'socioeconomic':
        if (!formData.socioEconomicInfo.primaryIncomeSource) {
          errors.push('Primary income source is required');
        }
        if (!formData.socioEconomicInfo.housingType) {
          errors.push('Housing type is required');
        }
        break;
      case 'documents':
        if (formData.documents.length === 0) {
          errors.push('At least one supporting document is required');
        }
        break;
      case 'review':
        if (!formData.consent.dataProcessing) {
          errors.push('Data processing consent is required');
        }
        if (!formData.consent.informationSharing) {
          errors.push('Information sharing consent is required');
        }
        break;
    }

    setStepErrors(prev => ({ ...prev, [stepId]: errors }));
    return errors.length === 0;
  }, [currentStep, formData]);

  // Navigate to next step
  const handleNext = useCallback(() => {
    if (validateCurrentStep() && currentStep < STEPS.length - 1) {
      setCurrentStep(prev => prev + 1);
    }
  }, [currentStep, validateCurrentStep]);

  // Navigate to previous step
  const handlePrevious = useCallback(() => {
    if (currentStep > 0) {
      setCurrentStep(prev => prev - 1);
    }
  }, [currentStep]);

  // Handle form submission
  const handleSubmit = useCallback(async () => {
    if (validateCurrentStep()) {
      await onSubmit(formData);
    }
  }, [formData, onSubmit, validateCurrentStep]);

  // Render current step content
  const renderStepContent = () => {
    const stepId = STEPS[currentStep].id;
    const errors = stepErrors[stepId] || [];

    switch (stepId) {
      case 'personal':
        return (
          <PersonalInformationStep
            data={formData.personalInfo}
            address={formData.address}
            onUpdate={updateFormData}
            errors={errors}
            currentUser={currentUser}
          />
        );
      case 'household':
        return (
          <HouseholdCompositionStep
            data={formData.householdMembers}
            onUpdate={updateFormData}
            errors={errors}
          />
        );
      case 'socioeconomic':
        return (
          <SocioEconomicStep
            data={formData.socioEconomicInfo}
            onUpdate={updateFormData}
            errors={errors}
          />
        );
      case 'documents':
        return (
          <DocumentUploadStep
            data={formData.documents}
            onUpdate={updateFormData}
            errors={errors}
          />
        );
      case 'review':
        return (
          <ReviewSubmitStep
            data={formData}
            onUpdate={updateFormData}
            errors={errors}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className='space-y-6'>
      {/* Progress Indicator */}
      <Card className='p-6'>
        <div className='flex items-center justify-between mb-4'>
          <h2 className='text-lg font-semibold text-gray-900'>
            Step {currentStep + 1} of {STEPS.length}: {STEPS[currentStep].title}
          </h2>
          <span className='text-sm text-gray-500'>
            {Math.round(((currentStep + 1) / STEPS.length) * 100)}% Complete
          </span>
        </div>

        {/* Progress Bar */}
        <div className='w-full bg-gray-200 rounded-full h-2 mb-4'>
          <div
            className='bg-primary-600 h-2 rounded-full transition-all duration-300'
            style={{ width: `${((currentStep + 1) / STEPS.length) * 100}%` }}
          />
        </div>

        {/* Step Indicators */}
        <div className='flex justify-between'>
          {STEPS.map((step, index) => (
            <div
              key={step.id}
              className={`flex flex-col items-center text-xs ${
                index <= currentStep ? 'text-primary-600' : 'text-gray-400'
              }`}
            >
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center mb-1 ${
                  index < currentStep
                    ? 'bg-primary-600 text-white'
                    : index === currentStep
                      ? 'bg-primary-100 text-primary-600 border-2 border-primary-600'
                      : 'bg-gray-200 text-gray-400'
                }`}
              >
                {index < currentStep ? (
                  <svg
                    className='w-4 h-4'
                    fill='currentColor'
                    viewBox='0 0 20 20'
                  >
                    <path
                      fillRule='evenodd'
                      d='M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z'
                      clipRule='evenodd'
                    />
                  </svg>
                ) : (
                  index + 1
                )}
              </div>
              <span className='text-center max-w-20'>{step.title}</span>
            </div>
          ))}
        </div>
      </Card>

      {/* Step Content */}
      <Card className='p-6'>
        <div className='mb-6'>
          <h3 className='text-xl font-semibold text-gray-900 mb-2'>
            {STEPS[currentStep].title}
          </h3>
          <p className='text-gray-600'>{STEPS[currentStep].description}</p>
        </div>

        {/* Error Alert */}
        {stepErrors[STEPS[currentStep].id]?.length > 0 && (
          <Alert
            variant='error'
            title='Please fix the following errors:'
            className='mb-6'
          >
            <ul className='list-disc list-inside space-y-1'>
              {stepErrors[STEPS[currentStep].id].map((error, index) => (
                <li key={index}>{error}</li>
              ))}
            </ul>
          </Alert>
        )}

        {/* Validation Warnings */}
        {validationWarnings.length > 0 && (
          <Alert
            variant='warning'
            title='Data Quality Warnings:'
            className='mb-6'
          >
            <ul className='list-disc list-inside space-y-1'>
              {validationWarnings.map((warning, index) => (
                <li key={index}>{warning}</li>
              ))}
            </ul>
          </Alert>
        )}

        {/* Duplicate Check Alert */}
        {duplicateCheckResult?.isDuplicate && (
          <Alert
            variant='warning'
            title='Potential Duplicate Registration Found'
            className='mb-6'
          >
            <div className='space-y-2'>
              <p>
                We found {duplicateCheckResult.matches.length} similar
                registration(s) in the system:
              </p>
              {duplicateCheckResult.matches
                .slice(0, 3)
                .map((match: any, index: number) => (
                  <div key={index} className='bg-yellow-50 p-3 rounded border'>
                    <p className='font-medium'>{match.householdHead}</p>
                    <p className='text-sm text-gray-600'>PSN: {match.psn}</p>
                    <p className='text-sm text-gray-600'>
                      Address: {match.address}
                    </p>
                    <p className='text-sm text-gray-600'>
                      Match Score: {Math.round(match.matchScore * 100)}%
                    </p>
                  </div>
                ))}
              <p className='text-sm text-gray-600 mt-2'>
                Please verify this is not a duplicate registration. If you
                believe this is a new household, you may continue.
              </p>
            </div>
          </Alert>
        )}

        {/* Validation Loading */}
        {isValidating && (
          <Alert variant='info' title='Validating Data...' className='mb-6'>
            <div className='flex items-center space-x-2'>
              <div className='animate-spin rounded-full h-4 w-4 border-b-2 border-primary-600'></div>
              <span>Checking data quality and duplicates...</span>
            </div>
          </Alert>
        )}

        {/* Step Content */}
        {renderStepContent()}
      </Card>

      {/* Navigation Buttons */}
      <div className='flex justify-between'>
        <div>
          {currentStep > 0 && (
            <Button
              variant='outline'
              onClick={handlePrevious}
              disabled={isSubmitting}
            >
              Previous
            </Button>
          )}
        </div>

        <div className='flex space-x-3'>
          <Button variant='outline' onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>

          {currentStep < STEPS.length - 1 ? (
            <Button onClick={handleNext} disabled={isSubmitting}>
              Next
            </Button>
          ) : (
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              {isSubmitting ? 'Submitting...' : 'Submit Registration'}
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};
