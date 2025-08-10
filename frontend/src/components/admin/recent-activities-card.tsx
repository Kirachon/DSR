'use client';

// Recent Activities Card Component
// Displays recent system activities and events

import React from 'react';

import { Card, Button, Badge, Loading } from '@/components/ui';
import type { SystemActivity } from '@/types';

// Recent activities card props interface
interface RecentActivitiesCardProps {
  activities: SystemActivity[];
  loading: boolean;
  onRefresh: () => void;
}

// Activity type icon mapping
const getActivityIcon = (type: string) => {
  switch (type) {
    case 'USER_LOGIN':
      return '👤';
    case 'USER_LOGOUT':
      return '🚪';
    case 'SYSTEM_UPDATE':
      return '🔧';
    case 'SECURITY_ALERT':
      return '🚨';
    case 'DATA_BACKUP':
      return '💾';
    case 'ERROR':
      return '❌';
    default:
      return '📝';
  }
};

// Severity badge variant mapping
const getSeverityVariant = (severity: string) => {
  switch (severity) {
    case 'INFO':
      return 'info';
    case 'WARNING':
      return 'warning';
    case 'ERROR':
      return 'error';
    case 'SUCCESS':
      return 'success';
    default:
      return 'secondary';
  }
};

// Recent Activities Card component
export const RecentActivitiesCard: React.FC<RecentActivitiesCardProps> = ({
  activities,
  loading,
  onRefresh,
}) => {
  return (
    <Card className="p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold text-gray-900">Recent Activities</h2>
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
            <div key={activity.id} className="flex items-start space-x-3 p-3 bg-gray-50 rounded-lg">
              <div className="flex-shrink-0 text-lg">
                {getActivityIcon(activity.type)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-1">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    {activity.description}
                  </p>
                  <Badge variant={getSeverityVariant(activity.severity)} size="sm">
                    {activity.severity}
                  </Badge>
                </div>
                <div className="flex items-center text-xs text-gray-500 space-x-2">
                  <span>{activity.userId}</span>
                  <span>•</span>
                  <span>{new Date(activity.timestamp).toLocaleString()}</span>
                </div>
              </div>
            </div>
          ))}
          
          {/* View All Link */}
          <div className="text-center pt-2">
            <Button variant="outline" size="sm">
              View All Activities
            </Button>
          </div>
        </div>
      ) : (
        <div className="text-center py-8">
          <div className="text-gray-500 mb-4">
            <svg className="mx-auto h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
          </div>
          <h3 className="text-sm font-medium text-gray-900 mb-1">No recent activities</h3>
          <p className="text-sm text-gray-600">
            System activities will appear here when they occur.
          </p>
        </div>
      )}
    </Card>
  );
};
