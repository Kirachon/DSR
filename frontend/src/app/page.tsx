'use client';

// DSR Landing Page
// Main landing page that redirects users based on authentication status

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useEffect } from 'react';

import { Button, Loading } from '@/components/ui';
import { useAuth } from '@/contexts';

export default function Home() {
  const { isAuthenticated, isLoading, isInitialized } = useAuth();
  const router = useRouter();

  // Redirect authenticated users to dashboard
  useEffect(() => {
    if (isInitialized && isAuthenticated) {
      router.push('/dashboard');
    }
  }, [isInitialized, isAuthenticated, router]);

  // Show loading while checking authentication
  if (!isInitialized || isLoading) {
    return (
      <div className='min-h-screen flex items-center justify-center bg-gray-50'>
        <Loading text='Loading DSR Portal...' />
      </div>
    );
  }

  // Show landing page for unauthenticated users
  return (
    <div className='min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100'>
      {/* Header */}
      <header className='bg-white shadow-sm'>
        <div className='max-w-7xl mx-auto px-4 sm:px-6 lg:px-8'>
          <div className='flex justify-between items-center py-6'>
            <div className='flex items-center'>
              <div className='h-10 w-10 bg-primary-600 rounded-lg flex items-center justify-center mr-3'>
                <span className='text-white font-bold text-lg'>DSR</span>
              </div>
              <h1 className='text-2xl font-bold text-gray-900'>
                Digital Social Registry
              </h1>
            </div>
            <div className='flex space-x-4'>
              <Link href='/auth/login'>
                <Button variant='outline'>Sign In</Button>
              </Link>
              <Link href='/auth/register'>
                <Button>Get Started</Button>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className='max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16'>
        <div className='text-center'>
          <h2 className='text-4xl font-bold text-gray-900 sm:text-5xl md:text-6xl'>
            Welcome to the
            <span className='block text-primary-600'>
              Digital Social Registry
            </span>
          </h2>
          <p className='mt-6 max-w-2xl mx-auto text-xl text-gray-600'>
            A comprehensive platform for managing social protection programs,
            citizen registration, eligibility assessment, and benefit distribution
            across the Philippines.
          </p>
          <div className='mt-10 flex justify-center space-x-6'>
            <Link href='/auth/register'>
              <Button size='lg' className='px-8 py-3'>
                Register as Citizen
              </Button>
            </Link>
            <Link href='/auth/login'>
              <Button variant='outline' size='lg' className='px-8 py-3'>
                Staff Login
              </Button>
            </Link>
          </div>
        </div>

        {/* Features Grid */}
        <div className='mt-20 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8'>
          <div className='bg-white rounded-lg shadow-md p-6'>
            <div className='h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4'>
              <svg
                className='h-6 w-6 text-blue-600'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z'
                />
              </svg>
            </div>
            <h3 className='text-lg font-semibold text-gray-900 mb-2'>
              Citizen Registration
            </h3>
            <p className='text-gray-600'>
              Streamlined registration process for citizens to access social
              protection programs and benefits.
            </p>
          </div>

          <div className='bg-white rounded-lg shadow-md p-6'>
            <div className='h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center mb-4'>
              <svg
                className='h-6 w-6 text-green-600'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
                />
              </svg>
            </div>
            <h3 className='text-lg font-semibold text-gray-900 mb-2'>
              Eligibility Assessment
            </h3>
            <p className='text-gray-600'>
              Automated assessment of citizen eligibility for various social
              protection programs and benefits.
            </p>
          </div>

          <div className='bg-white rounded-lg shadow-md p-6'>
            <div className='h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4'>
              <svg
                className='h-6 w-6 text-purple-600'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1'
                />
              </svg>
            </div>
            <h3 className='text-lg font-semibold text-gray-900 mb-2'>
              Payment Processing
            </h3>
            <p className='text-gray-600'>
              Secure and efficient payment processing for social benefits and
              program disbursements.
            </p>
          </div>
        </div>

        {/* Call to Action */}
        <div className='mt-20 bg-white rounded-lg shadow-lg p-8 text-center'>
          <h3 className='text-2xl font-bold text-gray-900 mb-4'>
            Ready to get started?
          </h3>
          <p className='text-gray-600 mb-6'>
            Join thousands of citizens already benefiting from our social
            protection programs.
          </p>
          <Link href='/auth/register'>
            <Button size='lg' className='px-8 py-3'>
              Create Your Account
            </Button>
          </Link>
        </div>
      </main>

      {/* Footer */}
      <footer className='bg-gray-800 text-white py-12'>
        <div className='max-w-7xl mx-auto px-4 sm:px-6 lg:px-8'>
          <div className='grid grid-cols-1 md:grid-cols-3 gap-8'>
            <div>
              <div className='flex items-center mb-4'>
                <div className='h-8 w-8 bg-primary-600 rounded-lg flex items-center justify-center mr-2'>
                  <span className='text-white font-bold'>DSR</span>
                </div>
                <span className='text-lg font-semibold'>
                  Digital Social Registry
                </span>
              </div>
              <p className='text-gray-400'>
                Empowering social protection through digital innovation.
              </p>
            </div>
            <div>
              <h4 className='text-lg font-semibold mb-4'>Quick Links</h4>
              <ul className='space-y-2 text-gray-400'>
                <li>
                  <Link href='/about' className='hover:text-white'>
                    About DSR
                  </Link>
                </li>
                <li>
                  <Link href='/contact' className='hover:text-white'>
                    Contact Us
                  </Link>
                </li>
                <li>
                  <Link href='/privacy' className='hover:text-white'>
                    Privacy Policy
                  </Link>
                </li>
                <li>
                  <Link href='/terms' className='hover:text-white'>
                    Terms of Service
                  </Link>
                </li>
              </ul>
            </div>
            <div>
              <h4 className='text-lg font-semibold mb-4'>Contact</h4>
              <div className='text-gray-400 space-y-2'>
                <p>Department of Social Welfare and Development</p>
                <p>Republic of the Philippines</p>
                <p>Email: support@dsr.gov.ph</p>
                <p>Phone: +63 (2) 8931-8101</p>
              </div>
            </div>
          </div>
          <div className='border-t border-gray-700 mt-8 pt-8 text-center text-gray-400'>
            <p>
              © 2024 Digital Social Registry. All rights reserved. Government of
              the Philippines.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}
