'use client';

// Privacy Settings Component
// Component for managing user privacy preferences

import React from 'react';

// Privacy settings props interface
interface PrivacySettingsProps {
  settings: any;
  onChange: (key: string, value: any) => void;
}

// Privacy Settings component
export const PrivacySettings: React.FC<PrivacySettingsProps> = ({
  settings,
  onChange,
}) => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Privacy Settings</h3>
        <p className="text-sm text-gray-600 mb-6">
          Control how your data is used and shared
        </p>
      </div>

      {/* Profile Visibility */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Profile Visibility</h4>
        <div className="space-y-3">
          <label className="flex items-center">
            <input
              type="radio"
              name="profileVisibility"
              value="public"
              checked={settings?.profileVisibility === 'public'}
              onChange={(e) => onChange('profileVisibility', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Public</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="profileVisibility"
              value="private"
              checked={settings?.profileVisibility === 'private'}
              onChange={(e) => onChange('profileVisibility', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Private</span>
          </label>
        </div>
      </div>

      {/* Data Collection */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Data Collection</h4>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Allow Data Collection</span>
              <p className="text-xs text-gray-500">Help improve our services by sharing usage data</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.allowDataCollection || false}
              onChange={(e) => onChange('allowDataCollection', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Analytics</span>
              <p className="text-xs text-gray-500">Allow analytics tracking for service improvement</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.allowAnalytics || false}
              onChange={(e) => onChange('allowAnalytics', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Marketing Communications</span>
              <p className="text-xs text-gray-500">Receive promotional emails and updates</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.allowMarketing || false}
              onChange={(e) => onChange('allowMarketing', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* Online Status */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Online Status</h4>
        <div className="flex items-center justify-between">
          <div>
            <span className="text-sm font-medium text-gray-700">Show Online Status</span>
            <p className="text-xs text-gray-500">Let others see when you're online</p>
          </div>
          <input
            type="checkbox"
            checked={settings?.showOnlineStatus || false}
            onChange={(e) => onChange('showOnlineStatus', e.target.checked)}
            className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
          />
        </div>
      </div>

      {/* Data Retention */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Data Retention</h4>
        <select
          value={settings?.dataRetention || '2years'}
          onChange={(e) => onChange('dataRetention', e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="1year">1 Year</option>
          <option value="2years">2 Years</option>
          <option value="5years">5 Years</option>
          <option value="indefinite">Indefinite</option>
        </select>
        <p className="text-xs text-gray-500 mt-2">
          How long to keep your data after account deletion
        </p>
      </div>

      {/* Privacy Notice */}
      <div className="bg-blue-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-blue-900 mb-3">Privacy Notice</h4>
        <p className="text-sm text-blue-800 mb-3">
          Your privacy is important to us. We are committed to protecting your personal information and being transparent about how we use it.
        </p>
        <div className="space-y-2">
          <a href="/privacy-policy" className="text-sm text-blue-600 hover:text-blue-800 underline block">
            Read our Privacy Policy
          </a>
          <a href="/data-policy" className="text-sm text-blue-600 hover:text-blue-800 underline block">
            View Data Usage Policy
          </a>
          <a href="/cookie-policy" className="text-sm text-blue-600 hover:text-blue-800 underline block">
            Cookie Policy
          </a>
        </div>
      </div>
    </div>
  );
};
