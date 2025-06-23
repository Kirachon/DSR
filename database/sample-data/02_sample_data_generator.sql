-- Philippine Dynamic Social Registry (DSR) - Sample Data Generator
-- Version: 1.0.0
-- Author: DSR Development Team
-- Purpose: Generate comprehensive sample data for local development and testing

-- Set client encoding and timezone
SET client_encoding = 'UTF8';
SET timezone = 'Asia/Manila';

-- Connect to the DSR database
\c dsr_local;

-- Create helper function to generate family members
CREATE OR REPLACE FUNCTION dsr_core.generate_family_members(p_household_id UUID, p_member_count INTEGER)
RETURNS void AS $$
DECLARE
    i INTEGER;
    member_psn VARCHAR(16);
    relationships TEXT[] := ARRAY['spouse', 'child', 'child', 'child', 'parent', 'sibling', 'grandchild'];
    first_names_male TEXT[] := ARRAY['Juan', 'Jose', 'Antonio', 'Pedro', 'Manuel', 'Francisco', 'Ricardo', 'Roberto', 'Carlos', 'Miguel'];
    first_names_female TEXT[] := ARRAY['Maria', 'Ana', 'Carmen', 'Rosa', 'Elena', 'Patricia', 'Isabel', 'Teresa', 'Luz', 'Gloria'];
    last_names TEXT[] := ARRAY['Santos', 'Reyes', 'Cruz', 'Bautista', 'Ocampo', 'Garcia', 'Mendoza', 'Torres', 'Gonzales', 'Lopez'];
    gender_val TEXT;
    age_days INTEGER;
    relationship TEXT;
BEGIN
    FOR i IN 1..p_member_count LOOP
        member_psn := dsr_core.generate_test_psn();
        gender_val := CASE WHEN RANDOM() < 0.5 THEN 'male' ELSE 'female' END;
        relationship := relationships[FLOOR(RANDOM() * array_length(relationships, 1)) + 1];
        
        -- Adjust age based on relationship
        age_days := CASE 
            WHEN relationship = 'spouse' THEN RANDOM() * 18250 + 6570  -- 18-68 years
            WHEN relationship = 'child' THEN RANDOM() * 6570  -- 0-18 years
            WHEN relationship = 'parent' THEN RANDOM() * 10950 + 14600  -- 40-70 years
            WHEN relationship = 'grandchild' THEN RANDOM() * 3650  -- 0-10 years
            ELSE RANDOM() * 25550 + 5475  -- 15-85 years
        END;
        
        INSERT INTO dsr_core.household_members (
            id, household_id, psn, relationship_to_head,
            first_name, last_name, birth_date, gender,
            civil_status, education_level, employment_status,
            monthly_income, created_by
        ) VALUES (
            uuid_generate_v4(),
            p_household_id,
            member_psn,
            relationship,
            CASE 
                WHEN gender_val = 'male' THEN first_names_male[FLOOR(RANDOM() * array_length(first_names_male, 1)) + 1]
                ELSE first_names_female[FLOOR(RANDOM() * array_length(first_names_female, 1)) + 1]
            END,
            last_names[FLOOR(RANDOM() * array_length(last_names, 1)) + 1],
            CURRENT_DATE - age_days::INTEGER,
            gender_val,
            CASE 
                WHEN age_days < 6570 THEN 'single'  -- Under 18
                WHEN RANDOM() < 0.6 THEN 'married'
                WHEN RANDOM() < 0.8 THEN 'single'
                ELSE 'widowed'
            END,
            CASE 
                WHEN age_days < 2190 THEN 'none'  -- Under 6
                WHEN age_days < 3650 THEN 'elementary'  -- 6-10
                WHEN age_days < 6570 THEN 'high_school'  -- 10-18
                WHEN RANDOM() < 0.3 THEN 'elementary'
                WHEN RANDOM() < 0.6 THEN 'high_school'
                WHEN RANDOM() < 0.8 THEN 'college'
                ELSE 'vocational'
            END,
            CASE 
                WHEN age_days < 5475 THEN 'student'  -- Under 15
                WHEN age_days > 23725 THEN 'retired'  -- Over 65
                WHEN RANDOM() < 0.4 THEN 'employed'
                WHEN RANDOM() < 0.7 THEN 'self_employed'
                ELSE 'unemployed'
            END,
            CASE 
                WHEN age_days < 5475 OR age_days > 23725 THEN 0  -- Students and retirees
                WHEN RANDOM() < 0.3 THEN 12000 + (RANDOM() * 8000)
                WHEN RANDOM() < 0.6 THEN 6000 + (RANDOM() * 6000)
                ELSE RANDOM() * 6000
            END,
            'system_generator'
        );
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Create function to generate program eligibility data
CREATE OR REPLACE FUNCTION dsr_core.generate_program_eligibility_data()
RETURNS void AS $$
DECLARE
    household_rec RECORD;
    program_names TEXT[] := ARRAY['4Ps', 'DSWD-SLP', 'PhilHealth-UHC', 'DOLE-TUPAD', 'DA-RCEF', 'DepEd-ALS'];
    eligibility_status TEXT[] := ARRAY['eligible', 'not_eligible', 'pending_review', 'conditionally_eligible'];
BEGIN
    -- Create program eligibility table if not exists
    CREATE TABLE IF NOT EXISTS dsr_core.program_eligibility (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        household_id UUID NOT NULL REFERENCES dsr_core.household_profiles(id),
        program_name VARCHAR(100) NOT NULL,
        eligibility_status VARCHAR(50) NOT NULL,
        assessment_date DATE NOT NULL DEFAULT CURRENT_DATE,
        assessment_score DECIMAL(5,2),
        eligibility_criteria JSONB,
        valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
        valid_until DATE,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
        created_by VARCHAR(100) DEFAULT 'system_generator'
    );
    
    -- Generate eligibility records for each household
    FOR household_rec IN SELECT id FROM dsr_core.household_profiles LOOP
        -- Generate 2-4 program eligibilities per household
        FOR i IN 1..(FLOOR(RANDOM() * 3) + 2) LOOP
            INSERT INTO dsr_core.program_eligibility (
                household_id, program_name, eligibility_status,
                assessment_date, assessment_score, eligibility_criteria,
                valid_from, valid_until
            ) VALUES (
                household_rec.id,
                program_names[FLOOR(RANDOM() * array_length(program_names, 1)) + 1],
                eligibility_status[FLOOR(RANDOM() * array_length(eligibility_status, 1)) + 1],
                CURRENT_DATE - (RANDOM() * 180)::INTEGER,
                RANDOM() * 100,
                jsonb_build_object(
                    'income_threshold_met', RANDOM() < 0.7,
                    'geographic_eligibility', RANDOM() < 0.9,
                    'demographic_criteria', RANDOM() < 0.8,
                    'documentation_complete', RANDOM() < 0.6
                ),
                CURRENT_DATE - (RANDOM() * 30)::INTEGER,
                CURRENT_DATE + (RANDOM() * 365 + 365)::INTEGER
            );
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Generated program eligibility data for all households';
END;
$$ LANGUAGE plpgsql;

-- Create function to generate service delivery records
CREATE OR REPLACE FUNCTION dsr_core.generate_service_delivery_data()
RETURNS void AS $$
DECLARE
    household_rec RECORD;
    service_types TEXT[] := ARRAY['cash_transfer', 'food_assistance', 'health_service', 'education_support', 'livelihood_program'];
    delivery_status TEXT[] := ARRAY['delivered', 'pending', 'failed', 'cancelled'];
BEGIN
    -- Create service delivery table if not exists
    CREATE TABLE IF NOT EXISTS dsr_core.service_delivery (
        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        household_id UUID NOT NULL REFERENCES dsr_core.household_profiles(id),
        service_type VARCHAR(100) NOT NULL,
        service_provider VARCHAR(200) NOT NULL,
        delivery_date DATE,
        delivery_status VARCHAR(50) NOT NULL,
        amount DECIMAL(12,2),
        delivery_method VARCHAR(100),
        reference_number VARCHAR(100),
        delivery_details JSONB,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
        created_by VARCHAR(100) DEFAULT 'system_generator'
    );
    
    -- Generate service delivery records
    FOR household_rec IN SELECT id FROM dsr_core.household_profiles WHERE status = 'active' LOOP
        -- Generate 1-3 service delivery records per active household
        FOR i IN 1..(FLOOR(RANDOM() * 3) + 1) LOOP
            INSERT INTO dsr_core.service_delivery (
                household_id, service_type, service_provider,
                delivery_date, delivery_status, amount,
                delivery_method, reference_number, delivery_details
            ) VALUES (
                household_rec.id,
                service_types[FLOOR(RANDOM() * array_length(service_types, 1)) + 1],
                'DSWD Region ' || (FLOOR(RANDOM() * 13) + 1)::TEXT,
                CURRENT_DATE - (RANDOM() * 90)::INTEGER,
                delivery_status[FLOOR(RANDOM() * array_length(delivery_status, 1)) + 1],
                CASE 
                    WHEN RANDOM() < 0.5 THEN 1400 + (RANDOM() * 2600)  -- 1400-4000 PHP
                    ELSE 500 + (RANDOM() * 1500)  -- 500-2000 PHP
                END,
                CASE 
                    WHEN RANDOM() < 0.6 THEN 'bank_transfer'
                    WHEN RANDOM() < 0.8 THEN 'cash_card'
                    ELSE 'over_the_counter'
                END,
                'REF-' || LPAD((RANDOM() * 999999)::INTEGER::TEXT, 6, '0'),
                jsonb_build_object(
                    'delivery_location', 'Barangay Hall',
                    'beneficiary_present', RANDOM() < 0.9,
                    'id_verified', RANDOM() < 0.95,
                    'delivery_notes', 'Sample delivery for local testing'
                )
            );
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Generated service delivery data for active households';
END;
$$ LANGUAGE plpgsql;

-- Create comprehensive sample data generation function
CREATE OR REPLACE FUNCTION dsr_core.generate_comprehensive_sample_data(
    p_household_count INTEGER DEFAULT 100,
    p_include_programs BOOLEAN DEFAULT TRUE,
    p_include_services BOOLEAN DEFAULT TRUE
)
RETURNS void AS $$
BEGIN
    -- Check if sample data already exists
    IF EXISTS (SELECT 1 FROM dsr_core.local_config WHERE config_key = 'sample_data_loaded' AND config_value = 'true') THEN
        RAISE NOTICE 'Sample data already loaded. Use dsr_core.reset_local_data() to clear first.';
        RETURN;
    END IF;
    
    RAISE NOTICE 'Starting comprehensive sample data generation...';
    
    -- Generate households with members and addresses
    PERFORM dsr_core.generate_sample_households(p_household_count);
    
    -- Generate program eligibility data
    IF p_include_programs THEN
        PERFORM dsr_core.generate_program_eligibility_data();
    END IF;
    
    -- Generate service delivery data
    IF p_include_services THEN
        PERFORM dsr_core.generate_service_delivery_data();
    END IF;
    
    -- Update configuration
    UPDATE dsr_core.local_config 
    SET config_value = 'true', updated_at = NOW() 
    WHERE config_key = 'sample_data_loaded';
    
    -- Log completion
    INSERT INTO dsr_audit.audit_log (table_name, record_id, operation, new_values, user_id, user_role)
    VALUES ('test_data_tracking', uuid_generate_v4(), 'INSERT', 
            jsonb_build_object(
                'action', 'comprehensive_sample_data_generated',
                'household_count', p_household_count,
                'include_programs', p_include_programs,
                'include_services', p_include_services,
                'timestamp', NOW()
            ),
            'system', 'data_generator');
    
    RAISE NOTICE 'Comprehensive sample data generation completed successfully!';
    RAISE NOTICE 'Generated: % households with members, addresses, programs, and services', p_household_count;
END;
$$ LANGUAGE plpgsql;

-- Display sample data generation instructions
DO $$
BEGIN
    RAISE NOTICE '=== DSR Sample Data Generator Ready ===';
    RAISE NOTICE 'To generate sample data, run:';
    RAISE NOTICE '  SELECT dsr_core.generate_comprehensive_sample_data(100, true, true);';
    RAISE NOTICE '';
    RAISE NOTICE 'Parameters:';
    RAISE NOTICE '  - household_count: Number of households to generate (default: 100)';
    RAISE NOTICE '  - include_programs: Generate program eligibility data (default: true)';
    RAISE NOTICE '  - include_services: Generate service delivery data (default: true)';
    RAISE NOTICE '';
    RAISE NOTICE 'To reset data: SELECT dsr_core.reset_local_data();';
    RAISE NOTICE '==========================================';
END
$$;
