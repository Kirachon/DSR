/**
 * Enhanced DSR Components Integration Tests
 * Comprehensive testing for new components with accessibility and performance validation
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { axe, toHaveNoViolations } from 'jest-axe';

import {
  DSRStatusBadge,
  ProgressIndicator,
  DataTable,
  RoleBasedNavigation,
  WorkflowTimeline,
  DSR_NAVIGATION_CONFIG,
} from '../index';
import type { Step, TimelineEvent, Column } from '../index';

// Extend Jest matchers
expect.extend(toHaveNoViolations);

// Mock Next.js router
jest.mock('next/navigation', () => ({
  usePathname: () => '/dashboard',
}));

describe('Enhanced DSR Components', () => {
  describe('DSRStatusBadge', () => {
    it('renders with correct status styling', () => {
      render(<DSRStatusBadge status="eligible">Eligible</DSRStatusBadge>);
      
      const badge = screen.getByRole('status');
      expect(badge).toBeInTheDocument();
      expect(badge).toHaveAttribute('aria-label', 'Status: eligible');
      expect(badge).toHaveTextContent('Eligible');
    });

    it('displays correct icon for status', () => {
      render(<DSRStatusBadge status="completed" showIcon={true}>Completed</DSRStatusBadge>);
      
      const badge = screen.getByRole('status');
      expect(badge).toHaveTextContent('🎉'); // Completed icon
    });

    it('supports priority variants', () => {
      render(
        <DSRStatusBadge status="pending" priority="urgent" pulse={true}>
          Urgent Pending
        </DSRStatusBadge>
      );
      
      const badge = screen.getByRole('status');
      expect(badge).toHaveClass('ring-2', 'ring-red-400', 'animate-bounce-gentle');
    });

    it('meets accessibility standards', async () => {
      const { container } = render(
        <DSRStatusBadge status="processing" showIcon={true}>
          Processing Application
        </DSRStatusBadge>
      );
      
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });

  describe('ProgressIndicator', () => {
    const mockSteps: Step[] = [
      { id: '1', label: 'Step 1', status: 'completed' },
      { id: '2', label: 'Step 2', status: 'current' },
      { id: '3', label: 'Step 3', status: 'pending' },
    ];

    it('renders stepped variant correctly', () => {
      render(
        <ProgressIndicator
          steps={mockSteps}
          variant="stepped"
          showLabels={true}
        />
      );
      
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
      expect(screen.getByText('Step 1')).toBeInTheDocument();
      expect(screen.getByText('Step 2')).toBeInTheDocument();
      expect(screen.getByText('Step 3')).toBeInTheDocument();
    });

    it('handles step clicks when clickable', async () => {
      const user = userEvent.setup();
      const mockOnStepClick = jest.fn();
      
      render(
        <ProgressIndicator
          steps={mockSteps}
          clickable={true}
          onStepClick={mockOnStepClick}
        />
      );
      
      const step1 = screen.getByText('Step 1').closest('[role="button"]');
      await user.click(step1!);
      
      expect(mockOnStepClick).toHaveBeenCalledWith(0, mockSteps[0]);
    });

    it('supports keyboard navigation', async () => {
      const user = userEvent.setup();
      const mockOnStepClick = jest.fn();
      
      render(
        <ProgressIndicator
          steps={mockSteps}
          clickable={true}
          onStepClick={mockOnStepClick}
        />
      );
      
      const step1 = screen.getByText('Step 1').closest('[role="button"]');
      step1!.focus();
      await user.keyboard('{Enter}');
      
      expect(mockOnStepClick).toHaveBeenCalledWith(0, mockSteps[0]);
    });

    it('renders circular variant with progress percentage', () => {
      render(
        <ProgressIndicator
          steps={mockSteps}
          variant="circular"
          currentStep={1}
        />
      );
      
      expect(screen.getByText('1/3')).toBeInTheDocument();
    });

    it('meets accessibility standards', async () => {
      const { container } = render(
        <ProgressIndicator
          steps={mockSteps}
          variant="stepped"
          showLabels={true}
          showDescriptions={true}
        />
      );
      
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });

  describe('DataTable', () => {
    const mockData = [
      { id: '1', name: 'John Doe', status: 'active', age: 30 },
      { id: '2', name: 'Jane Smith', status: 'inactive', age: 25 },
      { id: '3', name: 'Bob Johnson', status: 'pending', age: 35 },
    ];

    const mockColumns: Column[] = [
      {
        key: 'name',
        header: 'Name',
        accessor: 'name',
        sortable: true,
      },
      {
        key: 'status',
        header: 'Status',
        accessor: 'status',
        cell: (value) => <DSRStatusBadge status={value as any}>{value}</DSRStatusBadge>,
      },
      {
        key: 'age',
        header: 'Age',
        accessor: 'age',
        sortable: true,
      },
    ];

    it('renders table with data correctly', () => {
      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
        />
      );
      
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
    });

    it('handles sorting when enabled', async () => {
      const user = userEvent.setup();
      const mockOnSortChange = jest.fn();
      
      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortable={true}
          onSortChange={mockOnSortChange}
        />
      );
      
      const nameHeader = screen.getByText('Name');
      await user.click(nameHeader);
      
      expect(mockOnSortChange).toHaveBeenCalledWith({
        key: 'name',
        direction: 'asc',
      });
    });

    it('handles row selection when selectable', async () => {
      const user = userEvent.setup();
      const mockOnSelectionChange = jest.fn();
      
      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          selectable={true}
          onSelectionChange={mockOnSelectionChange}
        />
      );
      
      const firstCheckbox = screen.getAllByRole('checkbox')[1]; // Skip header checkbox
      await user.click(firstCheckbox);
      
      expect(mockOnSelectionChange).toHaveBeenCalledWith(['1']);
    });

    it('handles search when searchable', async () => {
      const user = userEvent.setup();
      const mockOnSearchChange = jest.fn();
      
      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          searchable={true}
          onSearchChange={mockOnSearchChange}
        />
      );
      
      const searchInput = screen.getByPlaceholderText('Search...');
      await user.type(searchInput, 'John');
      
      expect(mockOnSearchChange).toHaveBeenCalledWith('John');
    });

    it('executes bulk actions correctly', async () => {
      const user = userEvent.setup();
      const mockBulkAction = jest.fn();
      
      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          selectable={true}
          selectedIds={['1', '2']}
          bulkActions={[
            {
              label: 'Delete Selected',
              action: mockBulkAction,
              variant: 'destructive',
            },
          ]}
        />
      );
      
      const bulkActionButton = screen.getByText('Delete Selected (2)');
      await user.click(bulkActionButton);
      
      expect(mockBulkAction).toHaveBeenCalledWith(['1', '2']);
    });

    it('meets accessibility standards', async () => {
      const { container } = render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          selectable={true}
          sortable={true}
        />
      );
      
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });

  describe('RoleBasedNavigation', () => {
    it('filters navigation items by user role', () => {
      render(
        <RoleBasedNavigation
          sections={DSR_NAVIGATION_CONFIG}
          userRole="CITIZEN"
          showLabels={true}
        />
      );
      
      // Should show citizen-specific items
      expect(screen.getByText('Registration')).toBeInTheDocument();
      expect(screen.getByText('My Profile')).toBeInTheDocument();
      
      // Should not show staff-specific items
      expect(screen.queryByText('Citizens')).not.toBeInTheDocument();
      expect(screen.queryByText('User Management')).not.toBeInTheDocument();
    });

    it('shows appropriate items for staff roles', () => {
      render(
        <RoleBasedNavigation
          sections={DSR_NAVIGATION_CONFIG}
          userRole="LGU_STAFF"
          showLabels={true}
        />
      );
      
      // Should show staff-specific items
      expect(screen.getByText('Citizens')).toBeInTheDocument();
      expect(screen.getByText('Cases')).toBeInTheDocument();
      
      // Should not show citizen-specific items
      expect(screen.queryByText('Registration')).not.toBeInTheDocument();
    });

    it('handles item clicks correctly', async () => {
      const user = userEvent.setup();
      const mockOnItemClick = jest.fn();
      
      render(
        <RoleBasedNavigation
          sections={DSR_NAVIGATION_CONFIG}
          userRole="CITIZEN"
          onItemClick={mockOnItemClick}
        />
      );
      
      const registrationLink = screen.getByText('Registration');
      await user.click(registrationLink);
      
      expect(mockOnItemClick).toHaveBeenCalled();
    });

    it('meets accessibility standards', async () => {
      const { container } = render(
        <RoleBasedNavigation
          sections={DSR_NAVIGATION_CONFIG}
          userRole="CITIZEN"
          showLabels={true}
          showDescriptions={true}
        />
      );
      
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });

  describe('WorkflowTimeline', () => {
    const mockEvents: TimelineEvent[] = [
      {
        id: '1',
        title: 'Application Submitted',
        description: 'Citizen submitted application',
        timestamp: '2024-01-15T10:30:00Z',
        status: 'completed',
        actor: { name: 'John Doe', role: 'Citizen' },
      },
      {
        id: '2',
        title: 'Under Review',
        description: 'Staff reviewing application',
        timestamp: '2024-01-16T14:15:00Z',
        status: 'current',
        actor: { name: 'Jane Smith', role: 'Staff' },
      },
    ];

    it('renders timeline events correctly', () => {
      render(
        <WorkflowTimeline
          events={mockEvents}
          showTimestamps={true}
          showActors={true}
        />
      );
      
      expect(screen.getByText('Application Submitted')).toBeInTheDocument();
      expect(screen.getByText('Under Review')).toBeInTheDocument();
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    it('handles event clicks when interactive', async () => {
      const user = userEvent.setup();
      const mockOnEventClick = jest.fn();
      
      render(
        <WorkflowTimeline
          events={mockEvents}
          interactive={true}
          onEventClick={mockOnEventClick}
        />
      );
      
      const firstEvent = screen.getByText('Application Submitted').closest('div');
      await user.click(firstEvent!);
      
      expect(mockOnEventClick).toHaveBeenCalledWith(mockEvents[0]);
    });

    it('renders different variants correctly', () => {
      const { rerender } = render(
        <WorkflowTimeline
          events={mockEvents}
          variant="compact"
        />
      );
      
      expect(screen.getByRole('log')).toHaveClass('space-y-4');
      
      rerender(
        <WorkflowTimeline
          events={mockEvents}
          variant="detailed"
        />
      );
      
      expect(screen.getByRole('log')).toHaveClass('space-y-8');
    });

    it('meets accessibility standards', async () => {
      const { container } = render(
        <WorkflowTimeline
          events={mockEvents}
          showTimestamps={true}
          showActors={true}
          showMetadata={true}
        />
      );
      
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });

  describe('Performance Tests', () => {
    it('DataTable handles large datasets efficiently', () => {
      const largeDataset = Array.from({ length: 1000 }, (_, i) => ({
        id: i.toString(),
        name: `User ${i}`,
        status: i % 2 === 0 ? 'active' : 'inactive',
        age: 20 + (i % 50),
      }));

      const startTime = performance.now();

      render(
        <DataTable
          data={largeDataset}
          columns={mockColumns}
          pagination={{
            page: 1,
            pageSize: 25,
            total: largeDataset.length,
            onPageChange: () => {},
            onPageSizeChange: () => {},
          }}
        />
      );

      const endTime = performance.now();
      const renderTime = endTime - startTime;

      // Should render within reasonable time (< 100ms)
      expect(renderTime).toBeLessThan(100);
    });

    it('ProgressIndicator handles many steps efficiently', () => {
      const manySteps: Step[] = Array.from({ length: 50 }, (_, i) => ({
        id: i.toString(),
        label: `Step ${i + 1}`,
        status: i < 25 ? 'completed' : i === 25 ? 'current' : 'pending',
      }));

      const startTime = performance.now();

      render(
        <ProgressIndicator
          steps={manySteps}
          variant="stepped"
          showLabels={true}
        />
      );

      const endTime = performance.now();
      const renderTime = endTime - startTime;

      // Should render within reasonable time (< 50ms)
      expect(renderTime).toBeLessThan(50);
    });

    it('WorkflowTimeline renders large event lists efficiently', () => {
      const manyEvents: TimelineEvent[] = Array.from({ length: 100 }, (_, i) => ({
        id: i.toString(),
        title: `Event ${i + 1}`,
        description: `Description for event ${i + 1}`,
        timestamp: new Date(Date.now() - i * 86400000).toISOString(),
        status: i === 0 ? 'current' : i < 50 ? 'completed' : 'pending',
      }));

      const startTime = performance.now();

      render(
        <WorkflowTimeline
          events={manyEvents}
          variant="compact"
          showTimestamps={true}
        />
      );

      const endTime = performance.now();
      const renderTime = endTime - startTime;

      // Should render within reasonable time (< 75ms)
      expect(renderTime).toBeLessThan(75);
    });
  });

  describe('Integration Tests', () => {
    it('components work together in complex scenarios', async () => {
      const user = userEvent.setup();

      // Test DataTable with StatusBadge integration
      const dataWithStatus = mockData.map(item => ({
        ...item,
        statusBadge: <DSRStatusBadge status={item.status as any}>{item.status}</DSRStatusBadge>
      }));

      const columnsWithBadge: Column[] = [
        ...mockColumns,
        {
          key: 'statusBadge',
          header: 'Status Badge',
          accessor: 'statusBadge',
          cell: (value) => value,
        },
      ];

      render(
        <DataTable
          data={dataWithStatus}
          columns={columnsWithBadge}
          selectable={true}
          sortable={true}
          searchable={true}
        />
      );

      // Verify status badges are rendered
      expect(screen.getAllByRole('status')).toHaveLength(3);

      // Test search functionality
      const searchInput = screen.getByPlaceholderText('Search...');
      await user.type(searchInput, 'John');

      // Should still show status badges after search
      await waitFor(() => {
        expect(screen.getByRole('status')).toBeInTheDocument();
      });
    });

    it('maintains accessibility across component interactions', async () => {
      const user = userEvent.setup();

      const { container } = render(
        <div>
          <ProgressIndicator
            steps={[
              { id: '1', label: 'Step 1', status: 'completed' },
              { id: '2', label: 'Step 2', status: 'current' },
            ]}
            clickable={true}
            onStepClick={() => {}}
          />
          <DataTable
            data={mockData}
            columns={mockColumns}
            selectable={true}
          />
        </div>
      );

      // Test keyboard navigation between components
      const progressStep = screen.getByText('Step 1').closest('[role="button"]');
      const checkbox = screen.getAllByRole('checkbox')[0];

      progressStep!.focus();
      await user.keyboard('{Tab}');
      expect(checkbox).toHaveFocus();

      // Verify no accessibility violations
      const results = await axe(container);
      expect(results).toHaveNoViolations();
    });
  });
});
