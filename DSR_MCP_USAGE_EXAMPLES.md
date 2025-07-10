# DSR MCP Usage Examples
## Practical Examples for Figma MCP Integration with DSR Components

### Overview
This document provides practical examples of using Figma's Dev Mode MCP Server for DSR-specific design-to-code workflows, including prompts, expected outputs, and integration patterns.

## Example 1: Citizen Registration Form Component

### Figma Design Context
- **Component**: Multi-step registration form for citizens
- **User Role**: Citizens (primary users)
- **Design Requirements**: Simple, accessible, mobile-first
- **Figma Link**: `https://www.figma.com/file/DSR-SYSTEM?node-id=123:456`

### MCP Prompt
```
Generate a React + TypeScript component for this Figma citizen registration form:
[FIGMA_LINK]

Requirements:
- Use components from src/components/ui
- Follow DSR design system with Tailwind CSS
- Include form validation with Zod schema
- Ensure WCAG AA accessibility compliance
- Support mobile-first responsive design
- Integrate with registration service API
```

### Expected MCP Output
```typescript
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { StatusBadge } from '@/components/ui/status-badge';
import { Card } from '@/components/ui/card';

const registrationSchema = z.object({
  firstName: z.string().min(2, 'First name must be at least 2 characters'),
  lastName: z.string().min(2, 'Last name must be at least 2 characters'),
  email: z.string().email('Please enter a valid email address'),
  phoneNumber: z.string().regex(/^[0-9]{11}$/, 'Please enter a valid 11-digit phone number'),
});

type RegistrationFormData = z.infer<typeof registrationSchema>;

export function CitizenRegistrationForm() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<RegistrationFormData>({
    resolver: zodResolver(registrationSchema)
  });

  const onSubmit = async (data: RegistrationFormData) => {
    // Integration with DSR registration service
    try {
      const response = await fetch('/api/registration', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });
      // Handle response
    } catch (error) {
      console.error('Registration failed:', error);
    }
  };

  return (
    <Card className="w-full max-w-md mx-auto p-6 space-y-6">
      <div className="text-center space-y-2">
        <h1 className="text-display-md font-bold text-primary-800">
          Citizen Registration
        </h1>
        <p className="text-sm text-gray-600">
          Register for DSR benefits and services
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-2">
          <label htmlFor="firstName" className="text-sm font-medium text-gray-700">
            First Name *
          </label>
          <Input
            id="firstName"
            {...register('firstName')}
            className={errors.firstName ? 'border-error-500' : ''}
            aria-describedby={errors.firstName ? 'firstName-error' : undefined}
          />
          {errors.firstName && (
            <p id="firstName-error" className="text-xs text-error-600">
              {errors.firstName.message}
            </p>
          )}
        </div>

        <div className="space-y-2">
          <label htmlFor="lastName" className="text-sm font-medium text-gray-700">
            Last Name *
          </label>
          <Input
            id="lastName"
            {...register('lastName')}
            className={errors.lastName ? 'border-error-500' : ''}
            aria-describedby={errors.lastName ? 'lastName-error' : undefined}
          />
          {errors.lastName && (
            <p id="lastName-error" className="text-xs text-error-600">
              {errors.lastName.message}
            </p>
          )}
        </div>

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Registering...' : 'Register'}
        </Button>
      </form>

      <StatusBadge status="draft" className="mx-auto">
        Application Draft
      </StatusBadge>
    </Card>
  );
}
```

## Example 2: Staff Dashboard Component

### Figma Design Context
- **Component**: LGU Staff dashboard with application review interface
- **User Role**: LGU Staff (processing users)
- **Design Requirements**: Data-dense, efficient workflow, bulk actions
- **Figma Link**: `https://www.figma.com/file/DSR-SYSTEM?node-id=789:012`

### MCP Prompt
```
Generate a React + TypeScript dashboard component for LGU staff from this Figma design:
[FIGMA_LINK]

Requirements:
- Use DSR design system components
- Include data table with filtering and sorting
- Support bulk actions for application processing
- Integrate with data management service API
- Include status indicators and workflow controls
- Ensure responsive design for tablet and desktop
```

### Expected MCP Output
```typescript
import React, { useState, useEffect } from 'react';
import { DataTable } from '@/components/ui/data-table';
import { StatusBadge } from '@/components/ui/status-badge';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface Application {
  id: string;
  citizenName: string;
  submittedDate: string;
  status: 'submitted' | 'review' | 'approved' | 'rejected';
  priority: 'low' | 'normal' | 'high' | 'urgent';
  type: string;
}

export function LGUStaffDashboard() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [filter, setFilter] = useState<string>('all');

  const columns = [
    {
      header: 'Citizen Name',
      accessor: 'citizenName',
      cell: (value: string) => (
        <span className="font-medium text-gray-900">{value}</span>
      )
    },
    {
      header: 'Status',
      accessor: 'status',
      cell: (value: string) => (
        <StatusBadge status={value} size="sm">
          {getStatusText(value)}
        </StatusBadge>
      )
    },
    {
      header: 'Priority',
      accessor: 'priority',
      cell: (value: string) => (
        <Badge 
          variant={value === 'urgent' ? 'destructive' : 'secondary'}
          size="sm"
        >
          {value.toUpperCase()}
        </Badge>
      )
    },
    {
      header: 'Submitted',
      accessor: 'submittedDate',
      cell: (value: string) => (
        <span className="text-sm text-gray-600">
          {new Date(value).toLocaleDateString()}
        </span>
      )
    }
  ];

  const handleBulkApprove = async () => {
    try {
      await fetch('/api/applications/bulk-approve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ applicationIds: selectedIds })
      });
      // Refresh data
      fetchApplications();
      setSelectedIds([]);
    } catch (error) {
      console.error('Bulk approve failed:', error);
    }
  };

  const fetchApplications = async () => {
    try {
      const response = await fetch('/api/applications');
      const data = await response.json();
      setApplications(data);
    } catch (error) {
      console.error('Failed to fetch applications:', error);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, []);

  return (
    <div className="space-y-6 p-6">
      <div className="flex justify-between items-center">
        <h1 className="text-display-lg font-bold text-primary-800">
          Application Review Dashboard
        </h1>
        <div className="flex gap-2">
          <Button variant="secondary" size="md">
            Export Report
          </Button>
          <Button 
            variant="primary" 
            size="md"
            disabled={selectedIds.length === 0}
            onClick={handleBulkApprove}
          >
            Approve Selected ({selectedIds.length})
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-4">
          <div className="text-2xl font-bold text-primary-600">
            {applications.filter(app => app.status === 'submitted').length}
          </div>
          <div className="text-sm text-gray-600">Pending Review</div>
        </Card>
        <Card className="p-4">
          <div className="text-2xl font-bold text-dsr-processing">
            {applications.filter(app => app.status === 'review').length}
          </div>
          <div className="text-sm text-gray-600">In Review</div>
        </Card>
        <Card className="p-4">
          <div className="text-2xl font-bold text-dsr-eligible">
            {applications.filter(app => app.status === 'approved').length}
          </div>
          <div className="text-sm text-gray-600">Approved</div>
        </Card>
        <Card className="p-4">
          <div className="text-2xl font-bold text-error-600">
            {applications.filter(app => app.status === 'rejected').length}
          </div>
          <div className="text-sm text-gray-600">Rejected</div>
        </Card>
      </div>

      <Card className="p-6">
        <DataTable
          data={applications}
          columns={columns}
          selectable
          selectedIds={selectedIds}
          onSelectionChange={setSelectedIds}
          searchable
          filterable
          sortable
        />
      </Card>
    </div>
  );
}

function getStatusText(status: string): string {
  const statusMap = {
    submitted: 'Submitted',
    review: 'Under Review',
    approved: 'Approved',
    rejected: 'Rejected'
  };
  return statusMap[status] || status;
}
```

## Example 3: Design Token Extraction

### MCP Prompt for Token Extraction
```
Extract all design variables and tokens used in the DSR design system components.
Focus on:
- Color variables for government branding
- Typography scale for accessibility
- Spacing tokens for consistent layouts
- Component variants for different user roles

Provide the output in a format suitable for Tailwind CSS configuration.
```

### Expected Token Output
```typescript
// Design tokens extracted from Figma
export const dsrDesignTokens = {
  colors: {
    // Government Brand Colors
    primary: {
      50: '#eff6ff',
      100: '#dbeafe',
      200: '#bfdbfe',
      300: '#93c5fd',
      400: '#60a5fa',
      500: '#3b82f6',
      600: '#2563eb',
      700: '#1d4ed8',
      800: '#1e40af',
      900: '#1e3a8a',
      950: '#172554',
    },
    
    // DSR Status Colors
    dsr: {
      eligible: '#10b981',
      pending: '#f59e0b',
      rejected: '#ef4444',
      processing: '#3b82f6',
      completed: '#059669',
      draft: '#6b7280',
    }
  },
  
  typography: {
    fontFamily: {
      primary: ['Inter', 'system-ui', 'sans-serif'],
      mono: ['JetBrains Mono', 'Consolas', 'monospace'],
    },
    fontSize: {
      'display-sm': ['1.875rem', { lineHeight: '1.3', fontWeight: '600' }],
      'display-md': ['2.25rem', { lineHeight: '1.2', fontWeight: '600' }],
      'display-lg': ['3rem', { lineHeight: '1.1', fontWeight: '700' }],
    }
  },
  
  spacing: {
    micro: '0.25rem',    // 4px - Tight elements
    small: '0.5rem',     // 8px - Related items
    medium: '1rem',      // 16px - Standard spacing
    large: '1.5rem',     // 24px - Section spacing
  },
  
  components: {
    button: {
      variants: ['primary', 'secondary', 'eligible', 'pending', 'processing'],
      sizes: ['sm', 'md', 'lg', 'icon'],
    },
    statusBadge: {
      statuses: ['draft', 'submitted', 'review', 'approved', 'rejected'],
      priorities: ['low', 'normal', 'high', 'urgent'],
    }
  }
};
```

## Best Practices for DSR MCP Usage

### 1. Effective Prompting Patterns
```
// Good prompt structure:
"Generate [COMPONENT_TYPE] for [USER_ROLE] from Figma design: [LINK]

Requirements:
- Use DSR design system components from src/components/ui
- Follow [SPECIFIC_PATTERN] for [USER_ROLE] interfaces
- Include [SPECIFIC_FUNCTIONALITY] integration
- Ensure [ACCESSIBILITY/PERFORMANCE] requirements
- Support [RESPONSIVE_BREAKPOINTS]"
```

### 2. Component Integration Workflow
1. **Extract Design**: Use MCP to get component structure and tokens
2. **Enhance with DSR Patterns**: Add DSR-specific functionality and API integration
3. **Test Integration**: Verify component works with existing DSR services
4. **Update Documentation**: Document new component in design system

### 3. Quality Assurance Checklist
- [ ] Component follows DSR design system patterns
- [ ] Accessibility requirements met (WCAG AA)
- [ ] Responsive design works across all breakpoints
- [ ] API integration points are correct
- [ ] TypeScript interfaces are properly defined
- [ ] Error handling is implemented
- [ ] Loading states are included

### 4. Common Pitfalls and Solutions

**Issue**: Generated code doesn't match existing DSR patterns
**Solution**: Include specific DSR component references in prompts

**Issue**: Missing API integration points
**Solution**: Always specify which DSR service the component should integrate with

**Issue**: Accessibility issues in generated code
**Solution**: Explicitly mention WCAG AA compliance and screen reader support in prompts

**Issue**: Inconsistent design tokens
**Solution**: Use variable extraction first, then reference specific tokens in component generation

This guide provides practical examples for leveraging Figma MCP integration to enhance the DSR frontend while maintaining consistency with existing patterns and requirements.
