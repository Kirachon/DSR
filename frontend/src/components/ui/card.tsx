'use client';

// Card Component
// Flexible card component with header, body, footer, and various styling options

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { cn } from '@/utils';

// Card variants
const cardVariants = cva(
  // Base styles
  'rounded-lg border bg-white text-gray-950 shadow-sm',
  {
    variants: {
      variant: {
        default: 'border-gray-200',
        outlined: 'border-gray-300',
        elevated: 'border-gray-200 shadow-md',
        ghost: 'border-transparent shadow-none',
        primary: 'border-primary-200 bg-primary-50',
        secondary: 'border-secondary-200 bg-secondary-50',
        accent: 'border-accent-200 bg-accent-50',
        success: 'border-success-200 bg-success-50',
        warning: 'border-warning-200 bg-warning-50',
        error: 'border-error-200 bg-error-50',
      },
      size: {
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8',
      },
      interactive: {
        true: 'cursor-pointer transition-all duration-200 hover:shadow-md hover:scale-[1.02] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2',
        false: '',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
      interactive: false,
    },
  }
);

// Card props interface
export interface CardProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof cardVariants> {
  asChild?: boolean;
}

// Card Header props
export interface CardHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
  asChild?: boolean;
}

// Card Title props
export interface CardTitleProps
  extends React.HTMLAttributes<HTMLHeadingElement> {
  asChild?: boolean;
  level?: 1 | 2 | 3 | 4 | 5 | 6;
}

// Card Description props
export interface CardDescriptionProps
  extends React.HTMLAttributes<HTMLParagraphElement> {
  asChild?: boolean;
}

// Card Content props
export interface CardContentProps extends React.HTMLAttributes<HTMLDivElement> {
  asChild?: boolean;
}

// Card Footer props
export interface CardFooterProps extends React.HTMLAttributes<HTMLDivElement> {
  asChild?: boolean;
}

// Main Card component
const Card = forwardRef<HTMLDivElement, CardProps>(
  (
    { className, variant, size, interactive, asChild = false, ...props },
    ref
  ) => {
    const Comp = asChild ? 'div' : 'div';

    return (
      <Comp
        ref={ref}
        className={cn(cardVariants({ variant, size, interactive, className }))}
        {...props}
      />
    );
  }
);
Card.displayName = 'Card';

// Card Header component
const CardHeader = forwardRef<HTMLDivElement, CardHeaderProps>(
  ({ className, asChild = false, ...props }, ref) => {
    const Comp = asChild ? 'div' : 'div';

    return (
      <Comp
        ref={ref}
        className={cn('flex flex-col space-y-1.5 p-6', className)}
        {...props}
      />
    );
  }
);
CardHeader.displayName = 'CardHeader';

// Card Title component
const CardTitle = forwardRef<HTMLHeadingElement, CardTitleProps>(
  ({ className, asChild = false, level = 3, children, ...props }, ref) => {
    if (asChild) {
      return (
        <div
          ref={ref as React.Ref<HTMLDivElement>}
          className={cn(
            'text-2xl font-semibold leading-none tracking-tight',
            className
          )}
          {...(props as React.HTMLAttributes<HTMLDivElement>)}
        >
          {children}
        </div>
      );
    }

    // Use React.createElement to avoid complex union type issues
    return React.createElement(
      `h${level}`,
      {
        ref,
        className: cn(
          'text-2xl font-semibold leading-none tracking-tight',
          className
        ),
        ...props,
      },
      children
    );
  }
);
CardTitle.displayName = 'CardTitle';

// Card Description component
const CardDescription = forwardRef<HTMLParagraphElement, CardDescriptionProps>(
  ({ className, asChild = false, ...props }, ref) => {
    const Comp = asChild ? 'div' : 'p';

    return (
      <Comp
        ref={ref as any}
        className={cn('text-sm text-gray-500', className)}
        {...props}
      />
    );
  }
);
CardDescription.displayName = 'CardDescription';

// Card Content component
const CardContent = forwardRef<HTMLDivElement, CardContentProps>(
  ({ className, asChild = false, ...props }, ref) => {
    const Comp = asChild ? 'div' : 'div';

    return <Comp ref={ref} className={cn('p-6 pt-0', className)} {...props} />;
  }
);
CardContent.displayName = 'CardContent';

// Card Footer component
const CardFooter = forwardRef<HTMLDivElement, CardFooterProps>(
  ({ className, asChild = false, ...props }, ref) => {
    const Comp = asChild ? 'div' : 'div';

    return (
      <Comp
        ref={ref}
        className={cn('flex items-center p-6 pt-0', className)}
        {...props}
      />
    );
  }
);
CardFooter.displayName = 'CardFooter';

// Compound Card component with all sub-components
const CompoundCard = Object.assign(Card, {
  Header: CardHeader,
  Title: CardTitle,
  Description: CardDescription,
  Content: CardContent,
  Footer: CardFooter,
});

export {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
  CompoundCard,
  cardVariants,
};

export default CompoundCard;
