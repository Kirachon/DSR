'use client';

// Analytics Page
// Main analytics and reporting interface for the DSR system

import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { AnalyticsDashboard } from '@/components/analytics/analytics-dashboard';
import { FormSelect, FormInput } from '@/components/forms';
import { Card, Button, Alert, Modal } from '@/components/ui';
import { useAuth } from '@/contexts';
import { analyticsApi } from '@/lib/api';
import type { ReportTemplate, ReportExecution } from '@/types';

// Analytics Page component
export default function AnalyticsPage() {
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();

  const [activeTab, setActiveTab] = useState<'dashboard' | 'reports'>(
    'dashboard'
  );
  const [reportTemplates, setReportTemplates] = useState<ReportTemplate[]>([]);
  const [reportExecutions, setReportExecutions] = useState<ReportExecution[]>(
    []
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isExecuteReportModalOpen, setIsExecuteReportModalOpen] =
    useState(false);
  const [selectedTemplate, setSelectedTemplate] =
    useState<ReportTemplate | null>(null);

  // Load data when tab changes
  useEffect(() => {
    if (activeTab === 'reports') {
      loadReportTemplates();
      loadReportExecutions();
    }
  }, [activeTab]);

  // Load report templates
  const loadReportTemplates = async () => {
    try {
      setLoading(true);
      const templates = await analyticsApi.getReportTemplates();
      setReportTemplates(templates);
    } catch (err) {
      console.error('Failed to load report templates:', err);
      setError('Failed to load report templates');
    } finally {
      setLoading(false);
    }
  };

  // Load report executions
  const loadReportExecutions = async () => {
    try {
      const executions = await analyticsApi.getReportExecutions();
      setReportExecutions(executions.content || []);
    } catch (err) {
      console.warn('Failed to load report executions:', err);
    }
  };

  // Execute report
  const handleExecuteReport = async (templateId: string, parameters?: any) => {
    try {
      const execution = await analyticsApi.executeReport(
        templateId,
        parameters
      );
      setReportExecutions(prev => [execution, ...prev]);
      setIsExecuteReportModalOpen(false);
      setSelectedTemplate(null);

      // Poll for completion
      pollReportExecution(execution.id);
    } catch (err) {
      console.error('Failed to execute report:', err);
      setError('Failed to execute report');
    }
  };

  // Poll report execution status
  const pollReportExecution = async (executionId: string) => {
    const maxAttempts = 30; // 5 minutes with 10-second intervals
    let attempts = 0;

    const poll = async () => {
      try {
        const execution = await analyticsApi.getReportExecution(executionId);

        setReportExecutions(prev =>
          prev.map(e => (e.id === executionId ? execution : e))
        );

        if (execution.status === 'COMPLETED' || execution.status === 'FAILED') {
          return; // Stop polling
        }

        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(poll, 10000); // Poll every 10 seconds
        }
      } catch (err) {
        console.warn('Failed to poll report execution:', err);
      }
    };

    setTimeout(poll, 5000); // Start polling after 5 seconds
  };

  // Download report
  const handleDownloadReport = async (
    executionId: string,
    templateName: string
  ) => {
    try {
      const blob = await analyticsApi.downloadReport(executionId);

      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${templateName}_${new Date().toISOString().split('T')[0]}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to download report:', err);
      setError('Failed to download report');
    }
  };

  // Redirect if not authenticated
  if (!isAuthenticated) {
    router.push('/auth/login');
    return null;
  }

  // Check if user has permission to view analytics
  const canViewAnalytics =
    user?.role &&
    ['DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN'].includes(user.role);
  if (!canViewAnalytics) {
    return (
      <div className='max-w-2xl mx-auto p-6'>
        <Alert variant='error' title='Access Denied'>
          You don't have permission to view analytics. Please contact your
          administrator.
        </Alert>
      </div>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex justify-between items-center'>
        <div>
          <h1 className='text-3xl font-bold text-gray-900'>
            Analytics & Reporting
          </h1>
          <p className='text-gray-600 mt-1'>
            System analytics, KPIs, and comprehensive reporting
          </p>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant='error' title='Error'>
          {error}
          <Button
            variant='outline'
            size='sm'
            onClick={() => setError(null)}
            className='ml-4'
          >
            Dismiss
          </Button>
        </Alert>
      )}

      {/* Tab Navigation */}
      <div className='border-b border-gray-200'>
        <nav className='-mb-px flex space-x-8'>
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'dashboard'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Dashboard
          </button>
          <button
            onClick={() => setActiveTab('reports')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'reports'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Reports
          </button>
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'dashboard' && (
        <AnalyticsDashboard user={user} userRole={user.role} />
      )}

      {activeTab === 'reports' && (
        <div className='space-y-6'>
          {/* Report Templates */}
          <Card className='p-6'>
            <div className='flex justify-between items-center mb-4'>
              <h2 className='text-lg font-semibold text-gray-900'>
                Available Reports
              </h2>
              <Button onClick={() => loadReportTemplates()} disabled={loading}>
                {loading ? 'Loading...' : 'Refresh'}
              </Button>
            </div>

            <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
              {reportTemplates.map(template => (
                <Card
                  key={template.id}
                  className='p-4 hover:shadow-md transition-shadow'
                >
                  <h3 className='font-medium text-gray-900 mb-2'>
                    {template.name}
                  </h3>
                  <p className='text-sm text-gray-600 mb-3'>
                    {template.description}
                  </p>
                  <div className='flex justify-between items-center'>
                    <span className='text-xs text-gray-500'>
                      {template.category}
                    </span>
                    <Button
                      size='sm'
                      onClick={() => {
                        setSelectedTemplate(template);
                        setIsExecuteReportModalOpen(true);
                      }}
                    >
                      Generate
                    </Button>
                  </div>
                </Card>
              ))}
            </div>
          </Card>

          {/* Recent Report Executions */}
          <Card className='p-6'>
            <h2 className='text-lg font-semibold text-gray-900 mb-4'>
              Recent Reports
            </h2>

            <div className='space-y-3'>
              {reportExecutions.map(execution => (
                <div
                  key={execution.id}
                  className='flex items-center justify-between p-3 bg-gray-50 rounded-lg'
                >
                  <div className='flex-1'>
                    <p className='font-medium text-gray-900'>
                      {execution.templateName}
                    </p>
                    <p className='text-sm text-gray-600'>
                      Started: {new Date(execution.startTime).toLocaleString()}
                    </p>
                  </div>

                  <div className='flex items-center space-x-3'>
                    <span
                      className={`px-2 py-1 text-xs font-medium rounded-full ${
                        execution.status === 'COMPLETED'
                          ? 'bg-success-100 text-success-800'
                          : execution.status === 'RUNNING'
                            ? 'bg-primary-100 text-primary-800'
                            : execution.status === 'FAILED'
                              ? 'bg-error-100 text-error-800'
                              : 'bg-gray-100 text-gray-800'
                      }`}
                    >
                      {execution.status}
                    </span>

                    {execution.status === 'COMPLETED' && execution.fileUrl && (
                      <Button
                        size='sm'
                        variant='outline'
                        onClick={() =>
                          handleDownloadReport(
                            execution.id,
                            execution.templateName
                          )
                        }
                      >
                        Download
                      </Button>
                    )}
                  </div>
                </div>
              ))}

              {reportExecutions.length === 0 && (
                <div className='text-center py-8 text-gray-500'>
                  <p>No reports generated yet</p>
                  <p className='text-sm mt-1'>
                    Generate your first report using the templates above
                  </p>
                </div>
              )}
            </div>
          </Card>
        </div>
      )}

      {/* Execute Report Modal */}
      <Modal
        isOpen={isExecuteReportModalOpen}
        onClose={() => {
          setIsExecuteReportModalOpen(false);
          setSelectedTemplate(null);
        }}
        title={`Generate Report: ${selectedTemplate?.name}`}
      >
        <div className='space-y-4'>
          <p className='text-gray-600'>{selectedTemplate?.description}</p>

          {/* Report parameters would go here */}
          <div className='space-y-3'>
            <FormInput
              label='Report Name'
              placeholder='Enter custom report name (optional)'
            />
            <FormSelect
              label='Date Range'
              options={[
                { value: 'last_7_days', label: 'Last 7 days' },
                { value: 'last_30_days', label: 'Last 30 days' },
                { value: 'last_quarter', label: 'Last quarter' },
                { value: 'last_year', label: 'Last year' },
              ]}
              defaultValue='last_30_days'
            />
          </div>

          <div className='flex justify-end space-x-3 pt-4'>
            <Button
              variant='outline'
              onClick={() => {
                setIsExecuteReportModalOpen(false);
                setSelectedTemplate(null);
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={() =>
                selectedTemplate && handleExecuteReport(selectedTemplate.id)
              }
            >
              Generate Report
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
