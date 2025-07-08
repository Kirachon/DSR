'use client';

// Email Verification Page
// Email verification page with token validation and resend functionality

import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { Form, FormInput, FormSubmitButton } from '@/components/forms';
import { Button, Alert, Card, Loading } from '@/components/ui';
import { useAuthActions } from '@/contexts';
import {
  verifyEmailSchema,
  resendVerificationSchema,
  type VerifyEmailFormData,
  type ResendVerificationFormData,
} from '@/lib/validations';
import type { VerifyEmailRequest, ResendVerificationRequest } from '@/types';

// Email Verification page component
export default function VerifyEmailPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isVerified, setIsVerified] = useState(false);
  const [showResendForm, setShowResendForm] = useState(false);

  const { verifyEmail, resendVerification } = useAuthActions();
  const router = useRouter();
  const searchParams = useSearchParams();

  // Auto-verify if token is in URL
  useEffect(() => {
    const tokenParam = searchParams.get('token');
    if (tokenParam) {
      setToken(tokenParam);
      handleAutoVerify(tokenParam);
    }
  }, [searchParams]);

  const handleAutoVerify = async (verificationToken: string) => {
    setIsLoading(true);
    setError(null);

    try {
      await verifyEmail({ token: verificationToken });
      setSuccess(
        'Email verified successfully! You can now access all features.'
      );
      setIsVerified(true);
    } catch (err: any) {
      setError(
        err.message ||
          'Email verification failed. The token may be invalid or expired.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleManualVerify = async (data: VerifyEmailFormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      if (!data.token) {
        throw new Error('Verification token is required');
      }

      await verifyEmail(data as VerifyEmailRequest);
      setSuccess(
        'Email verified successfully! You can now access all features.'
      );
      setIsVerified(true);
    } catch (err: any) {
      setError(
        err.message ||
          'Email verification failed. Please check your token and try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendVerification = async (data: ResendVerificationFormData) => {
    setIsResending(true);
    setError(null);
    setSuccess(null);

    try {
      if (!data.email) {
        throw new Error('Email is required');
      }

      await resendVerification(data as ResendVerificationRequest);
      setSuccess('Verification email sent! Please check your inbox.');
      setShowResendForm(false);
    } catch (err: any) {
      setError(
        err.message || 'Failed to send verification email. Please try again.'
      );
    } finally {
      setIsResending(false);
    }
  };

  if (isLoading && token) {
    return (
      <div className='min-h-screen flex items-center justify-center bg-gray-50'>
        <div className='text-center'>
          <Loading size='xl' text='Verifying your email...' />
        </div>
      </div>
    );
  }

  if (isVerified) {
    return (
      <div className='min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8'>
        <div className='max-w-md w-full space-y-8'>
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
              Email Verified!
            </h2>
            <p className='mt-2 text-sm text-gray-600'>
              Your email address has been successfully verified
            </p>
          </div>

          <Alert variant='success'>{success}</Alert>

          <div className='space-y-4'>
            <Button
              variant='primary'
              fullWidth
              size='lg'
              onClick={() => router.push('/dashboard')}
            >
              Continue to Dashboard
            </Button>

            <div className='text-center'>
              <Link
                href='/auth/login'
                className='text-sm text-primary-600 hover:text-primary-500 font-medium'
              >
                Sign in to your account
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
                d='M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207'
              />
            </svg>
          </div>
          <h2 className='text-3xl font-bold text-gray-900'>
            Verify your email
          </h2>
          <p className='mt-2 text-sm text-gray-600'>
            Enter your verification token or check your email for the
            verification link
          </p>
        </div>

        {/* Alert Messages */}
        {error && (
          <Alert variant='error' dismissible onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        {success && <Alert variant='success'>{success}</Alert>}

        {/* Manual Verification Form */}
        {!showResendForm && (
          <Card className='p-6'>
            <Form
              schema={verifyEmailSchema}
              onSubmit={handleManualVerify}
              defaultValues={{
                token: token || '',
              }}
              className='space-y-6'
            >
              <FormInput
                name='token'
                label='Verification Token'
                placeholder='Enter your verification token'
                required
                description='Copy the token from your verification email'
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
                      d='M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z'
                    />
                  </svg>
                }
              />

              <FormSubmitButton loading={isLoading} fullWidth size='lg'>
                {isLoading ? 'Verifying...' : 'Verify Email'}
              </FormSubmitButton>
            </Form>
          </Card>
        )}

        {/* Resend Verification Form */}
        {showResendForm && (
          <Card className='p-6'>
            <div className='mb-4'>
              <h3 className='text-lg font-medium text-gray-900'>
                Resend Verification Email
              </h3>
              <p className='text-sm text-gray-600'>
                Enter your email address to receive a new verification link
              </p>
            </div>

            <Form
              schema={resendVerificationSchema}
              onSubmit={handleResendVerification}
              defaultValues={{
                email: '',
              }}
              className='space-y-6'
            >
              <FormInput
                name='email'
                type='email'
                label='Email Address'
                placeholder='Enter your email address'
                required
                autoComplete='email'
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

              <div className='flex space-x-3'>
                <FormSubmitButton
                  loading={isResending}
                  variant='primary'
                  className='flex-1'
                >
                  {isResending ? 'Sending...' : 'Send Verification Email'}
                </FormSubmitButton>

                <Button
                  variant='outline'
                  onClick={() => setShowResendForm(false)}
                  disabled={isResending}
                >
                  Cancel
                </Button>
              </div>
            </Form>
          </Card>
        )}

        {/* Actions */}
        <div className='space-y-4'>
          {!showResendForm && (
            <div className='text-center'>
              <button
                onClick={() => setShowResendForm(true)}
                className='text-sm text-primary-600 hover:text-primary-500 font-medium'
              >
                Didn't receive the email? Resend verification
              </button>
            </div>
          )}

          <div className='text-center'>
            <Link
              href='/auth/login'
              className='text-sm text-gray-500 hover:text-gray-700 inline-flex items-center'
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
        </div>

        {/* Help Section */}
        <Card className='p-4 bg-blue-50 border-blue-200'>
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
                Verification Help
              </h3>
              <div className='mt-2 text-sm text-blue-700'>
                <ul className='list-disc list-inside space-y-1'>
                  <li>
                    Check your spam/junk folder for the verification email
                  </li>
                  <li>Verification links expire after 24 hours</li>
                  <li>
                    Make sure you're using the same email address you registered
                    with
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
