'use client';

// DSR Citizen Dashboard Component
// Optimized interface for citizen users with simplified navigation and clear status tracking

import React, { useState, useEffect } from 'react';
import Link from 'next/link';

import { 
  Card, 
  CardHeader, 
  CardTitle, 
  CardContent,
  Button,
  DSRStatusBadge,
  ProgressIndicator,
  WorkflowTimeline
} from '@/components/ui';
import type { Step, TimelineEvent } from '@/components/ui';

// Citizen dashboard data interfaces
interface CitizenApplication {
  id: string;
  type: string;
  status: 'draft' | 'submitted' | 'review' | 'approved' | 'rejected';
  submittedDate: string;
  lastUpdated: string;
  progress: number;
}

interface CitizenBenefit {
  id: string;
  programName: string;
  status: 'eligible' | 'enrolled' | 'active' | 'suspended';
  monthlyAmount: number;
  nextPaymentDate: string;
}

interface CitizenNotification {
  id: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  timestamp: string;
  read: boolean;
}

// Citizen dashboard props
export interface CitizenDashboardProps {
  citizenId: string;
  applications: CitizenApplication[];
  benefits: CitizenBenefit[];
  notifications: CitizenNotification[];
  profileCompleteness: number;
}

// Registration progress steps
const registrationSteps: Step[] = [
  {
    id: 'personal',
    label: 'Personal Information',
    description: 'Basic personal details',
    status: 'completed',
  },
  {
    id: 'household',
    label: 'Household Details',
    description: 'Family and household information',
    status: 'completed',
  },
  {
    id: 'documents',
    label: 'Document Upload',
    description: 'Required identification documents',
    status: 'current',
  },
  {
    id: 'review',
    label: 'Review & Submit',
    description: 'Final review and submission',
    status: 'pending',
  },
];

// Quick action items for citizens
const quickActions = [
  {
    title: 'Update Profile',
    description: 'Keep your information current',
    href: '/profile',
    icon: '👤',
    color: 'bg-primary-50 text-primary-700',
  },
  {
    title: 'Upload Documents',
    description: 'Submit required documents',
    href: '/documents',
    icon: '📄',
    color: 'bg-blue-50 text-blue-700',
  },
  {
    title: 'Check Eligibility',
    description: 'See available programs',
    href: '/eligibility',
    icon: '✅',
    color: 'bg-green-50 text-green-700',
  },
  {
    title: 'Contact Support',
    description: 'Get help with your application',
    href: '/support',
    icon: '💬',
    color: 'bg-orange-50 text-orange-700',
  },
];

// Main citizen dashboard component
export function CitizenDashboard({
  citizenId,
  applications,
  benefits,
  notifications,
  profileCompleteness,
}: CitizenDashboardProps) {
  const [timelineEvents, setTimelineEvents] = useState<TimelineEvent[]>([]);
  const [loading, setLoading] = useState(true);

  // Fetch timeline events
  useEffect(() => {
    const fetchTimeline = async () => {
      try {
        // Simulate API call
        const events: TimelineEvent[] = [
          {
            id: '1',
            title: 'Registration Started',
            description: 'You began your registration process',
            timestamp: '2024-01-15T10:30:00Z',
            status: 'completed',
            actor: {
              name: 'You',
              role: 'Citizen',
            },
          },
          {
            id: '2',
            title: 'Documents Uploaded',
            description: 'Required documents submitted for verification',
            timestamp: '2024-01-16T14:15:00Z',
            status: 'completed',
            actor: {
              name: 'You',
              role: 'Citizen',
            },
          },
          {
            id: '3',
            title: 'Under Review',
            description: 'LGU staff is reviewing your application',
            timestamp: '2024-01-17T09:00:00Z',
            status: 'current',
            actor: {
              name: 'LGU Staff',
              role: 'Reviewer',
            },
          },
        ];
        setTimelineEvents(events);
      } catch (error) {
        console.error('Failed to fetch timeline:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchTimeline();
  }, [citizenId]);

  // Get status summary
  const getStatusSummary = () => {
    const activeApplications = applications.filter(app => 
      ['submitted', 'review'].includes(app.status)
    ).length;
    
    const activeBenefits = benefits.filter(benefit => 
      benefit.status === 'active'
    ).length;
    
    const unreadNotifications = notifications.filter(notif => !notif.read).length;

    return { activeApplications, activeBenefits, unreadNotifications };
  };

  const { activeApplications, activeBenefits, unreadNotifications } = getStatusSummary();

  return (
    <div className="space-y-6 p-6 max-w-7xl mx-auto">
      {/* Welcome Header */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 rounded-lg p-6 text-white">
        <h1 className="text-display-md font-bold mb-2">
          Welcome to DSR Services
        </h1>
        <p className="text-primary-100 text-lg">
          Track your applications, manage your benefits, and access government services.
        </p>
      </div>

      {/* Status Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="bg-white shadow-sm hover:shadow-md transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Profile Completeness</p>
                <p className="text-2xl font-bold text-gray-900">{profileCompleteness}%</p>
              </div>
              <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                <span className="text-primary-600 text-xl">👤</span>
              </div>
            </div>
            <div className="mt-4 w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-primary-600 h-2 rounded-full transition-all duration-500"
                style={{ width: `${profileCompleteness}%` }}
              />
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm hover:shadow-md transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Active Applications</p>
                <p className="text-2xl font-bold text-gray-900">{activeApplications}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                <span className="text-blue-600 text-xl">📋</span>
              </div>
            </div>
            {activeApplications > 0 && (
              <DSRStatusBadge status="processing" size="sm" className="mt-2">
                In Progress
              </DSRStatusBadge>
            )}
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm hover:shadow-md transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Active Benefits</p>
                <p className="text-2xl font-bold text-gray-900">{activeBenefits}</p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                <span className="text-green-600 text-xl">💰</span>
              </div>
            </div>
            {activeBenefits > 0 && (
              <DSRStatusBadge status="eligible" size="sm" className="mt-2">
                Receiving Benefits
              </DSRStatusBadge>
            )}
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm hover:shadow-md transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Notifications</p>
                <p className="text-2xl font-bold text-gray-900">{unreadNotifications}</p>
              </div>
              <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                <span className="text-orange-600 text-xl">🔔</span>
              </div>
            </div>
            {unreadNotifications > 0 && (
              <DSRStatusBadge status="pending" size="sm" className="mt-2">
                New Messages
              </DSRStatusBadge>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Applications and Progress */}
        <div className="lg:col-span-2 space-y-6">
          {/* Current Registration Progress */}
          {profileCompleteness < 100 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span className="text-xl">📝</span>
                  Complete Your Registration
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ProgressIndicator
                  steps={registrationSteps}
                  variant="stepped"
                  showLabels={true}
                  showDescriptions={true}
                  clickable={true}
                  onStepClick={(index, step) => {
                    // Navigate to specific step
                    window.location.href = `/registration?step=${step.id}`;
                  }}
                />
                <div className="mt-4 flex justify-between items-center">
                  <p className="text-sm text-gray-600">
                    Complete your registration to access all services
                  </p>
                  <Button variant="primary" size="sm" asChild>
                    <Link href="/registration">Continue Registration</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Recent Applications */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <span className="text-xl">📋</span>
                  My Applications
                </span>
                <Button variant="outline" size="sm" asChild>
                  <Link href="/applications">View All</Link>
                </Button>
              </CardTitle>
            </CardHeader>
            <CardContent>
              {applications.length === 0 ? (
                <div className="text-center py-8">
                  <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <span className="text-gray-400 text-2xl">📄</span>
                  </div>
                  <p className="text-gray-500 mb-4">No applications yet</p>
                  <Button variant="primary" asChild>
                    <Link href="/registration">Start Application</Link>
                  </Button>
                </div>
              ) : (
                <div className="space-y-4">
                  {applications.slice(0, 3).map((application) => (
                    <div
                      key={application.id}
                      className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      <div className="flex-1">
                        <h4 className="font-medium text-gray-900">{application.type}</h4>
                        <p className="text-sm text-gray-600">
                          Submitted {new Date(application.submittedDate).toLocaleDateString()}
                        </p>
                      </div>
                      <div className="flex items-center gap-3">
                        <DSRStatusBadge status={application.status} size="sm" />
                        <Button variant="outline" size="sm" asChild>
                          <Link href={`/applications/${application.id}`}>View</Link>
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Application Timeline */}
          {timelineEvents.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span className="text-xl">📅</span>
                  Recent Activity
                </CardTitle>
              </CardHeader>
              <CardContent>
                <WorkflowTimeline
                  events={timelineEvents}
                  variant="compact"
                  showTimestamps={true}
                  showActors={true}
                />
              </CardContent>
            </Card>
          )}
        </div>

        {/* Right Column - Quick Actions and Benefits */}
        <div className="space-y-6">
          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <span className="text-xl">⚡</span>
                Quick Actions
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 gap-3">
                {quickActions.map((action) => (
                  <Link
                    key={action.title}
                    href={action.href}
                    className="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all"
                  >
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${action.color}`}>
                      <span className="text-lg">{action.icon}</span>
                    </div>
                    <div className="flex-1">
                      <h4 className="font-medium text-gray-900">{action.title}</h4>
                      <p className="text-xs text-gray-600">{action.description}</p>
                    </div>
                    <span className="text-gray-400">→</span>
                  </Link>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Active Benefits */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <span className="text-xl">💰</span>
                  My Benefits
                </span>
                <Button variant="outline" size="sm" asChild>
                  <Link href="/benefits">View All</Link>
                </Button>
              </CardTitle>
            </CardHeader>
            <CardContent>
              {benefits.length === 0 ? (
                <div className="text-center py-6">
                  <div className="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <span className="text-gray-400 text-xl">💰</span>
                  </div>
                  <p className="text-gray-500 text-sm">No active benefits</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {benefits.slice(0, 3).map((benefit) => (
                    <div
                      key={benefit.id}
                      className="p-3 border border-gray-200 rounded-lg"
                    >
                      <div className="flex items-center justify-between mb-2">
                        <h4 className="font-medium text-gray-900 text-sm">
                          {benefit.programName}
                        </h4>
                        <DSRStatusBadge status={benefit.status} size="sm" />
                      </div>
                      <div className="text-xs text-gray-600">
                        <p>Monthly: ₱{benefit.monthlyAmount.toLocaleString()}</p>
                        <p>Next Payment: {new Date(benefit.nextPaymentDate).toLocaleDateString()}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Recent Notifications */}
          {notifications.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span className="flex items-center gap-2">
                    <span className="text-xl">🔔</span>
                    Notifications
                  </span>
                  <Button variant="outline" size="sm" asChild>
                    <Link href="/notifications">View All</Link>
                  </Button>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {notifications.slice(0, 3).map((notification) => (
                    <div
                      key={notification.id}
                      className={`p-3 rounded-lg border ${
                        notification.read 
                          ? 'border-gray-200 bg-white' 
                          : 'border-primary-200 bg-primary-50'
                      }`}
                    >
                      <h4 className="font-medium text-gray-900 text-sm">
                        {notification.title}
                      </h4>
                      <p className="text-xs text-gray-600 mt-1">
                        {notification.message}
                      </p>
                      <p className="text-xs text-gray-500 mt-2">
                        {new Date(notification.timestamp).toLocaleDateString()}
                      </p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
