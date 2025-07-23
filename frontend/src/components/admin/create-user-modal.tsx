'use client';

import React, { useState } from 'react';
import { Modal } from '@/components/ui/modal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { FormInput } from '@/components/forms/form-input';
import { FormSelect } from '@/components/forms/form-select';

interface CreateUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (userData: any) => void;
}

export function CreateUserModal({ isOpen, onClose, onSubmit }: CreateUserModalProps) {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    role: 'CITIZEN',
    phoneNumber: '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
    onClose();
  };

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New User">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <FormInput
            label="First Name"
            value={formData.firstName}
            onChange={(value) => handleChange('firstName', value)}
            required
          />
          <FormInput
            label="Last Name"
            value={formData.lastName}
            onChange={(value) => handleChange('lastName', value)}
            required
          />
        </div>
        
        <FormInput
          label="Email"
          type="email"
          value={formData.email}
          onChange={(value) => handleChange('email', value)}
          required
        />
        
        <FormSelect
          label="Role"
          value={formData.role}
          onChange={(value) => handleChange('role', value)}
          options={[
            { value: 'CITIZEN', label: 'Citizen' },
            { value: 'LGU_STAFF', label: 'LGU Staff' },
            { value: 'DSWD_STAFF', label: 'DSWD Staff' },
            { value: 'ADMIN', label: 'Administrator' },
          ]}
          required
        />
        
        <FormInput
          label="Phone Number"
          value={formData.phoneNumber}
          onChange={(value) => handleChange('phoneNumber', value)}
        />
        
        <div className="flex justify-end space-x-2 pt-4">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit">
            Create User
          </Button>
        </div>
      </form>
    </Modal>
  );
}

export default CreateUserModal;
