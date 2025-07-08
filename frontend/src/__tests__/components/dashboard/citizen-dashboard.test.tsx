// Citizen Dashboard Component Tests
// Comprehensive test suite for the CitizenDashboard component

import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import React from 'react';

import '@testing-library/jest-dom';
import { CitizenDashboard } from '@/components/dashboard/citizen-dashboard';
import * as apiClients from '@/lib/api';
import { UserRole, UserStatus } from '@/types';

import { mockUser, mockAuthContext } from '../../setup';

// Mock the API clients
jest.mock('@/lib/api', () => ({
  registrationApi: {
    getMyRegistrations: jest.fn(),
  },
  eligibilityApi: {
    getHouseholdEligibility: jest.fn(),
  },
  paymentApi: {
    getPayments: jest.fn(),
  },
  grievanceApi: {
    getMyCases: jest.fn(),
  },
}));

// Mock Next.js components
jest.mock('next/link', () => {
  return ({ children, href, ...props }: any) => (
    <a href={href} {...props}>
      {children}
    </a>
  );
});

// Mock the auth context
jest.mock('@/contexts', () => ({
  useAuth: () => mockAuthContext,
}));

describe('CitizenDashboard', () => {
  const mockRegistrationApi = apiClients.registrationApi as jest.Mocked<
    typeof apiClients.registrationApi
  >;
  const mockEligibilityApi = apiClients.eligibilityApi as jest.Mocked<
    typeof apiClients.eligibilityApi
  >;
  const mockPaymentApi = apiClients.paymentApi as jest.Mocked<
    typeof apiClients.paymentApi
  >;
  const mockGrievanceApi = apiClients.grievanceApi as jest.Mocked<
    typeof apiClients.grievanceApi
  >;

  beforeEach(() => {
    jest.clearAllMocks();

    // Setup default mock responses
    mockRegistrationApi.getMyRegistrations.mockResolvedValue([]);
    mockEligibilityApi.getHouseholdEligibility.mockResolvedValue([]);
    mockPaymentApi.getPayments.mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      numberOfElements: 0,
      empty: true,
    });
    mockGrievanceApi.getCases.mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      numberOfElements: 0,
      empty: true,
    });
  });

  it('renders welcome message with user name', () => {
    render(<CitizenDashboard user={mockUser} />);

    expect(
      screen.getByText(`Welcome back, ${mockUser.firstName}!`)
    ).toBeInTheDocument();
    expect(screen.getByText('Citizen')).toBeInTheDocument();
  });

  it('displays email verification alert for unverified users', () => {
    const unverifiedUser = {
      ...mockUser,
      emailVerified: false,
      phoneVerified: false,
    };

    render(<CitizenDashboard user={unverifiedUser} />);

    expect(screen.getByText('Email Verification Required')).toBeInTheDocument();
    expect(screen.getByText('Verify now')).toBeInTheDocument();
  });

  it('does not display email verification alert for verified users', () => {
    const verifiedUser = {
      ...mockUser,
      emailVerified: true,
      phoneVerified: true,
    };

    render(<CitizenDashboard user={verifiedUser} />);

    expect(
      screen.queryByText('Email Verification Required')
    ).not.toBeInTheDocument();
  });

  it('renders status cards with correct information', async () => {
    render(<CitizenDashboard user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Profile Status')).toBeInTheDocument();
      expect(screen.getByText('Active Benefits')).toBeInTheDocument();
      expect(screen.getByText('Pending Applications')).toBeInTheDocument();
      expect(screen.getByText('Account Status')).toBeInTheDocument();
    });
  });

  it('renders quick action buttons', () => {
    render(<CitizenDashboard user={mockUser} />);

    expect(screen.getByText('Update Profile')).toBeInTheDocument();
    expect(screen.getByText('Register Household')).toBeInTheDocument();
    expect(screen.getByText('View Benefits')).toBeInTheDocument();
    expect(screen.getByText('Get Support')).toBeInTheDocument();
  });

  it('loads and displays dashboard data', async () => {
    const mockRegistrations = [
      {
        id: '1',
        status: 'APPROVED' as const,
        submittedAt: '2024-01-15T10:00:00Z',
        reviewedAt: '2024-01-15T11:00:00Z',
        approvedAt: '2024-01-15T12:00:00Z',
      },
    ];
    const mockPayments = [
      {
        id: '1',
        paymentId: 'PAY-001',
        batchId: 'BATCH-001',
        beneficiaryId: 'BEN-001',
        beneficiaryName: 'John Doe',
        program: 'DSWD-4Ps',
        amount: 1500,
        currency: 'PHP',
        paymentMethod: 'BANK_TRANSFER' as const,
        status: 'COMPLETED' as const,
        scheduledDate: '2024-01-15T10:00:00Z',
        processedDate: '2024-01-15T10:00:00Z',
        reference: 'REF-001',
        fspProvider: 'BPI',
        createdAt: '2024-01-15T09:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z',
      },
    ];

    mockRegistrationApi.getMyRegistrations.mockResolvedValue(mockRegistrations);
    mockPaymentApi.getPayments.mockResolvedValue({
      content: mockPayments,
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      numberOfElements: 1,
      empty: false,
    });

    render(<CitizenDashboard user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Registration approved')).toBeInTheDocument();
      expect(
        screen.getByText('Payment completed - ₱1,500')
      ).toBeInTheDocument();
    });
  });

  it('handles API errors gracefully', async () => {
    mockRegistrationApi.getMyRegistrations.mockRejectedValue(
      new Error('API Error')
    );

    render(<CitizenDashboard user={mockUser} />);

    await waitFor(() => {
      expect(
        screen.getByText(/Failed to load some dashboard data/)
      ).toBeInTheDocument();
    });
  });

  it('displays loading state while fetching data', () => {
    // Mock a delayed response
    mockRegistrationApi.getMyRegistrations.mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve([]), 1000))
    );

    render(<CitizenDashboard user={mockUser} />);

    // Check for loading indicators
    expect(screen.getAllByText('...')).toHaveLength(2); // Status cards show loading
  });

  it('shows no activity message when no data is available', async () => {
    render(<CitizenDashboard user={mockUser} />);

    await waitFor(() => {
      expect(
        screen.getByText('No recent activity to display')
      ).toBeInTheDocument();
      expect(
        screen.getByText('Start by registering your household')
      ).toBeInTheDocument();
    });
  });

  it('calls correct API endpoints with user data', async () => {
    const userWithPSN = { ...mockUser, psn: 'PSN123456', phoneVerified: true };

    render(<CitizenDashboard user={userWithPSN} />);

    await waitFor(() => {
      expect(mockRegistrationApi.getMyRegistrations).toHaveBeenCalledTimes(1);
      expect(mockEligibilityApi.getHouseholdEligibility).toHaveBeenCalledWith(
        'PSN123456'
      );
      expect(mockPaymentApi.getPayments).toHaveBeenCalledWith({
        beneficiaryId: userWithPSN.id,
        limit: 5,
      });
      expect(mockGrievanceApi.getCases).toHaveBeenCalledTimes(1);
    });
  });

  it('does not call eligibility API when user has no PSN', async () => {
    const userWithoutPSN = { ...mockUser, psn: undefined, phoneVerified: true };

    render(<CitizenDashboard user={userWithoutPSN} />);

    await waitFor(() => {
      expect(mockRegistrationApi.getMyRegistrations).toHaveBeenCalledTimes(1);
      expect(mockEligibilityApi.getHouseholdEligibility).not.toHaveBeenCalled();
      expect(mockPaymentApi.getPayments).toHaveBeenCalledTimes(1);
      expect(mockGrievanceApi.getCases).toHaveBeenCalledTimes(1);
    });
  });

  it('renders important information section', () => {
    render(<CitizenDashboard user={mockUser} />);

    expect(
      screen.getByText('Keep Your Information Updated')
    ).toBeInTheDocument();
    expect(
      screen.getByText(/Make sure your personal information is always current/)
    ).toBeInTheDocument();
  });

  it('has accessible navigation links', () => {
    render(<CitizenDashboard user={mockUser} />);

    const profileLink = screen.getByRole('link', { name: /Update Profile/ });
    const registrationLink = screen.getByRole('link', {
      name: /Register Household/,
    });
    const benefitsLink = screen.getByRole('link', { name: /View Benefits/ });
    const supportLink = screen.getByRole('link', { name: /Get Support/ });

    expect(profileLink).toHaveAttribute('href', '/profile');
    expect(registrationLink).toHaveAttribute('href', '/dashboard/registration');
    expect(benefitsLink).toHaveAttribute('href', '/benefits');
    expect(supportLink).toHaveAttribute('href', '/support');
  });
});
