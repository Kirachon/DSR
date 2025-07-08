'use client';

// Alert Component
// Flexible alert component for displaying messages with different variants and actions

import { cva, type VariantProps } from 'class-variance-authority';
import React, { useState } from 'react';

import { cn } from '@/utils';

// Alert variants
const alertVariants = cva(
  // Base styles
  'relative w-full rounded-lg border p-4 [&>svg~*]:pl-7 [&>svg+div]:translate-y-[-3px] [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4 [&>svg]:text-foreground',
  {
    variants: {
      variant: {
        default: 'bg-white border-gray-200 text-gray-900',
        success:
          'bg-success-50 border-success-200 text-success-800 [&>svg]:text-success-600',
        warning:
          'bg-warning-50 border-warning-200 text-warning-800 [&>svg]:text-warning-600',
        error:
          'bg-error-50 border-error-200 text-error-800 [&>svg]:text-error-600',
        info: 'bg-primary-50 border-primary-200 text-primary-800 [&>svg]:text-primary-600',
      },
      size: {
        sm: 'p-3 text-sm',
        md: 'p-4 text-sm',
        lg: 'p-6 text-base',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

// Alert props interface
export interface AlertProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof alertVariants> {
  title?: string;
  description?: string;
  icon?: React.ReactNode;
  dismissible?: boolean;
  onDismiss?: () => void;
  actions?: React.ReactNode;
}

// Default icons for each variant
const defaultIcons = {
  default: (
    <svg className='h-4 w-4' fill='currentColor' viewBox='0 0 20 20'>
      <path
        fillRule='evenodd'
        d='M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z'
        clipRule='evenodd'
      />
    </svg>
  ),
  success: (
    <svg className='h-4 w-4' fill='currentColor' viewBox='0 0 20 20'>
      <path
        fillRule='evenodd'
        d='M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z'
        clipRule='evenodd'
      />
    </svg>
  ),
  warning: (
    <svg className='h-4 w-4' fill='currentColor' viewBox='0 0 20 20'>
      <path
        fillRule='evenodd'
        d='M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z'
        clipRule='evenodd'
      />
    </svg>
  ),
  error: (
    <svg className='h-4 w-4' fill='currentColor' viewBox='0 0 20 20'>
      <path
        fillRule='evenodd'
        d='M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z'
        clipRule='evenodd'
      />
    </svg>
  ),
  info: (
    <svg className='h-4 w-4' fill='currentColor' viewBox='0 0 20 20'>
      <path
        fillRule='evenodd'
        d='M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z'
        clipRule='evenodd'
      />
    </svg>
  ),
};

// Close icon
const CloseIcon: React.FC = () => (
  <svg
    className='h-4 w-4'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M6 18L18 6M6 6l12 12'
    />
  </svg>
);

// Alert component
const Alert: React.FC<AlertProps> = ({
  className,
  variant = 'default',
  size,
  title,
  description,
  icon,
  dismissible = false,
  onDismiss,
  actions,
  children,
  ...props
}) => {
  const [isVisible, setIsVisible] = useState(true);

  const handleDismiss = () => {
    setIsVisible(false);
    onDismiss?.();
  };

  if (!isVisible) return null;

  const displayIcon =
    icon || (variant ? defaultIcons[variant] : defaultIcons.default);

  return (
    <div
      className={cn(alertVariants({ variant, size }), className)}
      role='alert'
      {...props}
    >
      {displayIcon}

      <div className='flex-1'>
        {title && (
          <h5 className='mb-1 font-medium leading-none tracking-tight'>
            {title}
          </h5>
        )}

        {description && <div className='text-sm opacity-90'>{description}</div>}

        {children && !description && (
          <div className='text-sm opacity-90'>{children}</div>
        )}

        {actions && <div className='mt-3 flex space-x-2'>{actions}</div>}
      </div>

      {dismissible && (
        <button
          type='button'
          className='absolute right-2 top-2 rounded-md p-1 text-current opacity-70 hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-current focus:ring-offset-2'
          onClick={handleDismiss}
          aria-label='Dismiss alert'
        >
          <CloseIcon />
        </button>
      )}
    </div>
  );
};

// Alert Title component
export const AlertTitle: React.FC<React.HTMLAttributes<HTMLHeadingElement>> = ({
  className,
  ...props
}) => (
  <h5
    className={cn('mb-1 font-medium leading-none tracking-tight', className)}
    {...props}
  />
);

// Alert Description component
export const AlertDescription: React.FC<
  React.HTMLAttributes<HTMLParagraphElement>
> = ({ className, ...props }) => (
  <div className={cn('text-sm opacity-90', className)} {...props} />
);

// Toast-style alert (for notifications)
export const Toast: React.FC<
  AlertProps & {
    position?:
      | 'top-right'
      | 'top-left'
      | 'bottom-right'
      | 'bottom-left'
      | 'top-center'
      | 'bottom-center';
    autoClose?: boolean;
    duration?: number;
  }
> = ({
  position = 'top-right',
  autoClose = true,
  duration = 5000,
  onDismiss,
  ...props
}) => {
  const [isVisible, setIsVisible] = useState(true);

  React.useEffect(() => {
    if (autoClose && duration > 0) {
      const timer = setTimeout(() => {
        setIsVisible(false);
        onDismiss?.();
      }, duration);

      return () => clearTimeout(timer);
    }
    return undefined;
  }, [autoClose, duration, onDismiss]);

  if (!isVisible) return null;

  const positionClasses = {
    'top-right': 'fixed top-4 right-4 z-50',
    'top-left': 'fixed top-4 left-4 z-50',
    'bottom-right': 'fixed bottom-4 right-4 z-50',
    'bottom-left': 'fixed bottom-4 left-4 z-50',
    'top-center': 'fixed top-4 left-1/2 transform -translate-x-1/2 z-50',
    'bottom-center': 'fixed bottom-4 left-1/2 transform -translate-x-1/2 z-50',
  };

  return (
    <div className={positionClasses[position]}>
      <Alert
        {...props}
        dismissible={true}
        onDismiss={() => {
          setIsVisible(false);
          onDismiss?.();
        }}
        className={cn('shadow-lg animate-slide-down', props.className)}
      />
    </div>
  );
};

// Banner alert (full width)
export const Banner: React.FC<AlertProps> = ({ className, ...props }) => (
  <Alert {...props} className={cn('rounded-none border-x-0', className)} />
);

// Inline alert (compact)
export const InlineAlert: React.FC<AlertProps> = ({
  className,
  size = 'sm',
  ...props
}) => (
  <Alert
    {...props}
    size={size}
    className={cn(
      'border-l-4 border-t-0 border-r-0 border-b-0 rounded-none pl-3',
      className
    )}
  />
);

export { Alert, alertVariants };
export default Alert;
