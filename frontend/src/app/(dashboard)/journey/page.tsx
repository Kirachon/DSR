'use client';

// Citizen Journey Page
// Complete end-to-end citizen journey tracking and management

import React from 'react';

import { JourneyTracker } from '@/components/citizen-journey';
import { Card, Button, Alert } from '@/components/ui';
import { useAuth } from '@/contexts';

// Journey page component
export default function JourneyPage() {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated || !user) {
    return (
      <div className="space-y-6">
        <Alert variant="error" title="Authentication Required">
          Please log in to view your journey.
        </Alert>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Your Journey</h1>
          <p className="text-gray-600">
            Track your progress through the social protection system
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Button variant="outline" onClick={() => window.location.reload()}>
            Refresh Status
          </Button>
          <Button onClick={() => window.location.href = '/dashboard/registration'}>
            Start Registration
          </Button>
        </div>
      </div>

      {/* Journey Overview */}
      <Card className="p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Complete Citizen Journey
            </h2>
            <p className="text-gray-600 mb-4">
              The Digital Social Registry provides a comprehensive pathway from initial 
              registration to receiving social protection benefits. Follow your progress 
              through each step of the process.
            </p>
            <div className="space-y-2 text-sm text-gray-600">
              <p><strong>Step 1:</strong> Complete household registration with all required information</p>
              <p><strong>Step 2:</strong> AI-powered data validation and duplicate detection</p>
              <p><strong>Step 3:</strong> Eligibility assessment using Proxy Means Test (PMT)</p>
              <p><strong>Step 4:</strong> Enrollment in eligible social protection programs</p>
              <p><strong>Step 5:</strong> Regular benefit payments and status tracking</p>
            </div>
          </div>
          <div className="bg-gradient-to-br from-primary-50 to-primary-100 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-primary-900 mb-2">
              Need Help?
            </h3>
            <p className="text-primary-700 text-sm mb-4">
              Our support team is here to assist you throughout your journey.
            </p>
            <div className="space-y-2">
              <Button 
                variant="outline" 
                size="sm" 
                className="w-full"
                onClick={() => window.location.href = '/support'}
              >
                Contact Support
              </Button>
              <Button 
                variant="outline" 
                size="sm" 
                className="w-full"
                onClick={() => window.location.href = '/help'}
              >
                View Help Guide
              </Button>
            </div>
          </div>
        </div>
      </Card>

      {/* Journey Tracker */}
      <JourneyTracker user={user} />

      {/* Quick Actions */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Button
            variant="outline"
            className="h-auto p-4 flex flex-col items-center space-y-2"
            onClick={() => window.location.href = '/dashboard/registration'}
          >
            <svg className="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
            <span className="text-sm font-medium">New Registration</span>
          </Button>
          
          <Button
            variant="outline"
            className="h-auto p-4 flex flex-col items-center space-y-2"
            onClick={() => window.location.href = '/eligibility/status'}
          >
            <svg className="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span className="text-sm font-medium">Check Eligibility</span>
          </Button>
          
          <Button
            variant="outline"
            className="h-auto p-4 flex flex-col items-center space-y-2"
            onClick={() => window.location.href = '/payments/history'}
          >
            <svg className="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
            </svg>
            <span className="text-sm font-medium">Payment History</span>
          </Button>
          
          <Button
            variant="outline"
            className="h-auto p-4 flex flex-col items-center space-y-2"
            onClick={() => window.location.href = '/grievance/new'}
          >
            <svg className="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span className="text-sm font-medium">File Grievance</span>
          </Button>
        </div>
      </Card>

      {/* System Status */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">System Status</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="flex items-center space-x-3">
            <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            <div>
              <p className="text-sm font-medium text-gray-900">Registration Service</p>
              <p className="text-xs text-gray-600">Operational</p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            <div>
              <p className="text-sm font-medium text-gray-900">Eligibility Service</p>
              <p className="text-xs text-gray-600">Operational</p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            <div>
              <p className="text-sm font-medium text-gray-900">Payment Service</p>
              <p className="text-xs text-gray-600">Operational</p>
            </div>
          </div>
        </div>
      </Card>

      {/* Important Information */}
      <Card className="p-6 bg-blue-50 border-blue-200">
        <div className="flex items-start space-x-3">
          <svg className="w-5 h-5 text-blue-600 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <h3 className="text-sm font-semibold text-blue-900 mb-1">
              Important Information
            </h3>
            <div className="text-sm text-blue-800 space-y-1">
              <p>• Processing times may vary based on application volume and complexity</p>
              <p>• Ensure all required documents are uploaded for faster processing</p>
              <p>• You will receive notifications at each step of the process</p>
              <p>• Contact support if you experience any issues or delays</p>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
}
