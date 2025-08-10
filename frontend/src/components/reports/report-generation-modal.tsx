'use client';

// Report Generation Modal Component
// Modal for generating new reports from templates

import React, { useState } from 'react';

import { Modal, Button, Alert } from '@/components/ui';
// Removed FormInput, FormSelect imports to avoid useFormContext issues
import type { ReportTemplate, ReportGenerationRequest } from '@/types';

// Report generation modal props interface
interface ReportGenerationModalProps {
  isOpen: boolean;
  template: ReportTemplate | null;
  onClose: () => void;
  onGenerate: (templateId: string, parameters: any) => Promise<void>;
}

// Form validation
interface FormErrors {
  [key: string]: string;
}

// Report Generation Modal component
export const ReportGenerationModal: React.FC<ReportGenerationModalProps> = ({
  isOpen,
  template,
  onClose,
  onGenerate,
}) => {
  // State management
  const [parameters, setParameters] = useState<Record<string, any>>({});
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);

  // Handle parameter change
  const handleParameterChange = (paramName: string, value: any) => {
    setParameters(prev => ({
      ...prev,
      [paramName]: value,
    }));

    // Clear error for this parameter
    if (errors[paramName]) {
      setErrors(prev => ({
        ...prev,
        [paramName]: '',
      }));
    }
  };

  // Validate form
  const validateForm = (): boolean => {
    if (!template) return false;

    const newErrors: FormErrors = {};

    // Check required parameters
    template.parameters.forEach(param => {
      if (param.required && !parameters[param.name]) {
        newErrors[param.name] = `${param.label} is required`;
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!template || !validateForm()) {
      return;
    }

    try {
      setLoading(true);
      await onGenerate(template.id, parameters);
      handleClose();
    } catch (error) {
      console.error('Failed to generate report:', error);
    } finally {
      setLoading(false);
    }
  };

  // Handle modal close
  const handleClose = () => {
    setParameters({});
    setErrors({});
    onClose();
  };

  // Render parameter input based on type
  const renderParameterInput = (param: any) => {
    switch (param.type) {
      case 'DATE_RANGE':
        return (
          <div key={param.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {param.label} {param.required && '*'}
            </label>
            <div className="flex space-x-2">
              <input
                type="date"
                placeholder="Start Date"
                value={parameters[`${param.name}_start`] || ''}
                onChange={(e) => handleParameterChange(`${param.name}_start`, e.target.value)}
                className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              />
              <input
                type="date"
                placeholder="End Date"
                value={parameters[`${param.name}_end`] || ''}
                onChange={(e) => handleParameterChange(`${param.name}_end`, e.target.value)}
                className="w-1/2 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            {errors[param.name] && (
              <p className="text-sm text-red-600">{errors[param.name]}</p>
            )}
          </div>
        );

      case 'SELECT':
        return (
          <div key={param.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {param.label} {param.required && '*'}
            </label>
            <select
              value={parameters[param.name] || ''}
              onChange={(e) => handleParameterChange(param.name, e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              required={param.required}
            >
              <option value="">{`Select ${param.label}`}</option>
              {(param.options || []).map((option: string) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
            {errors[param.name] && (
              <p className="text-sm text-red-600">{errors[param.name]}</p>
            )}
          </div>
        );

      case 'TEXT':
        return (
          <div key={param.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {param.label} {param.required && '*'}
            </label>
            <input
              type="text"
              value={parameters[param.name] || ''}
              onChange={(e) => handleParameterChange(param.name, e.target.value)}
              placeholder={param.placeholder || `Enter ${param.label.toLowerCase()}`}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              required={param.required}
            />
            {errors[param.name] && (
              <p className="text-sm text-red-600">{errors[param.name]}</p>
            )}
          </div>
        );

      case 'NUMBER':
        return (
          <div key={param.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {param.label} {param.required && '*'}
            </label>
            <input
              type="number"
              value={parameters[param.name] || ''}
              onChange={(e) => handleParameterChange(param.name, e.target.value ? parseInt(e.target.value) : '')}
              placeholder={param.placeholder || `Enter ${param.label.toLowerCase()}`}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              required={param.required}
            />
            {errors[param.name] && (
              <p className="text-sm text-red-600">{errors[param.name]}</p>
            )}
          </div>
        );

      default:
        return (
          <div key={param.name} className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              {param.label} {param.required && '*'}
            </label>
            <input
              type="text"
              value={parameters[param.name] || ''}
              onChange={(e) => handleParameterChange(param.name, e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              required={param.required}
            />
            {errors[param.name] && (
              <p className="text-sm text-red-600">{errors[param.name]}</p>
            )}
          </div>
        );
    }
  };

  if (!template) {
    return null;
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Generate Report">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Template Information */}
        <div className="bg-gray-50 p-4 rounded-lg">
          <h3 className="text-lg font-medium text-gray-900 mb-2">{template.name}</h3>
          <p className="text-sm text-gray-600">{template.description}</p>
          <div className="mt-2">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary-100 text-primary-800">
              {template.category}
            </span>
          </div>
        </div>

        {/* Parameters */}
        {template.parameters.length > 0 && (
          <div>
            <h4 className="text-md font-medium text-gray-900 mb-4">Report Parameters</h4>
            <div className="space-y-4">
              {template.parameters.map(param => renderParameterInput(param))}
            </div>
          </div>
        )}

        {/* Generation Options */}
        <div>
          <h4 className="text-md font-medium text-gray-900 mb-4">Generation Options</h4>
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Output Format</label>
              <select
                value={parameters.format || 'PDF'}
                onChange={(e) => handleParameterChange('format', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="PDF">PDF Document</option>
                <option value="EXCEL">Excel Spreadsheet</option>
                <option value="CSV">CSV File</option>
              </select>
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Report Name (Optional)</label>
              <input
                type="text"
                value={parameters.customName || ''}
                onChange={(e) => handleParameterChange('customName', e.target.value)}
                placeholder="Enter custom report name"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
          </div>
        </div>

        {/* Estimated Generation Time */}
        <div className="bg-blue-50 p-4 rounded-lg">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-blue-800">
                Estimated Generation Time
              </h3>
              <div className="mt-2 text-sm text-blue-700">
                <p>This report typically takes 2-5 minutes to generate depending on the data range selected.</p>
              </div>
            </div>
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
          <Button type="button" variant="outline" onClick={handleClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? 'Generating...' : 'Generate Report'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
