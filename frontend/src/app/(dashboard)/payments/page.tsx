'use client';

// Payments Management Page
// Main interface for managing payment disbursements and processing

import { useRouter, useSearchParams } from 'next/navigation';
import React, { useState, useEffect } from 'react';

import { FormInput, FormSelect } from '@/components/forms';
import { CreatePaymentBatchModal } from '@/components/payments/create-payment-batch-modal';
import { PaymentFilters } from '@/components/payments/payment-filters';
import { PaymentList } from '@/components/payments/payment-list';
import { Card, Button, Alert, Modal } from '@/components/ui';
import { useAuth } from '@/contexts';
import { paymentApi } from '@/lib/api';
import type { Payment, PaymentFilters as PaymentFiltersType } from '@/types';

// Payments Management Page component
export default function PaymentsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, isAuthenticated } = useAuth();

  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCreateBatchModalOpen, setIsCreateBatchModalOpen] = useState(false);
  const [filters, setFilters] = useState<PaymentFiltersType>({
    status: searchParams.get('status') || '',
    program: searchParams.get('program') || '',
    paymentMethod: searchParams.get('paymentMethod') || '',
    batchId: searchParams.get('batchId') || '',
    dateRange: {
      start: searchParams.get('startDate') || '',
      end: searchParams.get('endDate') || '',
    },
    searchQuery: searchParams.get('q') || '',
    amountRange: {
      min: parseFloat(searchParams.get('minAmount') || '0') || 0,
      max: parseFloat(searchParams.get('maxAmount') || '0') || 0,
    },
  });

  // Load payments data
  useEffect(() => {
    loadPayments();
  }, [filters]);

  // Load payments from API
  const loadPayments = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await paymentApi.getPayments(filters);
      setPayments(response.content);
    } catch (err) {
      console.error('Failed to load payments:', err);
      setError(err instanceof Error ? err.message : 'Failed to load payments');
      setPayments([]); // Set empty array when API fails
    } finally {
      setLoading(false);
    }
  };

  // Handle filter changes
  const handleFiltersChange = (newFilters: PaymentFiltersType) => {
    setFilters(newFilters);

    // Update URL parameters
    const params = new URLSearchParams();
    if (newFilters.status) params.set('status', newFilters.status);
    if (newFilters.program) params.set('program', newFilters.program);
    if (newFilters.paymentMethod)
      params.set('paymentMethod', newFilters.paymentMethod);
    if (newFilters.batchId) params.set('batchId', newFilters.batchId);
    if (newFilters.dateRange.start)
      params.set('startDate', newFilters.dateRange.start);
    if (newFilters.dateRange.end)
      params.set('endDate', newFilters.dateRange.end);
    if (newFilters.searchQuery) params.set('q', newFilters.searchQuery);
    if (newFilters.amountRange.min > 0)
      params.set('minAmount', newFilters.amountRange.min.toString());
    if (newFilters.amountRange.max > 0)
      params.set('maxAmount', newFilters.amountRange.max.toString());

    router.push(`/payments?${params.toString()}`);
  };

  // Handle payment batch creation
  const handleCreateBatch = async (batchData: any) => {
    try {
      const response = await paymentApi.createBatch(batchData);
      setIsCreateBatchModalOpen(false);

      // Reload payments to show new batch
      await loadPayments();

      console.log('Payment batch created successfully:', response.batchId);
    } catch (err) {
      console.error('Failed to create payment batch:', err);
      setError(
        err instanceof Error ? err.message : 'Failed to create payment batch'
      );
    }
  };

  // Handle payment retry
  const handleRetryPayment = async (paymentId: string) => {
    try {
      await paymentApi.retryPayment(paymentId);

      // Update local state
      setPayments(prev =>
        prev.map(payment =>
          payment.id === paymentId
            ? {
                ...payment,
                status: 'PENDING',
                updatedAt: new Date().toISOString(),
              }
            : payment
        )
      );

      console.log(`Payment ${paymentId} queued for retry`);
    } catch (err) {
      console.error('Failed to retry payment:', err);
      setError(err instanceof Error ? err.message : 'Failed to retry payment');
    }
  };

  // Handle payment cancellation
  const handleCancelPayment = async (paymentId: string) => {
    try {
      await paymentApi.cancelPayment(paymentId, 'Cancelled by user');

      // Update local state
      setPayments(prev =>
        prev.map(payment =>
          payment.id === paymentId
            ? {
                ...payment,
                status: 'CANCELLED',
                updatedAt: new Date().toISOString(),
              }
            : payment
        )
      );

      console.log(`Payment ${paymentId} cancelled`);
    } catch (err) {
      console.error('Failed to cancel payment:', err);
      setError(err instanceof Error ? err.message : 'Failed to cancel payment');
    }
  };

  // Redirect if not authenticated
  if (!isAuthenticated) {
    router.push('/auth/login');
    return null;
  }

  // Check if user has permission to view payments
  const canViewPayments =
    user?.role &&
    ['DSWD_STAFF', 'LGU_STAFF', 'SYSTEM_ADMIN'].includes(user.role);
  if (!canViewPayments) {
    return (
      <div className='max-w-2xl mx-auto p-6'>
        <Alert variant='error' title='Access Denied'>
          You don't have permission to view payments. Please contact your
          administrator.
        </Alert>
      </div>
    );
  }

  // Calculate summary statistics
  const totalPayments = payments.length;
  const totalAmount = payments.reduce(
    (sum, payment) => sum + payment.amount,
    0
  );
  const completedPayments = payments.filter(
    p => p.status === 'COMPLETED'
  ).length;
  const failedPayments = payments.filter(p => p.status === 'FAILED').length;
  const pendingPayments = payments.filter(p => p.status === 'PENDING').length;

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex justify-between items-center'>
        <div>
          <h1 className='text-3xl font-bold text-gray-900'>
            Payment Management
          </h1>
          <p className='text-gray-600 mt-1'>
            Manage payment disbursements and track payment status
          </p>
        </div>

        <div className='flex space-x-3'>
          <Button variant='outline' onClick={() => loadPayments()}>
            Refresh
          </Button>
          <Button onClick={() => setIsCreateBatchModalOpen(true)}>
            Create Payment Batch
          </Button>
        </div>
      </div>

      {/* Summary Statistics */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6'>
        <Card className='p-6'>
          <div className='flex items-center'>
            <div className='flex-shrink-0 p-3 bg-primary-100 text-primary-600 rounded-lg'>
              <svg
                className='h-6 w-6'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1'
                />
              </svg>
            </div>
            <div className='ml-4'>
              <p className='text-sm font-medium text-gray-500'>
                Total Payments
              </p>
              <p className='text-2xl font-bold text-gray-900'>
                {totalPayments}
              </p>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center'>
            <div className='flex-shrink-0 p-3 bg-success-100 text-success-600 rounded-lg'>
              <svg
                className='h-6 w-6'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
                />
              </svg>
            </div>
            <div className='ml-4'>
              <p className='text-sm font-medium text-gray-500'>Completed</p>
              <p className='text-2xl font-bold text-gray-900'>
                {completedPayments}
              </p>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center'>
            <div className='flex-shrink-0 p-3 bg-warning-100 text-warning-600 rounded-lg'>
              <svg
                className='h-6 w-6'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z'
                />
              </svg>
            </div>
            <div className='ml-4'>
              <p className='text-sm font-medium text-gray-500'>Pending</p>
              <p className='text-2xl font-bold text-gray-900'>
                {pendingPayments}
              </p>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center'>
            <div className='flex-shrink-0 p-3 bg-error-100 text-error-600 rounded-lg'>
              <svg
                className='h-6 w-6'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z'
                />
              </svg>
            </div>
            <div className='ml-4'>
              <p className='text-sm font-medium text-gray-500'>Failed</p>
              <p className='text-2xl font-bold text-gray-900'>
                {failedPayments}
              </p>
            </div>
          </div>
        </Card>

        <Card className='p-6'>
          <div className='flex items-center'>
            <div className='flex-shrink-0 p-3 bg-accent-100 text-accent-600 rounded-lg'>
              <svg
                className='h-6 w-6'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth={2}
                  d='M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z'
                />
              </svg>
            </div>
            <div className='ml-4'>
              <p className='text-sm font-medium text-gray-500'>Total Amount</p>
              <p className='text-2xl font-bold text-gray-900'>
                ₱{totalAmount.toLocaleString()}
              </p>
            </div>
          </div>
        </Card>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant='error' title='Error Loading Payments'>
          {error}
        </Alert>
      )}

      {/* Filters */}
      <Card className='p-6'>
        <PaymentFilters
          filters={filters}
          onFiltersChange={handleFiltersChange}
          loading={loading}
        />
      </Card>

      {/* Payments List */}
      <PaymentList
        payments={payments}
        loading={loading}
        onPaymentClick={paymentId => router.push(`/payments/${paymentId}`)}
        onRetryPayment={handleRetryPayment}
        onCancelPayment={handleCancelPayment}
      />

      {/* Create Payment Batch Modal */}
      <CreatePaymentBatchModal
        isOpen={isCreateBatchModalOpen}
        onClose={() => setIsCreateBatchModalOpen(false)}
        onSubmit={handleCreateBatch}
        currentUser={user}
      />
    </div>
  );
}
