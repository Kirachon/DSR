'use client';

// Language Settings Component
// Component for managing user language and regional preferences

import React from 'react';

// Language settings props interface
interface LanguageSettingsProps {
  settings: any;
  onChange: (key: string, value: any) => void;
}

// Language Settings component
export const LanguageSettings: React.FC<LanguageSettingsProps> = ({
  settings,
  onChange,
}) => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Language & Region Settings</h3>
        <p className="text-sm text-gray-600 mb-6">
          Configure your language, date, time, and regional preferences
        </p>
      </div>

      {/* Language Selection */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Language</h4>
        <select
          value={settings?.locale || 'en-PH'}
          onChange={(e) => onChange('locale', e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="en-PH">English (Philippines)</option>
          <option value="fil-PH">Filipino</option>
          <option value="en-US">English (United States)</option>
        </select>
      </div>

      {/* Date Format */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Date Format</h4>
        <div className="space-y-3">
          <label className="flex items-center">
            <input
              type="radio"
              name="dateFormat"
              value="MM/DD/YYYY"
              checked={settings?.dateFormat === 'MM/DD/YYYY'}
              onChange={(e) => onChange('dateFormat', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">MM/DD/YYYY (01/25/2024)</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="dateFormat"
              value="DD/MM/YYYY"
              checked={settings?.dateFormat === 'DD/MM/YYYY'}
              onChange={(e) => onChange('dateFormat', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">DD/MM/YYYY (25/01/2024)</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="dateFormat"
              value="YYYY-MM-DD"
              checked={settings?.dateFormat === 'YYYY-MM-DD'}
              onChange={(e) => onChange('dateFormat', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">YYYY-MM-DD (2024-01-25)</span>
          </label>
        </div>
      </div>

      {/* Time Format */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Time Format</h4>
        <div className="space-y-3">
          <label className="flex items-center">
            <input
              type="radio"
              name="timeFormat"
              value="12h"
              checked={settings?.timeFormat === '12h'}
              onChange={(e) => onChange('timeFormat', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">12-hour (2:30 PM)</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="timeFormat"
              value="24h"
              checked={settings?.timeFormat === '24h'}
              onChange={(e) => onChange('timeFormat', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">24-hour (14:30)</span>
          </label>
        </div>
      </div>

      {/* Timezone */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Timezone</h4>
        <select
          value={settings?.timezone || 'Asia/Manila'}
          onChange={(e) => onChange('timezone', e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="Asia/Manila">Asia/Manila (GMT+8)</option>
          <option value="Asia/Tokyo">Asia/Tokyo (GMT+9)</option>
          <option value="America/New_York">America/New_York (GMT-5)</option>
          <option value="Europe/London">Europe/London (GMT+0)</option>
        </select>
      </div>

      {/* Currency */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Currency</h4>
        <select
          value={settings?.currency || 'PHP'}
          onChange={(e) => onChange('currency', e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="PHP">Philippine Peso (₱)</option>
          <option value="USD">US Dollar ($)</option>
          <option value="EUR">Euro (€)</option>
          <option value="JPY">Japanese Yen (¥)</option>
        </select>
      </div>

      {/* Number Format */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Number Format</h4>
        <select
          value={settings?.numberFormat || 'en-PH'}
          onChange={(e) => onChange('numberFormat', e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="en-PH">1,234.56 (Philippines)</option>
          <option value="en-US">1,234.56 (US)</option>
          <option value="de-DE">1.234,56 (Germany)</option>
          <option value="fr-FR">1 234,56 (France)</option>
        </select>
      </div>
    </div>
  );
};
