// Authentication Types for DSR Frontend
// Matches backend DTOs from Registration Service

// User Roles
export enum UserRole {
  CITIZEN = 'CITIZEN',
  LGU_STAFF = 'LGU_STAFF',
  DSWD_STAFF = 'DSWD_STAFF',
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
}

// User Status
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED',
  PENDING_VERIFICATION = 'PENDING_VERIFICATION',
}

// Base User Interface
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  status: UserStatus;
  phoneNumber?: string;
  dateOfBirth?: string;
  address?: string;
  profilePictureUrl?: string;
  emailVerified: boolean;
  phoneVerified: boolean;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

// Authentication Request DTOs
export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  address?: string;
  role: UserRole;
  acceptTerms: boolean;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface VerifyEmailRequest {
  token: string;
}

export interface ResendVerificationRequest {
  email: string;
}

// Authentication Response DTOs
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface MessageResponse {
  message: string;
  success: boolean;
  timestamp: string;
}

export interface UserProfileResponse {
  user: User;
  permissions: string[];
  preferences: UserPreferences;
}

export interface SecurityStatusResponse {
  twoFactorEnabled: boolean;
  lastPasswordChange: string;
  loginAttempts: number;
  accountLocked: boolean;
  lockoutExpiry?: string;
}

// User Preferences
export interface UserPreferences {
  theme: 'light' | 'dark' | 'system';
  language: string;
  timezone: string;
  emailNotifications: boolean;
  smsNotifications: boolean;
  pushNotifications: boolean;
}

// Authentication State
export interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  permissions: string[];
  preferences: UserPreferences | null;
  error: string | null;
}

// Authentication Actions
export interface AuthActions {
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  updateProfile: (updates: Partial<User>) => Promise<void>;
  changePassword: (data: ChangePasswordRequest) => Promise<void>;
  forgotPassword: (data: ForgotPasswordRequest) => Promise<void>;
  resetPassword: (data: ResetPasswordRequest) => Promise<void>;
  verifyEmail: (data: VerifyEmailRequest) => Promise<void>;
  resendVerification: (data: ResendVerificationRequest) => Promise<void>;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

// Combined Auth Store Interface
export interface AuthStore extends AuthState, AuthActions {}

// API Error Response
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path: string;
  errors?: Record<string, string[]>;
}

// Form Validation Schemas (for Zod)
export interface LoginFormData {
  email: string;
  password: string;
  rememberMe: boolean;
}

export interface RegisterFormData {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  dateOfBirth: string;
  address: string;
  role: UserRole;
  acceptTerms: boolean;
}

export interface ChangePasswordFormData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ForgotPasswordFormData {
  email: string;
}

export interface ResetPasswordFormData {
  newPassword: string;
  confirmPassword: string;
}

// Route Protection Types
export interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: UserRole;
  requiredPermissions?: string[];
  fallback?: React.ReactNode;
}

// Authentication Context Type
export interface AuthContextType extends AuthStore {
  isInitialized: boolean;
}
