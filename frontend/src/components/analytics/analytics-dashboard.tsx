'use client';

// Analytics Dashboard Component
// Comprehensive analytics dashboard with real-time data from Analytics Service

import React, { useState, useEffect } from 'react';

import { FormSelect } from '@/components/forms';
import { Card, Button, Alert } from '@/components/ui';
import { analyticsApi } from '@/lib/api';
import type { User, DashboardConfig, KPIValue } from '@/types';

// Analytics Dashboard props interface
interface AnalyticsDashboardProps {
  user: User;
  userRole: string;
}

// Dashboard data interface
interface DashboardData {
  dashboards: DashboardConfig[];
  currentDashboard: DashboardConfig | null;
  kpis: KPIValue[];
  widgetData: Record<string, any>;
  loading: boolean;
  error: string | null;
}

// KPI Card component
interface KPICardProps {
  kpi: KPIValue;
}

const KPICard: React.FC<KPICardProps> = ({ kpi }) => {
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'GOOD':
        return 'text-success-600 bg-success-100';
      case 'WARNING':
        return 'text-warning-600 bg-warning-100';
      case 'CRITICAL':
        return 'text-error-600 bg-error-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'UP':
        return (
          <svg
            className='w-4 h-4 text-success-600'
            fill='currentColor'
            viewBox='0 0 20 20'
          >
            <path
              fillRule='evenodd'
              d='M3.293 9.707a1 1 0 010-1.414l6-6a1 1 0 011.414 0l6 6a1 1 0 01-1.414 1.414L11 5.414V17a1 1 0 11-2 0V5.414L4.707 9.707a1 1 0 01-1.414 0z'
              clipRule='evenodd'
            />
          </svg>
        );
      case 'DOWN':
        return (
          <svg
            className='w-4 h-4 text-error-600'
            fill='currentColor'
            viewBox='0 0 20 20'
          >
            <path
              fillRule='evenodd'
              d='M16.707 10.293a1 1 0 010 1.414l-6 6a1 1 0 01-1.414 0l-6-6a1 1 0 111.414-1.414L9 14.586V3a1 1 0 012 0v11.586l4.293-4.293a1 1 0 011.414 0z'
              clipRule='evenodd'
            />
          </svg>
        );
      default:
        return (
          <svg
            className='w-4 h-4 text-gray-600'
            fill='currentColor'
            viewBox='0 0 20 20'
          >
            <path
              fillRule='evenodd'
              d='M3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z'
              clipRule='evenodd'
            />
          </svg>
        );
    }
  };

  return (
    <Card className='p-6'>
      <div className='flex items-center justify-between'>
        <div className='flex-1'>
          <p className='text-sm font-medium text-gray-500'>{kpi.kpiCode}</p>
          <div className='flex items-center space-x-2 mt-1'>
            <p className='text-2xl font-bold text-gray-900'>
              {kpi.value.toLocaleString()}
            </p>
            {kpi.target && (
              <span className='text-sm text-gray-500'>
                / {kpi.target.toLocaleString()}
              </span>
            )}
          </div>
          <div className='flex items-center space-x-2 mt-2'>
            {getTrendIcon(kpi.trend)}
            <span
              className={`text-sm ${
                kpi.changePercent > 0
                  ? 'text-success-600'
                  : kpi.changePercent < 0
                    ? 'text-error-600'
                    : 'text-gray-600'
              }`}
            >
              {kpi.changePercent > 0 ? '+' : ''}
              {kpi.changePercent.toFixed(1)}%
            </span>
          </div>
        </div>
        <div
          className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(kpi.status)}`}
        >
          {kpi.status}
        </div>
      </div>
    </Card>
  );
};

// Analytics Dashboard component
export const AnalyticsDashboard: React.FC<AnalyticsDashboardProps> = ({
  user,
  userRole,
}) => {
  const [dashboardData, setDashboardData] = useState<DashboardData>({
    dashboards: [],
    currentDashboard: null,
    kpis: [],
    widgetData: {},
    loading: true,
    error: null,
  });

  const [selectedDashboardId, setSelectedDashboardId] = useState<string>('');
  const [refreshInterval, setRefreshInterval] = useState<number>(30000); // 30 seconds

  // Load dashboard data
  useEffect(() => {
    loadDashboards();
  }, [userRole]);

  // Load KPIs when dashboard changes
  useEffect(() => {
    if (dashboardData.currentDashboard) {
      loadKPIs();
      loadWidgetData();
    }
  }, [dashboardData.currentDashboard]);

  // Auto-refresh data
  useEffect(() => {
    if (refreshInterval > 0) {
      const interval = setInterval(() => {
        if (dashboardData.currentDashboard) {
          loadKPIs();
          loadWidgetData();
        }
      }, refreshInterval);

      return () => clearInterval(interval);
    }
    return undefined;
  }, [refreshInterval, dashboardData.currentDashboard]);

  const loadDashboards = async () => {
    try {
      setDashboardData(prev => ({ ...prev, loading: true, error: null }));

      const dashboards = await analyticsApi.getDashboards(userRole);
      const defaultDashboard =
        dashboards.find(d => d.isDefault) || dashboards[0];

      setDashboardData(prev => ({
        ...prev,
        dashboards,
        currentDashboard: defaultDashboard,
        loading: false,
      }));

      if (defaultDashboard) {
        setSelectedDashboardId(defaultDashboard.id);
      }
    } catch (error) {
      console.error('Failed to load dashboards:', error);
      setDashboardData(prev => ({
        ...prev,
        loading: false,
        error: 'Failed to load dashboards',
      }));
    }
  };

  const loadKPIs = async () => {
    if (!dashboardData.currentDashboard) return;

    try {
      // Extract KPI codes from dashboard widgets
      const kpiCodes = dashboardData.currentDashboard.widgets
        .filter(w => w.type === 'KPI')
        .map(w => w.dataSource);

      if (kpiCodes.length > 0) {
        const kpis = await analyticsApi.getKPIValues(kpiCodes);
        setDashboardData(prev => ({ ...prev, kpis }));
      }
    } catch (error) {
      console.warn('Failed to load KPIs:', error);
    }
  };

  const loadWidgetData = async () => {
    if (!dashboardData.currentDashboard) return;

    try {
      const widgetData: Record<string, any> = {};

      // Load data for each widget
      for (const widget of dashboardData.currentDashboard.widgets) {
        if (widget.type !== 'KPI') {
          try {
            const data = await analyticsApi.getWidgetData(widget.id);
            widgetData[widget.id] = data;
          } catch (error) {
            console.warn(`Failed to load data for widget ${widget.id}:`, error);
            widgetData[widget.id] = null;
          }
        }
      }

      setDashboardData(prev => ({ ...prev, widgetData }));
    } catch (error) {
      console.warn('Failed to load widget data:', error);
    }
  };

  const handleDashboardChange = async (dashboardId: string) => {
    const dashboard = dashboardData.dashboards.find(d => d.id === dashboardId);
    if (dashboard) {
      setSelectedDashboardId(dashboardId);
      setDashboardData(prev => ({ ...prev, currentDashboard: dashboard }));
    }
  };

  const handleRefresh = () => {
    if (dashboardData.currentDashboard) {
      loadKPIs();
      loadWidgetData();
    }
  };

  const handleExportDashboard = async (format: 'PDF' | 'PNG' | 'JPEG') => {
    if (!dashboardData.currentDashboard) return;

    try {
      const blob = await analyticsApi.exportDashboard(
        dashboardData.currentDashboard.id,
        format
      );

      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${dashboardData.currentDashboard.name}.${format.toLowerCase()}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Failed to export dashboard:', error);
    }
  };

  if (dashboardData.loading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse'>
          <div className='h-8 bg-gray-300 rounded w-1/4 mb-4'></div>
          <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
            {[1, 2, 3, 4].map(i => (
              <div key={i} className='h-32 bg-gray-300 rounded'></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (dashboardData.error) {
    return (
      <Alert variant='error' title='Analytics Error'>
        {dashboardData.error}
      </Alert>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Dashboard Header */}
      <div className='flex justify-between items-center'>
        <div>
          <h1 className='text-3xl font-bold text-gray-900'>
            Analytics Dashboard
          </h1>
          <p className='text-gray-600 mt-1'>
            {dashboardData.currentDashboard?.description ||
              'Real-time system analytics and KPIs'}
          </p>
        </div>

        <div className='flex space-x-3'>
          <FormSelect
            value={selectedDashboardId}
            onChange={e =>
              handleDashboardChange(typeof e === 'string' ? e : e.target.value)
            }
            options={dashboardData.dashboards.map(d => ({
              value: d.id,
              label: d.name,
            }))}
            placeholder='Select Dashboard'
            className='w-48'
          />
          <Button variant='outline' onClick={handleRefresh}>
            Refresh
          </Button>
          <Button
            variant='outline'
            onClick={() => handleExportDashboard('PDF')}
            disabled={!dashboardData.currentDashboard}
          >
            Export PDF
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      {dashboardData.kpis.length > 0 && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
          {dashboardData.kpis.map((kpi, index) => (
            <KPICard key={index} kpi={kpi} />
          ))}
        </div>
      )}

      {/* Dashboard Widgets */}
      {dashboardData.currentDashboard && (
        <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
          {dashboardData.currentDashboard.widgets
            .filter(w => w.type !== 'KPI')
            .map(widget => (
              <Card key={widget.id} className='p-6'>
                <div className='flex justify-between items-center mb-4'>
                  <h3 className='text-lg font-semibold text-gray-900'>
                    {widget.title}
                  </h3>
                  {widget.description && (
                    <p className='text-sm text-gray-500'>
                      {widget.description}
                    </p>
                  )}
                </div>

                {dashboardData.widgetData[widget.id] ? (
                  <div className='h-64 flex items-center justify-center bg-gray-50 rounded'>
                    <p className='text-gray-600'>
                      Chart visualization would render here
                    </p>
                    <p className='text-xs text-gray-500 mt-1'>
                      Data:{' '}
                      {JSON.stringify(
                        dashboardData.widgetData[widget.id]
                      ).substring(0, 50)}
                      ...
                    </p>
                  </div>
                ) : (
                  <div className='h-64 flex items-center justify-center bg-gray-50 rounded'>
                    <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600'></div>
                  </div>
                )}
              </Card>
            ))}
        </div>
      )}

      {/* Auto-refresh Settings */}
      <Card className='p-4 bg-gray-50'>
        <div className='flex items-center justify-between'>
          <span className='text-sm text-gray-600'>Auto-refresh every:</span>
          <FormSelect
            value={refreshInterval.toString()}
            onChange={e =>
              setRefreshInterval(
                parseInt(typeof e === 'string' ? e : e.target.value)
              )
            }
            options={[
              { value: '0', label: 'Disabled' },
              { value: '15000', label: '15 seconds' },
              { value: '30000', label: '30 seconds' },
              { value: '60000', label: '1 minute' },
              { value: '300000', label: '5 minutes' },
            ]}
            className='w-32'
          />
        </div>
      </Card>
    </div>
  );
};

export default AnalyticsDashboard;
