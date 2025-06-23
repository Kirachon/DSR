// Central export for all TypeScript types and interfaces
// DSR Frontend Type Definitions

// Authentication Types
export * from './auth';

// API Types
export * from './api';

// Re-export commonly used types for convenience
export type {
  User,
  UserRole,
  UserStatus,
  AuthState,
  AuthActions,
  AuthStore,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ApiResponse,
  ApiErrorResponse,
  LoadingState,
  EnvironmentConfig,
} from './auth';

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
