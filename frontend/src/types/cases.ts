// Case Management Types and Interfaces
// TypeScript definitions for grievance and case management system

// Case Status Enum
export type CaseStatus =
  | 'NEW'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'PENDING_REVIEW'
  | 'PENDING_CITIZEN_RESPONSE'
  | 'ESCALATED'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED';

// Case Type Enum
export type CaseType =
  | 'GRIEVANCE'
  | 'APPEAL'
  | 'INQUIRY'
  | 'COMPLAINT'
  | 'FEEDBACK'
  | 'REQUEST';

// Case Priority Enum
export type CasePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | 'CRITICAL';

// Case Category Enum
export type CaseCategory =
  | 'PAYMENT_ISSUES'
  | 'ELIGIBILITY_ISSUES'
  | 'REGISTRATION_ISSUES'
  | 'DOCUMENT_ISSUES'
  | 'SERVICE_QUALITY'
  | 'SYSTEM_ISSUES'
  | 'POLICY_CLARIFICATION'
  | 'DISCRIMINATION'
  | 'CORRUPTION'
  | 'OTHER';

// Case Note Interface
export interface CaseNote {
  id: string;
  content: string;
  createdBy: string;
  createdById: string;
  createdAt: string;
  isInternal: boolean;
  attachments?: CaseAttachment[];
}

// Case Attachment Interface
export interface CaseAttachment {
  id: string;
  name: string;
  url: string;
  type: string;
  size?: number;
  uploadedBy?: string;
  uploadedAt: string;
}

// Case Resolution Interface
export interface CaseResolution {
  id: string;
  summary: string;
  details: string;
  resolutionType:
    | 'RESOLVED'
    | 'PARTIALLY_RESOLVED'
    | 'CANNOT_RESOLVE'
    | 'DUPLICATE'
    | 'INVALID';
  actionsTaken: string[];
  followUpRequired: boolean;
  followUpDate?: string;
  satisfactionRating?: number;
  resolvedBy: string;
  resolvedById: string;
  resolvedAt: string;
}

// Main Case Interface
export interface Case {
  id: string;
  caseNumber: string;
  title: string;
  description: string;
  type: CaseType;
  category: CaseCategory;
  priority: CasePriority;
  status: CaseStatus;

  // Assignment
  assignedTo: string | null;
  assignedToId: string | null;
  assignedAt?: string;

  // Submission details
  submittedBy: string;
  submittedById: string;
  submittedDate: string;

  // Contact information
  contactEmail?: string;
  contactPhone?: string;
  preferredContactMethod?: 'EMAIL' | 'PHONE' | 'SMS' | 'IN_PERSON';

  // Timeline
  dueDate: string;
  createdAt: string;
  updatedAt: string;
  closedAt?: string;

  // Related data
  notes: CaseNote[];
  attachments: CaseAttachment[];
  resolution?: CaseResolution;

  // Metadata
  tags?: string[];
  relatedCases?: string[];
  escalationLevel?: number;
  isUrgent?: boolean;

  // Citizen information
  citizenId?: string;
  householdId?: string;
  beneficiaryId?: string;
}

// Case Filters Interface
export interface CaseFilters {
  status?: string;
  type?: string;
  priority?: string;
  category?: string;
  assignedTo?: string;
  submittedBy?: string;
  dateRange: {
    start: string;
    end: string;
  };
  searchQuery?: string;
  tags?: string[];
  isUrgent?: boolean;

  // Pagination parameters
  page?: number;
  limit?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

// Create Case Request Interface
export interface CreateCaseRequest {
  title: string;
  description: string;
  type: CaseType;
  category: CaseCategory;
  priority: CasePriority;
  submittedBy?: string;
  submittedById?: string;
  contactEmail?: string;
  contactPhone?: string;
  preferredContactMethod?: 'EMAIL' | 'PHONE' | 'SMS' | 'IN_PERSON';
  attachments?: File[];
  tags?: string[];
  citizenId?: string;
  householdId?: string;
  beneficiaryId?: string;
}

// Update Case Request Interface
export interface UpdateCaseRequest {
  title?: string;
  description?: string;
  type?: CaseType;
  category?: CaseCategory;
  priority?: CasePriority;
  status?: CaseStatus;
  assignedToId?: string;
  dueDate?: string;
  tags?: string[];
  escalationLevel?: number;
  isUrgent?: boolean;
}

// Case Assignment Request Interface
export interface CaseAssignmentRequest {
  caseId: string;
  assignedToId: string;
  assignedBy: string;
  notes?: string;
}

// Case Note Request Interface
export interface CreateCaseNoteRequest {
  caseId: string;
  content: string;
  isInternal: boolean;
  attachments?: File[];
}

// Case Resolution Request Interface
export interface CreateCaseResolutionRequest {
  caseId: string;
  summary: string;
  details: string;
  resolutionType:
    | 'RESOLVED'
    | 'PARTIALLY_RESOLVED'
    | 'CANNOT_RESOLVE'
    | 'DUPLICATE'
    | 'INVALID';
  actionsTaken: string[];
  followUpRequired: boolean;
  followUpDate?: string;
}

// Case Statistics Interface
export interface CaseStatistics {
  total: number;
  byStatus: Record<CaseStatus, number>;
  byType: Record<CaseType, number>;
  byPriority: Record<CasePriority, number>;
  byCategory: Record<CaseCategory, number>;
  averageResolutionTime: number;
  overdueCount: number;
  urgentCount: number;
  assignedToMe: number;
  unassigned: number;
}

// Case Timeline Event Interface
export interface CaseTimelineEvent {
  id: string;
  type:
    | 'CREATED'
    | 'ASSIGNED'
    | 'STATUS_CHANGED'
    | 'NOTE_ADDED'
    | 'ATTACHMENT_ADDED'
    | 'RESOLVED'
    | 'CLOSED';
  description: string;
  performedBy: string;
  performedById: string;
  performedAt: string;
  metadata?: Record<string, any>;
}

// Case Search Result Interface
export interface CaseSearchResult {
  cases: Case[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
  filters: CaseFilters;
}

// Case Escalation Interface
export interface CaseEscalation {
  id: string;
  caseId: string;
  fromLevel: number;
  toLevel: number;
  reason: string;
  escalatedBy: string;
  escalatedById: string;
  escalatedAt: string;
  escalatedTo: string;
  escalatedToId: string;
  notes?: string;
}

// Case Template Interface
export interface CaseTemplate {
  id: string;
  name: string;
  description: string;
  type: CaseType;
  category: CaseCategory;
  priority: CasePriority;
  titleTemplate: string;
  descriptionTemplate: string;
  defaultAssigneeId?: string;
  estimatedResolutionDays: number;
  requiredFields: string[];
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

// Case Workflow Interface
export interface CaseWorkflow {
  id: string;
  name: string;
  description: string;
  applicableTypes: CaseType[];
  applicableCategories: CaseCategory[];
  steps: CaseWorkflowStep[];
  isActive: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

// Case Workflow Step Interface
export interface CaseWorkflowStep {
  id: string;
  name: string;
  description: string;
  order: number;
  requiredRole?: string;
  estimatedDuration: number;
  isOptional: boolean;
  conditions?: Record<string, any>;
  actions?: Record<string, any>;
}

// Case Report Interface
export interface CaseReport {
  id: string;
  title: string;
  description: string;
  reportType: 'SUMMARY' | 'DETAILED' | 'TREND' | 'PERFORMANCE';
  filters: CaseFilters;
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
