// Payment Management Types and Interfaces
// TypeScript definitions for payment processing and disbursement system

// Payment Status Enum
export type PaymentStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED'
  | 'REFUNDED'
  | 'ON_HOLD';

// Payment Method Enum
export type PaymentMethod =
  | 'BANK_TRANSFER'
  | 'DIGITAL_WALLET'
  | 'CASH_PICKUP'
  | 'CHECK'
  | 'PREPAID_CARD';

// Bank Account Interface
export interface BankAccount {
  accountNumber: string;
  bankName: string;
  bankCode?: string;
  accountName: string;
  accountType?: 'SAVINGS' | 'CHECKING';
  branchCode?: string;
  branchName?: string;
}

// Digital Wallet Interface
export interface DigitalWallet {
  walletType: 'GCASH' | 'PAYMAYA' | 'GRABPAY' | 'COINS_PH' | 'OTHER';
  walletNumber: string;
  accountName: string;
  walletProvider?: string;
}

// Cash Pickup Interface
export interface CashPickup {
  location: string;
  pickupCode: string;
  partnerId?: string;
  partnerName?: string;
  address?: string;
  contactNumber?: string;
  operatingHours?: string;
}

// Main Payment Interface
export interface Payment {
  id: string;
  paymentId: string;
  batchId: string;
  beneficiaryId: string;
  beneficiaryName: string;
  program: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;

  // Scheduling
  scheduledDate: string;
  processedDate: string | null;

  // Reference and tracking
  reference: string;
  externalReference?: string;

  // Payment method details
  bankAccount?: BankAccount;
  digitalWallet?: DigitalWallet;
  cashPickup?: CashPickup;

  // FSP (Financial Service Provider) details
  fspProvider: string;
  fspReference?: string;
  fspTransactionId?: string;

  // Error handling
  failureReason?: string;
  retryCount?: number;
  maxRetries?: number;

  // Metadata
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  notes?: string;
  tags?: string[];

  // Verification
  isVerified?: boolean;
  verifiedAt?: string;
  verifiedBy?: string;

  // Reconciliation
  isReconciled?: boolean;
  reconciledAt?: string;
  reconciledBy?: string;
}

// Payment Batch Interface
export interface PaymentBatch {
  id: string;
  batchId: string;
  name: string;
  description?: string;
  program: string;
  totalAmount: number;
  totalPayments: number;
  currency: string;

  // Status tracking
  status:
    | 'DRAFT'
    | 'PENDING_APPROVAL'
    | 'APPROVED'
    | 'PROCESSING'
    | 'COMPLETED'
    | 'FAILED'
    | 'CANCELLED';

  // Scheduling
  scheduledDate: string;
  processedDate?: string;

  // Approval workflow
  createdBy: string;
  createdAt: string;
  approvedBy?: string;
  approvedAt?: string;

  // Processing details
  processedBy?: string;
  startedAt?: string;
  completedAt?: string;

  // Statistics
  successfulPayments: number;
  failedPayments: number;
  pendingPayments: number;

  // Configuration
  paymentMethods: PaymentMethod[];
  fspProviders: string[];

  // Metadata
  updatedAt: string;
  notes?: string;
  tags?: string[];
}

// Payment Filters Interface
export interface PaymentFilters {
  status?: string;
  paymentMethod?: string;
  program?: string;
  batchId?: string;
  beneficiaryId?: string;
  fspProvider?: string;
  dateRange: {
    start: string;
    end: string;
  };
  amountRange: {
    min: number;
    max: number;
  };
  searchQuery?: string;
  isVerified?: boolean;
  isReconciled?: boolean;
  hasFailures?: boolean;

  // Pagination parameters
  page?: number;
  limit?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

// Create Payment Batch Request Interface
export interface CreatePaymentBatchRequest {
  name: string;
  description?: string;
  program: string;
  scheduledDate: string;
  beneficiaries: {
    beneficiaryId: string;
    amount: number;
    paymentMethod: PaymentMethod;
    bankAccount?: BankAccount;
    digitalWallet?: DigitalWallet;
    cashPickup?: CashPickup;
  }[];
  notes?: string;
  tags?: string[];
}

// Payment Summary Interface
export interface PaymentSummary {
  totalPayments: number;
  totalAmount: number;
  byStatus: Record<PaymentStatus, number>;
  byMethod: Record<PaymentMethod, number>;
  byProgram: Record<string, number>;
  byFspProvider: Record<string, number>;
  successRate: number;
  averageProcessingTime: number;
  pendingAmount: number;
  completedAmount: number;
  failedAmount: number;
}

// Payment Transaction Interface
export interface PaymentTransaction {
  id: string;
  paymentId: string;
  transactionType: 'DEBIT' | 'CREDIT' | 'REFUND' | 'REVERSAL';
  amount: number;
  currency: string;
  status: PaymentStatus;
  reference: string;
  fspReference?: string;
  processedAt: string;
  description?: string;
  metadata?: Record<string, any>;
}

// Payment Reconciliation Interface
export interface PaymentReconciliation {
  id: string;
  batchId: string;
  reconciliationDate: string;
  totalPayments: number;
  reconciledPayments: number;
  unreconciledPayments: number;
  discrepancies: {
    paymentId: string;
    expectedAmount: number;
    actualAmount: number;
    difference: number;
    reason: string;
  }[];
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  performedBy: string;
  performedAt: string;
  notes?: string;
}

// Payment Report Interface
export interface PaymentReport {
  id: string;
  title: string;
  reportType: 'SUMMARY' | 'DETAILED' | 'RECONCILIATION' | 'FAILURE_ANALYSIS';
  filters: PaymentFilters;
  dateRange: {
    start: string;
    end: string;
  };
  data: Record<string, any>;
  generatedBy: string;
  generatedAt: string;
  format: 'PDF' | 'EXCEL' | 'CSV' | 'JSON';
  downloadUrl?: string;
}

// FSP (Financial Service Provider) Interface
export interface FSPProvider {
  id: string;
  name: string;
  code: string;
  type: 'BANK' | 'DIGITAL_WALLET' | 'CASH_PICKUP' | 'REMITTANCE';
  supportedMethods: PaymentMethod[];
  isActive: boolean;
  configuration: Record<string, any>;
  fees: {
    fixedFee: number;
    percentageFee: number;
    minimumFee: number;
    maximumFee: number;
  };
  limits: {
    minimumAmount: number;
    maximumAmount: number;
    dailyLimit: number;
    monthlyLimit: number;
  };
  processingTime: {
    averageMinutes: number;
    maximumMinutes: number;
  };
  reliability: {
    successRate: number;
    uptimePercentage: number;
    lastDowntime?: string;
  };
}

// Payment Notification Interface
export interface PaymentNotification {
  id: string;
  paymentId: string;
  beneficiaryId: string;
  type: 'SMS' | 'EMAIL' | 'PUSH' | 'IN_APP';
  template: string;
  content: string;
  status: 'PENDING' | 'SENT' | 'DELIVERED' | 'FAILED';
  sentAt?: string;
  deliveredAt?: string;
  failureReason?: string;
  retryCount: number;
  metadata?: Record<string, any>;
}

// Payment Audit Log Interface
export interface PaymentAuditLog {
  id: string;
  paymentId: string;
  action: string;
  performedBy: string;
  performedAt: string;
  oldValues?: Record<string, any>;
  newValues?: Record<string, any>;
  ipAddress?: string;
  userAgent?: string;
  notes?: string;
}

// Payment Configuration Interface
export interface PaymentConfiguration {
  id: string;
  program: string;
  defaultPaymentMethod: PaymentMethod;
  allowedPaymentMethods: PaymentMethod[];
  defaultFspProvider: string;
  allowedFspProviders: string[];
  processingSchedule: {
    frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY';
    dayOfWeek?: number;
    dayOfMonth?: number;
    time: string;
  };
  approvalRequired: boolean;
  approvers: string[];
  notificationSettings: {
    beneficiaryNotifications: boolean;
    staffNotifications: boolean;
    failureAlerts: boolean;
    reconciliationReports: boolean;
  };
  retrySettings: {
    maxRetries: number;
    retryDelayMinutes: number;
    exponentialBackoff: boolean;
  };
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}
