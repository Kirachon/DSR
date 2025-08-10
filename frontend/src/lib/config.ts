// Configuration for DSR Frontend Application
import type { EnvironmentConfig, ApiEndpoints } from '@/types';

// Environment Configuration
export const config: EnvironmentConfig = {
  apiBaseUrl: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
  apiVersion: process.env.NEXT_PUBLIC_API_VERSION || 'v1',
  appName: process.env.NEXT_PUBLIC_APP_NAME || 'Dynamic Social Registry',
  appVersion: process.env.NEXT_PUBLIC_APP_VERSION || '1.0.0',
  environment: (process.env.NEXT_PUBLIC_ENVIRONMENT as any) || 'development',
  enableAnalytics: process.env.NEXT_PUBLIC_ENABLE_ANALYTICS === 'true',
  enableDebug: process.env.NEXT_PUBLIC_ENABLE_DEBUG === 'true',
  enableMockData: process.env.NEXT_PUBLIC_ENABLE_MOCK_DATA === 'true',
  secureCookies: process.env.NEXT_PUBLIC_SECURE_COOKIES === 'true',
  sameSiteCookies: (process.env.NEXT_PUBLIC_SAME_SITE_COOKIES as any) || 'lax',
  defaultTheme: (process.env.NEXT_PUBLIC_DEFAULT_THEME as any) || 'light',
  enableDarkMode: process.env.NEXT_PUBLIC_ENABLE_DARK_MODE === 'true',
};

// Service Base URLs Configuration
export const serviceUrls = {
  registration:
    process.env.NEXT_PUBLIC_REGISTRATION_SERVICE_URL || 'http://localhost:8080',
  dataManagement:
    process.env.NEXT_PUBLIC_DATA_MANAGEMENT_SERVICE_URL ||
    'http://localhost:8082',
  eligibility:
    process.env.NEXT_PUBLIC_ELIGIBILITY_SERVICE_URL || 'http://localhost:8083',
  interoperability:
    process.env.NEXT_PUBLIC_INTEROPERABILITY_SERVICE_URL ||
    'http://localhost:8084',
  payment:
    process.env.NEXT_PUBLIC_PAYMENT_SERVICE_URL || 'http://localhost:8085',
  grievance:
    process.env.NEXT_PUBLIC_GRIEVANCE_SERVICE_URL || 'http://localhost:8086',
  analytics:
    process.env.NEXT_PUBLIC_ANALYTICS_SERVICE_URL || 'http://localhost:8087',
};

// API Endpoints Configuration
export const apiEndpoints: ApiEndpoints = {
  auth: {
    login: '/api/v1/auth/login',
    register: '/api/v1/auth/register',
    logout: '/api/v1/auth/logout',
    refresh: '/api/v1/auth/refresh',
    profile: '/api/v1/auth/profile',
    changePassword: '/api/v1/auth/change-password',
    forgotPassword: '/api/v1/auth/forgot-password',
    resetPassword: '/api/v1/auth/reset-password',
    verifyEmail: '/api/v1/auth/verify-email',
    resendVerification: '/api/v1/auth/resend-verification',
    securityStatus: '/api/v1/auth/security-status',
  },
  health: '/api/v1/health',
  swagger: '/swagger-ui/index.html',
};

// JWT Token Configuration
export const tokenConfig = {
  accessTokenKey: process.env.NEXT_PUBLIC_JWT_STORAGE_KEY || 'dsr_access_token',
  refreshTokenKey:
    process.env.NEXT_PUBLIC_REFRESH_TOKEN_KEY || 'dsr_refresh_token',
  tokenPrefix: 'Bearer',
  expirationBuffer: 5 * 60 * 1000, // 5 minutes in milliseconds
};

// Application Constants
export const appConstants = {
  defaultPageSize: 20,
  maxPageSize: 100,
  requestTimeout: 30000, // 30 seconds
  retryAttempts: 3,
  retryDelay: 1000, // 1 second
  debounceDelay: 300, // 300ms
  toastDuration: 5000, // 5 seconds
  sessionWarningTime: 5 * 60 * 1000, // 5 minutes before expiry
  maxFileSize: 10 * 1024 * 1024, // 10MB
  allowedImageTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
  allowedDocumentTypes: [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  ],
};

// Validation Rules
export const validationRules = {
  password: {
    minLength: 8,
    maxLength: 128,
    requireUppercase: true,
    requireLowercase: true,
    requireNumbers: true,
    requireSpecialChars: true,
  },
  email: {
    maxLength: 255,
  },
  name: {
    minLength: 2,
    maxLength: 50,
  },
  phoneNumber: {
    minLength: 10,
    maxLength: 15,
  },
  address: {
    maxLength: 500,
  },
};

// UI Configuration
export const uiConfig = {
  breakpoints: {
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
    '2xl': 1536,
  },
  animations: {
    duration: {
      fast: 150,
      normal: 300,
      slow: 500,
    },
    easing: {
      easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
      easeOut: 'cubic-bezier(0, 0, 0.2, 1)',
      easeInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
    },
  },
  zIndex: {
    dropdown: 1000,
    modal: 1050,
    popover: 1060,
    tooltip: 1070,
    toast: 1080,
  },
};

// Feature Flags
export const featureFlags = {
  enableTwoFactor: false,
  enableSocialLogin: false,
  enableBiometric: false,
  enableOfflineMode: false,
  enablePushNotifications: false,
  enableRealTimeUpdates: false,
  enableAdvancedSearch: true,
  enableBulkOperations: true,
  enableDataExport: true,
  enableAuditLogs: true,
};

// Development Configuration
export const devConfig = {
  enableMockApi: config.environment === 'development' && config.enableMockData,
  enableReduxDevTools: config.environment === 'development',
  enableReactQueryDevTools: config.environment === 'development',
  logLevel: config.enableDebug ? 'debug' : 'error',
  enablePerformanceMonitoring: config.environment !== 'development',
};
