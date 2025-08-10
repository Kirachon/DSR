// Grievance Service API Client
// API client for case management and grievance handling

import type {
  Case,
  CaseFilters,
  CreateCaseRequest,
  UpdateCaseRequest,
  CreateCaseNoteRequest,
  CaseStatistics,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

import { apiClient } from '../api-client';

// Base URL for Grievance Service
const GRIEVANCE_BASE_URL = '/api/v1/grievances';

// Grievance API client
export const grievanceApi = {
  // Get all cases with filters
  getCases: async (filters: CaseFilters): Promise<PaginatedResponse<Case>> => {
    const params = new URLSearchParams();

    if (filters.status) params.append('status', filters.status);
    if (filters.type) params.append('type', filters.type);
    if (filters.priority) params.append('priority', filters.priority);
    if (filters.category) params.append('category', filters.category);
    if (filters.assignedTo) params.append('assignedTo', filters.assignedTo);
    if (filters.submittedBy) params.append('submittedBy', filters.submittedBy);
    if (filters.dateRange.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange.end) params.append('endDate', filters.dateRange.end);
    if (filters.searchQuery) params.append('q', filters.searchQuery);
    if (filters.tags && filters.tags.length > 0) {
      filters.tags.forEach(tag => params.append('tags', tag));
    }
    if (filters.isUrgent !== undefined)
      params.append('isUrgent', filters.isUrgent.toString());

    const response = await apiClient.get<PaginatedResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases?${params.toString()}`
    );
    return response.data;
  },

  // Get case by ID
  getCase: async (caseId: string): Promise<Case> => {
    const response = await apiClient.get<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}`
    );
    return response.data.data;
  },

  // Create new case
  createCase: async (caseData: CreateCaseRequest): Promise<Case> => {
    const response = await apiClient.post<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases`,
      caseData
    );
    return response.data.data;
  },

  // Update case
  updateCase: async (
    caseId: string,
    updates: UpdateCaseRequest
  ): Promise<Case> => {
    const response = await apiClient.patch<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}`,
      updates
    );
    return response.data.data;
  },

  // Update case status
  updateCaseStatus: async (caseId: string, status: string): Promise<Case> => {
    const response = await apiClient.patch<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/status`,
      { status }
    );
    return response.data.data;
  },

  // Assign case
  assignCase: async (
    caseId: string,
    assignedToId: string,
    notes?: string
  ): Promise<Case> => {
    const response = await apiClient.post<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/assign`,
      { assignedToId, notes }
    );
    return response.data.data;
  },

  // Add case note
  addCaseNote: async (
    caseId: string,
    noteData: Omit<CreateCaseNoteRequest, 'caseId'>
  ): Promise<Case> => {
    const response = await apiClient.post<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/notes`,
      noteData
    );
    return response.data.data;
  },

  // Upload case attachment
  uploadAttachment: async (caseId: string, file: File): Promise<Case> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/attachments`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  // Delete case attachment
  deleteAttachment: async (
    caseId: string,
    attachmentId: string
  ): Promise<Case> => {
    const response = await apiClient.delete<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/attachments/${attachmentId}`
    );
    return response.data.data;
  },

  // Escalate case
  escalateCase: async (
    caseId: string,
    reason: string,
    escalatedToId: string
  ): Promise<Case> => {
    const response = await apiClient.post<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/escalate`,
      { reason, escalatedToId }
    );
    return response.data.data;
  },

  // Close case
  closeCase: async (caseId: string, resolution: any): Promise<Case> => {
    const response = await apiClient.post<ApiResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/close`,
      { resolution }
    );
    return response.data.data;
  },

  // Get case statistics
  getCaseStatistics: async (
    filters?: Partial<CaseFilters>
  ): Promise<CaseStatistics> => {
    const params = new URLSearchParams();

    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);
    if (filters?.assignedTo) params.append('assignedTo', filters.assignedTo);

    const response = await apiClient.get<ApiResponse<CaseStatistics>>(
      `${GRIEVANCE_BASE_URL}/statistics?${params.toString()}`
    );
    return response.data.data;
  },

  // Search cases
  searchCases: async (
    query: string,
    filters?: Partial<CaseFilters>
  ): Promise<PaginatedResponse<Case>> => {
    const params = new URLSearchParams();
    params.append('q', query);

    if (filters?.status) params.append('status', filters.status);
    if (filters?.type) params.append('type', filters.type);
    if (filters?.priority) params.append('priority', filters.priority);

    const response = await apiClient.get<PaginatedResponse<Case>>(
      `${GRIEVANCE_BASE_URL}/search?${params.toString()}`
    );
    return response.data;
  },

  // Get my assigned cases
  getMyAssignedCases: async (): Promise<Case[]> => {
    const response = await apiClient.get<ApiResponse<Case[]>>(
      `${GRIEVANCE_BASE_URL}/cases/assigned-to-me`
    );
    return response.data.data;
  },

  // Get cases by status
  getCasesByStatus: async (status: string): Promise<Case[]> => {
    const response = await apiClient.get<ApiResponse<Case[]>>(
      `${GRIEVANCE_BASE_URL}/cases/status/${status}`
    );
    return response.data.data;
  },

  // Get urgent cases
  getUrgentCases: async (): Promise<Case[]> => {
    const response = await apiClient.get<ApiResponse<Case[]>>(
      `${GRIEVANCE_BASE_URL}/cases/urgent`
    );
    return response.data.data;
  },

  // Get overdue cases
  getOverdueCases: async (): Promise<Case[]> => {
    const response = await apiClient.get<ApiResponse<Case[]>>(
      `${GRIEVANCE_BASE_URL}/cases/overdue`
    );
    return response.data.data;
  },

  // Bulk update cases
  bulkUpdateCases: async (
    caseIds: string[],
    updates: Partial<UpdateCaseRequest>
  ): Promise<Case[]> => {
    const response = await apiClient.patch<ApiResponse<Case[]>>(
      `${GRIEVANCE_BASE_URL}/cases/bulk-update`,
      { caseIds, updates }
    );
    return response.data.data;
  },

  // Export cases
  exportCases: async (
    filters: CaseFilters,
    format: 'CSV' | 'EXCEL' | 'PDF'
  ): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);

    if (filters.status) params.append('status', filters.status);
    if (filters.type) params.append('type', filters.type);
    if (filters.dateRange.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange.end) params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get(
      `${GRIEVANCE_BASE_URL}/export?${params.toString()}`,
      { responseType: 'blob' }
    );
    return response.data;
  },

  // Get case timeline
  getCaseTimeline: async (caseId: string): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/timeline`
    );
    return response.data.data;
  },

  // Get case audit log
  getCaseAuditLog: async (caseId: string): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(
      `${GRIEVANCE_BASE_URL}/cases/${caseId}/audit-log`
    );
    return response.data.data;
  },
};
