'use client';

// DSR Workflow Timeline Component
// Visual timeline for application status tracking and case management

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { StatusBadge } from './status-badge';
import { cn } from '@/utils';

// Timeline variants
const timelineVariants = cva(
  'relative',
  {
    variants: {
      variant: {
        default: 'space-y-6',
        compact: 'space-y-4',
        detailed: 'space-y-8',
      },
      orientation: {
        vertical: 'flex flex-col',
        horizontal: 'flex flex-row items-center space-x-4 space-y-0',
      },
    },
    defaultVariants: {
      variant: 'default',
      orientation: 'vertical',
    },
  }
);

// Timeline event interface
export interface TimelineEvent {
  id: string;
  title: string;
  description?: string;
  timestamp: Date | string;
  status: 'completed' | 'current' | 'pending' | 'error' | 'skipped';
  actor?: {
    name: string;
    role: string;
    avatar?: string;
  };
  metadata?: {
    duration?: string;
    location?: string;
    reference?: string;
    attachments?: Array<{
      name: string;
      url: string;
      type: string;
    }>;
  };
  actions?: Array<{
    label: string;
    onClick: () => void;
    variant?: 'primary' | 'secondary' | 'outline';
  }>;
  icon?: React.ReactNode;
  color?: string;
}

// Timeline props
export interface WorkflowTimelineProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof timelineVariants> {
  events: TimelineEvent[];
  showTimestamps?: boolean;
  showActors?: boolean;
  showMetadata?: boolean;
  showActions?: boolean;
  interactive?: boolean;
  onEventClick?: (event: TimelineEvent) => void;
  emptyState?: React.ReactNode;
}

// Event status configuration
const eventStatusConfig = {
  completed: {
    bgColor: 'bg-dsr-completed',
    borderColor: 'border-green-600',
    textColor: 'text-green-700',
    icon: '✓',
    lineColor: 'bg-green-300',
  },
  current: {
    bgColor: 'bg-primary-600',
    borderColor: 'border-primary-600',
    textColor: 'text-primary-700',
    icon: '●',
    lineColor: 'bg-primary-300',
  },
  pending: {
    bgColor: 'bg-gray-300',
    borderColor: 'border-gray-400',
    textColor: 'text-gray-600',
    icon: '○',
    lineColor: 'bg-gray-200',
  },
  error: {
    bgColor: 'bg-error-600',
    borderColor: 'border-error-600',
    textColor: 'text-error-700',
    icon: '✕',
    lineColor: 'bg-error-300',
  },
  skipped: {
    bgColor: 'bg-gray-400',
    borderColor: 'border-gray-500',
    textColor: 'text-gray-600',
    icon: '—',
    lineColor: 'bg-gray-200',
  },
};

// Format timestamp
const formatTimestamp = (timestamp: Date | string): string => {
  const date = typeof timestamp === 'string' ? new Date(timestamp) : timestamp;
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

// Timeline event component
const TimelineEventComponent: React.FC<{
  event: TimelineEvent;
  index: number;
  isLast: boolean;
  variant: 'default' | 'compact' | 'detailed';
  orientation: 'vertical' | 'horizontal';
  showTimestamps: boolean;
  showActors: boolean;
  showMetadata: boolean;
  showActions: boolean;
  interactive: boolean;
  onEventClick?: (event: TimelineEvent) => void;
}> = ({
  event,
  index,
  isLast,
  variant,
  orientation,
  showTimestamps,
  showActors,
  showMetadata,
  showActions,
  interactive,
  onEventClick,
}) => {
  const config = eventStatusConfig[event.status];
  const isVertical = orientation === 'vertical';

  const handleClick = () => {
    if (interactive && onEventClick) {
      onEventClick(event);
    }
  };

  return (
    <div
      className={cn(
        'relative flex',
        isVertical ? 'flex-row' : 'flex-col items-center',
        interactive && 'cursor-pointer hover:bg-gray-50 rounded-lg p-2 -m-2 transition-colors',
        variant === 'compact' && 'text-sm'
      )}
      onClick={handleClick}
    >
      {/* Timeline Line (for vertical orientation) */}
      {isVertical && !isLast && (
        <div
          className={cn(
            'absolute left-4 top-8 w-0.5 h-full',
            config.lineColor,
            variant === 'compact' && 'top-6',
            variant === 'detailed' && 'top-10'
          )}
        />
      )}

      {/* Timeline Dot */}
      <div
        className={cn(
          'relative z-10 flex items-center justify-center rounded-full border-2 font-medium text-white',
          config.bgColor,
          config.borderColor,
          variant === 'compact' ? 'w-6 h-6 text-xs' : 'w-8 h-8 text-sm',
          variant === 'detailed' && 'w-10 h-10 text-base',
          event.status === 'current' && 'ring-2 ring-primary-200 ring-offset-2 animate-pulse-slow',
          isVertical ? 'mr-4' : 'mb-2'
        )}
      >
        {event.icon || config.icon}
      </div>

      {/* Event Content */}
      <div className={cn('flex-1 min-w-0', isVertical ? 'pb-6' : 'text-center')}>
        {/* Header */}
        <div className={cn('flex items-start justify-between', !isVertical && 'flex-col')}>
          <div className="flex-1 min-w-0">
            <h3
              className={cn(
                'font-medium text-gray-900',
                variant === 'compact' && 'text-sm',
                variant === 'detailed' && 'text-lg'
              )}
            >
              {event.title}
            </h3>
            
            {event.description && (
              <p
                className={cn(
                  'text-gray-600 mt-1',
                  variant === 'compact' && 'text-xs',
                  variant === 'detailed' && 'text-base'
                )}
              >
                {event.description}
              </p>
            )}
          </div>

          {/* Status Badge */}
          <StatusBadge
            status={event.status}
            size={variant === 'compact' ? 'sm' : 'md'}
            className={cn('flex-shrink-0', isVertical ? 'ml-4' : 'mt-2')}
          />
        </div>

        {/* Timestamp */}
        {showTimestamps && (
          <div
            className={cn(
              'text-gray-500 mt-1',
              variant === 'compact' && 'text-xs',
              variant === 'detailed' && 'text-sm'
            )}
          >
            {formatTimestamp(event.timestamp)}
          </div>
        )}

        {/* Actor Information */}
        {showActors && event.actor && (
          <div className="flex items-center mt-2 space-x-2">
            {event.actor.avatar && (
              <img
                src={event.actor.avatar}
                alt={event.actor.name}
                className="w-6 h-6 rounded-full"
              />
            )}
            <div className="text-sm text-gray-600">
              <span className="font-medium">{event.actor.name}</span>
              <span className="text-gray-500"> • {event.actor.role}</span>
            </div>
          </div>
        )}

        {/* Metadata */}
        {showMetadata && event.metadata && (
          <div className="mt-3 space-y-1">
            {event.metadata.duration && (
              <div className="text-sm text-gray-600">
                <span className="font-medium">Duration:</span> {event.metadata.duration}
              </div>
            )}
            {event.metadata.location && (
              <div className="text-sm text-gray-600">
                <span className="font-medium">Location:</span> {event.metadata.location}
              </div>
            )}
            {event.metadata.reference && (
              <div className="text-sm text-gray-600">
                <span className="font-medium">Reference:</span> {event.metadata.reference}
              </div>
            )}
            {event.metadata.attachments && event.metadata.attachments.length > 0 && (
              <div className="text-sm text-gray-600">
                <span className="font-medium">Attachments:</span>
                <div className="mt-1 space-y-1">
                  {event.metadata.attachments.map((attachment, idx) => (
                    <a
                      key={idx}
                      href={attachment.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-primary-600 hover:text-primary-700 underline"
                    >
                      {attachment.name}
                    </a>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* Actions */}
        {showActions && event.actions && event.actions.length > 0 && (
          <div className="mt-3 flex flex-wrap gap-2">
            {event.actions.map((action, idx) => (
              <button
                key={idx}
                onClick={(e) => {
                  e.stopPropagation();
                  action.onClick();
                }}
                className={cn(
                  'px-3 py-1 text-xs font-medium rounded-md transition-colors',
                  action.variant === 'primary' && 'bg-primary-600 text-white hover:bg-primary-700',
                  action.variant === 'secondary' && 'bg-gray-600 text-white hover:bg-gray-700',
                  (!action.variant || action.variant === 'outline') && 'border border-gray-300 text-gray-700 hover:bg-gray-50'
                )}
              >
                {action.label}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Horizontal connector line */}
      {!isVertical && !isLast && (
        <div className={cn('w-8 h-0.5 mx-2', config.lineColor)} />
      )}
    </div>
  );
};

// Empty state component
const TimelineEmptyState: React.FC<{ message?: string }> = ({ 
  message = 'No timeline events available' 
}) => (
  <div className="flex flex-col items-center justify-center py-12 text-center">
    <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mb-4">
      <span className="text-gray-400 text-2xl">📅</span>
    </div>
    <p className="text-gray-500">{message}</p>
  </div>
);

// Main workflow timeline component
const WorkflowTimeline = forwardRef<HTMLDivElement, WorkflowTimelineProps>(
  (
    {
      className,
      variant,
      orientation,
      events,
      showTimestamps = true,
      showActors = false,
      showMetadata = false,
      showActions = false,
      interactive = false,
      onEventClick,
      emptyState,
      ...props
    },
    ref
  ) => {
    if (events.length === 0) {
      return emptyState || <TimelineEmptyState />;
    }

    return (
      <div
        ref={ref}
        className={cn(timelineVariants({ variant, orientation }), className)}
        role="log"
        aria-label="Workflow timeline"
        {...props}
      >
        {events.map((event, index) => (
          <TimelineEventComponent
            key={event.id}
            event={event}
            index={index}
            isLast={index === events.length - 1}
            variant={variant || 'default'}
            orientation={orientation || 'vertical'}
            showTimestamps={showTimestamps}
            showActors={showActors}
            showMetadata={showMetadata}
            showActions={showActions}
            interactive={interactive}
            onEventClick={onEventClick}
          />
        ))}
      </div>
    );
  }
);

WorkflowTimeline.displayName = 'WorkflowTimeline';

export { WorkflowTimeline, timelineVariants };
export type { WorkflowTimelineProps, TimelineEvent };
