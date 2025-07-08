// Payment Service API Client
// API client for payment processing and disbursement

import type {
  Payment,
  PaymentBatch,
  PaymentFilters,
  CreatePaymentBatchRequest,
  PaymentSummary,
  PaymentReconciliation,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

import { apiClient } from '../api-client';

// Base URL for Payment Service
const PAYMENT_BASE_URL = '/api/v1/payments';

// Payment API client
export const paymentApi = {
  // Get all payments with filters
  getPayments: async (
    filters: PaymentFilters
  ): Promise<PaginatedResponse<Payment>> => {
    const params = new URLSearchParams();

    if (filters.status) params.append('status', filters.status);
    if (filters.paymentMethod)
      params.append('paymentMethod', filters.paymentMethod);
    if (filters.program) params.append('program', filters.program);
    if (filters.batchId) params.append('batchId', filters.batchId);
    if (filters.beneficiaryId)
      params.append('beneficiaryId', filters.beneficiaryId);
    if (filters.fspProvider) params.append('fspProvider', filters.fspProvider);
    if (filters.dateRange.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange.end) params.append('endDate', filters.dateRange.end);
    if (filters.amountRange.min > 0)
      params.append('minAmount', filters.amountRange.min.toString());
    if (filters.amountRange.max > 0)
      params.append('maxAmount', filters.amountRange.max.toString());
    if (filters.searchQuery) params.append('q', filters.searchQuery);
    if (filters.isVerified !== undefined)
      params.append('isVerified', filters.isVerified.toString());
    if (filters.isReconciled !== undefined)
      params.append('isReconciled', filters.isReconciled.toString());
    if (filters.hasFailures !== undefined)
      params.append('hasFailures', filters.hasFailures.toString());

    const response = await apiClient.get<PaginatedResponse<Payment>>(
      `${PAYMENT_BASE_URL}?${params.toString()}`
    );
    return response.data;
  },

  // Get payment by ID
  getPayment: async (paymentId: string): Promise<Payment> => {
    const response = await apiClient.get<ApiResponse<Payment>>(
      `${PAYMENT_BASE_URL}/${paymentId}`
    );
    return response.data.data;
  },

  // Create payment batch
  createBatch: async (
    batchData: CreatePaymentBatchRequest
  ): Promise<PaymentBatch> => {
    const response = await apiClient.post<ApiResponse<PaymentBatch>>(
      `${PAYMENT_BASE_URL}/batches`,
      batchData
    );
    return response.data.data;
  },

  // Get payment batches
  getBatches: async (
    filters?: Partial<PaymentFilters>
  ): Promise<PaginatedResponse<PaymentBatch>> => {
    const params = new URLSearchParams();

    if (filters?.program) params.append('program', filters.program);
    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get<PaginatedResponse<PaymentBatch>>(
      `${PAYMENT_BASE_URL}/batches?${params.toString()}`
    );
    return response.data;
  },

  // Get batch by ID
  getBatch: async (batchId: string): Promise<PaymentBatch> => {
    const response = await apiClient.get<ApiResponse<PaymentBatch>>(
      `${PAYMENT_BASE_URL}/batches/${batchId}`
    );
    return response.data.data;
  },

  // Get payments in batch
  getBatchPayments: async (batchId: string): Promise<Payment[]> => {
    const response = await apiClient.get<ApiResponse<Payment[]>>(
      `${PAYMENT_BASE_URL}/batches/${batchId}/payments`
    );
    return response.data.data;
  },

  // Approve payment batch
  approveBatch: async (
    batchId: string,
    notes?: string
  ): Promise<PaymentBatch> => {
    const response = await apiClient.post<ApiResponse<PaymentBatch>>(
      `${PAYMENT_BASE_URL}/batches/${batchId}/approve`,
      { notes }
    );
    return response.data.data;
  },

  // Process payment batch
  processBatch: async (batchId: string): Promise<PaymentBatch> => {
    const response = await apiClient.post<ApiResponse<PaymentBatch>>(
      `${PAYMENT_BASE_URL}/batches/${batchId}/process`
    );
    return response.data.data;
  },

  // Cancel payment batch
  cancelBatch: async (
    batchId: string,
    reason: string
  ): Promise<PaymentBatch> => {
    const response = await apiClient.post<ApiResponse<PaymentBatch>>(
      `${PAYMENT_BASE_URL}/batches/${batchId}/cancel`,
      { reason }
    );
    return response.data.data;
  },

  // Retry failed payment
  retryPayment: async (paymentId: string): Promise<Payment> => {
    const response = await apiClient.post<ApiResponse<Payment>>(
      `${PAYMENT_BASE_URL}/${paymentId}/retry`
    );
    return response.data.data;
  },

  // Cancel payment
  cancelPayment: async (
    paymentId: string,
    reason: string
  ): Promise<Payment> => {
    const response = await apiClient.post<ApiResponse<Payment>>(
      `${PAYMENT_BASE_URL}/${paymentId}/cancel`,
      { reason }
    );
    return response.data.data;
  },

  // Verify payment
  verifyPayment: async (
    paymentId: string,
    verificationData: any
  ): Promise<Payment> => {
    const response = await apiClient.post<ApiResponse<Payment>>(
      `${PAYMENT_BASE_URL}/${paymentId}/verify`,
      verificationData
    );
    return response.data.data;
  },

  // Get payment summary
  getPaymentSummary: async (
    filters?: Partial<PaymentFilters>
  ): Promise<PaymentSummary> => {
    const params = new URLSearchParams();

    if (filters?.dateRange?.start)
      params.append('startDate', filters.dateRange.start);
    if (filters?.dateRange?.end)
      params.append('endDate', filters.dateRange.end);
    if (filters?.program) params.append('program', filters.program);

    const response = await apiClient.get<ApiResponse<PaymentSummary>>(
      `${PAYMENT_BASE_URL}/summary?${params.toString()}`
    );
    return response.data.data;
  },

  // Get failed payments
  getFailedPayments: async (): Promise<Payment[]> => {
    const response = await apiClient.get<ApiResponse<Payment[]>>(
      `${PAYMENT_BASE_URL}/failed`
    );
    return response.data.data;
  },

  // Get pending payments
  getPendingPayments: async (): Promise<Payment[]> => {
    const response = await apiClient.get<ApiResponse<Payment[]>>(
      `${PAYMENT_BASE_URL}/pending`
    );
    return response.data.data;
  },

  // Bulk retry payments
  bulkRetryPayments: async (paymentIds: string[]): Promise<Payment[]> => {
    const response = await apiClient.post<ApiResponse<Payment[]>>(
      `${PAYMENT_BASE_URL}/bulk-retry`,
      { paymentIds }
    );
    return response.data.data;
  },

  // Reconcile payments
  reconcilePayments: async (
    batchId: string,
    reconciliationData: any
  ): Promise<PaymentReconciliation> => {
    const response = await apiClient.post<ApiResponse<PaymentReconciliation>>(
      `${PAYMENT_BASE_URL}/batches/${batchId}/reconcile`,
      reconciliationData
    );
    return response.data.data;
  },

  // Get reconciliation report
  getReconciliationReport: async (
    batchId: string
  ): Promise<PaymentReconciliation> => {
    const response = await apiClient.get<ApiResponse<PaymentReconciliation>>(
      `${PAYMENT_BASE_URL}/batches/${batchId}/reconciliation`
    );
    return response.data.data;
  },

  // Export payments
  exportPayments: async (
    filters: PaymentFilters,
    format: 'CSV' | 'EXCEL' | 'PDF'
  ): Promise<Blob> => {
    const params = new URLSearchParams();
    params.append('format', format);

    if (filters.status) params.append('status', filters.status);
    if (filters.program) params.append('program', filters.program);
    if (filters.dateRange.start)
      params.append('startDate', filters.dateRange.start);
    if (filters.dateRange.end) params.append('endDate', filters.dateRange.end);

    const response = await apiClient.get(
      `${PAYMENT_BASE_URL}/export?${params.toString()}`,
      { responseType: 'blob' }
    );
    return response.data;
  },

  // Get payment status history
  getPaymentStatusHistory: async (paymentId: string): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(
      `${PAYMENT_BASE_URL}/${paymentId}/status-history`
    );
    return response.data.data;
  },

  // Get FSP providers
  getFSPProviders: async (): Promise<any[]> => {
    const response = await apiClient.get<ApiResponse<any[]>>(
      `${PAYMENT_BASE_URL}/fsp-providers`
    );
    return response.data.data;
  },

  // Test FSP connection
  testFSPConnection: async (fspProviderId: string): Promise<any> => {
    const response = await apiClient.post<ApiResponse<any>>(
      `${PAYMENT_BASE_URL}/fsp-providers/${fspProviderId}/test`
    );
    return response.data.data;
  },

  // Get payment configuration
  getPaymentConfiguration: async (program: string): Promise<any> => {
    const response = await apiClient.get<ApiResponse<any>>(
      `${PAYMENT_BASE_URL}/configuration/${program}`
    );
    return response.data.data;
  },

  // Update payment configuration
  updatePaymentConfiguration: async (
    program: string,
    config: any
  ): Promise<any> => {
    const response = await apiClient.put<ApiResponse<any>>(
      `${PAYMENT_BASE_URL}/configuration/${program}`,
      config
    );
    return response.data.data;
  },
};
