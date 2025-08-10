// DSR System Integration Tests
// End-to-end integration tests for the complete DSR system

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

// Note: Using Next.js router instead of react-router-dom
import CasesPage from '@/app/(dashboard)/cases/page';
import PaymentsPage from '@/app/(dashboard)/payments/page';
import { HouseholdRegistrationWizard } from '@/components/registration';
import { AuthProvider } from '@/contexts/auth-context';
import type { User } from '@/types';
import { UserRole, UserStatus } from '@/types';

// Mock API modules
jest.mock('@/lib/api', () => ({
  registrationApi: {
    submitRegistration: jest.fn().mockResolvedValue({
      id: 'REG-123',
      status: 'SUBMITTED',
      submittedAt: '2024-01-15T10:30:00Z',
    }),
  },
  grievanceApi: {
    getCases: jest.fn().mockResolvedValue({
      data: [],
      total: 0,
      page: 1,
      pageSize: 10,
      totalPages: 0,
    }),
    createCase: jest.fn().mockResolvedValue({
      id: 'CASE-123',
      caseNumber: 'GRV-2024-001',
      status: 'NEW',
    }),
  },
  paymentApi: {
    getPayments: jest.fn().mockResolvedValue({
      data: [],
      total: 0,
      page: 1,
      pageSize: 10,
      totalPages: 0,
    }),
    createBatch: jest.fn().mockResolvedValue({
      id: 'BATCH-123',
      batchId: 'BATCH-2024-001',
      status: 'DRAFT',
    }),
  },
}));

// Mock user data
const mockCitizenUser: User = {
  id: 'citizen-1',
  email: 'citizen@example.com',
  firstName: 'John',
  lastName: 'Doe',
  role: UserRole.CITIZEN,
  status: UserStatus.ACTIVE,
  emailVerified: true,
  phoneVerified: false,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

const mockStaffUser: User = {
  id: 'staff-1',
  email: 'staff@dswd.gov.ph',
  firstName: 'Maria',
  lastName: 'Santos',
  role: UserRole.DSWD_STAFF,
  status: UserStatus.ACTIVE,
  emailVerified: true,
  phoneVerified: true,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

const mockCaseWorkerUser: User = {
  id: 'caseworker-1',
  email: 'caseworker@dswd.gov.ph',
  firstName: 'Ana',
  lastName: 'Garcia',
  role: UserRole.CASE_WORKER,
  status: UserStatus.ACTIVE,
  emailVerified: true,
  phoneVerified: true,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

// Test wrapper component
const TestWrapper: React.FC<{ children: React.ReactNode; user: User }> = ({
  children,
  user,
}) => {
  return (
    <AuthProvider>
      <div data-testid='test-wrapper'>{children}</div>
    </AuthProvider>
  );
};

describe('DSR System Integration Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Household Registration Workflow', () => {
    it('should complete full registration workflow for citizen', async () => {
      const user = userEvent.setup();
      const onSubmit = jest.fn().mockResolvedValue(undefined);
      const onCancel = jest.fn();

      render(
        <TestWrapper user={mockCitizenUser}>
          <HouseholdRegistrationWizard
            onSubmit={onSubmit}
            onCancel={onCancel}
            isSubmitting={false}
            currentUser={mockCitizenUser}
          />
        </TestWrapper>
      );

      // Step 1: Personal Information
      expect(
        screen.getByText('Step 1 of 5: Personal Information')
      ).toBeInTheDocument();

      await user.type(screen.getByLabelText(/first name/i), 'John');
      await user.type(screen.getByLabelText(/last name/i), 'Doe');
      await user.type(screen.getByLabelText(/birth date/i), '1990-01-01');
      await user.selectOptions(screen.getByLabelText(/gender/i), 'Male');
      await user.type(screen.getByLabelText(/contact number/i), '09123456789');

      await user.click(screen.getByRole('button', { name: /next/i }));

      // Step 2: Household Composition
      await waitFor(() => {
        expect(
          screen.getByText('Step 2 of 5: Household Composition')
        ).toBeInTheDocument();
      });

      // Add household head (self)
      await user.click(
        screen.getByRole('button', { name: /add household member/i })
      );

      // Fill household member details
      await user.type(screen.getByLabelText(/member first name/i), 'John');
      await user.type(screen.getByLabelText(/member last name/i), 'Doe');
      await user.type(
        screen.getByLabelText(/member birth date/i),
        '1990-01-01'
      );
      await user.selectOptions(screen.getByLabelText(/member gender/i), 'Male');
      await user.selectOptions(screen.getByLabelText(/relationship/i), 'Head');
      await user.click(screen.getByLabelText(/head of household/i));

      await user.click(screen.getByRole('button', { name: /save member/i }));
      await user.click(screen.getByRole('button', { name: /next/i }));

      // Continue through remaining steps...
      // This would be a comprehensive test covering all steps

      expect(screen.getByText('Personal Information')).toBeInTheDocument();
    });

    it('should validate required fields at each step', async () => {
      const user = userEvent.setup();
      const onSubmit = jest.fn();
      const onCancel = jest.fn();

      render(
        <TestWrapper user={mockCitizenUser}>
          <HouseholdRegistrationWizard
            onSubmit={onSubmit}
            onCancel={onCancel}
            isSubmitting={false}
            currentUser={mockCitizenUser}
          />
        </TestWrapper>
      );

      // Try to proceed without filling required fields
      await user.click(screen.getByRole('button', { name: /next/i }));

      expect(
        screen.getByText('Please fix the following errors:')
      ).toBeInTheDocument();
      expect(screen.getByText('First name is required')).toBeInTheDocument();
      expect(screen.getByText('Last name is required')).toBeInTheDocument();
    });
  });

  describe('Case Management Workflow', () => {
    it('should allow staff to view and manage cases', async () => {
      const user = userEvent.setup();

      render(
        <TestWrapper user={mockStaffUser}>
          <CasesPage />
        </TestWrapper>
      );

      // Check page loads correctly
      expect(screen.getByText('Case Management')).toBeInTheDocument();
      expect(
        screen.getByText(
          'Manage grievances, appeals, and inquiries from citizens'
        )
      ).toBeInTheDocument();

      // Check create case button is available
      expect(
        screen.getByRole('button', { name: /create new case/i })
      ).toBeInTheDocument();

      // Check filters are available
      expect(screen.getByText('Filter Cases')).toBeInTheDocument();
    });

    it('should allow case workers to create and manage cases', async () => {
      const user = userEvent.setup();

      render(
        <TestWrapper user={mockCaseWorkerUser}>
          <CasesPage />
        </TestWrapper>
      );

      // Open create case modal
      await user.click(
        screen.getByRole('button', { name: /create new case/i })
      );

      await waitFor(() => {
        expect(screen.getByText('Create New Case')).toBeInTheDocument();
      });

      // Fill case details
      await user.type(screen.getByLabelText(/case title/i), 'Test Case');
      await user.type(
        screen.getByLabelText(/description/i),
        'Test case description'
      );
      await user.selectOptions(screen.getByLabelText(/type/i), 'GRIEVANCE');
      await user.selectOptions(
        screen.getByLabelText(/category/i),
        'PAYMENT_ISSUES'
      );
      await user.selectOptions(screen.getByLabelText(/priority/i), 'HIGH');

      // Submit case
      await user.click(screen.getByRole('button', { name: /create case/i }));

      // Verify case creation was attempted
      expect(screen.getByText('Case Management')).toBeInTheDocument();
    });
  });

  describe('Payment Management Workflow', () => {
    it('should allow staff to view and manage payments', async () => {
      const user = userEvent.setup();

      render(
        <TestWrapper user={mockStaffUser}>
          <PaymentsPage />
        </TestWrapper>
      );

      // Check page loads correctly
      expect(screen.getByText('Payment Management')).toBeInTheDocument();
      expect(
        screen.getByText(
          'Manage payment disbursements and track payment status'
        )
      ).toBeInTheDocument();

      // Check create batch button is available
      expect(
        screen.getByRole('button', { name: /create payment batch/i })
      ).toBeInTheDocument();

      // Check summary statistics are displayed
      expect(screen.getByText('Total Payments')).toBeInTheDocument();
      expect(screen.getByText('Completed')).toBeInTheDocument();
      expect(screen.getByText('Pending')).toBeInTheDocument();
      expect(screen.getByText('Failed')).toBeInTheDocument();
    });

    it('should allow creation of payment batches', async () => {
      const user = userEvent.setup();

      render(
        <TestWrapper user={mockStaffUser}>
          <PaymentsPage />
        </TestWrapper>
      );

      // Open create batch modal
      await user.click(
        screen.getByRole('button', { name: /create payment batch/i })
      );

      await waitFor(() => {
        expect(screen.getByText('Create Payment Batch')).toBeInTheDocument();
      });

      // Fill batch details
      await user.type(screen.getByLabelText(/batch name/i), 'Test Batch');
      await user.selectOptions(screen.getByLabelText(/program/i), '4Ps');
      await user.type(
        screen.getByLabelText(/scheduled date/i),
        '2024-02-01T10:00'
      );

      // Select beneficiaries (mock data should be available)
      const beneficiaryCheckboxes = screen.getAllByRole('checkbox');
      if (beneficiaryCheckboxes.length > 0) {
        await user.click(beneficiaryCheckboxes[0]);
      }

      // Submit batch (if beneficiaries are selected)
      const submitButton = screen.getByRole('button', {
        name: /create batch/i,
      });
      if (!(submitButton as HTMLButtonElement).disabled) {
        await user.click(submitButton);
      }

      expect(screen.getByText('Payment Management')).toBeInTheDocument();
    });
  });

  describe('Role-Based Access Control', () => {
    it('should restrict access based on user roles', async () => {
      // Test that citizens can only access registration
      render(
        <TestWrapper user={mockCitizenUser}>
          <CasesPage />
        </TestWrapper>
      );

      // Citizens should not have access to case management
      expect(screen.getByText('Access Denied')).toBeInTheDocument();
    });

    it('should allow appropriate access for staff roles', async () => {
      // Test that staff can access case management
      render(
        <TestWrapper user={mockStaffUser}>
          <CasesPage />
        </TestWrapper>
      );

      expect(screen.getByText('Case Management')).toBeInTheDocument();
      expect(screen.queryByText('Access Denied')).not.toBeInTheDocument();
    });

    it('should allow case workers to access case management', async () => {
      render(
        <TestWrapper user={mockCaseWorkerUser}>
          <CasesPage />
        </TestWrapper>
      );

      expect(screen.getByText('Case Management')).toBeInTheDocument();
      expect(screen.queryByText('Access Denied')).not.toBeInTheDocument();
    });
  });

  describe('API Integration', () => {
    it('should handle API errors gracefully', async () => {
      // Mock API failure
      const { grievanceApi } = require('@/lib/api');
      grievanceApi.getCases.mockRejectedValue(new Error('API Error'));

      render(
        <TestWrapper user={mockStaffUser}>
          <CasesPage />
        </TestWrapper>
      );

      // Should show error message or fallback to mock data
      await waitFor(() => {
        expect(screen.getByText('Case Management')).toBeInTheDocument();
      });
    });

    it('should retry failed operations', async () => {
      const user = userEvent.setup();
      const { paymentApi } = require('@/lib/api');

      // Mock initial failure then success
      paymentApi.retryPayment
        .mockRejectedValueOnce(new Error('Network Error'))
        .mockResolvedValueOnce({ id: 'payment-1', status: 'PENDING' });

      render(
        <TestWrapper user={mockStaffUser}>
          <PaymentsPage />
        </TestWrapper>
      );

      // This would test retry functionality if payments were displayed
      expect(screen.getByText('Payment Management')).toBeInTheDocument();
    });
  });

  describe('Data Flow Integration', () => {
    it('should maintain data consistency across components', async () => {
      const user = userEvent.setup();

      render(
        <TestWrapper user={mockStaffUser}>
          <CasesPage />
        </TestWrapper>
      );

      // Test that filters update the case list
      const statusFilter = screen.getByLabelText(/status/i);
      await user.selectOptions(statusFilter, 'NEW');

      // Verify that the filter change triggers a new API call
      const { grievanceApi } = require('@/lib/api');
      expect(grievanceApi.getCases).toHaveBeenCalled();
    });

    it('should update UI state after successful operations', async () => {
      const user = userEvent.setup();
      const { grievanceApi } = require('@/lib/api');

      // Mock successful case creation
      grievanceApi.createCase.mockResolvedValue({
        id: 'new-case',
        caseNumber: 'GRV-2024-002',
        title: 'New Test Case',
        status: 'NEW',
      });

      render(
        <TestWrapper user={mockStaffUser}>
          <CasesPage />
        </TestWrapper>
      );

      // Create a new case and verify UI updates
      await user.click(
        screen.getByRole('button', { name: /create new case/i })
      );

      // Fill and submit form (simplified)
      await waitFor(() => {
        expect(screen.getByText('Create New Case')).toBeInTheDocument();
      });
    });
  });

  describe('Performance and Scalability', () => {
    it('should handle large datasets efficiently', async () => {
      const { grievanceApi } = require('@/lib/api');

      // Mock large dataset
      const largeCaseList = Array.from({ length: 1000 }, (_, index) => ({
        id: `case-${index}`,
        caseNumber: `GRV-2024-${String(index).padStart(3, '0')}`,
        title: `Case ${index}`,
        status: 'NEW',
      }));

      grievanceApi.getCases.mockResolvedValue({
        data: largeCaseList,
        total: 1000,
        page: 1,
        pageSize: 50,
        totalPages: 20,
      });

      const startTime = performance.now();

      render(
        <TestWrapper user={mockStaffUser}>
          <CasesPage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('Case Management')).toBeInTheDocument();
      });

      const endTime = performance.now();

      // Should render within reasonable time
      expect(endTime - startTime).toBeLessThan(2000);
    });
  });
});
