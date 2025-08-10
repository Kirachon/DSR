'use client';

// Document Upload Step Component
// Fourth step of household registration wizard

import React, { useState, useRef } from 'react';

import { FormSelect } from '@/components/forms';
import { Button, Card, Alert } from '@/components/ui';
import type { DocumentUpload, HouseholdRegistrationData } from '@/types';

// Component props interface
interface DocumentUploadStepProps {
  data: DocumentUpload[];
  onUpdate: (data: Partial<HouseholdRegistrationData>) => void;
  errors: string[];
}

// Document type options
const DOCUMENT_TYPE_OPTIONS = [
  { value: '', label: 'Select Document Type' },
  { value: 'BIRTH_CERTIFICATE', label: 'Birth Certificate' },
  { value: 'MARRIAGE_CERTIFICATE', label: 'Marriage Certificate' },
  { value: 'DEATH_CERTIFICATE', label: 'Death Certificate' },
  { value: 'VALID_ID', label: 'Valid ID' },
  { value: 'PROOF_OF_INCOME', label: 'Proof of Income' },
  { value: 'PROOF_OF_RESIDENCE', label: 'Proof of Residence' },
  { value: 'MEDICAL_CERTIFICATE', label: 'Medical Certificate' },
  { value: 'SCHOOL_ENROLLMENT', label: 'School Enrollment Certificate' },
  { value: 'PWD_ID', label: 'PWD ID' },
  { value: 'SENIOR_CITIZEN_ID', label: 'Senior Citizen ID' },
  { value: 'PHILSYS_ID', label: 'PhilSys ID' },
  { value: 'OTHER', label: 'Other' },
];

// Required documents
const REQUIRED_DOCUMENTS = [
  'BIRTH_CERTIFICATE',
  'VALID_ID',
  'PROOF_OF_RESIDENCE',
];

// Document Upload Step component
export const DocumentUploadStep: React.FC<DocumentUploadStepProps> = ({
  data,
  onUpdate,
  errors,
}) => {
  const [selectedDocumentType, setSelectedDocumentType] = useState('');
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Handle file selection
  const handleFileSelect = (files: FileList | null, documentType?: string) => {
    if (!files || files.length === 0) return;

    const file = files[0];
    const type = documentType || selectedDocumentType;

    if (!type) {
      alert('Please select a document type first');
      return;
    }

    // Validate file type
    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'image/jpg',
      'application/pdf',
    ];
    if (!allowedTypes.includes(file.type)) {
      alert('Only JPEG, PNG, and PDF files are allowed');
      return;
    }

    // Validate file size (max 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      alert('File size must be less than 5MB');
      return;
    }

    // Create new document
    const newDocument: DocumentUpload = {
      id: Date.now().toString(),
      type,
      name: file.name,
      file,
      status: 'PENDING',
    };

    // Add to documents list
    const updatedDocuments = [...data, newDocument];
    onUpdate({ documents: updatedDocuments });

    // Reset selection
    setSelectedDocumentType('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // Handle drag and drop
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    handleFileSelect(e.dataTransfer.files);
  };

  // Remove document
  const handleRemoveDocument = (documentId: string) => {
    const updatedDocuments = data.filter(doc => doc.id !== documentId);
    onUpdate({ documents: updatedDocuments });
  };

  // Get file size in readable format
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // Get document type display name
  const getDocumentTypeDisplayName = (type: string): string => {
    const option = DOCUMENT_TYPE_OPTIONS.find(opt => opt.value === type);
    return option ? option.label : type.replace('_', ' ');
  };

  // Check if required documents are uploaded
  const getRequiredDocumentStatus = () => {
    const uploadedTypes = data.map(doc => doc.type);
    const missingRequired = REQUIRED_DOCUMENTS.filter(
      type => !uploadedTypes.includes(type)
    );
    return {
      allRequired: missingRequired.length === 0,
      missing: missingRequired,
    };
  };

  const requiredStatus = getRequiredDocumentStatus();

  return (
    <div className='space-y-6'>
      {/* Instructions */}
      <div className='bg-blue-50 border border-blue-200 rounded-lg p-4'>
        <div className='flex'>
          <div className='flex-shrink-0'>
            <svg
              className='h-5 w-5 text-blue-400'
              fill='currentColor'
              viewBox='0 0 20 20'
            >
              <path
                fillRule='evenodd'
                d='M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z'
                clipRule='evenodd'
              />
            </svg>
          </div>
          <div className='ml-3'>
            <h3 className='text-sm font-medium text-blue-800'>
              Document Upload Requirements
            </h3>
            <div className='mt-2 text-sm text-blue-700'>
              <ul className='list-disc list-inside space-y-1'>
                <li>Upload clear, readable copies of required documents</li>
                <li>Accepted formats: JPEG, PNG, PDF (max 5MB per file)</li>
                <li>Ensure all text and details are visible</li>
                <li>
                  Required documents: Birth Certificate, Valid ID, Proof of
                  Residence
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Required Documents Status */}
      {!requiredStatus.allRequired && (
        <Alert variant='warning' title='Missing Required Documents'>
          <p className='mb-2'>
            The following required documents are still needed:
          </p>
          <ul className='list-disc list-inside space-y-1'>
            {requiredStatus.missing.map(type => (
              <li key={type}>{getDocumentTypeDisplayName(type)}</li>
            ))}
          </ul>
        </Alert>
      )}

      {/* Upload Section */}
      <Card className='p-6'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Upload Documents
        </h4>

        <div className='space-y-4'>
          {/* Document Type Selection */}
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            <FormSelect
              label='Document Type *'
              value={selectedDocumentType}
              onChange={value =>
                setSelectedDocumentType(
                  typeof value === 'string' ? value : value.target.value
                )
              }
              options={DOCUMENT_TYPE_OPTIONS}
              required
            />
          </div>

          {/* File Upload Area */}
          <div
            className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
              dragOver
                ? 'border-primary-400 bg-primary-50'
                : 'border-gray-300 hover:border-gray-400'
            }`}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
          >
            <svg
              className='mx-auto h-12 w-12 text-gray-400 mb-4'
              stroke='currentColor'
              fill='none'
              viewBox='0 0 48 48'
            >
              <path
                d='M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02'
                strokeWidth={2}
                strokeLinecap='round'
                strokeLinejoin='round'
              />
            </svg>
            <div className='text-gray-600'>
              <p className='text-lg font-medium mb-2'>
                {dragOver ? 'Drop files here' : 'Drag and drop files here'}
              </p>
              <p className='text-sm mb-4'>or</p>
              <Button
                variant='outline'
                onClick={() => fileInputRef.current?.click()}
                disabled={!selectedDocumentType}
              >
                Browse Files
              </Button>
              <p className='text-xs text-gray-500 mt-2'>
                JPEG, PNG, PDF up to 5MB
              </p>
            </div>
            <input
              ref={fileInputRef}
              type='file'
              className='hidden'
              accept='.jpg,.jpeg,.png,.pdf'
              onChange={e => handleFileSelect(e.target.files)}
            />
          </div>
        </div>
      </Card>

      {/* Uploaded Documents */}
      <Card className='p-6'>
        <div className='flex justify-between items-center mb-4'>
          <h4 className='text-lg font-medium text-gray-900'>
            Uploaded Documents ({data.length})
          </h4>
          {requiredStatus.allRequired && (
            <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-success-100 text-success-800'>
              All Required Documents Uploaded
            </span>
          )}
        </div>

        {data.length === 0 ? (
          <div className='text-center py-8'>
            <svg
              className='mx-auto h-12 w-12 text-gray-400 mb-4'
              fill='none'
              stroke='currentColor'
              viewBox='0 0 24 24'
            >
              <path
                strokeLinecap='round'
                strokeLinejoin='round'
                strokeWidth={2}
                d='M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
              />
            </svg>
            <h3 className='text-lg font-medium text-gray-900 mb-2'>
              No documents uploaded
            </h3>
            <p className='text-gray-600'>
              Upload your documents to complete the registration process.
            </p>
          </div>
        ) : (
          <div className='space-y-3'>
            {data.map(document => (
              <div
                key={document.id}
                className='flex items-center justify-between p-4 border border-gray-200 rounded-lg'
              >
                <div className='flex items-center space-x-4'>
                  <div className='flex-shrink-0'>
                    {document.file?.type.startsWith('image/') ? (
                      <svg
                        className='h-8 w-8 text-blue-500'
                        fill='currentColor'
                        viewBox='0 0 20 20'
                      >
                        <path
                          fillRule='evenodd'
                          d='M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z'
                          clipRule='evenodd'
                        />
                      </svg>
                    ) : (
                      <svg
                        className='h-8 w-8 text-red-500'
                        fill='currentColor'
                        viewBox='0 0 20 20'
                      >
                        <path
                          fillRule='evenodd'
                          d='M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z'
                          clipRule='evenodd'
                        />
                      </svg>
                    )}
                  </div>
                  <div className='flex-1'>
                    <div className='flex items-center space-x-2'>
                      <h5 className='font-medium text-gray-900'>
                        {document.name}
                      </h5>
                      <span
                        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                          REQUIRED_DOCUMENTS.includes(document.type)
                            ? 'bg-orange-100 text-orange-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {REQUIRED_DOCUMENTS.includes(document.type)
                          ? 'Required'
                          : 'Optional'}
                      </span>
                    </div>
                    <div className='text-sm text-gray-600'>
                      <span>{getDocumentTypeDisplayName(document.type)}</span>
                      <span className='mx-2'>•</span>
                      <span>
                        {document.file
                          ? formatFileSize(document.file.size)
                          : 'Unknown size'}
                      </span>
                      <span className='mx-2'>•</span>
                      <span
                        className={`${
                          document.status === 'PENDING'
                            ? 'text-yellow-600'
                            : document.status === 'UPLOADED'
                              ? 'text-blue-600'
                              : document.status === 'VERIFIED'
                                ? 'text-green-600'
                                : 'text-red-600'
                        }`}
                      >
                        {document.status}
                      </span>
                    </div>
                  </div>
                </div>
                <div className='flex items-center space-x-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => handleRemoveDocument(document.id)}
                    className='text-red-600 hover:text-red-700 hover:border-red-300'
                  >
                    Remove
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Document Guidelines */}
      <Card className='p-6 bg-gray-50'>
        <h4 className='text-lg font-medium text-gray-900 mb-4'>
          Document Guidelines
        </h4>
        <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
          <div>
            <h5 className='font-medium text-gray-900 mb-2'>
              Required Documents
            </h5>
            <ul className='text-sm text-gray-600 space-y-1'>
              <li>• Birth Certificate (PSA/NSO issued)</li>
              <li>• Valid Government ID</li>
              <li>• Proof of Residence (Barangay Certificate, Utility Bill)</li>
            </ul>
          </div>
          <div>
            <h5 className='font-medium text-gray-900 mb-2'>
              Optional Documents
            </h5>
            <ul className='text-sm text-gray-600 space-y-1'>
              <li>• Marriage Certificate (if applicable)</li>
              <li>• Medical Certificate (for health conditions)</li>
              <li>• School Enrollment (for students)</li>
              <li>• PWD ID (for persons with disability)</li>
            </ul>
          </div>
        </div>
      </Card>
    </div>
  );
};
