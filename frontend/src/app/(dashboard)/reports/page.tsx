'use client';

// Reports Page
// Interface for generating and viewing system reports

import React, { useState, useEffect } from 'react';

import { ReportFilters } from '@/components/reports/report-filters';
import { ReportList } from '@/components/reports/report-list';
import { ReportGenerationModal } from '@/components/reports/report-generation-modal';
import { ReportViewer } from '@/components/reports/report-viewer';
// Removed FormInput, FormSelect imports to avoid useFormContext issues
import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { analyticsApi } from '@/lib/api';
import type { Report, ReportTemplate, ReportFilters as ReportFiltersType } from '@/types';

// Report categories
const REPORT_CATEGORIES = [
  { value: '', label: 'All Categories' },
  { value: 'REGISTRATION', label: 'Registration Reports' },
  { value: 'ELIGIBILITY', label: 'Eligibility Reports' },
  { value: 'PAYMENT', label: 'Payment Reports' },
  { value: 'GRIEVANCE', label: 'Grievance Reports' },
  { value: 'ANALYTICS', label: 'Analytics Reports' },
  { value: 'SYSTEM', label: 'System Reports' },
];

// Report statuses
const REPORT_STATUSES = [
  { value: '', label: 'All Statuses' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'GENERATING', label: 'Generating' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'FAILED', label: 'Failed' },
];

// Reports page component
export default function ReportsPage() {
  const { user } = useAuth();

  // State management
  const [reports, setReports] = useState<Report[]>([]);
  const [reportTemplates, setReportTemplates] = useState<ReportTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<ReportFiltersType>({});
  const [selectedReport, setSelectedReport] = useState<Report | null>(null);
  const [selectedTemplate, setSelectedTemplate] = useState<ReportTemplate | null>(null);
  const [isGenerationModalOpen, setIsGenerationModalOpen] = useState(false);
  const [isViewerOpen, setIsViewerOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<'reports' | 'templates'>('reports');

  // Load data
  useEffect(() => {
    loadReports();
    loadReportTemplates();
  }, [filters]);

  // Load reports from API
  const loadReports = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await analyticsApi.getReports({
        ...filters,
        search: searchQuery,
        page: 0,
        size: 50,
      });

      setReports(response.content || []);
    } catch (err) {
      console.error('Failed to load reports:', err);
      setError('Analytics service is currently unavailable. Please try again later.');

      // Set empty array when Analytics service is unavailable
      setReports([]);
    } finally {
      setLoading(false);
    }
  };

  // Load report templates
  const loadReportTemplates = async () => {
    try {
      const templates = await analyticsApi.getReportTemplates();
      setReportTemplates(templates);
    } catch (err) {
      console.error('Failed to load report templates:', err);
      
      // Fallback to mock data
      setReportTemplates([
        {
          id: 'TPL001',
          name: 'Registration Summary',
          description: 'Summary of citizen registrations by period',
          category: 'REGISTRATION',
          parameters: [
            { name: 'dateRange', type: 'DATE_RANGE', required: true, label: 'Date Range' },
            { name: 'region', type: 'SELECT', required: false, label: 'Region', options: ['Metro Manila', 'Cebu', 'Davao'] },
          ],
        },
        {
          id: 'TPL002',
          name: 'Payment Analysis',
          description: 'Analysis of payment disbursements and trends',
          category: 'PAYMENT',
          parameters: [
            { name: 'dateRange', type: 'DATE_RANGE', required: true, label: 'Date Range' },
            { name: 'paymentType', type: 'SELECT', required: false, label: 'Payment Type', options: ['CASH', 'DIGITAL', 'ALL'] },
          ],
        },
        {
          id: 'TPL003',
          name: 'Eligibility Assessment',
          description: 'Eligibility assessment results and statistics',
          category: 'ELIGIBILITY',
          parameters: [
            { name: 'dateRange', type: 'DATE_RANGE', required: true, label: 'Date Range' },
            { name: 'program', type: 'SELECT', required: false, label: 'Program', options: ['4Ps', 'DSWD-SLP', 'ALL'] },
          ],
        },
      ] as ReportTemplate[]);
    }
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setTimeout(() => loadReports(), 300);
  };

  // Handle filter changes
  const handleFilterChange = (newFilters: ReportFiltersType) => {
    setFilters(newFilters);
  };

  // Handle report generation
  const handleReportGeneration = async (templateId: string, parameters: any) => {
    try {
      const newReport = await analyticsApi.generateReport(templateId, parameters);
      setReports(prev => [newReport, ...prev]);
      setIsGenerationModalOpen(false);
      setSelectedTemplate(null);
    } catch (err) {
      console.error('Failed to generate report:', err);
      setError('Failed to generate report. Please try again.');
    }
  };

  // Handle report view
  const handleReportView = (report: Report) => {
    setSelectedReport(report);
    setIsViewerOpen(true);
  };

  // Handle report download
  const handleReportDownload = (report: Report) => {
    if (report.fileUrl) {
      window.open(report.fileUrl, '_blank');
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Reports</h1>
          <p className="text-gray-600 mt-1">
            Generate and manage system reports and analytics
          </p>
        </div>

        <div className="flex space-x-3">
          <Button variant="outline" onClick={() => loadReports()}>
            Refresh
          </Button>
          <Button onClick={() => setIsGenerationModalOpen(true)}>
            Generate New Report
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="error" title="Error">
          {error}
        </Alert>
      )}

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('reports')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'reports'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Generated Reports
          </button>
          <button
            onClick={() => setActiveTab('templates')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'templates'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Report Templates
          </button>
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'reports' && (
        <>
          {/* Search and Filters */}
          <Card className="p-6">
            <div className="flex flex-col lg:flex-row gap-4">
              <div className="flex-1">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Search Reports
                </label>
                <input
                  type="text"
                  placeholder="Search by report name or description..."
                  value={searchQuery}
                  onChange={(e) => handleSearch(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div className="lg:w-48">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Category
                </label>
                <select
                  value={filters.category || ''}
                  onChange={(e) => handleFilterChange({ ...filters, category: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                >
                  {REPORT_CATEGORIES.map(option => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                  ))}
                </select>
              </div>
              <div className="lg:w-48">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Status
                </label>
                <select
                  value={filters.status || ''}
                  onChange={(e) => handleFilterChange({ ...filters, status: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                >
                  {REPORT_STATUSES.map(option => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                  ))}
                </select>
              </div>
            </div>
          </Card>

          {/* Reports List */}
          <Card className="p-6">
            <ReportList
              reports={reports}
              loading={loading}
              onReportView={handleReportView}
              onReportDownload={handleReportDownload}
              onRefresh={loadReports}
            />
          </Card>
        </>
      )}

      {activeTab === 'templates' && (
        <Card className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {reportTemplates.map(template => (
              <Card key={template.id} className="p-4 hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start mb-3">
                  <Badge variant="secondary">{template.category}</Badge>
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{template.name}</h3>
                <p className="text-sm text-gray-600 mb-4">{template.description}</p>
                <Button
                  size="sm"
                  onClick={() => {
                    setSelectedTemplate(template);
                    setIsGenerationModalOpen(true);
                  }}
                  className="w-full"
                >
                  Generate Report
                </Button>
              </Card>
            ))}
          </div>
        </Card>
      )}

      {/* Report Generation Modal */}
      <ReportGenerationModal
        isOpen={isGenerationModalOpen}
        template={selectedTemplate}
        onClose={() => {
          setIsGenerationModalOpen(false);
          setSelectedTemplate(null);
        }}
        onGenerate={handleReportGeneration}
      />

      {/* Report Viewer Modal */}
      <ReportViewer
        isOpen={isViewerOpen}
        report={selectedReport}
        onClose={() => {
          setIsViewerOpen(false);
          setSelectedReport(null);
        }}
      />
    </div>
  );
}
