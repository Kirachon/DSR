'use client';

// Input Component
// Reusable input component with validation states, icons, and accessibility features

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef, useState } from 'react';

import { cn } from '@/utils';

// Input variants
const inputVariants = cva(
  // Base styles - enhanced with modern design patterns
  'flex w-full rounded-lg border bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 transition-all duration-200 shadow-sm hover:shadow-md focus:shadow-md',
  {
    variants: {
      variant: {
        default: 'border-gray-300 focus-visible:ring-primary-500 hover:border-gray-400',
        error: 'border-red-500 focus-visible:ring-red-500 bg-red-50 hover:border-red-600',
        success: 'border-green-500 focus-visible:ring-green-500 bg-green-50 hover:border-green-600',
        warning: 'border-amber-500 focus-visible:ring-amber-500 bg-amber-50 hover:border-amber-600',
      },
      size: {
        sm: 'h-9 px-3 text-sm',
        md: 'h-11 px-4 text-sm',
        lg: 'h-13 px-5 text-base',
        xl: 'h-15 px-6 text-lg',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

// Input props interface
export interface InputProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'>,
    VariantProps<typeof inputVariants> {
  label?: string;
  error?: string;
  success?: string;
  warning?: string;
  helperText?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  showPasswordToggle?: boolean;
  containerClassName?: string;
  labelClassName?: string;
  showCharacterCount?: boolean;
  realTimeValidation?: boolean;
  validationDelay?: number;
  errorClassName?: string;
  helperClassName?: string;
}

// Icon wrapper component
const IconWrapper: React.FC<{
  children: React.ReactNode;
  position: 'left' | 'right';
  size: 'sm' | 'md' | 'lg';
}> = ({ children, position, size }) => {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-5 w-5',
    lg: 'h-6 w-6',
  };

  const positionClasses = {
    left: 'left-3',
    right: 'right-3',
  };

  return (
    <div
      className={cn(
        'absolute top-1/2 -translate-y-1/2 text-gray-400',
        positionClasses[position]
      )}
    >
      <div className={sizeClasses[size]}>{children}</div>
    </div>
  );
};

// Eye icon for password toggle
const EyeIcon: React.FC<{ isVisible: boolean }> = ({ isVisible }) => (
  <svg
    className='h-5 w-5'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
    xmlns='http://www.w3.org/2000/svg'
  >
    {isVisible ? (
      <path
        strokeLinecap='round'
        strokeLinejoin='round'
        strokeWidth={2}
        d='M15 12a3 3 0 11-6 0 3 3 0 016 0z M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z'
      />
    ) : (
      <path
        strokeLinecap='round'
        strokeLinejoin='round'
        strokeWidth={2}
        d='M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21'
      />
    )}
  </svg>
);

// Input component
const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      className,
      containerClassName,
      labelClassName,
      errorClassName,
      helperClassName,
      variant,
      size = 'md',
      type = 'text',
      label,
      error,
      success,
      warning,
      helperText,
      leftIcon,
      rightIcon,
      showPasswordToggle = false,
      id,
      ...props
    },
    ref
  ) => {
    const [showPassword, setShowPassword] = useState(false);
    const [isFocused, setIsFocused] = useState(false);

    // Determine the actual input type
    const inputType =
      showPasswordToggle && type === 'password'
        ? showPassword
          ? 'text'
          : 'password'
        : type;

    // Determine variant based on validation state
    const currentVariant = error
      ? 'error'
      : success
        ? 'success'
        : warning
          ? 'warning'
          : variant;

    // Generate unique ID if not provided
    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;

    // Calculate padding for icons
    const paddingLeft = leftIcon
      ? size === 'sm'
        ? 'pl-8'
        : size === 'lg'
          ? 'pl-12'
          : 'pl-10'
      : '';
    const paddingRight =
      rightIcon || showPasswordToggle
        ? size === 'sm'
          ? 'pr-8'
          : size === 'lg'
            ? 'pr-12'
            : 'pr-10'
        : '';

    return (
      <div className={cn('w-full', containerClassName)}>
        {/* Label */}
        {label && (
          <label
            htmlFor={inputId}
            className={cn(
              'mb-2 block text-sm font-medium text-gray-700',
              labelClassName
            )}
          >
            {label}
            {props.required && <span className='ml-1 text-error-500'>*</span>}
          </label>
        )}

        {/* Input container */}
        <div className='relative'>
          {/* Left icon */}
          {leftIcon && (
            <IconWrapper
              position='left'
              size={(size as 'sm' | 'md' | 'lg') || 'md'}
            >
              {leftIcon}
            </IconWrapper>
          )}

          {/* Input field */}
          <input
            type={inputType}
            className={cn(
              inputVariants({
                variant: currentVariant,
                size: size as 'sm' | 'md' | 'lg',
              }),
              paddingLeft,
              paddingRight,
              isFocused && 'ring-2 ring-offset-2',
              className
            )}
            ref={ref}
            id={inputId}
            onFocus={e => {
              setIsFocused(true);
              props.onFocus?.(e);
            }}
            onBlur={e => {
              setIsFocused(false);
              props.onBlur?.(e);
            }}
            aria-invalid={!!error}
            aria-describedby={
              error
                ? `${inputId}-error`
                : success
                  ? `${inputId}-success`
                  : warning
                    ? `${inputId}-warning`
                    : helperText
                      ? `${inputId}-helper`
                      : undefined
            }
            {...props}
          />

          {/* Right icon or password toggle */}
          {(rightIcon || showPasswordToggle) && (
            <IconWrapper
              position='right'
              size={(size as 'sm' | 'md' | 'lg') || 'md'}
            >
              {showPasswordToggle ? (
                <button
                  type='button'
                  onClick={() => setShowPassword(!showPassword)}
                  className='text-gray-400 hover:text-gray-600 focus:outline-none focus:text-gray-600'
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  tabIndex={-1}
                >
                  <EyeIcon isVisible={showPassword} />
                </button>
              ) : (
                rightIcon
              )}
            </IconWrapper>
          )}
        </div>

        {/* Validation messages and helper text */}
        {error && (
          <p
            id={`${inputId}-error`}
            className={cn('mt-1 text-sm text-error-600', errorClassName)}
            role='alert'
          >
            {error}
          </p>
        )}

        {success && !error && (
          <p
            id={`${inputId}-success`}
            className={cn('mt-1 text-sm text-success-600', helperClassName)}
          >
            {success}
          </p>
        )}

        {warning && !error && !success && (
          <p
            id={`${inputId}-warning`}
            className={cn('mt-1 text-sm text-warning-600', helperClassName)}
          >
            {warning}
          </p>
        )}

        {helperText && !error && !success && !warning && (
          <p
            id={`${inputId}-helper`}
            className={cn('mt-1 text-sm text-gray-500', helperClassName)}
          >
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export { Input, inputVariants };
export default Input;
