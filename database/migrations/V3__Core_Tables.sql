-- DSR Core Tables Migration
-- Philippine Dynamic Social Registry (DSR) System
-- Version: 3.0.0
-- Flyway Migration: V3__Core_Tables.sql

-- Set search path
SET search_path TO dsr_core, dsr_auth, public;

-- Households table - main household information
CREATE TABLE dsr_core.households (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_number VARCHAR(20) UNIQUE NOT NULL,
    head_of_household_psn VARCHAR(20),
    total_members INTEGER NOT NULL DEFAULT 1,
    monthly_income DECIMAL(12,2),
    is_indigenous BOOLEAN DEFAULT FALSE,
    is_pwd_household BOOLEAN DEFAULT FALSE,
    is_senior_citizen_household BOOLEAN DEFAULT FALSE,
    is_solo_parent_household BOOLEAN DEFAULT FALSE,
    housing_type VARCHAR(50),
    housing_tenure VARCHAR(50),
    water_source VARCHAR(50),
    toilet_facility VARCHAR(50),
    electricity_source VARCHAR(50),
    cooking_fuel VARCHAR(50),
    preferred_language VARCHAR(10) DEFAULT 'en',
    registration_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status dsr_core.registration_status DEFAULT 'DRAFT',
    registration_channel dsr_core.registration_channel DEFAULT 'WEB_PORTAL',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES dsr_auth.users(id),
    updated_by UUID REFERENCES dsr_auth.users(id)
);

-- Household members table
CREATE TABLE dsr_core.household_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES dsr_core.households(id) ON DELETE CASCADE,
    psn VARCHAR(20) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    suffix VARCHAR(20),
    birth_date DATE NOT NULL,
    gender dsr_core.gender NOT NULL,
    civil_status dsr_core.civil_status DEFAULT 'SINGLE',
    relationship_to_head dsr_core.relationship_type NOT NULL,
    education_level dsr_core.education_level,
    employment_status dsr_core.employment_status DEFAULT 'UNEMPLOYED',
    occupation VARCHAR(100),
    monthly_income DECIMAL(10,2),
    is_pwd BOOLEAN DEFAULT FALSE,
    is_indigenous BOOLEAN DEFAULT FALSE,
    is_senior_citizen BOOLEAN DEFAULT FALSE,
    is_solo_parent BOOLEAN DEFAULT FALSE,
    is_pregnant BOOLEAN DEFAULT FALSE,
    is_lactating BOOLEAN DEFAULT FALSE,
    health_conditions TEXT[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES dsr_auth.users(id),
    updated_by UUID REFERENCES dsr_auth.users(id)
);

-- Household addresses table
CREATE TABLE dsr_core.household_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES dsr_core.households(id) ON DELETE CASCADE,
    address_type VARCHAR(20) DEFAULT 'CURRENT',
    house_number VARCHAR(20),
    street VARCHAR(100),
    barangay VARCHAR(100) NOT NULL,
    municipality VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    region VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    is_primary BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Contact information table
CREATE TABLE dsr_core.contact_information (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES dsr_core.households(id) ON DELETE CASCADE,
    contact_type VARCHAR(20) NOT NULL,
    contact_value VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Registrations table - tracks registration applications
CREATE TABLE dsr_core.registrations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES dsr_core.households(id) ON DELETE CASCADE,
    application_number VARCHAR(30) UNIQUE NOT NULL,
    status dsr_core.registration_status DEFAULT 'DRAFT',
    registration_channel dsr_core.registration_channel DEFAULT 'WEB_PORTAL',
    submitted_at TIMESTAMP WITH TIME ZONE,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    reviewer_id UUID REFERENCES dsr_auth.users(id),
    approver_id UUID REFERENCES dsr_auth.users(id),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES dsr_auth.users(id),
    updated_by UUID REFERENCES dsr_auth.users(id)
);

-- Verification information table
CREATE TABLE dsr_core.verification_info (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registration_id UUID NOT NULL REFERENCES dsr_core.registrations(id) ON DELETE CASCADE,
    verification_type VARCHAR(50) NOT NULL,
    status dsr_core.verification_status DEFAULT 'PENDING',
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by UUID REFERENCES dsr_auth.users(id),
    verification_data JSONB,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Documents table
CREATE TABLE dsr_core.documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registration_id UUID NOT NULL REFERENCES dsr_core.registrations(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    uploaded_by UUID REFERENCES dsr_auth.users(id),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by UUID REFERENCES dsr_auth.users(id)
);

-- Life events table
CREATE TABLE dsr_core.life_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES dsr_core.households(id) ON DELETE CASCADE,
    member_id UUID REFERENCES dsr_core.household_members(id) ON DELETE SET NULL,
    event_type dsr_core.life_event_type NOT NULL,
    event_date DATE NOT NULL,
    description TEXT,
    supporting_documents JSONB,
    reported_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    reported_by UUID REFERENCES dsr_auth.users(id),
    processed_at TIMESTAMP WITH TIME ZONE,
    processed_by UUID REFERENCES dsr_auth.users(id)
);

-- Member health conditions table (for ElementCollection mapping)
CREATE TABLE dsr_core.member_health_conditions (
    household_member_id UUID NOT NULL REFERENCES dsr_core.household_members(id) ON DELETE CASCADE,
    condition VARCHAR(255) NOT NULL,
    PRIMARY KEY (household_member_id, condition)
);

-- Create indexes for performance
CREATE INDEX idx_households_household_number ON dsr_core.households(household_number);
CREATE INDEX idx_households_head_psn ON dsr_core.households(head_of_household_psn);
CREATE INDEX idx_households_status ON dsr_core.households(status);
CREATE INDEX idx_households_registration_date ON dsr_core.households(registration_date);
CREATE INDEX idx_households_monthly_income ON dsr_core.households(monthly_income);

CREATE INDEX idx_household_members_household_id ON dsr_core.household_members(household_id);
CREATE INDEX idx_household_members_psn ON dsr_core.household_members(psn);
CREATE INDEX idx_household_members_birth_date ON dsr_core.household_members(birth_date);
CREATE INDEX idx_household_members_relationship ON dsr_core.household_members(relationship_to_head);

CREATE INDEX idx_household_addresses_household_id ON dsr_core.household_addresses(household_id);
CREATE INDEX idx_household_addresses_barangay ON dsr_core.household_addresses(barangay);
CREATE INDEX idx_household_addresses_municipality ON dsr_core.household_addresses(municipality);
CREATE INDEX idx_household_addresses_province ON dsr_core.household_addresses(province);

CREATE INDEX idx_contact_information_household_id ON dsr_core.contact_information(household_id);
CREATE INDEX idx_contact_information_type ON dsr_core.contact_information(contact_type);

CREATE INDEX idx_registrations_household_id ON dsr_core.registrations(household_id);
CREATE INDEX idx_registrations_application_number ON dsr_core.registrations(application_number);
CREATE INDEX idx_registrations_status ON dsr_core.registrations(status);
CREATE INDEX idx_registrations_submitted_at ON dsr_core.registrations(submitted_at);

CREATE INDEX idx_verification_info_registration_id ON dsr_core.verification_info(registration_id);
CREATE INDEX idx_verification_info_type ON dsr_core.verification_info(verification_type);
CREATE INDEX idx_verification_info_status ON dsr_core.verification_info(status);

CREATE INDEX idx_documents_registration_id ON dsr_core.documents(registration_id);
CREATE INDEX idx_documents_type ON dsr_core.documents(document_type);
CREATE INDEX idx_documents_uploaded_at ON dsr_core.documents(uploaded_at);

CREATE INDEX idx_life_events_household_id ON dsr_core.life_events(household_id);
CREATE INDEX idx_life_events_member_id ON dsr_core.life_events(member_id);
CREATE INDEX idx_life_events_type ON dsr_core.life_events(event_type);

-- Add comments
COMMENT ON TABLE dsr_core.households IS 'Core household information and registration data';
COMMENT ON TABLE dsr_core.household_members IS 'Individual members of registered households';
COMMENT ON TABLE dsr_core.household_addresses IS 'Address information for households';
COMMENT ON TABLE dsr_core.contact_information IS 'Contact details for households';
COMMENT ON TABLE dsr_core.registrations IS 'Registration applications and their status';
COMMENT ON TABLE dsr_core.verification_info IS 'Verification details for registrations';
COMMENT ON TABLE dsr_core.documents IS 'Uploaded documents for registrations';
COMMENT ON TABLE dsr_core.life_events IS 'Significant life events affecting household status';
