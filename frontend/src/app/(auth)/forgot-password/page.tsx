'use client';

// Forgot Password Page
// Password reset request page with email validation

import Link from 'next/link';
import React, { useState } from 'react';

import { Form, FormInput, FormSubmitButton } from '@/components/forms';
import { Button, Alert, Card } from '@/components/ui';
import { useAuthActions } from '@/contexts';
import {
  forgotPasswordSchema,
  type ForgotPasswordFormData,
} from '@/lib/validations';
import type { ForgotPasswordRequest } from '@/types';

// Forgot Password page component
export default function ForgotPasswordPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [emailSent, setEmailSent] = useState(false);

  const { forgotPassword } = useAuthActions();

  const handleSubmit = async (data: ForgotPasswordFormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      if (!data.email) {
        throw new Error('Email is required');
      }

      await forgotPassword(data as ForgotPasswordRequest);
      setSuccess(
        'Password reset instructions have been sent to your email address.'
      );
      setEmailSent(true);
    } catch (err: any) {
      setError(
        err.message || 'Failed to send password reset email. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  if (emailSent) {
    return (
      <div className='min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8'>
        <div className='max-w-md w-full space-y-8'>
          {/* Header */}
          <div className='text-center'>
            <div className='mx-auto h-12 w-12 bg-success-600 rounded-lg flex items-center justify-center mb-4'>
              <svg
                className='h-6 w-6 text-white'
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
            <h2 className='text-3xl font-bold text-gray-900'>
              Check your email
            </h2>
            <p className='mt-2 text-sm text-gray-600'>
              We've sent password reset instructions to your email address
            </p>
          </div>

          {/* Success Message */}
          <Alert variant='success'>{success}</Alert>

          {/* Instructions */}
          <Card className='p-6'>
            <div className='space-y-4'>
              <h3 className='text-lg font-medium text-gray-900'>Next steps:</h3>
              <ol className='list-decimal list-inside space-y-2 text-sm text-gray-600'>
                <li>Check your email inbox for a message from DSR</li>
                <li>Click the password reset link in the email</li>
                <li>Follow the instructions to create a new password</li>
                <li>Sign in with your new password</li>
              </ol>

              <div className='mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-md'>
                <div className='flex'>
                  <div className='flex-shrink-0'>
                    <svg
                      className='h-5 w-5 text-yellow-400'
                      fill='currentColor'
                      viewBox='0 0 20 20'
                    >
                      <path
                        fillRule='evenodd'
                        d='M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z'
                        clipRule='evenodd'
                      />
                    </svg>
                  </div>
                  <div className='ml-3'>
                    <p className='text-sm text-yellow-700'>
                      <strong>Didn't receive the email?</strong> Check your spam
                      folder or wait a few minutes for delivery.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </Card>

          {/* Actions */}
          <div className='space-y-4'>
            <Button
              variant='outline'
              fullWidth
              onClick={() => {
                setEmailSent(false);
                setSuccess(null);
                setError(null);
              }}
            >
              Try a different email address
            </Button>

            <div className='text-center'>
              <Link
                href='/auth/login'
                className='text-sm text-primary-600 hover:text-primary-500 font-medium'
              >
                Back to sign in
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className='min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8'>
      <div className='max-w-md w-full space-y-8'>
        {/* Header */}
        <div className='text-center'>
          <div className='mx-auto h-12 w-12 bg-primary-600 rounded-lg flex items-center justify-center mb-4'>
            <svg
              className='h-6 w-6 text-white'
              fill='none'
              stroke='currentColor'
              viewBox='0 0 24 24'
            >
              <path
                strokeLinecap='round'
                strokeLinejoin='round'
                strokeWidth={2}
                d='M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z'
              />
            </svg>
          </div>
          <h2 className='text-3xl font-bold text-gray-900'>
            Forgot your password?
          </h2>
          <p className='mt-2 text-sm text-gray-600'>
            Enter your email address and we'll send you a link to reset your
            password
          </p>
        </div>

        {/* Alert Messages */}
        {error && (
          <Alert variant='error' dismissible onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Forgot Password Form */}
        <Card className='p-6'>
          <Form
            schema={forgotPasswordSchema}
            onSubmit={handleSubmit}
            defaultValues={{
              email: '',
            }}
            className='space-y-6'
          >
            <FormInput
              name='email'
              type='email'
              label='Email address'
              placeholder='Enter your email address'
              required
              autoComplete='email'
              description="We'll send password reset instructions to this email"
              leftIcon={
                <svg
                  className='h-5 w-5 text-gray-400'
                  fill='none'
                  stroke='currentColor'
                  viewBox='0 0 24 24'
                >
                  <path
                    strokeLinecap='round'
                    strokeLinejoin='round'
                    strokeWidth={2}
                    d='M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207'
                  />
                </svg>
              }
            />

            <FormSubmitButton loading={isLoading} fullWidth size='lg'>
              {isLoading ? 'Sending...' : 'Send reset instructions'}
            </FormSubmitButton>
          </Form>
        </Card>

        {/* Back to Login */}
        <div className='text-center'>
          <Link
            href='/auth/login'
            className='text-sm text-primary-600 hover:text-primary-500 font-medium inline-flex items-center'
          >
            <svg
              className='h-4 w-4 mr-1'
              fill='none'
              stroke='currentColor'
              viewBox='0 0 24 24'
            >
              <path
                strokeLinecap='round'
                strokeLinejoin='round'
                strokeWidth={2}
                d='M10 19l-7-7m0 0l7-7m-7 7h18'
              />
            </svg>
            Back to sign in
          </Link>
        </div>

        {/* Help Section */}
        <Card className='p-4 bg-gray-50 border-gray-200'>
          <div className='text-center'>
            <h3 className='text-sm font-medium text-gray-900 mb-2'>
              Need help?
            </h3>
            <p className='text-xs text-gray-600 mb-3'>
              If you're having trouble accessing your account, contact our
              support team.
            </p>
            <Link
              href='/contact'
              className='text-sm text-primary-600 hover:text-primary-500 font-medium'
            >
              Contact Support
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
}
