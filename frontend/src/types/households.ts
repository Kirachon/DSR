// Household Management Types
// TypeScript interfaces for household data and operations

import type { BaseEntity, Address, HouseholdMember } from './';

// Household data interface
export interface HouseholdData extends BaseEntity {
  psn: string; // PhilSys Number
  householdNumber?: string;
  headOfHouseholdPsn: string;
  totalMembers: number;
  monthlyIncome?: number;
  
  // Housing characteristics
  housingType?: string;
  housingTenure?: string;
  waterSource?: string;
  toiletFacility?: string;
  electricitySource?: string;
  cookingFuel?: string;
  
  // Location
  address: Address;
  region: string;
  province: string;
  municipality: string;
  barangay: string;
  
  // Status and validation
  status?: 'active' | 'inactive' | 'suspended' | 'pending';
  validationStatus?: 'validated' | 'pending' | 'rejected' | 'requires_review';
  
  // Household head information
  householdHead: HouseholdMember;
  
  // All household members
  members: HouseholdMember[];
  
  // System information
  sourceSystem?: string;
  preferredLanguage?: string;
  notes?: string;
  registrationDate?: string;
  
  // Audit fields
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

// Household filters interface
export interface HouseholdFilters {
  region?: string;
  province?: string;
  municipality?: string;
  barangay?: string;
  status?: string;
  validationStatus?: string;
  housingType?: string;
  incomeRange?: {
    min?: number;
    max?: number;
  };
  memberCountRange?: {
    min?: number;
    max?: number;
  };
  registrationDateRange?: {
    start?: string;
    end?: string;
  };
}

// Household search parameters
export interface HouseholdSearchParams {
  query?: string;
  filters?: HouseholdFilters;
  page?: number;
  limit?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

// Create household request
export interface CreateHouseholdRequest {
  householdNumber?: string;
  headOfHouseholdPsn: string;
  totalMembers: number;
  monthlyIncome?: number;
  
  // Housing characteristics
  housingType?: string;
  housingTenure?: string;
  waterSource?: string;
  toiletFacility?: string;
  electricitySource?: string;
  cookingFuel?: string;
  
  // Location
  region: string;
  province: string;
  municipality: string;
  barangay: string;
  streetAddress: string;
  zipCode?: string;
  
  // Household head information
  householdHead: Omit<HouseholdMember, 'id' | 'householdId'>;
  
  // Additional members (optional)
  members?: Omit<HouseholdMember, 'id' | 'householdId'>[];
  
  // System information
  sourceSystem?: string;
  preferredLanguage?: string;
  notes?: string;
  createdBy?: string;
}

// Update household request
export interface UpdateHouseholdRequest {
  householdNumber?: string;
  totalMembers?: number;
  monthlyIncome?: number;
  
  // Housing characteristics
  housingType?: string;
  housingTenure?: string;
  waterSource?: string;
  toiletFacility?: string;
  electricitySource?: string;
  cookingFuel?: string;
  
  // Location
  region?: string;
  province?: string;
  municipality?: string;
  barangay?: string;
  streetAddress?: string;
  zipCode?: string;
  
  // Status
  status?: 'active' | 'inactive' | 'suspended' | 'pending';
  validationStatus?: 'validated' | 'pending' | 'rejected' | 'requires_review';
  
  // System information
  preferredLanguage?: string;
  notes?: string;
  updatedBy?: string;
}

// Household summary for lists and cards
export interface HouseholdSummary {
  id: string;
  psn: string;
  householdNumber?: string;
  headName: string;
  totalMembers: number;
  monthlyIncome?: number;
  address: {
    municipality: string;
    province: string;
    region: string;
  };
  status?: string;
  validationStatus?: string;
  registrationDate?: string;
}

// Household statistics
export interface HouseholdStatistics {
  totalHouseholds: number;
  activeHouseholds: number;
  pendingValidation: number;
  averageHouseholdSize: number;
  averageMonthlyIncome: number;
  householdsByRegion: Record<string, number>;
  householdsByStatus: Record<string, number>;
  recentRegistrations: number;
}

// Household validation result
export interface HouseholdValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  validatedFields: string[];
  validationDate: string;
  validatedBy?: string;
}

// Household member management
export interface HouseholdMemberUpdate {
  id?: string;
  firstName?: string;
  lastName?: string;
  middleName?: string;
  relationshipToHead?: string;
  birthDate?: string;
  gender?: string;
  civilStatus?: string;
  occupation?: string;
  monthlyIncome?: number;
  educationLevel?: string;
  isHeadOfHousehold?: boolean;
}

// Household history entry
export interface HouseholdHistoryEntry {
  id: string;
  householdId: string;
  action: 'created' | 'updated' | 'validated' | 'status_changed' | 'member_added' | 'member_removed';
  description: string;
  changes?: Record<string, { from: any; to: any }>;
  performedBy: string;
  performedAt: string;
  metadata?: Record<string, any>;
}

// Household export options
export interface HouseholdExportOptions {
  format: 'csv' | 'excel' | 'pdf';
  filters?: HouseholdFilters;
  fields?: string[];
  includeMembers?: boolean;
  includeHistory?: boolean;
}

// Household import result
export interface HouseholdImportResult {
  totalRecords: number;
  successfulImports: number;
  failedImports: number;
  errors: Array<{
    row: number;
    field?: string;
    message: string;
  }>;
  warnings: Array<{
    row: number;
    field?: string;
    message: string;
  }>;
  importedHouseholds: string[]; // Array of household IDs
}
