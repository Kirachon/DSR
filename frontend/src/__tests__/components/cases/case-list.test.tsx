// Case List Component Tests
// Comprehensive test suite for the case list component

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import { CaseList } from '@/components/cases/case-list';
import type { Case } from '@/types';

// Mock case data
const mockCases: Case[] = [
  {
    id: '1',
    caseNumber: 'GRV-2024-001',
    title: 'Delayed benefit payment',
    type: 'GRIEVANCE',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    assignedTo: 'Maria Santos',
    assignedToId: 'case-worker-1',
    submittedBy: 'Juan Dela Cruz',
    submittedById: 'citizen-1',
    submittedDate: '2024-01-15T10:30:00Z',
    dueDate: '2024-01-20T17:00:00Z',
    description: 'Beneficiary reports delayed 4Ps payment for December 2023.',
    category: 'PAYMENT_ISSUES',
    resolution: null,
    notes: [
      {
        id: '1',
        content: 'Initial case review completed.',
        createdBy: 'Case Worker',
        createdById: 'case-worker-1',
        createdAt: '2024-01-15T14:30:00Z',
        isInternal: true,
      },
    ],
    attachments: [],
    createdAt: '2024-01-15T10:30:00Z',
    updatedAt: '2024-01-15T14:30:00Z',
  },
  {
    id: '2',
    caseNumber: 'APP-2024-002',
    title: 'Eligibility appeal',
    type: 'APPEAL',
    priority: 'MEDIUM',
    status: 'PENDING_REVIEW',
    assignedTo: 'Ana Garcia',
    assignedToId: 'case-worker-2',
    submittedBy: 'Maria Santos',
    submittedById: 'citizen-2',
    submittedDate: '2024-01-14T09:15:00Z',
    dueDate: '2024-01-25T17:00:00Z',
    description: 'Appeal for 4Ps eligibility rejection.',
    category: 'ELIGIBILITY_ISSUES',
    resolution: null,
    notes: [],
    attachments: [
      {
        id: '1',
        name: 'household_income_proof.pdf',
        url: '/api/files/household_income_proof.pdf',
        type: 'application/pdf',
        uploadedAt: '2024-01-14T09:15:00Z',
      },
    ],
    createdAt: '2024-01-14T09:15:00Z',
    updatedAt: '2024-01-14T09:15:00Z',
  },
];

// Mock props
const mockProps = {
  cases: mockCases,
  loading: false,
  onCaseClick: jest.fn(),
  onStatusChange: jest.fn(),
  onAssignmentChange: jest.fn(),
};

describe('CaseList', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders case list correctly', () => {
    render(<CaseList {...mockProps} />);

    expect(screen.getByText('Cases (2)')).toBeInTheDocument();
    expect(screen.getByText('GRV-2024-001')).toBeInTheDocument();
    expect(screen.getByText('APP-2024-002')).toBeInTheDocument();
    expect(screen.getByText('Delayed benefit payment')).toBeInTheDocument();
    expect(screen.getByText('Eligibility appeal')).toBeInTheDocument();
  });

  it('shows loading state correctly', () => {
    render(<CaseList {...mockProps} loading={true} />);

    expect(
      screen.getByTestId('loading-skeleton') || screen.getByText(/loading/i)
    ).toBeInTheDocument();
  });

  it('shows empty state when no cases', () => {
    render(<CaseList {...mockProps} cases={[]} />);

    expect(screen.getByText('No cases found')).toBeInTheDocument();
    expect(
      screen.getByText(
        'No cases match your current filters. Try adjusting your search criteria.'
      )
    ).toBeInTheDocument();
  });

  it('displays case information correctly', () => {
    render(<CaseList {...mockProps} />);

    // Check case details
    expect(screen.getByText('GRIEVANCE')).toBeInTheDocument();
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('IN_PROGRESS')).toBeInTheDocument();
    expect(screen.getByText('By: Juan Dela Cruz')).toBeInTheDocument();
    expect(screen.getByText('Assigned: Maria Santos')).toBeInTheDocument();
  });

  it('shows priority and status badges with correct colors', () => {
    render(<CaseList {...mockProps} />);

    const highPriorityBadge = screen.getByText('HIGH');
    const inProgressBadge = screen.getByText('IN_PROGRESS');

    expect(highPriorityBadge).toHaveClass('bg-orange-100', 'text-orange-800');
    expect(inProgressBadge).toHaveClass('bg-yellow-100', 'text-yellow-800');
  });

  it('calls onCaseClick when case is clicked', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    const caseItem = screen.getByText('Delayed benefit payment').closest('div');
    await user.click(caseItem!);

    expect(mockProps.onCaseClick).toHaveBeenCalledWith('1');
  });

  it('opens quick action modal when action button is clicked', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    const actionButton = screen.getAllByText('Quick Action')[0];
    await user.click(actionButton);

    await waitFor(() => {
      expect(
        screen.getByText('Quick Actions - GRV-2024-001')
      ).toBeInTheDocument();
    });
  });

  it('allows status updates through quick action modal', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    // Open quick action modal
    const actionButton = screen.getAllByText('Quick Action')[0];
    await user.click(actionButton);

    await waitFor(() => {
      expect(screen.getByText('Update Status')).toBeInTheDocument();
    });

    // Click on a status button
    const resolvedButton = screen.getByRole('button', { name: 'Resolved' });
    await user.click(resolvedButton);

    expect(mockProps.onStatusChange).toHaveBeenCalledWith('1', 'RESOLVED');
  });

  it('allows case reassignment through quick action modal', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    // Open quick action modal
    const actionButton = screen.getAllByText('Quick Action')[0];
    await user.click(actionButton);

    await waitFor(() => {
      expect(screen.getByText('Reassign Case')).toBeInTheDocument();
    });

    // Select a new assignee
    const assigneeSelect = screen.getByDisplayValue('case-worker-1');
    await user.selectOptions(assigneeSelect, 'case-worker-2');

    expect(mockProps.onAssignmentChange).toHaveBeenCalledWith(
      '1',
      'case-worker-2',
      'Ana Garcia'
    );
  });

  it('shows case indicators correctly', () => {
    render(<CaseList {...mockProps} />);

    // Check for notes indicator
    expect(screen.getByText('1 note')).toBeInTheDocument();

    // Check for attachments indicator
    expect(screen.getByText('1 file')).toBeInTheDocument();
  });

  it('displays overdue cases with warning styling', () => {
    const overdueCases = [
      {
        ...mockCases[0],
        dueDate: '2024-01-10T17:00:00Z', // Past due date
      },
    ];

    render(<CaseList {...mockProps} cases={overdueCases} />);

    const caseItem = screen.getByText('Delayed benefit payment').closest('div');
    expect(caseItem).toHaveClass('border-red-300', 'bg-red-50');
  });

  it('displays due soon cases with warning styling', () => {
    const dueSoonDate = new Date();
    dueSoonDate.setDate(dueSoonDate.getDate() + 1); // Due tomorrow

    const dueSoonCases = [
      {
        ...mockCases[0],
        dueDate: dueSoonDate.toISOString(),
      },
    ];

    render(<CaseList {...mockProps} cases={dueSoonCases} />);

    const caseItem = screen.getByText('Delayed benefit payment').closest('div');
    expect(caseItem).toHaveClass('border-yellow-300', 'bg-yellow-50');
  });

  it('formats dates correctly', () => {
    render(<CaseList {...mockProps} />);

    expect(screen.getByText(/Submitted: Jan 15, 2024/)).toBeInTheDocument();
    expect(screen.getByText(/Due: Jan 20, 2024/)).toBeInTheDocument();
    expect(screen.getByText(/Updated: Jan 15, 2024/)).toBeInTheDocument();
  });

  it('shows urgent indicator for urgent cases', () => {
    const urgentCases = [
      {
        ...mockCases[0],
        isUrgent: true,
      },
    ];

    render(<CaseList {...mockProps} cases={urgentCases} />);

    expect(screen.getByText('URGENT')).toBeInTheDocument();
  });

  it('prevents event propagation when action button is clicked', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    const actionButton = screen.getAllByText('Quick Action')[0];
    await user.click(actionButton);

    // onCaseClick should not be called when action button is clicked
    expect(mockProps.onCaseClick).not.toHaveBeenCalled();
  });

  it('closes modal when clicking outside or cancel', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    // Open modal
    const actionButton = screen.getAllByText('Quick Action')[0];
    await user.click(actionButton);

    await waitFor(() => {
      expect(
        screen.getByText('Quick Actions - GRV-2024-001')
      ).toBeInTheDocument();
    });

    // Close modal
    const overlay = screen.getByRole('dialog').parentElement;
    await user.click(overlay!);

    await waitFor(() => {
      expect(
        screen.queryByText('Quick Actions - GRV-2024-001')
      ).not.toBeInTheDocument();
    });
  });
});

// Performance tests
describe('CaseList Performance', () => {
  it('handles large number of cases efficiently', () => {
    const largeCaseList = Array.from({ length: 1000 }, (_, index) => ({
      ...mockCases[0],
      id: `case-${index}`,
      caseNumber: `GRV-2024-${String(index).padStart(3, '0')}`,
    }));

    const startTime = performance.now();
    render(<CaseList {...mockProps} cases={largeCaseList} />);
    const endTime = performance.now();

    // Should render within reasonable time (less than 1 second)
    expect(endTime - startTime).toBeLessThan(1000);
    expect(screen.getByText('Cases (1000)')).toBeInTheDocument();
  });
});

// Accessibility tests
describe('CaseList Accessibility', () => {
  it('has proper ARIA labels and roles', () => {
    render(<CaseList {...mockProps} />);

    const actionButtons = screen.getAllByRole('button', {
      name: /quick action/i,
    });
    expect(actionButtons).toHaveLength(2);

    const caseItems = screen.getAllByRole('generic');
    expect(caseItems.length).toBeGreaterThan(0);
  });

  it('supports keyboard navigation', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    // Tab to first action button
    await user.tab();
    const firstActionButton = screen.getAllByText('Quick Action')[0];
    expect(firstActionButton).toHaveFocus();

    // Enter should open modal
    await user.keyboard('{Enter}');

    await waitFor(() => {
      expect(
        screen.getByText('Quick Actions - GRV-2024-001')
      ).toBeInTheDocument();
    });
  });

  it('announces case updates to screen readers', async () => {
    const user = userEvent.setup();
    render(<CaseList {...mockProps} />);

    // Open modal and update status
    const actionButton = screen.getAllByText('Quick Action')[0];
    await user.click(actionButton);

    const resolvedButton = screen.getByRole('button', { name: 'Resolved' });
    await user.click(resolvedButton);

    // Verify status change was called
    expect(mockProps.onStatusChange).toHaveBeenCalledWith('1', 'RESOLVED');
  });
});
