'use client';

// DSR Progress Indicator Component
// Multi-step process visualization for citizen workflows and staff processing

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { cn } from '@/utils';

// Progress indicator variants
const progressIndicatorVariants = cva(
  // Base styles
  'flex items-center justify-between w-full',
  {
    variants: {
      variant: {
        linear: 'flex-row',
        stepped: 'flex-row',
        circular: 'flex-col items-center justify-center',
      },
      size: {
        sm: 'text-xs',
        md: 'text-sm',
        lg: 'text-base',
      },
      orientation: {
        horizontal: 'flex-row',
        vertical: 'flex-col space-y-4',
      },
    },
    defaultVariants: {
      variant: 'stepped',
      size: 'md',
      orientation: 'horizontal',
    },
  }
);

// Step status type
export type StepStatus = 'pending' | 'current' | 'completed' | 'error' | 'skipped';

// Step interface
export interface Step {
  id: string;
  label: string;
  description?: string;
  status: StepStatus;
  icon?: React.ReactNode;
  optional?: boolean;
}

// Progress indicator props
export interface ProgressIndicatorProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof progressIndicatorVariants> {
  steps: Step[];
  currentStep?: number;
  showLabels?: boolean;
  showDescriptions?: boolean;
  clickable?: boolean;
  onStepClick?: (stepIndex: number, step: Step) => void;
}

// Step status colors and icons
const stepStatusConfig = {
  pending: {
    bgColor: 'bg-gray-200',
    textColor: 'text-gray-500',
    borderColor: 'border-gray-300',
    icon: '○',
  },
  current: {
    bgColor: 'bg-primary-600',
    textColor: 'text-white',
    borderColor: 'border-primary-600',
    icon: '●',
  },
  completed: {
    bgColor: 'bg-dsr-completed',
    textColor: 'text-white',
    borderColor: 'border-green-600',
    icon: '✓',
  },
  error: {
    bgColor: 'bg-error-600',
    textColor: 'text-white',
    borderColor: 'border-error-600',
    icon: '✕',
  },
  skipped: {
    bgColor: 'bg-gray-300',
    textColor: 'text-gray-600',
    borderColor: 'border-gray-400',
    icon: '—',
  },
};

// Individual step component
const StepItem: React.FC<{
  step: Step;
  index: number;
  isLast: boolean;
  variant: 'linear' | 'stepped' | 'circular';
  size: 'sm' | 'md' | 'lg';
  orientation: 'horizontal' | 'vertical';
  showLabels: boolean;
  showDescriptions: boolean;
  clickable: boolean;
  onStepClick?: (stepIndex: number, step: Step) => void;
}> = ({
  step,
  index,
  isLast,
  variant,
  size,
  orientation,
  showLabels,
  showDescriptions,
  clickable,
  onStepClick,
}) => {
  const config = stepStatusConfig[step.status];
  const isVertical = orientation === 'vertical';
  
  const stepSizes = {
    sm: 'w-6 h-6 text-xs',
    md: 'w-8 h-8 text-sm',
    lg: 'w-10 h-10 text-base',
  };

  const handleClick = () => {
    if (clickable && onStepClick) {
      onStepClick(index, step);
    }
  };

  return (
    <div
      className={cn(
        'flex items-center',
        isVertical ? 'flex-col' : 'flex-row',
        clickable && 'cursor-pointer hover:opacity-80',
        'transition-all duration-200'
      )}
      onClick={handleClick}
      role={clickable ? 'button' : undefined}
      tabIndex={clickable ? 0 : undefined}
      onKeyDown={(e) => {
        if (clickable && (e.key === 'Enter' || e.key === ' ')) {
          e.preventDefault();
          handleClick();
        }
      }}
    >
      {/* Step Circle */}
      <div
        className={cn(
          'flex items-center justify-center rounded-full border-2 font-medium transition-all duration-200',
          stepSizes[size],
          config.bgColor,
          config.textColor,
          config.borderColor,
          step.status === 'current' && 'ring-2 ring-primary-200 ring-offset-2',
          step.status === 'error' && 'ring-2 ring-error-200 ring-offset-2'
        )}
        aria-label={`Step ${index + 1}: ${step.label} - ${step.status}`}
      >
        {step.icon || config.icon}
      </div>

      {/* Step Content */}
      {(showLabels || showDescriptions) && (
        <div
          className={cn(
            'flex flex-col',
            isVertical ? 'mt-2 text-center' : 'ml-3 text-left',
            size === 'sm' && 'space-y-0.5',
            size === 'md' && 'space-y-1',
            size === 'lg' && 'space-y-1.5'
          )}
        >
          {showLabels && (
            <span
              className={cn(
                'font-medium transition-colors duration-200',
                step.status === 'current' && 'text-primary-700',
                step.status === 'completed' && 'text-dsr-completed',
                step.status === 'error' && 'text-error-600',
                step.status === 'pending' && 'text-gray-600',
                step.status === 'skipped' && 'text-gray-500',
                size === 'sm' && 'text-xs',
                size === 'md' && 'text-sm',
                size === 'lg' && 'text-base'
              )}
            >
              {step.label}
              {step.optional && (
                <span className="ml-1 text-gray-400 text-xs">(Optional)</span>
              )}
            </span>
          )}
          {showDescriptions && step.description && (
            <span
              className={cn(
                'text-gray-600 transition-colors duration-200',
                size === 'sm' && 'text-xs',
                size === 'md' && 'text-xs',
                size === 'lg' && 'text-sm'
              )}
            >
              {step.description}
            </span>
          )}
        </div>
      )}

      {/* Connector Line */}
      {!isLast && variant !== 'circular' && (
        <div
          className={cn(
            'transition-colors duration-200',
            isVertical
              ? 'w-0.5 h-8 mx-auto'
              : 'h-0.5 flex-1 mx-4',
            step.status === 'completed' || 
            (index < steps.findIndex(s => s.status === 'current'))
              ? 'bg-dsr-completed'
              : 'bg-gray-300'
          )}
        />
      )}
    </div>
  );
};

// Main progress indicator component
const ProgressIndicator = forwardRef<HTMLDivElement, ProgressIndicatorProps>(
  (
    {
      className,
      variant,
      size,
      orientation,
      steps,
      currentStep,
      showLabels = true,
      showDescriptions = false,
      clickable = false,
      onStepClick,
      ...props
    },
    ref
  ) => {
    // Update step statuses based on currentStep if provided
    const processedSteps = React.useMemo(() => {
      if (currentStep === undefined) return steps;
      
      return steps.map((step, index) => ({
        ...step,
        status: index < currentStep 
          ? 'completed' as StepStatus
          : index === currentStep 
            ? 'current' as StepStatus
            : 'pending' as StepStatus
      }));
    }, [steps, currentStep]);

    // Calculate progress percentage for linear variant
    const progressPercentage = React.useMemo(() => {
      const completedSteps = processedSteps.filter(step => step.status === 'completed').length;
      const currentStepIndex = processedSteps.findIndex(step => step.status === 'current');
      const totalProgress = completedSteps + (currentStepIndex >= 0 ? 0.5 : 0);
      return Math.min((totalProgress / processedSteps.length) * 100, 100);
    }, [processedSteps]);

    if (variant === 'linear') {
      return (
        <div
          ref={ref}
          className={cn('w-full space-y-2', className)}
          {...props}
        >
          {/* Progress Bar */}
          <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
            <div
              className="h-full bg-primary-600 transition-all duration-500 ease-out"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
          
          {/* Step Labels */}
          {showLabels && (
            <div className="flex justify-between text-sm">
              {processedSteps.map((step, index) => (
                <span
                  key={step.id}
                  className={cn(
                    'transition-colors duration-200',
                    step.status === 'current' && 'text-primary-700 font-medium',
                    step.status === 'completed' && 'text-dsr-completed font-medium',
                    step.status === 'pending' && 'text-gray-500'
                  )}
                >
                  {step.label}
                </span>
              ))}
            </div>
          )}
        </div>
      );
    }

    if (variant === 'circular') {
      const currentStepData = processedSteps.find(step => step.status === 'current') || processedSteps[0];
      const completedCount = processedSteps.filter(step => step.status === 'completed').length;
      
      return (
        <div
          ref={ref}
          className={cn('flex flex-col items-center space-y-4', className)}
          {...props}
        >
          {/* Circular Progress */}
          <div className="relative w-24 h-24">
            <svg className="w-24 h-24 transform -rotate-90" viewBox="0 0 100 100">
              <circle
                cx="50"
                cy="50"
                r="40"
                stroke="currentColor"
                strokeWidth="8"
                fill="transparent"
                className="text-gray-200"
              />
              <circle
                cx="50"
                cy="50"
                r="40"
                stroke="currentColor"
                strokeWidth="8"
                fill="transparent"
                strokeDasharray={`${progressPercentage * 2.51} 251`}
                className="text-primary-600 transition-all duration-500"
              />
            </svg>
            <div className="absolute inset-0 flex items-center justify-center">
              <span className="text-lg font-bold text-primary-700">
                {completedCount}/{processedSteps.length}
              </span>
            </div>
          </div>
          
          {/* Current Step Info */}
          {currentStepData && (
            <div className="text-center">
              <h3 className="font-medium text-gray-900">{currentStepData.label}</h3>
              {showDescriptions && currentStepData.description && (
                <p className="text-sm text-gray-600 mt-1">{currentStepData.description}</p>
              )}
            </div>
          )}
        </div>
      );
    }

    // Stepped variant (default)
    return (
      <div
        ref={ref}
        className={cn(
          progressIndicatorVariants({ variant, size, orientation }),
          className
        )}
        role="progressbar"
        aria-valuenow={progressPercentage}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label="Progress through steps"
        {...props}
      >
        {processedSteps.map((step, index) => (
          <StepItem
            key={step.id}
            step={step}
            index={index}
            isLast={index === processedSteps.length - 1}
            variant={variant}
            size={size}
            orientation={orientation}
            showLabels={showLabels}
            showDescriptions={showDescriptions}
            clickable={clickable}
            onStepClick={onStepClick}
          />
        ))}
      </div>
    );
  }
);

ProgressIndicator.displayName = 'ProgressIndicator';

export { ProgressIndicator, progressIndicatorVariants };
export type { ProgressIndicatorProps, Step, StepStatus };
