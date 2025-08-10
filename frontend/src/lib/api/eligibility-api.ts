// Eligibility Service API Client
// API client for eligibility assessment, PMT calculation, and program matching

import type { ApiResponse, PaginatedResponse } from '@/types';

import { apiClient } from '../api-client';

// Base URL for Eligibility Service
const ELIGIBILITY_BASE_URL = '/api/v1/eligibility';

// Eligibility API Types
export interface EligibilityRequest {
  psn: string;
  programCode: string;
  householdData?: any;
  forceRecalculation?: boolean;
  assessmentDate?: string;
}

export interface EligibilityResponse {
  id: string;
  psn: string;
  programCode: string;
  programName: string;
  status: 'ELIGIBLE' | 'NOT_ELIGIBLE' | 'CONDITIONAL' | 'PENDING_REVIEW';
  eligibilityScore: number;
  pmtScore?: number;
  assessmentDate: string;
  validUntil: string;
  criteria: EligibilityCriteria[];
  recommendations: string[];
  conditions?: EligibilityCondition[];
  assessedBy: string;
  reviewedBy?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EligibilityCriteria {
  criteriaCode: string;
  criteriaName: string;
  required: boolean;
  met: boolean;
  value: any;
  threshold: any;
  weight: number;
  score: number;
  description: string;
}

export interface EligibilityCondition {
  conditionCode: string;
  conditionName: string;
  description: string;
  dueDate: string;
  status: 'PENDING' | 'MET' | 'OVERDUE' | 'WAIVED';
  evidence?: string[];
}

export interface PMTCalculationRequest {
  householdData: {
    householdSize: number;
    monthlyIncome: number;
    incomeSource: string;
    housingType: string;
    housingOwnership: string;
    waterSource: string;
    electricitySource: string;
    toiletFacility: string;
    cookingFuel: string;
    assets: string[];
    location: {
      region: string;
      province: string;
      municipality: string;
      barangay: string;
      urbanRural: 'URBAN' | 'RURAL';
    };
    demographics: {
      householdHeadAge: number;
      householdHeadGender: string;
      householdHeadEducation: string;
      householdHeadOccupation: string;
      dependencyRatio: number;
      elderlyMembers: number;
      childrenMembers: number;
      disabledMembers: number;
    };
  };
  calculationDate?: string;
}

export interface PMTCalculationResult {
  pmtScore: number;
  povertyProbability: number;
  povertyClassification: 'EXTREMELY_POOR' | 'POOR' | 'NEAR_POOR' | 'NOT_POOR';
  components: PMTComponent[];
  methodology: string;
  calculationDate: string;
  validUntil: string;
}

export interface PMTComponent {
  category: string;
  weight: number;
  value: number;
  score: number;
  contribution: number;
  description: string;
}

export interface ProgramEligibilityRules {
  programCode: string;
  programName: string;
  description: string;
  targetBeneficiaries: string;
  eligibilityCriteria: ProgramCriteria[];
  pmtThreshold?: number;
  categoricalRequirements: string[];
  exclusionCriteria: string[];
  conditionsForEligibility: string[];
  benefitAmount?: number;
  benefitDuration?: string;
  isActive: boolean;
  effectiveDate: string;
  expiryDate?: string;
}

export interface ProgramCriteria {
  criteriaCode: string;
  criteriaName: string;
  dataField: string;
  operator:
    | 'EQUALS'
    | 'NOT_EQUALS'
    | 'GREATER_THAN'
    | 'LESS_THAN'
    | 'GREATER_EQUAL'
    | 'LESS_EQUAL'
    | 'IN'
    | 'NOT_IN'
    | 'CONTAINS';
  value: any;
  weight: number;
  required: boolean;
  description: string;
}

export interface EligibilityAssessmentHistory {
  id: string;
  psn: string;
  programCode: string;
  assessmentDate: string;
  status: string;
  eligibilityScore: number;
  pmtScore?: number;
  assessedBy: string;
  changes: AssessmentChange[];
  reason: string;
}

export interface AssessmentChange {
  field: string;
  oldValue: any;
  newValue: any;
  reason: string;
}

// Eligibility API client
export const eligibilityApi = {
  // Eligibility Assessment
  assessEligibility: async (
    request: EligibilityRequest
  ): Promise<EligibilityResponse> => {
    const response = await apiClient.post<ApiResponse<EligibilityResponse>>(
      `${ELIGIBILITY_BASE_URL}/assess`,
      request
    );
    return response.data.data;
  },

  getEligibilityAssessment: async (
    psn: string,
    programCode: string
  ): Promise<EligibilityResponse> => {
    const response = await apiClient.get<ApiResponse<EligibilityResponse>>(
      `${ELIGIBILITY_BASE_URL}/assessments/${psn}/${programCode}`
    );
    return response.data.data;
  },

  getHouseholdEligibility: async (
    psn: string
  ): Promise<EligibilityResponse[]> => {
    const response = await apiClient.get<ApiResponse<EligibilityResponse[]>>(
      `${ELIGIBILITY_BASE_URL}/households/${psn}/eligibility`
    );
    return response.data.data;
  },

  updateEligibilityStatus: async (
    psn: string,
    programCode: string,
    status: string,
    reason: string
  ): Promise<EligibilityResponse> => {
    const response = await apiClient.put<ApiResponse<EligibilityResponse>>(
      `${ELIGIBILITY_BASE_URL}/status/${psn}`,
      { programCode, status, reason }
    );
    return response.data.data;
  },

  // PMT Calculation
  calculatePMT: async (
    request: PMTCalculationRequest
  ): Promise<PMTCalculationResult> => {
    const response = await apiClient.post<ApiResponse<PMTCalculationResult>>(
      `${ELIGIBILITY_BASE_URL}/pmt/calculate`,
      request
    );
    return response.data.data;
  },

  getPMTHistory: async (psn: string): Promise<PMTCalculationResult[]> => {
    const response = await apiClient.get<ApiResponse<PMTCalculationResult[]>>(
      `${ELIGIBILITY_BASE_URL}/pmt/history/${psn}`
    );
    return response.data.data;
  },

  // Program Management
  getPrograms: async (): Promise<ProgramEligibilityRules[]> => {
    const response = await apiClient.get<
      ApiResponse<ProgramEligibilityRules[]>
    >(`${ELIGIBILITY_BASE_URL}/programs`);
    return response.data.data;
  },

  getProgram: async (programCode: string): Promise<ProgramEligibilityRules> => {
    const response = await apiClient.get<ApiResponse<ProgramEligibilityRules>>(
      `${ELIGIBILITY_BASE_URL}/programs/${programCode}`
    );
    return response.data.data;
  },

  createProgram: async (
    program: Omit<ProgramEligibilityRules, 'programCode'>
  ): Promise<ProgramEligibilityRules> => {
    const response = await apiClient.post<ApiResponse<ProgramEligibilityRules>>(
      `${ELIGIBILITY_BASE_URL}/programs`,
      program
    );
    return response.data.data;
  },

  updateProgram: async (
    programCode: string,
    updates: Partial<ProgramEligibilityRules>
  ): Promise<ProgramEligibilityRules> => {
    const response = await apiClient.put<ApiResponse<ProgramEligibilityRules>>(
      `${ELIGIBILITY_BASE_URL}/programs/${programCode}`,
      updates
    );
    return response.data.data;
  },

  // Bulk Operations
  bulkAssessEligibility: async (
    requests: EligibilityRequest[]
  ): Promise<EligibilityResponse[]> => {
    const response = await apiClient.post<ApiResponse<EligibilityResponse[]>>(
      `${ELIGIBILITY_BASE_URL}/bulk/assess`,
      { requests }
    );
    return response.data.data;
  },

  // Search and Filtering
  searchEligibleHouseholds: async (
    programCode: string,
    filters?: any
  ): Promise<PaginatedResponse<EligibilityResponse>> => {
    const params = new URLSearchParams();
    params.append('programCode', programCode);

    if (filters?.region) params.append('region', filters.region);
    if (filters?.province) params.append('province', filters.province);
    if (filters?.municipality)
      params.append('municipality', filters.municipality);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.minScore)
      params.append('minScore', filters.minScore.toString());
    if (filters?.maxScore)
      params.append('maxScore', filters.maxScore.toString());

    const response = await apiClient.get<
      PaginatedResponse<EligibilityResponse>
    >(`${ELIGIBILITY_BASE_URL}/search/eligible?${params.toString()}`);
    return response.data;
  },

  // Assessment History
  getAssessmentHistory: async (
    psn: string
  ): Promise<EligibilityAssessmentHistory[]> => {
    const response = await apiClient.get<
      ApiResponse<EligibilityAssessmentHistory[]>
    >(`${ELIGIBILITY_BASE_URL}/history/${psn}`);
    return response.data.data;
  },

  // Statistics and Reports
  getEligibilityStatistics: async (filters?: any): Promise<any> => {
    const params = new URLSearchParams();

    if (filters?.programCode) params.append('programCode', filters.programCode);
    if (filters?.region) params.append('region', filters.region);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<ApiResponse<any>>(
      `${ELIGIBILITY_BASE_URL}/statistics?${params.toString()}`
    );
    return response.data.data;
  },

  getProgramStatistics: async (
    programCode: string,
    filters?: any
  ): Promise<any> => {
    const params = new URLSearchParams();

    if (filters?.region) params.append('region', filters.region);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<ApiResponse<any>>(
      `${ELIGIBILITY_BASE_URL}/programs/${programCode}/statistics?${params.toString()}`
    );
    return response.data.data;
  },

  // Export functionality
  exportEligibilityData: async (
    filters: any,
    format: 'CSV' | 'EXCEL' | 'PDF'
  ): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);

    if (filters.programCode) params.append('programCode', filters.programCode);
    if (filters.status) params.append('status', filters.status);
    if (filters.region) params.append('region', filters.region);
    if (filters.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange?.end) params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get(
      `${ELIGIBILITY_BASE_URL}/export?${params.toString()}`,
      { responseType: 'blob' }
    );
    return response.data;
  },
};
