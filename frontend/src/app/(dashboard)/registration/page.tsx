'use client';

// Household Registration Page
// Multi-step registration workflow for household registration

import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

import { HouseholdRegistrationWizard } from '@/components/registration/household-registration-wizard';
import { Card, Button, Alert } from '@/components/ui';
import { useAuth } from '@/contexts';
import { registrationApi } from '@/lib/api';
import type { HouseholdRegistrationData } from '@/types';

// Registration page component
export default function RegistrationPage() {
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // Handle form submission
  const handleSubmit = async (data: HouseholdRegistrationData) => {
    setIsSubmitting(true);
    setError(null);

    try {
      // Submit to Registration Service API
      console.log('Submitting registration data:', data);

      const response = await registrationApi.submitRegistration(data);
      console.log('Registration submitted successfully:', response);

      setSuccess(true);

      // Redirect to dashboard after successful registration
      setTimeout(() => {
        router.push('/dashboard');
      }, 3000);
    } catch (err) {
      console.error('Registration submission failed:', err);
      setError(
        err instanceof Error
          ? err.message
          : 'Registration failed. Please try again.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle cancellation
  const handleCancel = () => {
    router.push('/dashboard');
  };

  // Redirect if not authenticated
  if (!isAuthenticated) {
    router.push('/auth/login');
    return null;
  }

  // Show success message
  if (success) {
    return (
      <div className='max-w-2xl mx-auto p-6'>
        <Card className='p-8 text-center'>
          <div className='mb-6'>
            <div className='mx-auto w-16 h-16 bg-success-100 rounded-full flex items-center justify-center mb-4'>
              <svg
                className='w-8 h-8 text-success-600'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M5 13l4 4L19 7'
                />
              </svg>
            </div>
            <h1 className='text-2xl font-bold text-gray-900 mb-2'>
              Registration Submitted Successfully!
            </h1>
            <p className='text-gray-600'>
              Your household registration has been submitted for review. You
              will be notified once the verification process is complete.
            </p>
          </div>

          <div className='space-y-4'>
            <Alert variant='info' title="What's Next?">
              <ul className='text-left space-y-2 mt-2'>
                <li>• Your application will be reviewed by DSWD staff</li>
                <li>• You may be contacted for additional documentation</li>
                <li>• Verification typically takes 5-10 business days</li>
                <li>• You'll receive updates via email and SMS</li>
              </ul>
            </Alert>

            <Button
              onClick={() => router.push('/dashboard')}
              className='w-full'
            >
              Return to Dashboard
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className='max-w-4xl mx-auto p-6'>
      {/* Page Header */}
      <div className='mb-8'>
        <h1 className='text-3xl font-bold text-gray-900 mb-2'>
          Household Registration
        </h1>
        <p className='text-gray-600'>
          Register your household to access social protection programs and
          services.
        </p>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant='error' title='Registration Error' className='mb-6'>
          {error}
        </Alert>
      )}

      {/* Registration Wizard */}
      <HouseholdRegistrationWizard
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isSubmitting={isSubmitting}
        currentUser={user}
      />
    </div>
  );
}
