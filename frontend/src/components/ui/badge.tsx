'use client';

// Badge Component
// Versatile badge component for status indicators, labels, and notifications

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { cn } from '@/utils';

// Badge variants using class-variance-authority
export const badgeVariants = cva(
  'inline-flex items-center justify-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 shadow-sm',
  {
    variants: {
      variant: {
        default:
          'border-transparent bg-primary-500 text-white hover:bg-primary-600 hover:shadow-md',
        secondary:
          'border-transparent bg-gray-100 text-gray-800 hover:bg-gray-200 hover:shadow-md',
        destructive:
          'border-transparent bg-red-500 text-white hover:bg-red-600 hover:shadow-md',
        outline: 'text-gray-700 border-gray-300 bg-white hover:bg-gray-50 hover:shadow-md',
        success:
          'border-transparent bg-green-100 text-green-800 hover:bg-green-200 hover:shadow-md',
        warning:
          'border-transparent bg-amber-100 text-amber-800 hover:bg-amber-200 hover:shadow-md',
        error:
          'border-transparent bg-red-100 text-red-800 hover:bg-red-200 hover:shadow-md',
        info:
          'border-transparent bg-blue-100 text-blue-800 hover:bg-blue-200 hover:shadow-md',
        neutral:
          'border-transparent bg-gray-100 text-gray-800 hover:bg-gray-200 hover:shadow-md',
        primary:
          'border-transparent bg-primary-100 text-primary-800 hover:bg-primary-200 hover:shadow-md',
      },
      size: {
        default: 'px-2.5 py-0.5 text-xs',
        sm: 'px-2 py-0.5 text-xs',
        lg: 'px-3 py-1 text-sm',
        xl: 'px-4 py-1.5 text-base',
        '2xl': 'px-5 py-2 text-lg',
      },
      rounded: {
        default: 'rounded-full',
        sm: 'rounded',
        md: 'rounded-md',
        lg: 'rounded-lg',
        none: 'rounded-none',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
      rounded: 'default',
    },
  }
);

// Badge props interface
export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {
  asChild?: boolean;
  dot?: boolean;
  removable?: boolean;
  onRemove?: () => void;
}

// Badge component
const Badge = forwardRef<HTMLDivElement, BadgeProps>(
  (
    {
      className,
      variant,
      size,
      rounded,
      asChild = false,
      dot = false,
      removable = false,
      onRemove,
      children,
      ...props
    },
    ref
  ) => {
    const badgeContent = (
      <>
        {dot && (
          <span
            className={cn(
              'mr-1 h-1.5 w-1.5 rounded-full',
              variant === 'success' && 'bg-green-600',
              variant === 'warning' && 'bg-yellow-600',
              variant === 'error' && 'bg-red-600',
              variant === 'info' && 'bg-blue-600',
              variant === 'neutral' && 'bg-gray-600',
              variant === 'primary' && 'bg-primary-600',
              !variant && 'bg-current'
            )}
          />
        )}
        {children}
        {removable && onRemove && (
          <button
            type="button"
            onClick={onRemove}
            className={cn(
              'ml-1 inline-flex h-3 w-3 items-center justify-center rounded-full hover:bg-black/10 focus:outline-none focus:ring-1 focus:ring-offset-1',
              size === 'sm' && 'h-2.5 w-2.5',
              size === 'lg' && 'h-4 w-4',
              size === 'xl' && 'h-5 w-5'
            )}
            aria-label="Remove badge"
          >
            <svg
              className="h-2 w-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        )}
      </>
    );

    if (asChild) {
      return (
        <span
          className={cn(badgeVariants({ variant, size, rounded, className }))}
          ref={ref}
          {...props}
        >
          {badgeContent}
        </span>
      );
    }

    return (
      <div
        className={cn(badgeVariants({ variant, size, rounded, className }))}
        ref={ref}
        {...props}
      >
        {badgeContent}
      </div>
    );
  }
);

Badge.displayName = 'Badge';

// Specialized badge components
export const StatusBadge: React.FC<{
  status: 'active' | 'inactive' | 'pending' | 'suspended' | 'completed' | 'failed';
  children?: React.ReactNode;
  className?: string;
}> = ({ status, children, className }) => {
  const getVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
      case 'completed':
        return 'success';
      case 'pending':
        return 'warning';
      case 'inactive':
        return 'neutral';
      case 'suspended':
      case 'failed':
        return 'error';
      default:
        return 'neutral';
    }
  };

  return (
    <Badge variant={getVariant(status)} className={className} dot>
      {children || status.charAt(0).toUpperCase() + status.slice(1)}
    </Badge>
  );
};

export const PriorityBadge: React.FC<{
  priority: 'low' | 'medium' | 'high' | 'urgent';
  children?: React.ReactNode;
  className?: string;
}> = ({ priority, children, className }) => {
  const getVariant = (priority: string) => {
    switch (priority.toLowerCase()) {
      case 'low':
        return 'neutral';
      case 'medium':
        return 'info';
      case 'high':
        return 'warning';
      case 'urgent':
        return 'error';
      default:
        return 'neutral';
    }
  };

  return (
    <Badge variant={getVariant(priority)} className={className}>
      {children || priority.charAt(0).toUpperCase() + priority.slice(1)}
    </Badge>
  );
};

export const CountBadge: React.FC<{
  count: number;
  max?: number;
  className?: string;
}> = ({ count, max = 99, className }) => {
  const displayCount = count > max ? `${max}+` : count.toString();
  
  return (
    <Badge
      variant={count > 0 ? 'error' : 'neutral'}
      size="sm"
      className={cn('min-w-[1.25rem] px-1', className)}
    >
      {displayCount}
    </Badge>
  );
};

export { Badge };
export default Badge;
