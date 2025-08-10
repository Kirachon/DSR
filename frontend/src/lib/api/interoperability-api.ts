// Interoperability Service API Client
// API client for external system integration, service delivery tracking, and API gateway

import type { ApiResponse, PaginatedResponse } from '@/types';

import { apiClient } from '../api-client';

// Base URL for Interoperability Service
const INTEROPERABILITY_BASE_URL = '/api/v1/interoperability';

// Interoperability API Types
export interface ExternalSystem {
  id: string;
  systemCode: string;
  systemName: string;
  description: string;
  baseUrl: string;
  authType: 'API_KEY' | 'OAUTH2' | 'BASIC_AUTH' | 'JWT';
  authConfig: any;
  isActive: boolean;
  isHealthy: boolean;
  lastHealthCheck: string;
  rateLimitPerMinute: number;
  timeoutSeconds: number;
  retryAttempts: number;
  supportedOperations: string[];
  dataFormat: 'JSON' | 'XML' | 'CSV';
  version: string;
  contactInfo: {
    name: string;
    email: string;
    phone?: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface ServiceDeliveryRecord {
  id: string;
  beneficiaryPsn: string;
  beneficiaryName: string;
  serviceCode: string;
  serviceName: string;
  serviceProvider: string;
  deliveryDate: string;
  deliveryMethod: 'DIGITAL' | 'PHYSICAL' | 'HYBRID';
  deliveryLocation?: string;
  amount?: number;
  currency?: string;
  status: 'PENDING' | 'DELIVERED' | 'FAILED' | 'CANCELLED' | 'VERIFIED';
  verificationDate?: string;
  verifiedBy?: string;
  externalReference: string;
  metadata: any;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiGatewayRequest {
  systemCode: string;
  endpoint: string;
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  headers?: Record<string, string>;
  body?: any;
  timeout?: number;
}

export interface ApiGatewayResponse {
  statusCode: number;
  headers: Record<string, string>;
  body: any;
  responseTime: number;
  timestamp: string;
  requestId: string;
}

export interface DataSharingAgreement {
  id: string;
  agreementCode: string;
  partnerAgency: string;
  partnerContact: {
    name: string;
    email: string;
    phone?: string;
    position: string;
  };
  dataTypes: string[];
  accessLevel: 'READ_ONLY' | 'READ_WRITE' | 'WRITE_ONLY';
  purposeOfUse: string;
  legalBasis: string;
  effectiveDate: string;
  expiryDate: string;
  isActive: boolean;
  restrictions: string[];
  auditRequirements: string[];
  signedBy: string;
  signedDate: string;
  lastReviewDate?: string;
  nextReviewDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface SystemHealthStatus {
  systemCode: string;
  systemName: string;
  status: 'HEALTHY' | 'DEGRADED' | 'DOWN' | 'MAINTENANCE';
  responseTime: number;
  lastChecked: string;
  uptime: number;
  errorRate: number;
  throughput: number;
  issues: HealthIssue[];
}

export interface HealthIssue {
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  timestamp: string;
  resolved: boolean;
}

export interface IntegrationLog {
  id: string;
  systemCode: string;
  operation: string;
  method: string;
  endpoint: string;
  requestId: string;
  statusCode: number;
  responseTime: number;
  requestSize: number;
  responseSize: number;
  success: boolean;
  errorMessage?: string;
  userId?: string;
  ipAddress: string;
  userAgent: string;
  timestamp: string;
}

// Interoperability API client
export const interoperabilityApi = {
  // External System Management
  getExternalSystems: async (): Promise<ExternalSystem[]> => {
    const response = await apiClient.get<ApiResponse<ExternalSystem[]>>(
      `${INTEROPERABILITY_BASE_URL}/systems`
    );
    return response.data.data;
  },

  getExternalSystem: async (systemCode: string): Promise<ExternalSystem> => {
    const response = await apiClient.get<ApiResponse<ExternalSystem>>(
      `${INTEROPERABILITY_BASE_URL}/systems/${systemCode}`
    );
    return response.data.data;
  },

  createExternalSystem: async (
    system: Omit<ExternalSystem, 'id' | 'createdAt' | 'updatedAt'>
  ): Promise<ExternalSystem> => {
    const response = await apiClient.post<ApiResponse<ExternalSystem>>(
      `${INTEROPERABILITY_BASE_URL}/systems`,
      system
    );
    return response.data.data;
  },

  updateExternalSystem: async (
    systemCode: string,
    updates: Partial<ExternalSystem>
  ): Promise<ExternalSystem> => {
    const response = await apiClient.put<ApiResponse<ExternalSystem>>(
      `${INTEROPERABILITY_BASE_URL}/systems/${systemCode}`,
      updates
    );
    return response.data.data;
  },

  deleteExternalSystem: async (systemCode: string): Promise<void> => {
    await apiClient.delete(
      `${INTEROPERABILITY_BASE_URL}/systems/${systemCode}`
    );
  },

  // System Health Monitoring
  checkSystemHealth: async (
    systemCode: string
  ): Promise<SystemHealthStatus> => {
    const response = await apiClient.post<ApiResponse<SystemHealthStatus>>(
      `${INTEROPERABILITY_BASE_URL}/systems/${systemCode}/health-check`
    );
    return response.data.data;
  },

  getAllSystemsHealth: async (): Promise<SystemHealthStatus[]> => {
    const response = await apiClient.get<ApiResponse<SystemHealthStatus[]>>(
      `${INTEROPERABILITY_BASE_URL}/systems/health`
    );
    return response.data.data;
  },

  // API Gateway
  routeRequest: async (
    request: ApiGatewayRequest
  ): Promise<ApiGatewayResponse> => {
    const response = await apiClient.post<ApiResponse<ApiGatewayResponse>>(
      `${INTEROPERABILITY_BASE_URL}/gateway/route`,
      request
    );
    return response.data.data;
  },

  // Service Delivery Tracking
  recordServiceDelivery: async (
    record: Omit<ServiceDeliveryRecord, 'id' | 'createdAt' | 'updatedAt'>
  ): Promise<ServiceDeliveryRecord> => {
    const response = await apiClient.post<ApiResponse<ServiceDeliveryRecord>>(
      `${INTEROPERABILITY_BASE_URL}/service-delivery`,
      record
    );
    return response.data.data;
  },

  getServiceDeliveryRecord: async (
    recordId: string
  ): Promise<ServiceDeliveryRecord> => {
    const response = await apiClient.get<ApiResponse<ServiceDeliveryRecord>>(
      `${INTEROPERABILITY_BASE_URL}/service-delivery/${recordId}`
    );
    return response.data.data;
  },

  getServiceDeliveryHistory: async (
    beneficiaryPsn: string
  ): Promise<ServiceDeliveryRecord[]> => {
    const response = await apiClient.get<ApiResponse<ServiceDeliveryRecord[]>>(
      `${INTEROPERABILITY_BASE_URL}/service-delivery/beneficiary/${beneficiaryPsn}`
    );
    return response.data.data;
  },

  searchServiceDeliveries: async (
    filters?: any
  ): Promise<PaginatedResponse<ServiceDeliveryRecord>> => {
    const params = new URLSearchParams();

    if (filters?.beneficiaryPsn)
      params.append('beneficiaryPsn', filters.beneficiaryPsn);
    if (filters?.serviceCode) params.append('serviceCode', filters.serviceCode);
    if (filters?.serviceProvider)
      params.append('serviceProvider', filters.serviceProvider);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<
      PaginatedResponse<ServiceDeliveryRecord>
    >(
      `${INTEROPERABILITY_BASE_URL}/service-delivery/search?${params.toString()}`
    );
    return response.data;
  },

  updateServiceDeliveryStatus: async (
    recordId: string,
    status: string,
    notes?: string
  ): Promise<ServiceDeliveryRecord> => {
    const response = await apiClient.patch<ApiResponse<ServiceDeliveryRecord>>(
      `${INTEROPERABILITY_BASE_URL}/service-delivery/${recordId}/status`,
      { status, notes }
    );
    return response.data.data;
  },

  verifyServiceDelivery: async (
    recordId: string,
    verificationData: any
  ): Promise<ServiceDeliveryRecord> => {
    const response = await apiClient.post<ApiResponse<ServiceDeliveryRecord>>(
      `${INTEROPERABILITY_BASE_URL}/service-delivery/${recordId}/verify`,
      verificationData
    );
    return response.data.data;
  },

  // Data Sharing Agreements
  getDataSharingAgreements: async (): Promise<DataSharingAgreement[]> => {
    const response = await apiClient.get<ApiResponse<DataSharingAgreement[]>>(
      `${INTEROPERABILITY_BASE_URL}/data-sharing/agreements`
    );
    return response.data.data;
  },

  getDataSharingAgreement: async (
    agreementCode: string
  ): Promise<DataSharingAgreement> => {
    const response = await apiClient.get<ApiResponse<DataSharingAgreement>>(
      `${INTEROPERABILITY_BASE_URL}/data-sharing/agreements/${agreementCode}`
    );
    return response.data.data;
  },

  createDataSharingAgreement: async (
    agreement: Omit<DataSharingAgreement, 'id' | 'createdAt' | 'updatedAt'>
  ): Promise<DataSharingAgreement> => {
    const response = await apiClient.post<ApiResponse<DataSharingAgreement>>(
      `${INTEROPERABILITY_BASE_URL}/data-sharing/agreements`,
      agreement
    );
    return response.data.data;
  },

  updateDataSharingAgreement: async (
    agreementCode: string,
    updates: Partial<DataSharingAgreement>
  ): Promise<DataSharingAgreement> => {
    const response = await apiClient.put<ApiResponse<DataSharingAgreement>>(
      `${INTEROPERABILITY_BASE_URL}/data-sharing/agreements/${agreementCode}`,
      updates
    );
    return response.data.data;
  },

  // Integration Logs and Monitoring
  getIntegrationLogs: async (
    filters?: any
  ): Promise<PaginatedResponse<IntegrationLog>> => {
    const params = new URLSearchParams();

    if (filters?.systemCode) params.append('systemCode', filters.systemCode);
    if (filters?.operation) params.append('operation', filters.operation);
    if (filters?.success !== undefined)
      params.append('success', filters.success.toString());
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<PaginatedResponse<IntegrationLog>>(
      `${INTEROPERABILITY_BASE_URL}/logs?${params.toString()}`
    );
    return response.data;
  },

  // Statistics and Analytics
  getIntegrationStatistics: async (filters?: any): Promise<any> => {
    const params = new URLSearchParams();

    if (filters?.systemCode) params.append('systemCode', filters.systemCode);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<ApiResponse<any>>(
      `${INTEROPERABILITY_BASE_URL}/statistics?${params.toString()}`
    );
    return response.data.data;
  },

  getServiceDeliveryStatistics: async (filters?: any): Promise<any> => {
    const params = new URLSearchParams();

    if (filters?.serviceCode) params.append('serviceCode', filters.serviceCode);
    if (filters?.serviceProvider)
      params.append('serviceProvider', filters.serviceProvider);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<ApiResponse<any>>(
      `${INTEROPERABILITY_BASE_URL}/service-delivery/statistics?${params.toString()}`
    );
    return response.data.data;
  },

  // Export functionality
  exportServiceDeliveryData: async (
    filters: any,
    format: 'CSV' | 'EXCEL' | 'PDF'
  ): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);

    if (filters.serviceCode) params.append('serviceCode', filters.serviceCode);
    if (filters.status) params.append('status', filters.status);
    if (filters.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange?.end) params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get(
      `${INTEROPERABILITY_BASE_URL}/service-delivery/export?${params.toString()}`,
      { responseType: 'blob' }
    );
    return response.data;
  },
};
