// API Types for DSR Frontend
// Common API response structures and utility types

// Generic API Response Wrapper
export interface ApiResponse<T = any> {
  data: T;
  message: string;
  success: boolean;
  timestamp: string;
  status: number;
}

// Paginated Response
export interface PaginatedResponse<T = any> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

// API Error Response
export interface ApiErrorResponse {
  message: string;
  status: number;
  error: string;
  timestamp: string;
  path: string;
  errors?: Record<string, string[]>;
  details?: string;
}

// HTTP Methods
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

// API Request Configuration
export interface ApiRequestConfig {
  method: HttpMethod;
  url: string;
  data?: any;
  params?: Record<string, any>;
  headers?: Record<string, string>;
  timeout?: number;
  withCredentials?: boolean;
}

// API Client Response
export interface ApiClientResponse<T = any> {
  data: T;
  status: number;
  statusText: string;
  headers: Record<string, string>;
}

// Loading States
export interface LoadingState {
  isLoading: boolean;
  error: string | null;
}

// Form Field Error
export interface FieldError {
  field: string;
  message: string;
}

// Validation Error Response
export interface ValidationErrorResponse extends ApiErrorResponse {
  fieldErrors: FieldError[];
}

// Health Check Response
export interface HealthCheckResponse {
  service: string;
  version: string;
  status: 'UP' | 'DOWN';
  timestamp: string;
  details?: Record<string, any>;
}

// File Upload Response
export interface FileUploadResponse {
  fileName: string;
  fileUrl: string;
  fileSize: number;
  contentType: string;
  uploadedAt: string;
}

// Search/Filter Parameters
export interface SearchParams {
  query?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

// Common Entity Fields
export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

// Audit Information
export interface AuditInfo {
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  version: number;
}

// API Endpoints Configuration
export interface ApiEndpoints {
  auth: {
    login: string;
    register: string;
    logout: string;
    refresh: string;
    profile: string;
    changePassword: string;
    forgotPassword: string;
    resetPassword: string;
    verifyEmail: string;
    resendVerification: string;
    securityStatus: string;
  };
  health: string;
  swagger: string;
}

// Request/Response Interceptor Types
export interface RequestInterceptor {
  onFulfilled?: (config: any) => any;
  onRejected?: (error: any) => any;
}

export interface ResponseInterceptor {
  onFulfilled?: (response: any) => any;
  onRejected?: (error: any) => any;
}

// API Client Configuration
export interface ApiClientConfig {
  baseURL: string;
  timeout: number;
  headers: Record<string, string>;
  withCredentials: boolean;
  requestInterceptors: RequestInterceptor[];
  responseInterceptors: ResponseInterceptor[];
}

// Environment Configuration
export interface EnvironmentConfig {
  apiBaseUrl: string;
  apiVersion: string;
  appName: string;
  appVersion: string;
  environment: 'development' | 'staging' | 'production';
  enableAnalytics: boolean;
  enableDebug: boolean;
  enableMockData: boolean;
  secureCookies: boolean;
  sameSiteCookies: 'strict' | 'lax' | 'none';
  defaultTheme: 'light' | 'dark';
  enableDarkMode: boolean;
}

// Toast/Notification Types
export interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  persistent?: boolean;
  actions?: ToastAction[];
}

export interface ToastAction {
  label: string;
  action: () => void;
  style?: 'primary' | 'secondary' | 'danger';
}

// Modal/Dialog Types
export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
  closeOnOverlayClick?: boolean;
  closeOnEscape?: boolean;
  showCloseButton?: boolean;
}

// Table/List Types
export interface TableColumn<T = any> {
  key: keyof T;
  label: string;
  sortable?: boolean;
  width?: string;
  align?: 'left' | 'center' | 'right';
  render?: (value: any, row: T) => React.ReactNode;
}

export interface TableProps<T = any> {
  data: T[];
  columns: TableColumn<T>[];
  loading?: boolean;
  pagination?: {
    current: number;
    total: number;
    pageSize: number;
    onChange: (page: number, pageSize: number) => void;
  };
  selection?: {
    selectedRows: string[];
    onSelectionChange: (selectedRows: string[]) => void;
  };
  actions?: {
    label: string;
    action: (row: T) => void;
    icon?: React.ReactNode;
    variant?: 'primary' | 'secondary' | 'danger';
  }[];
}
