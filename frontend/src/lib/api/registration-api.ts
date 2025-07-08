// Registration Service API Client
// API client for household registration and data management

import type {
  HouseholdRegistrationData,
  RegistrationResponse,
  RegistrationStatus,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

import { apiClient } from '../api-client';

// Base URL for Registration Service
const REGISTRATION_BASE_URL = '/api/v1/registration';

// Registration API client
export const registrationApi = {
  // Submit household registration
  submitRegistration: async (
    registrationData: HouseholdRegistrationData
  ): Promise<RegistrationResponse> => {
    const response = await apiClient.post<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households`,
      registrationData
    );
    return response.data.data;
  },

  // Get registration by ID
  getRegistration: async (
    registrationId: string
  ): Promise<RegistrationResponse> => {
    const response = await apiClient.get<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}`
    );
    return response.data.data;
  },

  // Update registration
  updateRegistration: async (
    registrationId: string,
    updates: Partial<HouseholdRegistrationData>
  ): Promise<RegistrationResponse> => {
    const response = await apiClient.patch<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}`,
      updates
    );
    return response.data.data;
  },

  // Get my registrations
  getMyRegistrations: async (): Promise<RegistrationResponse[]> => {
    const response = await apiClient.get<ApiResponse<RegistrationResponse[]>>(
      `${REGISTRATION_BASE_URL}/households/my-registrations`
    );
    return response.data.data;
  },

  // Get registrations by status
  getRegistrationsByStatus: async (
    status: RegistrationStatus
  ): Promise<PaginatedResponse<RegistrationResponse>> => {
    const response = await apiClient.get<
      PaginatedResponse<RegistrationResponse>
    >(`${REGISTRATION_BASE_URL}/households/status/${status}`);
    return response.data;
  },

  // Search registrations
  searchRegistrations: async (
    query: string,
    filters?: any
  ): Promise<PaginatedResponse<RegistrationResponse>> => {
    const params = new URLSearchParams();
    params.append('q', query);

    if (filters?.status) params.append('status', filters.status);
    if (filters?.region) params.append('region', filters.region);
    if (filters?.municipality)
      params.append('municipality', filters.municipality);

    const response = await apiClient.get<
      PaginatedResponse<RegistrationResponse>
    >(`${REGISTRATION_BASE_URL}/households/search?${params.toString()}`);
    return response.data;
  },

  // Approve registration
  approveRegistration: async (
    registrationId: string,
    notes?: string
  ): Promise<RegistrationResponse> => {
    const response = await apiClient.post<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}/approve`,
      { notes }
    );
    return response.data.data;
  },

  // Reject registration
  rejectRegistration: async (
    registrationId: string,
    reason: string
  ): Promise<RegistrationResponse> => {
    const response = await apiClient.post<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}/reject`,
      { reason }
    );
    return response.data.data;
  },

  // Request additional documents
  requestAdditionalDocuments: async (
    registrationId: string,
    documentTypes: string[],
    notes: string
  ): Promise<RegistrationResponse> => {
    const response = await apiClient.post<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}/request-documents`,
      { documentTypes, notes }
    );
    return response.data.data;
  },

  // Upload document
  uploadDocument: async (
    registrationId: string,
    documentType: string,
    file: File
  ): Promise<RegistrationResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);

    const response = await apiClient.post<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}/documents`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  // Verify document
  verifyDocument: async (
    registrationId: string,
    documentId: string,
    isValid: boolean,
    notes?: string
  ): Promise<RegistrationResponse> => {
    const response = await apiClient.post<ApiResponse<RegistrationResponse>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}/documents/${documentId}/verify`,
      { isValid, notes }
    );
    return response.data.data;
  },

  // Get registration statistics
  getRegistrationStatistics: async (filters?: any): Promise<any> => {
    const params = new URLSearchParams();

    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);
    if (filters?.region) params.append('region', filters.region);

    const response = await apiClient.get<ApiResponse<any>>(
      `${REGISTRATION_BASE_URL}/statistics?${params.toString()}`
    );
    return response.data.data;
  },

  // Export registrations
  exportRegistrations: async (
    filters: any,
    format: 'CSV' | 'EXCEL' | 'PDF'
  ): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);

    if (filters.status) params.append('status', filters.status);
    if (filters.region) params.append('region', filters.region);
    if (filters.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange?.end) params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get(
      `${REGISTRATION_BASE_URL}/export?${params.toString()}`,
      { responseType: 'blob' }
    );
    return response.data;
  },

  // Validate household data
  validateHouseholdData: async (
    householdData: Partial<HouseholdRegistrationData>
  ): Promise<any> => {
    const response = await apiClient.post<ApiResponse<any>>(
      `${REGISTRATION_BASE_URL}/validate`,
      householdData
    );
    return response.data.data;
  },

  // Check duplicate registration
  checkDuplicateRegistration: async (personalInfo: any): Promise<any> => {
    const response = await apiClient.post<ApiResponse<any>>(
      `${REGISTRATION_BASE_URL}/check-duplicate`,
      personalInfo
    );
    return response.data.data;
  },

  // Get registration audit log
  getRegistrationAuditLog: async (registrationId: string): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}/audit-log`
    );
    return response.data.data;
  },

  // Get pending reviews
  getPendingReviews: async (): Promise<RegistrationResponse[]> => {
    const response = await apiClient.get<ApiResponse<RegistrationResponse[]>>(
      `${REGISTRATION_BASE_URL}/households/pending-review`
    );
    return response.data.data;
  },

  // Bulk approve registrations
  bulkApproveRegistrations: async (
    registrationIds: string[],
    notes?: string
  ): Promise<RegistrationResponse[]> => {
    const response = await apiClient.post<ApiResponse<RegistrationResponse[]>>(
      `${REGISTRATION_BASE_URL}/households/bulk-approve`,
      { registrationIds, notes }
    );
    return response.data.data;
  },

  // Get registration timeline
  getRegistrationTimeline: async (registrationId: string): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(
      `${REGISTRATION_BASE_URL}/households/${registrationId}/timeline`
    );
    return response.data.data;
  },

  // Get registrations with filters
  getRegistrations: async (
    filters: any = {}
  ): Promise<PaginatedResponse<RegistrationResponse>> => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, String(value));
      }
    });

    const response = await apiClient.get<
      PaginatedResponse<RegistrationResponse>
    >(`${REGISTRATION_BASE_URL}/households?${params.toString()}`);
    return response.data;
  },
};
