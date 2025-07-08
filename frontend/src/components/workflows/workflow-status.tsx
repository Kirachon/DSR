'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { 
  UserPlus, 
  FileCheck, 
  CreditCard, 
  MessageSquare, 
  BarChart3, 
  Settings,
  CheckCircle,
  Clock,
  XCircle
} from 'lucide-react';

interface WorkflowStep {
  id: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  status: 'completed' | 'in_progress' | 'pending' | 'failed';
  service: string;
  dependencies?: string[];
}

const WORKFLOW_STEPS: WorkflowStep[] = [
  {
    id: 'registration',
    name: 'Citizen Registration',
    description: 'Register new households and citizens',
    icon: <UserPlus className="h-4 w-4" />,
    status: 'completed',
    service: 'Registration Service',
  },
  {
    id: 'data_management',
    name: 'Data Validation',
    description: 'Validate and process citizen data',
    icon: <FileCheck className="h-4 w-4" />,
    status: 'completed',
    service: 'Data Management Service',
    dependencies: ['registration'],
  },
  {
    id: 'eligibility',
    name: 'Eligibility Assessment',
    description: 'Assess program eligibility',
    icon: <CheckCircle className="h-4 w-4" />,
    status: 'completed',
    service: 'Eligibility Service',
    dependencies: ['data_management'],
  },
  {
    id: 'payment',
    name: 'Payment Processing',
    description: 'Process benefit payments',
    icon: <CreditCard className="h-4 w-4" />,
    status: 'completed',
    service: 'Payment Service',
    dependencies: ['eligibility'],
  },
  {
    id: 'grievance',
    name: 'Case Management',
    description: 'Handle grievances and cases',
    icon: <MessageSquare className="h-4 w-4" />,
    status: 'completed',
    service: 'Grievance Service',
    dependencies: ['registration'],
  },
  {
    id: 'interoperability',
    name: 'External Integration',
    description: 'Integrate with external systems',
    icon: <Settings className="h-4 w-4" />,
    status: 'failed',
    service: 'Interoperability Service',
    dependencies: ['data_management'],
  },
  {
    id: 'analytics',
    name: 'Analytics & Reporting',
    description: 'Generate reports and analytics',
    icon: <BarChart3 className="h-4 w-4" />,
    status: 'failed',
    service: 'Analytics Service',
    dependencies: ['payment', 'grievance'],
  },
];

export function WorkflowStatus() {
  const getStatusIcon = (status: WorkflowStep['status']) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'in_progress':
        return <Clock className="h-4 w-4 text-blue-500" />;
      case 'failed':
        return <XCircle className="h-4 w-4 text-red-500" />;
      default:
        return <Clock className="h-4 w-4 text-gray-400" />;
    }
  };

  const getStatusBadge = (status: WorkflowStep['status']) => {
    switch (status) {
      case 'completed':
        return <Badge variant="default" className="bg-green-100 text-green-800">Completed</Badge>;
      case 'in_progress':
        return <Badge variant="default" className="bg-blue-100 text-blue-800">In Progress</Badge>;
      case 'failed':
        return <Badge variant="destructive">Failed</Badge>;
      default:
        return <Badge variant="secondary">Pending</Badge>;
    }
  };

  const completedSteps = WORKFLOW_STEPS.filter(step => step.status === 'completed').length;
  const totalSteps = WORKFLOW_STEPS.length;
  const progressPercentage = (completedSteps / totalSteps) * 100;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>End-to-End Workflow Status</span>
          <span className="text-sm font-normal">
            {completedSteps}/{totalSteps} Services
          </span>
        </CardTitle>
        <Progress value={progressPercentage} className="w-full" />
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {WORKFLOW_STEPS.map((step, index) => (
            <div
              key={step.id}
              className="flex items-center space-x-4 p-3 rounded-lg border"
            >
              <div className="flex items-center space-x-2">
                <span className="text-sm text-muted-foreground w-6">
                  {index + 1}.
                </span>
                {step.icon}
                {getStatusIcon(step.status)}
              </div>
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <h4 className="text-sm font-medium">{step.name}</h4>
                  {getStatusBadge(step.status)}
                </div>
                <p className="text-xs text-muted-foreground">{step.description}</p>
                <p className="text-xs text-muted-foreground">Service: {step.service}</p>
                {step.dependencies && (
                  <p className="text-xs text-muted-foreground">
                    Depends on: {step.dependencies.join(', ')}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
        
        <div className="mt-4 p-3 bg-muted rounded-lg">
          <h4 className="text-sm font-medium mb-2">Workflow Summary</h4>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-muted-foreground">Completed:</span>
              <span className="ml-2 font-medium text-green-600">{completedSteps}</span>
            </div>
            <div>
              <span className="text-muted-foreground">Failed:</span>
              <span className="ml-2 font-medium text-red-600">
                {WORKFLOW_STEPS.filter(s => s.status === 'failed').length}
              </span>
            </div>
            <div>
              <span className="text-muted-foreground">Progress:</span>
              <span className="ml-2 font-medium">{Math.round(progressPercentage)}%</span>
            </div>
            <div>
              <span className="text-muted-foreground">Status:</span>
              <span className="ml-2 font-medium">
                {progressPercentage === 100 ? 'Complete' : 'Partial'}
              </span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
