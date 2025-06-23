-- Philippine Dynamic Social Registry (DSR) - Local Development Database Initialization
-- Version: 1.0.0
-- Author: DSR Development Team
-- Purpose: Initialize local development database with sample data

-- Set client encoding and timezone
SET client_encoding = 'UTF8';
SET timezone = 'Asia/Manila';

-- Connect to the DSR database
\c dsr_local;

-- Create application users for local development
DO $$
BEGIN
    -- Create service users
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_registration_user') THEN
        CREATE ROLE dsr_registration_user WITH LOGIN PASSWORD 'reg_local_pass';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_data_mgmt_user') THEN
        CREATE ROLE dsr_data_mgmt_user WITH LOGIN PASSWORD 'data_local_pass';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_eligibility_user') THEN
        CREATE ROLE dsr_eligibility_user WITH LOGIN PASSWORD 'elig_local_pass';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_interop_user') THEN
        CREATE ROLE dsr_interop_user WITH LOGIN PASSWORD 'interop_local_pass';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_payment_user') THEN
        CREATE ROLE dsr_payment_user WITH LOGIN PASSWORD 'payment_local_pass';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_grievance_user') THEN
        CREATE ROLE dsr_grievance_user WITH LOGIN PASSWORD 'grievance_local_pass';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_analytics_user') THEN
        CREATE ROLE dsr_analytics_user WITH LOGIN PASSWORD 'analytics_local_pass';
    END IF;
    
    -- Create read-only user for reporting
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dsr_readonly_user') THEN
        CREATE ROLE dsr_readonly_user WITH LOGIN PASSWORD 'readonly_local_pass';
    END IF;
END
$$;

-- Grant permissions to service users
GRANT USAGE ON SCHEMA dsr_core TO dsr_registration_user, dsr_data_mgmt_user, dsr_eligibility_user, 
                                  dsr_interop_user, dsr_payment_user, dsr_grievance_user, dsr_analytics_user;

GRANT USAGE ON SCHEMA dsr_audit TO dsr_registration_user, dsr_data_mgmt_user, dsr_eligibility_user, 
                                   dsr_interop_user, dsr_payment_user, dsr_grievance_user, dsr_analytics_user;

-- Grant table permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA dsr_core TO dsr_registration_user, dsr_data_mgmt_user;
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA dsr_core TO dsr_eligibility_user, dsr_interop_user, dsr_payment_user, dsr_grievance_user;
GRANT SELECT ON ALL TABLES IN SCHEMA dsr_core TO dsr_analytics_user, dsr_readonly_user;

-- Grant sequence permissions
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA dsr_core TO dsr_registration_user, dsr_data_mgmt_user, 
                                                          dsr_eligibility_user, dsr_interop_user, 
                                                          dsr_payment_user, dsr_grievance_user;

-- Grant audit permissions
GRANT INSERT ON ALL TABLES IN SCHEMA dsr_audit TO dsr_registration_user, dsr_data_mgmt_user, dsr_eligibility_user, 
                                                  dsr_interop_user, dsr_payment_user, dsr_grievance_user, dsr_analytics_user;

-- Create local development configuration table
CREATE TABLE IF NOT EXISTS dsr_core.local_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Insert local development configuration
INSERT INTO dsr_core.local_config (config_key, config_value, description) VALUES
('environment', 'local', 'Current environment'),
('philsys_mock_enabled', 'true', 'Enable PhilSys mock service'),
('notification_mock_enabled', 'true', 'Enable notification mock services'),
('sample_data_loaded', 'false', 'Flag to track if sample data has been loaded'),
('debug_mode', 'true', 'Enable debug mode for local development'),
('auto_approve_registrations', 'true', 'Auto-approve registrations in local environment'),
('skip_document_validation', 'true', 'Skip document validation in local environment'),
('enable_test_endpoints', 'true', 'Enable test endpoints for local development')
ON CONFLICT (config_key) DO NOTHING;

-- Create test data tracking table
CREATE TABLE IF NOT EXISTS dsr_core.test_data_tracking (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    data_type VARCHAR(50) NOT NULL,
    record_count INTEGER NOT NULL DEFAULT 0,
    last_generated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    generation_notes TEXT
);

-- Create local development indexes for better performance
CREATE INDEX IF NOT EXISTS idx_household_profiles_local_dev ON dsr_core.household_profiles(registration_date, status);
CREATE INDEX IF NOT EXISTS idx_household_members_local_dev ON dsr_core.household_members(psn, is_active);
CREATE INDEX IF NOT EXISTS idx_audit_log_local_dev ON dsr_audit.audit_log(timestamp, table_name);

-- Create function to reset local data
CREATE OR REPLACE FUNCTION dsr_core.reset_local_data()
RETURNS void AS $$
BEGIN
    -- Disable triggers temporarily
    SET session_replication_role = replica;
    
    -- Clear test data (preserve schema)
    TRUNCATE TABLE dsr_core.household_members CASCADE;
    TRUNCATE TABLE dsr_core.household_addresses CASCADE;
    TRUNCATE TABLE dsr_core.economic_profiles CASCADE;
    TRUNCATE TABLE dsr_core.household_profiles CASCADE;
    TRUNCATE TABLE dsr_audit.audit_log CASCADE;
    TRUNCATE TABLE dsr_core.test_data_tracking CASCADE;
    
    -- Re-enable triggers
    SET session_replication_role = DEFAULT;
    
    -- Update configuration
    UPDATE dsr_core.local_config 
    SET config_value = 'false', updated_at = NOW() 
    WHERE config_key = 'sample_data_loaded';
    
    RAISE NOTICE 'Local development data has been reset successfully';
END;
$$ LANGUAGE plpgsql;

-- Create function to generate test PSN numbers
CREATE OR REPLACE FUNCTION dsr_core.generate_test_psn()
RETURNS VARCHAR(16) AS $$
DECLARE
    test_psn VARCHAR(16);
    counter INTEGER := 0;
BEGIN
    LOOP
        -- Generate test PSN with format: 9999-XXXX-XXXX-XXXX (9999 prefix for test data)
        test_psn := '9999-' || 
                   LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || '-' ||
                   LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || '-' ||
                   LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0');
        
        -- Check if PSN already exists
        IF NOT EXISTS (SELECT 1 FROM dsr_core.household_members WHERE psn = test_psn) THEN
            RETURN test_psn;
        END IF;
        
        counter := counter + 1;
        IF counter > 1000 THEN
            RAISE EXCEPTION 'Unable to generate unique test PSN after 1000 attempts';
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Create function to validate local environment
CREATE OR REPLACE FUNCTION dsr_core.validate_local_environment()
RETURNS TABLE(check_name TEXT, status TEXT, details TEXT) AS $$
BEGIN
    -- Check if we're in local environment
    RETURN QUERY
    SELECT 'Environment Check'::TEXT, 
           CASE WHEN config_value = 'local' THEN 'PASS' ELSE 'FAIL' END::TEXT,
           'Current environment: ' || COALESCE(config_value, 'unknown')::TEXT
    FROM dsr_core.local_config WHERE config_key = 'environment';
    
    -- Check database connections
    RETURN QUERY
    SELECT 'Database Connection'::TEXT, 'PASS'::TEXT, 'Database connection successful'::TEXT;
    
    -- Check schemas
    RETURN QUERY
    SELECT 'Schema Check'::TEXT,
           CASE WHEN COUNT(*) >= 2 THEN 'PASS' ELSE 'FAIL' END::TEXT,
           'Found ' || COUNT(*)::TEXT || ' schemas (dsr_core, dsr_audit)'::TEXT
    FROM information_schema.schemata 
    WHERE schema_name IN ('dsr_core', 'dsr_audit');
    
    -- Check tables
    RETURN QUERY
    SELECT 'Table Check'::TEXT,
           CASE WHEN COUNT(*) >= 5 THEN 'PASS' ELSE 'FAIL' END::TEXT,
           'Found ' || COUNT(*)::TEXT || ' core tables'::TEXT
    FROM information_schema.tables 
    WHERE table_schema = 'dsr_core' AND table_type = 'BASE TABLE';
    
    -- Check extensions
    RETURN QUERY
    SELECT 'Extensions Check'::TEXT,
           CASE WHEN COUNT(*) >= 3 THEN 'PASS' ELSE 'FAIL' END::TEXT,
           'Found ' || COUNT(*)::TEXT || ' required extensions'::TEXT
    FROM pg_extension 
    WHERE extname IN ('uuid-ossp', 'pg_trgm', 'btree_gin');
END;
$$ LANGUAGE plpgsql;

-- Log initialization completion
INSERT INTO dsr_audit.audit_log (table_name, record_id, operation, new_values, user_id, user_role)
VALUES ('local_config', uuid_generate_v4(), 'INSERT', 
        '{"action": "local_database_initialized", "timestamp": "' || NOW()::TEXT || '"}',
        'system', 'initialization');

-- Create sample data generation functions
CREATE OR REPLACE FUNCTION dsr_core.generate_sample_households(num_households INTEGER DEFAULT 50)
RETURNS void AS $$
DECLARE
    i INTEGER;
    household_id UUID;
    head_psn VARCHAR(16);
    provinces TEXT[] := ARRAY['Metro Manila', 'Cebu', 'Davao', 'Iloilo', 'Cagayan de Oro', 'Baguio', 'Zamboanga', 'Bacolod'];
    cities TEXT[] := ARRAY['Quezon City', 'Manila', 'Caloocan', 'Davao City', 'Cebu City', 'Zamboanga City', 'Antipolo', 'Taguig'];
    barangays TEXT[] := ARRAY['Barangay 1', 'Barangay 2', 'Barangay 3', 'San Antonio', 'San Jose', 'Santa Maria', 'Poblacion', 'Centro'];
BEGIN
    FOR i IN 1..num_households LOOP
        household_id := uuid_generate_v4();
        head_psn := dsr_core.generate_test_psn();

        -- Insert household profile
        INSERT INTO dsr_core.household_profiles (
            id, psn, household_head_name, registration_date, status,
            verification_status, data_source, created_by
        ) VALUES (
            household_id,
            head_psn,
            'Test Household Head ' || i,
            CURRENT_DATE - (RANDOM() * 365)::INTEGER,
            CASE WHEN RANDOM() < 0.8 THEN 'active' ELSE 'pending' END,
            CASE WHEN RANDOM() < 0.7 THEN 'verified' ELSE 'pending' END,
            'local_test_data',
            'system_generator'
        );

        -- Insert household address
        INSERT INTO dsr_core.household_addresses (
            id, household_id, address_type, house_number, street,
            barangay, city_municipality, province, postal_code,
            is_primary, created_by
        ) VALUES (
            uuid_generate_v4(),
            household_id,
            'residential',
            (FLOOR(RANDOM() * 999) + 1)::TEXT,
            'Test Street ' || i,
            barangays[FLOOR(RANDOM() * array_length(barangays, 1)) + 1],
            cities[FLOOR(RANDOM() * array_length(cities, 1)) + 1],
            provinces[FLOOR(RANDOM() * array_length(provinces, 1)) + 1],
            (FLOOR(RANDOM() * 9000) + 1000)::TEXT,
            true,
            'system_generator'
        );

        -- Insert household head as member
        INSERT INTO dsr_core.household_members (
            id, household_id, psn, relationship_to_head,
            first_name, last_name, birth_date, gender,
            civil_status, education_level, employment_status,
            monthly_income, created_by
        ) VALUES (
            uuid_generate_v4(),
            household_id,
            head_psn,
            'head',
            'Juan' || i,
            'Dela Cruz' || i,
            CURRENT_DATE - (RANDOM() * 18250 + 6570)::INTEGER, -- Age 18-68
            CASE WHEN RANDOM() < 0.5 THEN 'male' ELSE 'female' END,
            CASE WHEN RANDOM() < 0.6 THEN 'married' ELSE 'single' END,
            CASE
                WHEN RANDOM() < 0.3 THEN 'elementary'
                WHEN RANDOM() < 0.6 THEN 'high_school'
                WHEN RANDOM() < 0.8 THEN 'college'
                ELSE 'vocational'
            END,
            CASE
                WHEN RANDOM() < 0.4 THEN 'employed'
                WHEN RANDOM() < 0.7 THEN 'self_employed'
                ELSE 'unemployed'
            END,
            CASE
                WHEN RANDOM() < 0.3 THEN 15000 + (RANDOM() * 10000)
                WHEN RANDOM() < 0.6 THEN 8000 + (RANDOM() * 7000)
                ELSE 3000 + (RANDOM() * 5000)
            END,
            'system_generator'
        );

        -- Add 2-5 additional family members
        PERFORM dsr_core.generate_family_members(household_id, FLOOR(RANDOM() * 4) + 2);

        -- Insert economic profile
        INSERT INTO dsr_core.economic_profiles (
            id, household_id, monthly_income, income_sources,
            has_savings_account, has_formal_employment,
            poverty_status, created_by
        ) VALUES (
            uuid_generate_v4(),
            household_id,
            (SELECT SUM(monthly_income) FROM dsr_core.household_members WHERE household_id = household_profiles.id),
            ARRAY['employment', 'business', 'remittances'][1:FLOOR(RANDOM() * 3) + 1],
            RANDOM() < 0.4,
            RANDOM() < 0.5,
            CASE
                WHEN RANDOM() < 0.3 THEN 'poor'
                WHEN RANDOM() < 0.6 THEN 'near_poor'
                ELSE 'non_poor'
            END,
            'system_generator'
        ) FROM dsr_core.household_profiles WHERE id = household_id;
    END LOOP;

    -- Update tracking
    INSERT INTO dsr_core.test_data_tracking (data_type, record_count, generation_notes)
    VALUES ('households', num_households, 'Generated ' || num_households || ' sample households with members and addresses');

    RAISE NOTICE 'Generated % sample households with members and addresses', num_households;
END;
$$ LANGUAGE plpgsql;

-- Display initialization summary
DO $$
BEGIN
    RAISE NOTICE '=== DSR Local Development Database Initialized ===';
    RAISE NOTICE 'Database: dsr_local';
    RAISE NOTICE 'Environment: local development';
    RAISE NOTICE 'Service users created: 7';
    RAISE NOTICE 'Configuration entries: %', (SELECT COUNT(*) FROM dsr_core.local_config);
    RAISE NOTICE 'Ready for sample data loading';
    RAISE NOTICE 'Use: SELECT dsr_core.generate_sample_households(50); to create test data';
    RAISE NOTICE '================================================';
END
$$;
