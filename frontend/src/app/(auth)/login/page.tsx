'use client';

// Login Page
// User authentication page with form validation and error handling

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';

import { Button, Alert, Loading } from '@/components/ui';
import { Form, FormInput, FormCheckbox, FormSubmitButton } from '@/components/forms';
import { useAuthActions } from '@/contexts';
import { loginSchema, type LoginFormData } from '@/lib/validations';
import { cn } from '@/utils';

// Login page component
export default function LoginPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  const { login } = useAuthActions();
  const router = useRouter();
  const searchParams = useSearchParams();
  
  // Get redirect URL from query params
  const redirectTo = searchParams.get('redirect') || '/dashboard';

  const handleSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await login(data);
      setSuccess('Login successful! Redirecting...');
      
      // Small delay to show success message
      setTimeout(() => {
        router.push(redirectTo);
      }, 1000);
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check your credentials and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header */}
        <div className="text-center">
          <div className="mx-auto h-12 w-12 bg-primary-600 rounded-lg flex items-center justify-center mb-4">
            <span className="text-white font-bold text-lg">DSR</span>
          </div>
          <h2 className="text-3xl font-bold text-gray-900">
            Sign in to your account
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Access the Dynamic Social Registry portal
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

        {/* Login Form */}
        <Form
          schema={loginSchema}
          onSubmit={handleSubmit}
          defaultValues={{
            email: '',
            password: '',
            rememberMe: false,
          }}
          className="space-y-6"
        >
          <div className="space-y-4">
            <FormInput
              name="email"
              type="email"
              label="Email address"
              placeholder="Enter your email"
              required
              autoComplete="email"
              leftIcon={
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                </svg>
              }
            />

            <FormInput
              name="password"
              type="password"
              label="Password"
              placeholder="Enter your password"
              required
              autoComplete="current-password"
              showPasswordToggle
              leftIcon={
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              }
            />
          </div>

          <div className="flex items-center justify-between">
            <FormCheckbox
              name="rememberMe"
              label="Remember me"
              className="text-sm"
            />

            <Link
              href="/auth/forgot-password"
              className="text-sm text-primary-600 hover:text-primary-500 font-medium"
            >
              Forgot your password?
            </Link>
          </div>

          <FormSubmitButton
            loading={isLoading}
            fullWidth
            size="lg"
            className="relative"
          >
            {isLoading ? 'Signing in...' : 'Sign in'}
          </FormSubmitButton>
        </Form>

        {/* Register Link */}
        <div className="text-center">
          <p className="text-sm text-gray-600">
            Don't have an account?{' '}
            <Link
              href="/auth/register"
              className="font-medium text-primary-600 hover:text-primary-500"
            >
              Sign up here
            </Link>
          </p>
        </div>

        {/* Demo Accounts */}
        <div className="mt-8 p-4 bg-gray-100 rounded-lg">
          <h3 className="text-sm font-medium text-gray-900 mb-2">Demo Accounts</h3>
          <div className="space-y-2 text-xs text-gray-600">
            <div>
              <strong>Admin:</strong> admin@dsr.gov.ph / admin123
            </div>
            <div>
              <strong>Citizen:</strong> citizen@dsr.gov.ph / citizen123
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center text-xs text-gray-500">
          <p>
            By signing in, you agree to our{' '}
            <Link href="/terms" className="text-primary-600 hover:text-primary-500">
              Terms of Service
            </Link>{' '}
            and{' '}
            <Link href="/privacy" className="text-primary-600 hover:text-primary-500">
              Privacy Policy
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
