# DSR Enhanced Design System Documentation
## Comprehensive Guide to the Enhanced DSR Frontend Component Library

### Overview
This document provides comprehensive documentation for the enhanced DSR design system, featuring new components developed through Figma MCP integration and systematic design-to-code workflows. The enhanced system maintains backward compatibility while introducing powerful new capabilities for government service delivery.

## Enhanced Component Library

### 1. StatusBadge Component (Enhanced)
**Purpose**: Display application and process statuses with government-appropriate styling and accessibility features.

#### Features
- **DSR-Specific Status Types**: Eligible, pending, processing, completed, rejected, draft
- **Payment Status Support**: Payment-pending, payment-processing, payment-completed, payment-failed
- **Case Status Integration**: Case-new, case-assigned, case-in-progress, case-resolved, case-closed
- **Accessibility Compliance**: WCAG AA compliant with proper ARIA labels and screen reader support
- **Animation Support**: Status change animations and pulse effects for processing states

#### Usage Examples
```typescript
import { DSRStatusBadge } from '@/components/ui';

// Basic usage
<DSRStatusBadge status="eligible">Eligible for Benefits</DSRStatusBadge>

// With priority and animation
<DSRStatusBadge 
  status="processing" 
  priority="high" 
  pulse={true}
  showIcon={true}
>
  Processing Application
</DSRStatusBadge>

// Payment status
<DSRStatusBadge status="payment-completed" size="lg">
  Payment Completed
</DSRStatusBadge>
```

#### Props Interface
```typescript
interface StatusBadgeProps {
  status: 'draft' | 'submitted' | 'review' | 'approved' | 'rejected' | 
          'eligible' | 'pending' | 'processing' | 'completed' |
          'payment-pending' | 'payment-processing' | 'payment-completed' | 'payment-failed' |
          'case-new' | 'case-assigned' | 'case-in-progress' | 'case-resolved' | 'case-closed';
  size?: 'sm' | 'md' | 'lg';
  style?: 'solid' | 'outline' | 'soft';
  priority?: 'low' | 'normal' | 'high' | 'urgent';
  icon?: React.ReactNode;
  showIcon?: boolean;
  pulse?: boolean;
}
```

### 2. ProgressIndicator Component
**Purpose**: Visualize multi-step processes for citizen registration, staff workflows, and administrative tasks.

#### Features
- **Multiple Variants**: Linear, stepped, and circular progress indicators
- **Interactive Steps**: Clickable steps with navigation support
- **Status Management**: Pending, current, completed, error, and skipped states
- **Responsive Design**: Optimized for mobile, tablet, and desktop
- **Accessibility**: Full keyboard navigation and screen reader support

#### Usage Examples
```typescript
import { ProgressIndicator } from '@/components/ui';

// Citizen registration workflow
const registrationSteps = [
  { id: '1', label: 'Personal Info', status: 'completed' },
  { id: '2', label: 'Household Details', status: 'current' },
  { id: '3', label: 'Documents', status: 'pending' },
  { id: '4', label: 'Review', status: 'pending' },
];

<ProgressIndicator
  steps={registrationSteps}
  variant="stepped"
  showLabels={true}
  showDescriptions={true}
  clickable={true}
  onStepClick={(index, step) => navigateToStep(index)}
/>

// Circular progress for dashboard
<ProgressIndicator
  steps={applicationSteps}
  variant="circular"
  size="lg"
/>
```

### 3. DataTable Component
**Purpose**: Advanced data management for staff interfaces with filtering, sorting, and bulk operations.

#### Features
- **Advanced Filtering**: Column-specific filters with multiple data types
- **Sorting Support**: Multi-column sorting with visual indicators
- **Bulk Actions**: Selection and batch operations for staff efficiency
- **Pagination**: Built-in pagination with customizable page sizes
- **Search Integration**: Global search across all columns
- **Loading States**: Skeleton loading for better user experience
- **Responsive Design**: Mobile-optimized with horizontal scrolling

#### Usage Examples
```typescript
import { DataTable } from '@/components/ui';

// Staff application review table
const columns = [
  {
    key: 'citizenName',
    header: 'Citizen Name',
    accessor: 'citizenName',
    sortable: true,
    filterable: true,
  },
  {
    key: 'status',
    header: 'Status',
    accessor: 'status',
    cell: (value) => <DSRStatusBadge status={value} />,
    sortable: true,
  },
  {
    key: 'submittedDate',
    header: 'Submitted',
    accessor: 'submittedDate',
    cell: (value) => new Date(value).toLocaleDateString(),
    sortable: true,
  },
];

<DataTable
  data={applications}
  columns={columns}
  selectable={true}
  sortable={true}
  filterable={true}
  searchable={true}
  bulkActions={[
    {
      label: 'Approve Selected',
      action: (ids) => handleBulkApprove(ids),
      variant: 'primary',
    },
    {
      label: 'Reject Selected',
      action: (ids) => handleBulkReject(ids),
      variant: 'destructive',
    },
  ]}
  pagination={{
    page: currentPage,
    pageSize: 25,
    total: totalApplications,
    onPageChange: setCurrentPage,
    onPageSizeChange: setPageSize,
  }}
/>
```

### 4. RoleBasedNavigation Component
**Purpose**: Dynamic navigation system that adapts to user roles and permissions.

#### Features
- **Role-Based Access Control**: Automatic filtering based on user roles
- **Permission Integration**: Fine-grained permission checking
- **Multi-Level Navigation**: Support for nested navigation items
- **Responsive Variants**: Sidebar, horizontal, and mobile layouts
- **Badge Support**: Notification badges and status indicators
- **Accessibility**: Full keyboard navigation and ARIA support

#### Usage Examples
```typescript
import { RoleBasedNavigation, DSR_NAVIGATION_CONFIG } from '@/components/ui';

// Sidebar navigation for staff interface
<RoleBasedNavigation
  sections={DSR_NAVIGATION_CONFIG}
  userRole="LGU_STAFF"
  userPermissions={['applications.read', 'citizens.write']}
  variant="sidebar"
  showLabels={true}
  showDescriptions={true}
  showBadges={true}
  collapsible={true}
  onItemClick={(item) => trackNavigation(item)}
/>

// Horizontal navigation for citizen interface
<RoleBasedNavigation
  sections={citizenNavConfig}
  userRole="CITIZEN"
  variant="horizontal"
  showLabels={true}
  showBadges={false}
/>
```

### 5. WorkflowTimeline Component
**Purpose**: Visual timeline for tracking application progress, case management, and audit trails.

#### Features
- **Multiple Orientations**: Vertical and horizontal timeline layouts
- **Rich Event Data**: Support for actors, metadata, and attachments
- **Interactive Events**: Clickable events with custom actions
- **Status Visualization**: Color-coded status indicators with animations
- **Responsive Design**: Optimized layouts for all screen sizes
- **Accessibility**: Screen reader support and keyboard navigation

#### Usage Examples
```typescript
import { WorkflowTimeline } from '@/components/ui';

// Application processing timeline
const timelineEvents = [
  {
    id: '1',
    title: 'Application Submitted',
    description: 'Citizen submitted registration application',
    timestamp: '2024-01-15T10:30:00Z',
    status: 'completed',
    actor: {
      name: 'Juan Dela Cruz',
      role: 'Citizen',
    },
  },
  {
    id: '2',
    title: 'Document Verification',
    description: 'LGU staff reviewing submitted documents',
    timestamp: '2024-01-16T14:15:00Z',
    status: 'current',
    actor: {
      name: 'Maria Santos',
      role: 'LGU Staff',
    },
    metadata: {
      duration: '2 hours',
      location: 'Quezon City LGU',
    },
  },
];

<WorkflowTimeline
  events={timelineEvents}
  variant="detailed"
  showTimestamps={true}
  showActors={true}
  showMetadata={true}
  showActions={true}
  interactive={true}
  onEventClick={(event) => showEventDetails(event)}
/>
```

## Design Token Enhancements

### Enhanced Color System
```css
/* DSR-Specific Status Colors */
.text-dsr-eligible { color: #10b981; }
.text-dsr-pending { color: #f59e0b; }
.text-dsr-processing { color: #3b82f6; }
.text-dsr-completed { color: #059669; }
.text-dsr-rejected { color: #ef4444; }
.text-dsr-draft { color: #6b7280; }

/* Government Brand Colors */
.text-government-primary { color: #1e40af; }
.text-government-secondary { color: #059669; }
.text-government-accent { color: #dc2626; }
```

### Enhanced Typography Scale
```css
/* Display Typography for Government Headers */
.text-display-sm { font-size: 1.875rem; line-height: 1.3; font-weight: 600; }
.text-display-md { font-size: 2.25rem; line-height: 1.2; font-weight: 600; }
.text-display-lg { font-size: 3rem; line-height: 1.1; font-weight: 700; }
.text-display-xl { font-size: 3.75rem; line-height: 1; font-weight: 700; }
.text-display-2xl { font-size: 4.5rem; line-height: 1; font-weight: 800; }
```

### Enhanced Spacing System
```css
/* Semantic Spacing for Government Interfaces */
.space-micro { gap: 0.25rem; }    /* 4px - Tight elements */
.space-small { gap: 0.5rem; }     /* 8px - Related items */
.space-medium { gap: 1rem; }      /* 16px - Standard spacing */
.space-large { gap: 1.5rem; }     /* 24px - Section spacing */
.space-xl-space { gap: 2rem; }    /* 32px - Major sections */
.space-2xl-space { gap: 3rem; }   /* 48px - Page sections */
```

### Enhanced Animation System
```css
/* Status Change Animations */
.animate-status-change {
  animation: statusChange 0.4s ease-in-out;
}

.animate-pulse-slow {
  animation: pulseSlow 2s ease-in-out infinite;
}

.animate-progress-fill {
  animation: progressFill 1s ease-out;
}

/* Micro-interactions */
.animate-bounce-gentle {
  animation: bounceGentle 0.6s ease-out;
}
```

## Accessibility Enhancements

### WCAG AA Compliance Features
- **Color Contrast**: All components meet 4.5:1 contrast ratio requirements
- **Focus Management**: Visible focus indicators with high contrast
- **Keyboard Navigation**: Full keyboard accessibility for all interactive elements
- **Screen Reader Support**: Comprehensive ARIA labels and descriptions
- **Touch Targets**: Minimum 44px touch targets for mobile accessibility

### Government Accessibility Standards
- **Section 508 Compliance**: All components meet federal accessibility requirements
- **Multi-language Support**: RTL layout support and internationalization ready
- **Cognitive Accessibility**: Clear instructions and error messages
- **Motor Accessibility**: Adjustable timeouts and motion reduction support

## Performance Optimizations

### Bundle Size Management
- **Tree Shaking**: Components are individually exportable for optimal bundle sizes
- **Code Splitting**: Lazy loading support for large components like DataTable
- **CSS Optimization**: Purged unused styles in production builds

### Runtime Performance
- **Virtualization**: DataTable supports virtual scrolling for large datasets
- **Memoization**: React.memo and useMemo optimizations throughout
- **Animation Performance**: CSS transforms and GPU acceleration
- **Responsive Images**: Optimized image loading with Next.js integration

## Integration Guidelines

### API Integration Patterns
All enhanced components maintain compatibility with existing DSR API patterns:
- **Authentication**: JWT token integration preserved
- **Error Handling**: Consistent error states and recovery options
- **Loading States**: Unified loading patterns across all components
- **Data Validation**: Zod schema integration for form components

### Testing Requirements
- **Unit Tests**: 80%+ test coverage for all new components
- **Integration Tests**: API integration testing with mock services
- **Accessibility Tests**: Automated a11y testing with jest-axe
- **Visual Regression**: Playwright visual testing for design consistency

This enhanced design system provides a robust foundation for government service delivery while maintaining the high standards of accessibility, performance, and user experience required for public sector applications.
