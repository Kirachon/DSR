// Grievance API Tests
// Comprehensive test suite for the grievance API client

import { grievanceApi } from '@/lib/api/grievance-api';
import { apiClient } from '@/lib/api-client';
import type { Case, CaseFilters, CreateCaseRequest } from '@/types';

// Mock the API client
jest.mock('@/lib/api-client', () => ({
  apiClient: {
    get: jest.fn(),
    post: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
  },
}));

const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

// Mock data
const mockCase: Case = {
  id: '1',
  caseNumber: 'GRV-2024-001',
  title: 'Test Case',
  type: 'GRIEVANCE',
  priority: 'HIGH',
  status: 'NEW',
  assignedTo: null,
  assignedToId: null,
  submittedBy: 'John Doe',
  submittedById: 'user-1',
  submittedDate: '2024-01-15T10:30:00Z',
  dueDate: '2024-01-20T17:00:00Z',
  description: 'Test case description',
  category: 'PAYMENT_ISSUES',
  resolution: null,
  notes: [],
  attachments: [],
  createdAt: '2024-01-15T10:30:00Z',
  updatedAt: '2024-01-15T10:30:00Z',
};

const mockFilters: CaseFilters = {
  status: 'NEW',
  type: 'GRIEVANCE',
  priority: 'HIGH',
  category: 'PAYMENT_ISSUES',
  assignedTo: 'user-1',
  submittedBy: 'user-2',
  dateRange: {
    start: '2024-01-01',
    end: '2024-01-31',
  },
  searchQuery: 'test',
  tags: ['urgent'],
  isUrgent: true,
};

describe('grievanceApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getCases', () => {
    it('should fetch cases with filters', async () => {
      const mockResponse = {
        data: {
          data: [mockCase],
          total: 1,
          page: 1,
          pageSize: 10,
          totalPages: 1,
        },
      };

      mockedApiClient.get.mockResolvedValue(mockResponse);

      const result = await grievanceApi.getCases(mockFilters);

      expect(mockedApiClient.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/grievances/cases?')
      );
      expect(result).toEqual(mockResponse.data);
    });

    it('should build query parameters correctly', async () => {
      const mockResponse = {
        data: { data: [], total: 0, page: 1, pageSize: 10, totalPages: 0 },
      };
      mockedApiClient.get.mockResolvedValue(mockResponse);

      await grievanceApi.getCases(mockFilters);

      const calledUrl = mockedApiClient.get.mock.calls[0][0] as string;
      expect(calledUrl).toContain('status=NEW');
      expect(calledUrl).toContain('type=GRIEVANCE');
      expect(calledUrl).toContain('priority=HIGH');
      expect(calledUrl).toContain('category=PAYMENT_ISSUES');
      expect(calledUrl).toContain('assignedTo=user-1');
      expect(calledUrl).toContain('submittedBy=user-2');
      expect(calledUrl).toContain('startDate=2024-01-01');
      expect(calledUrl).toContain('endDate=2024-01-31');
      expect(calledUrl).toContain('q=test');
      expect(calledUrl).toContain('tags=urgent');
      expect(calledUrl).toContain('isUrgent=true');
    });

    it('should handle empty filters', async () => {
      const emptyFilters: CaseFilters = {
        status: '',
        type: '',
        priority: '',
        category: '',
        assignedTo: '',
        submittedBy: '',
        dateRange: { start: '', end: '' },
        searchQuery: '',
        tags: [],
        isUrgent: undefined,
      };

      const mockResponse = {
        data: { data: [], total: 0, page: 1, pageSize: 10, totalPages: 0 },
      };
      mockedApiClient.get.mockResolvedValue(mockResponse);

      await grievanceApi.getCases(emptyFilters);

      const calledUrl = mockedApiClient.get.mock.calls[0][0] as string;
      expect(calledUrl).toBe('/api/v1/grievances/cases?');
    });
  });

  describe('getCase', () => {
    it('should fetch a single case by ID', async () => {
      const mockResponse = { data: { data: mockCase } };
      mockedApiClient.get.mockResolvedValue(mockResponse);

      const result = await grievanceApi.getCase('1');

      expect(mockedApiClient.get).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1'
      );
      expect(result).toEqual(mockCase);
    });
  });

  describe('createCase', () => {
    it('should create a new case', async () => {
      const createRequest: CreateCaseRequest = {
        title: 'New Case',
        description: 'Case description',
        type: 'GRIEVANCE',
        category: 'PAYMENT_ISSUES',
        priority: 'HIGH',
        submittedBy: 'John Doe',
        submittedById: 'user-1',
        contactEmail: 'john@example.com',
        contactPhone: '09123456789',
        preferredContactMethod: 'EMAIL',
        attachments: [],
        tags: [],
        citizenId: 'citizen-1',
        householdId: 'household-1',
        beneficiaryId: 'beneficiary-1',
      };

      const mockResponse = { data: { data: mockCase } };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await grievanceApi.createCase(createRequest);

      expect(mockedApiClient.post).toHaveBeenCalledWith(
        '/api/v1/grievances/cases',
        createRequest
      );
      expect(result).toEqual(mockCase);
    });
  });

  describe('updateCase', () => {
    it('should update a case', async () => {
      const updates = { title: 'Updated Title', priority: 'MEDIUM' as const };
      const mockResponse = { data: { data: { ...mockCase, ...updates } } };
      mockedApiClient.patch.mockResolvedValue(mockResponse);

      const result = await grievanceApi.updateCase('1', updates);

      expect(mockedApiClient.patch).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1',
        updates
      );
      expect(result).toEqual({ ...mockCase, ...updates });
    });
  });

  describe('updateCaseStatus', () => {
    it('should update case status', async () => {
      const newStatus = 'IN_PROGRESS';
      const mockResponse = {
        data: { data: { ...mockCase, status: newStatus } },
      };
      mockedApiClient.patch.mockResolvedValue(mockResponse);

      const result = await grievanceApi.updateCaseStatus('1', newStatus);

      expect(mockedApiClient.patch).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1/status',
        { status: newStatus }
      );
      expect(result.status).toBe(newStatus);
    });
  });

  describe('assignCase', () => {
    it('should assign a case to a user', async () => {
      const assignedToId = 'user-2';
      const notes = 'Assignment notes';
      const mockResponse = {
        data: {
          data: {
            ...mockCase,
            assignedToId,
            assignedTo: 'Jane Doe',
          },
        },
      };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await grievanceApi.assignCase('1', assignedToId, notes);

      expect(mockedApiClient.post).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1/assign',
        { assignedToId, notes }
      );
      expect(result.assignedToId).toBe(assignedToId);
    });
  });

  describe('addCaseNote', () => {
    it('should add a note to a case', async () => {
      const noteData = {
        content: 'Test note',
        isInternal: true,
        attachments: [],
      };
      const mockResponse = { data: { data: mockCase } };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await grievanceApi.addCaseNote('1', noteData);

      expect(mockedApiClient.post).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1/notes',
        noteData
      );
      expect(result).toEqual(mockCase);
    });
  });

  describe('uploadAttachment', () => {
    it('should upload an attachment to a case', async () => {
      const file = new File(['test'], 'test.pdf', { type: 'application/pdf' });
      const mockResponse = { data: { data: mockCase } };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await grievanceApi.uploadAttachment('1', file);

      expect(mockedApiClient.post).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1/attachments',
        expect.any(FormData),
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      expect(result).toEqual(mockCase);
    });
  });

  describe('escalateCase', () => {
    it('should escalate a case', async () => {
      const reason = 'Escalation reason';
      const escalatedToId = 'supervisor-1';
      const mockResponse = { data: { data: mockCase } };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await grievanceApi.escalateCase(
        '1',
        reason,
        escalatedToId
      );

      expect(mockedApiClient.post).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1/escalate',
        { reason, escalatedToId }
      );
      expect(result).toEqual(mockCase);
    });
  });

  describe('closeCase', () => {
    it('should close a case with resolution', async () => {
      const resolution = {
        summary: 'Case resolved',
        details: 'Resolution details',
        resolutionType: 'RESOLVED',
        actionsTaken: ['Action 1', 'Action 2'],
        followUpRequired: false,
      };
      const mockResponse = { data: { data: mockCase } };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await grievanceApi.closeCase('1', resolution);

      expect(mockedApiClient.post).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/1/close',
        { resolution }
      );
      expect(result).toEqual(mockCase);
    });
  });

  describe('getCaseStatistics', () => {
    it('should fetch case statistics', async () => {
      const mockStats = {
        total: 100,
        byStatus: { NEW: 20, IN_PROGRESS: 30, RESOLVED: 50 },
        byType: { GRIEVANCE: 60, APPEAL: 40 },
        byPriority: { LOW: 30, MEDIUM: 40, HIGH: 30 },
        byCategory: { PAYMENT_ISSUES: 50, ELIGIBILITY_ISSUES: 50 },
        averageResolutionTime: 5.5,
        overdueCount: 10,
        urgentCount: 5,
        assignedToMe: 15,
        unassigned: 8,
      };
      const mockResponse = { data: { data: mockStats } };
      mockedApiClient.get.mockResolvedValue(mockResponse);

      const result = await grievanceApi.getCaseStatistics();

      expect(mockedApiClient.get).toHaveBeenCalledWith(
        '/api/v1/grievances/statistics?'
      );
      expect(result).toEqual(mockStats);
    });

    it('should include filters in statistics request', async () => {
      const filters = {
        dateRange: { start: '2024-01-01', end: '2024-01-31' },
        assignedTo: 'user-1',
      };
      const mockResponse = { data: { data: {} } };
      mockedApiClient.get.mockResolvedValue(mockResponse);

      await grievanceApi.getCaseStatistics(filters);

      const calledUrl = mockedApiClient.get.mock.calls[0][0] as string;
      expect(calledUrl).toContain('startDate=2024-01-01');
      expect(calledUrl).toContain('endDate=2024-01-31');
      expect(calledUrl).toContain('assignedTo=user-1');
    });
  });

  describe('error handling', () => {
    it('should propagate API errors', async () => {
      const error = new Error('API Error');
      mockedApiClient.get.mockRejectedValue(error);

      await expect(grievanceApi.getCase('1')).rejects.toThrow('API Error');
    });

    it('should handle network errors', async () => {
      const networkError = new Error('Network Error');
      mockedApiClient.post.mockRejectedValue(networkError);

      const createRequest: CreateCaseRequest = {
        title: 'Test',
        description: 'Test',
        type: 'GRIEVANCE',
        category: 'PAYMENT_ISSUES',
        priority: 'HIGH',
      };

      await expect(grievanceApi.createCase(createRequest)).rejects.toThrow(
        'Network Error'
      );
    });
  });

  describe('bulk operations', () => {
    it('should handle bulk case updates', async () => {
      const caseIds = ['1', '2', '3'];
      const updates = { priority: 'HIGH' as const };
      const mockResponse = { data: { data: [mockCase] } };
      mockedApiClient.patch.mockResolvedValue(mockResponse);

      const result = await grievanceApi.bulkUpdateCases(caseIds, updates);

      expect(mockedApiClient.patch).toHaveBeenCalledWith(
        '/api/v1/grievances/cases/bulk-update',
        { caseIds, updates }
      );
      expect(result).toEqual([mockCase]);
    });
  });

  describe('export functionality', () => {
    it('should export cases in specified format', async () => {
      const mockBlob = new Blob(['test data'], { type: 'text/csv' });
      mockedApiClient.get.mockResolvedValue({ data: mockBlob });

      const result = await grievanceApi.exportCases(mockFilters, 'CSV');

      expect(mockedApiClient.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/grievances/export?format=CSV'),
        { responseType: 'blob' }
      );
      expect(result).toEqual(mockBlob);
    });
  });
});
