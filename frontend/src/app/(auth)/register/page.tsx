'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

export default function RegisterPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    agreeToTerms: false
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      if (formData.password !== formData.confirmPassword) {
        setError('Passwords do not match');
        return;
      }

      if (!formData.agreeToTerms) {
        setError('Please agree to the terms and conditions');
        return;
      }

      await new Promise(resolve => setTimeout(resolve, 1500));
      
      if (formData.firstName && formData.lastName && formData.email && formData.password) {
        router.push('/login?message=Registration successful! Please sign in.');
      } else {
        setError('Please fill in all required fields');
      }
    } catch (err) {
      setError('Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  return (
    <div className='min-h-screen flex items-center justify-center py-12' style={{ 
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <div className='max-w-lg w-full mx-4'>
        <div className='p-8 rounded-2xl' style={{ 
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.1)'
        }}>
          <div className='text-center mb-8'>
            <div className='h-16 w-16 rounded-2xl flex items-center justify-center mx-auto mb-4' style={{ 
              background: 'linear-gradient(45deg, #3b82f6, #1d4ed8)',
              boxShadow: '0 4px 15px rgba(59, 130, 246, 0.3)'
            }}>
              <span className='font-bold text-2xl text-white'>DSR</span>
            </div>
            <h1 className='text-3xl font-bold text-gray-900 mb-2'>Create Account</h1>
            <p className='text-gray-600'>Join the Digital Social Registry</p>
          </div>

          {error && (
            <div className='mb-6 p-4 rounded-lg' style={{ backgroundColor: '#fee2e2', color: '#dc2626' }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className='space-y-6'>
            <div className='grid grid-cols-2 gap-4'>
              <div>
                <label htmlFor='firstName' className='block text-sm font-medium text-gray-700 mb-2'>
                  First Name *
                </label>
                <input
                  type='text'
                  id='firstName'
                  name='firstName'
                  value={formData.firstName}
                  onChange={handleInputChange}
                  required
                  className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200'
                  placeholder='Juan'
                  style={{ backgroundColor: '#f9fafb' }}
                />
              </div>
              <div>
                <label htmlFor='lastName' className='block text-sm font-medium text-gray-700 mb-2'>
                  Last Name *
                </label>
                <input
                  type='text'
                  id='lastName'
                  name='lastName'
                  value={formData.lastName}
                  onChange={handleInputChange}
                  required
                  className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200'
                  placeholder='Dela Cruz'
                  style={{ backgroundColor: '#f9fafb' }}
                />
              </div>
            </div>

            <div>
              <label htmlFor='email' className='block text-sm font-medium text-gray-700 mb-2'>
                Email Address *
              </label>
              <input
                type='email'
                id='email'
                name='email'
                value={formData.email}
                onChange={handleInputChange}
                required
                className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200'
                placeholder='juan.delacruz@email.com'
                style={{ backgroundColor: '#f9fafb' }}
              />
            </div>

            <div>
              <label htmlFor='phoneNumber' className='block text-sm font-medium text-gray-700 mb-2'>
                Phone Number
              </label>
              <input
                type='tel'
                id='phoneNumber'
                name='phoneNumber'
                value={formData.phoneNumber}
                onChange={handleInputChange}
                className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200'
                placeholder='+63 9XX XXX XXXX'
                style={{ backgroundColor: '#f9fafb' }}
              />
            </div>

            <div>
              <label htmlFor='password' className='block text-sm font-medium text-gray-700 mb-2'>
                Password *
              </label>
              <input
                type='password'
                id='password'
                name='password'
                value={formData.password}
                onChange={handleInputChange}
                required
                className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200'
                placeholder='Create a strong password'
                style={{ backgroundColor: '#f9fafb' }}
              />
            </div>

            <div>
              <label htmlFor='confirmPassword' className='block text-sm font-medium text-gray-700 mb-2'>
                Confirm Password *
              </label>
              <input
                type='password'
                id='confirmPassword'
                name='confirmPassword'
                value={formData.confirmPassword}
                onChange={handleInputChange}
                required
                className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200'
                placeholder='Confirm your password'
                style={{ backgroundColor: '#f9fafb' }}
              />
            </div>

            <div className='flex items-start'>
              <input
                type='checkbox'
                id='agreeToTerms'
                name='agreeToTerms'
                checked={formData.agreeToTerms}
                onChange={handleInputChange}
                required
                className='h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded mt-1'
              />
              <label htmlFor='agreeToTerms' className='ml-3 block text-sm text-gray-700'>
                I agree to the{' '}
                <Link href='/terms' className='text-blue-600 hover:text-blue-500'>
                  Terms and Conditions
                </Link>{' '}
                and{' '}
                <Link href='/privacy' className='text-blue-600 hover:text-blue-500'>
                  Privacy Policy
                </Link>
              </label>
            </div>

            <button
              type='submit'
              disabled={isLoading}
              className='w-full py-3 px-4 rounded-lg font-semibold text-white transition-all duration-200 hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed'
              style={{
                background: isLoading ? '#9ca3af' : 'linear-gradient(45deg, #f59e0b, #d97706)',
                boxShadow: isLoading ? 'none' : '0 4px 15px rgba(245, 158, 11, 0.3)'
              }}
            >
              {isLoading ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>

          <div className='mt-8 text-center'>
            <p className='text-gray-600'>
              Already have an account?{' '}
              <Link href='/login' className='text-blue-600 hover:text-blue-500 font-medium'>
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
