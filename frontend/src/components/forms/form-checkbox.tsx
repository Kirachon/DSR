'use client';

// Form Checkbox Component
// Checkbox component integrated with React Hook Form

import React from 'react';
import { useFormContext, Controller } from 'react-hook-form';

import { cn } from '@/utils';

// Form Checkbox props interface
export interface FormCheckboxProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'name' | 'type'> {
  name: string;
  label?: React.ReactNode;
  description?: string;
  required?: boolean;
}

// Form Checkbox component
export const FormCheckbox: React.FC<FormCheckboxProps> = ({
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
      render={({ field: { value, onChange, ...field } }) => (
        <div className={cn('space-y-2', className)}>
          <div className='flex items-start space-x-3'>
            <input
              {...field}
              {...props}
              type='checkbox'
              checked={value || false}
              onChange={e => onChange(e.target.checked)}
              className={cn(
                'h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500 focus:ring-offset-2',
                error && 'border-error-500 focus:ring-error-500'
              )}
              aria-invalid={!!error}
              aria-describedby={
                error
                  ? `${field.name}-error`
                  : description
                    ? `${field.name}-description`
                    : undefined
              }
            />

            {label && (
              <div className='flex-1'>
                <label
                  htmlFor={field.name}
                  className='text-sm font-medium text-gray-700 cursor-pointer'
                >
                  {label}
                  {required && <span className='ml-1 text-error-500'>*</span>}
                </label>

                {description && !error && (
                  <p
                    id={`${field.name}-description`}
                    className='text-sm text-gray-500 mt-1'
                  >
                    {description}
                  </p>
                )}
              </div>
            )}
          </div>

          {error && (
            <p
              id={`${field.name}-error`}
              className='text-sm text-error-600 ml-7'
              role='alert'
            >
              {error}
            </p>
          )}
        </div>
      )}
    />
  );
};

// Form Checkbox Group component
export interface CheckboxOption {
  value: string;
  label: string;
  description?: string;
  disabled?: boolean;
}

export interface FormCheckboxGroupProps {
  name: string;
  label?: string;
  description?: string;
  options: CheckboxOption[];
  required?: boolean;
  className?: string;
  orientation?: 'horizontal' | 'vertical';
}

export const FormCheckboxGroup: React.FC<FormCheckboxGroupProps> = ({
  name,
  label,
  description,
  options,
  required = false,
  className,
  orientation = 'vertical',
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
      render={({ field: { value = [], onChange } }) => (
        <div className={cn('space-y-3', className)}>
          {label && (
            <div>
              <label className='text-sm font-medium text-gray-700'>
                {label}
                {required && <span className='ml-1 text-error-500'>*</span>}
              </label>
              {description && !error && (
                <p className='text-sm text-gray-500 mt-1'>{description}</p>
              )}
            </div>
          )}

          <div
            className={cn(
              'space-y-2',
              orientation === 'horizontal' && 'flex flex-wrap gap-4 space-y-0'
            )}
          >
            {options.map(option => (
              <div key={option.value} className='flex items-start space-x-3'>
                <input
                  type='checkbox'
                  id={`${name}-${option.value}`}
                  checked={value.includes(option.value)}
                  disabled={option.disabled}
                  onChange={e => {
                    if (e.target.checked) {
                      onChange([...value, option.value]);
                    } else {
                      onChange(value.filter((v: string) => v !== option.value));
                    }
                  }}
                  className={cn(
                    'h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500 focus:ring-offset-2',
                    error && 'border-error-500 focus:ring-error-500'
                  )}
                />

                <div className='flex-1'>
                  <label
                    htmlFor={`${name}-${option.value}`}
                    className='text-sm font-medium text-gray-700 cursor-pointer'
                  >
                    {option.label}
                  </label>

                  {option.description && (
                    <p className='text-sm text-gray-500 mt-1'>
                      {option.description}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>

          {error && (
            <p className='text-sm text-error-600' role='alert'>
              {error}
            </p>
          )}
        </div>
      )}
    />
  );
};

export default FormCheckbox;
