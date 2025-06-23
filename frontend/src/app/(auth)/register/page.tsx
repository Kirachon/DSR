'use client';

// Register Page
// User registration page with role selection and comprehensive form validation

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

import { Button, Alert, Card } from '@/components/ui';
import { Form, FormInput, FormSelect, FormCheckbox, FormSubmitButton, FormGrid, FormSection } from '@/components/forms';
import { useAuthActions } from '@/contexts';
import { registerSchema, type RegisterFormData } from '@/lib/validations';
import type { UserRole } from '@/types';

// Role options for registration
const roleOptions = [
  { value: 'CITIZEN', label: 'Citizen - Register for social services and benefits' },
  { value: 'LGU_STAFF', label: 'LGU Staff - Local Government Unit employee' },
  { value: 'DSWD_STAFF', label: 'DSWD Staff - Department of Social Welfare employee' },
];

// Register page component
export default function RegisterPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  const { register } = useAuthActions();
  const router = useRouter();

  const handleSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await register(data);
      setSuccess('Registration successful! Redirecting to dashboard...');
      
      // Small delay to show success message
      setTimeout(() => {
        router.push('/dashboard');
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please check your information and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl w-full space-y-8">
        {/* Header */}
        <div className="text-center">
          <div className="mx-auto h-12 w-12 bg-primary-600 rounded-lg flex items-center justify-center mb-4">
            <span className="text-white font-bold text-lg">DSR</span>
          </div>
          <h2 className="text-3xl font-bold text-gray-900">
            Create your account
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Join the Dynamic Social Registry platform
          </p>
        </div>

        {/* Alert Messages */}
        {error && (
          <Alert variant="error" dismissible onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert variant="success">
            {success}
          </Alert>
        )}

        {/* Registration Form */}
        <Card className="p-6">
          <Form
            schema={registerSchema}
            onSubmit={handleSubmit}
            defaultValues={{
              email: '',
              password: '',
              confirmPassword: '',
              firstName: '',
              lastName: '',
              phoneNumber: '',
              dateOfBirth: '',
              address: '',
              role: 'CITIZEN' as UserRole,
              acceptTerms: false,
            }}
            className="space-y-6"
          >
            {/* Account Information */}
            <FormSection
              title="Account Information"
              description="Basic account details for authentication"
            >
              <FormGrid columns={2}>
                <FormInput
                  name="firstName"
                  label="First Name"
                  placeholder="Enter your first name"
                  required
                  autoComplete="given-name"
                />

                <FormInput
                  name="lastName"
                  label="Last Name"
                  placeholder="Enter your last name"
                  required
                  autoComplete="family-name"
                />
              </FormGrid>

              <FormInput
                name="email"
                type="email"
                label="Email Address"
                placeholder="Enter your email address"
                required
                autoComplete="email"
                description="This will be used for login and notifications"
              />

              <FormGrid columns={2}>
                <FormInput
                  name="password"
                  type="password"
                  label="Password"
                  placeholder="Create a strong password"
                  required
                  autoComplete="new-password"
                  showPasswordToggle
                  description="Must be at least 8 characters with uppercase, lowercase, number, and special character"
                />

                <FormInput
                  name="confirmPassword"
                  type="password"
                  label="Confirm Password"
                  placeholder="Confirm your password"
                  required
                  autoComplete="new-password"
                  showPasswordToggle
                />
              </FormGrid>
            </FormSection>

            {/* Personal Information */}
            <FormSection
              title="Personal Information"
              description="Additional details for your profile (optional)"
            >
              <FormGrid columns={2}>
                <FormInput
                  name="phoneNumber"
                  type="tel"
                  label="Phone Number"
                  placeholder="+63 9XX XXX XXXX"
                  autoComplete="tel"
                />

                <FormInput
                  name="dateOfBirth"
                  type="date"
                  label="Date of Birth"
                  autoComplete="bday"
                />
              </FormGrid>

              <FormInput
                name="address"
                label="Address"
                placeholder="Enter your complete address"
                autoComplete="street-address"
              />
            </FormSection>

            {/* Role Selection */}
            <FormSection
              title="Account Type"
              description="Select your role in the system"
            >
              <FormSelect
                name="role"
                label="Role"
                options={roleOptions}
                required
                description="Choose the role that best describes your position"
              />
            </FormSection>

            {/* Terms and Conditions */}
            <FormSection title="Terms and Conditions">
              <FormCheckbox
                name="acceptTerms"
                required
                label={
                  <span>
                    I agree to the{' '}
                    <Link href="/terms" className="text-primary-600 hover:text-primary-500 font-medium">
                      Terms of Service
                    </Link>{' '}
                    and{' '}
                    <Link href="/privacy" className="text-primary-600 hover:text-primary-500 font-medium">
                      Privacy Policy
                    </Link>
                  </span>
                }
                description="You must accept the terms and conditions to create an account"
              />
            </FormSection>

            {/* Submit Button */}
            <FormSubmitButton
              loading={isLoading}
              fullWidth
              size="lg"
            >
              {isLoading ? 'Creating Account...' : 'Create Account'}
            </FormSubmitButton>
          </Form>
        </Card>

        {/* Login Link */}
        <div className="text-center">
          <p className="text-sm text-gray-600">
            Already have an account?{' '}
            <Link
              href="/auth/login"
              className="font-medium text-primary-600 hover:text-primary-500"
            >
              Sign in here
            </Link>
          </p>
        </div>

        {/* Additional Information */}
        <Card className="p-4 bg-blue-50 border-blue-200">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-blue-800">
                Account Verification
              </h3>
              <div className="mt-2 text-sm text-blue-700">
                <p>
                  After registration, you may need to verify your email address and wait for account approval 
                  depending on your selected role. LGU and DSWD staff accounts require additional verification.
                </p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
