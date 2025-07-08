'use client';

// Appearance Settings Component
// Component for managing user appearance preferences

import React from 'react';

// Appearance settings props interface
interface AppearanceSettingsProps {
  settings: any;
  onChange: (key: string, value: any) => void;
}

// Appearance Settings component
export const AppearanceSettings: React.FC<AppearanceSettingsProps> = ({
  settings,
  onChange,
}) => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Appearance Settings</h3>
        <p className="text-sm text-gray-600 mb-6">
          Customize how the application looks and feels
        </p>
      </div>

      {/* Theme Selection */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Theme</h4>
        <div className="space-y-3">
          <label className="flex items-center">
            <input
              type="radio"
              name="theme"
              value="light"
              checked={settings?.theme === 'light'}
              onChange={(e) => onChange('theme', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Light</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="theme"
              value="dark"
              checked={settings?.theme === 'dark'}
              onChange={(e) => onChange('theme', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Dark</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="theme"
              value="auto"
              checked={settings?.theme === 'auto'}
              onChange={(e) => onChange('theme', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Auto (System)</span>
          </label>
        </div>
      </div>

      {/* Color Scheme */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Color Scheme</h4>
        <div className="grid grid-cols-4 gap-3">
          {['blue', 'green', 'purple', 'red'].map(color => (
            <button
              key={color}
              onClick={() => onChange('colorScheme', color)}
              className={`p-3 rounded-lg border-2 transition-all ${
                settings?.colorScheme === color
                  ? 'border-primary-500 bg-primary-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <div className={`w-full h-8 rounded bg-${color}-500 mb-2`}></div>
              <span className="text-xs text-gray-700 capitalize">{color}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Font Size */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Font Size</h4>
        <div className="space-y-3">
          <label className="flex items-center">
            <input
              type="radio"
              name="fontSize"
              value="small"
              checked={settings?.fontSize === 'small'}
              onChange={(e) => onChange('fontSize', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Small</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="fontSize"
              value="medium"
              checked={settings?.fontSize === 'medium'}
              onChange={(e) => onChange('fontSize', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Medium</span>
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="fontSize"
              value="large"
              checked={settings?.fontSize === 'large'}
              onChange={(e) => onChange('fontSize', e.target.value)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
            />
            <span className="ml-3 text-sm text-gray-700">Large</span>
          </label>
        </div>
      </div>

      {/* Display Options */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Display Options</h4>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Compact Mode</span>
              <p className="text-xs text-gray-500">Reduce spacing and padding</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.compactMode || false}
              onChange={(e) => onChange('compactMode', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Show Animations</span>
              <p className="text-xs text-gray-500">Enable smooth transitions and animations</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.showAnimations || false}
              onChange={(e) => onChange('showAnimations', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">High Contrast</span>
              <p className="text-xs text-gray-500">Increase contrast for better visibility</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.highContrast || false}
              onChange={(e) => onChange('highContrast', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>
    </div>
  );
};
