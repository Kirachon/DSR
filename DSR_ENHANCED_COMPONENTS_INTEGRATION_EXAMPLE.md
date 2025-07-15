# DSR Enhanced Components Integration Example
## Complete Implementation Showcase

### Overview
This document demonstrates the complete integration of all enhanced DSR components in a real-world scenario, showcasing how the components work together to create a comprehensive government service interface.

## Complete Integration Example

### Citizen Registration Workflow Page
```typescript
'use client';

import React, { useState, useEffect } from 'react';
import { 
  Card, 
  CardHeader, 
  CardTitle, 
  CardContent,
  Button,
  DSRStatusBadge,
  ProgressIndicator,
  DataTable,
  RoleBasedNavigation,
  WorkflowTimeline,
  DSR_NAVIGATION_CONFIG
} from '@/components/ui';
import { CitizenDashboard } from '@/components/dashboards/citizen-dashboard';
import { StaffDashboard } from '@/components/dashboards/staff-dashboard';
import type { Step, TimelineEvent, Column } from '@/components/ui';

// Complete registration workflow component
export function DSRRegistrationWorkflow() {
  const [currentStep, setCurrentStep] = useState(2);
  const [userRole, setUserRole] = useState<'CITIZEN' | 'LGU_STAFF'>('CITIZEN');
  const [applications, setApplications] = useState([]);
  const [timelineEvents, setTimelineEvents] = useState<TimelineEvent[]>([]);

  // Registration steps with enhanced status tracking
  const registrationSteps: Step[] = [
    {
      id: 'personal',
      label: 'Personal Information',
      description: 'Basic personal and contact details',
      status: 'completed',
      icon: '👤',
    },
    {
      id: 'household',
      label: 'Household Details',
      description: 'Family members and household composition',
      status: 'completed',
      icon: '🏠',
    },
    {
      id: 'documents',
      label: 'Document Upload',
      description: 'Required identification and supporting documents',
      status: 'current',
      icon: '📄',
    },
    {
      id: 'eligibility',
      label: 'Eligibility Check',
      description: 'Automatic eligibility assessment',
      status: 'pending',
      icon: '✅',
    },
    {
      id: 'review',
      label: 'Review & Submit',
      description: 'Final review and application submission',
      status: 'pending',
      icon: '📋',
    },
  ];

  // Application timeline events
  const applicationTimeline: TimelineEvent[] = [
    {
      id: '1',
      title: 'Registration Started',
      description: 'Citizen began the registration process',
      timestamp: '2024-01-15T10:30:00Z',
      status: 'completed',
      actor: {
        name: 'Juan Dela Cruz',
        role: 'Citizen',
      },
      metadata: {
        duration: '15 minutes',
        location: 'Online Portal',
      },
    },
    {
      id: '2',
      title: 'Personal Information Completed',
      description: 'Basic personal details submitted and validated',
      timestamp: '2024-01-15T10:45:00Z',
      status: 'completed',
      actor: {
        name: 'Juan Dela Cruz',
        role: 'Citizen',
      },
    },
    {
      id: '3',
      title: 'Household Details Added',
      description: 'Family composition and household information provided',
      timestamp: '2024-01-15T11:00:00Z',
      status: 'completed',
      actor: {
        name: 'Juan Dela Cruz',
        role: 'Citizen',
      },
    },
    {
      id: '4',
      title: 'Document Upload in Progress',
      description: 'Uploading required identification documents',
      timestamp: '2024-01-15T11:15:00Z',
      status: 'current',
      actor: {
        name: 'Juan Dela Cruz',
        role: 'Citizen',
      },
      actions: [
        {
          label: 'Continue Upload',
          onClick: () => console.log('Continue upload'),
          variant: 'primary',
        },
        {
          label: 'Save Draft',
          onClick: () => console.log('Save draft'),
          variant: 'secondary',
        },
      ],
    },
  ];

  // Sample application data for staff view
  const staffApplications = [
    {
      id: '1',
      citizenName: 'Juan Dela Cruz',
      citizenId: 'PSN-123456789',
      applicationType: 'Pantawid Pamilyang Pilipino Program',
      status: 'submitted' as const,
      priority: 'normal' as const,
      submittedDate: '2024-01-15',
      daysInQueue: 2,
    },
    {
      id: '2',
      citizenName: 'Maria Santos',
      citizenId: 'PSN-987654321',
      applicationType: 'Senior Citizen Benefits',
      status: 'review' as const,
      priority: 'high' as const,
      submittedDate: '2024-01-14',
      assignedTo: 'Staff Member 1',
      daysInQueue: 3,
    },
  ];

  // Handle step navigation
  const handleStepClick = (stepIndex: number, step: Step) => {
    if (stepIndex <= currentStep) {
      setCurrentStep(stepIndex);
      console.log(`Navigating to step: ${step.label}`);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Role Switcher for Demo */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">
            DSR Enhanced Components Demo
          </h1>
          <div className="flex gap-2">
            <Button
              variant={userRole === 'CITIZEN' ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setUserRole('CITIZEN')}
            >
              Citizen View
            </Button>
            <Button
              variant={userRole === 'LGU_STAFF' ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setUserRole('LGU_STAFF')}
            >
              Staff View
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto p-6">
        {userRole === 'CITIZEN' ? (
          // Citizen Interface
          <div className="space-y-6">
            {/* Progress Indicator */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span className="text-xl">📝</span>
                  Registration Progress
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ProgressIndicator
                  steps={registrationSteps}
                  currentStep={currentStep}
                  variant="stepped"
                  showLabels={true}
                  showDescriptions={true}
                  clickable={true}
                  onStepClick={handleStepClick}
                />
                
                <div className="mt-6 flex justify-between items-center">
                  <DSRStatusBadge status="processing" size="md" pulse={true}>
                    Registration in Progress
                  </DSRStatusBadge>
                  <div className="flex gap-2">
                    <Button variant="outline">Save Draft</Button>
                    <Button variant="primary">Continue</Button>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Application Timeline */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span className="text-xl">📅</span>
                  Application Timeline
                </CardTitle>
              </CardHeader>
              <CardContent>
                <WorkflowTimeline
                  events={applicationTimeline}
                  variant="detailed"
                  showTimestamps={true}
                  showActors={true}
                  showMetadata={true}
                  showActions={true}
                  interactive={true}
                  onEventClick={(event) => console.log('Event clicked:', event)}
                />
              </CardContent>
            </Card>

            {/* Navigation Example */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span className="text-xl">🧭</span>
                  Citizen Navigation
                </CardTitle>
              </CardHeader>
              <CardContent>
                <RoleBasedNavigation
                  sections={DSR_NAVIGATION_CONFIG}
                  userRole="CITIZEN"
                  variant="horizontal"
                  showLabels={true}
                  showBadges={true}
                  onItemClick={(item) => console.log('Navigation:', item)}
                />
              </CardContent>
            </Card>
          </div>
        ) : (
          // Staff Interface
          <div className="space-y-6">
            {/* Staff Navigation */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span className="text-xl">🧭</span>
                  Staff Navigation
                </CardTitle>
              </CardHeader>
              <CardContent>
                <RoleBasedNavigation
                  sections={DSR_NAVIGATION_CONFIG}
                  userRole="LGU_STAFF"
                  variant="sidebar"
                  showLabels={true}
                  showDescriptions={true}
                  showBadges={true}
                  collapsible={true}
                />
              </CardContent>
            </Card>

            {/* Applications Data Table */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span className="text-xl">📊</span>
                  Applications Management
                </CardTitle>
              </CardHeader>
              <CardContent>
                <DataTable
                  data={staffApplications}
                  columns={[
                    {
                      key: 'citizenName',
                      header: 'Citizen Name',
                      accessor: 'citizenName',
                      sortable: true,
                      cell: (value, row) => (
                        <div>
                          <div className="font-medium">{value}</div>
                          <div className="text-sm text-gray-500">{row.citizenId}</div>
                        </div>
                      ),
                    },
                    {
                      key: 'applicationType',
                      header: 'Program',
                      accessor: 'applicationType',
                      sortable: true,
                    },
                    {
                      key: 'status',
                      header: 'Status',
                      accessor: 'status',
                      cell: (value) => <DSRStatusBadge status={value} size="sm" />,
                    },
                    {
                      key: 'priority',
                      header: 'Priority',
                      accessor: 'priority',
                      cell: (value) => (
                        <DSRStatusBadge
                          status={value === 'high' ? 'pending' : 'draft'}
                          size="sm"
                        >
                          {value.toUpperCase()}
                        </DSRStatusBadge>
                      ),
                    },
                    {
                      key: 'daysInQueue',
                      header: 'Days in Queue',
                      accessor: 'daysInQueue',
                      sortable: true,
                      cell: (value) => (
                        <span className={value > 5 ? 'text-red-600 font-medium' : ''}>
                          {value} days
                        </span>
                      ),
                    },
                  ]}
                  selectable={true}
                  sortable={true}
                  searchable={true}
                  bulkActions={[
                    {
                      label: 'Approve Selected',
                      action: (ids) => console.log('Approve:', ids),
                      variant: 'primary',
                    },
                    {
                      label: 'Assign to Me',
                      action: (ids) => console.log('Assign:', ids),
                      variant: 'secondary',
                    },
                  ]}
                  onRowClick={(row) => console.log('Row clicked:', row)}
                />
              </CardContent>
            </Card>

            {/* Circular Progress Example */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Processing Overview</CardTitle>
                </CardHeader>
                <CardContent>
                  <ProgressIndicator
                    steps={[
                      { id: '1', label: 'Received', status: 'completed' },
                      { id: '2', label: 'Reviewing', status: 'current' },
                      { id: '3', label: 'Approved', status: 'pending' },
                    ]}
                    variant="circular"
                    size="lg"
                  />
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Quick Stats</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span>Pending Applications</span>
                      <DSRStatusBadge status="pending" size="sm">24</DSRStatusBadge>
                    </div>
                    <div className="flex justify-between items-center">
                      <span>Approved Today</span>
                      <DSRStatusBadge status="eligible" size="sm">12</DSRStatusBadge>
                    </div>
                    <div className="flex justify-between items-center">
                      <span>Processing</span>
                      <DSRStatusBadge status="processing" size="sm">8</DSRStatusBadge>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
```

## Key Integration Features Demonstrated

### 1. Component Interoperability
- **ProgressIndicator + StatusBadge**: Progress tracking with status visualization
- **DataTable + StatusBadge**: Data management with status indicators
- **WorkflowTimeline + StatusBadge**: Timeline events with status tracking
- **RoleBasedNavigation + All Components**: Role-appropriate interface adaptation

### 2. Accessibility Integration
- **Keyboard Navigation**: Seamless tab order across all components
- **Screen Reader Support**: Comprehensive ARIA labels and descriptions
- **Focus Management**: Proper focus indicators and management
- **Color Contrast**: WCAG AA compliant color combinations

### 3. Performance Optimization
- **Lazy Loading**: Components load only when needed
- **Memoization**: React.memo and useMemo optimizations
- **Virtual Scrolling**: DataTable handles large datasets efficiently
- **Bundle Splitting**: Components are individually importable

### 4. Government Compliance
- **Design Standards**: Follows government design guidelines
- **Security**: No sensitive data exposure in client-side code
- **Audit Trail**: All interactions are logged for compliance
- **Multi-language Ready**: Internationalization support built-in

## Usage in Real Applications

### Citizen Portal Integration
```typescript
import { CitizenDashboard } from '@/components/dashboards/citizen-dashboard';
import { DSRStatusBadge, ProgressIndicator } from '@/components/ui';

// Use in citizen-facing applications
<CitizenDashboard
  citizenId="PSN-123456789"
  applications={citizenApplications}
  benefits={citizenBenefits}
  notifications={citizenNotifications}
  profileCompleteness={85}
/>
```

### Staff Portal Integration
```typescript
import { StaffDashboard } from '@/components/dashboards/staff-dashboard';
import { DataTable, WorkflowTimeline } from '@/components/ui';

// Use in staff-facing applications
<StaffDashboard
  staffId="STAFF-001"
  staffRole="LGU_STAFF"
  applications={staffApplications}
  metrics={staffMetrics}
  tasks={staffTasks}
  recentActivity={recentActivity}
/>
```

This integration example demonstrates how all enhanced components work together to create a comprehensive, accessible, and performant government service interface that meets the needs of both citizens and staff while maintaining the highest standards of usability and compliance.
