'use client';

// Form Input Component
// Input component integrated with React Hook Form

import React from 'react';
import { useFormContext, Controller } from 'react-hook-form';

import { Input, type InputProps } from '@/components/ui';
import { cn } from '@/utils';

// Form Input props interface
export interface FormInputProps extends Omit<InputProps, 'name' | 'onChange'> {
  name?: string;
  label?: string;
  description?: string;
  required?: boolean;
  error?: string;
  onChange?: (value: string | React.ChangeEvent<HTMLInputElement>) => void;
}

// Form Input component
export const FormInput: React.FC<FormInputProps> = ({
  name,
  label,
  description,
  required = false,
  className,
  ...props
}) => {
  const {
    control,
    formState: { errors },
  } = useFormContext();

  const error = errors[name]?.message as string | undefined;

  return (
    <Controller
      name={name}
      control={control}
      render={({ field }) => (
        <div className={cn('space-y-2', className)}>
          {label && (
            <label
              htmlFor={field.name}
              className='block text-sm font-medium text-gray-700'
            >
              {label}
              {required && <span className='ml-1 text-error-500'>*</span>}
            </label>
          )}

          <Input
            {...field}
            {...props}
            id={field.name}
            error={error}
            aria-invalid={!!error}
            aria-describedby={
              error
                ? `${field.name}-error`
                : description
                  ? `${field.name}-description`
                  : undefined
            }
          />

          {description && !error && (
            <p
              id={`${field.name}-description`}
              className='text-sm text-gray-500'
            >
              {description}
            </p>
          )}
        </div>
      )}
    />
  );
};

export default FormInput;
