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
  const [settings, setSettings] = useState<SystemSettings | null>(null);
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

      const response = await registrationApi.getSystemSettings();
      setSettings(response);
    } catch (err) {
      console.error('Failed to load settings:', err);
      setError('Failed to load settings. Please try again.');
      
      // Fallback to mock data for development
      setSettings({
        general: {
          systemName: 'Dynamic Social Registry',
          systemDescription: 'Philippine Government Social Registry System',
          defaultLanguage: 'en',
          timezone: 'Asia/Manila',
          maintenanceMode: false,
          registrationEnabled: true,
          maxFileUploadSize: 10, // MB
        },
        authentication: {
          sessionTimeout: 30, // minutes
          passwordMinLength: 8,
          passwordRequireSpecialChars: true,
          passwordRequireNumbers: true,
          passwordRequireUppercase: true,
          maxLoginAttempts: 5,
          lockoutDuration: 15, // minutes
          twoFactorEnabled: false,
          ssoEnabled: false,
        },
        notifications: {
          emailNotificationsEnabled: true,
          smsNotificationsEnabled: true,
          pushNotificationsEnabled: false,
          emailFromAddress: 'noreply@dsr.gov.ph',
          emailFromName: 'DSR System',
          smsProvider: 'GLOBE',
          notificationRetryAttempts: 3,
        },
        security: {
          encryptionEnabled: true,
          auditLoggingEnabled: true,
          ipWhitelistEnabled: false,
          allowedIpRanges: [],
          securityHeadersEnabled: true,
          corsEnabled: true,
          allowedOrigins: ['https://dsr.gov.ph'],
        },
        integrations: {
          philsysEnabled: true,
          philsysApiUrl: 'https://api.philsys.gov.ph',
          philsysTimeout: 30, // seconds
          bankingIntegrationEnabled: true,
          paymentGatewayProvider: 'GCASH',
          externalApiTimeout: 30, // seconds
        },
        maintenance: {
          backupEnabled: true,
          backupFrequency: 'DAILY',
          backupRetentionDays: 30,
          logRetentionDays: 90,
          performanceMonitoringEnabled: true,
          healthCheckInterval: 5, // minutes
        },
      } as SystemSettings);
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
        ...prev![category as keyof SystemSettings],
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

      await registrationApi.updateSystemSettings(settings);
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
      await registrationApi.resetSystemSettings();
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
            {settings && (
              <SettingsSection
                category={activeCategory}
                settings={settings[activeCategory]}
                onChange={(key, value) => handleSettingChange(activeCategory, key, value)}
              />
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
