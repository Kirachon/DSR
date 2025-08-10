'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

export default function LoginPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async () => {
    console.log('Button clicked, starting login...');
    setIsLoading(true);
    setError('');

    try {
      await new Promise(resolve => setTimeout(resolve, 1000));

      if (formData.email && formData.password) {
        console.log('Redirecting to dashboard...');
        // Use window.location instead of router.push to avoid conflicts
        window.location.href = '/dashboard';
      } else {
        setError('Please enter both email and password');
      }
    } catch (err) {
      setError('Login failed. Please try again.');
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
    <div className='min-h-screen flex items-center justify-center' style={{
      background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 50%, #cbd5e1 100%)'
    }}>
      <div className='max-w-md w-full mx-4'>
        <div className='card-elevated p-8 glass-strong animate-fade-in'>
          <div className='text-center mb-8'>
            <div className='h-20 w-20 rounded-3xl gradient-primary flex items-center justify-center mx-auto mb-6 shadow-xl'>
              <span className='font-bold text-3xl text-white'>DSR</span>
            </div>
            <h1 className='text-4xl font-bold text-gradient mb-3'>Welcome Back</h1>
            <p className='text-gray-600 text-lg'>Sign in to your DSR account</p>
          </div>

          {error && (
            <div className='mb-6 p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 animate-slide-up'>
              <div className='flex items-center space-x-2'>
                <span className='text-red-500'>⚠️</span>
                <span>{error}</span>
              </div>
            </div>
          )}

          <div className='space-y-6'>
            <div>
              <label htmlFor='email' className='block text-sm font-semibold text-gray-700 mb-3'>
                Email Address
              </label>
              <input
                type='email'
                id='email'
                name='email'
                value={formData.email}
                onChange={handleInputChange}
                required
                className='input-modern'
                placeholder='Enter your email address'
              />
            </div>

            <div>
              <label htmlFor='password' className='block text-sm font-semibold text-gray-700 mb-3'>
                Password
              </label>
              <input
                type='password'
                id='password'
                name='password'
                value={formData.password}
                onChange={handleInputChange}
                required
                className='input-modern'
                placeholder='Enter your password'
              />
            </div>

            <div className='flex items-center justify-between'>
              <div className='flex items-center'>
                <input
                  type='checkbox'
                  id='rememberMe'
                  name='rememberMe'
                  checked={formData.rememberMe}
                  onChange={handleInputChange}
                  className='h-5 w-5 text-blue-600 focus:ring-blue-500 border-gray-300 rounded transition-all'
                />
                <label htmlFor='rememberMe' className='ml-3 block text-sm font-medium text-gray-700'>
                  Remember me
                </label>
              </div>
              <Link href='/forgot-password' className='text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors'>
                Forgot password?
              </Link>
            </div>

            <button
              type='button'
              onClick={handleSubmit}
              disabled={isLoading}
              className='btn-primary w-full py-4 text-lg font-semibold disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none'
            >
              {isLoading ? (
                <div className='flex items-center justify-center space-x-2'>
                  <div className='w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin'></div>
                  <span>Signing In...</span>
                </div>
              ) : (
                'Sign In'
              )}
            </button>
          </div>

          <div className='mt-8 text-center space-y-4'>
            <p className='text-gray-600'>
              Don't have an account?{' '}
              <Link href='/register' className='text-blue-600 hover:text-blue-700 font-semibold transition-colors'>
                Register here
              </Link>
            </p>
            <div>
              <Link href='/' className='text-sm text-gray-500 hover:text-gray-700 transition-colors'>
                ← Back to Home
              </Link>
            </div>
          </div>

          <div className='mt-6 p-4 rounded-lg' style={{ backgroundColor: '#f0f9ff', border: '1px solid #0ea5e9' }}>
            <h4 className='text-sm font-medium text-blue-900 mb-2'>Demo Credentials:</h4>
            <div className='text-xs text-blue-700 space-y-1'>
              <p><strong>Any email/password combination works for demo</strong></p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
