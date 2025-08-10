'use client';

// User Settings Page
// Interface for users to configure their personal application settings

import React, { useState, useEffect } from 'react';

import { AppearanceSettings } from '@/components/settings/appearance-settings';
import { LanguageSettings } from '@/components/settings/language-settings';
import { PrivacySettings } from '@/components/settings/privacy-settings';
import { AccessibilitySettings } from '@/components/settings/accessibility-settings';
import { FormSelect, FormCheckbox, FormSlider } from '@/components/forms';
import { Card, Button, Alert } from '@/components/ui';
import { useAuth } from '@/contexts';
import { registrationApi } from '@/lib/api';
import type { UserSettings } from '@/types';

// Settings tabs
const SETTINGS_TABS = [
  { id: 'appearance', label: 'Appearance', icon: '🎨' },
  { id: 'language', label: 'Language & Region', icon: '🌐' },
  { id: 'privacy', label: 'Privacy', icon: '🔒' },
  { id: 'accessibility', label: 'Accessibility', icon: '♿' },
] as const;

type SettingsTab = typeof SETTINGS_TABS[number]['id'];

// User Settings page component
export default function UserSettingsPage() {
  const { user } = useAuth();

  // State management
  const [settings, setSettings] = useState<UserSettings | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<SettingsTab>('appearance');
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

      const response = await registrationApi.getUserSettings(user?.id || '');
      setSettings(response);
    } catch (err) {
      console.error('Failed to load settings:', err);
      setError('Failed to load settings. Please try again.');
      
      // Fallback to mock data for development
      setSettings({
        appearance: {
          theme: 'light',
          colorScheme: 'blue',
          fontSize: 'medium',
          compactMode: false,
          showAnimations: true,
          highContrast: false,
        },
        language: {
          locale: 'en-PH',
          dateFormat: 'MM/DD/YYYY',
          timeFormat: '12h',
          timezone: 'Asia/Manila',
          currency: 'PHP',
          numberFormat: 'en-PH',
        },
        privacy: {
          profileVisibility: 'private',
          showOnlineStatus: false,
          allowDataCollection: true,
          allowAnalytics: true,
          allowMarketing: false,
          dataRetention: '2years',
        },
        accessibility: {
          screenReader: false,
          keyboardNavigation: true,
          reducedMotion: false,
          largeText: false,
          highContrast: false,
          focusIndicators: true,
          skipLinks: true,
        },
        notifications: {
          desktop: true,
          email: true,
          sms: false,
          push: false,
          sound: true,
          vibration: false,
        },
      } as UserSettings);
    } finally {
      setLoading(false);
    }
  };

  // Handle setting change
  const handleSettingChange = (category: string, key: string, value: any) => {
    if (!settings) return;

    setSettings(prev => ({
      ...prev!,
      [category]: {
        ...prev![category as keyof UserSettings],
        [key]: value,
      },
    }));
    setHasChanges(true);
    setSuccess(null);
  };

  // Handle save settings
  const handleSaveSettings = async () => {
    if (!settings) return;

    try {
      setSaving(true);
      setError(null);

      await registrationApi.updateUserSettings(user?.id || '', settings);
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
      await registrationApi.resetUserSettings(user?.id || '');
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
          <h1 className="text-3xl font-bold text-gray-900">Settings</h1>
          <p className="text-gray-600 mt-1">
            Customize your application experience and preferences
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
              {SETTINGS_TABS.map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors flex items-center space-x-2 ${
                    activeTab === tab.id
                      ? 'bg-primary-100 text-primary-700'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                  }`}
                >
                  <span>{tab.icon}</span>
                  <span>{tab.label}</span>
                </button>
              ))}
            </nav>
          </Card>
        </div>

        {/* Settings Content */}
        <div className="flex-1">
          <Card className="p-6">
            {settings && (
              <>
                {activeTab === 'appearance' && (
                  <AppearanceSettings
                    settings={settings.appearance}
                    onChange={(key, value) => handleSettingChange('appearance', key, value)}
                  />
                )}

                {activeTab === 'language' && (
                  <LanguageSettings
                    settings={settings.language}
                    onChange={(key, value) => handleSettingChange('language', key, value)}
                  />
                )}

                {activeTab === 'privacy' && (
                  <PrivacySettings
                    settings={settings.privacy}
                    onChange={(key, value) => handleSettingChange('privacy', key, value)}
                  />
                )}

                {activeTab === 'accessibility' && (
                  <AccessibilitySettings
                    settings={settings.accessibility}
                    onChange={(key, value) => handleSettingChange('accessibility', key, value)}
                  />
                )}
              </>
            )}
          </Card>
        </div>
      </div>

      {/* Quick Settings Preview */}
      <Card className="p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Settings Preview</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
          <div>
            <span className="font-medium text-gray-700">Theme:</span>
            <span className="ml-2 text-gray-600 capitalize">
              {settings?.appearance?.theme || 'Light'}
            </span>
          </div>
          <div>
            <span className="font-medium text-gray-700">Language:</span>
            <span className="ml-2 text-gray-600">
              {settings?.language?.locale === 'en-PH' ? 'English (Philippines)' : 
               settings?.language?.locale === 'fil-PH' ? 'Filipino' : 'English'}
            </span>
          </div>
          <div>
            <span className="font-medium text-gray-700">Timezone:</span>
            <span className="ml-2 text-gray-600">
              {settings?.language?.timezone || 'Asia/Manila'}
            </span>
          </div>
          <div>
            <span className="font-medium text-gray-700">Accessibility:</span>
            <span className="ml-2 text-gray-600">
              {settings?.accessibility?.screenReader ? 'Screen Reader' :
               settings?.accessibility?.highContrast ? 'High Contrast' :
               settings?.accessibility?.largeText ? 'Large Text' : 'Standard'}
            </span>
          </div>
        </div>
      </Card>
    </div>
  );
}
