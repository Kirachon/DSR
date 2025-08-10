-- Core DSR Business Tables
-- Registration, Households, and Core Entities

SET search_path TO dsr_core, dsr_auth, public;

-- Households table
CREATE TABLE households (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_number VARCHAR(50) UNIQUE NOT NULL,
    head_of_household_psn VARCHAR(16),
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status registration_status NOT NULL DEFAULT 'DRAFT',
    registration_channel registration_channel NOT NULL DEFAULT 'WEB_PORTAL',
    total_members INTEGER DEFAULT 0,
    monthly_income DECIMAL(12, 2),
    is_indigenous BOOLEAN DEFAULT FALSE,
    is_pwd_household BOOLEAN DEFAULT FALSE,
    is_senior_citizen_household BOOLEAN DEFAULT FALSE,
    consent_given BOOLEAN DEFAULT FALSE,
    consent_date TIMESTAMP WITH TIME ZONE,
    preferred_language VARCHAR(10) DEFAULT 'en',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES dsr_auth.users(id),
    updated_by UUID REFERENCES dsr_auth.users(id)
);

-- Household members table
CREATE TABLE household_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    psn VARCHAR(16) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    suffix VARCHAR(20),
    birth_date DATE NOT NULL,
    gender gender NOT NULL,
    civil_status civil_status NOT NULL,
    relationship_to_head relationship_type NOT NULL,
    is_head_of_household BOOLEAN DEFAULT FALSE,
    education_level education_level,
    employment_status employment_status,
    occupation VARCHAR(100),
    monthly_income DECIMAL(10, 2),
    is_pwd BOOLEAN DEFAULT FALSE,
    pwd_type VARCHAR(100),
    is_indigenous BOOLEAN DEFAULT FALSE,
    indigenous_group VARCHAR(100),
    is_solo_parent BOOLEAN DEFAULT FALSE,
    is_ofw BOOLEAN DEFAULT FALSE,
    health_conditions TEXT[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Household addresses table
CREATE TABLE household_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    address_type VARCHAR(20) NOT NULL DEFAULT 'CURRENT', -- CURRENT, PERMANENT, PREVIOUS
    street_address VARCHAR(255),
    barangay VARCHAR(100) NOT NULL,
    municipality VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    region VARCHAR(50) NOT NULL,
    zip_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'Philippines',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    housing_type VARCHAR(50), -- OWN, RENT, FREE, SHARED
    housing_material VARCHAR(50), -- CONCRETE, WOOD, BAMBOO, MIXED
    roof_material VARCHAR(50), -- CONCRETE, GI_SHEET, NIPA, MIXED
    has_electricity BOOLEAN DEFAULT FALSE,
    has_water_supply BOOLEAN DEFAULT FALSE,
    has_toilet BOOLEAN DEFAULT FALSE,
    water_source VARCHAR(50),
    toilet_type VARCHAR(50),
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Contact information table
CREATE TABLE contact_information (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    contact_type VARCHAR(20) NOT NULL, -- PRIMARY, SECONDARY, EMERGENCY
    contact_person_name VARCHAR(200),
    mobile_number VARCHAR(20),
    landline_number VARCHAR(20),
    email_address VARCHAR(255),
    preferred_contact_method VARCHAR(20) DEFAULT 'mobile', -- mobile, landline, email, sms
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Registrations table (tracks registration applications)
CREATE TABLE registrations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registration_number VARCHAR(50) UNIQUE NOT NULL,
    household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    applicant_user_id UUID REFERENCES dsr_auth.users(id),
    status registration_status NOT NULL DEFAULT 'DRAFT',
    registration_channel registration_channel NOT NULL DEFAULT 'WEB_PORTAL',
    submission_date TIMESTAMP WITH TIME ZONE,
    verification_date TIMESTAMP WITH TIME ZONE,
    approval_date TIMESTAMP WITH TIME ZONE,
    rejection_date TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    assigned_to UUID REFERENCES dsr_auth.users(id),
    priority_level INTEGER DEFAULT 3, -- 1=HIGH, 2=MEDIUM, 3=LOW
    estimated_completion_date DATE,
    completion_date DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES dsr_auth.users(id),
    updated_by UUID REFERENCES dsr_auth.users(id)
);

-- Verification information table
CREATE TABLE verification_info (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registration_id UUID NOT NULL REFERENCES registrations(id) ON DELETE CASCADE,
    verification_type VARCHAR(50) NOT NULL, -- PHILSYS, DOCUMENT, FIELD, BIOMETRIC
    verification_method VARCHAR(50), -- QR_CODE, MANUAL, API, VISIT
    verification_status verification_status NOT NULL DEFAULT 'PENDING',
    verification_date TIMESTAMP WITH TIME ZONE,
    verified_by UUID REFERENCES dsr_auth.users(id),
    verification_data JSONB, -- Store verification details
    expiry_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Documents table
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registration_id UUID REFERENCES registrations(id) ON DELETE CASCADE,
    household_member_id UUID REFERENCES household_members(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL, -- BIRTH_CERT, ID, PROOF_INCOME, etc.
    document_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by UUID REFERENCES dsr_auth.users(id),
    verified_at TIMESTAMP WITH TIME ZONE,
    expiry_date DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Life events table (track changes in household circumstances)
CREATE TABLE life_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    household_member_id UUID REFERENCES household_members(id) ON DELETE SET NULL,
    event_type life_event_type NOT NULL,
    event_date DATE NOT NULL,
    description TEXT,
    impact_on_eligibility BOOLEAN DEFAULT FALSE,
    requires_reassessment BOOLEAN DEFAULT FALSE,
    reported_by UUID REFERENCES dsr_auth.users(id),
    verified BOOLEAN DEFAULT FALSE,
    verified_by UUID REFERENCES dsr_auth.users(id),
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_households_number ON households(household_number);
CREATE INDEX idx_households_status ON households(status);
CREATE INDEX idx_households_head_psn ON households(head_of_household_psn);
CREATE INDEX idx_household_members_household_id ON household_members(household_id);
CREATE INDEX idx_household_members_psn ON household_members(psn);
CREATE INDEX idx_household_members_head ON household_members(is_head_of_household);
CREATE INDEX idx_household_addresses_household_id ON household_addresses(household_id);
CREATE INDEX idx_household_addresses_current ON household_addresses(is_current);
CREATE INDEX idx_contact_information_household_id ON contact_information(household_id);
CREATE INDEX idx_registrations_number ON registrations(registration_number);
CREATE INDEX idx_registrations_status ON registrations(status);
CREATE INDEX idx_registrations_household_id ON registrations(household_id);
CREATE INDEX idx_registrations_assigned_to ON registrations(assigned_to);
CREATE INDEX idx_verification_info_registration_id ON verification_info(registration_id);
CREATE INDEX idx_verification_info_status ON verification_info(verification_status);
CREATE INDEX idx_documents_registration_id ON documents(registration_id);
CREATE INDEX idx_documents_member_id ON documents(household_member_id);
CREATE INDEX idx_life_events_household_id ON life_events(household_id);
CREATE INDEX idx_life_events_member_id ON life_events(household_member_id);
CREATE INDEX idx_life_events_type ON life_events(event_type);

-- Add comments
COMMENT ON TABLE households IS 'Core household information and registration data';
COMMENT ON TABLE household_members IS 'Individual members of registered households';
COMMENT ON TABLE household_addresses IS 'Address information for households';
COMMENT ON TABLE contact_information IS 'Contact details for households';
COMMENT ON TABLE registrations IS 'Registration applications and their status';
COMMENT ON TABLE verification_info IS 'Verification details for registrations';
COMMENT ON TABLE documents IS 'Uploaded documents for registrations';
COMMENT ON TABLE life_events IS 'Significant life events affecting household status';
