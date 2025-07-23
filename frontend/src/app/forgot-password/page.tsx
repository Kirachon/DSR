'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    email: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async () => {
    setIsLoading(true);
    setError('');

    try {
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      if (formData.email) {
        setSuccess(true);
      } else {
        setError('Please enter your email address');
      }
    } catch (err) {
      setError('Failed to send reset email. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  if (success) {
    return (
      <div className='min-h-screen flex items-center justify-center' style={{ 
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }}>
        <div className='max-w-md w-full mx-4'>
          <div className='p-8 rounded-2xl text-center' style={{ 
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 20px 40px rgba(0, 0, 0, 0.1)'
          }}>
            <div className='h-16 w-16 rounded-2xl flex items-center justify-center mx-auto mb-4' style={{ 
              background: 'linear-gradient(45deg, #10b981, #059669)',
              boxShadow: '0 4px 15px rgba(16, 185, 129, 0.3)'
            }}>
              <span className='text-2xl'>✉️</span>
            </div>
            <h1 className='text-3xl font-bold text-gray-900 mb-4'>Check Your Email</h1>
            <p className='text-gray-600 mb-6'>
              We've sent a password reset link to <strong>{formData.email}</strong>
            </p>
            <p className='text-sm text-gray-500 mb-8'>
              Didn't receive the email? Check your spam folder or try again.
            </p>
            <div className='space-y-4'>
              <Link href='/login' className='block w-full px-6 py-3 rounded-lg font-semibold text-white transition-all duration-200 hover:scale-105 text-center' style={{ background: 'linear-gradient(45deg, #3b82f6, #1d4ed8)' }}>
                Back to Login
              </Link>
              <button
                onClick={() => setSuccess(false)}
                className='block w-full px-6 py-3 rounded-lg font-medium text-gray-600 hover:text-gray-900 transition-all duration-200'
              >
                Try Different Email
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className='min-h-screen flex items-center justify-center' style={{ 
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <div className='max-w-md w-full mx-4'>
        <div className='p-8 rounded-2xl' style={{ 
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.1)'
        }}>
          <div className='text-center mb-8'>
            <div className='h-16 w-16 rounded-2xl flex items-center justify-center mx-auto mb-4' style={{ 
              background: 'linear-gradient(45deg, #f59e0b, #d97706)',
              boxShadow: '0 4px 15px rgba(245, 158, 11, 0.3)'
            }}>
              <span className='font-bold text-2xl text-white'>🔑</span>
            </div>
            <h1 className='text-3xl font-bold text-gray-900 mb-2'>Forgot Password?</h1>
            <p className='text-gray-600'>Enter your email to reset your password</p>
          </div>

          {error && (
            <div className='mb-6 p-4 rounded-lg' style={{ backgroundColor: '#fee2e2', color: '#dc2626' }}>
              {error}
            </div>
          )}

          <div className='space-y-6'>
            <div>
              <label htmlFor='email' className='block text-sm font-medium text-gray-700 mb-2'>
                Email Address
              </label>
              <input
                type='email'
                id='email'
                name='email'
                value={formData.email}
                onChange={handleInputChange}
                required
                className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all duration-200'
                placeholder='Enter your email address'
                style={{ backgroundColor: '#f9fafb' }}
              />
            </div>

            <button
              type='button'
              onClick={handleSubmit}
              disabled={isLoading}
              className='w-full py-3 px-4 rounded-lg font-semibold text-white transition-all duration-200 hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed'
              style={{
                background: isLoading ? '#9ca3af' : 'linear-gradient(45deg, #f59e0b, #d97706)',
                boxShadow: isLoading ? 'none' : '0 4px 15px rgba(245, 158, 11, 0.3)'
              }}
            >
              {isLoading ? 'Sending Reset Link...' : 'Send Reset Link'}
            </button>
          </div>

          <div className='mt-8 text-center'>
            <p className='text-gray-600'>
              Remember your password?{' '}
              <Link href='/login' className='text-orange-600 hover:text-orange-500 font-medium'>
                Sign in here
              </Link>
            </p>
            <div className='mt-4'>
              <Link href='/' className='text-sm text-gray-500 hover:text-gray-700'>
                ← Back to Home
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
