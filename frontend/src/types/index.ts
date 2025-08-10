// Central export for all TypeScript types and interfaces
// DSR Frontend Type Definitions

// Authentication Types
export * from './auth';

// API Types
export * from './api';

// Registration Types
export * from './registration';

// Case Management Types
export * from './cases';

// Payment Management Types
export * from './payments';

// Household Management Types
export * from './households';

// Re-export commonly used types for convenience
export type {
  User,
  AuthState,
  AuthActions,
  AuthStore,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
} from './auth';

export type {
  ApiResponse,
  ApiErrorResponse,
  LoadingState,
  EnvironmentConfig,
} from './api';

// Re-export enums as values
export { UserRole, UserStatus } from './auth';

export type {
  ApiResponse as GenericApiResponse,
  PaginatedResponse,
  ApiErrorResponse as GenericApiErrorResponse,
  HttpMethod,
  ApiRequestConfig,
  ApiClientResponse,
  BaseEntity,
  SearchParams,
} from './api';

// Registration Types
export type {
  PersonalInfo,
  Address,
  HouseholdMember,
  SocioEconomicInfo,
  DocumentUpload,
  ConsentData,
  HouseholdRegistrationData,
  RegistrationStep,
  RegistrationStatus,
} from './registration';

// Case Management Types
export type {
  Case,
  CaseNote,
  CaseAttachment,
  CaseFilters,
  CaseStatus,
  CaseType,
  CasePriority,
  CaseCategory,
  CaseResolution,
  CreateCaseRequest,
  UpdateCaseRequest,
} from './cases';

// Payment Management Types
export type {
  Payment,
  PaymentBatch,
  PaymentFilters,
  PaymentStatus,
  PaymentMethod,
  BankAccount,
  DigitalWallet,
  CashPickup,
  CreatePaymentBatchRequest,
  PaymentSummary,
} from './payments';

// Household Management Types
export type {
  HouseholdData,
  HouseholdFilters,
  HouseholdSearchParams,
  CreateHouseholdRequest,
  UpdateHouseholdRequest,
  HouseholdSummary,
  HouseholdStatistics,
  HouseholdValidationResult,
  HouseholdMemberUpdate,
  HouseholdHistoryEntry,
  HouseholdExportOptions,
  HouseholdImportResult,
} from './households';

// Analytics Types
export interface ReportTemplate {
  id: string;
  name: string;
  description: string;
  category: string;
  reportType: 'TABULAR' | 'CHART' | 'DASHBOARD' | 'DOCUMENT';
  parameters: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface ReportExecution {
  id: string;
  templateId: string;
  templateName: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  parameters: Record<string, any>;
  result?: any;
  error?: string;
  startTime: string;
  endTime?: string;
  fileUrl?: string;
  createdAt?: string;
  completedAt?: string;
}

export interface DashboardConfig {
  id: string;
  name: string;
  description: string;
  userRole: string;
  layout: any;
  widgets: any[];
  permissions?: string[];
}

export interface KPIValue {
  kpiCode: string;
  value: number;
  target?: number;
  status: 'GOOD' | 'WARNING' | 'CRITICAL';
  trend: 'UP' | 'DOWN' | 'STABLE';
  changePercent: number;
  calculationDate?: string;
  lastUpdated?: string;
}
