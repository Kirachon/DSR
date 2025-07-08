// Registration Types and Interfaces
// TypeScript definitions for household registration workflow

// Personal Information Interface
export interface PersonalInfo {
  firstName: string;
  lastName: string;
  middleName: string;
  suffix: string;
  birthDate: string;
  gender: string;
  civilStatus: string;
  nationality: string;
  religion: string;
  educationLevel: string;
  occupation: string;
  monthlyIncome: number;
  philSysNumber: string;
  contactNumber: string;
  emailAddress: string;
}

// Address Information Interface
export interface Address {
  houseNumber: string;
  street: string;
  streetAddress: string;
  barangay: string;
  municipality: string;
  province: string;
  region: string;
  zipCode: string;
  coordinates?: {
    latitude: number;
    longitude: number;
  };
}

// Household Member Interface
export interface HouseholdMember {
  id: string;
  firstName: string;
  lastName: string;
  middleName: string;
  suffix: string;
  birthDate: string;
  gender: string;
  civilStatus: string;
  relationship: string;
  occupation: string;
  monthlyIncome: number;
  philSysNumber: string;
  isHeadOfHousehold: boolean;
  hasDisability: boolean;
  disabilityType: string;
  isPregnant: boolean;
  isLactating: boolean;
  isStudent: boolean;
  schoolLevel: string;
}

// Socio-Economic Information Interface
export interface SocioEconomicInfo {
  householdSize: number;
  totalMonthlyIncome: number;
  primaryIncomeSource: string;
  housingType: string;
  housingOwnership: string;
  accessToUtilities: {
    electricity: boolean;
    water: boolean;
    internet: boolean;
    sewerage: boolean;
  };
  assets: Asset[];
  vulnerabilities: Vulnerability[];
}

// Asset Interface
export interface Asset {
  id: string;
  type: string;
  description: string;
  estimatedValue: number;
}

// Vulnerability Interface
export interface Vulnerability {
  id: string;
  type: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

// Document Upload Interface
export interface DocumentUpload {
  id: string;
  type: string;
  name: string;
  file: File | null;
  url?: string;
  uploadedAt?: string;
  status: 'PENDING' | 'UPLOADED' | 'VERIFIED' | 'REJECTED';
  rejectionReason?: string;
}

// Consent Data Interface
export interface ConsentData {
  dataProcessing: boolean;
  informationSharing: boolean;
  programEligibility: boolean;
  consentDate?: string;
  ipAddress?: string;
}

// Complete Household Registration Data Interface
export interface HouseholdRegistrationData {
  personalInfo: PersonalInfo;
  address: Address;
  householdMembers: HouseholdMember[];
  socioEconomicInfo: SocioEconomicInfo;
  documents: DocumentUpload[];
  consent: ConsentData;
}

// Registration Step Interface
export interface RegistrationStep {
  id: string;
  title: string;
  description: string;
  isCompleted: boolean;
  isActive: boolean;
  errors: string[];
}

// Registration Status Enum
export type RegistrationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'VERIFIED'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED';

// Registration Response Interface
export interface RegistrationResponse {
  id: string;
  status: RegistrationStatus;
  submittedAt: string;
  reviewedAt?: string;
  approvedAt?: string;
  rejectedAt?: string;
  rejectionReason?: string;
  reviewerNotes?: string;
  eligibilityScore?: number;
  recommendedPrograms?: string[];
}

// Registration Create Request Interface
export interface RegistrationCreateRequest {
  personalInfo: PersonalInfo;
  address: Address;
  householdMembers: HouseholdMember[];
  socioEconomicInfo: SocioEconomicInfo;
  documents: DocumentUpload[];
  consent: ConsentData;
}

// Registration Update Request Interface
export interface RegistrationUpdateRequest {
  personalInfo?: Partial<PersonalInfo>;
  address?: Partial<Address>;
  householdMembers?: HouseholdMember[];
  socioEconomicInfo?: Partial<SocioEconomicInfo>;
  documents?: DocumentUpload[];
  consent?: Partial<ConsentData>;
}

// Document Type Enum
export type DocumentType =
  | 'BIRTH_CERTIFICATE'
  | 'MARRIAGE_CERTIFICATE'
  | 'DEATH_CERTIFICATE'
  | 'VALID_ID'
  | 'PROOF_OF_INCOME'
  | 'PROOF_OF_RESIDENCE'
  | 'MEDICAL_CERTIFICATE'
  | 'SCHOOL_ENROLLMENT'
  | 'PWD_ID'
  | 'SENIOR_CITIZEN_ID'
  | 'PHILSYS_ID'
  | 'OTHER';

// Housing Type Enum
export type HousingType =
  | 'SINGLE_DETACHED'
  | 'DUPLEX'
  | 'APARTMENT'
  | 'CONDOMINIUM'
  | 'TOWNHOUSE'
  | 'INFORMAL_SETTLEMENT'
  | 'BOARDING_HOUSE'
  | 'INSTITUTION'
  | 'OTHER';

// Housing Ownership Enum
export type HousingOwnership =
  | 'OWNED'
  | 'RENTED'
  | 'RENT_FREE_WITH_CONSENT'
  | 'RENT_FREE_WITHOUT_CONSENT'
  | 'OTHER';

// Income Source Enum
export type IncomeSource =
  | 'EMPLOYMENT'
  | 'BUSINESS'
  | 'AGRICULTURE'
  | 'FISHING'
  | 'REMITTANCES'
  | 'PENSION'
  | 'SOCIAL_ASSISTANCE'
  | 'OTHER';

// Asset Type Enum
export type AssetType =
  | 'REAL_ESTATE'
  | 'VEHICLE'
  | 'LIVESTOCK'
  | 'APPLIANCES'
  | 'ELECTRONICS'
  | 'JEWELRY'
  | 'SAVINGS'
  | 'INVESTMENTS'
  | 'OTHER';

// Vulnerability Type Enum
export type VulnerabilityType =
  | 'POVERTY'
  | 'DISABILITY'
  | 'CHRONIC_ILLNESS'
  | 'ELDERLY'
  | 'PREGNANT_LACTATING'
  | 'CHILD_LABOR'
  | 'OUT_OF_SCHOOL'
  | 'UNEMPLOYED'
  | 'INFORMAL_WORKER'
  | 'DISASTER_AFFECTED'
  | 'CONFLICT_AFFECTED'
  | 'OTHER';

// Form Validation Error Interface
export interface FormValidationError {
  field: string;
  message: string;
  code?: string;
}

// Step Validation Result Interface
export interface StepValidationResult {
  isValid: boolean;
  errors: FormValidationError[];
  warnings: FormValidationError[];
}

// Registration Context Interface
export interface RegistrationContextType {
  currentStep: number;
  totalSteps: number;
  formData: HouseholdRegistrationData;
  isSubmitting: boolean;
  errors: Record<string, string[]>;
  updateFormData: (data: Partial<HouseholdRegistrationData>) => void;
  nextStep: () => void;
  previousStep: () => void;
  submitRegistration: () => Promise<void>;
  validateStep: (stepIndex: number) => StepValidationResult;
}
