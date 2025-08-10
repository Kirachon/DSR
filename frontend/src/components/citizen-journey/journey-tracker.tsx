'use client';

// Citizen Journey Tracker Component
// Comprehensive tracking of citizen journey from registration to payment

import React, { useState, useEffect } from 'react';

import { Card, Button, Alert, Badge } from '@/components/ui';
import {
  registrationApi,
  eligibilityApi,
  paymentApi,
  dataManagementApi,
} from '@/lib/api';
import type { User } from '@/types';

// Journey step interface
interface JourneyStep {
  id: string;
  title: string;
  description: string;
  status: 'not_started' | 'in_progress' | 'completed' | 'failed' | 'pending';
  completedAt?: string;
  estimatedDuration?: string;
  actions?: Array<{
    label: string;
    href: string;
    variant?: 'primary' | 'secondary';
  }>;
  details?: Record<string, any>;
}

// Journey tracker props
interface JourneyTrackerProps {
  user: User;
  householdId?: string;
  className?: string;
}

// Journey data interface
interface JourneyData {
  registration?: any;
  eligibility?: any;
  enrollment?: any;
  payments?: any[];
  loading: boolean;
  error: string | null;
}

// Status badge component
const StepStatusBadge: React.FC<{ status: JourneyStep['status'] }> = ({ status }) => {
  const getVariant = () => {
    switch (status) {
      case 'completed':
        return 'success';
      case 'in_progress':
        return 'info';
      case 'failed':
        return 'error';
      case 'pending':
        return 'warning';
      default:
        return 'neutral';
    }
  };

  const getLabel = () => {
    switch (status) {
      case 'completed':
        return 'Completed';
      case 'in_progress':
        return 'In Progress';
      case 'failed':
        return 'Failed';
      case 'pending':
        return 'Pending';
      default:
        return 'Not Started';
    }
  };

  return <Badge variant={getVariant()}>{getLabel()}</Badge>;
};

// Step icon component
const StepIcon: React.FC<{ status: JourneyStep['status']; stepNumber: number }> = ({ 
  status, 
  stepNumber 
}) => {
  const getIconColor = () => {
    switch (status) {
      case 'completed':
        return 'bg-green-500 text-white';
      case 'in_progress':
        return 'bg-blue-500 text-white';
      case 'failed':
        return 'bg-red-500 text-white';
      case 'pending':
        return 'bg-yellow-500 text-white';
      default:
        return 'bg-gray-300 text-gray-600';
    }
  };

  return (
    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${getIconColor()}`}>
      {status === 'completed' ? (
        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
        </svg>
      ) : status === 'failed' ? (
        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
        </svg>
      ) : (
        stepNumber
      )}
    </div>
  );
};

// Generate journey steps based on data
const generateJourneySteps = (data: JourneyData, user: User): JourneyStep[] => {
  const steps: JourneyStep[] = [
    {
      id: 'registration',
      title: 'Household Registration',
      description: 'Complete household profile and member information',
      status: data.registration ? 'completed' : 'not_started',
      completedAt: data.registration?.submittedAt,
      estimatedDuration: '15-30 minutes',
      actions: data.registration ? [] : [
        {
          label: 'Start Registration',
          href: '/dashboard/registration',
          variant: 'primary',
        },
      ],
      details: data.registration,
    },
    {
      id: 'validation',
      title: 'Data Validation',
      description: 'AI-powered validation and duplicate detection',
      status: data.registration?.validationStatus === 'VALIDATED' ? 'completed' : 
              data.registration?.validationStatus === 'PENDING' ? 'in_progress' : 'not_started',
      completedAt: data.registration?.validatedAt,
      estimatedDuration: '1-3 days',
      details: {
        validationStatus: data.registration?.validationStatus,
        validationScore: data.registration?.validationScore,
      },
    },
    {
      id: 'eligibility',
      title: 'Eligibility Assessment',
      description: 'PMT calculation and program eligibility determination',
      status: data.eligibility?.status === 'COMPLETED' ? 'completed' :
              data.eligibility?.status === 'IN_PROGRESS' ? 'in_progress' : 'not_started',
      completedAt: data.eligibility?.assessedAt,
      estimatedDuration: '3-7 days',
      actions: data.eligibility?.status === 'ELIGIBLE' ? [
        {
          label: 'View Eligibility',
          href: '/eligibility/status',
          variant: 'secondary',
        },
      ] : [],
      details: data.eligibility,
    },
    {
      id: 'enrollment',
      title: 'Program Enrollment',
      description: 'Enrollment in eligible social protection programs',
      status: data.enrollment?.status === 'ENROLLED' ? 'completed' :
              data.enrollment?.status === 'PENDING' ? 'in_progress' : 'not_started',
      completedAt: data.enrollment?.enrolledAt,
      estimatedDuration: '1-2 weeks',
      actions: data.enrollment?.status === 'ENROLLED' ? [
        {
          label: 'View Programs',
          href: '/programs/enrolled',
          variant: 'secondary',
        },
      ] : [],
      details: data.enrollment,
    },
    {
      id: 'payment',
      title: 'Payment Processing',
      description: 'Benefit payment disbursement and tracking',
      status: data.payments && data.payments.length > 0 ? 'completed' : 'not_started',
      completedAt: data.payments?.[0]?.completedAt,
      estimatedDuration: 'Monthly',
      actions: data.payments && data.payments.length > 0 ? [
        {
          label: 'View Payments',
          href: '/payments/history',
          variant: 'secondary',
        },
      ] : [],
      details: {
        totalPayments: data.payments?.length || 0,
        lastPayment: data.payments?.[0],
      },
    },
  ];

  return steps;
};

// Journey Tracker component
export const JourneyTracker: React.FC<JourneyTrackerProps> = ({
  user,
  householdId,
  className,
}) => {
  const [journeyData, setJourneyData] = useState<JourneyData>({
    loading: true,
    error: null,
  });

  // Load journey data
  useEffect(() => {
    const loadJourneyData = async () => {
      try {
        setJourneyData(prev => ({ ...prev, loading: true, error: null }));

        // Load registration data
        let registration = null;
        try {
          const registrations = await registrationApi.getMyRegistrations();
          registration = registrations.find((r: any) => 
            householdId ? r.householdId === householdId : r.userId === user.id
          ) || registrations[0];
        } catch (err) {
          console.warn('Failed to load registration data:', err);
        }

        // Load eligibility data
        let eligibility = null;
        if (user.psn || registration?.psn) {
          try {
            const eligibilityData = await eligibilityApi.getHouseholdEligibility(
              user.psn || registration.psn
            );
            eligibility = eligibilityData[0]; // Latest assessment
          } catch (err) {
            console.warn('Failed to load eligibility data:', err);
          }
        }

        // Load enrollment data (mock for now)
        let enrollment = null;
        if (eligibility?.status === 'ELIGIBLE') {
          enrollment = {
            status: 'ENROLLED',
            enrolledAt: new Date().toISOString(),
            programs: ['4Ps', 'DSWD-SLP'],
          };
        }

        // Load payment data
        let payments = [];
        try {
          const paymentData = await paymentApi.getPayments({
            beneficiaryId: user.id,
            limit: 10,
          });
          payments = paymentData.content || [];
        } catch (err) {
          console.warn('Failed to load payment data:', err);
        }

        setJourneyData({
          registration,
          eligibility,
          enrollment,
          payments,
          loading: false,
          error: null,
        });
      } catch (error) {
        console.error('Error loading journey data:', error);
        setJourneyData(prev => ({
          ...prev,
          loading: false,
          error: 'Failed to load journey data. Please try again.',
        }));
      }
    };

    loadJourneyData();
  }, [user.id, user.psn, householdId]);

  const journeySteps = generateJourneySteps(journeyData, user);
  const currentStep = journeySteps.find(step => step.status === 'in_progress') || 
                     journeySteps.find(step => step.status === 'not_started');

  if (journeyData.loading) {
    return (
      <Card className={`p-6 ${className}`}>
        <div className="animate-pulse space-y-4">
          <div className="h-6 bg-gray-200 rounded w-1/3"></div>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="flex items-center space-x-3">
                <div className="w-8 h-8 bg-gray-200 rounded-full"></div>
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                  <div className="h-3 bg-gray-200 rounded w-3/4"></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </Card>
    );
  }

  return (
    <Card className={`p-6 ${className}`}>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">Your Journey</h2>
            <p className="text-gray-600">
              Track your progress through the social protection system
            </p>
          </div>
          {currentStep && (
            <div className="text-right">
              <p className="text-sm text-gray-500">Current Step</p>
              <p className="font-medium text-gray-900">{currentStep.title}</p>
            </div>
          )}
        </div>

        {/* Error Display */}
        {journeyData.error && (
          <Alert variant="error" title="Error">
            {journeyData.error}
          </Alert>
        )}

        {/* Journey Steps */}
        <div className="space-y-4">
          {journeySteps.map((step, index) => (
            <div key={step.id} className="flex items-start space-x-4">
              {/* Step Icon */}
              <div className="flex flex-col items-center">
                <StepIcon status={step.status} stepNumber={index + 1} />
                {index < journeySteps.length - 1 && (
                  <div className="w-px h-12 bg-gray-300 mt-2"></div>
                )}
              </div>

              {/* Step Content */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center space-x-3">
                    <h3 className="text-lg font-medium text-gray-900">
                      {step.title}
                    </h3>
                    <StepStatusBadge status={step.status} />
                  </div>
                  {step.estimatedDuration && (
                    <span className="text-sm text-gray-500">
                      {step.estimatedDuration}
                    </span>
                  )}
                </div>

                <p className="text-gray-600 mb-3">{step.description}</p>

                {/* Step Details */}
                {step.details && Object.keys(step.details).length > 0 && (
                  <div className="bg-gray-50 rounded-lg p-3 mb-3">
                    <div className="grid grid-cols-2 gap-2 text-sm">
                      {Object.entries(step.details).map(([key, value]) => (
                        <div key={key}>
                          <span className="text-gray-500 capitalize">
                            {key.replace(/([A-Z])/g, ' $1').toLowerCase()}:
                          </span>
                          <span className="ml-1 text-gray-900">
                            {typeof value === 'object' ? JSON.stringify(value) : String(value)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Step Actions */}
                {step.actions && step.actions.length > 0 && (
                  <div className="flex items-center space-x-2">
                    {step.actions.map((action, actionIndex) => (
                      <Button
                        key={actionIndex}
                        variant={action.variant || 'secondary'}
                        size="sm"
                        onClick={() => window.location.href = action.href}
                      >
                        {action.label}
                      </Button>
                    ))}
                  </div>
                )}

                {/* Completion Date */}
                {step.completedAt && (
                  <p className="text-xs text-gray-500 mt-2">
                    Completed on {new Date(step.completedAt).toLocaleDateString()}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* Journey Summary */}
        <div className="border-t pt-4">
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-2xl font-bold text-green-600">
                {journeySteps.filter(s => s.status === 'completed').length}
              </p>
              <p className="text-sm text-gray-600">Completed</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-blue-600">
                {journeySteps.filter(s => s.status === 'in_progress').length}
              </p>
              <p className="text-sm text-gray-600">In Progress</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-600">
                {journeySteps.filter(s => s.status === 'not_started').length}
              </p>
              <p className="text-sm text-gray-600">Remaining</p>
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
};
