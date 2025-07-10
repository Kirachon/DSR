'use client';

// DSR Status Badge Component
// Specialized badge component for DSR application and process statuses

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { cn } from '@/utils';

// Status badge variants
const statusBadgeVariants = cva(
  // Base styles with enhanced accessibility
  'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-1',
  {
    variants: {
      status: {
        // Application Statuses
        draft: 'bg-gray-100 text-gray-700 border border-gray-200',
        submitted: 'bg-blue-100 text-blue-700 border border-blue-200',
        review: 'bg-purple-100 text-purple-700 border border-purple-200',
        approved: 'bg-green-100 text-green-700 border border-green-200',
        rejected: 'bg-red-100 text-red-700 border border-red-200',
        
        // Eligibility Statuses
        eligible: 'bg-dsr-eligible/10 text-green-700 border border-green-200 animate-status-change',
        pending: 'bg-dsr-pending/10 text-yellow-700 border border-yellow-200 animate-pulse-slow',
        processing: 'bg-dsr-processing/10 text-blue-700 border border-blue-200 animate-pulse-slow',
        completed: 'bg-dsr-completed/10 text-green-800 border border-green-300',
        
        // Payment Statuses
        'payment-pending': 'bg-yellow-100 text-yellow-700 border border-yellow-200',
        'payment-processing': 'bg-blue-100 text-blue-700 border border-blue-200 animate-pulse-slow',
        'payment-completed': 'bg-green-100 text-green-700 border border-green-200',
        'payment-failed': 'bg-red-100 text-red-700 border border-red-200',
        
        // Case Statuses
        'case-new': 'bg-blue-100 text-blue-700 border border-blue-200',
        'case-assigned': 'bg-purple-100 text-purple-700 border border-purple-200',
        'case-in-progress': 'bg-yellow-100 text-yellow-700 border border-yellow-200',
        'case-resolved': 'bg-green-100 text-green-700 border border-green-200',
        'case-closed': 'bg-gray-100 text-gray-700 border border-gray-200',
      },
      size: {
        sm: 'px-2 py-0.5 text-xs',
        md: 'px-2.5 py-1 text-xs',
        lg: 'px-3 py-1.5 text-sm',
      },
      style: {
        solid: '',
        outline: 'bg-transparent',
        soft: '',
      },
      priority: {
        low: 'ring-1 ring-gray-300',
        normal: '',
        high: 'ring-2 ring-orange-300',
        urgent: 'ring-2 ring-red-400 animate-bounce-gentle',
      },
    },
    defaultVariants: {
      status: 'draft',
      size: 'md',
      style: 'soft',
      priority: 'normal',
    },
  }
);

// Status badge props interface
export interface StatusBadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof statusBadgeVariants> {
  asChild?: boolean;
  icon?: React.ReactNode;
  showIcon?: boolean;
  pulse?: boolean;
}

// Status icons mapping
const statusIcons = {
  draft: '📝',
  submitted: '📤',
  review: '👀',
  approved: '✅',
  rejected: '❌',
  eligible: '✅',
  pending: '⏳',
  processing: '⚙️',
  completed: '🎉',
  'payment-pending': '💰',
  'payment-processing': '💳',
  'payment-completed': '✅',
  'payment-failed': '❌',
  'case-new': '🆕',
  'case-assigned': '👤',
  'case-in-progress': '🔄',
  'case-resolved': '✅',
  'case-closed': '📁',
};

// Status badge component
const StatusBadge = forwardRef<HTMLSpanElement, StatusBadgeProps>(
  (
    {
      className,
      status,
      size,
      style,
      priority,
      asChild = false,
      icon,
      showIcon = true,
      pulse = false,
      children,
      ...props
    },
    ref
  ) => {
    const statusIcon = icon || (showIcon && status ? statusIcons[status] : null);
    
    return (
      <span
        ref={ref}
        className={cn(
          statusBadgeVariants({ status, size, style, priority }),
          pulse && 'animate-pulse-slow',
          className
        )}
        role="status"
        aria-label={`Status: ${status}`}
        {...props}
      >
        {statusIcon && (
          <span className="flex-shrink-0" aria-hidden="true">
            {statusIcon}
          </span>
        )}
        {children}
      </span>
    );
  }
);

StatusBadge.displayName = 'StatusBadge';

// Helper function to get status display text
export const getStatusText = (status: string): string => {
  const statusTexts: Record<string, string> = {
    draft: 'Draft',
    submitted: 'Submitted',
    review: 'Under Review',
    approved: 'Approved',
    rejected: 'Rejected',
    eligible: 'Eligible',
    pending: 'Pending',
    processing: 'Processing',
    completed: 'Completed',
    'payment-pending': 'Payment Pending',
    'payment-processing': 'Processing Payment',
    'payment-completed': 'Payment Completed',
    'payment-failed': 'Payment Failed',
    'case-new': 'New Case',
    'case-assigned': 'Assigned',
    'case-in-progress': 'In Progress',
    'case-resolved': 'Resolved',
    'case-closed': 'Closed',
  };
  
  return statusTexts[status] || status;
};

// Helper function to get status color for custom styling
export const getStatusColor = (status: string): string => {
  const statusColors: Record<string, string> = {
    draft: 'gray',
    submitted: 'blue',
    review: 'purple',
    approved: 'green',
    rejected: 'red',
    eligible: 'green',
    pending: 'yellow',
    processing: 'blue',
    completed: 'green',
    'payment-pending': 'yellow',
    'payment-processing': 'blue',
    'payment-completed': 'green',
    'payment-failed': 'red',
    'case-new': 'blue',
    'case-assigned': 'purple',
    'case-in-progress': 'yellow',
    'case-resolved': 'green',
    'case-closed': 'gray',
  };
  
  return statusColors[status] || 'gray';
};

export { StatusBadge, statusBadgeVariants };
export type { StatusBadgeProps };
