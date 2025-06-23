'use client';

// Loading Component
// Various loading indicators and spinners with different styles and sizes

import React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/utils';

// Loading variants
const loadingVariants = cva(
  // Base styles
  'animate-spin',
  {
    variants: {
      variant: {
        spinner: 'rounded-full border-2 border-gray-300 border-t-primary-600',
        dots: 'flex space-x-1',
        pulse: 'bg-gray-300 rounded animate-pulse',
        bars: 'flex space-x-1',
        ring: 'rounded-full border-4 border-gray-200 border-t-primary-600',
      },
      size: {
        xs: 'h-3 w-3',
        sm: 'h-4 w-4',
        md: 'h-6 w-6',
        lg: 'h-8 w-8',
        xl: 'h-12 w-12',
        '2xl': 'h-16 w-16',
      },
      color: {
        primary: 'border-t-primary-600',
        secondary: 'border-t-secondary-600',
        accent: 'border-t-accent-600',
        success: 'border-t-success-600',
        warning: 'border-t-warning-600',
        error: 'border-t-error-600',
        white: 'border-t-white',
        gray: 'border-t-gray-600',
      },
    },
    defaultVariants: {
      variant: 'spinner',
      size: 'md',
      color: 'primary',
    },
  }
);

// Loading props interface
export interface LoadingProps extends VariantProps<typeof loadingVariants> {
  className?: string;
  text?: string;
  overlay?: boolean;
  fullScreen?: boolean;
}

// Spinner component
const Spinner: React.FC<LoadingProps> = ({ 
  variant = 'spinner', 
  size, 
  color, 
  className 
}) => {
  if (variant === 'dots') {
    return (
      <div className={cn('flex space-x-1', className)}>
        {[0, 1, 2].map((i) => (
          <div
            key={i}
            className={cn(
              'rounded-full bg-primary-600 animate-bounce',
              size === 'xs' ? 'h-1 w-1' :
              size === 'sm' ? 'h-1.5 w-1.5' :
              size === 'md' ? 'h-2 w-2' :
              size === 'lg' ? 'h-3 w-3' :
              size === 'xl' ? 'h-4 w-4' :
              'h-5 w-5'
            )}
            style={{
              animationDelay: `${i * 0.1}s`,
            }}
          />
        ))}
      </div>
    );
  }

  if (variant === 'bars') {
    return (
      <div className={cn('flex space-x-1', className)}>
        {[0, 1, 2, 3].map((i) => (
          <div
            key={i}
            className={cn(
              'bg-primary-600 animate-pulse',
              size === 'xs' ? 'h-3 w-0.5' :
              size === 'sm' ? 'h-4 w-0.5' :
              size === 'md' ? 'h-6 w-1' :
              size === 'lg' ? 'h-8 w-1' :
              size === 'xl' ? 'h-12 w-1.5' :
              'h-16 w-2'
            )}
            style={{
              animationDelay: `${i * 0.1}s`,
            }}
          />
        ))}
      </div>
    );
  }

  if (variant === 'pulse') {
    return (
      <div
        className={cn(
          loadingVariants({ variant: 'pulse', size }),
          className
        )}
      />
    );
  }

  return (
    <div
      className={cn(
        loadingVariants({ variant, size, color }),
        className
      )}
    />
  );
};

// Loading component with text and overlay options
const Loading: React.FC<LoadingProps> = ({
  variant,
  size,
  color,
  className,
  text,
  overlay = false,
  fullScreen = false,
}) => {
  const content = (
    <div className={cn(
      'flex flex-col items-center justify-center space-y-2',
      fullScreen && 'min-h-screen',
      className
    )}>
      <Spinner variant={variant} size={size} color={color} />
      {text && (
        <p className={cn(
          'text-sm text-gray-600',
          size === 'xs' && 'text-xs',
          size === 'sm' && 'text-xs',
          size === 'lg' && 'text-base',
          size === 'xl' && 'text-lg',
          size === '2xl' && 'text-xl'
        )}>
          {text}
        </p>
      )}
    </div>
  );

  if (overlay) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-white bg-opacity-75 backdrop-blur-sm">
        {content}
      </div>
    );
  }

  return content;
};

// Skeleton loading component
export const Skeleton: React.FC<{
  className?: string;
  lines?: number;
  avatar?: boolean;
  button?: boolean;
}> = ({ className, lines = 3, avatar = false, button = false }) => {
  return (
    <div className={cn('animate-pulse', className)}>
      {avatar && (
        <div className="flex items-center space-x-4 mb-4">
          <div className="rounded-full bg-gray-300 h-10 w-10" />
          <div className="flex-1 space-y-2">
            <div className="h-4 bg-gray-300 rounded w-3/4" />
            <div className="h-3 bg-gray-300 rounded w-1/2" />
          </div>
        </div>
      )}
      
      <div className="space-y-3">
        {Array.from({ length: lines }).map((_, i) => (
          <div
            key={i}
            className={cn(
              'h-4 bg-gray-300 rounded',
              i === lines - 1 ? 'w-2/3' : 'w-full'
            )}
          />
        ))}
      </div>

      {button && (
        <div className="mt-4 h-10 bg-gray-300 rounded w-24" />
      )}
    </div>
  );
};

// Card skeleton
export const CardSkeleton: React.FC<{ className?: string }> = ({ className }) => (
  <div className={cn('p-6 border border-gray-200 rounded-lg', className)}>
    <Skeleton lines={4} avatar />
  </div>
);

// Table skeleton
export const TableSkeleton: React.FC<{ 
  rows?: number; 
  columns?: number; 
  className?: string; 
}> = ({ rows = 5, columns = 4, className }) => (
  <div className={cn('space-y-3', className)}>
    {/* Header */}
    <div className="grid gap-4" style={{ gridTemplateColumns: `repeat(${columns}, 1fr)` }}>
      {Array.from({ length: columns }).map((_, i) => (
        <div key={i} className="h-4 bg-gray-300 rounded" />
      ))}
    </div>
    
    {/* Rows */}
    {Array.from({ length: rows }).map((_, rowIndex) => (
      <div key={rowIndex} className="grid gap-4" style={{ gridTemplateColumns: `repeat(${columns}, 1fr)` }}>
        {Array.from({ length: columns }).map((_, colIndex) => (
          <div key={colIndex} className="h-4 bg-gray-200 rounded" />
        ))}
      </div>
    ))}
  </div>
);

// Page loading component
export const PageLoading: React.FC<{ text?: string }> = ({ text = 'Loading...' }) => (
  <div className="flex items-center justify-center min-h-screen">
    <Loading size="xl" text={text} />
  </div>
);

// Button loading component
export const ButtonLoading: React.FC<{ size?: 'sm' | 'md' | 'lg' }> = ({ size = 'md' }) => (
  <Spinner 
    variant="spinner" 
    size={size === 'sm' ? 'xs' : size === 'lg' ? 'md' : 'sm'} 
    color="white" 
  />
);

export { Loading, Spinner, loadingVariants };
export default Loading;
