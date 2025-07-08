// Data Management Service API Client
// API client for data ingestion, validation, deduplication, and PhilSys integration

import type { ApiResponse, PaginatedResponse } from '@/types';

import { serviceClients } from '../service-clients';

// Base URL for Data Management Service
const DATA_MANAGEMENT_BASE_URL = '/api/v1/data-management';

// Data Management API Types
export interface HouseholdData {
  id: string;
  psn: string;
  householdHead: {
    firstName: string;
    lastName: string;
    middleName?: string;
    birthDate: string;
    gender: string;
    civilStatus: string;
    nationality: string;
    religion?: string;
    occupation?: string;
    monthlyIncome?: number;
    educationLevel?: string;
    philsysId?: string;
  };
  members: HouseholdMember[];
  address: {
    region: string;
    province: string;
    municipality: string;
    barangay: string;
    streetAddress: string;
    zipCode: string;
    coordinates?: {
      latitude: number;
      longitude: number;
    };
  };
  socioEconomic: {
    monthlyIncome: number;
    incomeSource: string;
    housingType: string;
    housingOwnership: string;
    waterSource: string;
    electricitySource: string;
    toiletFacility: string;
    cookingFuel: string;
    assets: string[];
    vulnerabilities: string[];
  };
  status: 'DRAFT' | 'SUBMITTED' | 'VALIDATED' | 'APPROVED' | 'REJECTED';
  validationStatus: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  duplicateCheckStatus: 'PENDING' | 'CHECKED' | 'DUPLICATE_FOUND' | 'CLEAN';
  philsysStatus: 'PENDING' | 'VERIFIED' | 'FAILED' | 'NOT_FOUND';
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface HouseholdMember {
  id: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  birthDate: string;
  gender: string;
  relationship: string;
  civilStatus: string;
  occupation?: string;
  monthlyIncome?: number;
  educationLevel?: string;
  philsysId?: string;
  isHouseholdHead: boolean;
  disabilities?: string[];
  chronicIllnesses?: string[];
}

export interface ValidationResult {
  isValid: boolean;
  errors: ValidationError[];
  warnings: ValidationWarning[];
  score: number;
  recommendations: string[];
}

export interface ValidationError {
  field: string;
  code: string;
  message: string;
  severity: 'ERROR' | 'WARNING' | 'INFO';
}

export interface ValidationWarning {
  field: string;
  code: string;
  message: string;
  suggestion: string;
}

export interface DuplicateCheckResult {
  isDuplicate: boolean;
  confidence: number;
  matches: DuplicateMatch[];
  recommendations: string[];
}

export interface DuplicateMatch {
  householdId: string;
  psn: string;
  householdHead: string;
  matchScore: number;
  matchingFields: string[];
  address: string;
  registrationDate: string;
}

export interface PhilSysValidationResult {
  isValid: boolean;
  status: 'VERIFIED' | 'NOT_FOUND' | 'INVALID' | 'ERROR';
  citizenInfo?: {
    fullName: string;
    birthDate: string;
    gender: string;
    address: string;
    civilStatus: string;
  };
  errorMessage?: string;
}

export interface DataProcessingJob {
  id: string;
  jobType: 'VALIDATION' | 'DEDUPLICATION' | 'PHILSYS_CHECK' | 'BULK_IMPORT';
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  progress: number;
  totalRecords: number;
  processedRecords: number;
  successfulRecords: number;
  failedRecords: number;
  startTime: string;
  endTime?: string;
  errorMessage?: string;
  results?: any;
  createdBy: string;
}

// Data Management API client
export const dataManagementApi = {
  // Household Data Management
  getHouseholdData: async (householdId: string): Promise<HouseholdData> => {
    const response = await serviceClients.dataManagement.get<
      ApiResponse<HouseholdData>
    >(`${DATA_MANAGEMENT_BASE_URL}/households/${householdId}`);
    return response.data.data;
  },

  // Alias for consistency with household management interface
  getHousehold: async (householdId: string): Promise<HouseholdData> => {
    return dataManagementApi.getHouseholdData(householdId);
  },

  // Get all households with pagination
  getHouseholds: async (params?: {
    page?: number;
    limit?: number;
    region?: string;
    province?: string;
    municipality?: string;
    status?: string;
    validationStatus?: string;
  }): Promise<PaginatedResponse<HouseholdData>> => {
    const searchParams = new URLSearchParams();

    if (params?.page) searchParams.append('page', params.page.toString());
    if (params?.limit) searchParams.append('limit', params.limit.toString());
    if (params?.region) searchParams.append('region', params.region);
    if (params?.province) searchParams.append('province', params.province);
    if (params?.municipality) searchParams.append('municipality', params.municipality);
    if (params?.status) searchParams.append('status', params.status);
    if (params?.validationStatus) searchParams.append('validationStatus', params.validationStatus);

    const response = await serviceClients.dataManagement.get<
      PaginatedResponse<HouseholdData>
    >(`${DATA_MANAGEMENT_BASE_URL}/households?${searchParams.toString()}`);
    return response.data;
  },

  // Create new household
  createHousehold: async (householdData: any): Promise<HouseholdData> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<HouseholdData>
    >(`${DATA_MANAGEMENT_BASE_URL}/households`, householdData);
    return response.data.data;
  },

  // Delete household
  deleteHousehold: async (householdId: string): Promise<void> => {
    await serviceClients.dataManagement.delete(
      `${DATA_MANAGEMENT_BASE_URL}/households/${householdId}`
    );
  },

  searchHouseholds: async (
    query: string,
    filters?: any
  ): Promise<PaginatedResponse<HouseholdData>> => {
    const params = new URLSearchParams();
    params.append('q', query);

    if (filters?.region) params.append('region', filters.region);
    if (filters?.province) params.append('province', filters.province);
    if (filters?.municipality)
      params.append('municipality', filters.municipality);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.validationStatus)
      params.append('validationStatus', filters.validationStatus);

    const response = await serviceClients.dataManagement.get<
      PaginatedResponse<HouseholdData>
    >(`${DATA_MANAGEMENT_BASE_URL}/households/search?${params.toString()}`);
    return response.data;
  },

  updateHouseholdData: async (
    householdId: string,
    updates: Partial<HouseholdData>
  ): Promise<HouseholdData> => {
    const response = await serviceClients.dataManagement.patch<
      ApiResponse<HouseholdData>
    >(`${DATA_MANAGEMENT_BASE_URL}/households/${householdId}`, updates);
    return response.data.data;
  },

  // Data Validation
  validateHouseholdData: async (
    householdData: Partial<HouseholdData>
  ): Promise<ValidationResult> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<ValidationResult>
    >(`${DATA_MANAGEMENT_BASE_URL}/validation/validate`, householdData);
    return response.data.data;
  },

  getValidationRules: async (): Promise<any[]> => {
    const response = await serviceClients.dataManagement.get<
      ApiResponse<any[]>
    >(`${DATA_MANAGEMENT_BASE_URL}/validation/rules`);
    return response.data.data;
  },

  updateValidationRules: async (rules: any[]): Promise<any[]> => {
    const response = await serviceClients.dataManagement.put<
      ApiResponse<any[]>
    >(`${DATA_MANAGEMENT_BASE_URL}/validation/rules`, { rules });
    return response.data.data;
  },

  // Duplicate Detection
  checkDuplicates: async (
    householdData: Partial<HouseholdData>
  ): Promise<DuplicateCheckResult> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<DuplicateCheckResult>
    >(`${DATA_MANAGEMENT_BASE_URL}/deduplication/check`, householdData);
    return response.data.data;
  },

  resolveDuplicate: async (
    householdId: string,
    action: 'MERGE' | 'KEEP_BOTH' | 'MARK_INVALID',
    targetId?: string
  ): Promise<HouseholdData> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<HouseholdData>
    >(`${DATA_MANAGEMENT_BASE_URL}/deduplication/resolve`, {
      householdId,
      action,
      targetId,
    });
    return response.data.data;
  },

  // PhilSys Integration
  validateWithPhilSys: async (
    philsysId: string,
    personalInfo: any
  ): Promise<PhilSysValidationResult> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<PhilSysValidationResult>
    >(`${DATA_MANAGEMENT_BASE_URL}/philsys/validate`, {
      philsysId,
      personalInfo,
    });
    return response.data.data;
  },

  syncWithPhilSys: async (householdId: string): Promise<HouseholdData> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<HouseholdData>
    >(`${DATA_MANAGEMENT_BASE_URL}/philsys/sync/${householdId}`);
    return response.data.data;
  },

  // Data Processing Jobs
  createProcessingJob: async (
    jobType: string,
    parameters: any
  ): Promise<DataProcessingJob> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<DataProcessingJob>
    >(`${DATA_MANAGEMENT_BASE_URL}/jobs`, { jobType, parameters });
    return response.data.data;
  },

  getProcessingJob: async (jobId: string): Promise<DataProcessingJob> => {
    const response = await serviceClients.dataManagement.get<
      ApiResponse<DataProcessingJob>
    >(`${DATA_MANAGEMENT_BASE_URL}/jobs/${jobId}`);
    return response.data.data;
  },

  getProcessingJobs: async (
    filters?: any
  ): Promise<PaginatedResponse<DataProcessingJob>> => {
    const params = new URLSearchParams();

    if (filters?.status) params.append('status', filters.status);
    if (filters?.jobType) params.append('jobType', filters.jobType);
    if (filters?.createdBy) params.append('createdBy', filters.createdBy);

    const response = await serviceClients.dataManagement.get<
      PaginatedResponse<DataProcessingJob>
    >(`${DATA_MANAGEMENT_BASE_URL}/jobs?${params.toString()}`);
    return response.data;
  },

  cancelProcessingJob: async (jobId: string): Promise<DataProcessingJob> => {
    const response = await serviceClients.dataManagement.post<
      ApiResponse<DataProcessingJob>
    >(`${DATA_MANAGEMENT_BASE_URL}/jobs/${jobId}/cancel`);
    return response.data.data;
  },

  // Data Import/Export
  importData: async (
    file: File,
    format: 'CSV' | 'EXCEL' | 'JSON'
  ): Promise<DataProcessingJob> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('format', format);

    const response = await serviceClients.dataManagement.post<
      ApiResponse<DataProcessingJob>
    >(`${DATA_MANAGEMENT_BASE_URL}/import`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data.data;
  },

  exportData: async (
    filters: any,
    format: 'CSV' | 'EXCEL' | 'JSON'
  ): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);

    if (filters.region) params.append('region', filters.region);
    if (filters.status) params.append('status', filters.status);
    if (filters.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange?.end) params.append('endDate', filters.dateRange.end);

    const response = await serviceClients.dataManagement.get(
      `${DATA_MANAGEMENT_BASE_URL}/export?${params.toString()}`,
      { responseType: 'blob' }
    );
    return response.data;
  },

  // Statistics and Analytics
  getDataQualityMetrics: async (filters?: any): Promise<any> => {
    const params = new URLSearchParams();

    if (filters?.region) params.append('region', filters.region);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await serviceClients.dataManagement.get<ApiResponse<any>>(
      `${DATA_MANAGEMENT_BASE_URL}/metrics/data-quality?${params.toString()}`
    );
    return response.data.data;
  },

  getProcessingStatistics: async (filters?: any): Promise<any> => {
    const params = new URLSearchParams();

    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await serviceClients.dataManagement.get<ApiResponse<any>>(
      `${DATA_MANAGEMENT_BASE_URL}/metrics/processing?${params.toString()}`
    );
    return response.data.data;
  },
};
