'use client';

// Integration Status Component
// Displays real-time status of all system integrations and services

import React, { useState, useEffect } from 'react';

import { Card, Button, Badge, Alert, Loading } from '@/components/ui';
import { checkAllServicesHealth } from '@/lib/api';

interface ServiceStatus {
  name: string;
  status: 'healthy' | 'degraded' | 'down' | 'unknown';
  responseTime?: number;
  lastChecked: Date;
  error?: string;
}

interface IntegrationStatusProps {
  className?: string;
  autoRefresh?: boolean;
  refreshInterval?: number;
}

export const IntegrationStatus: React.FC<IntegrationStatusProps> = ({
  className,
  autoRefresh = true,
  refreshInterval = 30000, // 30 seconds
}) => {
  const [services, setServices] = useState<ServiceStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null);

  const checkServices = async () => {
    try {
      setLoading(true);
      const healthData = await checkAllServicesHealth();
      
      const serviceStatuses: ServiceStatus[] = [
        {
          name: 'Registration Service',
          status: healthData.registration ? 'healthy' : 'down',
          responseTime: healthData.registrationTime,
          lastChecked: new Date(),
          error: healthData.registration ? undefined : 'Service unavailable',
        },
        {
          name: 'Data Management Service',
          status: healthData.dataManagement ? 'healthy' : 'down',
          responseTime: healthData.dataManagementTime,
          lastChecked: new Date(),
          error: healthData.dataManagement ? undefined : 'Service unavailable',
        },
        {
          name: 'Payment Service',
          status: healthData.payment ? 'healthy' : 'down',
          responseTime: healthData.paymentTime,
          lastChecked: new Date(),
          error: healthData.payment ? undefined : 'Service unavailable',
        },
        {
          name: 'Analytics Service',
          status: healthData.analytics ? 'healthy' : 'down',
          responseTime: healthData.analyticsTime,
          lastChecked: new Date(),
          error: healthData.analytics ? undefined : 'Service unavailable',
        },
        {
          name: 'Eligibility Service',
          status: healthData.eligibility ? 'healthy' : 'down',
          responseTime: healthData.eligibilityTime,
          lastChecked: new Date(),
          error: healthData.eligibility ? undefined : 'Service unavailable',
        },
        {
          name: 'Interoperability Service',
          status: healthData.interoperability ? 'healthy' : 'down',
          responseTime: healthData.interoperabilityTime,
          lastChecked: new Date(),
          error: healthData.interoperability ? undefined : 'Service unavailable',
        },
        {
          name: 'Grievance Service',
          status: healthData.grievance ? 'healthy' : 'down',
          responseTime: healthData.grievanceTime,
          lastChecked: new Date(),
          error: healthData.grievance ? undefined : 'Service unavailable',
        },
      ];

      setServices(serviceStatuses);
      setLastUpdate(new Date());
    } catch (error) {
      console.error('Failed to check service health:', error);
      // Set all services as unknown if health check fails
      const unknownServices: ServiceStatus[] = [
        'Registration Service',
        'Data Management Service',
        'Payment Service',
        'Analytics Service',
        'Eligibility Service',
        'Interoperability Service',
        'Grievance Service',
      ].map(name => ({
        name,
        status: 'unknown' as const,
        lastChecked: new Date(),
        error: 'Health check failed',
      }));
      
      setServices(unknownServices);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkServices();

    if (autoRefresh) {
      const interval = setInterval(checkServices, refreshInterval);
      return () => clearInterval(interval);
    }
    return undefined;
  }, [autoRefresh, refreshInterval]);

  const getStatusBadge = (status: ServiceStatus['status']) => {
    switch (status) {
      case 'healthy':
        return <Badge variant="success" size="sm">Healthy</Badge>;
      case 'degraded':
        return <Badge variant="warning" size="sm">Degraded</Badge>;
      case 'down':
        return <Badge variant="error" size="sm">Down</Badge>;
      case 'unknown':
        return <Badge variant="neutral" size="sm">Unknown</Badge>;
      default:
        return <Badge variant="neutral" size="sm">Unknown</Badge>;
    }
  };

  const getOverallStatus = () => {
    const healthyCount = services.filter(s => s.status === 'healthy').length;
    const totalCount = services.length;
    
    if (healthyCount === totalCount) return 'All systems operational';
    if (healthyCount === 0) return 'Multiple systems down';
    return `${healthyCount}/${totalCount} systems operational`;
  };

  const getOverallStatusVariant = () => {
    const healthyCount = services.filter(s => s.status === 'healthy').length;
    const totalCount = services.length;
    
    if (healthyCount === totalCount) return 'success';
    if (healthyCount === 0) return 'error';
    return 'warning';
  };

  return (
    <Card className={className}>
      <div className="p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold text-gray-900">
            System Integration Status
          </h2>
          <div className="flex items-center space-x-3">
            {lastUpdate && (
              <span className="text-sm text-gray-500">
                Last updated: {lastUpdate.toLocaleTimeString()}
              </span>
            )}
            <Button
              variant="outline"
              size="sm"
              onClick={checkServices}
              disabled={loading}
            >
              {loading ? <Loading size="sm" /> : 'Refresh'}
            </Button>
          </div>
        </div>

        {/* Overall Status */}
        <Alert
          variant={getOverallStatusVariant()}
          className="mb-6"
          title="System Status"
          description={getOverallStatus()}
        />

        {/* Service List */}
        <div className="space-y-4">
          {services.map(service => (
            <div
              key={service.name}
              className="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
            >
              <div className="flex-1">
                <div className="flex items-center space-x-3">
                  <h3 className="font-medium text-gray-900">{service.name}</h3>
                  {getStatusBadge(service.status)}
                </div>
                {service.error && (
                  <p className="text-sm text-red-600 mt-1">{service.error}</p>
                )}
              </div>
              
              <div className="text-right">
                {service.responseTime && (
                  <p className="text-sm text-gray-600">
                    {service.responseTime}ms
                  </p>
                )}
                <p className="text-xs text-gray-500">
                  {service.lastChecked.toLocaleTimeString()}
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* Auto-refresh indicator */}
        {autoRefresh && (
          <div className="mt-4 text-center">
            <p className="text-xs text-gray-500">
              Auto-refreshing every {refreshInterval / 1000} seconds
            </p>
          </div>
        )}
      </div>
    </Card>
  );
};
