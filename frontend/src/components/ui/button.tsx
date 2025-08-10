'use client';

// Button Component
// Reusable button component with multiple variants, sizes, and states

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { cn } from '@/utils';
import { useTheme } from '@/contexts/theme-context';

// Button variants using class-variance-authority
const buttonVariants = cva(
  // Base styles - flat government design with sharp corners and authority
  'inline-flex items-center justify-center rounded-sm text-sm font-semibold transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-1 disabled:pointer-events-none disabled:opacity-50 border',
  {
    variants: {
      variant: {
        primary:
          'bg-primary-500 border-primary-600 text-white hover:bg-primary-600 focus-visible:ring-primary-500 shadow-flat-sm',
        secondary:
          'bg-gray-100 border-gray-300 text-gray-900 hover:bg-gray-200 focus-visible:ring-gray-500 shadow-flat-sm',
        accent:
          'bg-accent-500 border-accent-600 text-white hover:bg-accent-600 focus-visible:ring-accent-500 shadow-flat-sm',
        outline:
          'border-gray-400 bg-white text-gray-800 hover:bg-gray-100 hover:border-gray-500 focus-visible:ring-primary-500 shadow-flat-sm',
        ghost: 'border-transparent text-gray-800 hover:bg-gray-100 focus-visible:ring-primary-500',
        link: 'border-transparent text-primary-500 underline-offset-4 hover:underline focus-visible:ring-primary-500 p-0 h-auto',
        destructive:
          'bg-error-500 border-error-600 text-white hover:bg-error-600 focus-visible:ring-error-500 shadow-flat-sm',
        success:
          'bg-success-500 border-success-600 text-white hover:bg-success-600 focus-visible:ring-success-500 shadow-flat-sm',
        warning:
          'bg-warning-500 border-warning-600 text-white hover:bg-warning-600 focus-visible:ring-warning-500 shadow-flat-sm',
        // DSR-specific variants - flat government design
        eligible:
          'bg-success-500 border-success-600 text-white hover:bg-success-600 focus-visible:ring-success-500 shadow-flat-sm',
        pending:
          'bg-warning-500 border-warning-600 text-white hover:bg-warning-600 focus-visible:ring-warning-500 shadow-flat-sm',
        processing:
          'bg-primary-500 border-primary-600 text-white hover:bg-primary-600 focus-visible:ring-primary-500 shadow-flat-sm',
        completed:
          'bg-success-600 border-success-700 text-white hover:bg-success-700 focus-visible:ring-success-500 shadow-flat-sm',
      },
      size: {
        xs: 'h-8 px-2 text-xs min-w-[2rem]',
        sm: 'h-9 px-3 text-sm min-w-[2.5rem]',
        md: 'h-11 px-4 text-sm min-w-[3rem]', // 44px minimum touch target
        lg: 'h-12 px-6 text-base min-w-[3.5rem]',
        xl: 'h-14 px-8 text-lg min-w-[4rem]',
        icon: 'h-11 w-11 p-0', // 44px minimum touch target
        'icon-sm': 'h-9 w-9 p-0',
        'icon-lg': 'h-12 w-12 p-0',
      },
      fullWidth: {
        true: 'w-full',
        false: 'w-auto',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md',
      fullWidth: false,
    },
  }
);

// Button props interface
export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
  loading?: boolean;
  success?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

// Loading spinner component
const LoadingSpinner: React.FC<{ size?: 'sm' | 'md' | 'lg' }> = ({
  size = 'md',
}) => {
  const sizeClasses = {
    sm: 'h-3 w-3',
    md: 'h-4 w-4',
    lg: 'h-5 w-5',
  };

  return (
    <svg
      className={cn('animate-spin', sizeClasses[size])}
      xmlns='http://www.w3.org/2000/svg'
      fill='none'
      viewBox='0 0 24 24'
    >
      <circle
        className='opacity-25'
        cx='12'
        cy='12'
        r='10'
        stroke='currentColor'
        strokeWidth='4'
      />
      <path
        className='opacity-75'
        fill='currentColor'
        d='M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z'
      />
    </svg>
  );
};

// Success checkmark component
const SuccessCheckmark: React.FC<{ size?: 'sm' | 'md' | 'lg' }> = ({
  size = 'md',
}) => {
  const sizeClasses = {
    sm: 'h-3 w-3',
    md: 'h-4 w-4',
    lg: 'h-5 w-5',
  };

  return (
    <svg
      className={cn('animate-bounce-gentle', sizeClasses[size])}
      xmlns='http://www.w3.org/2000/svg'
      fill='none'
      viewBox='0 0 24 24'
      stroke='currentColor'
    >
      <path
        strokeLinecap='round'
        strokeLinejoin='round'
        strokeWidth={2}
        d='M5 13l4 4L19 7'
      />
    </svg>
  );
};

// Button component
const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant,
      size,
      fullWidth,
      asChild = false,
      loading = false,
      success = false,
      leftIcon,
      rightIcon,
      children,
      disabled,
      ...props
    },
    ref
  ) => {
    const isDisabled = disabled || loading;
    const iconSize = size === 'xs' || size === 'sm' ? 'sm' : size === 'lg' || size === 'xl' ? 'lg' : 'md';

    const buttonContent = (
      <>
        {loading && <LoadingSpinner size={iconSize} />}
        {success && !loading && <SuccessCheckmark size={iconSize} />}
        {!loading && !success && leftIcon && (
          <span className='mr-2 flex-shrink-0'>{leftIcon}</span>
        )}
        {children && (
          <span className={cn((loading || success) && 'ml-2')}>
            {success ? 'Success!' : children}
          </span>
        )}
        {!loading && !success && rightIcon && (
          <span className='ml-2 flex-shrink-0'>{rightIcon}</span>
        )}
      </>
    );

    if (asChild) {
      return (
        <span
          className={cn(
            buttonVariants({ variant, size, fullWidth, className })
          )}
          {...props}
        >
          {buttonContent}
        </span>
      );
    }

    return (
      <button
        className={cn(buttonVariants({ variant, size, fullWidth, className }))}
        ref={ref}
        disabled={isDisabled}
        aria-disabled={isDisabled}
        {...props}
      >
        {buttonContent}
      </button>
    );
  }
);

Button.displayName = 'Button';

export { Button, buttonVariants };
export default Button;
