-- Philippine Dynamic Social Registry (DSR) - Load Sample Data
-- Version: 1.0.0
-- Author: DSR Development Team
-- Purpose: Load sample data for local development and testing

-- Set client encoding and timezone
SET client_encoding = 'UTF8';
SET timezone = 'Asia/Manila';

-- Connect to the DSR database
\c dsr_local;

-- Check if we should load sample data
DO $$
DECLARE
    should_load BOOLEAN := FALSE;
    load_config TEXT;
BEGIN
    -- Check configuration
    SELECT config_value INTO load_config 
    FROM dsr_core.local_config 
    WHERE config_key = 'sample_data_loaded';
    
    IF load_config IS NULL OR load_config = 'false' THEN
        should_load := TRUE;
    END IF;
    
    IF should_load THEN
        RAISE NOTICE 'Loading sample data for local development...';
        
        -- Generate comprehensive sample data
        -- 50 households for faster local development
        PERFORM dsr_core.generate_comprehensive_sample_data(50, true, true);
        
        RAISE NOTICE 'Sample data loaded successfully!';
        RAISE NOTICE '';
        RAISE NOTICE '=== Sample Data Summary ===';
        RAISE NOTICE 'Households: %', (SELECT COUNT(*) FROM dsr_core.household_profiles);
        RAISE NOTICE 'Members: %', (SELECT COUNT(*) FROM dsr_core.household_members);
        RAISE NOTICE 'Addresses: %', (SELECT COUNT(*) FROM dsr_core.household_addresses);
        RAISE NOTICE 'Economic Profiles: %', (SELECT COUNT(*) FROM dsr_core.economic_profiles);
        
        -- Check if program eligibility table exists and show count
        IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'dsr_core' AND table_name = 'program_eligibility') THEN
            RAISE NOTICE 'Program Eligibilities: %', (SELECT COUNT(*) FROM dsr_core.program_eligibility);
        END IF;
        
        -- Check if service delivery table exists and show count
        IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'dsr_core' AND table_name = 'service_delivery') THEN
            RAISE NOTICE 'Service Deliveries: %', (SELECT COUNT(*) FROM dsr_core.service_delivery);
        END IF;
        
        RAISE NOTICE '===========================';
        
    ELSE
        RAISE NOTICE 'Sample data already loaded. Skipping data generation.';
        RAISE NOTICE 'To reload data, run: SELECT dsr_core.reset_local_data(); then restart the database.';
    END IF;
END
$$;

-- Create some test user accounts for local development
DO $$
BEGIN
    -- Create test users table if not exists
    CREATE TABLE IF NOT EXISTS dsr_core.test_users (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        username VARCHAR(100) NOT NULL UNIQUE,
        email VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        role VARCHAR(50) NOT NULL,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
        last_login TIMESTAMP WITH TIME ZONE
    );
    
    -- Insert test users (using simple hashing for local development only)
    INSERT INTO dsr_core.test_users (username, email, password_hash, role) VALUES
    ('admin', 'admin@dsr.local', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'ADMIN'),
    ('caseworker', 'caseworker@dsr.local', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'CASEWORKER'),
    ('supervisor', 'supervisor@dsr.local', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'SUPERVISOR'),
    ('analyst', 'analyst@dsr.local', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'ANALYST'),
    ('citizen', 'citizen@dsr.local', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'CITIZEN')
    ON CONFLICT (username) DO NOTHING;
    
    RAISE NOTICE 'Test user accounts created:';
    RAISE NOTICE '  admin@dsr.local (password: admin123)';
    RAISE NOTICE '  caseworker@dsr.local (password: admin123)';
    RAISE NOTICE '  supervisor@dsr.local (password: admin123)';
    RAISE NOTICE '  analyst@dsr.local (password: admin123)';
    RAISE NOTICE '  citizen@dsr.local (password: admin123)';
END
$$;

-- Create test API keys for local development
DO $$
BEGIN
    -- Create API keys table if not exists
    CREATE TABLE IF NOT EXISTS dsr_core.api_keys (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        key_name VARCHAR(100) NOT NULL,
        api_key VARCHAR(255) NOT NULL UNIQUE,
        service_name VARCHAR(100) NOT NULL,
        permissions TEXT[] DEFAULT ARRAY[]::TEXT[],
        is_active BOOLEAN DEFAULT TRUE,
        expires_at TIMESTAMP WITH TIME ZONE,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
        created_by VARCHAR(100) DEFAULT 'system'
    );
    
    -- Insert test API keys
    INSERT INTO dsr_core.api_keys (key_name, api_key, service_name, permissions) VALUES
    ('Local Registration Service', 'reg_local_key_12345', 'registration-service', ARRAY['read', 'write', 'delete']),
    ('Local Data Management', 'data_local_key_12345', 'data-management-service', ARRAY['read', 'write']),
    ('Local Eligibility Service', 'elig_local_key_12345', 'eligibility-service', ARRAY['read', 'write']),
    ('Local Payment Service', 'pay_local_key_12345', 'payment-service', ARRAY['read', 'write']),
    ('Local Analytics Service', 'analytics_local_key_12345', 'analytics-service', ARRAY['read']),
    ('Test Client Application', 'test_client_key_12345', 'test-client', ARRAY['read'])
    ON CONFLICT (api_key) DO NOTHING;
    
    RAISE NOTICE 'Test API keys created for local services';
END
$$;

-- Create sample notification templates
DO $$
BEGIN
    -- Create notification templates table if not exists
    CREATE TABLE IF NOT EXISTS dsr_core.notification_templates (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        template_name VARCHAR(100) NOT NULL UNIQUE,
        template_type VARCHAR(50) NOT NULL,
        subject VARCHAR(255),
        content TEXT NOT NULL,
        variables JSONB,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );
    
    -- Insert sample notification templates
    INSERT INTO dsr_core.notification_templates (template_name, template_type, subject, content, variables) VALUES
    ('registration_welcome', 'email', 'Welcome to DSR - Registration Successful', 
     'Dear {{first_name}}, your registration with PSN {{psn}} has been successfully submitted. Reference: {{reference_number}}',
     '{"first_name": "string", "psn": "string", "reference_number": "string"}'),
    ('eligibility_approved', 'sms', NULL,
     'Congratulations! You are eligible for {{program_name}}. Visit your barangay office for next steps. Ref: {{reference}}',
     '{"program_name": "string", "reference": "string"}'),
    ('payment_notification', 'email', 'Payment Disbursement - {{program_name}}',
     'Dear {{first_name}}, your payment of PHP {{amount}} for {{program_name}} has been processed. Transaction ID: {{transaction_id}}',
     '{"first_name": "string", "amount": "number", "program_name": "string", "transaction_id": "string"}'),
    ('document_required', 'sms', NULL,
     'DSR: Additional documents required for your application. Please submit {{document_type}} within 30 days. Ref: {{reference}}',
     '{"document_type": "string", "reference": "string"}')
    ON CONFLICT (template_name) DO NOTHING;
    
    RAISE NOTICE 'Sample notification templates created';
END
$$;

-- Create sample grievance data
DO $$
BEGIN
    -- Create grievances table if not exists
    CREATE TABLE IF NOT EXISTS dsr_core.grievances (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        household_id UUID REFERENCES dsr_core.household_profiles(id),
        grievance_type VARCHAR(100) NOT NULL,
        subject VARCHAR(255) NOT NULL,
        description TEXT NOT NULL,
        status VARCHAR(50) DEFAULT 'open',
        priority VARCHAR(20) DEFAULT 'medium',
        assigned_to VARCHAR(100),
        resolution TEXT,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
        resolved_at TIMESTAMP WITH TIME ZONE,
        created_by VARCHAR(100) DEFAULT 'system'
    );
    
    -- Insert sample grievances
    INSERT INTO dsr_core.grievances (household_id, grievance_type, subject, description, status, priority)
    SELECT 
        id,
        CASE 
            WHEN RANDOM() < 0.3 THEN 'payment_issue'
            WHEN RANDOM() < 0.6 THEN 'eligibility_dispute'
            WHEN RANDOM() < 0.8 THEN 'data_correction'
            ELSE 'service_complaint'
        END,
        'Sample grievance for testing',
        'This is a sample grievance created for local development and testing purposes.',
        CASE 
            WHEN RANDOM() < 0.4 THEN 'open'
            WHEN RANDOM() < 0.7 THEN 'in_progress'
            WHEN RANDOM() < 0.9 THEN 'resolved'
            ELSE 'closed'
        END,
        CASE 
            WHEN RANDOM() < 0.2 THEN 'high'
            WHEN RANDOM() < 0.8 THEN 'medium'
            ELSE 'low'
        END
    FROM dsr_core.household_profiles 
    WHERE RANDOM() < 0.1  -- 10% of households have grievances
    LIMIT 5;
    
    RAISE NOTICE 'Sample grievance data created';
END
$$;

-- Final summary and instructions
DO $$
DECLARE
    household_count INTEGER;
    member_count INTEGER;
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO household_count FROM dsr_core.household_profiles;
    SELECT COUNT(*) INTO member_count FROM dsr_core.household_members;
    SELECT COUNT(*) INTO user_count FROM dsr_core.test_users;
    
    RAISE NOTICE '';
    RAISE NOTICE '=== DSR Local Development Environment Ready ===';
    RAISE NOTICE 'Database: dsr_local';
    RAISE NOTICE 'Sample Data Loaded: YES';
    RAISE NOTICE '';
    RAISE NOTICE 'Data Summary:';
    RAISE NOTICE '  - Households: %', household_count;
    RAISE NOTICE '  - Members: %', member_count;
    RAISE NOTICE '  - Test Users: %', user_count;
    RAISE NOTICE '  - API Keys: %', (SELECT COUNT(*) FROM dsr_core.api_keys);
    RAISE NOTICE '  - Notification Templates: %', (SELECT COUNT(*) FROM dsr_core.notification_templates);
    RAISE NOTICE '';
    RAISE NOTICE 'Test Credentials:';
    RAISE NOTICE '  - Admin: admin@dsr.local / admin123';
    RAISE NOTICE '  - Caseworker: caseworker@dsr.local / admin123';
    RAISE NOTICE '  - Citizen: citizen@dsr.local / admin123';
    RAISE NOTICE '';
    RAISE NOTICE 'Ready to start DSR services!';
    RAISE NOTICE '===============================================';
END
$$;
