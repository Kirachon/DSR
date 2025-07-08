'use client';

// Login Page
// User authentication page with form validation and error handling

import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import React, { useState } from 'react';

import {
  Form,
  FormInput,
  FormCheckbox,
  FormSubmitButton,
} from '@/components/forms';
import { Button, Alert, Loading } from '@/components/ui';
import { useAuth, useAuthActions } from '@/contexts';
import { loginSchema, type LoginFormData } from '@/lib/validations';
import type { LoginRequest } from '@/types';
import { cn } from '@/utils';

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectTo = searchParams.get('redirect') || '/dashboard';

  const { isLoading, error } = useAuth();
  const { login, clearError } = useAuthActions();

  const [rememberMe, setRememberMe] = useState(false);

  const handleSubmit = async (data: LoginFormData) => {
    try {
      clearError();
      
      const loginRequest: LoginRequest = {
        email: data.email,
        password: data.password,
        rememberMe,
      };

      await login(loginRequest);
      
      // Redirect will be handled by the auth context
      router.push(redirectTo);
    } catch (error) {
      // Error handling is managed by the auth store
      console.error('Login failed:', error);
    }
  };

  return (
    <div className='min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8'>
      <div className='max-w-md w-full space-y-8'>
        {/* Header */}
        <div className='text-center'>
          <div className='mx-auto h-12 w-12 bg-primary-600 rounded-lg flex items-center justify-center mb-4'>
            <span className='text-white font-bold text-lg'>DSR</span>
          </div>
          <h2 className='text-3xl font-bold text-gray-900'>
            Sign in to your account
          </h2>
          <p className='mt-2 text-sm text-gray-600'>
            Access the Digital Social Registry portal
          </p>
        </div>

        {/* Error Alert */}
        {error && (
          <Alert variant='destructive' className='mb-4'>
            {error}
          </Alert>
        )}

        {/* Login Form */}
        <Form
          schema={loginSchema}
          onSubmit={handleSubmit}
          className='space-y-6'
        >
          <div className='space-y-4'>
            <FormInput
              name='email'
              type='email'
              label='Email address'
              placeholder='Enter your email address'
              required
              autoComplete='email'
              className='block w-full'
            />

            <FormInput
              name='password'
              type='password'
              label='Password'
              placeholder='Enter your password'
              required
              autoComplete='current-password'
              className='block w-full'
            />

            <div className='flex items-center justify-between'>
              <FormCheckbox
                name='rememberMe'
                label='Remember me'
                checked={rememberMe}
                onChange={setRememberMe}
              />

              <Link
                href='/auth/forgot-password'
                className='text-sm text-primary-600 hover:text-primary-500'
              >
                Forgot your password?
              </Link>
            </div>
          </div>

          <FormSubmitButton
            loading={isLoading}
            loadingText='Signing in...'
            className='w-full'
          >
            Sign in
          </FormSubmitButton>
        </Form>

        {/* Register Link */}
        <div className='text-center'>
          <p className='text-sm text-gray-600'>
            Don't have an account?{' '}
            <Link
              href='/auth/register'
              className='font-medium text-primary-600 hover:text-primary-500'
            >
              Create an account
            </Link>
          </p>
        </div>

        {/* Additional Links */}
        <div className='text-center space-y-2'>
          <div>
            <Link
              href='/'
              className='text-sm text-gray-500 hover:text-gray-700'
            >
              ← Back to home
            </Link>
          </div>
          <div>
            <Link
              href='/auth/help'
              className='text-sm text-gray-500 hover:text-gray-700'
            >
              Need help signing in?
            </Link>
          </div>
        </div>

        {/* Demo Accounts - Only show in development */}
        {process.env.NODE_ENV === 'development' && (
          <div className='mt-8 p-4 bg-yellow-50 border border-yellow-200 rounded-lg'>
            <h3 className='text-sm font-medium text-yellow-800 mb-2'>
              Demo Accounts (Development Only)
            </h3>
            <div className='space-y-2 text-xs text-yellow-700'>
              <div>
                <strong>Admin:</strong> admin@dsr.gov.ph / admin123
              </div>
              <div>
                <strong>Staff:</strong> staff@dsr.gov.ph / staff123
              </div>
              <div>
                <strong>Citizen:</strong> citizen@dsr.gov.ph / citizen123
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
