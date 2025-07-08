'use client';

// User Profile Page
// Interface for users to view and edit their personal profile information

import React, { useState, useEffect } from 'react';

import { ProfileForm } from '@/components/profile/profile-form';
import { SecuritySettings } from '@/components/profile/security-settings';
import { NotificationSettings } from '@/components/profile/notification-settings';
import { ActivityLog } from '@/components/profile/activity-log';
import { Card, Button, Alert, Badge } from '@/components/ui';
import { useAuth } from '@/contexts';
import { registrationApi } from '@/lib/api';
import type { UserProfile, UserActivity } from '@/types';

// Profile tabs
const PROFILE_TABS = [
  { id: 'profile', label: 'Profile Information', icon: '👤' },
  { id: 'security', label: 'Security', icon: '🔒' },
  { id: 'notifications', label: 'Notifications', icon: '🔔' },
  { id: 'activity', label: 'Activity Log', icon: '📋' },
] as const;

type ProfileTab = typeof PROFILE_TABS[number]['id'];

// User Profile page component
export default function UserProfilePage() {
  const { user, updateUser } = useAuth();

  // State management
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [activities, setActivities] = useState<UserActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<ProfileTab>('profile');

  // Load profile data
  useEffect(() => {
    loadProfileData();
  }, []);

  // Load profile data from API
  const loadProfileData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Load user profile
      const profileResponse = await registrationApi.getUserProfile(user?.id || '');
      setProfile(profileResponse);

      // Load user activities
      if (activeTab === 'activity') {
        const activitiesResponse = await registrationApi.getUserActivities(user?.id || '');
        setActivities(activitiesResponse);
      }
    } catch (err) {
      console.error('Failed to load profile data:', err);
      setError('Failed to load profile data. Please try again.');

      // Use basic user data from auth context when API fails
      if (user) {
        setProfile({
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          phoneNumber: user.phoneNumber || '',
          dateOfBirth: '',
          gender: '',
          address: null,
          emergencyContact: null,
          preferences: {
            language: 'en',
            timezone: 'Asia/Manila',
            emailNotifications: true,
            smsNotifications: true,
            pushNotifications: false,
          },
          securitySettings: {
            twoFactorEnabled: false,
            lastPasswordChange: '',
            loginNotifications: true,
            securityAlerts: true,
          },
          createdAt: user.createdAt || '',
          updatedAt: user.updatedAt || '',
        } as UserProfile);
      }

      setActivities([
        {
          id: '1',
          type: 'LOGIN',
          description: 'Logged in from Chrome on Windows',
          timestamp: '2024-01-25T10:30:00Z',
          ipAddress: '192.168.1.100',
          userAgent: 'Chrome/120.0.0.0',
        },
        {
          id: '2',
          type: 'PROFILE_UPDATE',
          description: 'Updated profile information',
          timestamp: '2024-01-24T14:15:00Z',
          ipAddress: '192.168.1.100',
          userAgent: 'Chrome/120.0.0.0',
        },
        {
          id: '3',
          type: 'PASSWORD_CHANGE',
          description: 'Changed account password',
          timestamp: '2024-01-20T09:45:00Z',
          ipAddress: '192.168.1.100',
          userAgent: 'Chrome/120.0.0.0',
        },
      ] as UserActivity[]);
    } finally {
      setLoading(false);
    }
  };

  // Handle profile update
  const handleProfileUpdate = async (updatedProfile: Partial<UserProfile>) => {
    try {
      setSaving(true);
      setError(null);

      const response = await registrationApi.updateUserProfile(user?.id || '', updatedProfile);
      setProfile(response);
      
      // Update auth context if basic info changed
      if (updatedProfile.firstName || updatedProfile.lastName || updatedProfile.email) {
        updateUser({
          ...user!,
          firstName: response.firstName,
          lastName: response.lastName,
          email: response.email,
        });
      }

      setSuccess('Profile updated successfully');
    } catch (err) {
      console.error('Failed to update profile:', err);
      setError('Failed to update profile. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  // Handle password change
  const handlePasswordChange = async (currentPassword: string, newPassword: string) => {
    try {
      setSaving(true);
      setError(null);

      await registrationApi.changePassword({
        currentPassword,
        newPassword,
      });

      setSuccess('Password changed successfully');
    } catch (err) {
      console.error('Failed to change password:', err);
      setError('Failed to change password. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  // Handle two-factor authentication toggle
  const handleTwoFactorToggle = async (enabled: boolean) => {
    try {
      setSaving(true);
      setError(null);

      await registrationApi.updateTwoFactorAuth(enabled);
      
      if (profile) {
        setProfile({
          ...profile,
          securitySettings: {
            ...profile.securitySettings,
            twoFactorEnabled: enabled,
          },
        });
      }

      setSuccess(`Two-factor authentication ${enabled ? 'enabled' : 'disabled'} successfully`);
    } catch (err) {
      console.error('Failed to update two-factor authentication:', err);
      setError('Failed to update two-factor authentication. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  // Handle notification settings update
  const handleNotificationUpdate = async (settings: any) => {
    try {
      setSaving(true);
      setError(null);

      await registrationApi.updateNotificationSettings(settings);
      
      if (profile) {
        setProfile({
          ...profile,
          preferences: {
            ...profile.preferences,
            ...settings,
          },
        });
      }

      setSuccess('Notification settings updated successfully');
    } catch (err) {
      console.error('Failed to update notification settings:', err);
      setError('Failed to update notification settings. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-2 text-gray-600">Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">My Profile</h1>
          <p className="text-gray-600 mt-1">
            Manage your personal information and account settings
          </p>
        </div>

        <div className="flex items-center space-x-3">
          {profile?.securitySettings?.twoFactorEnabled && (
            <Badge variant="success">2FA Enabled</Badge>
          )}
          <Button variant="outline" onClick={() => loadProfileData()}>
            Refresh
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

      <div className="flex flex-col lg:flex-row gap-6">
        {/* Profile Navigation */}
        <div className="lg:w-64">
          <Card className="p-4">
            <nav className="space-y-1">
              {PROFILE_TABS.map(tab => (
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

        {/* Profile Content */}
        <div className="flex-1">
          <Card className="p-6">
            {profile && (
              <>
                {activeTab === 'profile' && (
                  <ProfileForm
                    profile={profile}
                    onUpdate={handleProfileUpdate}
                    loading={saving}
                  />
                )}

                {activeTab === 'security' && (
                  <SecuritySettings
                    profile={profile}
                    onPasswordChange={handlePasswordChange}
                    onTwoFactorToggle={handleTwoFactorToggle}
                    loading={saving}
                  />
                )}

                {activeTab === 'notifications' && (
                  <NotificationSettings
                    settings={profile.preferences}
                    onUpdate={handleNotificationUpdate}
                    loading={saving}
                  />
                )}

                {activeTab === 'activity' && (
                  <ActivityLog
                    activities={activities}
                    loading={loading}
                    onRefresh={() => loadProfileData()}
                  />
                )}
              </>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
