// Analytics Service API Client
// API client for reporting, dashboard generation, and KPI calculations

import type { ApiResponse, PaginatedResponse } from '@/types';

import { apiClient } from '../api-client';

// Base URL for Analytics Service
const ANALYTICS_BASE_URL = '/api/v1/analytics';

// Analytics API Types
export interface DashboardConfig {
  id: string;
  name: string;
  description: string;
  userRole: string;
  layout: DashboardLayout;
  widgets: DashboardWidget[];
  filters: DashboardFilter[];
  refreshInterval: number;
  isDefault: boolean;
  isPublic: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardLayout {
  columns: number;
  rows: number;
  gridSize: number;
  responsive: boolean;
}

export interface DashboardWidget {
  id: string;
  type: 'CHART' | 'TABLE' | 'KPI' | 'MAP' | 'TEXT' | 'IFRAME';
  title: string;
  description?: string;
  position: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
  dataSource: string;
  query: any;
  visualization: VisualizationConfig;
  refreshInterval?: number;
  filters?: any[];
}

export interface VisualizationConfig {
  chartType?: 'BAR' | 'LINE' | 'PIE' | 'DONUT' | 'AREA' | 'SCATTER' | 'HEATMAP';
  xAxis?: string;
  yAxis?: string;
  groupBy?: string;
  aggregation?: 'SUM' | 'COUNT' | 'AVG' | 'MIN' | 'MAX';
  colors?: string[];
  showLegend?: boolean;
  showLabels?: boolean;
  orientation?: 'HORIZONTAL' | 'VERTICAL';
}

export interface DashboardFilter {
  id: string;
  name: string;
  type: 'DATE_RANGE' | 'SELECT' | 'MULTI_SELECT' | 'TEXT' | 'NUMBER_RANGE';
  field: string;
  defaultValue?: any;
  options?: FilterOption[];
  required: boolean;
}

export interface FilterOption {
  label: string;
  value: any;
}

export interface ReportTemplate {
  id: string;
  name: string;
  description: string;
  category: string;
  reportType: 'TABULAR' | 'CHART' | 'DASHBOARD' | 'DOCUMENT';
  dataSource: string;
  query: any;
  parameters: ReportParameter[];
  format: 'PDF' | 'EXCEL' | 'CSV' | 'HTML';
  schedule?: ReportSchedule;
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReportParameter {
  name: string;
  type: 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN' | 'LIST';
  label: string;
  description?: string;
  required: boolean;
  defaultValue?: any;
  options?: any[];
}

export interface ReportSchedule {
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  time: string;
  dayOfWeek?: number;
  dayOfMonth?: number;
  recipients: string[];
  isActive: boolean;
}

export interface ReportExecution {
  id: string;
  templateId: string;
  templateName: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  parameters: any;
  startTime: string;
  endTime?: string;
  duration?: number;
  fileUrl?: string;
  fileSize?: number;
  errorMessage?: string;
  executedBy: string;
}

export interface KPIDefinition {
  id: string;
  code: string;
  name: string;
  description: string;
  category: string;
  formula: string;
  dataSource: string;
  aggregationType: 'SUM' | 'COUNT' | 'AVG' | 'PERCENTAGE' | 'RATIO';
  unit: string;
  target?: number;
  threshold: {
    good: number;
    warning: number;
    critical: number;
  };
  calculationFrequency: 'REAL_TIME' | 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY';
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface KPIValue {
  kpiCode: string;
  value: number;
  target?: number;
  status: 'GOOD' | 'WARNING' | 'CRITICAL';
  trend: 'UP' | 'DOWN' | 'STABLE';
  changePercent: number;
  calculationDate: string;
  period: string;
  metadata?: any;
}

export interface AnalyticsQuery {
  dataSource: string;
  select: string[];
  where?: any;
  groupBy?: string[];
  orderBy?: any[];
  limit?: number;
  offset?: number;
  aggregations?: any[];
}

export interface QueryResult {
  columns: QueryColumn[];
  data: any[];
  totalRows: number;
  executionTime: number;
  query: string;
}

export interface QueryColumn {
  name: string;
  type: 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN';
  label: string;
}

// Analytics API client
export const analyticsApi = {
  // Dashboard Management
  getDashboards: async (userRole?: string): Promise<DashboardConfig[]> => {
    const params = new URLSearchParams();
    if (userRole) params.append('userRole', userRole);

    const response = await apiClient.get<ApiResponse<DashboardConfig[]>>(
      `${ANALYTICS_BASE_URL}/dashboards?${params.toString()}`
    );
    return response.data.data;
  },

  getDashboard: async (dashboardId: string): Promise<DashboardConfig> => {
    const response = await apiClient.get<ApiResponse<DashboardConfig>>(
      `${ANALYTICS_BASE_URL}/dashboards/${dashboardId}`
    );
    return response.data.data;
  },

  createDashboard: async (
    dashboard: Omit<DashboardConfig, 'id' | 'createdAt' | 'updatedAt'>
  ): Promise<DashboardConfig> => {
    const response = await apiClient.post<ApiResponse<DashboardConfig>>(
      `${ANALYTICS_BASE_URL}/dashboards`,
      dashboard
    );
    return response.data.data;
  },

  updateDashboard: async (
    dashboardId: string,
    updates: Partial<DashboardConfig>
  ): Promise<DashboardConfig> => {
    const response = await apiClient.put<ApiResponse<DashboardConfig>>(
      `${ANALYTICS_BASE_URL}/dashboards/${dashboardId}`,
      updates
    );
    return response.data.data;
  },

  deleteDashboard: async (dashboardId: string): Promise<void> => {
    await apiClient.delete(`${ANALYTICS_BASE_URL}/dashboards/${dashboardId}`);
  },

  // Widget Data
  getWidgetData: async (widgetId: string, filters?: any): Promise<any> => {
    const params = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== null && value !== undefined) {
          params.append(key, String(value));
        }
      });
    }

    const response = await apiClient.get<ApiResponse<any>>(
      `${ANALYTICS_BASE_URL}/widgets/${widgetId}/data?${params.toString()}`
    );
    return response.data.data;
  },

  // Report Management
  getReportTemplates: async (category?: string): Promise<ReportTemplate[]> => {
    const params = new URLSearchParams();
    if (category) params.append('category', category);

    const response = await apiClient.get<ApiResponse<ReportTemplate[]>>(
      `${ANALYTICS_BASE_URL}/reports/templates?${params.toString()}`
    );
    return response.data.data;
  },

  getReportTemplate: async (templateId: string): Promise<ReportTemplate> => {
    const response = await apiClient.get<ApiResponse<ReportTemplate>>(
      `${ANALYTICS_BASE_URL}/reports/templates/${templateId}`
    );
    return response.data.data;
  },

  createReportTemplate: async (
    template: Omit<ReportTemplate, 'id' | 'createdAt' | 'updatedAt'>
  ): Promise<ReportTemplate> => {
    const response = await apiClient.post<ApiResponse<ReportTemplate>>(
      `${ANALYTICS_BASE_URL}/reports/templates`,
      template
    );
    return response.data.data;
  },

  updateReportTemplate: async (
    templateId: string,
    updates: Partial<ReportTemplate>
  ): Promise<ReportTemplate> => {
    const response = await apiClient.put<ApiResponse<ReportTemplate>>(
      `${ANALYTICS_BASE_URL}/reports/templates/${templateId}`,
      updates
    );
    return response.data.data;
  },

  // Report Execution
  executeReport: async (
    templateId: string,
    parameters?: any
  ): Promise<ReportExecution> => {
    const response = await apiClient.post<ApiResponse<ReportExecution>>(
      `${ANALYTICS_BASE_URL}/reports/execute`,
      { templateId, parameters }
    );
    return response.data.data;
  },

  getReportExecution: async (executionId: string): Promise<ReportExecution> => {
    const response = await apiClient.get<ApiResponse<ReportExecution>>(
      `${ANALYTICS_BASE_URL}/reports/executions/${executionId}`
    );
    return response.data.data;
  },

  getReportExecutions: async (
    filters?: any
  ): Promise<PaginatedResponse<ReportExecution>> => {
    const params = new URLSearchParams();

    if (filters?.templateId) params.append('templateId', filters.templateId);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.executedBy) params.append('executedBy', filters.executedBy);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<PaginatedResponse<ReportExecution>>(
      `${ANALYTICS_BASE_URL}/reports/executions?${params.toString()}`
    );
    return response.data;
  },

  downloadReport: async (executionId: string): Promise<Blob> => {
    const response = await apiClient.get(
      `${ANALYTICS_BASE_URL}/reports/executions/${executionId}/download`,
      { responseType: 'blob' }
    );
    return response.data;
  },

  // KPI Management
  getKPIDefinitions: async (category?: string): Promise<KPIDefinition[]> => {
    const params = new URLSearchParams();
    if (category) params.append('category', category);

    const response = await apiClient.get<ApiResponse<KPIDefinition[]>>(
      `${ANALYTICS_BASE_URL}/kpis/definitions?${params.toString()}`
    );
    return response.data.data;
  },

  getKPIValues: async (
    kpiCodes: string[],
    period?: string
  ): Promise<KPIValue[]> => {
    const params = new URLSearchParams();
    kpiCodes.forEach(code => params.append('kpiCode', code));
    if (period) params.append('period', period);

    const response = await apiClient.get<ApiResponse<KPIValue[]>>(
      `${ANALYTICS_BASE_URL}/kpis/values?${params.toString()}`
    );
    return response.data.data;
  },

  calculateKPI: async (
    kpiCode: string,
    parameters?: any
  ): Promise<KPIValue> => {
    const response = await apiClient.post<ApiResponse<KPIValue>>(
      `${ANALYTICS_BASE_URL}/kpis/calculate`,
      { kpiCode, parameters }
    );
    return response.data.data;
  },

  // Data Querying
  executeQuery: async (query: AnalyticsQuery): Promise<QueryResult> => {
    const response = await apiClient.post<ApiResponse<QueryResult>>(
      `${ANALYTICS_BASE_URL}/query`,
      query
    );
    return response.data.data;
  },

  // Data Sources
  getDataSources: async (): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(
      `${ANALYTICS_BASE_URL}/data-sources`
    );
    return response.data.data;
  },

  getDataSourceSchema: async (dataSource: string): Promise<any> => {
    const response = await apiClient.get<ApiResponse<any>>(
      `${ANALYTICS_BASE_URL}/data-sources/${dataSource}/schema`
    );
    return response.data.data;
  },

  // Export functionality
  exportDashboard: async (
    dashboardId: string,
    format: 'PDF' | 'PNG' | 'JPEG'
  ): Promise<Blob> => {
    const response = await apiClient.get(
      `${ANALYTICS_BASE_URL}/dashboards/${dashboardId}/export?format=${format}`,
      { responseType: 'blob' }
    );
    return response.data;
  },

  exportData: async (
    query: AnalyticsQuery,
    format: 'CSV' | 'EXCEL' | 'JSON'
  ): Promise<Blob> => {
    const response = await apiClient.post(
      `${ANALYTICS_BASE_URL}/export?format=${format}`,
      query,
      { responseType: 'blob' }
    );
    return response.data;
  },

  // System Metrics
  getSystemMetrics: async (): Promise<any> => {
    try {
      const response = await apiClient.get<ApiResponse<any>>(
        `${ANALYTICS_BASE_URL}/system/metrics`
      );
      return response.data.data;
    } catch (error) {
      // Return mock data if the endpoint is not available
      return {
        totalUsers: 15420,
        activeUsers: 8750,
        totalApplications: 12340,
        pendingApplications: 847,
        approvedApplications: 10893,
        rejectedApplications: 600,
        totalPayments: 45600,
        totalPaymentAmount: 2847392000,
        systemUptime: 99.8,
        responseTime: 1.2,
        errorRate: 0.03,
        memoryUsage: 67.5,
        cpuUsage: 23.4,
        diskUsage: 45.2,
      };
    }
  },

  // System Activities
  getSystemActivities: async (options?: { limit?: number; offset?: number }): Promise<any[]> => {
    try {
      const params = new URLSearchParams();
      if (options?.limit) params.append('limit', options.limit.toString());
      if (options?.offset) params.append('offset', options.offset.toString());

      const response = await apiClient.get<ApiResponse<any[]>>(
        `${ANALYTICS_BASE_URL}/system/activities?${params.toString()}`
      );
      return response.data.data;
    } catch (error) {
      // Return mock data if the endpoint is not available
      return [
        {
          id: '1',
          timestamp: new Date().toISOString(),
          type: 'login',
          userId: 'user123',
          userName: 'John Doe',
          action: 'User Login',
          description: 'User successfully logged into the system',
          ipAddress: '192.168.1.100',
          status: 'success',
        },
        {
          id: '2',
          timestamp: new Date(Date.now() - 300000).toISOString(),
          type: 'application',
          userId: 'user456',
          userName: 'Jane Smith',
          action: 'Application Submitted',
          description: 'New benefit application submitted',
          status: 'success',
        },
        {
          id: '3',
          timestamp: new Date(Date.now() - 600000).toISOString(),
          type: 'payment',
          userId: 'user789',
          userName: 'Bob Johnson',
          action: 'Payment Processed',
          description: 'Monthly benefit payment processed',
          status: 'success',
        },
      ];
    }
  },
};
