'use client';

// Button Component
// Reusable button component with multiple variants, sizes, and states

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { cn } from '@/utils';

// Button variants using class-variance-authority
const buttonVariants = cva(
  // Base styles with enhanced accessibility and animations
  'inline-flex items-center justify-center rounded-md text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 active:scale-95',
  {
    variants: {
      variant: {
        primary:
          'bg-primary-600 text-white hover:bg-primary-700 focus-visible:ring-primary-500 shadow-sm hover:shadow-md',
        secondary:
          'bg-secondary-600 text-white hover:bg-secondary-700 focus-visible:ring-secondary-500 shadow-sm hover:shadow-md',
        accent:
          'bg-accent-600 text-white hover:bg-accent-700 focus-visible:ring-accent-500 shadow-sm hover:shadow-md',
        outline:
          'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 hover:border-gray-400 focus-visible:ring-primary-500 shadow-sm hover:shadow-md',
        ghost: 'text-gray-700 hover:bg-gray-100 focus-visible:ring-primary-500 hover:shadow-sm',
        link: 'text-primary-600 underline-offset-4 hover:underline focus-visible:ring-primary-500 p-0 h-auto',
        destructive:
          'bg-error-600 text-white hover:bg-error-700 focus-visible:ring-error-500 shadow-sm hover:shadow-md',
        success:
          'bg-success-600 text-white hover:bg-success-700 focus-visible:ring-success-500 shadow-sm hover:shadow-md',
        warning:
          'bg-warning-600 text-white hover:bg-warning-700 focus-visible:ring-warning-500 shadow-sm hover:shadow-md',
        // DSR-specific variants
        eligible:
          'bg-dsr-eligible text-white hover:bg-green-600 focus-visible:ring-green-500 shadow-sm hover:shadow-md',
        pending:
          'bg-dsr-pending text-white hover:bg-yellow-600 focus-visible:ring-yellow-500 shadow-sm hover:shadow-md',
        processing:
          'bg-dsr-processing text-white hover:bg-blue-600 focus-visible:ring-blue-500 shadow-sm hover:shadow-md animate-pulse-slow',
        completed:
          'bg-dsr-completed text-white hover:bg-green-700 focus-visible:ring-green-500 shadow-sm hover:shadow-md',
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
      leftIcon,
      rightIcon,
      children,
      disabled,
      ...props
    },
    ref
  ) => {
    const isDisabled = disabled || loading;

    const buttonContent = (
      <>
        {loading && (
          <LoadingSpinner
            size={
              size === 'xs' || size === 'sm'
                ? 'sm'
                : size === 'lg' || size === 'xl'
                  ? 'lg'
                  : 'md'
            }
          />
        )}
        {!loading && leftIcon && (
          <span className='mr-2 flex-shrink-0'>{leftIcon}</span>
        )}
        {children && <span className={cn(loading && 'ml-2')}>{children}</span>}
        {!loading && rightIcon && (
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
