'use client';

// System Metrics Card Component
// Displays key system metrics and performance indicators

import React from 'react';

import { Card, Loading } from '@/components/ui';
import type { SystemMetrics } from '@/types';

// System metrics card props interface
interface SystemMetricsCardProps {
  metrics: SystemMetrics | null;
  loading: boolean;
}

// System Metrics Card component
export const SystemMetricsCard: React.FC<SystemMetricsCardProps> = ({
  metrics,
  loading,
}) => {
  return (
    <Card className="p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold text-gray-900">System Metrics</h2>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-8">
          <Loading text="Loading metrics..." />
        </div>
      ) : metrics ? (
        <div className="space-y-6">
          {/* User Metrics */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-3">User Statistics</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="text-center p-3 bg-blue-50 rounded-lg">
                <div className="text-2xl font-bold text-blue-600">
                  {metrics.totalUsers?.toLocaleString() || '0'}
                </div>
                <div className="text-xs text-gray-600">Total Users</div>
              </div>
              <div className="text-center p-3 bg-green-50 rounded-lg">
                <div className="text-2xl font-bold text-green-600">
                  {metrics.activeUsers?.toLocaleString() || '0'}
                </div>
                <div className="text-xs text-gray-600">Active Users</div>
              </div>
            </div>
          </div>

          {/* System Performance */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-3">Performance</h3>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">System Uptime</span>
                <span className="text-sm font-medium text-gray-900">
                  {metrics.systemUptime?.toFixed(1) || '0'}%
                </span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className="bg-green-500 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${metrics.systemUptime || 0}%` }}
                />
              </div>

              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">Avg Response Time</span>
                <span className="text-sm font-medium text-gray-900">
                  {metrics.averageResponseTime?.toFixed(1) || '0'}s
                </span>
              </div>

              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-600">Error Rate</span>
                <span className="text-sm font-medium text-gray-900">
                  {metrics.errorRate?.toFixed(2) || '0'}%
                </span>
              </div>
            </div>
          </div>

          {/* Storage Usage */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-3">Storage</h3>
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm text-gray-600">Storage Usage</span>
              <span className="text-sm font-medium text-gray-900">
                {metrics.storageUsage?.toFixed(1) || '0'}%
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className={`h-2 rounded-full transition-all duration-300 ${
                  (metrics.storageUsage || 0) > 80 ? 'bg-red-500' :
                  (metrics.storageUsage || 0) > 60 ? 'bg-yellow-500' : 'bg-green-500'
                }`}
                style={{ width: `${metrics.storageUsage || 0}%` }}
              />
            </div>
          </div>

          {/* Data Summary */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-3">Data Summary</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="text-center p-3 bg-purple-50 rounded-lg">
                <div className="text-lg font-bold text-purple-600">
                  {metrics.totalHouseholds?.toLocaleString() || '0'}
                </div>
                <div className="text-xs text-gray-600">Households</div>
              </div>
              <div className="text-center p-3 bg-orange-50 rounded-lg">
                <div className="text-lg font-bold text-orange-600">
                  {metrics.totalPayments?.toLocaleString() || '0'}
                </div>
                <div className="text-xs text-gray-600">Payments</div>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="text-center py-8">
          <div className="text-gray-500 mb-2">
            <svg className="mx-auto h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
          <p className="text-sm text-gray-600">No metrics available</p>
        </div>
      )}
    </Card>
  );
};
