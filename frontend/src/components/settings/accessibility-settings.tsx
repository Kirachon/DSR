'use client';

// Accessibility Settings Component
// Component for managing user accessibility preferences

import React from 'react';

// Accessibility settings props interface
interface AccessibilitySettingsProps {
  settings: any;
  onChange: (key: string, value: any) => void;
}

// Accessibility Settings component
export const AccessibilitySettings: React.FC<AccessibilitySettingsProps> = ({
  settings,
  onChange,
}) => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Accessibility Settings</h3>
        <p className="text-sm text-gray-600 mb-6">
          Configure accessibility features to improve your experience
        </p>
      </div>

      {/* Screen Reader Support */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Screen Reader Support</h4>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Enable Screen Reader</span>
              <p className="text-xs text-gray-500">Optimize interface for screen readers</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.screenReader || false}
              onChange={(e) => onChange('screenReader', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Skip Links</span>
              <p className="text-xs text-gray-500">Show skip navigation links</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.skipLinks || false}
              onChange={(e) => onChange('skipLinks', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* Visual Accessibility */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Visual Accessibility</h4>
        <div className="space-y-4">
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
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Large Text</span>
              <p className="text-xs text-gray-500">Increase text size for better readability</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.largeText || false}
              onChange={(e) => onChange('largeText', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-medium text-gray-700">Focus Indicators</span>
              <p className="text-xs text-gray-500">Enhanced focus indicators for keyboard navigation</p>
            </div>
            <input
              type="checkbox"
              checked={settings?.focusIndicators || false}
              onChange={(e) => onChange('focusIndicators', e.target.checked)}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
          </div>
        </div>
      </div>

      {/* Motion & Animation */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Motion & Animation</h4>
        <div className="flex items-center justify-between">
          <div>
            <span className="text-sm font-medium text-gray-700">Reduced Motion</span>
            <p className="text-xs text-gray-500">Minimize animations and transitions</p>
          </div>
          <input
            type="checkbox"
            checked={settings?.reducedMotion || false}
            onChange={(e) => onChange('reducedMotion', e.target.checked)}
            className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
          />
        </div>
      </div>

      {/* Keyboard Navigation */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Keyboard Navigation</h4>
        <div className="flex items-center justify-between">
          <div>
            <span className="text-sm font-medium text-gray-700">Enhanced Keyboard Navigation</span>
            <p className="text-xs text-gray-500">Improve keyboard accessibility features</p>
          </div>
          <input
            type="checkbox"
            checked={settings?.keyboardNavigation || false}
            onChange={(e) => onChange('keyboardNavigation', e.target.checked)}
            className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
          />
        </div>
      </div>

      {/* Accessibility Resources */}
      <div className="bg-blue-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-blue-900 mb-3">Accessibility Resources</h4>
        <p className="text-sm text-blue-800 mb-3">
          We're committed to making our platform accessible to everyone. If you need additional assistance or have suggestions for improvement, please let us know.
        </p>
        <div className="space-y-2">
          <a href="/accessibility-statement" className="text-sm text-blue-600 hover:text-blue-800 underline block">
            Accessibility Statement
          </a>
          <a href="/accessibility-help" className="text-sm text-blue-600 hover:text-blue-800 underline block">
            Accessibility Help
          </a>
          <a href="/contact-accessibility" className="text-sm text-blue-600 hover:text-blue-800 underline block">
            Contact Accessibility Team
          </a>
        </div>
      </div>

      {/* Keyboard Shortcuts */}
      <div className="bg-gray-50 p-6 rounded-lg">
        <h4 className="text-md font-medium text-gray-900 mb-3">Keyboard Shortcuts</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-600">Navigate to Dashboard:</span>
            <kbd className="px-2 py-1 bg-gray-200 rounded text-xs">Alt + D</kbd>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Open Search:</span>
            <kbd className="px-2 py-1 bg-gray-200 rounded text-xs">Ctrl + K</kbd>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Open Help:</span>
            <kbd className="px-2 py-1 bg-gray-200 rounded text-xs">F1</kbd>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Skip to Content:</span>
            <kbd className="px-2 py-1 bg-gray-200 rounded text-xs">Tab</kbd>
          </div>
        </div>
      </div>
    </div>
  );
};
