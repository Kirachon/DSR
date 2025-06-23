'use client';

// Reset Password Page
// Password reset page with token validation and new password form

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';

import { Button, Alert, Card } from '@/components/ui';
import { Form, FormInput, FormSubmitButton, FormGrid } from '@/components/forms';
import { useAuthActions } from '@/contexts';
import { resetPasswordSchema, type ResetPasswordFormData } from '@/lib/validations';
import { getPasswordStrength } from '@/lib/validations';

// Reset Password page component
export default function ResetPasswordPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [passwordStrength, setPasswordStrength] = useState({ score: 0, feedback: [] });
  
  const { resetPassword } = useAuthActions();
  const router = useRouter();
  const searchParams = useSearchParams();

  // Get token from URL params
  useEffect(() => {
    const tokenParam = searchParams.get('token');
    if (tokenParam) {
      setToken(tokenParam);
    } else {
      setError('Invalid or missing reset token. Please request a new password reset.');
    }
  }, [searchParams]);

  const handleSubmit = async (data: ResetPasswordFormData) => {
    if (!token) {
      setError('Invalid reset token. Please request a new password reset.');
      return;
    }

    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await resetPassword({
        ...data,
        token,
      });
      setSuccess('Password reset successful! Redirecting to login...');
      
      // Redirect to login after success
      setTimeout(() => {
        router.push('/auth/login?message=password-reset-success');
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Failed to reset password. Please try again or request a new reset link.');
    } finally {
      setIsLoading(false);
    }
  };

  // Handle password strength checking
  const handlePasswordChange = (password: string) => {
    const strength = getPasswordStrength(password);
    setPasswordStrength(strength);
  };

  if (!token && !error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Validating reset token...</p>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div className="text-center">
            <div className="mx-auto h-12 w-12 bg-success-600 rounded-lg flex items-center justify-center mb-4">
              <svg className="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-3xl font-bold text-gray-900">Password Reset Complete</h2>
            <p className="mt-2 text-sm text-gray-600">Your password has been successfully updated</p>
          </div>

          <Alert variant="success">
            {success}
          </Alert>

          <div className="text-center">
            <Link
              href="/auth/login"
              className="text-primary-600 hover:text-primary-500 font-medium"
            >
              Continue to sign in
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div className="text-center">
          <div className="mx-auto h-12 w-12 bg-primary-600 rounded-lg flex items-center justify-center mb-4">
            <svg className="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
          <h2 className="text-3xl font-bold text-gray-900">
            Reset your password
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Enter your new password below
          </p>
        </div>

        {/* Alert Messages */}
        {error && (
          <Alert variant="error" dismissible onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Reset Password Form */}
        {token && (
          <Card className="p-6">
            <Form
              schema={resetPasswordSchema}
              onSubmit={handleSubmit}
              defaultValues={{
                newPassword: '',
                confirmPassword: '',
              }}
              className="space-y-6"
            >
              <FormInput
                name="newPassword"
                type="password"
                label="New Password"
                placeholder="Enter your new password"
                required
                autoComplete="new-password"
                showPasswordToggle
                description="Must be at least 8 characters with uppercase, lowercase, number, and special character"
                onChange={(e) => handlePasswordChange(e.target.value)}
                leftIcon={
                  <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                }
              />

              {/* Password Strength Indicator */}
              {passwordStrength.score > 0 && (
                <div className="space-y-2">
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-600">Password strength:</span>
                    <div className="flex-1 bg-gray-200 rounded-full h-2">
                      <div
                        className={`h-2 rounded-full transition-all duration-300 ${
                          passwordStrength.score <= 2 ? 'bg-error-500' :
                          passwordStrength.score <= 3 ? 'bg-warning-500' :
                          'bg-success-500'
                        }`}
                        style={{ width: `${(passwordStrength.score / 5) * 100}%` }}
                      />
                    </div>
                    <span className={`text-sm font-medium ${
                      passwordStrength.score <= 2 ? 'text-error-600' :
                      passwordStrength.score <= 3 ? 'text-warning-600' :
                      'text-success-600'
                    }`}>
                      {passwordStrength.score <= 2 ? 'Weak' :
                       passwordStrength.score <= 3 ? 'Fair' :
                       passwordStrength.score <= 4 ? 'Good' : 'Strong'}
                    </span>
                  </div>
                  {passwordStrength.feedback.length > 0 && (
                    <ul className="text-xs text-gray-600 space-y-1">
                      {passwordStrength.feedback.map((feedback, index) => (
                        <li key={index} className="flex items-center space-x-1">
                          <span className="text-error-500">•</span>
                          <span>{feedback}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              )}

              <FormInput
                name="confirmPassword"
                type="password"
                label="Confirm New Password"
                placeholder="Confirm your new password"
                required
                autoComplete="new-password"
                showPasswordToggle
                leftIcon={
                  <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                }
              />

              <FormSubmitButton
                loading={isLoading}
                fullWidth
                size="lg"
              >
                {isLoading ? 'Resetting Password...' : 'Reset Password'}
              </FormSubmitButton>
            </Form>
          </Card>
        )}

        {/* Back to Login */}
        <div className="text-center">
          <Link
            href="/auth/login"
            className="text-sm text-primary-600 hover:text-primary-500 font-medium inline-flex items-center"
          >
            <svg className="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to sign in
          </Link>
        </div>
      </div>
    </div>
  );
}
