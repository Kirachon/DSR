'use client';

// Activity Log Component
// Component for displaying user activity history

import React from 'react';

import { Button, Badge, Loading } from '@/components/ui';
import type { UserActivity } from '@/types';

// Activity log props interface
interface ActivityLogProps {
  activities: UserActivity[];
  loading: boolean;
  onRefresh: () => void;
}

// Activity type icon mapping
const getActivityIcon = (type: string) => {
  switch (type) {
    case 'LOGIN':
      return '🔑';
    case 'LOGOUT':
      return '🚪';
    case 'PROFILE_UPDATE':
      return '✏️';
    case 'PASSWORD_CHANGE':
      return '🔒';
    case 'SETTINGS_UPDATE':
      return '⚙️';
    case 'DATA_ACCESS':
      return '📄';
    default:
      return '📝';
  }
};

// Activity Log component
export const ActivityLog: React.FC<ActivityLogProps> = ({
  activities,
  loading,
  onRefresh,
}) => {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-medium text-gray-900">Activity Log</h3>
          <p className="text-sm text-gray-600">
            Recent activity on your account
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={onRefresh} disabled={loading}>
          {loading ? 'Loading...' : 'Refresh'}
        </Button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-8">
          <Loading text="Loading activities..." />
        </div>
      ) : activities.length > 0 ? (
        <div className="space-y-4">
          {activities.map((activity) => (
            <div key={activity.id} className="bg-gray-50 p-4 rounded-lg">
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0 text-lg">
                  {getActivityIcon(activity.type)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-sm font-medium text-gray-900">
                      {activity.description}
                    </p>
                    <Badge variant="secondary" size="sm">
                      {activity.type}
                    </Badge>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-2 text-xs text-gray-500">
                    <div>
                      <span className="font-medium">Time:</span> {new Date(activity.timestamp).toLocaleString()}
                    </div>
                    {activity.ipAddress && (
                      <div>
                        <span className="font-medium">IP:</span> {activity.ipAddress}
                      </div>
                    )}
                    {activity.userAgent && (
                      <div>
                        <span className="font-medium">Device:</span> {activity.userAgent.split(' ')[0]}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-8">
          <div className="text-gray-500 mb-4">
            <svg className="mx-auto h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
          </div>
          <h3 className="text-sm font-medium text-gray-900 mb-1">No activity found</h3>
          <p className="text-sm text-gray-600">
            Your account activity will appear here.
          </p>
        </div>
      )}
    </div>
  );
};
