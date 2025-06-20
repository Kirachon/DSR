-- Philippine Dynamic Social Registry (DSR) - Core Database Schema
-- Version: 3.0.0
-- Author: DSR Development Team
-- Date: 2024-12-20

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pg_trgm for text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Enable btree_gin for composite indexes
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- Create schemas
CREATE SCHEMA IF NOT EXISTS dsr_core;
CREATE SCHEMA IF NOT EXISTS dsr_audit;
CREATE SCHEMA IF NOT EXISTS dsr_analytics;

-- Set search path
SET search_path TO dsr_core, public;

-- =====================================================
-- CORE TABLES
-- =====================================================

-- Household Profiles Table
CREATE TABLE household_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id VARCHAR(20) UNIQUE NOT NULL,
    head_of_household_psn VARCHAR(16) NOT NULL,
    socio_economic_score DECIMAL(5,2),
    registration_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    data_quality_score DECIMAL(5,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_household_id_format CHECK (household_id ~ '^HH-[0-9]{4}-[0-9]{8}$'),
    CONSTRAINT chk_psn_format CHECK (head_of_household_psn ~ '^[0-9]{16}$'),
    CONSTRAINT chk_status_values CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'ARCHIVED')),
    CONSTRAINT chk_score_range CHECK (socio_economic_score >= 0 AND socio_economic_score <= 100),
    CONSTRAINT chk_quality_range CHECK (data_quality_score >= 0 AND data_quality_score <= 100)
);

-- Household Addresses Table
CREATE TABLE household_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES household_profiles(id),
    address_type VARCHAR(20) NOT NULL DEFAULT 'CURRENT',
    region VARCHAR(10) NOT NULL,
    province VARCHAR(100) NOT NULL,
    municipality VARCHAR(100) NOT NULL,
    barangay VARCHAR(100) NOT NULL,
    street_address VARCHAR(200),
    zip_code VARCHAR(4),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    accuracy_meters INTEGER,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_address_type CHECK (address_type IN ('PERMANENT', 'CURRENT', 'TEMPORARY')),
    CONSTRAINT chk_region_values CHECK (region IN ('NCR', 'CAR', 'I', 'II', 'III', 'IV-A', 'IV-B', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII', 'XIII', 'BARMM')),
    CONSTRAINT chk_zip_format CHECK (zip_code IS NULL OR zip_code ~ '^[0-9]{4}$'),
    CONSTRAINT chk_verification_status CHECK (verification_status IN ('VERIFIED', 'UNVERIFIED', 'DISPUTED')),
    CONSTRAINT chk_latitude_range CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_longitude_range CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

-- Household Members Table
CREATE TABLE household_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES household_profiles(id),
    psn VARCHAR(16) NOT NULL,
    relationship_to_head VARCHAR(20) NOT NULL,
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    civil_status VARCHAR(20),
    education_level VARCHAR(30),
    employment_status VARCHAR(30),
    occupation VARCHAR(100),
    monthly_income DECIMAL(12,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_joined DATE NOT NULL DEFAULT CURRENT_DATE,
    date_left DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_psn_format CHECK (psn ~ '^[0-9]{16}$'),
    CONSTRAINT chk_relationship CHECK (relationship_to_head IN ('HEAD', 'SPOUSE', 'CHILD', 'PARENT', 'GRANDPARENT', 'GRANDCHILD', 'SIBLING', 'IN_LAW', 'RELATIVE', 'NON_RELATIVE')),
    CONSTRAINT chk_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT chk_civil_status CHECK (civil_status IN ('SINGLE', 'MARRIED', 'WIDOWED', 'SEPARATED', 'DIVORCED', 'LIVE_IN')),
    CONSTRAINT chk_education_level CHECK (education_level IN ('NO_FORMAL_EDUCATION', 'ELEMENTARY_UNDERGRADUATE', 'ELEMENTARY_GRADUATE', 'HIGH_SCHOOL_UNDERGRADUATE', 'HIGH_SCHOOL_GRADUATE', 'VOCATIONAL', 'COLLEGE_UNDERGRADUATE', 'COLLEGE_GRADUATE', 'POST_GRADUATE')),
    CONSTRAINT chk_employment_status CHECK (employment_status IN ('EMPLOYED_FULL_TIME', 'EMPLOYED_PART_TIME', 'SELF_EMPLOYED', 'UNEMPLOYED', 'STUDENT', 'RETIRED', 'DISABLED', 'HOMEMAKER')),
    CONSTRAINT chk_income_positive CHECK (monthly_income IS NULL OR monthly_income >= 0),
    CONSTRAINT chk_birth_date_valid CHECK (birth_date <= CURRENT_DATE),
    CONSTRAINT chk_date_consistency CHECK (date_left IS NULL OR date_left >= date_joined)
);

-- Member Special Conditions Table
CREATE TABLE member_special_conditions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES household_members(id),
    condition_type VARCHAR(30) NOT NULL,
    condition_details JSONB,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_condition_type CHECK (condition_type IN ('PWD', 'SENIOR_CITIZEN', 'PREGNANT', 'LACTATING', 'SOLO_PARENT', 'INDIGENOUS', 'OVERSEAS_WORKER', 'STUDENT')),
    CONSTRAINT chk_date_consistency CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Economic Profiles Table
CREATE TABLE economic_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    household_id UUID NOT NULL REFERENCES household_profiles(id),
    total_household_income DECIMAL(12,2),
    land_ownership VARCHAR(20),
    house_type VARCHAR(20),
    assessment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    assessment_method VARCHAR(30) NOT NULL DEFAULT 'SELF_REPORTED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_income_positive CHECK (total_household_income IS NULL OR total_household_income >= 0),
    CONSTRAINT chk_land_ownership CHECK (land_ownership IN ('OWNED', 'RENTED', 'SHARED', 'SQUATTER', 'OTHER')),
    CONSTRAINT chk_house_type CHECK (house_type IN ('CONCRETE', 'SEMI_CONCRETE', 'WOOD', 'BAMBOO', 'MAKESHIFT', 'APARTMENT', 'CONDOMINIUM')),
    CONSTRAINT chk_assessment_method CHECK (assessment_method IN ('SELF_REPORTED', 'VERIFIED', 'SURVEYED', 'ESTIMATED'))
);

-- Income Sources Table
CREATE TABLE income_sources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    economic_profile_id UUID NOT NULL REFERENCES economic_profiles(id),
    source_type VARCHAR(30) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_source_type CHECK (source_type IN ('EMPLOYMENT', 'BUSINESS', 'REMITTANCES', 'GOVERNMENT_ASSISTANCE', 'PENSION', 'INVESTMENTS', 'AGRICULTURE', 'OTHER')),
    CONSTRAINT chk_frequency CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY', 'IRREGULAR')),
    CONSTRAINT chk_amount_positive CHECK (amount >= 0)
);

-- Assets Table
CREATE TABLE household_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    economic_profile_id UUID NOT NULL REFERENCES economic_profiles(id),
    asset_type VARCHAR(30) NOT NULL,
    asset_subtype VARCHAR(50),
    estimated_value DECIMAL(12,2),
    acquisition_year INTEGER,
    description VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_asset_type CHECK (asset_type IN ('VEHICLE', 'APPLIANCE', 'LIVESTOCK', 'LAND', 'BUILDING', 'FINANCIAL', 'OTHER')),
    CONSTRAINT chk_value_positive CHECK (estimated_value IS NULL OR estimated_value >= 0),
    CONSTRAINT chk_year_valid CHECK (acquisition_year IS NULL OR (acquisition_year >= 1900 AND acquisition_year <= EXTRACT(YEAR FROM CURRENT_DATE)))
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Primary lookup indexes
CREATE INDEX idx_household_profiles_household_id ON household_profiles(household_id);
CREATE INDEX idx_household_profiles_psn ON household_profiles(head_of_household_psn);
CREATE INDEX idx_household_profiles_status ON household_profiles(status) WHERE NOT deleted;
CREATE INDEX idx_household_profiles_score ON household_profiles(socio_economic_score) WHERE NOT deleted;

-- Member lookup indexes
CREATE INDEX idx_household_members_household_id ON household_members(household_id);
CREATE INDEX idx_household_members_psn ON household_members(psn);
CREATE INDEX idx_household_members_active ON household_members(household_id, is_active) WHERE NOT deleted;

-- Address lookup indexes
CREATE INDEX idx_household_addresses_household_id ON household_addresses(household_id);
CREATE INDEX idx_household_addresses_region ON household_addresses(region) WHERE NOT deleted;
CREATE INDEX idx_household_addresses_location ON household_addresses(region, province, municipality) WHERE NOT deleted;

-- Geospatial index for location-based queries
CREATE INDEX idx_household_addresses_coordinates ON household_addresses USING GIST(ll_to_earth(latitude, longitude)) WHERE latitude IS NOT NULL AND longitude IS NOT NULL AND NOT deleted;

-- Text search indexes
CREATE INDEX idx_household_members_name_search ON household_members USING GIN(to_tsvector('english', COALESCE(first_name, '') || ' ' || COALESCE(middle_name, '') || ' ' || COALESCE(last_name, ''))) WHERE NOT deleted;

-- Composite indexes for common queries
CREATE INDEX idx_household_profiles_status_score ON household_profiles(status, socio_economic_score) WHERE NOT deleted;
CREATE INDEX idx_household_members_household_relationship ON household_members(household_id, relationship_to_head) WHERE NOT deleted;

-- =====================================================
-- AUDIT TRIGGERS
-- =====================================================

-- Create audit schema tables
CREATE TABLE dsr_audit.audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_name VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    operation VARCHAR(10) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changed_fields TEXT[],
    user_id VARCHAR(100),
    user_role VARCHAR(50),
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_operation CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE'))
);

-- Create audit trigger function
CREATE OR REPLACE FUNCTION dsr_audit.audit_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
    old_data JSONB;
    new_data JSONB;
    changed_fields TEXT[] := '{}';
    field_name TEXT;
BEGIN
    -- Determine operation type and data
    IF TG_OP = 'DELETE' THEN
        old_data := to_jsonb(OLD);
        new_data := NULL;
    ELSIF TG_OP = 'INSERT' THEN
        old_data := NULL;
        new_data := to_jsonb(NEW);
    ELSIF TG_OP = 'UPDATE' THEN
        old_data := to_jsonb(OLD);
        new_data := to_jsonb(NEW);
        
        -- Identify changed fields
        FOR field_name IN SELECT jsonb_object_keys(new_data) LOOP
            IF old_data->field_name IS DISTINCT FROM new_data->field_name THEN
                changed_fields := array_append(changed_fields, field_name);
            END IF;
        END LOOP;
    END IF;

    -- Insert audit record
    INSERT INTO dsr_audit.audit_log (
        table_name,
        record_id,
        operation,
        old_values,
        new_values,
        changed_fields,
        user_id,
        timestamp
    ) VALUES (
        TG_TABLE_NAME,
        COALESCE(NEW.id, OLD.id),
        TG_OP,
        old_data,
        new_data,
        changed_fields,
        current_setting('app.current_user_id', true),
        NOW()
    );

    -- Return appropriate record
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create audit triggers for all main tables
CREATE TRIGGER audit_household_profiles
    AFTER INSERT OR UPDATE OR DELETE ON household_profiles
    FOR EACH ROW EXECUTE FUNCTION dsr_audit.audit_trigger_function();

CREATE TRIGGER audit_household_members
    AFTER INSERT OR UPDATE OR DELETE ON household_members
    FOR EACH ROW EXECUTE FUNCTION dsr_audit.audit_trigger_function();

CREATE TRIGGER audit_household_addresses
    AFTER INSERT OR UPDATE OR DELETE ON household_addresses
    FOR EACH ROW EXECUTE FUNCTION dsr_audit.audit_trigger_function();

CREATE TRIGGER audit_economic_profiles
    AFTER INSERT OR UPDATE OR DELETE ON economic_profiles
    FOR EACH ROW EXECUTE FUNCTION dsr_audit.audit_trigger_function();

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON SCHEMA dsr_core IS 'Core DSR database schema containing household and member data';
COMMENT ON SCHEMA dsr_audit IS 'Audit trail schema for tracking all data changes';

COMMENT ON TABLE household_profiles IS 'Main household profile information';
COMMENT ON TABLE household_members IS 'Individual members within households';
COMMENT ON TABLE household_addresses IS 'Address information for households';
COMMENT ON TABLE economic_profiles IS 'Economic and asset information for households';
COMMENT ON TABLE dsr_audit.audit_log IS 'Comprehensive audit trail for all data changes';
