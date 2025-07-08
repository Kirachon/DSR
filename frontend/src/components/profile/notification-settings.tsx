'use client';

// Notification Settings Component
// Component for managing user notification preferences

import React, { useState } from 'react';

import { Button } from '@/components/ui';

// Notification settings props interface
interface NotificationSettingsProps {
  settings: any;
  onUpdate: (settings: any) => Promise<void>;
  loading: boolean;
}

// Notification Settings component
export const NotificationSettings: React.FC<NotificationSettingsProps> = ({
  settings,
  onUpdate,
  loading,
}) => {
  const [formData, setFormData] = useState({
    emailNotifications: settings?.emailNotifications || false,
    smsNotifications: settings?.smsNotifications || false,
    pushNotifications: settings?.pushNotifications || false,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onUpdate(formData);
  };

  const handleToggle = (field: string, value: boolean) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Email Notifications */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900">Email Notifications</h3>
            <p className="text-sm text-gray-600">
              Receive notifications via email
            </p>
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              checked={formData.emailNotifications}
              onChange={(e) => handleToggle('emailNotifications', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* SMS Notifications */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900">SMS Notifications</h3>
            <p className="text-sm text-gray-600">
              Receive notifications via SMS
            </p>
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              checked={formData.smsNotifications}
              onChange={(e) => handleToggle('smsNotifications', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* Push Notifications */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900">Push Notifications</h3>
            <p className="text-sm text-gray-600">
              Receive push notifications in your browser
            </p>
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              checked={formData.pushNotifications}
              onChange={(e) => handleToggle('pushNotifications', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* Form Actions */}
      <div className="flex justify-end pt-6 border-t border-gray-200">
        <Button type="submit" disabled={loading}>
          {loading ? 'Updating...' : 'Update Notifications'}
        </Button>
      </div>
    </form>
  );
};
