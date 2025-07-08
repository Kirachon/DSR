import { Platform } from 'react-native';

// API Configuration
export const API_BASE_URL = __DEV__ 
  ? Platform.OS === 'ios' 
    ? 'http://localhost:8081/api/v1'
    : 'http://10.0.2.2:8081/api/v1'
  : 'https://api.dsr.gov.ph/v1';

export const API_TIMEOUT = 30000; // 30 seconds

// App Configuration
export const APP_NAME = 'DSR Mobile';
export const APP_VERSION = '1.0.0';
export const APP_BUILD = '1';

// Storage Keys
export const STORAGE_KEYS = {
  AUTH_TOKENS: 'dsr_auth_tokens',
  USER_DATA: 'dsr_user',
  BIOMETRIC_ENABLED: 'dsr_biometric_enabled',
  NOTIFICATION_PREFERENCES: 'notification_preferences',
  OFFLINE_DATA: 'dsr_offline_data',
  FCM_TOKEN: 'fcm_token',
  ONBOARDING_COMPLETED: 'onboarding_completed',
  THEME_PREFERENCE: 'theme_preference',
  LANGUAGE_PREFERENCE: 'language_preference',
} as const;

// Database Configuration
export const DATABASE_CONFIG = {
  NAME: 'DSROffline.db',
  VERSION: '1.0',
  DISPLAY_NAME: 'DSR Offline Database',
  SIZE: 5 * 1024 * 1024, // 5MB
} as const;

// Notification Configuration
export const NOTIFICATION_CONFIG = {
  CHANNELS: {
    REGISTRATION: 'dsr-registration',
    PAYMENT: 'dsr-payment',
    CASE: 'dsr-case',
    SYSTEM: 'dsr-system',
    REMINDER: 'dsr-reminder',
  },
  PRIORITIES: {
    HIGH: 'high',
    NORMAL: 'normal',
    LOW: 'low',
  },
} as const;

// Biometric Configuration
export const BIOMETRIC_CONFIG = {
  PROMPT_TITLE: 'Biometric Authentication',
  PROMPT_SUBTITLE: 'Use your biometric to authenticate',
  PROMPT_DESCRIPTION: 'Place your finger on the sensor or look at the camera',
  FALLBACK_TITLE: 'Use Password',
  NEGATIVE_BUTTON: 'Cancel',
} as const;

// Offline Configuration
export const OFFLINE_CONFIG = {
  MAX_RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 5000, // 5 seconds
  SYNC_INTERVAL: 300000, // 5 minutes
  MAX_OFFLINE_ITEMS: 1000,
  CLEANUP_INTERVAL: 86400000, // 24 hours
} as const;

// QR Code Configuration
export const QR_CONFIG = {
  SCAN_TIMEOUT: 30000, // 30 seconds
  VIBRATION_DURATION: 100, // milliseconds
  SUPPORTED_TYPES: ['psn', 'household', 'case', 'verification'],
  PSN_PATTERN: /^\d{4}-\d{4}-\d{4}$/,
  HOUSEHOLD_PATTERN: /^HH-[A-Z0-9]{8,}$/,
  CASE_PATTERN: /^CASE-[A-Z0-9]{10,}$/,
} as const;

// Form Validation
export const VALIDATION_RULES = {
  USERNAME: {
    MIN_LENGTH: 3,
    MAX_LENGTH: 50,
    PATTERN: /^[a-zA-Z0-9_.-]+$/,
  },
  PASSWORD: {
    MIN_LENGTH: 6,
    MAX_LENGTH: 128,
    REQUIRE_UPPERCASE: true,
    REQUIRE_LOWERCASE: true,
    REQUIRE_NUMBER: true,
    REQUIRE_SPECIAL: false,
  },
  PSN: {
    PATTERN: /^\d{4}-\d{4}-\d{4}$/,
    LENGTH: 14, // Including dashes
  },
  PHONE: {
    PATTERN: /^(\+63|0)[0-9]{10}$/,
    MIN_LENGTH: 11,
    MAX_LENGTH: 13,
  },
  EMAIL: {
    PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    MAX_LENGTH: 254,
  },
} as const;

// UI Configuration
export const UI_CONFIG = {
  ANIMATION_DURATION: 300,
  DEBOUNCE_DELAY: 500,
  PAGINATION_SIZE: 20,
  MAX_IMAGE_SIZE: 5 * 1024 * 1024, // 5MB
  SUPPORTED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/webp'],
  TOAST_DURATION: 3000,
} as const;

// Network Configuration
export const NETWORK_CONFIG = {
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
  TIMEOUT: 30000,
  CACHE_DURATION: 300000, // 5 minutes
} as const;

// Security Configuration
export const SECURITY_CONFIG = {
  TOKEN_REFRESH_THRESHOLD: 300000, // 5 minutes before expiry
  MAX_LOGIN_ATTEMPTS: 5,
  LOCKOUT_DURATION: 900000, // 15 minutes
  SESSION_TIMEOUT: 3600000, // 1 hour
  ENCRYPTION_KEY_SIZE: 256,
} as const;

// Feature Flags
export const FEATURE_FLAGS = {
  BIOMETRIC_LOGIN: true,
  OFFLINE_MODE: true,
  QR_SCANNER: true,
  PUSH_NOTIFICATIONS: true,
  ANALYTICS: __DEV__ ? false : true,
  CRASH_REPORTING: __DEV__ ? false : true,
  PERFORMANCE_MONITORING: __DEV__ ? false : true,
  DEBUG_MODE: __DEV__,
} as const;

// Error Messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network connection failed. Please check your internet connection.',
  AUTHENTICATION_FAILED: 'Authentication failed. Please check your credentials.',
  BIOMETRIC_NOT_AVAILABLE: 'Biometric authentication is not available on this device.',
  BIOMETRIC_NOT_ENROLLED: 'No biometrics are enrolled on this device.',
  CAMERA_PERMISSION_DENIED: 'Camera permission is required to scan QR codes.',
  LOCATION_PERMISSION_DENIED: 'Location permission is required for this feature.',
  STORAGE_FULL: 'Device storage is full. Please free up some space.',
  SYNC_FAILED: 'Failed to sync offline data. Please try again.',
  INVALID_QR_CODE: 'Invalid or unsupported QR code format.',
  SESSION_EXPIRED: 'Your session has expired. Please log in again.',
  SERVER_ERROR: 'Server error occurred. Please try again later.',
  VALIDATION_ERROR: 'Please check your input and try again.',
} as const;

// Success Messages
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Successfully logged in.',
  LOGOUT_SUCCESS: 'Successfully logged out.',
  SYNC_SUCCESS: 'Data synchronized successfully.',
  BIOMETRIC_ENABLED: 'Biometric authentication enabled.',
  BIOMETRIC_DISABLED: 'Biometric authentication disabled.',
  DATA_SAVED: 'Data saved successfully.',
  NOTIFICATION_SENT: 'Notification sent successfully.',
  QR_SCANNED: 'QR code scanned successfully.',
} as const;

// Endpoints
export const ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    PROFILE: '/auth/profile',
  },
  REGISTRATION: {
    HOUSEHOLDS: '/registration/households',
    MEMBERS: '/registration/members',
    DOCUMENTS: '/registration/documents',
  },
  DATA_MANAGEMENT: {
    PHILSYS: '/data-management/philsys',
    VALIDATION: '/data-management/validation',
    DEDUPLICATION: '/data-management/deduplication',
  },
  ELIGIBILITY: {
    ASSESSMENT: '/eligibility/assessment',
    PROGRAMS: '/eligibility/programs',
    CRITERIA: '/eligibility/criteria',
  },
  PAYMENT: {
    DISBURSEMENTS: '/payment/disbursements',
    HISTORY: '/payment/history',
    STATUS: '/payment/status',
  },
  GRIEVANCE: {
    CASES: '/grievance/cases',
    SUBMISSIONS: '/grievance/submissions',
    TRACKING: '/grievance/tracking',
  },
  ANALYTICS: {
    DASHBOARD: '/analytics/dashboard',
    REPORTS: '/analytics/reports',
    KPI: '/analytics/kpi',
  },
  NOTIFICATIONS: {
    PREFERENCES: '/notifications/preferences',
    HISTORY: '/notifications/history',
    TOKEN: '/notifications/token',
  },
} as const;

// Date Formats
export const DATE_FORMATS = {
  DISPLAY: 'MMM DD, YYYY',
  DISPLAY_WITH_TIME: 'MMM DD, YYYY HH:mm',
  API: 'YYYY-MM-DD',
  API_WITH_TIME: 'YYYY-MM-DD HH:mm:ss',
  TIME_ONLY: 'HH:mm',
  RELATIVE: 'relative', // for libraries like moment.js
} as const;

// Supported Languages
export const SUPPORTED_LANGUAGES = [
  { code: 'en', name: 'English', nativeName: 'English' },
  { code: 'fil', name: 'Filipino', nativeName: 'Filipino' },
  { code: 'ceb', name: 'Cebuano', nativeName: 'Cebuano' },
  { code: 'ilo', name: 'Ilocano', nativeName: 'Ilocano' },
] as const;

// Default Values
export const DEFAULTS = {
  LANGUAGE: 'en',
  THEME: 'light',
  PAGINATION_SIZE: 20,
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
} as const;
