'use client';

// Form Textarea Component
// Textarea component integrated with React Hook Form

import React from 'react';
import { useFormContext, Controller } from 'react-hook-form';

import { cn } from '@/utils';

// Form Textarea props interface
export interface FormTextareaProps
  extends Omit<React.TextareaHTMLAttributes<HTMLTextAreaElement>, 'name'> {
  name?: string;
  label?: string;
  description?: string;
  required?: boolean;
  error?: string;
  resize?: 'none' | 'vertical' | 'horizontal' | 'both';
  showCharCount?: boolean;
  maxLength?: number;
}

// Form Textarea component
export const FormTextarea: React.FC<FormTextareaProps> = ({
  name,
  label,
  description,
  required = false,
  resize = 'vertical',
  showCharCount = false,
  maxLength,
  className,
  ...props
}) => {
  const {
    control,
    formState: { errors },
    watch,
  } = useFormContext();

  const error = errors[name]?.message as string | undefined;
  const value = watch(name) || '';
  const charCount = value.length;

  const resizeClasses = {
    none: 'resize-none',
    vertical: 'resize-y',
    horizontal: 'resize-x',
    both: 'resize',
  };

  return (
    <Controller
      name={name}
      control={control}
      render={({ field }) => (
        <div className='space-y-2'>
          {label && (
            <label
              htmlFor={field.name}
              className='block text-sm font-medium text-gray-700'
            >
              {label}
              {required && <span className='ml-1 text-error-500'>*</span>}
            </label>
          )}

          <textarea
            {...field}
            {...props}
            id={field.name}
            maxLength={maxLength}
            className={cn(
              'flex min-h-[80px] w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-white placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
              resizeClasses[resize],
              error && 'border-error-500 focus-visible:ring-error-500',
              className
            )}
            aria-invalid={!!error}
            aria-describedby={
              error
                ? `${field.name}-error`
                : description
                  ? `${field.name}-description`
                  : undefined
            }
          />

          {(showCharCount || maxLength) && (
            <div className='flex justify-between items-center text-xs text-gray-500'>
              <div />
              {(showCharCount || maxLength) && (
                <span
                  className={cn(
                    maxLength &&
                      charCount > maxLength * 0.9 &&
                      'text-warning-600',
                    maxLength && charCount >= maxLength && 'text-error-600'
                  )}
                >
                  {charCount}
                  {maxLength && `/${maxLength}`}
                </span>
              )}
            </div>
          )}

          {error && (
            <p
              id={`${field.name}-error`}
              className='text-sm text-error-600'
              role='alert'
            >
              {error}
            </p>
          )}

          {description && !error && (
            <p
              id={`${field.name}-description`}
              className='text-sm text-gray-500'
            >
              {description}
            </p>
          )}
        </div>
      )}
    />
  );
};

export default FormTextarea;
