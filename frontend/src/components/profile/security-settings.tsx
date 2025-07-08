'use client';

// Security Settings Component
// Component for managing user security settings

import React, { useState } from 'react';

import { Button, Badge } from '@/components/ui';
import type { UserProfile } from '@/types';

// Security settings props interface
interface SecuritySettingsProps {
  profile: UserProfile;
  onPasswordChange: (currentPassword: string, newPassword: string) => Promise<void>;
  onTwoFactorToggle: (enabled: boolean) => Promise<void>;
  loading: boolean;
}

// Security Settings component
export const SecuritySettings: React.FC<SecuritySettingsProps> = ({
  profile,
  onPasswordChange,
  onTwoFactorToggle,
  loading,
}) => {
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [showPasswordForm, setShowPasswordForm] = useState(false);

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      alert('New passwords do not match');
      return;
    }

    await onPasswordChange(passwordForm.currentPassword, passwordForm.newPassword);
    setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    setShowPasswordForm(false);
  };

  return (
    <div className="space-y-6">
      {/* Two-Factor Authentication */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-lg font-medium text-gray-900">Two-Factor Authentication</h3>
            <p className="text-sm text-gray-600">
              Add an extra layer of security to your account
            </p>
          </div>
          <div className="flex items-center space-x-3">
            {profile.securitySettings?.twoFactorEnabled && (
              <Badge variant="success">Enabled</Badge>
            )}
            <Button
              variant={profile.securitySettings?.twoFactorEnabled ? 'outline' : 'primary'}
              onClick={() => onTwoFactorToggle(!profile.securitySettings?.twoFactorEnabled)}
              disabled={loading}
            >
              {profile.securitySettings?.twoFactorEnabled ? 'Disable' : 'Enable'} 2FA
            </Button>
          </div>
        </div>
        
        {profile.securitySettings?.twoFactorEnabled && (
          <div className="text-sm text-gray-600">
            Two-factor authentication is currently enabled for your account.
          </div>
        )}
      </div>

      {/* Password Management */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-lg font-medium text-gray-900">Password</h3>
            <p className="text-sm text-gray-600">
              Last changed: {profile.securitySettings?.lastPasswordChange 
                ? new Date(profile.securitySettings.lastPasswordChange).toLocaleDateString()
                : 'Never'
              }
            </p>
          </div>
          <Button
            variant="outline"
            onClick={() => setShowPasswordForm(!showPasswordForm)}
          >
            Change Password
          </Button>
        </div>

        {showPasswordForm && (
          <form onSubmit={handlePasswordSubmit} className="mt-4 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Current Password
              </label>
              <input
                type="password"
                value={passwordForm.currentPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, currentPassword: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                New Password
              </label>
              <input
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, newPassword: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                required
                minLength={8}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Confirm New Password
              </label>
              <input
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm(prev => ({ ...prev, confirmPassword: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                required
              />
            </div>
            <div className="flex space-x-3">
              <Button type="submit" disabled={loading}>
                {loading ? 'Updating...' : 'Update Password'}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => setShowPasswordForm(false)}
              >
                Cancel
              </Button>
            </div>
          </form>
        )}
      </div>

      {/* Login Notifications */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900">Login Notifications</h3>
            <p className="text-sm text-gray-600">
              Get notified when someone logs into your account
            </p>
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              checked={profile.securitySettings?.loginNotifications || false}
              onChange={(e) => {
                // Handle login notifications toggle
                console.log('Login notifications:', e.target.checked);
              }}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* Security Alerts */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900">Security Alerts</h3>
            <p className="text-sm text-gray-600">
              Get notified about security events and suspicious activity
            </p>
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              checked={profile.securitySettings?.securityAlerts || false}
              onChange={(e) => {
                // Handle security alerts toggle
                console.log('Security alerts:', e.target.checked);
              }}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* Security Recommendations */}
      <div className="bg-blue-50 p-6 rounded-lg">
        <h3 className="text-lg font-medium text-blue-900 mb-3">Security Recommendations</h3>
        <ul className="space-y-2 text-sm text-blue-800">
          <li className="flex items-center">
            <svg className="h-4 w-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
            Use a strong, unique password
          </li>
          <li className="flex items-center">
            <svg className="h-4 w-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
            Enable two-factor authentication
          </li>
          <li className="flex items-center">
            <svg className="h-4 w-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
            Keep your contact information up to date
          </li>
          <li className="flex items-center">
            <svg className="h-4 w-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
            Review your account activity regularly
          </li>
        </ul>
      </div>
    </div>
  );
};
