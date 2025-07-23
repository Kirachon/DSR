'use client';

// Card Component
// Flexible card component with header, body, footer, and various styling options

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef } from 'react';

import { cn } from '@/utils';

// Card variants
const cardVariants = cva(
  // Base styles - flat government design with sharp corners
  'rounded-sm border bg-white text-gray-950 shadow-flat-sm',
  {
    variants: {
      variant: {
        default: 'border-gray-300', // Stronger border for government authority
        outlined: 'border-gray-400', // More defined borders
        elevated: 'border-gray-300 shadow-flat-md', // Minimal elevation only
        ghost: 'border-transparent shadow-none',
        primary: 'border-primary-500 bg-primary-50', // Stronger borders for authority
        secondary: 'border-secondary-500 bg-secondary-50',
        accent: 'border-accent-500 bg-accent-50',
        success: 'border-success-500 bg-success-50',
        warning: 'border-warning-500 bg-warning-50',
        error: 'border-error-500 bg-error-50',
      },
      size: {
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8',
      },
      interactive: {
        true: 'cursor-pointer transition-colors duration-150 hover:bg-gray-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-1',
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
