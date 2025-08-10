'use client';

// DSR Staff Dashboard Component
// Optimized interface for LGU and DSWD staff with advanced data management and workflow tools

import React, { useState, useEffect } from 'react';
import Link from 'next/link';

import { 
  Card, 
  CardHeader, 
  CardTitle, 
  CardContent,
  Button,
  DSRStatusBadge,
  DataTable,
  WorkflowTimeline
} from '@/components/ui';
import type { Column, TimelineEvent } from '@/components/ui';

// Staff dashboard data interfaces
interface StaffApplication {
  id: string;
  citizenName: string;
  citizenId: string;
  applicationType: string;
  status: 'submitted' | 'review' | 'approved' | 'rejected';
  priority: 'low' | 'normal' | 'high' | 'urgent';
  submittedDate: string;
  assignedTo?: string;
  daysInQueue: number;
}

interface StaffMetrics {
  totalApplications: number;
  pendingReview: number;
  approvedToday: number;
  rejectedToday: number;
  averageProcessingTime: number;
  myAssignedCases: number;
}

interface StaffTask {
  id: string;
  title: string;
  description: string;
  priority: 'low' | 'normal' | 'high' | 'urgent';
  dueDate: string;
  status: 'pending' | 'in-progress' | 'completed';
  relatedApplication?: string;
}

// Staff dashboard props
export interface StaffDashboardProps {
  staffId: string;
  staffRole: 'LGU_STAFF' | 'DSWD_STAFF' | 'CASE_WORKER';
  applications: StaffApplication[];
  metrics: StaffMetrics;
  tasks: StaffTask[];
  recentActivity: TimelineEvent[];
}

// Main staff dashboard component
export function StaffDashboard({
  staffId,
  staffRole,
  applications,
  metrics,
  tasks,
  recentActivity,
}: StaffDashboardProps) {
  const [selectedApplications, setSelectedApplications] = useState<string[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(25);

  // Define table columns for applications
  const applicationColumns: Column<StaffApplication>[] = [
    {
      key: 'citizenName',
      header: 'Citizen Name',
      accessor: 'citizenName',
      sortable: true,
      filterable: true,
      cell: (value, row) => (
        <div>
          <div className="font-medium text-gray-900">{value}</div>
          <div className="text-sm text-gray-500">ID: {row.citizenId}</div>
        </div>
      ),
    },
    {
      key: 'applicationType',
      header: 'Application Type',
      accessor: 'applicationType',
      sortable: true,
      filterable: true,
    },
    {
      key: 'status',
      header: 'Status',
      accessor: 'status',
      sortable: true,
      cell: (value) => <DSRStatusBadge status={value} size="sm" />,
    },
    {
      key: 'priority',
      header: 'Priority',
      accessor: 'priority',
      sortable: true,
      cell: (value) => (
        <DSRStatusBadge
          status={value === 'urgent' ? 'rejected' : value === 'high' ? 'pending' : 'draft'}
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
        <span className={value > 7 ? 'text-error-600 font-medium' : 'text-gray-900'}>
          {value} days
        </span>
      ),
    },
    {
      key: 'submittedDate',
      header: 'Submitted',
      accessor: 'submittedDate',
      sortable: true,
      cell: (value) => new Date(value).toLocaleDateString(),
    },
    {
      key: 'assignedTo',
      header: 'Assigned To',
      accessor: 'assignedTo',
      cell: (value) => value || 'Unassigned',
    },
    {
      key: 'actions',
      header: 'Actions',
      accessor: () => null,
      cell: (_, row) => (
        <div className="flex gap-2">
          <Button variant="outline" size="sm" asChild>
            <Link href={`/applications/${row.id}`}>Review</Link>
          </Button>
          <Button variant="primary" size="sm" asChild>
            <Link href={`/applications/${row.id}/process`}>Process</Link>
          </Button>
        </div>
      ),
    },
  ];

  // Bulk actions for applications
  const bulkActions = [
    {
      label: 'Assign to Me',
      action: (ids: string[]) => handleBulkAssign(ids),
      variant: 'secondary' as const,
    },
    {
      label: 'Mark for Review',
      action: (ids: string[]) => handleBulkReview(ids),
      variant: 'primary' as const,
    },
    {
      label: 'Export Selected',
      action: (ids: string[]) => handleBulkExport(ids),
      variant: 'outline' as const,
    },
  ];

  // Handle bulk operations
  const handleBulkAssign = async (applicationIds: string[]) => {
    try {
      // API call to assign applications
      console.log('Assigning applications:', applicationIds);
      // Refresh data after assignment
    } catch (error) {
      console.error('Failed to assign applications:', error);
    }
  };

  const handleBulkReview = async (applicationIds: string[]) => {
    try {
      // API call to mark for review
      console.log('Marking for review:', applicationIds);
    } catch (error) {
      console.error('Failed to mark for review:', error);
    }
  };

  const handleBulkExport = async (applicationIds: string[]) => {
    try {
      // API call to export applications
      console.log('Exporting applications:', applicationIds);
    } catch (error) {
      console.error('Failed to export applications:', error);
    }
  };

  // Get role-specific quick actions
  const getQuickActions = () => {
    const baseActions = [
      {
        title: 'Review Applications',
        description: 'Process pending applications',
        href: '/applications?status=submitted',
        icon: '📋',
        color: 'bg-primary-50 text-primary-700',
        count: metrics.pendingReview,
      },
      {
        title: 'My Tasks',
        description: 'View assigned tasks',
        href: '/tasks',
        icon: '✅',
        color: 'bg-green-50 text-green-700',
        count: tasks.filter(t => t.status !== 'completed').length,
      },
    ];

    if (staffRole === 'LGU_STAFF') {
      baseActions.push(
        {
          title: 'Citizen Verification',
          description: 'Verify citizen documents',
          href: '/verification',
          icon: '🔍',
          color: 'bg-blue-50 text-blue-700',
          count: 0,
        },
        {
          title: 'Local Reports',
          description: 'Generate local reports',
          href: '/reports/local',
          icon: '📊',
          color: 'bg-purple-50 text-purple-700',
          count: 0,
        }
      );
    }

    if (staffRole === 'DSWD_STAFF') {
      baseActions.push(
        {
          title: 'Policy Management',
          description: 'Manage program policies',
          href: '/policies',
          icon: '⚖️',
          color: 'bg-orange-50 text-orange-700',
          count: 0,
        },
        {
          title: 'System Analytics',
          description: 'View system-wide analytics',
          href: '/analytics',
          icon: '📈',
          color: 'bg-indigo-50 text-indigo-700',
          count: 0,
        }
      );
    }

    return baseActions;
  };

  const quickActions = getQuickActions();

  return (
    <div className="space-y-6 p-6 max-w-full">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-display-md font-bold text-gray-900">
            {staffRole === 'LGU_STAFF' ? 'LGU Staff Dashboard' : 
             staffRole === 'DSWD_STAFF' ? 'DSWD Staff Dashboard' : 
             'Case Worker Dashboard'}
          </h1>
          <p className="text-gray-600">
            Manage applications, process cases, and track performance metrics.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" asChild>
            <Link href="/reports">Generate Report</Link>
          </Button>
          <Button variant="primary" asChild>
            <Link href="/applications/new">New Application</Link>
          </Button>
        </div>
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-4">
        <Card className="bg-white shadow-sm">
          <CardContent className="p-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-gray-900">{metrics.totalApplications}</p>
              <p className="text-sm text-gray-600">Total Applications</p>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm">
          <CardContent className="p-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-dsr-pending">{metrics.pendingReview}</p>
              <p className="text-sm text-gray-600">Pending Review</p>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm">
          <CardContent className="p-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-dsr-eligible">{metrics.approvedToday}</p>
              <p className="text-sm text-gray-600">Approved Today</p>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm">
          <CardContent className="p-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-error-600">{metrics.rejectedToday}</p>
              <p className="text-sm text-gray-600">Rejected Today</p>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm">
          <CardContent className="p-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-primary-600">{metrics.averageProcessingTime}</p>
              <p className="text-sm text-gray-600">Avg. Processing (days)</p>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-white shadow-sm">
          <CardContent className="p-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-dsr-processing">{metrics.myAssignedCases}</p>
              <p className="text-sm text-gray-600">My Assigned Cases</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 xl:grid-cols-4 gap-6">
        {/* Applications Table - Takes up 3 columns */}
        <div className="xl:col-span-3">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <span className="text-xl">📋</span>
                  Applications Queue
                </span>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm">
                    Filter
                  </Button>
                  <Button variant="outline" size="sm">
                    Export
                  </Button>
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <DataTable
                data={applications}
                columns={applicationColumns}
                selectable={true}
                selectedIds={selectedApplications}
                onSelectionChange={setSelectedApplications}
                sortable={true}
                filterable={true}
                searchable={true}
                bulkActions={bulkActions}
                pagination={{
                  page: currentPage,
                  pageSize: pageSize,
                  total: applications.length,
                  onPageChange: setCurrentPage,
                  onPageSizeChange: setPageSize,
                }}
                getRowId={(row) => row.id}
                onRowClick={(row) => {
                  window.location.href = `/applications/${row.id}`;
                }}
                rowClassName={(row) => 
                  row.priority === 'urgent' ? 'bg-red-50 hover:bg-red-100' :
                  row.priority === 'high' ? 'bg-orange-50 hover:bg-orange-100' :
                  ''
                }
              />
            </CardContent>
          </Card>
        </div>

        {/* Right Sidebar - Takes up 1 column */}
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
              <div className="space-y-3">
                {quickActions.map((action) => (
                  <Link
                    key={action.title}
                    href={action.href}
                    className="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all"
                  >
                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${action.color}`}>
                      <span className="text-lg">{action.icon}</span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <h4 className="font-medium text-gray-900 text-sm">{action.title}</h4>
                        {action.count > 0 && (
                          <DSRStatusBadge status="pending" size="sm">
                            {action.count}
                          </DSRStatusBadge>
                        )}
                      </div>
                      <p className="text-xs text-gray-600">{action.description}</p>
                    </div>
                  </Link>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* My Tasks */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <span className="text-xl">✅</span>
                  My Tasks
                </span>
                <Button variant="outline" size="sm" asChild>
                  <Link href="/tasks">View All</Link>
                </Button>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {tasks.slice(0, 5).map((task) => (
                  <div
                    key={task.id}
                    className="p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <div className="flex items-start justify-between mb-2">
                      <h4 className="font-medium text-gray-900 text-sm">{task.title}</h4>
                      <DSRStatusBadge
                        status={task.status === 'completed' ? 'completed' : 
                               task.status === 'in-progress' ? 'processing' : 'pending'}
                        size="sm"
                      />
                    </div>
                    <p className="text-xs text-gray-600 mb-2">{task.description}</p>
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-gray-500">
                        Due: {new Date(task.dueDate).toLocaleDateString()}
                      </span>
                      <DSRStatusBadge
                        status={task.priority === 'urgent' ? 'rejected' : 
                               task.priority === 'high' ? 'pending' : 'draft'}
                        size="sm"
                      >
                        {task.priority}
                      </DSRStatusBadge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Recent Activity */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <span className="text-xl">📅</span>
                Recent Activity
              </CardTitle>
            </CardHeader>
            <CardContent>
              <WorkflowTimeline
                events={recentActivity.slice(0, 5)}
                variant="compact"
                showTimestamps={true}
                showActors={false}
              />
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
