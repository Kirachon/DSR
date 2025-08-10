'use client';

// Global Error Boundary
// Handles unexpected errors with user-friendly interface and recovery options

import { useRouter } from 'next/navigation';
import React, { useEffect } from 'react';

import { Button, Card, Alert } from '@/components/ui';

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function Error({ error, reset }: ErrorProps) {
  const router = useRouter();

  useEffect(() => {
    // Log error to monitoring service
    console.error('Application error:', error);
    
    // You can integrate with error monitoring services here
    // Example: Sentry, LogRocket, etc.
  }, [error]);

  const handleRefresh = () => {
    window.location.reload();
  };

  const handleGoHome = () => {
    router.push('/dashboard');
  };

  const getErrorMessage = (error: Error) => {
    // Provide user-friendly error messages
    if (error.message.includes('Network')) {
      return 'Network connection error. Please check your internet connection.';
    }
    if (error.message.includes('Authentication')) {
      return 'Authentication error. Please log in again.';
    }
    if (error.message.includes('Permission')) {
      return 'You don\'t have permission to access this resource.';
    }
    return 'An unexpected error occurred. Our team has been notified.';
  };

  const getErrorSuggestions = (error: Error) => {
    const suggestions = [];
    
    if (error.message.includes('Network')) {
      suggestions.push('Check your internet connection');
      suggestions.push('Try refreshing the page');
      suggestions.push('Contact your network administrator if the problem persists');
    } else if (error.message.includes('Authentication')) {
      suggestions.push('Log out and log back in');
      suggestions.push('Clear your browser cache');
      suggestions.push('Contact support if you continue having issues');
    } else {
      suggestions.push('Try refreshing the page');
      suggestions.push('Go back to the dashboard');
      suggestions.push('Contact support if the error persists');
    }
    
    return suggestions;
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-2xl">
        {/* Error Icon */}
        <div className="flex justify-center mb-8">
          <div className="w-24 h-24 bg-red-100 rounded-full flex items-center justify-center">
            <svg
              className="w-12 h-12 text-red-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
        </div>

        {/* Error Message */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            Something went wrong
          </h1>
          <p className="text-lg text-gray-600 mb-4">
            {getErrorMessage(error)}
          </p>
          {error.digest && (
            <p className="text-sm text-gray-500 font-mono">
              Error ID: {error.digest}
            </p>
          )}
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center mb-8">
          <Button
            variant="primary"
            size="lg"
            onClick={reset}
          >
            Try Again
          </Button>
          <Button
            variant="secondary"
            size="lg"
            onClick={handleRefresh}
          >
            Refresh Page
          </Button>
          <Button
            variant="outline"
            size="lg"
            onClick={handleGoHome}
          >
            Go to Dashboard
          </Button>
        </div>

        {/* Error Details and Suggestions */}
        <div className="space-y-6">
          {/* Suggestions */}
          <Card className="p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              What you can try:
            </h2>
            <ul className="space-y-2">
              {getErrorSuggestions(error).map((suggestion, index) => (
                <li key={index} className="flex items-start">
                  <span className="flex-shrink-0 w-2 h-2 bg-primary-500 rounded-full mt-2 mr-3" />
                  <span className="text-gray-700">{suggestion}</span>
                </li>
              ))}
            </ul>
          </Card>

          {/* Technical Details (for development) */}
          {process.env.NODE_ENV === 'development' && (
            <Card className="p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Technical Details
              </h2>
              <Alert variant="error" className="mb-4">
                <div className="font-mono text-sm">
                  <div className="font-semibold mb-2">Error:</div>
                  <div className="whitespace-pre-wrap">{error.message}</div>
                  {error.stack && (
                    <>
                      <div className="font-semibold mt-4 mb-2">Stack Trace:</div>
                      <div className="whitespace-pre-wrap text-xs">
                        {error.stack}
                      </div>
                    </>
                  )}
                </div>
              </Alert>
            </Card>
          )}
        </div>

        {/* Help Section */}
        <div className="text-center mt-8">
          <p className="text-sm text-gray-500 mb-4">
            Still having trouble? Our support team is here to help.
          </p>
          <div className="flex justify-center space-x-4">
            <Button
              variant="link"
              size="sm"
              onClick={() => router.push('/support')}
            >
              Contact Support
            </Button>
            <span className="text-gray-300">|</span>
            <Button
              variant="link"
              size="sm"
              onClick={() => window.open('mailto:support@dsr.gov.ph', '_blank')}
            >
              Email Support
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
