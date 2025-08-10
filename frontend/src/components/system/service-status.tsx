'use client';

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { RefreshCw, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { checkAllServicesHealth } from '@/lib/api';

interface ServiceStatus {
  name: string;
  port: number;
  status: 'healthy' | 'unhealthy' | 'unknown';
  responseTime?: number;
  lastChecked?: string;
}

const SERVICES = [
  { name: 'Registration Service', port: 8080 },
  { name: 'Data Management Service', port: 8081 },
  { name: 'Eligibility Service', port: 8082 },
  { name: 'Interoperability Service', port: 8083 },
  { name: 'Payment Service', port: 8084 },
  { name: 'Grievance Service', port: 8085 },
  { name: 'Analytics Service', port: 8086 },
];

export function ServiceStatus() {
  const [services, setServices] = useState<ServiceStatus[]>(
    SERVICES.map(s => ({ ...s, status: 'unknown' as const }))
  );
  const [loading, setLoading] = useState(false);

  const checkServices = async () => {
    setLoading(true);
    try {
      const healthResults = await checkAllServicesHealth();
      
      const updatedServices = SERVICES.map(service => {
        const result = healthResults.find(r => r.port === service.port);
        return {
          ...service,
          status: result?.healthy ? 'healthy' : 'unhealthy' as const,
          responseTime: result?.responseTime,
          lastChecked: new Date().toISOString(),
        };
      });
      
      setServices(updatedServices);
    } catch (error) {
      console.error('Failed to check service health:', error);
      // Mark all as unknown if health check fails
      setServices(prev => prev.map(s => ({ ...s, status: 'unknown' as const })));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkServices();
    // Check every 30 seconds
    const interval = setInterval(checkServices, 30000);
    return () => clearInterval(interval);
  }, []);

  const getStatusIcon = (status: ServiceStatus['status']) => {
    switch (status) {
      case 'healthy':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'unhealthy':
        return <XCircle className="h-4 w-4 text-red-500" />;
      default:
        return <AlertCircle className="h-4 w-4 text-yellow-500" />;
    }
  };

  const getStatusBadge = (status: ServiceStatus['status']) => {
    switch (status) {
      case 'healthy':
        return <Badge variant="default" className="bg-green-100 text-green-800">Healthy</Badge>;
      case 'unhealthy':
        return <Badge variant="destructive">Unhealthy</Badge>;
      default:
        return <Badge variant="secondary">Unknown</Badge>;
    }
  };

  const healthyCount = services.filter(s => s.status === 'healthy').length;
  const totalCount = services.length;

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">
          Service Health Status ({healthyCount}/{totalCount})
        </CardTitle>
        <Button
          variant="outline"
          size="sm"
          onClick={checkServices}
          disabled={loading}
        >
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          {services.map((service) => (
            <div
              key={service.name}
              className="flex items-center justify-between p-2 rounded-lg border"
            >
              <div className="flex items-center space-x-2">
                {getStatusIcon(service.status)}
                <span className="text-sm font-medium">{service.name}</span>
                <span className="text-xs text-muted-foreground">:{service.port}</span>
              </div>
              <div className="flex items-center space-x-2">
                {service.responseTime && (
                  <span className="text-xs text-muted-foreground">
                    {service.responseTime}ms
                  </span>
                )}
                {getStatusBadge(service.status)}
              </div>
            </div>
          ))}
        </div>
        {services.some(s => s.lastChecked) && (
          <p className="text-xs text-muted-foreground mt-2">
            Last checked: {new Date(services.find(s => s.lastChecked)?.lastChecked || '').toLocaleTimeString()}
          </p>
        )}
      </CardContent>
    </Card>
  );
}
