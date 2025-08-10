'use client';

// System Health Card Component
// Displays the health status of all system services

import React from 'react';

import { Card, Button, Badge, Loading } from '@/components/ui';

// System health card props interface
interface SystemHealthCardProps {
  serviceHealth: Record<string, boolean>;
  loading: boolean;
  onRefresh: () => void;
}

// Service configuration
const SERVICES = [
  { key: 'registration', name: 'Registration Service', port: 8080 },
  { key: 'dataManagement', name: 'Data Management Service', port: 8081 },
  { key: 'eligibility', name: 'Eligibility Service', port: 8082 },
  { key: 'interoperability', name: 'Interoperability Service', port: 8083 },
  { key: 'payment', name: 'Payment Service', port: 8084 },
  { key: 'grievance', name: 'Grievance Service', port: 8085 },
  { key: 'analytics', name: 'Analytics Service', port: 8086 },
];

// System Health Card component
export const SystemHealthCard: React.FC<SystemHealthCardProps> = ({
  serviceHealth,
  loading,
  onRefresh,
}) => {
  // Calculate overall health
  const totalServices = SERVICES.length;
  const healthyServices = SERVICES.filter(service => serviceHealth[service.key]).length;
  const healthPercentage = totalServices > 0 ? (healthyServices / totalServices) * 100 : 0;

  // Get overall status
  const getOverallStatus = () => {
    if (healthPercentage === 100) return { status: 'Healthy', variant: 'success' as const };
    if (healthPercentage >= 80) return { status: 'Warning', variant: 'warning' as const };
    if (healthPercentage >= 50) return { status: 'Degraded', variant: 'error' as const };
    return { status: 'Critical', variant: 'error' as const };
  };

  const overallStatus = getOverallStatus();

  return (
    <Card className="p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold text-gray-900">System Health</h2>
        <Button variant="outline" size="sm" onClick={onRefresh} disabled={loading}>
          {loading ? 'Checking...' : 'Refresh'}
        </Button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-8">
          <Loading text="Checking system health..." />
        </div>
      ) : (
        <>
          {/* Overall Status */}
          <div className="mb-6">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-700">Overall Status</span>
              <Badge variant={overallStatus.variant}>{overallStatus.status}</Badge>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className={`h-2 rounded-full transition-all duration-300 ${
                  healthPercentage === 100 ? 'bg-green-500' :
                  healthPercentage >= 80 ? 'bg-yellow-500' :
                  healthPercentage >= 50 ? 'bg-orange-500' : 'bg-red-500'
                }`}
                style={{ width: `${healthPercentage}%` }}
              />
            </div>
            <div className="flex justify-between text-xs text-gray-600 mt-1">
              <span>{healthyServices} of {totalServices} services healthy</span>
              <span>{healthPercentage.toFixed(0)}%</span>
            </div>
          </div>

          {/* Service List */}
          <div className="space-y-3">
            <h3 className="text-sm font-medium text-gray-700">Service Status</h3>
            {SERVICES.map((service) => {
              const isHealthy = serviceHealth[service.key];
              return (
                <div key={service.key} className="flex items-center justify-between py-2 px-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center">
                    <div className={`w-3 h-3 rounded-full mr-3 ${
                      isHealthy ? 'bg-green-500' : 'bg-red-500'
                    }`} />
                    <div>
                      <div className="text-sm font-medium text-gray-900">
                        {service.name}
                      </div>
                      <div className="text-xs text-gray-500">
                        Port {service.port}
                      </div>
                    </div>
                  </div>
                  <Badge variant={isHealthy ? 'success' : 'error'}>
                    {isHealthy ? 'Online' : 'Offline'}
                  </Badge>
                </div>
              );
            })}
          </div>

          {/* Health Summary */}
          <div className="mt-6 pt-4 border-t border-gray-200">
            <div className="grid grid-cols-2 gap-4 text-center">
              <div>
                <div className="text-2xl font-bold text-green-600">{healthyServices}</div>
                <div className="text-xs text-gray-600">Healthy Services</div>
              </div>
              <div>
                <div className="text-2xl font-bold text-red-600">{totalServices - healthyServices}</div>
                <div className="text-xs text-gray-600">Unhealthy Services</div>
              </div>
            </div>
          </div>

          {/* Last Updated */}
          <div className="mt-4 text-xs text-gray-500 text-center">
            Last checked: {new Date().toLocaleTimeString()}
          </div>
        </>
      )}
    </Card>
  );
};
