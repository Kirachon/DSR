'use client';

// System Settings Page
// Interface for configuring system parameters and settings

import React, { useState, useEffect } from 'react';

import { SettingsSection } from '@/components/admin/settings-section';
import { FormInput, FormSelect, FormTextarea, FormCheckbox } from '@/components/forms';
import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { registrationApi } from '@/lib/api';
import type { SystemSettings } from '@/types';

// Settings categories
const SETTINGS_CATEGORIES = [
  'general',
  'authentication',
  'notifications',
  'security',
  'integrations',
  'maintenance',
] as const;

type SettingsCategory = typeof SETTINGS_CATEGORIES[number];

// System Settings page component
export default function SystemSettingsPage() {
  const { user } = useAuth();

  // State management
  const [settings, setSettings] = useState<SystemSettings[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeCategory, setActiveCategory] = useState<SettingsCategory>('general');
  const [hasChanges, setHasChanges] = useState(false);

  // Load settings data
  useEffect(() => {
    loadSettings();
  }, []);

  // Load settings from API
  const loadSettings = async () => {
    try {
      setLoading(true);
      setError(null);

      // Mock system settings for development
      const mockSettings: SystemSettings[] = [
        {
          id: '1',
          category: 'General',
          key: 'system.name',
          value: 'DSR System',
          type: 'string',
          description: 'System display name',
          isEditable: true,
          isPublic: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
        {
          id: '2',
          category: 'Security',
          key: 'auth.session.timeout',
          value: '3600',
          type: 'number',
          description: 'Session timeout in seconds',
          isEditable: true,
          isPublic: false,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
        {
          id: '3',
          category: 'Features',
          key: 'features.registration.enabled',
          value: 'true',
          type: 'boolean',
          description: 'Enable registration feature',
          isEditable: true,
          isPublic: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
      ];
      setSettings(mockSettings);
    } catch (err) {
      console.error('Failed to load settings:', err);
      setError('Failed to load settings. Please try again.');

      // Fallback to empty array
      setSettings([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle setting change
  const handleSettingChange = (settingId: string, value: any) => {
    setSettings(prev =>
      prev.map(setting =>
        setting.id === settingId
          ? { ...setting, value: value.toString() }
          : setting
      )
    );
    setHasChanges(true);
    setSuccess(null);
  };

  // Handle save settings
  const handleSaveSettings = async () => {
    if (settings.length === 0) return;

    try {
      setSaving(true);
      setError(null);

      // Mock save operation for development
      await new Promise(resolve => setTimeout(resolve, 1000));
      setSuccess('Settings saved successfully');
      setHasChanges(false);
    } catch (err) {
      console.error('Failed to save settings:', err);
      setError('Failed to save settings. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  // Handle reset settings
  const handleResetSettings = async () => {
    if (!confirm('Are you sure you want to reset all settings to default values?')) {
      return;
    }

    try {
      setSaving(true);
      // Mock reset operation for development
      await new Promise(resolve => setTimeout(resolve, 1000));
      await loadSettings();
      setSuccess('Settings reset to default values');
      setHasChanges(false);
    } catch (err) {
      console.error('Failed to reset settings:', err);
      setError('Failed to reset settings. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-2 text-gray-600">Loading settings...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">System Settings</h1>
          <p className="text-gray-600 mt-1">
            Configure system parameters and application settings
          </p>
        </div>

        <div className="flex space-x-3">
          <Button 
            variant="outline" 
            onClick={handleResetSettings}
            disabled={saving}
          >
            Reset to Defaults
          </Button>
          <Button 
            onClick={handleSaveSettings}
            disabled={!hasChanges || saving}
          >
            {saving ? 'Saving...' : 'Save Changes'}
          </Button>
        </div>
      </div>

      {/* Status Alerts */}
      {error && (
        <Alert variant="error" title="Error">
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" title="Success">
          {success}
        </Alert>
      )}

      {hasChanges && (
        <Alert variant="warning" title="Unsaved Changes">
          You have unsaved changes. Don't forget to save your settings.
        </Alert>
      )}

      <div className="flex flex-col lg:flex-row gap-6">
        {/* Settings Navigation */}
        <div className="lg:w-64">
          <Card className="p-4">
            <nav className="space-y-1">
              {SETTINGS_CATEGORIES.map(category => (
                <button
                  key={category}
                  onClick={() => setActiveCategory(category)}
                  className={`w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    activeCategory === category
                      ? 'bg-primary-100 text-primary-700'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  {category.charAt(0).toUpperCase() + category.slice(1)}
                </button>
              ))}
            </nav>
          </Card>
        </div>

        {/* Settings Content */}
        <div className="flex-1">
          <Card className="p-6">
            <SettingsSection
              title={`${activeCategory.charAt(0).toUpperCase() + activeCategory.slice(1)} Settings`}
              description={`Configure ${activeCategory} settings for the DSR system`}
            >
              <div className="space-y-4">
                {settings
                  .filter(setting => setting.category.toLowerCase() === activeCategory)
                  .map(setting => (
                    <div key={setting.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                      <div className="flex-1">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          {setting.key}
                        </label>
                        <p className="text-xs text-gray-500">{setting.description}</p>
                      </div>
                      <div className="ml-4">
                        {setting.type === 'boolean' ? (
                          <input
                            type="checkbox"
                            checked={setting.value === 'true'}
                            onChange={(e) => handleSettingChange(setting.id, e.target.checked)}
                            disabled={!setting.isEditable}
                            className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                          />
                        ) : setting.type === 'number' ? (
                          <input
                            type="number"
                            value={setting.value}
                            onChange={(e) => handleSettingChange(setting.id, e.target.value)}
                            disabled={!setting.isEditable}
                            className="w-24 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-primary-500 focus:border-primary-500"
                          />
                        ) : (
                          <input
                            type="text"
                            value={setting.value}
                            onChange={(e) => handleSettingChange(setting.id, e.target.value)}
                            disabled={!setting.isEditable}
                            className="w-48 px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-primary-500 focus:border-primary-500"
                          />
                        )}
                      </div>
                    </div>
                  ))}
                {settings.filter(setting => setting.category.toLowerCase() === activeCategory).length === 0 && (
                  <p className="text-gray-500 text-center py-8">No settings available for this category.</p>
                )}
              </div>
            </SettingsSection>
          </Card>
        </div>
      </div>
    </div>
  );
}
