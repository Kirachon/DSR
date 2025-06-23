'use client';

// Form Select Component
// Select dropdown component integrated with React Hook Form

import React from 'react';
import { useFormContext, Controller } from 'react-hook-form';

import { cn } from '@/utils';

// Select option interface
export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

// Form Select props interface
export interface FormSelectProps extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'name'> {
  name: string;
  label?: string;
  description?: string;
  placeholder?: string;
  options: SelectOption[];
  required?: boolean;
  error?: string;
}

// Form Select component
export const FormSelect: React.FC<FormSelectProps> = ({
  name,
  label,
  description,
  placeholder = 'Select an option...',
  options,
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
        <div className="space-y-2">
          {label && (
            <label
              htmlFor={field.name}
              className="block text-sm font-medium text-gray-700"
            >
              {label}
              {required && <span className="ml-1 text-error-500">*</span>}
            </label>
          )}
          
          <select
            {...field}
            {...props}
            id={field.name}
            className={cn(
              'flex w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
              error && 'border-error-500 focus-visible:ring-error-500',
              className
            )}
            aria-invalid={!!error}
            aria-describedby={
              error ? `${field.name}-error` : 
              description ? `${field.name}-description` : 
              undefined
            }
          >
            {placeholder && (
              <option value="" disabled>
                {placeholder}
              </option>
            )}
            {options.map((option) => (
              <option
                key={option.value}
                value={option.value}
                disabled={option.disabled}
              >
                {option.label}
              </option>
            ))}
          </select>
          
          {error && (
            <p
              id={`${field.name}-error`}
              className="text-sm text-error-600"
              role="alert"
            >
              {error}
            </p>
          )}
          
          {description && !error && (
            <p
              id={`${field.name}-description`}
              className="text-sm text-gray-500"
            >
              {description}
            </p>
          )}
        </div>
      )}
    />
  );
};

export default FormSelect;
