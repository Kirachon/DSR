'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

export default function ApplicationsPage() {
  const router = useRouter();
  const [selectedProgram, setSelectedProgram] = useState('');
  const [applicationSubmitted, setApplicationSubmitted] = useState(false);

  const programs = [
    {
      id: 'pantawid',
      name: 'Pantawid Pamilyang Pilipino Program (4Ps)',
      description: 'Conditional cash transfer program for poor families',
      amount: '₱1,400 - ₱3,000 per month',
      icon: '👨‍👩‍👧‍👦'
    },
    {
      id: 'sss',
      name: 'Social Security System (SSS)',
      description: 'Social insurance program for private sector workers',
      amount: 'Variable benefits',
      icon: '🛡️'
    },
    {
      id: 'philhealth',
      name: 'PhilHealth',
      description: 'Universal health insurance program',
      amount: 'Medical coverage',
      icon: '🏥'
    },
    {
      id: 'dswd',
      name: 'DSWD Emergency Assistance',
      description: 'Emergency financial assistance for families in crisis',
      amount: '₱5,000 - ₱15,000',
      icon: '🆘'
    }
  ];

  const handleApply = async () => {
    if (!selectedProgram) return;
    
    // Simulate application submission
    await new Promise(resolve => setTimeout(resolve, 2000));
    setApplicationSubmitted(true);
  };

  if (applicationSubmitted) {
    return (
      <div className='min-h-screen' style={{ backgroundColor: '#f9fafb' }}>
        <header className='shadow-sm' style={{ backgroundColor: 'white' }}>
          <div className='max-w-7xl mx-auto px-4 sm:px-6 lg:px-8'>
            <div className='flex justify-between items-center py-4'>
              <div className='flex items-center'>
                <div className='h-10 w-10 rounded-lg flex items-center justify-center mr-3' style={{ 
                  background: 'linear-gradient(45deg, #3b82f6, #1d4ed8)'
                }}>
                  <span className='font-bold text-lg text-white'>DSR</span>
                </div>
                <h1 className='text-xl font-bold text-gray-900'>Application Submitted</h1>
              </div>
              <Link href='/dashboard' className='text-gray-600 hover:text-gray-900'>
                Dashboard
              </Link>
            </div>
          </div>
        </header>

        <main className='max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8'>
          <div className='text-center p-8 rounded-2xl' style={{ 
            background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
            color: 'white'
          }}>
            <div className='h-20 w-20 rounded-full flex items-center justify-center mx-auto mb-6' style={{ backgroundColor: 'rgba(255, 255, 255, 0.2)' }}>
              <span className='text-4xl'>✅</span>
            </div>
            <h2 className='text-3xl font-bold mb-4'>Application Submitted Successfully!</h2>
            <p className='text-green-100 mb-6'>
              Your application has been received and is being processed. You will receive updates via email and SMS.
            </p>
            <div className='bg-white bg-opacity-20 rounded-lg p-4 mb-6'>
              <p className='text-sm'>
                <strong>Application ID:</strong> DSR-{Date.now().toString().slice(-6)}
              </p>
              <p className='text-sm'>
                <strong>Program:</strong> {programs.find(p => p.id === selectedProgram)?.name}
              </p>
              <p className='text-sm'>
                <strong>Status:</strong> Under Review
              </p>
            </div>
            <div className='space-y-4'>
              <Link href='/dashboard' className='block w-full px-6 py-3 rounded-lg font-semibold transition-all duration-200 hover:scale-105 text-center' style={{ backgroundColor: 'white', color: '#059669' }}>
                Return to Dashboard
              </Link>
              <button
                onClick={() => setApplicationSubmitted(false)}
                className='block w-full px-6 py-3 rounded-lg font-medium text-white hover:bg-white hover:bg-opacity-10 transition-all duration-200'
              >
                Submit Another Application
              </button>
            </div>
          </div>
        </main>
      </div>
    );
  }

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
                <p className='text-sm text-gray-600'>Apply for Benefits</p>
              </div>
            </div>
            <div className='flex items-center space-x-4'>
              <Link href='/dashboard' className='text-gray-600 hover:text-gray-900'>
                Dashboard
              </Link>
              <Link href='/profile' className='text-gray-600 hover:text-gray-900'>
                Profile
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className='max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8'>
        {/* Header Section */}
        <div className='mb-8 p-6 rounded-2xl' style={{ 
          background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
          color: 'white'
        }}>
          <h2 className='text-3xl font-bold mb-2'>Apply for Social Programs</h2>
          <p className='text-green-100'>
            Choose from available government assistance programs and submit your application.
          </p>
        </div>

        {/* Program Selection */}
        <div className='mb-8'>
          <h3 className='text-xl font-semibold text-gray-900 mb-6'>Available Programs</h3>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
            {programs.map((program) => (
              <div
                key={program.id}
                onClick={() => setSelectedProgram(program.id)}
                className={`p-6 rounded-xl cursor-pointer transition-all duration-200 hover:scale-105 ${
                  selectedProgram === program.id 
                    ? 'ring-2 ring-green-500 shadow-lg' 
                    : 'shadow-sm hover:shadow-md'
                }`}
                style={{ 
                  backgroundColor: 'white',
                  border: selectedProgram === program.id ? '2px solid #10b981' : '1px solid #e5e7eb'
                }}
              >
                <div className='flex items-start space-x-4'>
                  <div className='text-3xl'>{program.icon}</div>
                  <div className='flex-1'>
                    <h4 className='text-lg font-semibold text-gray-900 mb-2'>{program.name}</h4>
                    <p className='text-gray-600 text-sm mb-3'>{program.description}</p>
                    <div className='flex items-center justify-between'>
                      <span className='text-green-600 font-medium'>{program.amount}</span>
                      {selectedProgram === program.id && (
                        <span className='text-green-600 font-medium'>✓ Selected</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Application Form */}
        {selectedProgram && (
          <div className='p-6 rounded-xl shadow-sm' style={{ backgroundColor: 'white' }}>
            <h3 className='text-xl font-semibold text-gray-900 mb-6'>Application Details</h3>
            
            <div className='space-y-6'>
              <div className='p-4 rounded-lg' style={{ backgroundColor: '#f0f9ff', border: '1px solid #0ea5e9' }}>
                <h4 className='font-medium text-blue-900 mb-2'>Selected Program:</h4>
                <p className='text-blue-800'>{programs.find(p => p.id === selectedProgram)?.name}</p>
              </div>

              <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
                <div>
                  <label className='block text-sm font-medium text-gray-700 mb-2'>
                    Household Size
                  </label>
                  <select className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-green-500 focus:border-transparent' style={{ backgroundColor: '#f9fafb' }}>
                    <option>Select household size</option>
                    <option>1-2 members</option>
                    <option>3-4 members</option>
                    <option>5-6 members</option>
                    <option>7+ members</option>
                  </select>
                </div>

                <div>
                  <label className='block text-sm font-medium text-gray-700 mb-2'>
                    Monthly Income
                  </label>
                  <select className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-green-500 focus:border-transparent' style={{ backgroundColor: '#f9fafb' }}>
                    <option>Select income range</option>
                    <option>Below ₱10,000</option>
                    <option>₱10,000 - ₱20,000</option>
                    <option>₱20,000 - ₱30,000</option>
                    <option>Above ₱30,000</option>
                  </select>
                </div>
              </div>

              <div>
                <label className='block text-sm font-medium text-gray-700 mb-2'>
                  Reason for Application
                </label>
                <textarea
                  rows={4}
                  className='w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-green-500 focus:border-transparent'
                  placeholder='Please describe your situation and why you need assistance...'
                  style={{ backgroundColor: '#f9fafb' }}
                />
              </div>

              <button
                onClick={handleApply}
                className='w-full py-3 px-6 rounded-lg font-semibold text-white transition-all duration-200 hover:scale-105'
                style={{ background: 'linear-gradient(45deg, #10b981, #059669)' }}
              >
                Submit Application
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
