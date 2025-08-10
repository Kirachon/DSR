'use client';

// Form Component
// Wrapper component for React Hook Form with Zod validation integration

import { zodResolver } from '@hookform/resolvers/zod';
import React from 'react';
import {
  useForm,
  FormProvider,
  UseFormReturn,
  FieldValues,
  SubmitHandler,
} from 'react-hook-form';
import { z } from 'zod';

import { Button, Loading } from '@/components/ui';
import { cn } from '@/utils';

// Form props interface
export interface FormProps<T extends FieldValues> {
  schema: z.ZodType<T>;
  onSubmit: SubmitHandler<T>;
  defaultValues?: Partial<T>;
  children: React.ReactNode | ((methods: UseFormReturn<T>) => React.ReactNode);
  className?: string;
  loading?: boolean;
  disabled?: boolean;
  resetOnSubmit?: boolean;
  validateOnChange?: boolean;
  validateOnBlur?: boolean;
}

// Form component
export function Form<T extends FieldValues>({
  schema,
  onSubmit,
  defaultValues,
  children,
  className,
  loading = false,
  disabled = false,
  resetOnSubmit = false,
  validateOnChange = false,
  validateOnBlur = true,
}: FormProps<T>) {
  const methods = useForm<T>({
    resolver: zodResolver(schema as any),
    defaultValues: defaultValues as any,
    mode: validateOnChange
      ? 'onChange'
      : validateOnBlur
        ? 'onBlur'
        : 'onSubmit',
  });

  const handleSubmit = async (data: T) => {
    try {
      await onSubmit(data);
      if (resetOnSubmit) {
        methods.reset();
      }
    } catch (error) {
      console.error('Form submission error:', error);
    }
  };

  return (
    <FormProvider {...methods}>
      <form
        onSubmit={methods.handleSubmit(handleSubmit as any)}
        className={cn('space-y-4', className)}
        noValidate
      >
        {loading && (
          <div className='absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center z-10'>
            <Loading size='lg' text='Processing...' />
          </div>
        )}

        <fieldset disabled={disabled || loading} className='space-y-4'>
          {typeof children === 'function' ? children(methods as any) : children}
        </fieldset>
      </form>
    </FormProvider>
  );
}

// Form Field wrapper component
export interface FormFieldProps {
  name: string;
  children: React.ReactNode;
  className?: string;
}

export const FormField: React.FC<FormFieldProps> = ({
  name,
  children,
  className,
}) => {
  return (
    <div className={cn('space-y-2', className)} data-field={name}>
      {children}
    </div>
  );
};

// Form Actions component
export interface FormActionsProps {
  children: React.ReactNode;
  className?: string;
  align?: 'left' | 'center' | 'right' | 'between';
}

export const FormActions: React.FC<FormActionsProps> = ({
  children,
  className,
  align = 'right',
}) => {
  const alignClasses = {
    left: 'justify-start',
    center: 'justify-center',
    right: 'justify-end',
    between: 'justify-between',
  };

  return (
    <div
      className={cn(
        'flex items-center space-x-2 pt-4',
        alignClasses[align],
        className
      )}
    >
      {children}
    </div>
  );
};

// Form Section component
export interface FormSectionProps {
  title?: string;
  description?: string;
  children: React.ReactNode;
  className?: string;
}

export const FormSection: React.FC<FormSectionProps> = ({
  title,
  description,
  children,
  className,
}) => {
  return (
    <div className={cn('space-y-4', className)}>
      {(title || description) && (
        <div className='space-y-1'>
          {title && (
            <h3 className='text-lg font-medium text-gray-900'>{title}</h3>
          )}
          {description && (
            <p className='text-sm text-gray-500'>{description}</p>
          )}
        </div>
      )}
      <div className='space-y-4'>{children}</div>
    </div>
  );
};

// Form Grid component
export interface FormGridProps {
  children: React.ReactNode;
  columns?: 1 | 2 | 3 | 4;
  gap?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const FormGrid: React.FC<FormGridProps> = ({
  children,
  columns = 2,
  gap = 'md',
  className,
}) => {
  const columnClasses = {
    1: 'grid-cols-1',
    2: 'grid-cols-1 md:grid-cols-2',
    3: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
    4: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  };

  const gapClasses = {
    sm: 'gap-2',
    md: 'gap-4',
    lg: 'gap-6',
  };

  return (
    <div
      className={cn('grid', columnClasses[columns], gapClasses[gap], className)}
    >
      {children}
    </div>
  );
};

// Form Error component
export interface FormErrorProps {
  message?: string;
  className?: string;
}

export const FormError: React.FC<FormErrorProps> = ({ message, className }) => {
  if (!message) return null;

  return (
    <p className={cn('text-sm text-error-600', className)} role='alert'>
      {message}
    </p>
  );
};

// Form Success component
export interface FormSuccessProps {
  message?: string;
  className?: string;
}

export const FormSuccess: React.FC<FormSuccessProps> = ({
  message,
  className,
}) => {
  if (!message) return null;

  return <p className={cn('text-sm text-success-600', className)}>{message}</p>;
};

// Form Helper Text component
export interface FormHelperTextProps {
  children: React.ReactNode;
  className?: string;
}

export const FormHelperText: React.FC<FormHelperTextProps> = ({
  children,
  className,
}) => {
  return <p className={cn('text-sm text-gray-500', className)}>{children}</p>;
};

// Form Submit Button component
export interface FormSubmitButtonProps {
  children: React.ReactNode;
  loading?: boolean;
  disabled?: boolean;
  variant?: 'primary' | 'secondary' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
  className?: string;
}

export const FormSubmitButton: React.FC<FormSubmitButtonProps> = ({
  children,
  loading = false,
  disabled = false,
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  className,
}) => {
  return (
    <Button
      type='submit'
      variant={variant}
      size={size}
      fullWidth={fullWidth}
      loading={loading}
      disabled={disabled}
      className={className}
    >
      {children}
    </Button>
  );
};

// Form Reset Button component
export interface FormResetButtonProps {
  children: React.ReactNode;
  onReset?: () => void;
  variant?: 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const FormResetButton: React.FC<FormResetButtonProps> = ({
  children,
  onReset,
  variant = 'outline',
  size = 'md',
  className,
}) => {
  return (
    <Button
      type='reset'
      variant={variant}
      size={size}
      onClick={onReset}
      className={className}
    >
      {children}
    </Button>
  );
};

export default Form;
