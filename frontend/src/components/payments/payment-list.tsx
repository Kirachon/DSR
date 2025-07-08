'use client';

// Payment List Component
// Display and manage list of payments with actions

import React, { useState } from 'react';

import { FormSelect } from '@/components/forms';
import { Card, Button, Modal } from '@/components/ui';
import type { Payment, PaymentStatus } from '@/types';

// Component props interface
interface PaymentListProps {
  payments: Payment[];
  loading?: boolean;
  onPaymentClick: (paymentId: string) => void;
  onRetryPayment: (paymentId: string) => void;
  onCancelPayment: (paymentId: string) => void;
}

// Get status color
const getStatusColor = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800';
    case 'PROCESSING':
      return 'bg-blue-100 text-blue-800';
    case 'COMPLETED':
      return 'bg-green-100 text-green-800';
    case 'FAILED':
      return 'bg-red-100 text-red-800';
    case 'CANCELLED':
      return 'bg-gray-100 text-gray-800';
    case 'REFUNDED':
      return 'bg-purple-100 text-purple-800';
    case 'ON_HOLD':
      return 'bg-orange-100 text-orange-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// Get payment method color
const getPaymentMethodColor = (method: string) => {
  switch (method) {
    case 'BANK_TRANSFER':
      return 'bg-blue-50 text-blue-700 border-blue-200';
    case 'DIGITAL_WALLET':
      return 'bg-green-50 text-green-700 border-green-200';
    case 'CASH_PICKUP':
      return 'bg-orange-50 text-orange-700 border-orange-200';
    case 'CHECK':
      return 'bg-purple-50 text-purple-700 border-purple-200';
    case 'PREPAID_CARD':
      return 'bg-indigo-50 text-indigo-700 border-indigo-200';
    default:
      return 'bg-gray-50 text-gray-700 border-gray-200';
  }
};

// Format currency
const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-PH', {
    style: 'currency',
    currency: 'PHP',
  }).format(amount);
};

// Format date
const formatDate = (dateString: string | null): string => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

// Payment List component
export const PaymentList: React.FC<PaymentListProps> = ({
  payments,
  loading = false,
  onPaymentClick,
  onRetryPayment,
  onCancelPayment,
}) => {
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
  const [isActionModalOpen, setIsActionModalOpen] = useState(false);

  // Handle payment action modal
  const handlePaymentAction = (payment: Payment) => {
    setSelectedPayment(payment);
    setIsActionModalOpen(true);
  };

  // Handle retry payment
  const handleRetry = () => {
    if (selectedPayment) {
      onRetryPayment(selectedPayment.id);
      setIsActionModalOpen(false);
      setSelectedPayment(null);
    }
  };

  // Handle cancel payment
  const handleCancel = () => {
    if (selectedPayment) {
      onCancelPayment(selectedPayment.id);
      setIsActionModalOpen(false);
      setSelectedPayment(null);
    }
  };

  if (loading) {
    return (
      <Card className='p-6'>
        <div className='animate-pulse space-y-4'>
          {[...Array(5)].map((_, index) => (
            <div key={index} className='border border-gray-200 rounded-lg p-4'>
              <div className='flex items-center justify-between mb-3'>
                <div className='h-4 bg-gray-200 rounded w-1/4'></div>
                <div className='flex space-x-2'>
                  <div className='h-6 bg-gray-200 rounded w-16'></div>
                  <div className='h-6 bg-gray-200 rounded w-20'></div>
                </div>
              </div>
              <div className='h-4 bg-gray-200 rounded w-3/4 mb-2'></div>
              <div className='h-3 bg-gray-200 rounded w-1/2'></div>
            </div>
          ))}
        </div>
      </Card>
    );
  }

  if (payments.length === 0) {
    return (
      <Card className='p-8 text-center'>
        <div className='text-gray-500'>
          <svg
            className='mx-auto h-12 w-12 text-gray-400 mb-4'
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
          <h3 className='text-lg font-medium text-gray-900 mb-2'>
            No payments found
          </h3>
          <p className='text-gray-600'>
            No payments match your current filters. Try adjusting your search
            criteria.
          </p>
        </div>
      </Card>
    );
  }

  return (
    <>
      <Card className='p-6'>
        <div className='flex justify-between items-center mb-6'>
          <h2 className='text-lg font-semibold text-gray-900'>
            Payments ({payments.length})
          </h2>
          <div className='text-sm text-gray-600'>
            Total:{' '}
            {formatCurrency(payments.reduce((sum, p) => sum + p.amount, 0))}
          </div>
        </div>

        <div className='space-y-4'>
          {payments.map(payment => (
            <div
              key={payment.id}
              className='border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer'
              onClick={() => onPaymentClick(payment.id)}
            >
              <div className='flex items-start justify-between mb-3'>
                <div className='flex-1'>
                  <div className='flex items-center space-x-3 mb-2'>
                    <h3 className='font-medium text-gray-900 text-sm'>
                      {payment.paymentId}
                    </h3>
                    <span
                      className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getPaymentMethodColor(payment.paymentMethod)}`}
                    >
                      {payment.paymentMethod.replace('_', ' ')}
                    </span>
                    <span
                      className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(payment.status)}`}
                    >
                      {payment.status}
                    </span>
                    {payment.isVerified && (
                      <span className='inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800'>
                        Verified
                      </span>
                    )}
                  </div>

                  <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-3'>
                    <div>
                      <span className='text-xs text-gray-500'>
                        Beneficiary:
                      </span>
                      <p className='font-medium text-gray-900'>
                        {payment.beneficiaryName}
                      </p>
                    </div>
                    <div>
                      <span className='text-xs text-gray-500'>Program:</span>
                      <p className='font-medium text-gray-900'>
                        {payment.program}
                      </p>
                    </div>
                    <div>
                      <span className='text-xs text-gray-500'>Amount:</span>
                      <p className='font-medium text-gray-900'>
                        {formatCurrency(payment.amount)}
                      </p>
                    </div>
                    <div>
                      <span className='text-xs text-gray-500'>
                        FSP Provider:
                      </span>
                      <p className='font-medium text-gray-900'>
                        {payment.fspProvider}
                      </p>
                    </div>
                  </div>

                  <div className='flex items-center space-x-4 text-xs text-gray-500'>
                    <span>Batch: {payment.batchId}</span>
                    <span>Scheduled: {formatDate(payment.scheduledDate)}</span>
                    {payment.processedDate && (
                      <span>
                        Processed: {formatDate(payment.processedDate)}
                      </span>
                    )}
                    <span>Reference: {payment.reference}</span>
                  </div>

                  {/* Payment Method Details */}
                  <div className='mt-2 text-xs text-gray-600'>
                    {payment.bankAccount && (
                      <span>
                        Bank: {payment.bankAccount.bankName} -{' '}
                        {payment.bankAccount.accountNumber}
                      </span>
                    )}
                    {payment.digitalWallet && (
                      <span>
                        Wallet: {payment.digitalWallet.walletType} -{' '}
                        {payment.digitalWallet.walletNumber}
                      </span>
                    )}
                    {payment.cashPickup && (
                      <span>
                        Pickup: {payment.cashPickup.location} - Code:{' '}
                        {payment.cashPickup.pickupCode}
                      </span>
                    )}
                  </div>

                  {/* Failure Reason */}
                  {payment.status === 'FAILED' && payment.failureReason && (
                    <div className='mt-2 p-2 bg-red-50 border border-red-200 rounded text-xs text-red-700'>
                      <strong>Failure Reason:</strong> {payment.failureReason}
                    </div>
                  )}

                  {/* Notes */}
                  {payment.notes && (
                    <div className='mt-2 text-xs text-gray-600'>
                      <strong>Notes:</strong> {payment.notes}
                    </div>
                  )}
                </div>

                <div className='flex items-center space-x-2 ml-4'>
                  {(payment.status === 'FAILED' ||
                    payment.status === 'PENDING') && (
                    <Button
                      size='sm'
                      variant='outline'
                      onClick={e => {
                        e.stopPropagation();
                        handlePaymentAction(payment);
                      }}
                    >
                      Actions
                    </Button>
                  )}
                </div>
              </div>

              {/* Additional indicators */}
              <div className='flex items-center justify-between'>
                <div className='flex items-center space-x-2'>
                  {payment.retryCount && payment.retryCount > 0 && (
                    <span className='inline-flex items-center text-xs text-gray-500'>
                      <svg
                        className='h-3 w-3 mr-1'
                        fill='none'
                        stroke='currentColor'
                        viewBox='0 0 24 24'
                      >
                        <path
                          strokeLinecap='round'
                          strokeLinejoin='round'
                          strokeWidth={2}
                          d='M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15'
                        />
                      </svg>
                      {payment.retryCount} retr
                      {payment.retryCount === 1 ? 'y' : 'ies'}
                    </span>
                  )}
                  {payment.fspReference && (
                    <span className='inline-flex items-center text-xs text-gray-500'>
                      <svg
                        className='h-3 w-3 mr-1'
                        fill='none'
                        stroke='currentColor'
                        viewBox='0 0 24 24'
                      >
                        <path
                          strokeLinecap='round'
                          strokeLinejoin='round'
                          strokeWidth={2}
                          d='M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
                        />
                      </svg>
                      FSP Ref: {payment.fspReference}
                    </span>
                  )}
                </div>
                <div className='text-xs text-gray-500'>
                  Updated: {formatDate(payment.updatedAt)}
                </div>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Payment Action Modal */}
      <Modal
        isOpen={isActionModalOpen}
        onClose={() => setIsActionModalOpen(false)}
        title={`Payment Actions - ${selectedPayment?.paymentId}`}
      >
        {selectedPayment && (
          <div className='space-y-6'>
            <div>
              <h4 className='font-medium text-gray-900 mb-2'>
                Payment Details
              </h4>
              <div className='bg-gray-50 rounded-lg p-4 space-y-2'>
                <div className='flex justify-between'>
                  <span className='text-sm text-gray-600'>Beneficiary:</span>
                  <span className='text-sm font-medium'>
                    {selectedPayment.beneficiaryName}
                  </span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-sm text-gray-600'>Amount:</span>
                  <span className='text-sm font-medium'>
                    {formatCurrency(selectedPayment.amount)}
                  </span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-sm text-gray-600'>Status:</span>
                  <span
                    className={`text-sm font-medium px-2 py-1 rounded-full ${getStatusColor(selectedPayment.status)}`}
                  >
                    {selectedPayment.status}
                  </span>
                </div>
                {selectedPayment.failureReason && (
                  <div className='pt-2 border-t border-gray-200'>
                    <span className='text-sm text-gray-600'>
                      Failure Reason:
                    </span>
                    <p className='text-sm text-red-600 mt-1'>
                      {selectedPayment.failureReason}
                    </p>
                  </div>
                )}
              </div>
            </div>

            <div className='space-y-3'>
              {selectedPayment.status === 'FAILED' && (
                <Button onClick={handleRetry} className='w-full'>
                  Retry Payment
                </Button>
              )}

              {(selectedPayment.status === 'PENDING' ||
                selectedPayment.status === 'PROCESSING') && (
                <Button
                  onClick={handleCancel}
                  variant='outline'
                  className='w-full'
                >
                  Cancel Payment
                </Button>
              )}

              <Button
                variant='outline'
                className='w-full'
                onClick={() => {
                  setIsActionModalOpen(false);
                  onPaymentClick(selectedPayment.id);
                }}
              >
                View Full Details
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </>
  );
};
