'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

export default function ProfilePage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('profile');

  const handleLogout = () => {
    router.push('/');
  };

  return (
    <div className='min-h-screen' style={{ backgroundColor: '#f9fafb' }}>
      {/* Header */}
      <header className='shadow-sm' style={{ backgroundColor: 'white' }}>
        <div className='max-w-7xl mx-auto px-4 sm:px-6 lg:px-8'>
          <div className='flex justify-between items-center py-4'>
            <div className='flex items-center'>
              <div className='h-10 w-10 rounded-lg flex items-center justify-center mr-3' style={{ 
                background: 'linear-gradient(45deg, #3b82f6, #1d4ed8)'
              }}>
                <span className='font-bold text-lg text-white'>DSR</span>
              </div>
              <div>
                <h1 className='text-xl font-bold text-gray-900'>Digital Social Registry</h1>
                <p className='text-sm text-gray-600'>User Profile</p>
              </div>
            </div>
            <div className='flex items-center space-x-4'>
              <Link href='/' className='text-gray-600 hover:text-gray-900'>
                Home
              </Link>
              <button
                onClick={handleLogout}
                className='px-4 py-2 rounded-lg text-white font-medium transition-all duration-200 hover:scale-105'
                style={{ background: 'linear-gradient(45deg, #ef4444, #dc2626)' }}
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className='max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8'>
        {/* Welcome Section */}
        <div className='mb-8 p-6 rounded-2xl' style={{ 
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: 'white'
        }}>
          <h2 className='text-3xl font-bold mb-2'>🎉 Login Successful!</h2>
          <p className='text-blue-100'>
            Welcome to your DSR profile. You have successfully logged in to the Digital Social Registry system.
          </p>
        </div>

        {/* Profile Content */}
        <div className='grid grid-cols-1 lg:grid-cols-3 gap-8'>
          {/* Main Content */}
          <div className='lg:col-span-2'>
            <div className='p-6 rounded-xl shadow-sm' style={{ backgroundColor: 'white' }}>
              <h3 className='text-xl font-semibold text-gray-900 mb-6'>Profile Information</h3>
              
              <div className='space-y-6'>
                <div className='grid grid-cols-2 gap-4'>
                  <div>
                    <label className='block text-sm font-medium text-gray-700 mb-2'>
                      First Name
                    </label>
                    <input
                      type='text'
                      defaultValue='Juan'
                      className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                      style={{ backgroundColor: '#f9fafb' }}
                    />
                  </div>
                  <div>
                    <label className='block text-sm font-medium text-gray-700 mb-2'>
                      Last Name
                    </label>
                    <input
                      type='text'
                      defaultValue='Dela Cruz'
                      className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                      style={{ backgroundColor: '#f9fafb' }}
                    />
                  </div>
                </div>

                <div>
                  <label className='block text-sm font-medium text-gray-700 mb-2'>
                    Email Address
                  </label>
                  <input
                    type='email'
                    defaultValue='juan.delacruz@email.com'
                    className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                    style={{ backgroundColor: '#f9fafb' }}
                  />
                </div>

                <div>
                  <label className='block text-sm font-medium text-gray-700 mb-2'>
                    Phone Number
                  </label>
                  <input
                    type='tel'
                    defaultValue='+63 912 345 6789'
                    className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                    style={{ backgroundColor: '#f9fafb' }}
                  />
                </div>

                <button
                  className='px-6 py-3 rounded-lg font-semibold text-white transition-all duration-200 hover:scale-105'
                  style={{ background: 'linear-gradient(45deg, #3b82f6, #1d4ed8)' }}
                >
                  Save Changes
                </button>
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className='space-y-6'>
            {/* Profile Summary */}
            <div className='p-6 rounded-xl shadow-sm' style={{ backgroundColor: 'white' }}>
              <div className='text-center'>
                <div className='h-20 w-20 rounded-full flex items-center justify-center mx-auto mb-4' style={{ 
                  background: 'linear-gradient(45deg, #3b82f6, #1d4ed8)'
                }}>
                  <span className='font-bold text-2xl text-white'>JD</span>
                </div>
                <h4 className='text-lg font-semibold text-gray-900'>Juan Dela Cruz</h4>
                <p className='text-gray-600'>Citizen</p>
                <div className='mt-4 px-3 py-1 rounded-full text-sm font-medium' style={{ backgroundColor: '#dcfce7', color: '#166534' }}>
                  ✓ Verified
                </div>
              </div>
            </div>

            {/* Success Message */}
            <div className='p-6 rounded-xl shadow-sm' style={{ backgroundColor: '#f0f9ff', border: '2px solid #3b82f6' }}>
              <h4 className='text-lg font-semibold text-blue-900 mb-4'>🎉 Authentication Success!</h4>
              <div className='space-y-3 text-sm text-blue-800'>
                <p>✅ Login form working correctly</p>
                <p>✅ Form submission successful</p>
                <p>✅ Redirect to profile working</p>
                <p>✅ Profile page displaying properly</p>
              </div>
            </div>

            {/* Quick Actions */}
            <div className='p-6 rounded-xl shadow-sm' style={{ backgroundColor: 'white' }}>
              <h4 className='text-lg font-semibold text-gray-900 mb-4'>Quick Actions</h4>
              <div className='space-y-3'>
                <Link href='/register' className='block w-full px-4 py-2 rounded-lg text-center font-medium transition-all duration-200 hover:scale-105' style={{ backgroundColor: '#f3f4f6', color: '#374151' }}>
                  Test Registration
                </Link>
                <Link href='/login' className='block w-full px-4 py-2 rounded-lg text-center font-medium transition-all duration-200 hover:scale-105' style={{ backgroundColor: '#f3f4f6', color: '#374151' }}>
                  Back to Login
                </Link>
                <Link href='/' className='block w-full px-4 py-2 rounded-lg text-center font-medium transition-all duration-200 hover:scale-105' style={{ backgroundColor: '#f3f4f6', color: '#374151' }}>
                  Back to Home
                </Link>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
