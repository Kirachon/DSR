// Jest Test Setup
// Global test configuration and mocks for DSR frontend

import React from 'react';
import '@testing-library/jest-dom';
import 'whatwg-fetch';

// Mock Next.js router
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
    prefetch: jest.fn(),
  }),
  useSearchParams: () => ({
    get: jest.fn(),
    getAll: jest.fn(),
    has: jest.fn(),
    keys: jest.fn(),
    values: jest.fn(),
    entries: jest.fn(),
    forEach: jest.fn(),
    toString: jest.fn(),
  }),
  useParams: () => ({}),
  usePathname: () => '/test',
}));

// Mock Next.js Link component
jest.mock('next/link', () => {
  return ({ children, href, ...props }: any) => {
    return (
      <a href={href} {...props}>
        {children}
      </a>
    );
  };
});

// Mock Next.js Image component
jest.mock('next/image', () => {
  return ({ src, alt, ...props }: any) => {
    return <img src={src} alt={alt} {...props} />;
  };
});

// Mock environment variables
process.env.NEXT_PUBLIC_API_BASE_URL = 'http://localhost:8080';
process.env.NEXT_PUBLIC_APP_ENV = 'test';

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
  length: 0,
  key: jest.fn(),
};

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

// Mock sessionStorage
const sessionStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
  length: 0,
  key: jest.fn(),
};

Object.defineProperty(window, 'sessionStorage', {
  value: sessionStorageMock,
});

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  root: Element | null = null;
  rootMargin: string = '0px';
  thresholds: ReadonlyArray<number> = [0];

  constructor(
    callback: IntersectionObserverCallback,
    options?: IntersectionObserverInit
  ) {}

  observe(target: Element): void {}
  disconnect(): void {}
  unobserve(target: Element): void {}
  takeRecords(): IntersectionObserverEntry[] {
    return [];
  }
} as any;

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() {}
  observe() {
    return null;
  }
  disconnect() {
    return null;
  }
  unobserve() {
    return null;
  }
};

// Mock File and FileReader
global.File = class File extends Blob {
  name: string;
  lastModified: number;
  webkitRelativePath: string = '';

  constructor(
    fileBits: BlobPart[],
    fileName: string,
    options?: FilePropertyBag
  ) {
    super(fileBits, options);
    this.name = fileName;
    this.lastModified = options?.lastModified || Date.now();
  }
} as any;

global.FileReader = class FileReader {
  result: any = null;
  error: any = null;
  readyState: number = 0;
  onload: any = null;
  onerror: any = null;
  onabort: any = null;
  onloadstart: any = null;
  onloadend: any = null;
  onprogress: any = null;

  readAsDataURL() {
    this.result = 'data:text/plain;base64,dGVzdA==';
    if (this.onload) this.onload({ target: this });
  }

  readAsText() {
    this.result = 'test';
    if (this.onload) this.onload({ target: this });
  }

  abort() {}
} as any;

// Mock URL.createObjectURL
global.URL.createObjectURL = jest.fn(() => 'mocked-url');
global.URL.revokeObjectURL = jest.fn();

// Mock fetch
global.fetch = jest.fn();

// Mock console methods in test environment
const originalError = console.error;
const originalWarn = console.warn;

beforeAll(() => {
  console.error = (...args: any[]) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('Warning: ReactDOM.render is no longer supported')
    ) {
      return;
    }
    originalError.call(console, ...args);
  };

  console.warn = (...args: any[]) => {
    if (
      typeof args[0] === 'string' &&
      (args[0].includes('componentWillReceiveProps') ||
        args[0].includes('componentWillUpdate'))
    ) {
      return;
    }
    originalWarn.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
  console.warn = originalWarn;
});

import { UserRole, UserStatus } from '@/types';

// Global test utilities
export const mockUser = {
  id: 'test-user-1',
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  role: UserRole.CITIZEN,
  status: UserStatus.ACTIVE,
  emailVerified: true,
  phoneVerified: false,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

export const mockAuthContext = {
  user: mockUser,
  isAuthenticated: true,
  isLoading: false,
  login: jest.fn(),
  logout: jest.fn(),
  register: jest.fn(),
  refreshToken: jest.fn(),
};

// Test data factories
export const createMockCase = (overrides = {}) => ({
  id: 'case-1',
  caseNumber: 'GRV-2024-001',
  title: 'Test Case',
  type: 'GRIEVANCE' as const,
  priority: 'MEDIUM' as const,
  status: 'NEW' as const,
  assignedTo: null,
  assignedToId: null,
  submittedBy: 'Test User',
  submittedById: 'user-1',
  submittedDate: '2024-01-15T10:30:00Z',
  dueDate: '2024-01-20T17:00:00Z',
  description: 'Test case description',
  category: 'PAYMENT_ISSUES' as const,
  resolution: null,
  notes: [],
  attachments: [],
  createdAt: '2024-01-15T10:30:00Z',
  updatedAt: '2024-01-15T10:30:00Z',
  ...overrides,
});

export const createMockPayment = (overrides = {}) => ({
  id: 'payment-1',
  paymentId: 'PAY-2024-001',
  batchId: 'BATCH-2024-001',
  beneficiaryId: 'BEN-001',
  beneficiaryName: 'Test Beneficiary',
  program: '4Ps',
  amount: 1500.0,
  currency: 'PHP',
  paymentMethod: 'BANK_TRANSFER' as const,
  status: 'PENDING' as const,
  scheduledDate: '2024-01-15T00:00:00Z',
  processedDate: null,
  reference: 'REF-001-2024',
  fspProvider: 'BDO',
  fspReference: null,
  createdAt: '2024-01-10T09:00:00Z',
  updatedAt: '2024-01-14T16:00:00Z',
  notes: 'Test payment',
  ...overrides,
});

// Custom render function with providers
export const renderWithProviders = (ui: React.ReactElement, options = {}) => {
  const { render } = require('@testing-library/react');
  const { BrowserRouter } = require('react-router-dom');

  const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
    return <BrowserRouter>{children}</BrowserRouter>;
  };

  return render(ui, { wrapper: AllTheProviders, ...options });
};

// Async test utilities
export const waitForLoadingToFinish = () => {
  const {
    waitForElementToBeRemoved,
    screen,
  } = require('@testing-library/react');
  return waitForElementToBeRemoved(
    () => screen.queryByTestId('loading') || screen.queryByText(/loading/i),
    { timeout: 5000 }
  );
};

// Mock API responses
export const mockApiResponse = (data: any, status = 200) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => ({ data }),
  text: async () => JSON.stringify({ data }),
});

// Test cleanup
afterEach(() => {
  jest.clearAllMocks();
  localStorageMock.clear();
  sessionStorageMock.clear();
});
