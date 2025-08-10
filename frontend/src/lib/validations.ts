// Validation Schemas using Zod
// Centralized validation rules for forms and data validation

import { z } from 'zod';

import { UserRole } from '@/types';

import { validationRules } from './config';

// Common validation patterns
const emailSchema = z
  .string()
  .min(1, 'Email is required')
  .max(
    validationRules.email.maxLength,
    `Email must be less than ${validationRules.email.maxLength} characters`
  )
  .email('Please enter a valid email address');

const passwordSchema = z
  .string()
  .min(
    validationRules.password.minLength,
    `Password must be at least ${validationRules.password.minLength} characters`
  )
  .max(
    validationRules.password.maxLength,
    `Password must be less than ${validationRules.password.maxLength} characters`
  )
  .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
  .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
  .regex(/[0-9]/, 'Password must contain at least one number')
  .regex(
    /[^A-Za-z0-9]/,
    'Password must contain at least one special character'
  );

const nameSchema = z
  .string()
  .min(
    validationRules.name.minLength,
    `Name must be at least ${validationRules.name.minLength} characters`
  )
  .max(
    validationRules.name.maxLength,
    `Name must be less than ${validationRules.name.maxLength} characters`
  )
  .regex(
    /^[a-zA-Z\s'-]+$/,
    'Name can only contain letters, spaces, hyphens, and apostrophes'
  );

const phoneSchema = z
  .string()
  .min(
    validationRules.phoneNumber.minLength,
    `Phone number must be at least ${validationRules.phoneNumber.minLength} digits`
  )
  .max(
    validationRules.phoneNumber.maxLength,
    `Phone number must be less than ${validationRules.phoneNumber.maxLength} digits`
  )
  .regex(/^[\+]?[1-9][\d]{0,15}$/, 'Please enter a valid phone number');

// Authentication schemas
export const loginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, 'Password is required'),
  rememberMe: z.boolean().optional().default(false),
});

export const registerSchema = z
  .object({
    email: emailSchema,
    password: passwordSchema,
    confirmPassword: z.string().min(1, 'Please confirm your password'),
    firstName: nameSchema,
    lastName: nameSchema,
    phoneNumber: phoneSchema.optional().or(z.literal('')),
    dateOfBirth: z.string().optional().or(z.literal('')),
    address: z
      .string()
      .max(
        validationRules.address.maxLength,
        `Address must be less than ${validationRules.address.maxLength} characters`
      )
      .optional()
      .or(z.literal('')),
    role: z.nativeEnum(UserRole),
    acceptTerms: z
      .boolean()
      .refine(val => val === true, 'You must accept the terms and conditions'),
  })
  .refine(data => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: passwordSchema,
    confirmPassword: z.string().min(1, 'Please confirm your new password'),
  })
  .refine(data => data.newPassword === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

export const forgotPasswordSchema = z.object({
  email: emailSchema,
});

export const resetPasswordSchema = z
  .object({
    newPassword: passwordSchema,
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine(data => data.newPassword === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

export const verifyEmailSchema = z.object({
  token: z.string().min(1, 'Verification token is required'),
});

export const resendVerificationSchema = z.object({
  email: emailSchema,
});

// Profile schemas
export const updateProfileSchema = z.object({
  firstName: nameSchema,
  lastName: nameSchema,
  phoneNumber: phoneSchema.optional().or(z.literal('')),
  dateOfBirth: z.string().optional().or(z.literal('')),
  address: z
    .string()
    .max(
      validationRules.address.maxLength,
      `Address must be less than ${validationRules.address.maxLength} characters`
    )
    .optional()
    .or(z.literal('')),
});

export const updatePreferencesSchema = z.object({
  theme: z.enum(['light', 'dark', 'system']),
  language: z.string().min(1, 'Language is required'),
  timezone: z.string().min(1, 'Timezone is required'),
  emailNotifications: z.boolean(),
  smsNotifications: z.boolean(),
  pushNotifications: z.boolean(),
});

// Contact form schema
export const contactFormSchema = z.object({
  name: nameSchema,
  email: emailSchema,
  subject: z
    .string()
    .min(1, 'Subject is required')
    .max(100, 'Subject must be less than 100 characters'),
  message: z
    .string()
    .min(10, 'Message must be at least 10 characters')
    .max(1000, 'Message must be less than 1000 characters'),
});

// Search schema
export const searchSchema = z.object({
  query: z
    .string()
    .min(1, 'Search query is required')
    .max(100, 'Search query must be less than 100 characters'),
  filters: z
    .object({
      category: z.string().optional(),
      dateFrom: z.string().optional(),
      dateTo: z.string().optional(),
      status: z.string().optional(),
    })
    .optional(),
});

// File upload schema
export const fileUploadSchema = z.object({
  file: z
    .instanceof(File, { message: 'Please select a file' })
    .refine(
      file => file.size <= 10 * 1024 * 1024,
      'File size must be less than 10MB'
    )
    .refine(
      file =>
        [
          'image/jpeg',
          'image/png',
          'image/gif',
          'image/webp',
          'application/pdf',
        ].includes(file.type),
      'File must be an image (JPEG, PNG, GIF, WebP) or PDF'
    ),
  description: z
    .string()
    .max(500, 'Description must be less than 500 characters')
    .optional(),
});

// Pagination schema
export const paginationSchema = z.object({
  page: z.number().min(1, 'Page must be at least 1').default(1),
  size: z
    .number()
    .min(1, 'Page size must be at least 1')
    .max(100, 'Page size must be at most 100')
    .default(20),
  sort: z.string().optional(),
  direction: z.enum(['asc', 'desc']).default('asc'),
});

// Filter schema
export const filterSchema = z.object({
  search: z.string().optional(),
  status: z.string().optional(),
  role: z
    .enum(['CITIZEN', 'LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN'] as const)
    .optional(),
  dateFrom: z.string().optional(),
  dateTo: z.string().optional(),
  category: z.string().optional(),
});

// Type exports for TypeScript
export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;
export type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>;
export type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;
export type VerifyEmailFormData = z.infer<typeof verifyEmailSchema>;
export type ResendVerificationFormData = z.infer<
  typeof resendVerificationSchema
>;
export type UpdateProfileFormData = z.infer<typeof updateProfileSchema>;
export type UpdatePreferencesFormData = z.infer<typeof updatePreferencesSchema>;
export type ContactFormData = z.infer<typeof contactFormSchema>;
export type SearchFormData = z.infer<typeof searchSchema>;
export type FileUploadFormData = z.infer<typeof fileUploadSchema>;
export type PaginationData = z.infer<typeof paginationSchema>;
export type FilterData = z.infer<typeof filterSchema>;

// Validation helper functions
export const validateEmail = (email: string): boolean => {
  try {
    emailSchema.parse(email);
    return true;
  } catch {
    return false;
  }
};

export const validatePassword = (password: string): boolean => {
  try {
    passwordSchema.parse(password);
    return true;
  } catch {
    return false;
  }
};

export const validatePhoneNumber = (phone: string): boolean => {
  try {
    phoneSchema.parse(phone);
    return true;
  } catch {
    return false;
  }
};

export const getPasswordStrength = (
  password: string
): {
  score: number;
  feedback: string[];
} => {
  const feedback: string[] = [];
  let score = 0;

  if (password.length >= 8) score += 1;
  else feedback.push('Use at least 8 characters');

  if (/[A-Z]/.test(password)) score += 1;
  else feedback.push('Include uppercase letters');

  if (/[a-z]/.test(password)) score += 1;
  else feedback.push('Include lowercase letters');

  if (/[0-9]/.test(password)) score += 1;
  else feedback.push('Include numbers');

  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  else feedback.push('Include special characters');

  return { score, feedback };
};
